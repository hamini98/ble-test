package com.example.bletest.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DeviceValueDto {

    private String value;
    private String recDate;

    public DeviceValueDto(String value, String recDate) {
        this.value = value;
        this.recDate = recDate;
    }

    public String getValue() {
        return this.value;
    }

    public String getRecDate() {
        return this.recDate;
    }
}
