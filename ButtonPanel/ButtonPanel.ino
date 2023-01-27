#define SELECT_PORT PORTB
#define SELECT_DDR DDRB
#define LIGHT_PORT PORTD
#define LIGHT_DDR DDRD
#define BUTTON_PIN PINF
#define BUTTON_DDR DDRF

#define MATRIX_GROUP_COUNT 6
#define MATRIX_GROUP_SIZE 6

//#define PERSIST_TIME 250
#define PERSIST_TIME 2.5

// Converts from button inputs on BUTTON_PIN to lights on LIGHT_PORT
// Since Teensy does not have pins F2 and F3 exposed, we need to shift
// the bits around to account for it
// B | 1 2 . . 3 4 5 6
// L | 1 2 3 4 5 6
inline uint8_t buttons_to_lights(uint8_t buttons_group) {
  return (buttons_group & 3) | ((buttons_group & 0xF0) >> 2);
}

void setup() {
  uint8_t size_mask = (2 << MATRIX_GROUP_SIZE) - 1;
  
  // Set as outputs
  SELECT_DDR = size_mask;
  LIGHT_DDR = size_mask;

  // Set as input
  BUTTON_DDR = 0;
}

void loop() {
  for (uint8_t group = 0; group < MATRIX_GROUP_COUNT; group++) {
    SELECT_PORT = 1 << group;
    delayMicroseconds(20); // Give the select pins some time to switch
    LIGHT_PORT = buttons_to_lights(BUTTON_PIN); // Lights on if button isn't pressed
    delay(PERSIST_TIME);
  }
}
