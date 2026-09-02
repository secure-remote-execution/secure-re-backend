package com.eci.secureproductchallenge.exception;

import java.util.UUID;

public class DeviceNotFoundException extends RuntimeException {

    public DeviceNotFoundException(UUID deviceId) {
        super("No se encontró el dispositivo con id " + deviceId);
    }
}
