#include <SoftwareSerial.h>

#define rxPin 3
#define txPin 11

SoftwareSerial mySerial(rxPin, txPin); // RX, TX
int values[2];
unsigned long timer;



void setup() {
  pinMode(5, OUTPUT);
  pinMode(6, OUTPUT);
  pinMode(9, OUTPUT);
  pinMode(10, OUTPUT);
  values[0]=0;
  values[1]=0;
  mySerial.begin(57600);
  Serial.begin(57600);   
  timer = millis();
}

void loop() {
    while (mySerial.available()){//&&(millis()-timer)<100) {
        // read the incoming byte(s)

       //if((millis()-timer)<100){
        for(int i=0; i<2; i++){
          values[i]=(256*(int)mySerial.read())+mySerial.read();
                  
          Serial.print(values[i]);
          Serial.print("\t");
          if(i==1){Serial.println();}
        
        
        }
        timer = millis();
      /* }else{
        values[0]=0;
        values[1]=0;
       }*/
       if(!(values[0]==-1||values[0]==-257||values[0]==1023||values[1]==-1||values[1]==-257||values[1]==1023)){
        sturen(values[0]/118);
        rijden(values[1]/118);
       }

}
}


// -255 =< kracht <= 255
void sturen(int kracht){
  if(kracht<0){
    digitalWrite(6, LOW);
    analogWrite(5, -1*kracht);
  }else{
    digitalWrite(5, LOW);
    analogWrite(6, kracht);
  }
}

// -255 =< kracht <= 255
void rijden(int kracht){
  if(kracht<0){
    digitalWrite(9, LOW);
    analogWrite(10,-1*kracht);
  }else{
    digitalWrite(10,LOW);
    analogWrite(9,  kracht);
  }
}



