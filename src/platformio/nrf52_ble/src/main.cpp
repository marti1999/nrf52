// Copyright (c) Sandeep Mistry. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

#include <Arduino.h>
// Import libraries (BLEPeripheral depends on SPI)
#include <SPI.h>
#include <BLEPeripheral.h>

// LED pin
#define LED_PIN   LED_BUILTIN

//custom boards may override default pin definitions with BLEPeripheral(PIN_REQ, PIN_RDY, PIN_RST)
BLEPeripheral                    blePeripheral                            = BLEPeripheral();

// create service
BLEService               ledService           = BLEService("ee910d6a61f948929f27c1b2fa7e1ebe");

// create switch characteristic
BLEIntCharacteristic    switchCharacteristic = BLEIntCharacteristic("a89b4483df7f4539ab8ae6bfb4070640", BLERead | BLENotify);

int getRandomTemp(){
  return (rand() % (40-0+1)) + 0;
}
int getRandomWind(){
  return (rand() % (120-0+1)) + 0;
}
int getRandomPrecipitation(){
  return (rand() % (20-0+1)) + 0;
}

void setup() {
  Serial.begin(9600);
#if defined (__AVR_ATmega32U4__)
  delay(5000);  //5 seconds delay for enabling to see the start up comments on the serial board
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

void loop() {
  BLECentral central = blePeripheral.central();

  if (central) {
    // central connected to peripheral
    Serial.print(F("Connected to central: "));
    Serial.println(central.address());
    int8_t temp = 0;
    int8_t humidity = 32;
    int8_t wind = 64;
    int32_t data = 0;

    while (central.connected()) {

      delay(5000);
      temp = getRandomTemp();
      humidity = getRandomPrecipitation();
      wind = getRandomWind();

      data = temp + (humidity << 8) + (wind << 16);

      switchCharacteristic.setValue(data);
      Serial.print(data);

      
      


      // // central still connected to peripheral
      // if (switchCharacteristic.written()) {
      //   // central wrote new value to characteristic, update LED
      //   if (switchCharacteristic.value()) {
      //     Serial.println(F("LED on"));
      //     digitalWrite(LED_PIN, HIGH);
      //   } else {
      //     Serial.println(F("LED off"));
      //     digitalWrite(LED_PIN, LOW);
      //   }
      // }
    }

    // central disconnected
    Serial.print(F("Disconnected from central: "));
    Serial.println(central.address());
  }
} 

