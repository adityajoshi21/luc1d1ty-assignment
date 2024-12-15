package com.springboot.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class OfferRequest {

    @JsonProperty("restaurant_id")
    private int restaurantId;

    @JsonProperty("offer_type")
    private String offerType;

    @JsonProperty("offer_value")
    private int offerValue;

    @JsonProperty("customer_segment")
    private List<String> customerSegment;
}