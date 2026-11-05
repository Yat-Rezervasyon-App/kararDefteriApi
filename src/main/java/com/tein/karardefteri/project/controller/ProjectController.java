
package com.tein.karardefteri.project.controller;

import com.tein.karardefteri.project.dto.ProjectRequestDto;
import com.tein.karardefteri.project.dto.ProjectResponseDto;
import com.tein.karardefteri.project.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.tein.karardefteri.user.dto.UserResponseDTO;
import com.tein.karardefteri.user.entity.UserRole;
import com.tein.karardefteri.user.service.UserService;
import org.springframework.security.core.Authentication;

import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final UserService userService;

    public ProjectController(
            ProjectService projectService,
           UserService userService) {
        this.projectService = projectService;
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponseDto create(
            @Valid @RequestBody ProjectRequestDto request) {

        UserResponseDTO currentUser = getCurrentUser();

        return projectService.create(
                request,
                currentUser.id()
        );
    }

    @GetMapping
    public List<ProjectResponseDto> findAll() {
        return projectService.findAll();
    }

    @GetMapping("/{id}")
    public ProjectResponseDto findById(@PathVariable UUID id){
        return projectService.findById(id);
    }

    @PutMapping("/{id}")
    public ProjectResponseDto update(
            @PathVariable UUID id,
            @Valid @RequestBody ProjectRequestDto request
    ) {
        UserResponseDTO currentUser = getCurrentUser();

        boolean isAdmin = currentUser.role() == UserRole.ADMIN;

        return projectService.update(
                id,
                request,
                currentUser.id(),
                isAdmin
        );
    }

    @PatchMapping("/{id}/archive")
    public ProjectResponseDto archive(
            @PathVariable UUID id
    ) {
        UserResponseDTO currentUser = getCurrentUser();

        boolean isAdmin = currentUser.role() == UserRole.ADMIN;

        return projectService.archive(
                id,
                currentUser.id(),
                isAdmin
        );
    }

    private UserResponseDTO getCurrentUser() {
        Authentication authentication =
                org.springframework.security.core.context.SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        return userService.getCurrentUser(authentication.getName());
    }
}
