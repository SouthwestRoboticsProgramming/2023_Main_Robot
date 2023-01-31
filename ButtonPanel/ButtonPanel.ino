#define SELECT_PORT PORTB
#define SELECT_DDR DDRB
#define LIGHT_PORT PORTD
#define LIGHT_DDR DDRD
#define BUTTON_PIN PINF
#define BUTTON_DDR DDRF

#define SWITCH_PIN 10

#define MATRIX_GROUP_COUNT 6
#define MATRIX_GROUP_SIZE 6

#define PERSIST_TIME 2.5

#define PACKET_BEGIN 0xA5

uint8_t packet_buf_in[MATRIX_GROUP_COUNT];
uint8_t packet_buf_out[MATRIX_GROUP_COUNT + 1];

bool waiting_for_start = false;
uint8_t read_idx = 0;

uint8_t light_data[MATRIX_GROUP_COUNT];

// Since Teensy does not have pins F2 and F3 exposed, we need to shift
// the bits around to account for it
// B | 1 2 . . 3 4 5 6
// L | 1 2 3 4 5 6
inline uint8_t shift_down_buttons(uint8_t buttons_group) {
  return (buttons_group & 3) | ((buttons_group & 0xF0) >> 2);
}

void setup() {
  uint8_t size_mask = (1 << MATRIX_GROUP_SIZE) - 1;
  
  // Set as outputs
  SELECT_DDR = size_mask;
  LIGHT_DDR = size_mask;

  // Set as input
  BUTTON_DDR = 0;

  Serial.begin(115200);
  packet_buf_out[0] = PACKET_BEGIN;

  memset(light_data, 0, sizeof(light_data));

  pinMode(SWITCH_PIN, INPUT);
}

// All code here must run as fast as possible to avoid flickering lights
void loop() {
  // Read in light data changes
  if (Serial.available()) {
    uint8_t in = Serial.read();
    if (waiting_for_start) {
      if (in == PACKET_BEGIN) {
        waiting_for_start = false;
        read_idx = 0;
      }
    } else {
      packet_buf_in[read_idx++] = in;
      if (read_idx == 6) {
        memcpy(light_data, packet_buf_in, 6);
        waiting_for_start = true;
      }
    }
  }

  for (uint8_t group = 0; group < MATRIX_GROUP_COUNT; group++) {
    SELECT_PORT = 1 << group;
    delayMicroseconds(20); // Give the select pins some time to switch
    
    uint8_t buttons_group = shift_down_buttons(BUTTON_PIN);
    LIGHT_PORT = light_data[group];

    packet_buf_out[group + 1] = buttons_group;

    delay(PERSIST_TIME);
  }

  // Dump button state over serial, will be decoded by ShuffleLog

  if (digitalRead(SWITCH_PIN))
    packet_buf_out[1] |= 128;
  Serial.write(packet_buf_out, sizeof(packet_buf_out));
}
