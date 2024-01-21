///////////////////////////////////////////////////////////////////////////
//// Driver for AD9850 DDS chip from Analog Devices                    
///////////////////////////////////////////////////////////////////////////
#include "AD9850.h"

#define pulseHigh(pin) { digitalWrite(pin, HIGH); delay(1); digitalWrite(pin, LOW); }

void AD9850_Reset(){
  pulseHigh(RST_LINE); //Reset Signal
  pulseHigh(CLK_LINE); //Clock Signal
  pulseHigh(FQ_LINE);  //Frequenz Update Signal
}


void initTX(){
  pinMode(RST_LINE, OUTPUT);
  pinMode(FQ_LINE, OUTPUT);
  pinMode(CLK_LINE, OUTPUT);
  pinMode(DATA_LINE, OUTPUT);

  digitalWrite(RST_LINE, 0);
  digitalWrite(FQ_LINE, 0);
  digitalWrite(CLK_LINE, 0);
  digitalWrite(DATA_LINE, 0);

  AD9850_Reset();
}


void AD9850_SendData(unsigned char c) {
  int i;
  
  for(i=0; i<8; i++) {
    digitalWrite(DATA_LINE, (c>>i)&0x01);
    pulseHigh(CLK_LINE);
  }

}

void setTxFreq(uint32_t freq){
  long int y;
  
  double frequenz = (double) freq;
  frequenz=frequenz/1000000*4294967295/125; //für einen 125 MHz Quarz
  y=frequenz;
  AD9850_SendData(y);     // w4 - Frequenzdaten LSB übertragen
  AD9850_SendData(y>>8);  // w3
  AD9850_SendData(y>>16); // w2
  AD9850_SendData(y>>24); // w1 - Frequenzdaten MSB
  AD9850_SendData(0x00);  // w0 - 0x00 keine Phase
  pulseHigh(FQ_LINE);          // Die neue Frequenz ausgeben
}
