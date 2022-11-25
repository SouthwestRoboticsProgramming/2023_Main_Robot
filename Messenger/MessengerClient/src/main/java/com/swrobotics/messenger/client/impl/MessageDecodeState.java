package com.swrobotics.messenger.client.impl;

public enum MessageDecodeState {
    TYPE_LENGTH,
    TYPE,
    DATA_LENGTH,
    DATA
}
