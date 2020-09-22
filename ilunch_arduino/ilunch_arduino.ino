#include <LiquidCrystal.h>
#include <Wire.h>
#include "RTClib.h"
#include <SoftwareSerial.h>
RTC_DS1307 RTC;
#include <SPI.h>
#include <SD.h>
#include <EEPROM.h>

int eepromOffset = 0;
int totalOffset = 0;

void writeStringToEEPROM(int addrOffset, const String &strToWrite)
{
  byte len = strToWrite.length();
  for (int i = 0; i < len; i++)
  {
    EEPROM.write(addrOffset + 1 + i, strToWrite[i]);
  }
}

SoftwareSerial btm(7,8);

String readStringFromEEPROM(int addrOffset)
{
  char data[totalOffset + 1];
  for (int i = 0; i < totalOffset; i++)
  {
  data[i] = EEPROM.read( 1 + i);
  Serial.print((char) EEPROM.read( 1 + i));
  btm.print((char) EEPROM.read( 1 + i));
  if((char) EEPROM.read( 1 + i) == "\n"){
          delay(100);

  }
  }

  
  data[totalOffset] = '\0';
  return String(data);
}


const int rs = 10, en = 9, d4 = 5, d5 = 6, d6 = 3, d7 = 2;
LiquidCrystal lcd(rs, en, d4, d5, d6, d7);
int index = 0; 
char data[100]; 
char msg[100]="     iLunch"; 
char c; 
boolean flag = false;
File ratings;

void setup() {

    Serial.begin(9600);

    Serial.print("Initializing SD card...");
    if (!SD.begin(4)) {
      Serial.println("initialization failed!");
      while (1);
    }
    Serial.print("SD card ready!");

    Wire.begin();
    RTC.begin();

    lcd.begin(16, 2);
    lcd.print(msg);
    delay(2000);
  
    pinMode(A0,INPUT);
    pinMode(A1,INPUT);
    pinMode(A2,INPUT);
    pinMode(A3,INPUT);

    btm.begin(9600);

     for (int i = 0 ; i < EEPROM.length() ; i++) {
    EEPROM.write(i, 0);
  }

    
}

  //thhis is a list of int variables used in this clock program
int s=0;
int sec=0;
int hrs=0;
int minutes=0;
int initialHours = 02;//variable to initiate hours
int initialMins = 0;//variable to initiate minutes
int initialSecs = 00;//variable to initiate seconds
 

void loop() {
    lcd.setCursor(4, 1);
    DateTime now = RTC.now();

    printDigits(now.hour());
    lcd.print(':');
    printDigits(now.minute());
   lcd.print(':');
  printDigits(now.second());


    if(analogRead(A0)>500)
    {
        lcd.clear();
        lcd.setCursor(4, 0);
        lcd.print("Bad food!");
        btm.println("Bad food pressed.");
        delay(1000);
        lcd.clear();
        lcd.begin(16, 2);
        lcd.print(msg);
        writeFile('1');
       
    }

    if(analogRead(A1)>500)
    {
        lcd.clear();
        lcd.setCursor(1, 0);
        lcd.print("Moderate food!");
        btm.println("Moderate food pressed.");
        delay(1000);
        lcd.clear();
        lcd.begin(16, 2);
        lcd.print(msg);
        writeFile('3');
    }

    if(analogRead(A2)>500)
    {
        lcd.clear();
        lcd.setCursor(4, 0);
        lcd.print("Good food!");
        btm.println("Good food pressed.");
        delay(1000);
        lcd.clear();
        lcd.begin(16, 2);
        lcd.print(msg);
        writeFile('4');

    }

    if(analogRead(A3)>500)
    {
        lcd.clear();
        lcd.setCursor(3, 0);
        lcd.print("Great food!");
        btm.println("Great food pressed.");
        delay(1000);
        lcd.clear();
        lcd.begin(16, 2);
        lcd.print(msg);
        writeFile('5');

    }

    delay(100);

    if(btm.available() > 0){ 
     while(btm.available() > 0){ 
          c = btm.read(); 
          delay(10); 
          data[index] = c; 
          index++; 
     } 
     data[index] = '\0'; 
     flag = true;   
   }  
   if(flag){ 
     processCommand(); 
     flag = false; 
     index = 0; 
     data[0] = '\0'; 
   } 
} 

