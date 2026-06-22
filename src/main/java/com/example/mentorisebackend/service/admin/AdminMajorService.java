package com.example.mentorisebackend.service.admin;

import com.example.mentorisebackend.api.entity.Major;
import com.example.mentorisebackend.dto.major.MajorDto;
import com.example.mentorisebackend.exception.BadRequestException;
import com.example.mentorisebackend.exception.DuplicateFieldException;
import com.example.mentorisebackend.exception.ResourceNotFoundException;
import com.example.mentorisebackend.mapper.MajorMapper;
import com.example.mentorisebackend.repository.MajorRepository;
import com.example.mentorisebackend.util.AppConstants;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminMajorService {

    private final MajorRepository majorRepository;
    private final MajorMapper majorMapper;

    @Transactional
    public MajorDto createMajor(MajorDto request) {
        if (majorRepository.existsByName(request.getName())) {
            throw new DuplicateFieldException("name", request.getName(), AppConstants.ANOTHER_MAJOR_HAS_SAME_NAME);
        }
        Major major=new Major();
        major.setName(request.getName());
        Major saved = majorRepository.save(major);
        return majorMapper.toDto(saved);
    }
    @Transactional
    public MajorDto updateMajor(Long majorId, MajorDto patch) {
        Major major = majorRepository.findById(majorId)
                .orElseThrow(() -> new ResourceNotFoundException(AppConstants.MAJOR_NOT_FOUND_WITH_ID + majorId));

        if (patch.getName() != null) {
            major.setName(patch.getName());
        }

        Major saved = majorRepository.save(major);
        return majorMapper.toDto(saved);
    }
    public List<Major> getAllMajors() {
        return majorRepository.findAll();

    }

    public void deleteMajor(Long majorId) {
        throw new BadRequestException(AppConstants.CANNOT_DELETE_MAJOR);
    }
}
