#include <SoftwareSerial.h>

#define trigPin 3
#define echoPin 2
#define buzzPin 11 
SoftwareSerial BT1(8,9); //RX,TX CAMBIAR LOS PINES A MI ANTOJO
const char TOGGLE_BUZZER = 'B';
const char TOGGLE_VIBRATION = 'V';
const char TOGGLE_LIGHT = 'L';
const char DATA_PETITION = 'D';
const char SET_VOLUME = 'v';
const char SET_BRIGHTNESS = 'b';
const char SET_VIB_POWER = 'p';
bool even;
bool buzzer;
bool vibration;
bool light;
int volume;
int brightness;
int vibPower;

void setup() {
  Serial.begin(9600);
  BT1.begin(9600);
  pinMode(trigPin, OUTPUT);
  pinMode(echoPin, INPUT);
  digitalWrite(trigPin, LOW);
}

void loop() {
    if(even){
      //Zona de recepción de datos
      digitalWrite(trigPin, HIGH);
      delayMicroseconds(10);          //Enviamos un pulso de 10us
      digitalWrite(trigPin, LOW);

      int t = pulseIn(echoPin, HIGH);  //obtenemos el ancho del pulso
      int d = t/59;
      if(buzzer)   //FALTA ENCENDER EL LED
        if (d<20)
          tone(buzzPin,1000);
        else
          noTone(buzzPin);
      if (BT1.availableForWrite()>0){
        BT1.print(d);
      }
      even = !even;
    }
    else{
      //Zona para configurar
      if (BT1.available()>0)
      {
        String msg = BT1.readStringUntil('*');
        if (msg.indexOf("B")>0)
        {
          buzzer=!buzzer;
        }
        if (msg.indexOf("V")>0)
        {
          vibration=!vibration;
        }
        if (msg.indexOf("L")>0)
        {
          light=!light;
        }
        if (msg.indexOf("D")>0)
        {
          even = !even;
        }
        if (msg.indexOf("v")>0)
        {
          String vol = msg.substring(msg.indexOf("v")+1,msg.indexOf("v")+2);
          volume = vol.toInt();
        }
        if (msg.indexOf("b")>0)
        {
          String bri = msg.substring(msg.indexOf("b")+1,msg.indexOf("b")+2);
          brightness = bri.toInt();
        }
        if (msg.indexOf("p")>0)
        {
          String vPower = msg.substring(msg.indexOf("p")+1,msg.indexOf("p")+2);
          vibPower = vPower.toInt();
        }
      }
    }
}
