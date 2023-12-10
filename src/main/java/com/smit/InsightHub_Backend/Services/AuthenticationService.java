package com.smit.InsightHub_Backend.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.smit.InsightHub_Backend.Exceptions.BadRequestException;
import com.smit.InsightHub_Backend.Exceptions.NotFoundException;
import com.smit.InsightHub_Backend.Helpers.JWTHelper;
import com.smit.InsightHub_Backend.Models.Entities.UserEntity;
import com.smit.InsightHub_Backend.Models.RequestModels.LoginRequestModel;
import com.smit.InsightHub_Backend.Models.RequestModels.RegisterRequestModel;
import com.smit.InsightHub_Backend.Models.ResponseModels.LoginResponseModel;
import com.smit.InsightHub_Backend.Repositories.Interfaces.IUserRepository;
import com.smit.InsightHub_Backend.Services.Interfaces.IAuthenticationService;

@Service
public class AuthenticationService implements IAuthenticationService {
    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTHelper jwtHelper;

    @Autowired
    public AuthenticationService(IUserRepository userRepository, PasswordEncoder passwordEncoder,
            JWTHelper jwtHelper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtHelper = jwtHelper;
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
    public LoginResponseModel register(RegisterRequestModel loginRequestModel) {
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

        String token = jwtHelper.generateToken(user.getId());
        var response = new LoginResponseModel(token);

        return response;
    }

}
