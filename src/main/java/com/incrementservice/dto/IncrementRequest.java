package com.incrementservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

/**
 * Represents the request data for incrementing a value in Redis.
 */
@Data
@Builder
public class IncrementRequest {

    /**
     * The Redis key.
     */
    @NotBlank(message = "Key is mandatory")
    private String key;

    /**
     * The value to increment.
     */
    @NotNull(message = "Value is mandatory")
    private Integer value;
}
