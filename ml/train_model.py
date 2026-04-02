import pandas as pd
import numpy as np
from sklearn.ensemble import IsolationForest
import joblib
import os

def generate_synthetic_data(num_samples=5000):
    np.random.seed(42)
    
    # Normal behavior: mostly office hours, few failures, known devices and IPs
    login_hour = np.random.normal(loc=12, scale=3, size=num_samples)
    login_hour = np.clip(login_hour, 0, 23).astype(int)
    
    failed_attempts = np.random.poisson(lam=0.2, size=num_samples)
    failed_attempts = np.clip(failed_attempts, 0, 10)
    
    is_new_device = np.random.choice([0, 1], p=[0.95, 0.05], size=num_samples)
    is_new_ip = np.random.choice([0, 1], p=[0.9, 0.1], size=num_samples)
    
    df_normal = pd.DataFrame({
        'login_hour': login_hour,
        'failed_attempts': failed_attempts,
        'is_new_device': is_new_device,
        'is_new_ip': is_new_ip
    })
    
    # Anomalous behavior: midnight logins, high failures, new devices and IPs
    num_anomalies = int(num_samples * 0.05)
    
    anom_login_hour = np.random.choice([0, 1, 2, 3, 4, 22, 23], size=num_anomalies)
    anom_failed_attempts = np.random.randint(3, 15, size=num_anomalies)
    anom_new_device = np.random.choice([0, 1], p=[0.2, 0.8], size=num_anomalies)
    anom_new_ip = np.random.choice([0, 1], p=[0.1, 0.9], size=num_anomalies)
    
    df_anom = pd.DataFrame({
        'login_hour': anom_login_hour,
        'failed_attempts': anom_failed_attempts,
        'is_new_device': anom_new_device,
        'is_new_ip': anom_new_ip
    })
    
    df = pd.concat([df_normal, df_anom], ignore_index=True)
    # Shuffle
    df = df.sample(frac=1, random_state=42).reset_index(drop=True)
    
    return df

def train_model():
    print("Generating synthetic authentication data...")
    df = generate_synthetic_data(10000)
    
    print("Training Isolation Forest model...")
    # Features used: login_hour, failed_attempts, is_new_device, is_new_ip
    X = df[['login_hour', 'failed_attempts', 'is_new_device', 'is_new_ip']]
    
    # contamination = ratio of expected anomalies
    model = IsolationForest(n_estimators=100, contamination=0.05, random_state=42)
    model.fit(X)
    
    # Save model
    model_path = os.path.join(os.path.dirname(__file__), 'model.pkl')
    joblib.dump(model, model_path)
    print(f"Model successfully trained and saved to {model_path}")

if __name__ == '__main__':
    train_model()
