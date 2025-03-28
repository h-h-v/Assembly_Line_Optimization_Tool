import joblib  # or any method you used to save your model

# Load your trained model
model = joblib.load('linear_regression_model.pkl')

def predict(data):
    # Preprocess data if necessary
    return model.predict([data])