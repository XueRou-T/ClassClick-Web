# ClassClick

ClassClick is a cross-platform classroom response system developed to facilitate interactive quizzes between instructors and students. The system allows instructors to create and manage quiz sessions while students can join using session codes and submit multiple-choice responses through an Android mobile application.

## Features

- Instructor-led quiz session management
- Student access through session codes
- Multiple-choice response submission (A–D)
- User authentication and account management
- Database-backed answer recording
- Controlled question flow between instructor and students
- Cross-platform interaction between web server and Android client

## Technologies Used

- Java
- Java Servlets
- Android
- Android Studio
- MySQL
- Apache Tomcat
- HTML
- CSS
- JavaScript

## System Architecture

ClassClick consists of:

- **Android Client**
  - Allows students to login, join quiz sessions and submit answers

- **Server Application**
  - Handles authentication, session management, question flow, and response processing using Java Servlets

- **Database**
  - Stores user information, quiz data, and submitted responses using MySQL

## Contributors
### Xue Rou
- Developed Android application functionality for student quiz participation
- Implemented user authentication using Java Servlets and MySQL
- Developed multiple-choice response submission and answer recording features
- Integrated database operations for storing and retrieving quiz session data
- Implemented question flow control between instructor and student clients

## Database Setup

1. Create a MySQL database named `clicker_db`.
2. Import the provided SQL backup file.
3. Update the database connection details in the server source code.

## Project Information

This project was developed collaboratively as part of an academic coursework project.
