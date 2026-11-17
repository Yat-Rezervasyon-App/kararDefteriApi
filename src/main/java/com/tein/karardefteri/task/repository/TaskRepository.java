package com.tein.karardefteri.task.repository;

import com.tein.karardefteri.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, Long>,
        JpaSpecificationExecutor<Task> {

    List<Task> findByAssigneeId(UUID assigneeId);

    List<Task> findByMeetingId(UUID meetingId);

    @Query(value = """
            SELECT COUNT(*)
            FROM tasks t
            JOIN meetings m ON m.id = t.meeting_id
            WHERE m.project_id = :projectId
              AND t.status <> 'DONE'
            """, nativeQuery = true)
    long countOpenTasksByProjectId(
            @Param("projectId") UUID projectId);

    @Query(value = """
            SELECT COUNT(*)
            FROM tasks t
            JOIN meetings m ON m.id = t.meeting_id
            WHERE m.project_id = :projectId
              AND t.status <> 'DONE'
              AND t.due_date IS NOT NULL
              AND t.due_date < :now
            """, nativeQuery = true)
    long countOverdueTasksByProjectId(
            @Param("projectId") UUID projectId,
            @Param("now") OffsetDateTime now);

    @Query(value = """
            SELECT COUNT(*)
            FROM tasks t
            WHERE t.assignee_id = :assigneeId
              AND t.status <> 'DONE'
            """, nativeQuery = true)
    long countOpenTasksByAssigneeId(
            @Param("assigneeId") UUID assigneeId);

    @Query(value = """
            SELECT COUNT(*)
            FROM tasks t
            WHERE t.assignee_id = :assigneeId
              AND t.status <> 'DONE'
              AND t.due_date IS NOT NULL
              AND t.due_date < :now
            """, nativeQuery = true)
    long countOverdueTasksByAssigneeId(
            @Param("assigneeId") UUID assigneeId,
            @Param("now") OffsetDateTime now);
}