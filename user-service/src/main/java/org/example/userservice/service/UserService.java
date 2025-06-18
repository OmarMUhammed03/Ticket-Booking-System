package org.example.userservice.service;

import org.example.userservice.dto.CreateUserDto;
import org.example.userservice.dto.UserResponseDto;
import org.example.userservice.mapper.UserMapper;
import org.example.userservice.model.User;
import org.example.userservice.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserCreatedDto)
                .toList();
    }

    public Optional<UserResponseDto> createUser(final CreateUserDto createUserDto) {
        User user = UserMapper.toUser(createUserDto);
        if (!user.isValid()) {
            return Optional.empty();
        }
        userRepository.save(user);
        return Optional.of(UserMapper.toUserCreatedDto(user));
    }

    public Optional<UserResponseDto> getUserById(final UUID userId) {
        return userRepository.findById(userId)
                .map(UserMapper::toUserCreatedDto);
    }

    public Optional<UserResponseDto> deleteUser(final UUID userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            userRepository.deleteById(userId);
            return user.map(UserMapper::toUserCreatedDto);
        }
        return Optional.empty();
    }

}
