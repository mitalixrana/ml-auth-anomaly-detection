from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import joblib
import os
import pandas as pd

app = FastAPI(title="Authentication Anomaly Detection API")

# Load model globally on startup
model_path = os.path.join(os.path.dirname(__file__), 'model.pkl')
model = None

try:
    if os.path.exists(model_path):
        model = joblib.load(model_path)
        print("Model loaded successfully.")
    else:
        print("Warning: model.pkl not found. Please run train_model.py first.")
except Exception as e:
    print(f"Error loading model: {e}")

class LoginFeatureRequest(BaseModel):
    user_id: int
    login_hour: int
    failed_attempts: int
    is_new_device: int
    is_new_ip: int

class AnomalyResponse(BaseModel):
    anomaly_score: float
    is_anomalous: int

@app.post("/predict", response_model=AnomalyResponse)
def predict_anomaly(req: LoginFeatureRequest):
    if model is None:
        raise HTTPException(status_code=500, detail="Machine learning model is not loaded.")
        
    try:
        # Format input for scikit-learn
        df = pd.DataFrame([{
            'login_hour': req.login_hour,
            'failed_attempts': req.failed_attempts,
            'is_new_device': req.is_new_device,
            'is_new_ip': req.is_new_ip
        }])
        
        # Predict: 1 for normal, -1 for anomaly
        pred = model.predict(df)[0]
        
        # Decision function: lower values mean more anomalous. 
        # For simplicity, we can invert the sign or map to a probability-like score.
        score = model.decision_function(df)[0]
        
        is_anom = 1 if pred == -1 else 0
        
        # Mapping score so that higher score = more anomalous (optional)
        # IsolationForest decision_function is negative for anomalies.
        # So we can just negate it. Higher positive = anomaly.
        anomaly_score = float(-score)
        
        return AnomalyResponse(
            anomaly_score=anomaly_score,
            is_anomalous=is_anom
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="127.0.0.1", port=8000)
