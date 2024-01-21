/*
   inet2RF Firmware for iRadioAndroid based on SerialEvent example from ArduinoIDE
*/
#include <Arduino.h>
#include <FreqCount.h>  // lib by Paul Stoffregen https://www.pjrc.com/teensy/td_libs_FreqCount.html

#include "AD9832.h"

String inputString = "";      // a String to hold incoming data
bool stringComplete = false;  // whether the string is complete

void setup() {
  // initialize serial:
  Serial.begin(115200);
  // reserve 200 bytes for the inputString:
  inputString.reserve(200);

  // set gate time to 250 ms, so result must be multiplied by 4
  FreqCount.begin(250); // input pin  -> https://www.pjrc.com/teensy/td_libs_FreqCount.html

  // init DDS TX module
  initTX();
  setTxFreq(1000); // initial test frequency is 1 kHz 

}

void loop() {
  // print the string when a newline arrives:
  if (stringComplete) {
    Serial.println(inputString);
    // clear the string:
    inputString = "";
    stringComplete = false;
  }

 // -> get local oscillator frequency and send result in kHz to iRadioAndoid <FRX=...> command
 if (FreqCount.available()) {
    unsigned long count = FreqCount.read();
    Serial.println("FRX=" + String(count*4)); // *4 -> gate time is 250ms only
  }
}

/*
  SerialEvent occurs whenever a new data comes in the hardware serial RX. This
  routine is run between each time loop() runs, so using delay inside loop can
  delay response. Multiple bytes of data may be available.
*/
void serialEvent() {
  while (Serial.available()) {
    // get the new byte:
    char inChar = (char)Serial.read();
    // add it to the inputString:
    inputString += inChar;
    // if the incoming character is a newline, set a flag so the main loop can
    // do something about it:
    if (inChar == '\n') {
      stringComplete = true;
       // get a new transmitter frequency from iRadioAndroid ?
       if (inputString.indexOf("FTX=") != -1) {
         // extract frequency [FTX=].... 
         if (inputString.substring(String("FTX=").length()).toInt() != 0) {
           // send result [multiplied by 1000 -> kHz] to transmitter module
           setTxFreq((uint32_t) inputString.substring(String("FTX=").length()).toInt() * 1000);
         }
       }
    }
  }
}
