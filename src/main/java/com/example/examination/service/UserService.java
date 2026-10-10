package com.example.examination.service;

import com.example.examination.dto.UserDTO;
import com.example.examination.dto.UserProfileUpdateDTO;
import com.example.examination.entity.User;
import com.example.examination.exception.BadRequestException;
import com.example.examination.exception.ResourceNotFoundException;
import com.example.examination.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserDTO> getAllStudents() {
        return userRepository.findByRoleOrderByCreatedAtDesc("ROLE_STUDENT")
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToDTO(user);
    }

    @Transactional
    public UserDTO updateUserProfile(Long userId, UserProfileUpdateDTO updateDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setName(updateDTO.getName().trim());
        user.setPhone(updateDTO.getPhone() != null ? updateDTO.getPhone().trim() : null);

        if (updateDTO.getNewPassword() != null && !updateDTO.getNewPassword().trim().isEmpty()) {
            if (updateDTO.getCurrentPassword() == null || !passwordEncoder.matches(updateDTO.getCurrentPassword(), user.getPassword())) {
                throw new BadRequestException("Current password does not match");
            }
            if (updateDTO.getNewPassword().trim().length() < 6) {
                throw new BadRequestException("New password must be at least 6 characters long");
            }
            user.setPassword(passwordEncoder.encode(updateDTO.getNewPassword().trim()));
        }

        User updatedUser = userRepository.save(user);
        return mapToDTO(updatedUser);
    }

    @Transactional
    public void deleteStudent(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        if ("ROLE_ADMIN".equals(user.getRole())) {
            throw new BadRequestException("Cannot delete administrator account");
        }
        userRepository.delete(user);
    }

    public UserDTO mapToDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getName(),
                user.getStudentId(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
