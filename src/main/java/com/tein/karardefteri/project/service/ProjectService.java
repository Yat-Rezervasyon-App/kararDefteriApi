package com.tein.karardefteri.project.service;

import com.tein.karardefteri.common.dto.PageResponse;
import com.tein.karardefteri.project.dto.ProjectRequestDto;
import com.tein.karardefteri.project.dto.ProjectResponseDto;
import com.tein.karardefteri.project.entity.Project;
import com.tein.karardefteri.project.repository.ProjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Locale;
import jakarta.persistence.criteria.Predicate;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Transactional
    public ProjectResponseDto create(ProjectRequestDto request, UUID createdBy) {
        Project project = new Project();

        project.setName(request.name());
        project.setDescription(request.description());
        project.setCreatedBy(createdBy);

        Project savedProject = projectRepository.save(project);

        return toResponse(savedProject);
    }


    @Transactional(readOnly = true)
    public PageResponse<ProjectResponseDto> findAll(
            String name,
            boolean archived,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Order.desc("createdAt"),
                        Sort.Order.desc("id")
                )
        );

        Specification<Project> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(
                    criteriaBuilder.equal(root.get("archived"), archived)
            );

            if (name != null && !name.isBlank()) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("name")),
                                "%" + name.trim().toLowerCase(Locale.ROOT) + "%"
                        )
                );
            }

            return criteriaBuilder.and(
                    predicates.toArray(new Predicate[0])
            );
        };

        Page<ProjectResponseDto> result = projectRepository
                .findAll(specification, pageable)
                .map(this::toResponse);

        return new PageResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }


    // Proje detayını getirir. Arşivlenmiş projeler de okunabilir.
    @Transactional(readOnly = true)
    public ProjectResponseDto findById(UUID projectId) {
        return toResponse(getProject(projectId));
    }

    // Projenin arşiv durumunu kontrol eder.
    @Transactional(readOnly = true)
    public boolean isArchived(UUID projectId) {
        return projectRepository.existsByIdAndArchivedTrue(projectId);
    }

    // Kullanıcının proje sahibi olup olmadığını kontrol eder.
    @Transactional(readOnly = true)
    public boolean isOwner(UUID projectId, UUID userId) {
        return projectRepository.existsByIdAndCreatedBy(projectId, userId);
    }

    @Transactional
    public ProjectResponseDto update(
            UUID projectId,
            ProjectRequestDto request,
            UUID userId,
            boolean isAdmin
    ) {
        Project project = getProject(projectId);

        if (project.isArchived()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Arşivlenmiş proje güncellenemez"
            );
        }

        if (!isAdmin && !project.getCreatedBy().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Bu projeyi güncelleme yetkiniz yok"
            );
        }

        project.setName(request.name());
        project.setDescription(request.description());

        return toResponse(projectRepository.save(project));
    }

    @Transactional
    public ProjectResponseDto archive(
            UUID projectId,
            UUID userId,
            boolean isAdmin
    ) {
        Project project = getProject(projectId);

        if (!isAdmin && !project.getCreatedBy().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Bu projeyi arşivleme yetkiniz yok"
            );
        }

        if (project.isArchived()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Proje zaten arşivlenmiş"
            );
        }

        project.setArchived(true);

        return toResponse(projectRepository.save(project));
    }

    private Project getProject(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Proje bulunamadı"
                ));
    }

    private ProjectResponseDto toResponse(Project project) {
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
