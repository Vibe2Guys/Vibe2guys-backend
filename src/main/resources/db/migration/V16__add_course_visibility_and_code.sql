ALTER TABLE courses
    ADD COLUMN is_public BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN course_code VARCHAR(20);

UPDATE courses
SET course_code = CONCAT('COURSE-', LPAD(id::TEXT, 6, '0'))
WHERE course_code IS NULL;

ALTER TABLE courses
    ALTER COLUMN course_code SET NOT NULL;

ALTER TABLE courses
    ADD CONSTRAINT uk_courses_course_code UNIQUE (course_code);

CREATE INDEX idx_courses_is_public_title ON courses (is_public, title);
