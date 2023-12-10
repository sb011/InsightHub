package com.smit.InsightHub_Backend.Services.Interfaces;

import com.smit.InsightHub_Backend.Models.RequestModels.LoginRequestModel;
import com.smit.InsightHub_Backend.Models.RequestModels.RegisterRequestModel;
import com.smit.InsightHub_Backend.Models.ResponseModels.LoginResponseModel;

public interface IAuthenticationService {
    LoginResponseModel login(LoginRequestModel loginRequestModel);

    LoginResponseModel register(RegisterRequestModel loginRequestModel);
}
