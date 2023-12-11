package com.smit.InsightHub_Backend.Models.Entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity extends BaseEntity {
    private String username;
    private String email;
    private String password;
}
