#include <ADS1256.h>
#include <SPI.h>
#include <filters.h>
#include <limits.h>

// FILTER

const float cutoff_freq = 20.0;
const float sampling_time = 1 / 400.0;
IIR::ORDER order = IIR::ORDER::OD4;

Filter filter(cutoff_freq, sampling_time, order);

// ADC

#define PIN_DRDY 9

float clockMHZ = 7.68; // crystal frequency used on ADS1256
float vRef = 2.5;      // voltage reference

// Construct and init ADS1256 object
ADS1256 adc(clockMHZ, vRef, false); // RESETPIN is permanently tied to 3.3v

// SAMPLING

const int SAMPLE_RATES[5] = { 20, 40, 60, 100, 200 };

int sample_rate;
long sample_time_micros;

// RUNTIME

unsigned long last_time;
unsigned long current_time;

double reading;
long double sum;
int count;
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

    Serial.print("Selected ");
    Serial.print(sample_rate);
    Serial.println(" sps");

    last_time = 0;
    sum = 0;
    count = 0;
    log_num = 0;
    shift = 0;
}

void setup()
{
    Serial.begin(115200);

    get_sample_rate();

    adc.begin(ADS1256_DRATE_500SPS, ADS1256_GAIN_64, false);
}

void loop()
{
    if (!(digitalRead(PIN_DRDY))) {
        reading = filter.filterIn((adc.readCurrentChannel() / (2.0 * vRef)) * 0x7FFFFF);
        sum += reading;
        count++;
    }

    current_time = micros();

    if (abs(current_time - last_time) >= sample_time_micros + shift) {
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
        }
    }
}
