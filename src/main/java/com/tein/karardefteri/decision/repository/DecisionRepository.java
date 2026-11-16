
package com.tein.karardefteri.decision.repository;

import com.tein.karardefteri.decision.entity.Decision;
import com.tein.karardefteri.decision.entity.DecisionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface DecisionRepository extends JpaRepository<Decision, UUID> {

    Page<Decision> findByMeetingId(UUID meetingId, Pageable pageable);

    Page<Decision> findByMeetingIdAndStatus(
            UUID meetingId,
            DecisionStatus status,
            Pageable pageable);

    @Query(value = """
            SELECT COUNT(d.id)
            FROM decisions d
            INNER JOIN meetings m ON m.id = d.meeting_id
            WHERE m.project_id = :projectId
            AND d.status = 'ACTIVE'
            """, nativeQuery = true)
    long countActiveDecisionsByProjectId(@Param("projectId") UUID projectId);
}
