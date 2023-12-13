package com.smit.InsightHub_Backend.Services;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.smit.InsightHub_Backend.Exceptions.BadRequestException;
import com.smit.InsightHub_Backend.Exceptions.NotFoundException;
import com.smit.InsightHub_Backend.Helpers.EmailHelper;
import com.smit.InsightHub_Backend.Helpers.JWTHelper;
import com.smit.InsightHub_Backend.Helpers.RedisHelper;
import com.smit.InsightHub_Backend.Models.EmailDetails;
import com.smit.InsightHub_Backend.Models.Entities.UserEntity;
import com.smit.InsightHub_Backend.Models.RequestModels.ResetPasswordRequestModel;
import com.smit.InsightHub_Backend.Models.RequestModels.SendForgotPasswordLinkRequestModel;
import com.smit.InsightHub_Backend.Models.RequestModels.ForgotPasswordRequestModel;
import com.smit.InsightHub_Backend.Models.RequestModels.LoginRequestModel;
import com.smit.InsightHub_Backend.Models.RequestModels.OtpRequestModel;
import com.smit.InsightHub_Backend.Models.RequestModels.RegisterRequestModel;
import com.smit.InsightHub_Backend.Models.RequestModels.VerificationRequestModel;
import com.smit.InsightHub_Backend.Models.ResponseModels.LoginResponseModel;
import com.smit.InsightHub_Backend.Models.ResponseModels.RegisterResponseModel;
import com.smit.InsightHub_Backend.Models.ResponseModels.UserResponseModel;
import com.smit.InsightHub_Backend.Repositories.Interfaces.IUserRepository;
import com.smit.InsightHub_Backend.Services.Interfaces.IAuthenticationService;

@Service
public class AuthenticationService implements IAuthenticationService {
    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTHelper jwtHelper;
    private final RedisHelper redisHelper;
    private final EmailHelper emailHelper;

    @Autowired
    public AuthenticationService(IUserRepository userRepository, PasswordEncoder passwordEncoder,
            JWTHelper jwtHelper, RedisHelper redisHelper, EmailHelper emailHelper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtHelper = jwtHelper;
        this.redisHelper = redisHelper;
        this.emailHelper = emailHelper;
    }

    @Override
    public LoginResponseModel login(LoginRequestModel loginRequestModel) {
        // email null or empty
        if (loginRequestModel.getEmail().isEmpty() || loginRequestModel.getEmail() == null
                || loginRequestModel.getEmail().isBlank()) {
            throw new BadRequestException("Email is required");
        }

        if (loginRequestModel.getPassword().isEmpty() || loginRequestModel.getPassword() == null
                || loginRequestModel.getPassword().isBlank()) {
            throw new BadRequestException("Password is required");
        }

        var user = userRepository.findByEmail(loginRequestModel.getEmail());
        if (user == null) {
            throw new NotFoundException("Email or password is incorrect");
        }

        if (!passwordEncoder.matches(loginRequestModel.getPassword(), user.getPassword())) {
            throw new NotFoundException("Email or password is incorrect");
        }

        String token = jwtHelper.generateToken(user.getId());
        var response = new LoginResponseModel(token,
                new UserResponseModel(user.getId(), user.getUsername(), user.getEmail()));

        return response;
    }

    @Override
    public RegisterResponseModel register(RegisterRequestModel loginRequestModel) {
        if (loginRequestModel.getEmail().isEmpty() || loginRequestModel.getEmail() == null
                || loginRequestModel.getEmail().isBlank()) {
            throw new BadRequestException("Email is required");
        }

        if (loginRequestModel.getUsername().isEmpty() || loginRequestModel.getUsername() == null
                || loginRequestModel.getUsername().isBlank()) {
            throw new BadRequestException("Username is required");
        }

        if (loginRequestModel.getPassword().isEmpty() || loginRequestModel.getPassword() == null
                || loginRequestModel.getPassword().isBlank()) {
            throw new BadRequestException("Password is required");
        }

        var user = userRepository.findByEmail(loginRequestModel.getEmail());
        if (user != null) {
            throw new NotFoundException("Email is already taken");
        }

        user = userRepository.findByUsername(loginRequestModel.getUsername());
        if (user != null) {
            throw new NotFoundException("Username is already taken");
        }

        user = new UserEntity();
        user.setEmail(loginRequestModel.getEmail());
        user.setUsername(loginRequestModel.getUsername());
        user.setPassword(passwordEncoder.encode(loginRequestModel.getPassword()));

        user = userRepository.save(user);

        sendOTP(new OtpRequestModel(user.getId(), user.getEmail()));

        var response = new RegisterResponseModel(
                new UserResponseModel(user.getId(), user.getUsername(), user.getEmail()));
        return response;
    }

    @Override
    public LoginResponseModel verify(VerificationRequestModel verificationRequest) {
        if (verificationRequest.getCode().isEmpty() || verificationRequest.getCode() == null
                || verificationRequest.getCode().isBlank()) {
            throw new BadRequestException("Code is required");
        }

        var radisVerificationCode = redisHelper.getValue(verificationRequest.getUserId());
        if (radisVerificationCode == null) {
            throw new NotFoundException("Code is expired");
        }

        if (!verificationRequest.getCode().equals(radisVerificationCode)) {
            throw new NotFoundException("Code is invalid");
        }

        String token = jwtHelper.generateToken(verificationRequest.getUserId());
        var user = userRepository.findById(verificationRequest.getUserId()).get();
        var response = new LoginResponseModel(token,
                new UserResponseModel(user.getId(), user.getUsername(), user.getEmail()));

        user.setVerified(true);
        userRepository.save(user);

        return response;
    }

