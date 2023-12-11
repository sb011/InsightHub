package com.smit.InsightHub_Backend.Services.Interfaces;

import com.smit.InsightHub_Backend.Models.RequestModels.ResetPasswordRequestModel;
import com.smit.InsightHub_Backend.Models.RequestModels.SendForgotPasswordLinkRequestModel;
import com.smit.InsightHub_Backend.Models.RequestModels.ForgotPasswordRequestModel;
import com.smit.InsightHub_Backend.Models.RequestModels.LoginRequestModel;
import com.smit.InsightHub_Backend.Models.RequestModels.OtpRequestModel;
import com.smit.InsightHub_Backend.Models.RequestModels.RegisterRequestModel;
import com.smit.InsightHub_Backend.Models.RequestModels.VerificationRequestModel;
import com.smit.InsightHub_Backend.Models.ResponseModels.LoginResponseModel;
import com.smit.InsightHub_Backend.Models.ResponseModels.RegisterResponseModel;

public interface IAuthenticationService {
    LoginResponseModel login(LoginRequestModel loginRequestModel);

    RegisterResponseModel register(RegisterRequestModel loginRequestModel);

    LoginResponseModel verify(VerificationRequestModel verificationRequest);

    void sendOTP(OtpRequestModel verificationRequest);

    void sendForgotPasswordLink(SendForgotPasswordLinkRequestModel sendForgotPasswordLinkRequestModel);

    void forgotPassword(ForgotPasswordRequestModel forgotPasswordRequestModel);

    void resetPassword(ResetPasswordRequestModel resetPasswordRequestModel);
}
