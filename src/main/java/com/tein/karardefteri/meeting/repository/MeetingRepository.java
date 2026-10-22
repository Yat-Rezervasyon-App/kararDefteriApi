package com.tein.karardefteri.meeting.repository;

import com.tein.karardefteri.meeting.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MeetingRepository extends JpaRepository<Meeting, UUID> {

    boolean existsByIdAndCreatedBy(UUID id, UUID createdBy);

}