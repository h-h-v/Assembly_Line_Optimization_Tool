import joblib
import logging
import os
from flask import Flask, request, jsonify
from flask_cors import CORS
import pandas as pd

# Configure logging
logging.basicConfig(level=logging.INFO)

app = Flask(__name__)
CORS(app)

# Model paths
ERROR_MODEL_PATH = "linear_regression_model.pkl"
FAILURE_MODEL_PATH = "fail.pkl"
ENCODER_PATH = "label_encoder.pkl"

# Load error prediction model
if os.path.exists(ERROR_MODEL_PATH):
    try:
        error_model = joblib.load(ERROR_MODEL_PATH)
        logging.info("✅ Error prediction model loaded successfully.")
    except Exception as e:
        error_model = None
        logging.error(f"🚨 Error loading error prediction model: {e}")
else:
    error_model = None
    logging.error(f"🚨 Model file {ERROR_MODEL_PATH} not found!")

# Load failure prediction model & encoder
if os.path.exists(FAILURE_MODEL_PATH) and os.path.exists(ENCODER_PATH):
    try:
        failure_model = joblib.load(FAILURE_MODEL_PATH)
        label_encoder = joblib.load(ENCODER_PATH)
        logging.info("✅ Failure prediction model and encoder loaded successfully.")
    except Exception as e:
        failure_model = None
        label_encoder = None
        logging.error(f"🚨 Error loading failure prediction model: {e}")
else:
    failure_model = None
    label_encoder = None
    logging.error(f"🚨 Missing model files: {FAILURE_MODEL_PATH} or {ENCODER_PATH}")

@app.route('/predict', methods=['POST'])
def predict():
    if error_model is None:
        return jsonify({"error": "Error prediction model not loaded properly."}), 500

    try:
        data = request.get_json()

        # Validate input fields
        required_fields = ["size", "machine_count", "state_encoded"]
        if not all(field in data for field in required_fields):
            return jsonify({"error": f"Missing fields: {', '.join(set(required_fields) - set(data.keys()))}"}), 400

        try:
            features = pd.DataFrame([[float(data["size"]), int(data["machine_count"]), int(data["state_encoded"])]],
                                    columns=["size", "machine_count", "state_encoded"])
        except ValueError:
            return jsonify({"error": "Invalid data types. 'size' and 'state_encoded' must be numbers, 'machine_count' must be an integer."}), 400

        logging.info(f"🔍 Input to model: {features}")

        prediction = error_model.predict(features)[0]

        return jsonify({"predicted_error": float(prediction)})

    except Exception as e:
        logging.error(f"🚨 Prediction error: {e}")
        return jsonify({"error": "Prediction failed due to model error."}), 500

@app.route('/predict_failure', methods=['POST'])
def predict_failure():
    if failure_model is None or label_encoder is None:
        return jsonify({"error": "Failure prediction model or encoder not loaded properly."}), 500
    
    try:
        data = request.get_json()
        logging.info(f"Received data: {data}")

        # Validate input fields
        required_fields = ["state", "machine_count", "production"]
        if not all(field in data for field in required_fields):
            return jsonify({"error": f"Missing fields: {', '.join(set(required_fields) - set(data.keys()))}"}), 400

        try:
            if label_encoder:
                state_encoded = label_encoder.transform([str(data['state'])])[0]  # Encode state
            else:
                return jsonify({"error": "Label encoder is not available."}), 500

            features = pd.DataFrame([[state_encoded, int(data["machine_count"]), float(data["production"])]],
                                    columns=["state", "machine_count", "production"])
        except ValueError:
            return jsonify({"error": "Invalid data types. 'state' must be a valid string, 'machine_count' must be an integer, 'production' must be a number."}), 400
        
        logging.info(f"Model input features: {features}")

        prediction = failure_model.predict(features)[0]
        prediction = int(bool(prediction))  # Convert to binary 0/1

        logging.info(f"Predicted failure: {prediction}")
        return jsonify({"predicted_failure": prediction})

    except Exception as e:
        logging.error(f"Error in prediction: {e}")
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(debug=True)
