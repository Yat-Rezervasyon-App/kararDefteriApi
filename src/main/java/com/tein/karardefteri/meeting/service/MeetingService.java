
package com.tein.karardefteri.meeting.service;

import com.tein.karardefteri.meeting.dto.MeetingCreateRequest;
import com.tein.karardefteri.meeting.dto.MeetingDetailResponseDto;
import com.tein.karardefteri.meeting.entity.Meeting;
import com.tein.karardefteri.meeting.repository.MeetingRepository;
import com.tein.karardefteri.meeting.specification.MeetingSpecification;
import com.tein.karardefteri.project.dto.ProjectResponseDto;
import com.tein.karardefteri.project.entity.Project;
import com.tein.karardefteri.project.repository.ProjectRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final JdbcTemplate jdbcTemplate;
    private final ProjectRepository projectRepository;

    public MeetingService(
            MeetingRepository meetingRepository,
            JdbcTemplate jdbcTemplate,
            ProjectRepository projectRepository) {
        this.meetingRepository = meetingRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.projectRepository = projectRepository;
    }
    public long countMeetingsByProjectId(UUID projectId) {
        return meetingRepository.countByProjectId(projectId);
    }

    public List<Meeting> getAllMeetings() {
        return meetingRepository.findAll();
    }

    // Projeye ait toplantilarin sayisini getirir.
    public long countMeetingsByProjectId(UUID projectId) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM meetings WHERE project_id = ?",
                Long.class,
                projectId
        );

        return count != null ? count : 0L;
    }

    @Transactional(readOnly = true)
    public Page<Meeting> getMeetings(
            UUID projectId,
            String title,
            OffsetDateTime from,
            OffsetDateTime to,
            Boolean archived,
            int page,
            int size) {

        if (projectId != null) {
            Integer projectExists = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM projects WHERE id = ?",
                    Integer.class,
                    projectId
            );

            if (projectExists == null || projectExists == 0) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Belirtilen proje bulunamadi."
                );
            }
        }

        if (from != null && to != null && !from.isBefore(to)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Baslangic tarihi bitis tarihinden once olmalidir."
            );
        }

        if (page < 0 || size < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Sayfa numarasi 0 veya daha buyuk, sayfa boyutu 1 veya daha buyuk olmalidir."
            );
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Order.desc("meetingAt"),
                        Sort.Order.desc("id")
                )
        );

        return meetingRepository.findAll(
                MeetingSpecification.filter(
                        projectId,
                        title,
                        from,
                        to,
                        archived
                ),
                pageable
        );
    }

    public Meeting getMeetingById(UUID id) {
        return meetingRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Toplanti bulunamadi: " + id));
    }

    @Transactional(readOnly = true)
    public MeetingDetailResponseDto getMeetingDetailById(UUID id) {

        Meeting meeting = getMeetingById(id);

        Project project = projectRepository.findById(meeting.getProjectId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Toplantinin bagli oldugu proje bulunamadi."
                        )
                );

        ProjectResponseDto projectDto = new ProjectResponseDto(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getCreatedBy(),
                project.isArchived(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );

        return new MeetingDetailResponseDto(
                meeting.getId(),
                meeting.getProjectId(),
                meeting.getTitle(),
                meeting.getMeetingAt(),
                meeting.getDescription(),
                meeting.getNotes(),
                meeting.getCreatedBy(),
                meeting.isArchived(),
                meeting.getCreatedAt(),
                meeting.getUpdatedAt(),
                projectDto
        );
    }

    @Transactional
    public Meeting createMeeting(
            MeetingCreateRequest request,
            UUID createdBy) {

        if (request.getTitle() == null ||
                request.getTitle().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Toplanti basligi bos olamaz.");
        }

        if (request.getTitle().length() > 200) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Baslik en fazla 200 karakter olabilir.");
        }

        Integer projectExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM projects WHERE id = ?",
                Integer.class,
                request.getProjectId());

        if (projectExists == null || projectExists == 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Belirtilen proje bulunamadi.");
        }

        Meeting meeting = new Meeting();
        meeting.setProjectId(request.getProjectId());
        meeting.setTitle(request.getTitle());
        meeting.setMeetingAt(request.getMeetingAt());
        meeting.setDescription(request.getDescription());
        meeting.setNotes(request.getNotes());
        meeting.setCreatedBy(createdBy);
        meeting.setArchived(false);

        return meetingRepository.save(meeting);
    }

    @Transactional
    public Meeting updateMeeting(
            UUID id,
            String title,
            String description,
            UUID currentUserId,
            boolean isAdmin) {

        Meeting meeting = getMeetingById(id);
        checkPermission(meeting, currentUserId, isAdmin);
        checkNotArchived(meeting);

        if (title != null && title.length() > 200) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Baslik en fazla 200 karakter olabilir.");
        }

        if (title != null && title.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Toplanti basligi bos olamaz.");
        }

        if (title != null) {
            meeting.setTitle(title);
        }

        meeting.setDescription(description);

        return meetingRepository.save(meeting);
    }

    @Transactional
    public Meeting archiveMeeting(
            UUID id,
            UUID currentUserId,
            boolean isAdmin) {

        Meeting meeting = getMeetingById(id);
        checkPermission(meeting, currentUserId, isAdmin);
        checkNotArchived(meeting);

        meeting.setArchived(true);

        return meetingRepository.save(meeting);
    }

    @Transactional
    public Meeting updateMeetingNotes(
            UUID id,
            String notes,
            UUID currentUserId,
            boolean isAdmin) {

        Meeting meeting = getMeetingById(id);
        checkPermission(meeting, currentUserId, isAdmin);

        if (meeting.isArchived()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Arsivlenmis toplantinin notlari degistirilemez.");
        }

        Boolean projectArchived = jdbcTemplate.queryForObject(
                "SELECT archived FROM projects WHERE id = ?",
                Boolean.class,
                meeting.getProjectId());

        if (Boolean.TRUE.equals(projectArchived)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Arsivlenmis projenin toplantı notlari degistirilemez.");
        }

        if (notes != null && notes.length() > 20000) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Notlar en fazla 20000 karakter olabilir.");
        }

        meeting.setNotes(notes);

        return meetingRepository.save(meeting);
    }

    private void checkPermission(
            Meeting meeting,
            UUID currentUserId,
            boolean isAdmin) {

        if (isAdmin) {
            return;
        }

        if (meeting.getCreatedBy() == null ||
                !meeting.getCreatedBy().equals(currentUserId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Bu toplanti uzerinde islem yapma yetkiniz yok.");
        }
    }

    private void checkNotArchived(Meeting meeting) {
        if (meeting.isArchived()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Arsivlenmis toplanti degistirilemez.");
        }
    }
}
