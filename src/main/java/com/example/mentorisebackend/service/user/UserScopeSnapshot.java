package com.example.mentorisebackend.service.user;

import com.example.mentorisebackend.enums.ScopeType;

import java.util.List;

public record UserScopeSnapshot(ScopeType mode, List<Integer> years, List<Long> courseIds) {}
