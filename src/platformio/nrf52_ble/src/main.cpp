// Copyright (c) Sandeep Mistry. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

#include <Arduino.h>
// Import libraries (BLEPeripheral depends on SPI)
#include <SPI.h>
#include <BLEPeripheral.h>

// LED pin
#define LED_PIN LED_BUILTIN

// custom boards may override default pin definitions with BLEPeripheral(PIN_REQ, PIN_RDY, PIN_RST)
BLEPeripheral blePeripheral = BLEPeripheral();

// create service
BLEService ledService = BLEService("ee910d6a61f948929f27c1b2fa7e1ebe");

// create switch characteristic
BLEIntCharacteristic switchCharacteristic = BLEIntCharacteristic("a89b4483df7f4539ab8ae6bfb4070640", BLERead | BLENotify);


int8_t X_test[93][3] =
{
{26,92,2},
{23,94,5},
{28,80,10},
{26,91,15},
{24,95,14},
{25,84,16},
{32,63,8},
{30,57,31},
{24,94,8},
{26,79,13},
{28,75,6},
{23,95,3},
{26,90,8},
{28,82,11},
{25,92,22},
{31,53,18},
{26,83,7},
{25,94,3},
{27,92,17},
{25,88,2},
{25,92,6},
{26,89,13},
{23,95,3},
{31,59,26},
{25,89,5},
{26,80,18},
{28,82,7},
{30,65,28},
{25,92,10},
{28,75,25},
{24,91,6},
{32,59,30},
{25,94,13},
{24,93,3},
{23,96,23},
{25,89,4},
{28,80,13},
{24,96,11},
{25,84,9},
{27,78,29},
{23,93,4},
{24,94,8},
{31,64,23},
{27,84,12},
{24,95,8},
{23,92,13},
{27,87,19},
{23,95,3},
{25,93,9},
{32,59,21},
{24,89,16},
{25,85,8},
{27,79,13},
{29,87,10},
{25,89,4},
{30,72,26},
{24,92,15},
{29,82,16},
{25,88,33},
{25,91,14},
{26,86,6},
{28,71,11},
{24,94,4},
{28,86,9},
{24,93,7},
{27,81,26},
{27,78,8},
{25,88,6},
{26,90,14},
{30,75,13},
{27,78,30},
{23,95,7},
{29,84,20},
{26,89,3},
{24,95,9},
{28,68,26},
{24,95,2},
{24,93,7},
{27,84,17},
{25,89,4},
{24,95,8},
{23,93,5},
{30,66,25},
{25,88,5},
{25,90,7},
{23,93,4},
{24,92,12},
{25,94,15},
{25,94,10},
{24,95,18},
{24,94,4},
{27,85,11},
{24,92,18}
};


int getRandomTemp()
{
  return (rand() % (40 - 0 + 1)) + 0;
}
int getRandomWind()
{
  return (rand() % (120 - 0 + 1)) + 0;
}
int getRandomPrecipitation()
{
  return (rand() % (20 - 0 + 1)) + 0;
}

void setup()
{
  Serial.begin(9600);
#if defined(__AVR_ATmega32U4__)
  delay(5000); // 5 seconds delay for enabling to see the start up comments on the serial board
#endif

  // set LED pin to output mode
  pinMode(LED_PIN, OUTPUT);

  // set advertised local name and service UUID
  blePeripheral.setLocalName("Senyora placa");
  blePeripheral.setAdvertisedServiceUuid(ledService.uuid());

  // add service and characteristic
  blePeripheral.addAttribute(ledService);
  blePeripheral.addAttribute(switchCharacteristic);

  // begin initialization
  blePeripheral.begin();

  Serial.println(F("BLE LED Peripheral"));
}

void loop()
{

  BLECentral central = blePeripheral.central();

  int num_rows = sizeof(X_test)/sizeof(X_test[0]);
  int num_cols = sizeof(X_test[0])/sizeof(X_test[0][0]);
  int index = 0;

  
  if (central) {
    // central connected to peripheral
    Serial.print(F("Connected to central: "));
    Serial.println(central.address());
    int8_t temp;
    int8_t humidity;
    int8_t wind;
    int32_t data;


    while (central.connected()) {

      if (index >= num_rows){
        index = 0;
      }

      delay(5000);
      temp = X_test[index][0];
      humidity = X_test[index][1];
      wind = X_test[index][2];

      data = temp + (humidity << 8) + (wind << 16);

      switchCharacteristic.setValue(data);
      Serial.print(data);
      index++;
    }

    // central disconnected
    Serial.print(F("Disconnected from central: "));
    Serial.println(central.address());
  }
  
}
