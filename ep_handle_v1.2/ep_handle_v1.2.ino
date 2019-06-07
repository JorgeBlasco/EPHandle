#define trigPin 3
#define echoPin 2
#define buzzPin 11
#define ledPin 13
#define vibPin 7
const char TOGGLE_BUZZER = 'B';
const char TOGGLE_VIBRATION = 'V';
const char TOGGLE_LIGHT = 'L';
const char SET_TONE = 't';
const char SET_BRIGHTNESS = 'b';
const char SET_VIB_POWER = 'p';
bool buzzer;
bool vibration;
bool light;
int btone;
int brightness;
int vibPower;

void setup() {
  Serial.begin(9600);
  pinMode(trigPin, OUTPUT);
  pinMode(echoPin, INPUT);
  pinMode(ledPin, OUTPUT);
  digitalWrite(trigPin, LOW);
  buzzer = true;
  vibration = false;
  light = true;
  btone = 1000;
  brightness = 3;
  vibPower = 1;
}

void loop() {
      //Zona de recepción de datos
      digitalWrite(trigPin, HIGH);
      delayMicroseconds(10);          //Enviamos un pulso de 10us
      digitalWrite(trigPin, LOW);

      int t = pulseIn(echoPin, HIGH);  //obtenemos el ancho del pulso
      int d = t/59;
      Serial.println((String)d);
      if (d<30){
        if(buzzer)
          //tone(buzzPin,(-120*d)+4000);
          tone(buzzPin,btone);
        else
          noTone(buzzPin);
        if(light)
          digitalWrite(ledPin, HIGH);
        else
          digitalWrite(ledPin, LOW);
        if(vibration)
          digitalWrite(vibPin, HIGH);
        else
          digitalWrite(vibPin, LOW);
      }
      else{
          noTone(buzzPin);
          digitalWrite(ledPin, LOW);
          //digitalWrite(vibPin, LOW);
      }
      delay(200);
    }



    void serialEvent(){
      //Zona para configurar
      while(Serial.available())
      {
        String msg = Serial.readStringUntil('*');
        if (msg.indexOf(TOGGLE_BUZZER)>=0)
        {
          buzzer=!buzzer;
        }
        if (msg.indexOf(TOGGLE_VIBRATION)>=0)
        {
          vibration=!vibration;
        }
        if (msg.indexOf(TOGGLE_LIGHT)>=0)
        {
          light=!light;
        }
        if (msg.indexOf(SET_TONE)>=0)
        {
          String vol = msg.substring(msg.indexOf('t')+1,msg.indexOf('t')+5); //numero de 4 dígitos
          btone = vol.toInt();
        }
        if (msg.indexOf(SET_BRIGHTNESS)>=0)
        {
          String bri = msg.substring(msg.indexOf('b')+1,msg.indexOf('b')+3); //numero de 2 dígitos
          brightness = bri.toInt();
        }
        if (msg.indexOf(SET_VIB_POWER)>=0)
        {
          String vPower = msg.substring(msg.indexOf('p')+1,msg.indexOf('p')+3); //numero de 2 dígitos
          vibPower = vPower.toInt();
        }
      }
    }
