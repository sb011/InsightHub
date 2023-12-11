package com.smit.InsightHub_Backend.Models.RequestModels;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificationRequestModel {
    String code;
    String userId;
}