void processCommand(){ 
 char command = data[0]; 
 char inst = data[1]; 
 switch(command){ 
     case 'A': 
      lcd.clear();
      lcd.setCursor(1, 0);
      lcd.print("Synching...");
      getMSG(1);
      lcd.clear();
      lcd.begin(16, 2);
      lcd.print(msg);
   break; 
     case 'B': 
      lcd.clear();
      lcd.setCursor(1, 0);
      lcd.print("Synching...");
      getMSG(2);
      lcd.clear();
      lcd.begin(16, 2);
      lcd.print(msg);
   break; 
        case 'C': 
      lcd.clear();
      lcd.setCursor(1, 0);
      lcd.print("Synching...");
      getMSG(3);
      lcd.clear();
      lcd.begin(16, 2);
      lcd.print(msg);
   break;
           case 'E': 
      lcd.clear();
      lcd.setCursor(1, 0);
      lcd.print("Synching...");
      getMSG(4);
      lcd.clear();
      lcd.begin(16, 2);
      lcd.print(msg);
   break;
            case 'F': 
      lcd.clear();
      lcd.setCursor(1, 0);
      lcd.print("Synching...");
      getMSG(5);
      lcd.clear();
      lcd.begin(16, 2);
      lcd.print(msg);
   break;
   case 'D': 
      lcd.clear();
      lcd.setCursor(1, 0);
      lcd.print("Downloading");
      delay(100);
      download();
      lcd.clear();
      lcd.begin(16, 2);
      lcd.print(msg);
   break; 
   case 'T': 
    //RTC.adjust(DateTime(2020, 9, 06, 01, 48, 0)); 
 printf("%s\n", inst);
    //syncTime();
    lcd.clear();
      lcd.setCursor(1, 0);
      lcd.print("Time Sync");
       delay(1000);
      lcd.clear();
      lcd.begin(16, 2);
      lcd.print(msg);
   break; 
   case 'N': 
   strcpy(msg, "");
  
   break; 
 } 
} 

void writeFile(char rate){
              
  Serial.println(rate);
  
  DateTime now = RTC.now();
  
  String unixtimestamp = String(now.unixtime());
  String ratingstr = String(rate);
  String toWrite = unixtimestamp+","+ratingstr+"\n";
  writeStringToEEPROM(totalOffset, toWrite);
  totalOffset = totalOffset + toWrite.length();
   Serial.println(toWrite);



}

int download(){

 readStringFromEEPROM(0);
 btm.println("success");
 for (int i = 0 ; i < EEPROM.length() ; i++) {
    EEPROM.write(i, 0);
 }

 return 0;
}

void printDigits(byte digits){
    if(digits < 10)
        lcd.print('0');
    lcd.print(digits);
}

int getMSG(int messagenum){
 
  return 0;
}


int syncTime(){
    char** tokens;

    tokens = str_split(data, ',');

    if (tokens)
    {
        int i;
        for (i = 0; *(tokens + i); i++)
        {
            printf("time=[%s]\n", *(tokens + i));
        }
        printf("\n");
        RTC.adjust(DateTime(tokens + 1, tokens + 2, tokens + 3, tokens + 4, tokens + 5, tokens + 6));   
    }

}


char** str_split(char* a_str, const char a_delim)
{
    char** result    = 0;
    size_t count     = 0;
    char* tmp        = a_str;
    char* last_comma = 0;
    char delim[2];
    delim[0] = a_delim;
    delim[1] = 0;

    while (*tmp)
    {
        if (a_delim == *tmp)
        {
            count++;
            last_comma = tmp;
        }
        tmp++;
    }
    count += last_comma < (a_str + strlen(a_str) - 1);
    count++;
    result = malloc(sizeof(char*) * count);
    if (result)
    {
        size_t idx  = 0;
        char* token = strtok(a_str, delim);

        while (token)
        {
            //assert(idx < count);
            *(result + idx++) = strdup(token);
            token = strtok(0, delim);
        }
        //assert(idx == count - 1);
        *(result + idx) = 0;
    }
    return result;
}
