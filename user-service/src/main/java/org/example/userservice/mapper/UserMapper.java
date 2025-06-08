package org.example.userservice.mapper;

import org.example.userservice.dto.CreateUserDto;
import org.example.userservice.dto.UserResponseDto;
import org.example.userservice.model.User;

public class UserMapper {
    public static UserResponseDto toUserCreatedDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getEmail(),
                user.getFirstname(),
                user.getLastname(),
                user.getDateOfBirth(),
                user.getProfilePictureUrl(),
                user.getGender()
        );
    }

    public static User toUser(CreateUserDto dto) {
        User user = new User();
        user.setId(dto.getId());
        user.setEmail(dto.getEmail());
        user.setFirstname(dto.getFirstname());
        user.setLastname(dto.getLastname());
        user.setDateOfBirth(dto.getDateOfBirth());
        user.setProfilePictureUrl(dto.getProfilePictureUrl());
        user.setGender(dto.getGender());
        return user;
    }
}

