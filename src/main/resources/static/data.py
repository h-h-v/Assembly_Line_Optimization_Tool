import pandas as pd
import numpy as np

# Set random seed for consistency
np.random.seed(42)

# Number of data points
num_samples = 500  

# Generate random data
data = {
    "machine_utilization": np.random.randint(50, 100, num_samples),  # 50% to 100% utilization
    "workload": np.random.choice(["Low", "Medium", "High"], num_samples),  # Workload level
    "inventory": np.random.randint(10, 100, num_samples),  # Inventory stock levels
    "failures": np.random.randint(0, 5, num_samples),  # Number of failures
    "cycle_time": np.random.randint(5, 20, num_samples),  # Cycle time in minutes
    "state": np.random.choice(["chennai", "coimbatore", "delhi", "mumbai"], num_samples),  # States
    "name": [f"f{i}" for i in np.random.randint(1, 10, num_samples)],  # Random names
    "production": np.random.randint(20, 150, num_samples),  # Production numbers
    "size": np.repeat(16000, num_samples),  # Constant size value
    "machine_count": np.random.randint(20, 100, num_samples)  # Random machine counts
}

# Convert workload to numerical values (for ML training)
workload_mapping = {"Low": 0, "Medium": 1, "High": 2}
data["workload"] = [workload_mapping[w] for w in data["workload"]]

# Create DataFrame and save as CSV
df = pd.DataFrame(data)
df.to_csv("production_data.csv", index=False)

print("Simulated data saved to production_data.csv")