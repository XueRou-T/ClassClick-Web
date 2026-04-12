USE clicker_db;

-- Users
INSERT INTO users (username, display_name, password, usertype) VALUES
('instructor1', 'Dr. Smith', 'pass123', 'instructor'),
('student1', 'Alice', 'pass123', 'student'),
('student2', 'Bob', 'pass123', 'student'),
('student3', 'Charlie', 'pass123', 'student');

-- Question sets
INSERT INTO question_set (set_name) VALUES
('Physics Basics'),
('Math Fundamentals'),
('Computer Science'),
('General Knowledge');

-- Physics Basics
INSERT INTO question (text, set_id) VALUES
('What is the speed of light?', 1),
('Which planet is known as the Red Planet?', 1);

INSERT INTO choices (choice, label, is_correct, question_id) VALUES
('A','3 x 10^8 m/s', TRUE, 1),
('B','1 x 10^6 m/s', FALSE, 1),
('C','3 x 10^5 m/s', FALSE, 1),
('D','None of the above', FALSE, 1),

('A','Earth', FALSE, 2),
('B','Mars', TRUE, 2),
('C','Jupiter', FALSE, 2),
('D','Venus', FALSE, 2);

-- Math Fundamentals
INSERT INTO question (text, set_id) VALUES
('What is 12 multiplied by 8?', 2),
('What is one half of 50?', 2);

INSERT INTO choices (choice, label, is_correct, question_id) VALUES
('A','96', TRUE, 3),
('B','88', FALSE, 3),
('C','108', FALSE, 3),
('D','100', FALSE, 3),

('A','20', FALSE, 4),
('B','25', TRUE, 4),
('C','30', FALSE, 4),
('D','15', FALSE, 4);

-- Computer Science
INSERT INTO question (text, set_id) VALUES
('Which data structure uses FIFO?', 3),
('What does SQL stand for?', 3);

INSERT INTO choices (choice, label, is_correct, question_id) VALUES
('A','Stack', FALSE, 5),
('B','Queue', TRUE, 5),
('C','Tree', FALSE, 5),
('D','Graph', FALSE, 5),

('A','Structured Query Language', TRUE, 6),
('B','Simple Query Logic', FALSE, 6),
('C','Sequential Question Language', FALSE, 6),
('D','System Query List', FALSE, 6);

-- General Knowledge
INSERT INTO question (text, set_id) VALUES
('Who wrote "Hamlet"?', 4),
('What is the capital of Japan?', 4);

INSERT INTO choices (choice, label, is_correct, question_id) VALUES
('A','Charles Dickens', FALSE, 7),
('B','William Shakespeare', TRUE, 7),
('C','Mark Twain', FALSE, 7),
('D','Jane Austen', FALSE, 7),

('A','Tokyo', TRUE, 8),
('B','Kyoto', FALSE, 8),
('C','Osaka', FALSE, 8),
('D','Nagoya', FALSE, 8);

