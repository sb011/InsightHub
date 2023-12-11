package com.smit.InsightHub_Backend.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smit.InsightHub_Backend.Models.RequestModels.ResetPasswordRequestModel;
import com.smit.InsightHub_Backend.Models.RequestModels.SendForgotPasswordLinkRequestModel;
import com.smit.InsightHub_Backend.Models.RequestModels.ForgotPasswordRequestModel;
import com.smit.InsightHub_Backend.Models.RequestModels.LoginRequestModel;
import com.smit.InsightHub_Backend.Models.RequestModels.OtpRequestModel;
import com.smit.InsightHub_Backend.Models.RequestModels.RegisterRequestModel;
import com.smit.InsightHub_Backend.Models.RequestModels.VerificationRequestModel;
import com.smit.InsightHub_Backend.Models.ResponseModels.LoginResponseModel;
import com.smit.InsightHub_Backend.Models.ResponseModels.RegisterResponseModel;
import com.smit.InsightHub_Backend.Services.Interfaces.IAuthenticationService;

@RestController
@RequestMapping("/api/authentication")
public class AuthenticationController {
    private final IAuthenticationService authenticationService;

    @Autowired
    public AuthenticationController(IAuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public LoginResponseModel login(@RequestBody LoginRequestModel loginRequest) {
        return authenticationService.login(loginRequest);
    }

    @PostMapping("/register")
    public RegisterResponseModel register(@RequestBody RegisterRequestModel registerRequest) {
        return authenticationService.register(registerRequest);
    }

    @PostMapping("/verify")
    public LoginResponseModel verify(@RequestBody VerificationRequestModel verificationRequest) {
        return authenticationService.verify(verificationRequest);
    }

    @PostMapping("/resend-otp")
    public void resendOTP(@RequestBody OtpRequestModel verificationRequest) {
        authenticationService.sendOTP(verificationRequest);
    }

    @PostMapping("/send-forgot-password-link")
    public void sendForgotPasswordLink(
            @RequestBody SendForgotPasswordLinkRequestModel sendForgotPasswordLinkRequestModel) {
        authenticationService.sendForgotPasswordLink(sendForgotPasswordLinkRequestModel);
    }

    @PostMapping("/forgot-password")
    public void forgotPassword(@RequestBody ForgotPasswordRequestModel forgotPasswordRequestModel) {
        authenticationService.forgotPassword(forgotPasswordRequestModel);
    }

    @PostMapping("/reset-password")
    public void resetPassword(@RequestBody ResetPasswordRequestModel resetPasswordRequestModel) {
        authenticationService.resetPassword(resetPasswordRequestModel);
    }
}
