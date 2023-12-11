package com.smit.InsightHub_Backend.Models.RequestModels;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestModel {
    private String id;
    private String email;
    private String username;
    private String password;
}
