-- =====================================================================
-- Skill Gap Analyzer - Seed Data (Realistic Industry Benchmarks)
-- =====================================================================

USE skill_gap_db;

-- ---------------------------------------------------------------------
-- 1. Job Roles
-- ---------------------------------------------------------------------
INSERT INTO job_roles (id, title, slug, category, description, icon, avg_salary, demand_level, experience_level) VALUES
(1, 'Full Stack Java Developer', 'full-stack-java-developer', 'Software Engineering',
 'Builds modern enterprise web applications using Java Servlets, Spring Boot, MySQL, RESTful APIs, and responsive frontend technologies.',
 'fas fa-code', '$95,000 - $130,000 / yr', 'Very High', 'Entry to Mid Level'),

(2, 'Data Scientist & AI Specialist', 'data-scientist-ai', 'Artificial Intelligence',
 'Leverages statistical modeling, machine learning algorithms, Python, and data visualization to uncover patterns and build predictive systems.',
 'fas fa-brain', '$105,000 - $145,000 / yr', 'Very High', 'Mid Level'),

(3, 'DevOps & Cloud Engineer', 'devops-cloud-engineer', 'Infrastructure & Cloud',
 'Designs continuous integration/deployment pipelines, container orchestration with Docker & Kubernetes, and automated cloud infrastructure.',
 'fas fa-cloud', '$100,000 - $140,000 / yr', 'High', 'Entry to Mid Level'),

(4, 'Cybersecurity Analyst', 'cybersecurity-analyst', 'Security',
 'Monitors, investigates, and neutralizes security vulnerabilities, secures web architectures, and ensures regulatory compliance.',
 'fas fa-shield-alt', '$90,000 - $135,000 / yr', 'Very High', 'Entry to Mid Level'),

(5, 'Frontend Web Developer', 'frontend-web-developer', 'Web Development',
 'Creates intuitive, accessible, and high-performance user interfaces using HTML5, CSS3, JavaScript, modern reactive frameworks, and API integrations.',
 'fas fa-laptop-code', '$85,000 - $120,000 / yr', 'High', 'Entry Level')
ON DUPLICATE KEY UPDATE title=VALUES(title);

-- ---------------------------------------------------------------------
-- 2. Skills Catalog
-- ---------------------------------------------------------------------
INSERT INTO skills (id, name, category, description, max_rating) VALUES
-- Programming & Core
(1, 'Core Java (OOP, Collections, Multithreading)', 'Programming', 'Deep understanding of OOP principles, Java Collections framework, Streams, and concurrency.', 5),
(2, 'Python Programming', 'Programming', 'Writing clean, idiomatic Python code, data structures, and script automation.', 5),
(3, 'Data Structures & Algorithms', 'Core CS', 'Proficiency in Big-O analysis, trees, graphs, sorting, searching, and dynamic programming.', 5),

-- Web & Backend
(4, 'HTML5, CSS3 & Responsive Design', 'Web Frontend', 'Semantic markup, Flexbox, CSS Grid, mobile-first design, and accessibility.', 5),
(5, 'Modern JavaScript (ES6+)', 'Web Frontend', 'Async/await, Promises, DOM manipulation, closures, and modular JS.', 5),
(6, 'Java Servlets & JSP (MVC)', 'Web Backend', 'Building server-side Java web applications, session tracking, filters, and MVC architecture.', 5),
(7, 'RESTful API Design & Integration', 'Web Backend', 'Designing scalable HTTP/JSON endpoints, status codes, authentication, and API clients.', 5),
(8, 'Spring Boot & Microservices', 'Web Backend', 'Enterprise backend framework, dependency injection, Spring Data JPA, and security.', 5),

-- Database & Storage
(9, 'MySQL & Relational Database Design', 'Database', 'ER modeling, normalization, complex SQL joins, indexing, and stored procedures.', 5),
(10, 'JDBC & Database Connectivity', 'Database', 'Connecting Java backends to relational databases with PreparedStatements, transactions, and pooling.', 5),
(11, 'NoSQL & Document Databases (MongoDB)', 'Database', 'Schema-less data modeling, aggregation pipelines, and high-throughput document storage.', 5),

-- DevOps, Cloud & Tools
(12, 'Git & Version Control', 'DevOps & Tools', 'Branching strategies, pull requests, merge conflict resolution, and GitHub collaboration.', 5),
(13, 'Docker & Containerization', 'DevOps & Tools', 'Containerizing applications, Dockerfiles, multi-stage builds, and Docker Compose.', 5),
(14, 'CI/CD Pipelines (Jenkins / GitHub Actions)', 'DevOps & Tools', 'Automated testing, continuous integration, and zero-downtime deployment pipelines.', 5),
(15, 'Cloud Platforms (AWS / GCP / Azure)', 'Cloud', 'Core cloud services (EC2, S3, RDS, IAM, VPC) and serverless deployment.', 5),
(16, 'Linux CLI & Shell Scripting', 'Systems', 'Bash scripting, file permissions, process management, and remote server administration.', 5),

