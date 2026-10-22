
CREATE TABLE IF NOT EXISTS meetings (
                                        id UUID PRIMARY KEY,
                                        project_id UUID NOT NULL REFERENCES projects(id),
    title VARCHAR(200) NOT NULL,
    meeting_at TIMESTAMP WITH TIME ZONE NOT NULL,
                             description VARCHAR(2000),
    notes VARCHAR(5000),
    created_by UUID NOT NULL REFERENCES users(id),
    archived BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
                             );
