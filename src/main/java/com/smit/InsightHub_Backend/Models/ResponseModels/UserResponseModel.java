package com.smit.InsightHub_Backend.Models.ResponseModels;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseModel {
    private String id;
    private String username;
    private String email;
}
