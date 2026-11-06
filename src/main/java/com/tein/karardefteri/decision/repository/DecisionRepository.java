package com.tein.karardefteri.decision.repository;

import com.tein.karardefteri.decision.entity.Decision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface DecisionRepository extends JpaRepository<Decision, UUID> {

    List<Decision> findByMeetingId(UUID meetingId);
    @Query(value = """
        SELECT COUNT(*)
        FROM decisions d
        JOIN meetings m ON m.id = d.meeting_id
        WHERE m.project_id = :projectId
          AND d.status = 'ACTIVE'
        """, nativeQuery = true)
    long countActiveDecisionsByProjectId(@Param("projectId") UUID projectId);



}