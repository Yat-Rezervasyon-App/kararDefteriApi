ALTER TABLE tasks
    ADD CONSTRAINT fk_tasks_meeting
        FOREIGN KEY (meeting_id) REFERENCES meetings(id);

ALTER TABLE tasks
    ADD CONSTRAINT fk_tasks_assignee
        FOREIGN KEY (assignee_id) REFERENCES users(id);