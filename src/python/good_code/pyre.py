import pyrebase
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

not_first_try = False



def process(dictionary, pathname):
    
    name = pathname[1:]
    #p = dictionary[str(name)]["precipitation"]
    #w = dictionary[str(name)]["wind"]
    p = dictionary["precipitation"]
    w = dictionary["wind"]
    darray = np.array([p,w])
    res = model_clone.predict(darray.reshape(1,-1))
    new_data = {str(name) : str(int(res[0]))}
    db.child('Predictions').set(new_data, token_id)


def stream_handler(message):
    global not_first_try
    print("Event: " + str(message["event"]) + "    Path: " + str(message["path"]) + "    Data: " + str(message["data"]))
    
    if (type(message["data"]) == dict):
        if (not_first_try):
            process(message["data"], str(message["path"]))
        else:
            not_first_try = True



my_stream = db.child("Entries").stream(stream_handler)


