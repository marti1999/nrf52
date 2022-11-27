# nrf52840-DK
This project uses a NRF52 development board to read air temperature and humidity. By its Bluetooth Low Energy module, it sends all the data to the android app.
<img src="./img/device.jpeg" alt="device" width="600"/>

The app, using the Android Bluetooth library, read the data and shows it on the screen. 
<img src="./img/screen1.jpeg" alt="first screen" width="600"/>
<img src="./img/screen2.jpeg" alt="second screen" width="600"/>

Additionally, all the data is later sent to the cloud (Google Firestore).
<img src="./img/cloud.png" alt="cloud control panel" width="600"/>