    @Override
    public void sendOTP(OtpRequestModel verificationRequest) {
        if (verificationRequest.getUserId().isEmpty() || verificationRequest.getUserId() == null
                || verificationRequest.getUserId().isBlank()) {
            throw new BadRequestException("User Id is required");
        }

        if (verificationRequest.getEmail().isEmpty() || verificationRequest.getEmail() == null
                || verificationRequest.getEmail().isBlank()) {
            throw new BadRequestException("Email is required");
        }

        var isUserPresent = userRepository.findById(verificationRequest.getUserId());
        if (isUserPresent == null) {
            throw new NotFoundException("User not found");
        }

        var isEmailPresent = userRepository.findByEmail(verificationRequest.getEmail());
        if (isEmailPresent == null) {
            throw new NotFoundException("Email is invalid");
        }

        var verificationCode = generateVerificationCode(000001, 999999);
        redisHelper.setValue(verificationRequest.getUserId(), verificationCode, 9000);

        // send email with verification code
        var subject = "InsightHub - Verify Your Email Address";
        var body = "Dear User,\n\n"
                + "Thank you for registering with our service. To complete the registration process, please use the following 6-digit verification code:\n\n"
                + "Verification Code: " + verificationCode + "\n\n"
                + "Please enter this code on the registration page to verify your email address.\n\n"
                + "If you did not request this code, please ignore this email.\n\n"
                + "Thank you,\n"
                + "InsightHub Team";

        var email = new EmailDetails();
        email.setSubject(subject);
        email.setMsgBody(body);
        email.setRecipient(verificationRequest.getEmail());
        emailHelper.sendSimpleMail(email);
    }

    @Override
    public void sendForgotPasswordLink(SendForgotPasswordLinkRequestModel sendForgotPasswordLinkRequestModel) {
        if (sendForgotPasswordLinkRequestModel.getEmail().isEmpty()
                || sendForgotPasswordLinkRequestModel.getEmail() == null
                || sendForgotPasswordLinkRequestModel.getEmail().isBlank()) {
            throw new BadRequestException("Email is required");
        }

        var user = userRepository.findByEmail(sendForgotPasswordLinkRequestModel.getEmail());
        if (user == null) {
            throw new NotFoundException("Email is invalid");
        }

        var subject = "InsightHub - Reset Your Password";
        var body = "Dear User,\n\n"
                + "We received a request to reset your password."
                + "http://localhost:3000/forgot-password/ " + user.getId() + ""
                + "If you did not request this code, please ignore this email.\n\n"
                + "Thank you,\n"
                + "InsightHub Team";

        var email = new EmailDetails();
        email.setSubject(subject);
        email.setMsgBody(body);
        email.setRecipient(sendForgotPasswordLinkRequestModel.getEmail());
        emailHelper.sendSimpleMail(email);
    }

    @Override
    public void forgotPassword(ForgotPasswordRequestModel forgotPasswordRequestModel) {
        if (forgotPasswordRequestModel.getId().isEmpty() || forgotPasswordRequestModel.getId() == null
                || forgotPasswordRequestModel.getId().isBlank()) {
            throw new BadRequestException("Email is required");
        }

        if (forgotPasswordRequestModel.getPassword().isEmpty() || forgotPasswordRequestModel.getPassword() == null
                || forgotPasswordRequestModel.getPassword().isBlank()) {
            throw new BadRequestException("Password is required");
        }

        var user = userRepository.findById(forgotPasswordRequestModel.getId()).get();
        if (user == null) {
            throw new NotFoundException("User not found");
        }

        user.setPassword(passwordEncoder.encode(forgotPasswordRequestModel.getPassword()));
        userRepository.save(user);
    }

    @Override
    public void resetPassword(ResetPasswordRequestModel resetPasswordRequestModel) {
        if (resetPasswordRequestModel.getEmail().isEmpty() || resetPasswordRequestModel.getEmail() == null
                || resetPasswordRequestModel.getEmail().isBlank()) {
            throw new BadRequestException("Email is required");
        }

        if (resetPasswordRequestModel.getOldPassword().isEmpty() || resetPasswordRequestModel.getOldPassword() == null
                || resetPasswordRequestModel.getOldPassword().isBlank()) {
            throw new BadRequestException("Old Password is required");
        }

        var user = userRepository.findByEmail(resetPasswordRequestModel.getEmail());
        if (user == null) {
            throw new NotFoundException("User not found");
        }

        if (resetPasswordRequestModel.getNewPassword().isEmpty() || resetPasswordRequestModel.getNewPassword() == null
                || resetPasswordRequestModel.getNewPassword().isBlank()) {
            throw new BadRequestException("New Password is required");
        }

        if (resetPasswordRequestModel.getReNewPassword().isEmpty()
                || resetPasswordRequestModel.getReNewPassword() == null
                || resetPasswordRequestModel.getReNewPassword().isBlank()) {
            throw new BadRequestException("Re-enter New Password is required");
        }

        if (!passwordEncoder.matches(resetPasswordRequestModel.getOldPassword(), user.getPassword())) {
            throw new NotFoundException("Old Password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(resetPasswordRequestModel.getNewPassword()));
        userRepository.save(user);
    }

    private static String generateVerificationCode(int MIN_CODE, int MAX_CODE) {
        Random random = new Random();
        int randomCode = random.nextInt((MAX_CODE - MIN_CODE) + 1) + MIN_CODE;
        return String.format("%06d", randomCode);
    }
}
