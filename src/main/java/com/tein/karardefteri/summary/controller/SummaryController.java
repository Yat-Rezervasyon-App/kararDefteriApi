package com.tein.karardefteri.summary.controller;

import com.tein.karardefteri.summary.dto.ProjectSummaryResponseDto;
import com.tein.karardefteri.summary.dto.UserSummaryResponseDto;
import com.tein.karardefteri.summary.service.SummaryService;
import com.tein.karardefteri.user.dto.UserResponseDTO;
import com.tein.karardefteri.user.entity.UserRole;
import com.tein.karardefteri.user.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class SummaryController {

    private final SummaryService summaryService;
    private final UserService userService;

    public SummaryController(
            SummaryService summaryService,
            UserService userService
    ) {
        this.summaryService = summaryService;
        this.userService = userService;
    }

    @GetMapping("/projects/{id}/summary")
    public ProjectSummaryResponseDto getProjectSummary(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        UserResponseDTO currentUser =
                userService.getCurrentUser(authentication.getName());

        boolean isAdmin = currentUser.role() == UserRole.ADMIN;

        return summaryService.getProjectSummary(
                id,
                currentUser.id(),
                isAdmin
        );
    }

    @GetMapping("/users/me/summary")
    public UserSummaryResponseDto getUserSummary(
            Authentication authentication
    ) {
        UserResponseDTO currentUser =
                userService.getCurrentUser(authentication.getName());

        return summaryService.getUserSummary(currentUser.id());
    }
}