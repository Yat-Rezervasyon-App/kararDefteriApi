
package com.tein.karardefteri.meeting;

import com.tein.karardefteri.meeting.entity.Meeting;
import com.tein.karardefteri.meeting.repository.MeetingRepository;
import com.tein.karardefteri.meeting.service.MeetingService;
import com.tein.karardefteri.project.repository.ProjectRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeetingFilterServiceTest {

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private MeetingService meetingService;

    @Test
    void fromDateAfterToDateReturns400() {
        OffsetDateTime from =
                OffsetDateTime.parse("2026-11-10T10:00:00+03:00");
        OffsetDateTime to =
                OffsetDateTime.parse("2026-11-09T10:00:00+03:00");

        assertThrows(ResponseStatusException.class, () ->
                meetingService.getMeetings(
                        null, null, from, to, false, 0, 20));
    }

    @Test
    void equalFromAndToDatesReturns400() {
        OffsetDateTime date =
                OffsetDateTime.parse("2026-11-10T10:00:00+03:00");

        assertThrows(ResponseStatusException.class, () ->
                meetingService.getMeetings(
                        null, null, date, date, false, 0, 20));
    }

    @Test
    void invalidPageReturns400() {
        assertThrows(ResponseStatusException.class, () ->
                meetingService.getMeetings(
                        null, null, null, null, false, -1, 20));
    }

    @Test
    void invalidSizeReturns400() {
        assertThrows(ResponseStatusException.class, () ->
                meetingService.getMeetings(
                        null, null, null, null, false, 0, 0));
    }

    @Test
    void missingProjectReturns404() {
        UUID projectId = UUID.randomUUID();

        when(jdbcTemplate.queryForObject(
                anyString(), eq(Integer.class), eq(projectId)))
                .thenReturn(0);

        assertThrows(ResponseStatusException.class, () ->
                meetingService.getMeetings(
                        projectId, null, null, null, false, 0, 20));

        verify(meetingRepository, never())
                .findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void existingProjectCanBeFiltered() {
        UUID projectId = UUID.randomUUID();

        when(jdbcTemplate.queryForObject(
                anyString(), eq(Integer.class), eq(projectId)))
                .thenReturn(1);

        Page<Meeting> expectedPage = new PageImpl<>(List.of());

        when(meetingRepository.findAll(
                any(Specification.class), any(Pageable.class)))
                .thenReturn(expectedPage);

        Page<Meeting> result = meetingService.getMeetings(
                projectId,
                "Toplantı",
                null,
                null,
                false,
                0,
                20
        );

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());

        verify(meetingRepository).findAll(
                any(Specification.class), any(Pageable.class));
    }

    @Test
    void meetingsCanBeFilteredWithoutProjectId() {
        Page<Meeting> expectedPage = new PageImpl<>(List.of());

        when(meetingRepository.findAll(
                any(Specification.class), any(Pageable.class)))
                .thenReturn(expectedPage);

        Page<Meeting> result = meetingService.getMeetings(
                null, null, null, null, false, 0, 20);

        assertNotNull(result);
        verify(meetingRepository).findAll(
                any(Specification.class), any(Pageable.class));
    }
}
