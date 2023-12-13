// switches for next & prev channel
#define PIN_TASTER_PROGRAM_NEXT  2   
#define PIN_TASTER_PROGRAM_PREV  3

// rotary encoder for volume setting
#define PIN_ENCODER_VOL_CLK    5
#define PIN_ENCODER_VOL_DT     4


unsigned char flag;
unsigned char Last_RoB_Status;
unsigned char Current_RoB_Status;

static unsigned long last_interrupt_time = 0;

void setup() {
  // put your setup code here, to run once:
  // switches with interrupt support, rotary in polling mode
  pinMode(PIN_TASTER_PROGRAM_NEXT, INPUT_PULLUP);
  pinMode(PIN_TASTER_PROGRAM_PREV, INPUT_PULLUP);
  attachInterrupt(digitalPinToInterrupt(PIN_TASTER_PROGRAM_NEXT), Interrupt, FALLING);
  attachInterrupt(digitalPinToInterrupt(PIN_TASTER_PROGRAM_PREV), Interrupt, FALLING);

  pinMode(PIN_ENCODER_VOL_CLK, INPUT_PULLUP);
  pinMode(PIN_ENCODER_VOL_DT, INPUT_PULLUP);

 
  // Send commands via serial to Smartphone
  Serial.begin(115200); 

}

void rotaryProcessing(void)
{
	Last_RoB_Status = digitalRead(PIN_ENCODER_VOL_CLK);

	while(!digitalRead(PIN_ENCODER_VOL_DT)){
		Current_RoB_Status = digitalRead(PIN_ENCODER_VOL_CLK);
		flag = 1;
	}

	if(flag == 1){
		flag = 0;
		if((Last_RoB_Status == 0)&&(Current_RoB_Status == 1)){
			 Serial.println("VUP"); // send iRadioAndroid command for volume up
		}
		if((Last_RoB_Status == 1)&&(Current_RoB_Status == 0)){
			 Serial.println("VDW"); // send iRadioAndroid command for volume down
		}

	}
}

// ISR for switches
void Interrupt() {
  unsigned long interrupt_time = millis();
  // If interrupts come faster than 500ms, assume it's a bounce and ignore
  if (interrupt_time - last_interrupt_time > 500) 
  {
    if (digitalRead(PIN_TASTER_PROGRAM_NEXT) == 0)
      Serial.println("PNX"); // send iRadioAndroid command for next program

    if (digitalRead(PIN_TASTER_PROGRAM_PREV) == 0)
      Serial.println("PPR"); // send iRadioAndroid command for prev program
  }

  last_interrupt_time = interrupt_time;
}

void loop() {
  // put your main code here, to run repeatedly:
  rotaryProcessing();
  delay(10);
    
}
