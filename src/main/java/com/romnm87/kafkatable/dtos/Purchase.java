package com.romnm87.kafkatable.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Purchase implements Serializable {
    private String id;
    private String name;
    private double price;
    private String currency = "RUR";
    private long timestamp;
}
