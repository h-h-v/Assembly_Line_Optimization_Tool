import pandas as pd
import numpy as np
import pickle
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import LabelEncoder
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import accuracy_score

# Load Dataset from CSV
csv_file_path = "production_data.csv"  # Your uploaded file path
data = pd.read_csv(csv_file_path)

# Ensure Column Names Match
required_columns = {'state', 'machine_count', 'production', 'failures'}
if not required_columns.issubset(data.columns):
    raise ValueError(f"CSV file must contain these columns: {required_columns}")

# Encode Categorical 'State' into Numeric Values
label_encoder = LabelEncoder()
data['state'] = label_encoder.fit_transform(data['state'])

# Define Features and Target
X = data[['state', 'machine_count', 'production']]
y = data['failures']

# Split Data (80% Training, 20% Testing)
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

# Train Random Forest Classifier
model = RandomForestClassifier(n_estimators=100, random_state=42)
model.fit(X_train, y_train)

# Predict and Check Accuracy
y_pred = model.predict(X_test)
print(f"Model Accuracy: {accuracy_score(y_test, y_pred):.2f}")

# Save Model as Pickle File
with open("fail.pkl", "wb") as file:
    pickle.dump(model, file)

# Save Label Encoder
with open("label_encoder.pkl", "wb") as file:
    pickle.dump(label_encoder, file)

print("✅ Model and encoder saved successfully!")
