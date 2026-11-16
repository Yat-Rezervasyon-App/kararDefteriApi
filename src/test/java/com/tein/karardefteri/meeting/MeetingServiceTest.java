
package com.tein.karardefteri.meeting;

import com.tein.karardefteri.meeting.entity.Meeting;
import com.tein.karardefteri.meeting.repository.MeetingRepository;
import com.tein.karardefteri.meeting.service.MeetingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeetingServiceTest {

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private MeetingService meetingService;

    private UUID meetingId;
    private UUID ownerId;
    private UUID anotherUserId;

    private Meeting meeting;

    @BeforeEach
    void setUp() {
        meetingId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        anotherUserId = UUID.randomUUID();

        meeting = new Meeting();
        meeting.setCreatedBy(ownerId);
        meeting.setArchived(false);

        when(meetingRepository.findById(meetingId))
                .thenReturn(Optional.of(meeting));
    }

    @Test
    void ownerCanUpdateMeeting() {
        when(meetingRepository.save(any(Meeting.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Meeting result = meetingService.updateMeeting(
                meetingId, "Yeni başlık", "Yeni açıklama", ownerId, false);

        assertEquals("Yeni başlık", result.getTitle());
        verify(meetingRepository).save(meeting);
    }

    @Test
    void anotherUserCannotUpdateMeeting() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> meetingService.updateMeeting(
                        meetingId, "Yeni başlık", "Açıklama", anotherUserId, false));

        assertEquals(403, exception.getStatusCode().value());
        verify(meetingRepository, never()).save(any(Meeting.class));
    }

    @Test
    void adminCanUpdateMeeting() {
        when(meetingRepository.save(any(Meeting.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Meeting result = meetingService.updateMeeting(
                meetingId, "Admin başlığı", "Açıklama", anotherUserId, true);

        assertEquals("Admin başlığı", result.getTitle());
        verify(meetingRepository).save(meeting);
    }

    @Test
    void missingMeetingReturns404() {
        when(meetingRepository.findById(meetingId))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> meetingService.updateMeeting(
                        meetingId, "Başlık", "Açıklama", ownerId, false));

        assertEquals(404, exception.getStatusCode().value());
    }

    @Test
    void archivedMeetingCannotBeUpdated() {
        meeting.setArchived(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> meetingService.updateMeeting(
                        meetingId, "Başlık", "Açıklama", ownerId, false));

        assertEquals(409, exception.getStatusCode().value());
        verify(meetingRepository, never()).save(any(Meeting.class));
    }

    @Test
    void ownerCanArchiveMeeting() {
        when(meetingRepository.save(any(Meeting.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Meeting result = meetingService.archiveMeeting(
                meetingId, ownerId, false);

        assertTrue(result.isArchived());
        verify(meetingRepository).save(meeting);
    }

    @Test
    void anotherUserCannotArchiveMeeting() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> meetingService.archiveMeeting(
                        meetingId, anotherUserId, false));

        assertEquals(403, exception.getStatusCode().value());
        verify(meetingRepository, never()).save(any(Meeting.class));
    }
}
