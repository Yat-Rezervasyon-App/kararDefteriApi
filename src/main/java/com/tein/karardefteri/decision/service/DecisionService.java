
package com.tein.karardefteri.decision.service;

import com.tein.karardefteri.decision.dto.DecisionCreateRequest;
import com.tein.karardefteri.decision.dto.DecisionStatusRequest;
import com.tein.karardefteri.decision.entity.Decision;
import com.tein.karardefteri.decision.entity.DecisionStatus;
import com.tein.karardefteri.decision.repository.DecisionRepository;
import com.tein.karardefteri.meeting.entity.Meeting;
import com.tein.karardefteri.meeting.repository.MeetingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class DecisionService {

    private final DecisionRepository decisionRepository;
    private final MeetingRepository meetingRepository;
    private final JdbcTemplate jdbcTemplate;

    public DecisionService(
            DecisionRepository decisionRepository,
            MeetingRepository meetingRepository,
            JdbcTemplate jdbcTemplate) {
        this.decisionRepository = decisionRepository;
        this.meetingRepository = meetingRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public Page<Decision> getDecisionsByMeetingId(
            UUID meetingId,
            DecisionStatus status,
            int page,
            int size) {

        getMeeting(meetingId);

        if (page < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Sayfa numarasi 0 veya daha buyuk olmalidir.");
        }

        if (size < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Sayfa boyutu en az 1 olmalidir.");
        }

        Pageable pageable = PageRequest.of(page, size);

        if (status == null) {
            return decisionRepository.findByMeetingId(meetingId, pageable);
        }

        return decisionRepository.findByMeetingIdAndStatus(
                meetingId,
                status,
                pageable);
    }

    public long countActiveDecisionsByProjectId(UUID projectId) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) " +
                        "FROM decisions d " +
                        "INNER JOIN meetings m ON d.meeting_id = m.id " +
                        "WHERE m.project_id = ? " +
                        "AND d.status = 'ACTIVE'",
                Long.class,
                projectId
        );

        return count != null ? count : 0L;
    }

    @Transactional
    public Decision createDecision(
            UUID meetingId,
            DecisionCreateRequest request,
            UUID currentUserId,
            boolean isAdmin) {

        Meeting meeting = getMeeting(meetingId);

        checkPermission(meeting, currentUserId, isAdmin);
        checkNotArchived(meeting);
        checkProjectNotArchived(meeting);

        Decision decision = new Decision();
        decision.setMeetingId(meetingId);
        decision.setContent(request.getContent());
        decision.setStatus(DecisionStatus.ACTIVE);

        return decisionRepository.save(decision);
    }

    @Transactional
    public Decision updateDecision(
            UUID id,
            DecisionCreateRequest request,
            UUID currentUserId,
            boolean isAdmin) {

        Decision decision = getDecision(id);
        Meeting meeting = getMeeting(decision.getMeetingId());

        checkPermission(meeting, currentUserId, isAdmin);
        checkNotArchived(meeting);
        checkProjectNotArchived(meeting);

        decision.setContent(request.getContent());

        return decisionRepository.save(decision);
    }

    @Transactional
    public void deleteDecision(
            UUID id,
            UUID currentUserId,
            boolean isAdmin) {

        Decision decision = getDecision(id);
        Meeting meeting = getMeeting(decision.getMeetingId());

        checkPermission(meeting, currentUserId, isAdmin);
        checkNotArchived(meeting);
        checkProjectNotArchived(meeting);

        decisionRepository.delete(decision);
    }

    @Transactional
    public Decision updateDecisionStatus(
            UUID id,
            DecisionStatusRequest request,
            UUID currentUserId,
            boolean isAdmin) {

        Decision decision = getDecision(id);
        Meeting meeting = getMeeting(decision.getMeetingId());

        checkPermission(meeting, currentUserId, isAdmin);
        checkNotArchived(meeting);
        checkProjectNotArchived(meeting);

        decision.setStatus(request.getStatus());

        return decisionRepository.save(decision);
    }

    private Decision getDecision(UUID id) {
        return decisionRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Karar bulunamadi: " + id));
    }

    private Meeting getMeeting(UUID id) {
        return meetingRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Toplanti bulunamadi: " + id));
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
                    "Bu karar uzerinde islem yapma yetkiniz yok.");
        }
    }

    private void checkNotArchived(Meeting meeting) {
        if (meeting.isArchived()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Arsivlenmis toplanti degistirilemez.");
        }
    }

    private void checkProjectNotArchived(Meeting meeting) {
        Boolean archived = jdbcTemplate.queryForObject(
                "SELECT archived FROM projects WHERE id = ?",
                Boolean.class,
                meeting.getProjectId());

        if (Boolean.TRUE.equals(archived)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Arsivlenmis projenin kararlari degistirilemez.");
        }
    }
}
