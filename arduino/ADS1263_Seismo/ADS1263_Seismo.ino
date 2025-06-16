#include <ADS126X.h>
#include <SPI.h>
#include <filters.h>
#include <limits.h>

// ADS1263 PINS

#define PIN_DRDY 9
#define PIN_CS 10

#define POS_PIN ADS126X_AIN3
#define NEG_PIN ADS126X_AIN2

// ADS1263 OVERSAMPLING

#define ADS_SAMPLE_RATE ADS126X_RATE_1200
const double ads_sample_rate = 1200.0;
const double sampling_time = 1.0 / ads_sample_rate;

// OFFSET CALIBRATION

const int calibration_seconds = 60;
const int initial_ignore_seconds = 12;

// FILTER

float cutoff_freq = 20.0;
IIR::ORDER order = IIR::ORDER::OD4;

// INTERNAL (DO NOT CHANGE AFTER THIS LINE)
Filter filter(cutoff_freq, sampling_time, order);
ADS126X adc;
const int SAMPLE_RATES[5] = { 20, 40, 60, 100, 200 };

int sample_rate;
long sample_time_micros;

bool calibrating;
int32_t offset;

unsigned long last_time;
unsigned long current_time;

int64_t sum;
int32_t count;
int32_t calibration_count;

byte log_num;
int shift;

void get_sample_rate()
{
    int index = -1;
    Serial.println("Please select sample rate");
    while(index == -1) {
        while (!Serial.available()) {
            delay(1);
        }
        int raw = Serial.read();
        if (raw >= '0' && raw <= '4') {
            index = raw - '0';
            break;
        }
    }

    shift = 0;
    sample_rate = SAMPLE_RATES[index];
    sample_time_micros = 1000000 / sample_rate;

    cutoff_freq = sample_rate / 2.0;
    filter.setCutoffFreqHZ(cutoff_freq);

    Serial.print("Selected ");
    Serial.print(sample_rate);
    Serial.println(" sps");

    last_time = 0;
    sum = 0;
    count = 0;
    log_num = 0;
    shift = 0;
    calibrating = true;
    calibration_count = 0;
    offset = 0;
    Serial.println("Offset calibration start, be patient...");
}

void setup()
{
    Serial.begin(115200);

    pinMode(PIN_DRDY, INPUT);

    get_sample_rate();

    adc.begin(PIN_CS);
    delay(10);
    adc.setRate(ADS_SAMPLE_RATE);
    delay(10);
    adc.startADC1();
    delay(10);
    adc.setGain(ADS126X_GAIN_32);
    delay(10);
    adc.setFilter(ADS126X_SINC4);
    delay(10);
    adc.setBiasMagnitude(ADS126X_BIAS_MAG_0);
    delay(10);
    adc.setReference(ADS126X_REF_NEG_VSS, ADS126X_REF_POS_VDD);
}

void loop()
{
    bool rdy = !(digitalRead(PIN_DRDY));
    if (rdy) {
        int32_t current_val = adc.readADC1(POS_PIN, NEG_PIN);
        if (calibrating) {
            if( calibration_count >= initial_ignore_seconds * ads_sample_rate) {
                sum += current_val;
                count++;
            }

            calibration_count++;

            if (count >= calibration_seconds * ads_sample_rate) {
                calibrating = false;
                offset = lround(sum / count);
                sum = 0;
                count = 0;
                Serial.print("Offset calibration finish, offset = ");
                Serial.println(offset);
            }
        } else {
            sum += filter.filterIn(current_val - offset);
            count++;
        }
    }

    current_time = micros();

    if (!calibrating && abs(current_time - last_time) >= sample_time_micros + shift) {
        int32_t value = lround(sum / count);
        sum = 0;
        count = 0;

        Serial.print('s');
        Serial.print(shift);
        Serial.print('l');
        Serial.print(log_num);
        Serial.print('v');
        Serial.println(value);

        last_time = current_time;
        log_num++;
    }

    if (Serial.available()) {
        char ch = Serial.read();
        if (ch == '-' && shift != INT_MIN) {
            shift--;
        } else if (ch == '+' && shift != INT_MAX) {
            shift++;
        } else if (ch == 'r') {
            get_sample_rate();
        } else if (ch == 'c') {
            adc.calibrateSysOffsetADC1(POS_PIN, NEG_PIN);
        } else if (ch == 's') {
            adc.calibrateSelfOffsetADC1();
        } else if (ch == 'b') {
            adc.setGain(ADS126X_GAIN_32);
        }
    }
}