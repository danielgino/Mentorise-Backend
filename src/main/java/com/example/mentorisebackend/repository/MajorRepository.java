package com.example.mentorisebackend.repository;

import com.example.mentorisebackend.api.entity.Major;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MajorRepository  extends JpaRepository<Major, Long> {

    boolean existsByName(String name);



}



