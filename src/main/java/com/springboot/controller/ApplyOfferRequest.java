package com.springboot.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplyOfferRequest {

    @JsonProperty("cart_value")
    private int cartValue;  // Maps to "cart_value" in JSON of incoming request

    @JsonProperty("restaurant_id")
    private int restaurantId;  // Maps to "restaurant_id" in JSON

    @JsonProperty("user_id")
    private int userId;  // Maps to "user_id" in JSON
}

