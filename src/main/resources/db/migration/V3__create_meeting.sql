CREATE TABLE meetings (
                          id UUID PRIMARY KEY,
                          project_id UUID NOT NULL,
                          title VARCHAR(200) NOT NULL,
                          meeting_at TIMESTAMP WITH TIME ZONE NOT NULL,
                          description VARCHAR(2000),
                          notes VARCHAR(5000),
                          created_by UUID NOT NULL,
                          archived BOOLEAN NOT NULL DEFAULT FALSE,
                          created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT fk_meetings_project
                              FOREIGN KEY (project_id) REFERENCES projects(id),

                          CONSTRAINT fk_meetings_created_by
                              FOREIGN KEY (created_by) REFERENCES users(id)
);