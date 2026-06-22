package com.example.mentorisebackend.service.auth;

import com.example.mentorisebackend.api.entity.Major;
import com.example.mentorisebackend.api.entity.User;
import com.example.mentorisebackend.dto.auth.LoginRequestDto;
import com.example.mentorisebackend.dto.auth.RegisterDto;
import com.example.mentorisebackend.dto.auth.UserLoginResponseDto;
import com.example.mentorisebackend.enums.Role;
import com.example.mentorisebackend.exception.BadRequestException;
import com.example.mentorisebackend.exception.DuplicateFieldException;
import com.example.mentorisebackend.exception.ResourceNotFoundException;
import com.example.mentorisebackend.repository.MajorRepository;
import com.example.mentorisebackend.repository.UserRepository;
import com.example.mentorisebackend.security.UserPrincipal;
import com.example.mentorisebackend.util.AppConstants;
import com.example.mentorisebackend.security.jwt.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAuthService {


        private final UserRepository userRepository;
        private final MajorRepository majorRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;



    public UserLoginResponseDto login(LoginRequestDto request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        Long   userId = principal.getUserId();
        String email  = principal.getUsername();
        Role   role   = Role.valueOf(
                principal.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "")
        );

        String token = jwtUtil.generateToken(email);
        User user = userRepository.findByIdWithMajor(userId).orElseThrow();
        Long majorId = (user.getMajor() != null) ? user.getMajor().getId() : null;

        return new UserLoginResponseDto(token, userId, email, role, user.getFullName(), majorId);
    }

    @Transactional
        public Long register(RegisterDto dto) {
            String email = dto.getEmail().trim().toLowerCase();
            String nationalId = dto.getNationalId().trim();
            String firstName = dto.getFirstName().trim();
            String lastName = dto.getLastName().trim();
            String phone = normalizeIlPhone(dto.getPhoneNumber());

            if (userRepository.existsByNationalId(nationalId)) {
                throw new DuplicateFieldException("nationalId", nationalId, AppConstants.NATIONAL_ID_ALREADY_EXISTS_SIMPLE);
            }
            if (userRepository.existsByEmail(email)) {
                throw new DuplicateFieldException("email", email, AppConstants.EMAIL_ALREADY_EXISTS);
            }
            if (userRepository.existsByPhoneNumber(phone)) {
                throw new DuplicateFieldException("phoneNumber", phone, AppConstants.PHONE_NUMBER_ALREADY_EXISTS);
            }

            Major major = majorRepository.findById(dto.getMajorId())
                    .orElseThrow(() -> new ResourceNotFoundException("majorId", dto.getMajorId()));

            // 4) בניית וסימון ערכים בטוחים
            User user = User.builder()
                    .nationalId(nationalId)
                    .email(email)
                    .passwordHash(passwordEncoder.encode(dto.getPassword()))
                    .firstName(firstName)
                    .lastName(lastName)
                    .phoneNumber(phone)
                    .role(Role.STUDENT)
                    .isAlumni(dto.isAlumni())
                    .major(major)
                    .build();

            try {
                userRepository.saveAndFlush(user); // חשוב להעלות את ה-UNIQUE עכשיו
                return user.getId();
            } catch (DataIntegrityViolationException ex) {
                Throwable root = NestedExceptionUtils.getMostSpecificCause(ex);
                if (root instanceof org.hibernate.exception.ConstraintViolationException cve) {
                    String name = cve.getConstraintName();
                    if (AppConstants.CONSTRAINT_EMAIL_UNIQUE.equalsIgnoreCase(name))
                        throw new DuplicateFieldException("email", user.getEmail(), AppConstants.EMAIL_ALREADY_EXISTS);
                    if (AppConstants.CONSTRAINT_NATIONAL_ID_UNIQUE.equalsIgnoreCase(name))
                        throw new DuplicateFieldException("nationalId", user.getNationalId(), AppConstants.NATIONAL_ID_ALREADY_EXISTS_SIMPLE);
                    if (AppConstants.CONSTRAINT_PHONE_NUMBER_UNIQUE.equalsIgnoreCase(name))
                        throw new DuplicateFieldException("phoneNumber", user.getPhoneNumber(), AppConstants.PHONE_NUMBER_ALREADY_EXISTS);
              }
                 throw new DuplicateFieldException("unknown", "", AppConstants.REQUEST_EMPTY);

            }
        }

    private String normalizeIlPhone(String raw) {
        if (raw == null) return null;

        String s = raw.trim().replaceAll("[^0-9+]", "");
        if (s.isEmpty()) return null;

        // המרה מ-+972 או 972 לקידומת 0
        if (s.startsWith("+972")) {
            s = "0" + s.substring(4);
        } else if (s.startsWith("972")) {
            s = "0" + s.substring(3);
        }

        // אם נשאר פלוס או תווים חריגים
        if (s.startsWith("+")) {
            s = s.substring(1);
        }

        // ולידציה – לוודא שזה 10 ספרות ומתחיל ב-0
        if (!s.matches(AppConstants.PHONE_VALIDATION_PATTERN)) {
            throw new BadRequestException(AppConstants.INVALID_ISRAELI_PHONE_FORMAT + raw);
        }

        return s;
    }


}


