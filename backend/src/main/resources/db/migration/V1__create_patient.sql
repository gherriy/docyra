CREATE TABLE patient (
    id BIGSERIAL PRIMARY KEY,
    last_name VARCHAR(100) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    CONSTRAINT uk_patient_name UNIQUE (last_name, first_name)
);