-- Data Science & AI
(17, 'Data Analysis (Pandas & NumPy)', 'Data Science', 'Data wrangling, cleaning, transformation, and exploratory data analysis.', 5),
(18, 'Machine Learning (Scikit-Learn)', 'Data Science', 'Supervised & unsupervised learning, cross-validation, regression, and classification.', 5),
(19, 'Data Visualization (Matplotlib, Seaborn)', 'Data Science', 'Plotting distributions, correlations, and presenting analytical dashboards.', 5),

-- Security
(20, 'Web Security (OWASP Top 10)', 'Security', 'Mitigating SQL injection, XSS, CSRF, insecure deserialization, and authentication flaws.', 5),
(21, 'Cryptography & Network Security', 'Security', 'HTTPS/TLS protocols, symmetric/asymmetric encryption, JWT tokens, and firewalls.', 5)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- ---------------------------------------------------------------------
-- 3. Role - Skill Benchmark Matrix (Required Proficiency: 1-5)
-- ---------------------------------------------------------------------
-- Role 1: Full Stack Java Developer
INSERT INTO role_skills (role_id, skill_id, required_proficiency, importance) VALUES
(1, 1, 5, 'Critical'), -- Core Java
(1, 4, 4, 'High'),     -- HTML5/CSS3
(1, 5, 4, 'High'),     -- JavaScript
(1, 6, 5, 'Critical'), -- Java Servlets & JSP
(1, 7, 4, 'Critical'), -- REST APIs
(1, 9, 4, 'Critical'), -- MySQL
(1, 10, 5, 'Critical'),-- JDBC
(1, 12, 4, 'High'),    -- Git
(1, 3, 3, 'High'),     -- Data Structures & Algorithms
(1, 8, 3, 'Medium'),   -- Spring Boot
(1, 13, 3, 'Medium'),  -- Docker
(1, 20, 3, 'High')     -- Web Security
ON DUPLICATE KEY UPDATE required_proficiency=VALUES(required_proficiency);

-- Role 2: Data Scientist & AI Specialist
INSERT INTO role_skills (role_id, skill_id, required_proficiency, importance) VALUES
(2, 2, 5, 'Critical'), -- Python
(2, 3, 4, 'High'),     -- DSA
(2, 9, 3, 'High'),     -- MySQL
(2, 12, 3, 'Medium'),  -- Git
(2, 17, 5, 'Critical'),-- Pandas & NumPy
(2, 18, 5, 'Critical'),-- Machine Learning
(2, 19, 4, 'High'),    -- Data Visualization
(2, 15, 3, 'Medium')   -- Cloud
ON DUPLICATE KEY UPDATE required_proficiency=VALUES(required_proficiency);

-- Role 3: DevOps & Cloud Engineer
INSERT INTO role_skills (role_id, skill_id, required_proficiency, importance) VALUES
(3, 12, 5, 'Critical'),-- Git
(3, 13, 5, 'Critical'),-- Docker
(3, 14, 5, 'Critical'),-- CI/CD
(3, 15, 5, 'Critical'),-- Cloud (AWS)
(3, 16, 5, 'Critical'),-- Linux CLI & Bash
(3, 2, 3, 'High'),     -- Python scripting
(3, 20, 4, 'High'),    -- Web Security
(3, 7, 3, 'Medium')    -- REST APIs
ON DUPLICATE KEY UPDATE required_proficiency=VALUES(required_proficiency);

-- Role 4: Cybersecurity Analyst
INSERT INTO role_skills (role_id, skill_id, required_proficiency, importance) VALUES
(4, 16, 5, 'Critical'),-- Linux
(4, 20, 5, 'Critical'),-- Web Security (OWASP)
(4, 21, 5, 'Critical'),-- Cryptography & Network Security
(4, 2, 4, 'High'),     -- Python
(4, 9, 3, 'High'),     -- MySQL
(4, 7, 3, 'Medium'),   -- REST APIs
(4, 12, 3, 'Medium')   -- Git
ON DUPLICATE KEY UPDATE required_proficiency=VALUES(required_proficiency);

