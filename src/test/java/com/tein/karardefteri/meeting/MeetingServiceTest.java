
package com.tein.karardefteri.meeting;

import com.tein.karardefteri.meeting.dto.MeetingCreateRequest;
import com.tein.karardefteri.meeting.entity.Meeting;
import com.tein.karardefteri.meeting.repository.MeetingRepository;
import com.tein.karardefteri.meeting.service.MeetingService;
import com.tein.karardefteri.project.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeetingServiceTest {

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private MeetingService meetingService;

    private UUID meetingId;
    private UUID ownerId;
    private UUID anotherUserId;
    private UUID projectId;
    private Meeting meeting;

    @BeforeEach
    void setUp() throws Exception {
        meetingId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        anotherUserId = UUID.randomUUID();
        projectId = UUID.randomUUID();

        meeting = new Meeting();

        Field idField = Meeting.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(meeting, meetingId);

        meeting.setProjectId(projectId);
        meeting.setCreatedBy(ownerId);
        meeting.setTitle("Test Toplantısı");
        meeting.setArchived(false);

        lenient().when(meetingRepository.findById(meetingId))
                .thenReturn(Optional.of(meeting));
    }

    @Test
    void ownerCanUpdateMeeting() {
        when(meetingRepository.save(any(Meeting.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Meeting result = meetingService.updateMeeting(
                meetingId, "Yeni Başlık", "Açıklama", ownerId, false);

        assertEquals("Yeni Başlık", result.getTitle());
        verify(meetingRepository).save(meeting);
    }

    @Test
    void anotherUserCannotUpdateMeeting() {
        assertThrows(ResponseStatusException.class, () ->
                meetingService.updateMeeting(
                        meetingId, "Yeni Başlık", "Açıklama",
                        anotherUserId, false));

        verify(meetingRepository, never()).save(any());
    }

    @Test
    void adminCanUpdateMeeting() {
        when(meetingRepository.save(any(Meeting.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Meeting result = meetingService.updateMeeting(
                meetingId, "Admin Başlığı", "Açıklama", anotherUserId, true);

        assertEquals("Admin Başlığı", result.getTitle());
        verify(meetingRepository).save(meeting);
    }

    @Test
    void missingMeetingReturns404() {
        when(meetingRepository.findById(meetingId))
                .thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () ->
                meetingService.updateMeeting(
                        meetingId, "Başlık", "Açıklama", ownerId, false));
    }

    @Test
    void archivedMeetingCannotBeUpdated() {
        meeting.setArchived(true);

        assertThrows(ResponseStatusException.class, () ->
                meetingService.updateMeeting(
                        meetingId, "Yeni Başlık", "Açıklama", ownerId, false));
    }

    @Test
    void ownerCanArchiveMeeting() {
        when(meetingRepository.save(any(Meeting.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        meetingService.archiveMeeting(meetingId, ownerId, false);

        assertTrue(meeting.isArchived());
        verify(meetingRepository).save(meeting);
    }

    @Test
    void anotherUserCannotArchiveMeeting() {
        assertThrows(ResponseStatusException.class, () ->
                meetingService.archiveMeeting(meetingId, anotherUserId, false));

        verify(meetingRepository, never()).save(any());
    }

    @Test
    void adminCanArchiveMeeting() {
        when(meetingRepository.save(any(Meeting.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        meetingService.archiveMeeting(meetingId, anotherUserId, true);

        assertTrue(meeting.isArchived());
        verify(meetingRepository).save(meeting);
    }

    @Test
    void alreadyArchivedMeetingCannotBeArchivedAgain() {
        meeting.setArchived(true);

        assertThrows(ResponseStatusException.class, () ->
                meetingService.archiveMeeting(meetingId, ownerId, false));
    }

    @Test
    void createMeetingWithValidProject() {
        MeetingCreateRequest request = new MeetingCreateRequest();
        request.setProjectId(projectId);
        request.setTitle("Yeni Toplantı");

        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any()))
                .thenReturn(1);

        when(meetingRepository.save(any(Meeting.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Meeting result = meetingService.createMeeting(request, ownerId);

        assertEquals("Yeni Toplantı", result.getTitle());
        assertEquals(projectId, result.getProjectId());
        assertEquals(ownerId, result.getCreatedBy());
        verify(meetingRepository).save(any(Meeting.class));
    }

    @Test
    void createMeetingWithMissingProjectReturns404() {
        MeetingCreateRequest request = new MeetingCreateRequest();
        request.setProjectId(projectId);
        request.setTitle("Yeni Toplantı");

        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any()))
                .thenReturn(0);

        assertThrows(ResponseStatusException.class, () ->
                meetingService.createMeeting(request, ownerId));

        verify(meetingRepository, never()).save(any());
    }

    @Test
    void createMeetingWithBlankTitleReturns400() {
        MeetingCreateRequest request = new MeetingCreateRequest();
        request.setProjectId(projectId);
        request.setTitle(" ");

        assertThrows(ResponseStatusException.class, () ->
                meetingService.createMeeting(request, ownerId));
    }

    @Test
    void createMeetingWithTooLongTitleReturns400() {
        MeetingCreateRequest request = new MeetingCreateRequest();
        request.setProjectId(projectId);
        request.setTitle("A".repeat(201));

        assertThrows(ResponseStatusException.class, () ->
                meetingService.createMeeting(request, ownerId));
    }

    @Test
    void updateMeetingWithBlankTitleReturns400() {
        assertThrows(ResponseStatusException.class, () ->
                meetingService.updateMeeting(
                        meetingId, " ", "Açıklama", ownerId, false));
    }

    @Test
    void updateMeetingWithTooLongTitleReturns400() {
        assertThrows(ResponseStatusException.class, () ->
                meetingService.updateMeeting(
                        meetingId, "A".repeat(201), "Açıklama", ownerId, false));
    }

    @Test
    void ownerCanUpdateMeetingNotes() {
        when(jdbcTemplate.queryForObject(
                contains("SELECT archived"),
                eq(Boolean.class),
                eq(projectId)
        )).thenReturn(false);

        when(meetingRepository.save(any(Meeting.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Meeting result = meetingService.updateMeetingNotes(
                meetingId, "Yeni toplantı notları", ownerId, false);

        assertEquals("Yeni toplantı notları", result.getNotes());
        verify(meetingRepository).save(meeting);
    }

    @Test
    void ownerCanClearMeetingNotes() {
        meeting.setNotes("Eski notlar");

        when(jdbcTemplate.queryForObject(
                contains("SELECT archived"),
                eq(Boolean.class),
                eq(projectId)
        )).thenReturn(false);

        when(meetingRepository.save(any(Meeting.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Meeting result = meetingService.updateMeetingNotes(
                meetingId, null, ownerId, false);

        assertNull(result.getNotes());
        verify(meetingRepository).save(meeting);
    }

    @Test
    void anotherUserCannotUpdateMeetingNotes() {
        assertThrows(ResponseStatusException.class, () ->
                meetingService.updateMeetingNotes(
                        meetingId, "Yeni notlar", anotherUserId, false));

        verify(meetingRepository, never()).save(any());
    }

    @Test
    void adminCanUpdateMeetingNotes() {
        when(jdbcTemplate.queryForObject(
                contains("SELECT archived"),
                eq(Boolean.class),
                eq(projectId)
        )).thenReturn(false);

        when(meetingRepository.save(any(Meeting.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Meeting result = meetingService.updateMeetingNotes(
                meetingId, "Admin notları", anotherUserId, true);

        assertEquals("Admin notları", result.getNotes());
    }

    @Test
    void archivedMeetingCannotUpdateNotes() {
        meeting.setArchived(true);

        assertThrows(ResponseStatusException.class, () ->
                meetingService.updateMeetingNotes(
                        meetingId, "Yeni notlar", ownerId, false));

        verify(meetingRepository, never()).save(any());
    }

    @Test
    void archivedProjectCannotUpdateMeetingNotes() {
        when(jdbcTemplate.queryForObject(
                contains("SELECT archived"),
                eq(Boolean.class),
                eq(projectId)
        )).thenReturn(true);

        assertThrows(ResponseStatusException.class, () ->
                meetingService.updateMeetingNotes(
                        meetingId, "Yeni notlar", ownerId, false));

        verify(meetingRepository, never()).save(any());
    }

    @Test
    void notesLongerThan20000CharactersReturns400() {
        when(jdbcTemplate.queryForObject(
                contains("SELECT archived"),
                eq(Boolean.class),
                eq(projectId)
        )).thenReturn(false);

        assertThrows(ResponseStatusException.class, () ->
                meetingService.updateMeetingNotes(
                        meetingId, "A".repeat(20001), ownerId, false));

        verify(meetingRepository, never()).save(any());
    }

    @Test
    void notesWith20000CharactersCanBeSaved() {
        String notes = "A".repeat(20000);

        when(jdbcTemplate.queryForObject(
                contains("SELECT archived"),
                eq(Boolean.class),
                eq(projectId)
        )).thenReturn(false);

        when(meetingRepository.save(any(Meeting.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Meeting result = meetingService.updateMeetingNotes(
                meetingId, notes, ownerId, false);

        assertEquals(20000, result.getNotes().length());
    }


}
