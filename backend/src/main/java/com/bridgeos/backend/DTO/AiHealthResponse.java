package com.bridgeos.backend.DTO;

import lombok.Data;

@Data
public class AiHealthResponse {
    private String status;
    private String provider;
    private String model;
    private boolean sidecarReachable;
}
