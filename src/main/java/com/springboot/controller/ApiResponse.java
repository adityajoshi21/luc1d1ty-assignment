package com.springboot.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse {

    @JsonProperty("response_msg")
    private String responseMsg;  // Maps to "response_msg" in JSON pf request
}
