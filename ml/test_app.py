import requests

url = "http://127.0.0.1:8000/predict"

normal_payload = {
    "user_id": 1,
    "login_hour": 14,
    "failed_attempts": 0,
    "is_new_device": 0,
    "is_new_ip": 0
}

anomaly_payload = {
    "user_id": 1,
    "login_hour": 3,
    "failed_attempts": 10,
    "is_new_device": 1,
    "is_new_ip": 1
}

try:
    print("Testing Normal Login:")
    resp = requests.post(url, json=normal_payload)
    print("Status Code:", resp.status_code)
    print("Response:", resp.json())

    print("\nTesting Anomalous Login:")
    resp = requests.post(url, json=anomaly_payload)
    print("Status Code:", resp.status_code)
    print("Response:", resp.json())
except requests.exceptions.ConnectionError:
    print("Error: Could not connect to the API. Make sure app.py is running on port 8000.")
