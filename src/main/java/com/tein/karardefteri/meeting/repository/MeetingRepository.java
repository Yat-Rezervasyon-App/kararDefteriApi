package com.tein.karardefteri.meeting.repository;

import com.tein.karardefteri.meeting.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface MeetingRepository extends JpaRepository<Meeting, UUID>,
        JpaSpecificationExecutor<Meeting>{

    boolean existsByIdAndCreatedBy(UUID id,UUID createdBy);
}