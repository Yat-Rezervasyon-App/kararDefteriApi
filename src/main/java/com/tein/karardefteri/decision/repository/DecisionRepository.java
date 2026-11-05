package com.tein.karardefteri.decision.repository;

import com.tein.karardefteri.decision.entity.Decision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DecisionRepository extends JpaRepository<Decision, UUID> {

    List<Decision> findByMeetingId(UUID meetingId);
}