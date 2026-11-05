CREATE TABLE decisions (
                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                           meeting_id UUID NOT NULL,

                           content VARCHAR(5000) NOT NULL,

                           status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

                           created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                           updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                           CONSTRAINT fk_decisions_meeting
                               FOREIGN KEY (meeting_id)
                                   REFERENCES meetings(id)
                                   ON DELETE CASCADE,

                           CONSTRAINT chk_decisions_status
                               CHECK (status IN ('ACTIVE', 'CANCELLED'))
);

CREATE INDEX idx_decisions_meeting_id
    ON decisions(meeting_id);