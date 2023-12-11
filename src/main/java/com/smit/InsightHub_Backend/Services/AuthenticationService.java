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
import com.smit.InsightHub_Backend.Models.RequestModels.LoginRequestModel;
import com.smit.InsightHub_Backend.Models.RequestModels.RegisterRequestModel;
import com.smit.InsightHub_Backend.Models.RequestModels.VerificationRequestModel;
import com.smit.InsightHub_Backend.Models.ResponseModels.LoginResponseModel;
import com.smit.InsightHub_Backend.Models.ResponseModels.RegisterResponseModel;
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
        if (loginRequestModel.getEmail() == null) {
            throw new BadRequestException("Email is required");
        }

        if (loginRequestModel.getPassword() == null) {
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
        var response = new LoginResponseModel(token);

        return response;
    }

    @Override
    public RegisterResponseModel register(RegisterRequestModel loginRequestModel) {
        if (loginRequestModel.getEmail() == null) {
            throw new BadRequestException("Email is required");
        }

        if (loginRequestModel.getUsername() == null) {
            throw new BadRequestException("Username is required");
        }

        if (loginRequestModel.getPassword() == null) {
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

        var verificationCode = generateVerificationCode(000001, 999999);
        redisHelper.setValue(user.getId(), verificationCode, 9000);

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
        email.setRecipient(user.getEmail());
        emailHelper.sendSimpleMail(email);

        var response = new RegisterResponseModel(user.getId());
        return response;
    }

    @Override
    public LoginResponseModel verify(VerificationRequestModel verificationRequest) {
        if (verificationRequest.getCode() == null) {
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
        var response = new LoginResponseModel(token);

        return response;
    }

    private static String generateVerificationCode(int MIN_CODE, int MAX_CODE) {
        Random random = new Random();
        int randomCode = random.nextInt((MAX_CODE - MIN_CODE) + 1) + MIN_CODE;
        return String.format("%06d", randomCode);
    }
}
