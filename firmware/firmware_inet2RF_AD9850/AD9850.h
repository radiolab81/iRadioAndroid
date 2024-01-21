#ifndef _AD9850_H_
#define _AD9850_H_

#include <Arduino.h>

// pin configuration     
#define RST_LINE    4
#define FQ_LINE     8
#define CLK_LINE    6
#define DATA_LINE   7

void setTxFreq(uint32_t);
void initTX(void);

#endif