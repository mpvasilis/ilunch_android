#include <LiquidCrystal.h>
#include <Wire.h>
#include "RTClib.h"
#include <SoftwareSerial.h>
RTC_DS1307 RTC;
#include <SPI.h>
#include <SD.h>

const int rs = 10, en = 9, d4 = 5, d5 = 6, d6 = 3, d7 = 2;
LiquidCrystal lcd(rs, en, d4, d5, d6, d7);
SoftwareSerial btm(7,8);
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
}

void loop() {
    lcd.setCursor(6, 1);
    DateTime now = RTC.now();

    lcd.print(now.hour(), DEC);
    lcd.print(':');
    lcd.print(now.minute(), DEC);
 

    if(analogRead(A0)>500)
    {
        lcd.clear();
        lcd.setCursor(4, 0);
        lcd.print("Bad food!");
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
   case 'D': 
      lcd.clear();
      lcd.setCursor(1, 0);
      lcd.print("Downloading");
      download();
      lcd.clear();
      lcd.begin(16, 2);
      lcd.print(msg);
   break; 
   case 'T': 
   //// RTC.adjust(DateTime(2020, 06, 25, 13, 54, 0)); 
              //  printf("%s\n", command);
                //                printf("%s\n", inst);

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
 ratings = SD.open("ratings.txt", FILE_WRITE);
  if (ratings) {
    ratings.print(now.unixtime());
    ratings.print(",");
    ratings.println(rate);
    ratings.close();
  } else {
    Serial.println("error opening file");
  }
}

int download(){


    ratings = SD.open("ratings.txt");
  if (ratings) {
   
    while (ratings.available()) {
    String buffer = ratings.readStringUntil('\n');
    Serial.println(buffer);  
          btm.println(buffer);  

    }
    ratings.close();
        SD.remove("ratings.txt");

  } else {
    Serial.println("No data");
        btm.println("No data");
  }

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
