#include <SoftwareSerial.h>

#define rxPin 10
#define txPin 11

SoftwareSerial mySerial(rxPin, txPin); // RX, TX

int values[6];
void setup() {
  Serial.begin(230400);   
  mySerial.begin(115200);
}

void loop(){

  while (mySerial.available()) {
        // read the incoming byte(s)
       for(int i=0; i<6; i++){
        values[i]=(256*(int)mySerial.read())+mySerial.read();
        Serial.print(values[i]);
        Serial.print("\t");
        if(i==5){Serial.println();}
       }
  
       
  }
  
  while(Serial.available()){
   mySerial.print(Serial.read());
  }
}

