package com.smit.InsightHub_Backend.Services.Interfaces;

import com.smit.InsightHub_Backend.Models.RequestModels.LoginRequestModel;
import com.smit.InsightHub_Backend.Models.RequestModels.RegisterRequestModel;
import com.smit.InsightHub_Backend.Models.RequestModels.VerificationRequestModel;
import com.smit.InsightHub_Backend.Models.ResponseModels.LoginResponseModel;
import com.smit.InsightHub_Backend.Models.ResponseModels.RegisterResponseModel;

public interface IAuthenticationService {
    LoginResponseModel login(LoginRequestModel loginRequestModel);

    RegisterResponseModel register(RegisterRequestModel loginRequestModel);

    LoginResponseModel verify(VerificationRequestModel verificationRequest);
}
