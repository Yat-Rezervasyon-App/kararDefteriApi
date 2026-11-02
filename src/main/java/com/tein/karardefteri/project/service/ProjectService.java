package com.tein.karardefteri.project.service;

import com.tein.karardefteri.project.dto.ProjectRequestDto;
import com.tein.karardefteri.project.dto.ProjectResponseDto;
import com.tein.karardefteri.project.entity.Project;
import com.tein.karardefteri.project.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository){
        this.projectRepository = projectRepository;
    }

    @Transactional
    public ProjectResponseDto create(ProjectRequestDto request, UUID createdBy)
    {
        Project project =new Project();

        project.setName(request.name());
        project.setDescription(request.description());
        project.setCreatedBy(createdBy);

        Project savedProject = projectRepository.save(project);

        return toResponse(savedProject);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponseDto> findAll(){
        return projectRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }
    private ProjectResponseDto toResponse(Project project){
        return new ProjectResponseDto(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getCreatedBy(),
                project.isArchived(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
