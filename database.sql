-- CREATE DATABASE attendance_db;

-- USE attendance_db;

-- CREATE TABLE attendance (
--     id INT PRIMARY KEY AUTO_INCREMENT,
--     student_name VARCHAR(100) NOT NULL,
--     roll_no VARCHAR(30) NOT NULL,
--     attendance_date DATE NOT NULL,
--     status VARCHAR(10) NOT NULL,
--     UNIQUE (roll_no, attendance_date)
-- );

-- SELECT * FROM attendance;


CREATE DATABASE IF NOT EXISTS attendance_db;

USE attendance_db;

CREATE TABLE IF NOT EXISTS attendance (
    id INT PRIMARY KEY AUTO_INCREMENT,
    student_name VARCHAR(100) NOT NULL,
    roll_no VARCHAR(30) NOT NULL,
    attendance_date DATE NOT NULL,
    status VARCHAR(10) NOT NULL,
    UNIQUE (roll_no, attendance_date)
);

-- View all saved attendance records
SELECT * FROM attendance
ORDER BY attendance_date DESC, roll_no ASC;


