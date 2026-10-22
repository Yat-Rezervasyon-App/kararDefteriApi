
package com.tein.karardefteri.meeting.service;

import com.tein.karardefteri.meeting.dto.MeetingCreateRequest;
import com.tein.karardefteri.meeting.entity.Meeting;
import com.tein.karardefteri.meeting.repository.MeetingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final JdbcTemplate jdbcTemplate;

    public MeetingService(
            MeetingRepository meetingRepository,
            JdbcTemplate jdbcTemplate) {
        this.meetingRepository = meetingRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Meeting> getAllMeetings() {
        return meetingRepository.findAll();
    }

    public Meeting getMeetingById(UUID id) {
        return meetingRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Toplantı bulunamadı: " + id));
    }

    @Transactional
    public Meeting createMeeting(
            MeetingCreateRequest request,
            UUID createdBy) {

        if (request.getTitle() == null ||
                request.getTitle().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Toplantı başlığı boş olamaz.");
        }

        if (request.getTitle().length() > 200) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Başlık en fazla 200 karakter olabilir.");
        }

        Integer projectExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM projects WHERE id = ?",
                Integer.class,
                request.getProjectId());

        if (projectExists == null || projectExists == 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Belirtilen proje bulunamadı.");
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

        if (title != null && title.length() > 200) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Başlık en fazla 200 karakter olabilir.");
        }

        if (title != null && !title.isBlank()) {
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

        meeting.setArchived(true);

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
                    "Bu toplantı üzerinde işlem yapma yetkiniz yok.");
        }
    }
}
