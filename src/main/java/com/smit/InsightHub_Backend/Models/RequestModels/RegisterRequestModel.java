package com.smit.InsightHub_Backend.Models.RequestModels;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestModel {
    private String id;
    private String email;
    private String username;
    private String password;
}