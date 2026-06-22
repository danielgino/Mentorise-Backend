package com.example.mentorisebackend.service.media;

import com.cloudinary.Cloudinary;
import com.example.mentorisebackend.api.entity.User;
import com.example.mentorisebackend.dto.user.UpdateProfileImageRequest;
import com.example.mentorisebackend.exception.BadRequestException;
import com.example.mentorisebackend.exception.UserNotFoundException;
import com.example.mentorisebackend.repository.UserRepository;
import com.example.mentorisebackend.util.AppConstants;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Objects;


@Service
@RequiredArgsConstructor
public class ProfileImageService {

    private final Cloudinary cloudinary;
    private final UserRepository userRepository;

    @Transactional
    public Map<String, String> updateProfileImageByEmail(Long userId, UpdateProfileImageRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(AppConstants.USER_NOT_FOUND));

        String newUrl = request.getProfileImageUrl();
        String newPublicId = request.getProfileImagePublicId();

        if (newUrl == null || newUrl.isBlank()) {
            throw new BadRequestException(AppConstants.PROFILE_IMAGE_URL_REQUIRED);
        }

        if (newPublicId == null || newPublicId.isBlank()) {
            throw new BadRequestException(AppConstants.PROFILE_IMAGE_PUBLIC_ID_REQUIRED);
        }

        String oldPublicId = user.getProfileImagePublicId();

        user.setProfileImageUrl(newUrl);
        user.setProfileImagePublicId(newPublicId);

        if (oldPublicId != null && !oldPublicId.isBlank() && !Objects.equals(oldPublicId, newPublicId)) {
            try {
                cloudinary.uploader().destroy(oldPublicId, Map.of("invalidate", true));
            } catch (Exception ignored) {
                // אפשר להוסיף לוג
            }
        }

        return Map.of(
                "profileImageUrl", user.getProfileImageUrl(),
                "profileImagePublicId", user.getProfileImagePublicId()
        );
    }

    @Transactional
    public void deleteProfileImageByEmail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(AppConstants.USER_NOT_FOUND));

        String publicId = user.getProfileImagePublicId();

        if (publicId != null && !publicId.isBlank()) {
            try {
                cloudinary.uploader().destroy(publicId, Map.of("invalidate", true));
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Cloudinary delete failed", e);
            }
        }

        user.setProfileImageUrl(null);
        user.setProfileImagePublicId(null);
    }
}