-- Role 5: Frontend Web Developer
INSERT INTO role_skills (role_id, skill_id, required_proficiency, importance) VALUES
(5, 4, 5, 'Critical'), -- HTML5 & CSS3
(5, 5, 5, 'Critical'), -- Modern JavaScript
(5, 7, 4, 'High'),     -- REST APIs
(5, 12, 4, 'High'),    -- Git
(5, 3, 3, 'Medium'),   -- Data Structures
(5, 20, 3, 'Medium')   -- Web Security (XSS/CSRF)
ON DUPLICATE KEY UPDATE required_proficiency=VALUES(required_proficiency);

-- ---------------------------------------------------------------------
-- 4. Learning Recommendations Catalog
-- ---------------------------------------------------------------------
INSERT INTO recommendations (skill_id, title, type, provider, url, estimated_hours, difficulty_level) VALUES
-- Core Java
(1, 'Java Programming Masterclass for Software Developers', 'Course', 'Coursera / Duke University', 'https://www.coursera.org/specializations/java-programming', '40 hours', 'Intermediate'),
(1, 'Oracle Official Java Documentation & Tutorials', 'Documentation', 'Oracle Java Docs', 'https://docs.oracle.com/javase/tutorial/', '20 hours', 'Beginner'),
(1, 'Java OOP & Concurrency in Practice', 'Hands-on Project', 'Hyperskill / JetBrains Academy', 'https://hyperskill.org/tracks/1', '25 hours', 'Advanced'),

-- Python
(2, 'Python for Everybody Specialization', 'Course', 'Coursera / Univ. of Michigan', 'https://www.coursera.org/specializations/python', '30 hours', 'Beginner'),
(2, 'Official Python Documentation & Tutorial', 'Documentation', 'Python Software Foundation', 'https://docs.python.org/3/tutorial/', '15 hours', 'Beginner'),

-- DSA
(3, 'Algorithms, Part I & II by Robert Sedgewick', 'Course', 'Coursera / Princeton University', 'https://www.coursera.org/learn/algorithms-part1', '50 hours', 'Intermediate'),
(3, 'NeetCode 150 - Curated Coding Interview Roadmap', 'Practice', 'NeetCode.io', 'https://neetcode.io/practice', '60 hours', 'Intermediate'),

-- HTML5 / CSS3
(4, 'Responsive Web Design Certification', 'Course', 'freeCodeCamp', 'https://www.freecodecamp.org/learn/2022/responsive-web-design/', '30 hours', 'Beginner'),
(4, 'MDN Web Docs: HTML & CSS Complete Reference', 'Documentation', 'Mozilla Developer Network', 'https://developer.mozilla.org/en-US/docs/Learn', '20 hours', 'Beginner'),

-- JavaScript
(5, 'JavaScript: The Hard Parts (Closures, Async, Callbacks)', 'Video Tutorial', 'Frontend Masters / YouTube', 'https://developer.mozilla.org/en-US/docs/Web/JavaScript', '15 hours', 'Intermediate'),
(5, 'JavaScript Algorithms and Data Structures', 'Course', 'freeCodeCamp', 'https://www.freecodecamp.org/learn/javascript-algorithms-and-data-structures/', '40 hours', 'Beginner'),

-- Java Servlets & JSP
(6, 'Java Servlet and JSP Tutorial for Beginners', 'Video Tutorial', 'freeCodeCamp YouTube', 'https://www.youtube.com/results?search_query=java+servlet+jsp+tutorial', '12 hours', 'Intermediate'),
(6, 'Apache Tomcat 9 Servlet & JSP Documentation', 'Documentation', 'Apache Software Foundation', 'https://tomcat.apache.org/tomcat-9.0-doc/servletapi/', '10 hours', 'Intermediate'),
(6, 'Build an E-Commerce / Student Portal with Servlets & JDBC', 'Hands-on Project', 'GitHub Open Source Showcase', 'https://github.com/topics/servlet-jsp', '20 hours', 'Intermediate'),

-- RESTful APIs
(7, 'RESTful Web API Design & Best Practices Guide', 'Documentation', 'RESTful API Guidelines (GitHub)', 'https://restfulapi.net/', '8 hours', 'Beginner'),
(7, 'Designing RESTful APIs with Postman & Java', 'Practice', 'Postman Student Program', 'https://www.postman.com/student-program/', '10 hours', 'Intermediate'),

-- Spring Boot
(8, 'Spring Boot Fundamentals & Microservices', 'Course', 'Spring.io Official Guides', 'https://spring.io/guides', '25 hours', 'Intermediate'),

