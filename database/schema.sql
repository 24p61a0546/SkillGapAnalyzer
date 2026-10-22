-- =====================================================================
-- Skill Gap Analyzer - Database Schema
-- Database: skill_gap_db
-- Compatible with MySQL 8.0+
-- =====================================================================

CREATE DATABASE IF NOT EXISTS skill_gap_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE skill_gap_db;

-- 1. Users Table (Students & Administrators)
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    department VARCHAR(100) DEFAULT 'Computer Science & Engineering',
    year_of_study VARCHAR(20) DEFAULT '3rd Year',
    target_role_id INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_email (email)
) ENGINE=InnoDB;

-- 2. Job Roles Table
CREATE TABLE IF NOT EXISTS job_roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(120) NOT NULL UNIQUE,
    slug VARCHAR(120) NOT NULL UNIQUE,
    category VARCHAR(80) NOT NULL,
    description TEXT NOT NULL,
    icon VARCHAR(60) DEFAULT 'fa-briefcase',
    avg_salary VARCHAR(60) DEFAULT '$85,000 - $125,000 / yr',
    demand_level ENUM('Very High', 'High', 'Medium') DEFAULT 'High',
    experience_level VARCHAR(40) DEFAULT 'Entry to Mid Level',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 3. Skills Master Table
CREATE TABLE IF NOT EXISTS skills (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    category VARCHAR(80) NOT NULL,
    description TEXT,
    max_rating INT DEFAULT 5,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_skill_category (category)
) ENGINE=InnoDB;

-- 4. Role - Skill Benchmark Matrix
CREATE TABLE IF NOT EXISTS role_skills (
    role_id INT NOT NULL,
    skill_id INT NOT NULL,
    required_proficiency INT NOT NULL CHECK (required_proficiency BETWEEN 1 AND 5),
    importance ENUM('Critical', 'High', 'Medium') DEFAULT 'High',
    PRIMARY KEY (role_id, skill_id),
    CONSTRAINT fk_role_skills_role FOREIGN KEY (role_id) 
        REFERENCES job_roles(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_role_skills_skill FOREIGN KEY (skill_id) 
        REFERENCES skills(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- 5. Student Assessments Table (Records student's self-assessed skill level)
CREATE TABLE IF NOT EXISTS student_assessments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    skill_id INT NOT NULL,
    proficiency_level INT NOT NULL CHECK (proficiency_level BETWEEN 1 AND 5),
    notes VARCHAR(255) NULL,
    assessed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_user_skill (user_id, skill_id),
    CONSTRAINT fk_assessment_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_assessment_skill FOREIGN KEY (skill_id) 
        REFERENCES skills(id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_assessment_user (user_id)
) ENGINE=InnoDB;

-- 6. Learning Recommendations Catalog
CREATE TABLE IF NOT EXISTS recommendations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    skill_id INT NOT NULL,
    title VARCHAR(220) NOT NULL,
    type ENUM('Course', 'Hands-on Project', 'Documentation', 'Video Tutorial', 'Practice') NOT NULL DEFAULT 'Course',
    provider VARCHAR(100) NOT NULL,
    url VARCHAR(500) NOT NULL,
    estimated_hours VARCHAR(40) DEFAULT '15-20 hours',
    difficulty_level ENUM('Beginner', 'Intermediate', 'Advanced') DEFAULT 'Intermediate',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_recom_skill FOREIGN KEY (skill_id) 
        REFERENCES skills(id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_recom_skill (skill_id)
) ENGINE=InnoDB;

-- Add foreign key constraint for user target_role_id
ALTER TABLE users
    ADD CONSTRAINT fk_user_target_role
    FOREIGN KEY (target_role_id) REFERENCES job_roles(id)
    ON DELETE SET NULL ON UPDATE CASCADE;
