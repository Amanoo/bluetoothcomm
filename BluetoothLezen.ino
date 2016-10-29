#include <SoftwareSerial.h>

#define rxPin 10
#define txPin 11

SoftwareSerial mySerial(rxPin, txPin); // RX, TX
int myChar ; 

uint8_t values[12];
int realValues[6];

void setup() {
  Serial.begin(230400);   
  mySerial.begin(115200);
}

void loop(){

  while (mySerial.available()) {
        // read the incoming byte(s)
       for(int i=0; i<12; i++){
        values[i]=mySerial.read();
//        Serial.print(j,BIN);
//        Serial.print("\t");
       }
       realValues[0]=256*values[0]+values[1];
       realValues[1]=256*values[2]+values[3];
       realValues[2]=256*values[4]+values[5];
       realValues[3]=256*values[6]+values[7];
       realValues[4]=256*values[8]+values[9];
       realValues[5]=256*values[10]+values[11];

//       if(abs(realValues[0])<420&&abs(realValues[1])<420&&abs(realValues[2])<420&&abs(realValues[3])<420){
       Serial.print(realValues[0]);
       Serial.print("\t");
       Serial.print(realValues[1]);
       Serial.print("\t");
       Serial.print(realValues[2]);
       Serial.print("\t");
       Serial.print(realValues[3]);
       Serial.print("\t");
       Serial.print(realValues[4]);
       Serial.print("\t");
       Serial.print(realValues[5]);
       Serial.print("\t");
       Serial.println();
//       }
  }
//delay(5); 
/*  while(mySerial.available()){
    myChar = mySerial.read();
    Serial.println(myChar);
  }*/

  while(Serial.available()){
   myChar = Serial.read();
   mySerial.print(myChar);
  }
}
