
package com.tein.karardefteri.meeting.dto;

public class MeetingUpdateRequest {

    private String title;
    private String description;

    public MeetingUpdateRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
