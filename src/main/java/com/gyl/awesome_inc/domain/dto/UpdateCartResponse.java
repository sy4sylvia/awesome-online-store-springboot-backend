package com.gyl.awesome_inc.domain.dto;

import lombok.Data;

@Data
public class UpdateCartResponse {
    private String productId;
    private String quantity;
}