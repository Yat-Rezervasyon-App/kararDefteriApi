
package com.tein.karardefteri.project.controller;

import com.tein.karardefteri.project.dto.ProjectRequestDto;
import com.tein.karardefteri.project.dto.ProjectResponseDto;
import com.tein.karardefteri.project.service.DevelopmentProjectCreator;
import com.tein.karardefteri.project.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@Profile("dev")
public class ProjectController {

    private final ProjectService projectService;
    private final DevelopmentProjectCreator developmentProjectCreator;

    public ProjectController(
            ProjectService projectService,
            DevelopmentProjectCreator developmentProjectCreator) {
        this.projectService = projectService;
        this.developmentProjectCreator = developmentProjectCreator;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponseDto create(
            @Valid @RequestBody ProjectRequestDto request) {
        return projectService.create(
                request,
                developmentProjectCreator.getUserId()
        );
    }

    @GetMapping
    public List<ProjectResponseDto> findAll() {
        return projectService.findAll();
    }
}
