package com.tein.karardefteri.meeting;

import com.tein.karardefteri.meeting.repository.MeetingRepository;
import com.tein.karardefteri.meeting.service.MeetingService;
import com.tein.karardefteri.project.repository.ProjectRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class MeetingFilterServiceTest{

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private MeetingService meetingService;

    @Test
    void fromDateAfterToDateReturns400(){

        OffsetDateTime from = OffsetDateTime.parse("2026-11-10T10:00:00+03:00");
        OffsetDateTime to = OffsetDateTime.parse("2026-11-09T10:00:00+03:00");

        assertThrows(
                ResponseStatusException.class,
                ()-> meetingService.getMeetings(
                        null,null,from,to,false,0,20
                )
        );
    }
    @Test
    void equalFromAndToDatesReturns400(){

        OffsetDateTime date =OffsetDateTime.parse("2026-11-10T10:00:00+03:00");

        assertThrows(
          ResponseStatusException.class,
                ()->meetingService.getMeetings(
                        null,null,date,date,false,0,20
                        )
        );
    }
    @Test
    void invalPageReturns400(){

        assertThrows(
                ResponseStatusException.class,
                ()-> meetingService.getMeetings(
                        null,null,null,null,false,-1,20
                )
        );
    }
    @Test
    void invalidSizeReturns400(){

        assertThrows(
                ResponseStatusException.class,
                ()-> meetingService.getMeetings(
                        null,null,null,null,false,0,0
                )
        );
    }
}