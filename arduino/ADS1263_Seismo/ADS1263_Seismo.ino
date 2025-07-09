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

#define ADS_output_sample_rate ADS126X_RATE_1200
const double ads_output_sample_rate = 1200.0;
const double input_sampling_time = 1.0 / ads_output_sample_rate;

// OFFSET CALIBRATION

const int calibration_seconds = 20;
const int initial_ignore_seconds = 4;
const double continuous_offset_fade_time_seconds = 600.0;

// FILTER

float cutoff_freq = 20.0;
IIR::ORDER order = IIR::ORDER::OD4;

// INTERNAL (DO NOT CHANGE AFTER THIS LINE)
Filter filter(cutoff_freq, input_sampling_time, order);
ADS126X adc;
const int output_sample_rateS[5] = { 20, 40, 60, 100, 200 };

int output_sample_rate;
long output_sample_time_micros;

bool calibrating;

double offset = 0.0;
double offset_alpha = input_sampling_time / continuous_offset_fade_time_seconds;

unsigned long last_time;
unsigned long current_time;

double sum;
int32_t count;
int32_t calibration_count;

byte log_num;
int shift;

void get_output_sample_rate()
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
    output_sample_rate = output_sample_rateS[index];
    output_sample_time_micros = 1000000 / output_sample_rate;

    cutoff_freq = output_sample_rate / 2.0;
    filter.setCutoffFreqHZ(cutoff_freq);

    Serial.print("Selected ");
    Serial.print(output_sample_rate);
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

void resetADC() {
    adc.begin(PIN_CS);
    adc.setRate(ADS_output_sample_rate);
    adc.startADC1();
    adc.setGain(ADS126X_GAIN_32);
    adc.setFilter(ADS126X_SINC4);
    adc.setBiasMagnitude(ADS126X_BIAS_MAG_0);
    adc.enableInternalReference();
    adc.setReference(ADS126X_REF_NEG_INT, ADS126X_REF_POS_INT);
}

void setup()
{
    Serial.begin(115200);

    pinMode(PIN_DRDY, INPUT);

    get_output_sample_rate();

    resetADC();
}

void loop()
{
    current_time = micros();

    bool rdy = !(digitalRead(PIN_DRDY));
    if (rdy) {
        int32_t current_val = adc.readADC1(POS_PIN, NEG_PIN);
        if (calibrating) {
            if( calibration_count >= initial_ignore_seconds * ads_output_sample_rate) {
                sum += current_val;
                count++;
            }

            calibration_count++;

            if (count >= calibration_seconds * ads_output_sample_rate) {
                calibrating = false;
                offset = double(sum) / double(count);
                sum = 0.0;
                count = 0;
                Serial.print("Offset calibration finish, offset = ");
                Serial.println(offset);
            }
        } else {
            offset += offset_alpha * (current_val - offset);

            int32_t corrected = current_val - lround(offset);
            float y = filter.filterIn(corrected);

            sum += y;
            count += 1;
        }
    }

    if (!calibrating && (current_time - last_time) >= (unsigned long)output_sample_time_micros + shift) {
        int32_t value = count == 0 ? 0 : lround(sum / count);
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
            get_output_sample_rate();
        } else if (ch == 'c') {
            adc.calibrateSysOffsetADC1(POS_PIN, NEG_PIN);
        } else if (ch == 's') {
            adc.calibrateSelfOffsetADC1();
        } else if (ch == 'b') {
            adc.setGain(ADS126X_GAIN_32);
        }
    }
}