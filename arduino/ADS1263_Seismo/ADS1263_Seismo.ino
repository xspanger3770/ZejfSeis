#include <ADS126X.h>
#include <SPI.h>
#include <filters.h>
#include <limits.h>

// FILTER

float cutoff_freq = 20.0;
const float sampling_time = 1 / 1200.0;
IIR::ORDER order = IIR::ORDER::OD4;

Filter filter(cutoff_freq, sampling_time, order);

// ADC

#define PIN_DRDY 9
#define PIN_CS 10

#define POS_PIN ADS126X_AIN1
#define NEG_PIN ADS126X_AIN0
#define ADS_SAMPLE_RATE ADS126X_RATE_1200

ADS126X adc;

// SAMPLING

const int SAMPLE_RATES[5] = { 20, 40, 60, 100, 200 };

int sample_rate;
long sample_time_micros;

// RUNTIME

#define CALIBRATION_SECONDS 20
#define IGNORE 20

bool calibrating;
long offset;

unsigned long last_time;
unsigned long current_time;

long double sum;
unsigned int count;
long value;

byte log_num;
int shift;

char ch;

void get_sample_rate()
{
    int index = 1;
    Serial.println("Please select sample rate");
    while (true) {
        char ch = Serial.read();
        index = ch - '0';
        if (index >= 0 && index < 5) {
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
    offset = 0;
    Serial.println("Offset calibration start, be patient...");
}

void setup()
{
    Serial.begin(115200);

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
        if (calibrating && count >= IGNORE) {
            sum += adc.readADC1(POS_PIN, NEG_PIN);
            count++;
        } else {
            sum += filter.filterIn(adc.readADC1(POS_PIN, NEG_PIN)) - offset;
            count++;
        }
    }

    current_time = micros();

    if (calibrating) {
        if (count % (1200 * 5) == 0 && rdy) {
            Serial.println("wait...");
        }
        if (count >= CALIBRATION_SECONDS * 1200) {
            calibrating = false;
            offset = round(sum / count);
            sum = 0;
            count = 0;
            Serial.print("Offset calibration finish, offset = ");
            Serial.println(offset);
        }
    } else if (abs(current_time - last_time) >= sample_time_micros + shift) {
        value = round(sum / count);
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
        ch = Serial.read();
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
