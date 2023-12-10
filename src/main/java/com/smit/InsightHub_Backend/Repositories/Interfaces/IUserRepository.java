package com.smit.InsightHub_Backend.Repositories.Interfaces;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.smit.InsightHub_Backend.Models.Entities.UserEntity;

public interface IUserRepository extends MongoRepository<UserEntity, String> {
    UserEntity findByUsername(String username);

    UserEntity findByEmail(String email);
}
