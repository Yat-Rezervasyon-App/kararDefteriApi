
package com.tein.karardefteri.meeting.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

public class MeetingCreateRequest {

    @NotNull(message = "Proje ID zorunludur.")
    private UUID projectId;

    @NotBlank(message = "Toplantı başlığı boş olamaz.")
    @Size(max = 200, message = "Başlık en fazla 200 karakter olabilir.")
    private String title;

    @NotNull(message = "Toplantı tarih ve saati zorunludur.")
    private OffsetDateTime meetingAt;

    @Size(max = 2000, message = "Açıklama en fazla 2000 karakter olabilir.")
    private String description;

    @Size(max = 5000, message = "Notlar en fazla 5000 karakter olabilir.")
    private String notes;

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public OffsetDateTime getMeetingAt() {
        return meetingAt;
    }

    public void setMeetingAt(OffsetDateTime meetingAt) {
        this.meetingAt = meetingAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