-- MySQL
(9, 'Relational Database Design and SQL Masterclass', 'Course', 'freeCodeCamp / Khan Academy', 'https://www.freecodecamp.org/news/sql-and-databases-full-course/', '16 hours', 'Beginner'),
(9, 'MySQL 8.0 Reference Manual: Indexing & Optimization', 'Documentation', 'Oracle MySQL Docs', 'https://dev.mysql.com/doc/refman/8.0/en/optimization.html', '12 hours', 'Intermediate'),

-- JDBC
(10, 'Java JDBC Database Access Official Tutorial', 'Documentation', 'Oracle Java Docs', 'https://docs.oracle.com/javase/tutorial/jdbc/', '10 hours', 'Beginner'),
(10, 'Building Production-Ready JDBC Connection Pools with HikariCP', 'Hands-on Project', 'Baeldung Guides', 'https://www.baeldung.com/hikaricp', '8 hours', 'Intermediate'),

-- Git
(12, 'Version Control with Git & GitHub Professional Guide', 'Course', 'Coursera / Google Career Certificates', 'https://www.coursera.org/learn/introduction-git-github', '15 hours', 'Beginner'),
(12, 'Pro Git Interactive Book (Free)', 'Documentation', 'Git SCM Official', 'https://git-scm.com/book/en/v2', '12 hours', 'Beginner'),

-- Docker
(13, 'Docker for Developers: From Containerization to Deployment', 'Course', 'Docker Official Learning', 'https://docs.docker.com/get-started/', '18 hours', 'Intermediate'),

-- CI/CD
(14, 'Automated CI/CD with GitHub Actions', 'Course', 'GitHub Skills', 'https://skills.github.com/', '10 hours', 'Intermediate'),

-- Cloud (AWS)
(15, 'AWS Certified Cloud Practitioner Essentials', 'Course', 'AWS Skill Builder', 'https://aws.amazon.com/training/digital/aws-cloud-practitioner-essentials/', '20 hours', 'Beginner'),

-- Linux CLI
(16, 'Linux Command Line Basics for Developers', 'Course', 'edX / Linux Foundation', 'https://www.edx.org/learn/linux', '15 hours', 'Beginner'),

-- Data Analysis & ML
(17, 'Python for Data Science and Machine Learning Bootcamp', 'Course', 'Coursera / Kaggle Learn', 'https://www.kaggle.com/learn', '30 hours', 'Beginner'),
(18, 'Machine Learning Specialization by Andrew Ng', 'Course', 'DeepLearning.AI & Stanford', 'https://www.coursera.org/specializations/machine-learning-introduction', '45 hours', 'Intermediate'),
(19, 'Data Visualization with Matplotlib & Seaborn', 'Practice', 'Kaggle Learn Tutorials', 'https://www.kaggle.com/learn/data-visualization', '12 hours', 'Beginner'),

-- Security
(20, 'OWASP Top 10 Web Application Vulnerabilities Lab', 'Hands-on Project', 'PortSwigger Web Security Academy', 'https://portswigger.net/web-security', '35 hours', 'Intermediate'),
(21, 'Applied Cryptography & Network Security Fundamentals', 'Course', 'Coursera / Stanford University', 'https://www.coursera.org/learn/crypto', '25 hours', 'Advanced');

-- ---------------------------------------------------------------------
-- 5. Demo Student Account
-- Password: password123 (SHA-256: ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f)
-- ---------------------------------------------------------------------
INSERT INTO users (id, full_name, email, password_hash, department, year_of_study, target_role_id) VALUES
(1, 'Alex Mercer', 'student@college.edu', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'Computer Science & Engineering', '3rd Year', 1)
ON DUPLICATE KEY UPDATE full_name=VALUES(full_name);

-- ---------------------------------------------------------------------
-- 6. Initial Sample Assessment for Demo Student (Alex Mercer)
-- ---------------------------------------------------------------------
INSERT INTO student_assessments (user_id, skill_id, proficiency_level, notes) VALUES
(1, 1, 3, 'Familiar with OOP and basic collections, need more multithreading practice'),
(1, 4, 4, 'Comfortable with responsive Flexbox and Grid layouts'),
(1, 5, 3, 'Know ES6 syntax, working on async/await and promises'),
(1, 6, 2, 'Basic understanding of doGet/doPost, need more session and filter practice'),
(1, 7, 2, 'Have consumed APIs, need practice designing standard REST endpoints'),
(1, 9, 3, 'Can write standard SQL queries, need indexing & optimization skills'),
(1, 10, 2, 'Basic JDBC code written, want to understand connection pools'),
(1, 12, 3, 'Use Git for personal repos, learning branch management and PRs')
ON DUPLICATE KEY UPDATE proficiency_level=VALUES(proficiency_level);
