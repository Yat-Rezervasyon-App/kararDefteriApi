package com.tein.karardefteri.meeting.dto;

import com.tein.karardefteri.meeting.entity.Meeting;

public class MeetingNotesUpdateRequest {

    private String notes;

    public MeetingNotesUpdateRequest(){
    }

    public String getNotes(){
        return notes;
    }
    public void setNotes(String notes){
        this.notes=notes;
    }
}