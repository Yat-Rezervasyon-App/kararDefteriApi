
package com.tein.karardefteri.project.controller;

import com.tein.karardefteri.project.dto.ProjectRequestDto;
import com.tein.karardefteri.project.dto.ProjectResponseDto;
import com.tein.karardefteri.project.service.ProjectService;
import com.tein.karardefteri.common.dto.PageResponse;
import com.tein.karardefteri.common.exception.InvalidRequestException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.tein.karardefteri.user.dto.UserResponseDTO;
import com.tein.karardefteri.user.entity.UserRole;
import com.tein.karardefteri.user.service.UserService;
import org.springframework.security.core.Authentication;

import java.util.UUID;

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
    public PageResponse<ProjectResponseDto> findAll(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "false") boolean archived,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        if(page < 0) {
            throw new InvalidRequestException(
                    "INVALID_PAGE",
                    "Sayfa numarası 0 veya daha büyük olmalıdır"
            );
        }
        if ( size < 1 || size > 100){
            throw new InvalidRequestException(
                        "INVALID_SIZE",
                        "Sayfa boyutu 1 ile 100 arasında olmalıdır"
            );
        }
        return projectService.findAll(name, archived, page, size);
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
