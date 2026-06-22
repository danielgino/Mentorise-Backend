package com.example.mentorisebackend.service.admin;


import com.example.mentorisebackend.api.entity.User;
import com.example.mentorisebackend.dto.admin.CreateAdminDto;
import com.example.mentorisebackend.dto.user.UserDto;
import com.example.mentorisebackend.dto.admin.MeResponseDto;
import com.example.mentorisebackend.dto.admin.UserUpdateDto;
import com.example.mentorisebackend.enums.Role;
import com.example.mentorisebackend.exception.BadRequestException;
import com.example.mentorisebackend.exception.DuplicateFieldException;
import com.example.mentorisebackend.exception.UserNotFoundException;
import com.example.mentorisebackend.mapper.UserMapper;
import com.example.mentorisebackend.repository.UserRepository;
import com.example.mentorisebackend.security.UserPrincipal;
import com.example.mentorisebackend.service.tutor.TutorService;
import com.example.mentorisebackend.util.AppConstants;
import com.example.mentorisebackend.security.AuthUtil;
import com.example.mentorisebackend.util.DateFilterUtil;
import jakarta.persistence.criteria.Expression;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AdminUserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final TutorService tutorService;

    public AdminUserService(UserRepository userRepository, PasswordEncoder passwordEncoder, TutorService tutorService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tutorService = tutorService;
        this.userMapper = new UserMapper();
    }

    public UserDto getUserById(Long userId){
        User user = userRepository.findByIdWithMajor(userId)
                .orElseThrow(() -> new UserNotFoundException(AppConstants.USER_NOT_FOUND));
        return userMapper.toDto(user);
    }

@Transactional(readOnly = true)
public MeResponseDto getCurrentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal principal = (UserPrincipal) auth.getPrincipal();

    Long   userId = principal.getUserId();
    String email  = principal.getUsername();
    Role   role   = Role.valueOf(
            principal.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "")
    );

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(AppConstants.USER_NOT_FOUND));

    return new MeResponseDto(userId, user.getFullName(), email, role);
}
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(AppConstants.USER_NOT_FOUND));

        userRepository.delete(user);
    }

    @Transactional
    public UserDto updateUserPartial(Long userId, UserUpdateDto patch) {
        User user = userRepository.findByIdWithMajor(userId)
                .orElseThrow(() -> new UserNotFoundException(AppConstants.USER_NOT_FOUND_WITH_ID + userId));

        if (patch.getFirstName() != null) {
            user.setFirstName(patch.getFirstName());
        }
        if (patch.getLastName() != null) {
            user.setLastName(patch.getLastName());
        }
        if (patch.getEmail() != null && !patch.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmailAndIdNot(patch.getEmail(), user.getId())) {
                throw new DuplicateFieldException("email", patch.getEmail(), AppConstants.EMAIL_ALREADY_IN_USE_ADMIN);
            }
            user.setEmail(patch.getEmail());
        }
        if (patch.getPhoneNumber() != null) {
            user.setPhoneNumber(patch.getPhoneNumber());
        }
        if (patch.getRole() == Role.TUTOR) {
            throw new BadRequestException(AppConstants.CANNOT_PROMOTE_TO_TUTOR_VIA_ADMIN);
        }
        if (patch.getRole() != null && patch.getRole() != Role.ADMIN) {
            user.setRole(patch.getRole());
        }
        if (patch.getAlumni() != null) {
            user.setAlumni(patch.getAlumni());
        }


        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    @Transactional
    public UserDto createAdmin(CreateAdminDto request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateFieldException("email", request.getEmail(), AppConstants.EMAIL_ALREADY_IN_USE);
        }
        if (userRepository.existsByNationalId(request.getNationalId())) {
            throw new DuplicateFieldException("nationalId", request.getNationalId(), AppConstants.NATIONAL_ID_ALREADY_EXISTS);

        }

        User user = new User();
        user.setNationalId(request.getNationalId());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAlumni(false);
        user.setRole(Role.ADMIN);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));


        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    public Page<UserDto> getUsersAndSearch(
            int page,
            int size,
            String search,
            Role role,
            Boolean isAlumni,
            String joinDateFilter
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Specification<User> spec = (root, query, cb) -> cb.conjunction();
        if (search != null && !search.isBlank()) {
            String like = "%" + search.toLowerCase() + "%";

            spec = spec.and((root, query, cb) -> {
                Expression<String> firstNameExpr = cb.lower(root.get("firstName"));
                Expression<String> lastNameExpr  = cb.lower(root.get("lastName"));

                Expression<String> fullNameExpr = cb.concat(
                        cb.concat(firstNameExpr, " "),
                        lastNameExpr
                );

                return cb.or(
                        cb.like(firstNameExpr, like),
                        cb.like(lastNameExpr, like),
                        cb.like(fullNameExpr, like),
                        cb.like(cb.lower(root.get("nationalId")), like)
                );
            });
        }
        if (role != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("role"), role)
            );
        }
        if (isAlumni != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("isAlumni"), isAlumni)
            );
        }

        LocalDateTime createdFrom = DateFilterUtil.parseJoinDateFilter(joinDateFilter);
        if (createdFrom != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("createdAt"), createdFrom)
            );
        }
        Page<User> result = userRepository.findAll(spec, pageable);
        return result.map(userMapper::toDto);
    }

    @Transactional
    public UserDto revokeTutorPermissions(Long userId) {
        User user = userRepository.findByIdWithMajor(userId)
                .orElseThrow(() -> new UserNotFoundException(AppConstants.USER_NOT_FOUND_WITH_ID + userId));

        if (user.getRole() != Role.TUTOR) {
            throw new BadRequestException(AppConstants.USER_IS_NOT_A_TUTOR);
        }

        user.setRole(Role.STUDENT);
        User saved = userRepository.save(user);

        tutorService.deactivateTutorForUser(userId);

        return userMapper.toDto(saved);
    }

    // Removed: getJoinDateFilter() method - now using DateFilterUtil.parseJoinDateFilter()

//////////מתודה שמחזיר משתמש ולא משתמש DTO.
    public User getCurrentUserEntity() {
        return AuthUtil.getCurrentUserEntity(userRepository);
    }

}
