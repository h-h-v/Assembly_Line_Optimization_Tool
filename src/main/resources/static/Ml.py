import pandas as pd
import joblib
import os
import logging
from sklearn.linear_model import LinearRegression
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_squared_error

# Configure logging
logging.basicConfig(level=logging.INFO)

# Load data from CSV
file_path = "production_data.csv"  # Update this if needed

if not os.path.exists(file_path):
    raise FileNotFoundError(f"❌ CSV file not found at: {file_path}")

df = pd.read_csv(file_path)

# Check data structure
logging.info(f"📊 First 5 rows of dataset:\n{df.head()}")

# Ensure required columns exist
required_columns = {'state', 'production', 'size', 'machine_count'}
missing_columns = required_columns - set(df.columns)

if missing_columns:
    raise ValueError(f"❌ Missing required columns: {', '.join(missing_columns)}")

# Handle missing values
if df.isnull().sum().sum() > 0:
    logging.warning("⚠️ Missing values detected. Filling with mean values.")
    df.fillna(df.mean(numeric_only=True), inplace=True)

# Encode 'state' column (if categorical)
df['state_encoded'] = df['state'].astype('category').cat.codes if df['state'].dtype == object else df['state']

# Define features and target
X = df[['size', 'machine_count', 'state_encoded']]
y = df['production']  # Predicting production

# Ensure we have enough data for train-test split
if len(df) > 1:
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)
else:
    X_train, y_train = X, y  # If only 1 row, use all for training
    X_test, y_test = X, y  # Avoid errors

# Train model
model = LinearRegression()
model.fit(X_train, y_train)

# Predictions
y_pred = model.predict(X_test) if len(X_test) > 0 else []

# Calculate Mean Squared Error (if test data exists)
if len(y_pred) > 0:
    mse = mean_squared_error(y_test, y_pred)
    logging.info(f"📉 Mean Squared Error: {mse:.4f}")

# Save model
model_path = "linear_regression_model.pkl"
joblib.dump(model, model_path)
logging.info(f"✅ Model saved successfully as {model_path}")
