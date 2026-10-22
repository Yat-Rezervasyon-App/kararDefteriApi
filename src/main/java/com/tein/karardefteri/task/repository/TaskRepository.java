
package com.tein.karardefteri.task.repository;

import com.tein.karardefteri.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByMeetingId(UUID meetingId);
}
