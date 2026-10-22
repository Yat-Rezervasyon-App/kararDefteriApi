
ALTER TABLE users
    ADD COLUMN username VARCHAR(255);

ALTER TABLE users
    ADD COLUMN role VARCHAR(255);

UPDATE users
SET username = 'developer',
    role = 'USER';

ALTER TABLE users
    ALTER COLUMN username SET NOT NULL;

ALTER TABLE users
    ALTER COLUMN role SET NOT NULL;
