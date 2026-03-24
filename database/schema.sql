CREATE DATABASE IF NOT EXISTS clicker_db;
USE clicker_db;

-- Users: instructors and students
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    password VARCHAR(50) NOT NULL,   -
    usertype ENUM('instructor', 'student') NOT NULL
);

-- Categories: group question sets 
CREATE TABLE category (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL UNIQUE
);

-- Question sets: belong to a category
CREATE TABLE question_set (
    set_id INT AUTO_INCREMENT PRIMARY KEY,
    set_name VARCHAR(100) NOT NULL UNIQUE,
    category_id INT NOT NULL,
    FOREIGN KEY (category_id) REFERENCES category(category_id)
        ON DELETE CASCADE
);

-- Questions: belong to a set
CREATE TABLE question (
    question_id INT AUTO_INCREMENT PRIMARY KEY,
    text TEXT NOT NULL,
    set_id INT NOT NULL,
    FOREIGN KEY (set_id) REFERENCES question_set(set_id)
        ON DELETE CASCADE
);

-- Choices: belong to a question (fixed MCQ with 4 options)
CREATE TABLE choices (
    choice_id INT AUTO_INCREMENT PRIMARY KEY,
    choice VARCHAR(1) NOT NULL,       -- 'A', 'B', 'C', 'D'
    label TEXT NOT NULL,              -- full text of the option
    question_id INT NOT NULL,
    is_correct BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (question_id) REFERENCES question(question_id)
        ON DELETE CASCADE
);

-- Sessions: instructor runs a quiz set
CREATE TABLE sessions (
    session_id INT AUTO_INCREMENT PRIMARY KEY,
    set_id INT NOT NULL,
    instructor_id INT NOT NULL,
    current_question_id INT,
    status ENUM('active', 'ended') DEFAULT 'active',
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP NULL,

    FOREIGN KEY (set_id) REFERENCES question_set(set_id),
    FOREIGN KEY (instructor_id) REFERENCES users(user_id),
    FOREIGN KEY (current_question_id) REFERENCES question(question_id)
);

-- Responses: students answering MCQs
CREATE TABLE responses (
    response_id INT AUTO_INCREMENT PRIMARY KEY,
    session_id INT NOT NULL,
    question_id INT NOT NULL,
    choice_id INT NOT NULL,
    user_id INT NOT NULL,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (session_id) REFERENCES sessions(session_id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES question(question_id) ON DELETE CASCADE,
    FOREIGN KEY (choice_id) REFERENCES choices(choice_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,

    UNIQUE (session_id, question_id, user_id) -- one answer per student per question
);