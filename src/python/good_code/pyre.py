import pyrebase
import time
import joblib
import numpy as np

model_clone = joblib.load('temperature_regr.pkl')

config = {
  "apiKey": "AIzaSyCVoioFYzEuHICmSVGOaU7gFSexdm0k8g8",
  "authDomain": "test-37a21.firebaseapp.com",
  "databaseURL": "https://test-37a21-default-rtdb.europe-west1.firebasedatabase.app",
  "projectId": "test-37a21",
  "storageBucket": "test-37a21.appspot.com",
  "messagingSenderId": "1044411441884",
  "appId": "1:1044411441884:web:c48522ef19d83c5b0fca19"
}

firebase = pyrebase.initialize_app(config)
auth = firebase.auth()
db = firebase.database()

email = 'alcs222@gmail.com'
pwd = 'password'


auth_token = auth.sign_in_with_email_and_password(email, pwd)
auth_token

token_id = auth_token['idToken']
token_id



def predict(darray):
    return model_clone.predict(darray.reshape(1,-1))


def process(dictionary, path):
    print(dictionary)
    p = dictionary["precipitation"]
    w = dictionary["wind"]
    print(p)
    print(w)
    darray = np.array([p,w])
    
    res = predict(darray)
    name = path[1:]
    new_data = {'temperature' : str(res[0])}
    db.child('Predictions').child(str(name)).set(new_data, token_id)

def stream_handler(message):
    print("Event: " + str(message["event"]) + "    Path: " + str(message["path"]) + "    Data: " + str(message["data"]))
    if (type(message["data"]) == dict):
        process(message["data"], str(message["path"]))



my_stream = db.child("Entries").stream(stream_handler)



my_stream.close()
my_stream.close()
my_stream.close()
my_stream.close()
my_stream.close()
my_stream.close()
