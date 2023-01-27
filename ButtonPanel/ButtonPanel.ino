#define SELECT_PORT PORTB
#define SELECT_DDR DDRB
#define LIGHT_PORT PORTD
#define LIGHT_DDR DDRD

#define MATRIX_GROUP_COUNT 6
#define MATRIX_GROUP_SIZE 6

#define PERSIST_TIME 3

void setup() {
  uint8_t size_mask = (2 << MATRIX_GROUP_SIZE) - 1;
  SELECT_DDR = size_mask;
  LIGHT_DDR = size_mask;
}

void loop() {
  for (uint8_t group = 0; group < MATRIX_GROUP_COUNT; group++) {
    SELECT_PORT = 1 << group;
    LIGHT_PORT = 0; // All lights on

    delay(PERSIST_TIME);
  }
}
