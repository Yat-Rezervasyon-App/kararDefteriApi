package com.tein.karardefteri.decision.service;

import com.tein.karardefteri.decision.dto.DecisionCreateRequest;
import com.tein.karardefteri.decision.dto.DecisionStatusRequest;
import com.tein.karardefteri.decision.entity.Decision;
import com.tein.karardefteri.decision.entity.DecisionStatus;
import com.tein.karardefteri.decision.repository.DecisionRepository;
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

    // Toplantıya ait kararları listeler.
    public List<Decision> getDecisionsByMeetingId(UUID meetingId) {
        getMeeting(meetingId);
        return decisionRepository.findByMeetingId(meetingId);
    }

    // Yeni karar oluşturur.
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

    // Kararın içeriğini günceller.
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

    // Kararı siler.
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

    // Kararın durumunu günceller.
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

    // ID ile karar bulur.
    private Decision getDecision(UUID id) {
        return decisionRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Karar bulunamadı: " + id));
    }

    // ID ile toplantı bulur.
    private Meeting getMeeting(UUID id) {
        return meetingRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Toplantı bulunamadı: " + id));
    }

    // İşlemi yapan kişinin toplantı sahibi veya ADMIN olup olmadığını kontrol eder.
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
                    "Bu karar üzerinde işlem yapma yetkiniz yok.");
        }
    }

    // Toplantının arşivlenmiş olup olmadığını kontrol eder.
    private void checkNotArchived(Meeting meeting) {
        if (meeting.isArchived()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Arşivlenmiş toplantı değiştirilemez.");
        }
    }

    // Bağlı projenin arşivlenmiş olup olmadığını kontrol eder.
    private void checkProjectNotArchived(Meeting meeting) {
        Boolean archived = jdbcTemplate.queryForObject(
                "SELECT archived FROM projects WHERE id = ?",
                Boolean.class,
                meeting.getProjectId());

        if (Boolean.TRUE.equals(archived)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Arşivlenmiş projenin kararları değiştirilemez.");
        }
    }
}

