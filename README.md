# Skill Gap Analyzer 🎓🚀
> **College Capstone Web Application for Campus Placement & Career Readiness**  
> Developed with **Java Servlets (MVC)**, **MySQL (JDBC)**, **Apache Tomcat**, **Apache Ant**, and **HTML5/CSS3/JavaScript**.

[![Java 8+](https://img.shields.io/badge/Java-8%20%7C%2011%20%7C%2017-orange.svg)](https://www.oracle.com/java/)
[![Tomcat 9+](https://img.shields.io/badge/Server-Apache%20Tomcat%209.x-blue.svg)](https://tomcat.apache.org/)
[![Build Tool](https://img.shields.io/badge/Build-Apache%20Ant-red.svg)](https://ant.apache.org/)
[![Database](https://img.shields.io/badge/Database-MySQL%208.0+-4479A1.svg)](https://www.mysql.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

---

## 📌 1. Project Overview

The **Skill Gap Analyzer** is a dynamic web application built for college engineering and computer science students. It bridges the critical divide between university curriculum and industry expectations.

Students can:
1. **Choose an Industry Target Role** (e.g., *Full Stack Java Developer*, *Data Scientist & AI Specialist*, *DevOps & Cloud Engineer*, *Cybersecurity Analyst*, *Frontend Web Developer*).
2. **Perform an Interactive Skill Assessment** across 20+ core and emerging software proficiencies using a 1-to-5 scoring rubric.
3. **Analyze Skill Gaps in Real Time** using an automated weighted mathematical readiness algorithm, visualized with **Chart.js Radar Charts** and **Side-by-Side Benchmark Bar Graphs**.
4. **Follow Tailored Learning Roadmaps** with direct links to free curated certifications, official documentation, video tutorials, and GitHub hands-on projects.

---

## 🛠️ 2. Technology Stack

| Layer | Technology | Details |
|---|---|---|
| **Frontend** | HTML5, CSS3, Modern JavaScript (ES6+) | Vanilla JS (Fetch API), Chart.js (Radar & Bar), FontAwesome 6, Google Fonts |
| **Backend** | Java Servlets (Java EE / Jakarta Servlet 4.0) | Clean **MVC** Architecture with **DAO** and **Service** layers |
| **Database** | MySQL 8.0+ | Relational schema with Foreign Keys, Cascading, and Constraints |
| **Connectivity**| JDBC (`mysql-connector-j-8.3.0`) | PreparedStatements (SQL Injection Safe), Thread-safe connection pool |
| **Build Tool** | Apache Ant (`build.xml`) | Standard targets: `clean`, `init`, `compile`, `war`, `deploy` |
| **Web Server** | Apache Tomcat 9.0+ | Standard `.war` archive deployment |
| **Serialization**| Google Gson 2.10.1 | High-performance JSON request/response handling |

---

## 📂 3. Project Directory Structure

```text
SKILLGAPANALYZER/
├── build.xml                       # Canonical Apache Ant build script
├── build.properties                # Build configurations (Tomcat home, paths)
├── build.bat / build.ps1           # One-click build script (Ant or JDK fallback)
├── setup_db.bat / setup_db.ps1     # One-click MySQL database installer
├── Dockerfile                      # Production Docker container for Tomcat + App
├── docker-compose.yml              # Complete MySQL + Tomcat stack container
├── .gitignore                      # Git ignore rules for Java/Tomcat/IDEs
├── README.md                       # Complete documentation & viva guide
│
├── database/
│   ├── schema.sql                  # Database table definitions (DDL)
│   └── seed_data.sql               # Pre-populated industry benchmarks & roles
│
├── lib/                            # Compile-time dependencies for Ant
│   ├── javax.servlet-api-4.0.1.jar
│   ├── mysql-connector-j-8.3.0.jar
│   └── gson-2.10.1.jar
│
├── src/
│   ├── db.properties               # Database connection parameters
│   └── com/
│       └── skillgap/
│           ├── controller/         # Java Servlets (HTTP Handlers)
│           │   ├── AuthServlet.java           # Login, Register, Logout, Me
│           │   ├── DashboardServlet.java      # Student profile & dashboard stats
│           │   ├── JobRoleServlet.java        # Roles catalog & role selection
│           │   ├── AssessmentServlet.java     # Skill quiz & batch score submission
│           │   ├── AnalysisServlet.java       # Gap calculation & report generator
│           │   └── RecommendationServlet.java # Learning roadmaps & resources
│           ├── dao/                # Data Access Objects (JDBC queries)
│           │   ├── UserDAO.java
│           │   ├── JobRoleDAO.java
│           │   ├── SkillDAO.java
│           │   ├── AssessmentDAO.java
│           │   └── RecommendationDAO.java
│           ├── model/              # Domain Models / POJOs
│           │   ├── User.java
│           │   ├── JobRole.java
│           │   ├── Skill.java
│           │   ├── RoleSkill.java
│           │   ├── Assessment.java
│           │   ├── SkillGapResult.java
│           │   ├── GapAnalysisReport.java
│           │   └── Recommendation.java
│           ├── service/            # Business & Algorithmic Logic
│           │   ├── AuthService.java
│           │   ├── GapAnalysisService.java    # Weighted gap scoring engine
│           │   └── RecommendationService.java
│           ├── filter/             # Security & Request Filters
│           │   ├── AuthenticationFilter.java  # Session gatekeeper
│           │   └── CorsAndEncodingFilter.java # UTF-8 encoding & headers
│           └── util/               # Utility classes
│               ├── DBConnection.java          # JDBC connection manager
│               ├── PasswordUtil.java          # SHA-256 password hashing
│               └── JsonUtil.java              # Gson response serializing
│
└── WebContent/                     # Web root deployed into WAR
    ├── WEB-INF/
    │   ├── web.xml                 # Deployment descriptor
    │   └── lib/                    # Runtime dependencies
    │       ├── mysql-connector-j-8.3.0.jar
    │       └── gson-2.10.1.jar
    ├── assets/
    │   ├── css/                    # Modular CSS design system
    │   │   ├── global.css
    │   │   ├── auth.css
    │   │   ├── dashboard.css
    │   │   ├── assessment.css
    │   │   └── analysis.css
    │   └── js/                     # Client-side controllers
    │       ├── api.js              # Fetch API client & notifications
    │       ├── auth.js             # Authentication logic
    │       ├── dashboard.js        # KPI populator
    │       ├── assessment.js       # Dynamic scoring matrix
    │       ├── analysis.js         # Chart.js Radar & Bar visualizations
    │       ├── roles.js            # Role catalog & switcher
    │       └── recommendations.js  # Roadmap resources
    ├── index.html                  # Landing Page
    ├── login.html                  # Student Sign In
    ├── register.html               # Student Registration
    ├── dashboard.html              # Main Student Portal
    ├── roles.html                  # Industry Roles Catalog
    ├── assessment.html             # Self-Assessment Quiz
    ├── analysis.html               # Visual Skill Gap Report
    └── recommendations.html        # Curated Learning Roadmaps
```

---

## 🧮 4. The Skill Gap & Readiness Algorithm

The application does not use arbitrary scores. It calculates student readiness through a **Weighted Euclidean Gap Formula**:

1. **Individual Skill Gap**:
   $$\text{Gap}_i = \max(0, \text{RequiredLevel}_i - \text{CurrentLevel}_i)$$
2. **Importance Weighting ($w_i$)**:
   - `Critical` = $3.0$
   - `High` = $2.0$
   - `Medium` = $1.0$
3. **Role Readiness Score (%)**:
   $$\text{Readiness Percentage} = \left( \frac{\sum_{i=1}^{n} w_i \times \min(\text{CurrentLevel}_i, \text{RequiredLevel}_i)}{\sum_{i=1}^{n} w_i \times \text{RequiredLevel}_i} \right) \times 100$$
4. **Classification**:
   - $\ge 85\%$: **Industry Ready**
   - $65\% - 84\%$: **High Potential (Moderate Gap)**
   - $40\% - 64\%$: **Intermediate Learner**
   - $< 40\%$: **Foundational Phase**

---

## ⚙️ 5. Step-by-Step Setup Instructions

### Prerequisites
- **JDK 8, 11, or 17+** (`javac -version`)
- **MySQL Server 8.0+** running on port 3306
- **Apache Tomcat 9.0+**
- **Apache Ant** (Optional: a standalone fallback build script is included)

---

### Step 1: Database Setup
1. Make sure your MySQL service is running.
2. Run the automated database installer:
   ```cmd
   setup_db.bat
   ```
   *(Or in PowerShell)*:
   ```powershell
   powershell -ExecutionPolicy Bypass -File .\setup_db.ps1
   ```
3. *Alternative manual import via MySQL Workbench or CLI*:
   ```sql
   mysql -u root -p < database/schema.sql
   mysql -u root -p < database/seed_data.sql
   ```
4. Verify database credentials in `src/db.properties` match your local MySQL configuration:
   ```properties
   db.host=localhost
   db.port=3306
   db.name=skill_gap_db
   db.user=root
   db.pass=your_password
   ```

---

### Step 2: Build the Application (`.war`)

#### Option A: Using Apache Ant
Open a terminal in the project root directory and run:
```cmd
ant war
```
*Ant will compile the Java source files, run the deployment descriptor checks, and generate `dist/SkillGapAnalyzer.war`.*

#### Option B: Using the Included Windows Batch / PowerShell Helper
If Apache Ant is not yet added to your environment `PATH`:
```cmd
build.bat
```
*(Or in PowerShell)*:
```powershell
powershell -ExecutionPolicy Bypass -File .\build.ps1
```

Both methods produce the standardized archive:
$$\text{Output: } \texttt{dist/SkillGapAnalyzer.war}$$

---

### Step 3: Deploy to Apache Tomcat
1. Copy the generated `dist/SkillGapAnalyzer.war` to your Tomcat `webapps/` folder:
   ```cmd
   copy dist\SkillGapAnalyzer.war C:\apache-tomcat-9.0.x\webapps\
   ```
2. Start Tomcat:
   ```cmd
   C:\apache-tomcat-9.0.x\bin\startup.bat
   ```
3. Open your browser and navigate to:
   ```text
   http://localhost:8080/SkillGapAnalyzer/
   ```

---

## 👤 6. Demo Student Account Credentials

For quick evaluation and demonstrations, the database seed includes a pre-configured student profile:

- **Email**: `student@college.edu`
- **Password**: `password123`
- **Name**: Alex Mercer (Computer Science & Engineering, 3rd Year)
- **Pre-assessed Skills**: 8 skills evaluated, target role set to *Full Stack Java Developer*.

*(You can also click the **"Auto Fill"** button on the login screen to sign in instantly!)*

---

## 🌐 7. Public Deployment Options for Your Professor

To allow your professor or evaluation panel to test the live project over the internet without installing anything on their machine, use either of the following two methods:

### Method 1: Instant Live HTTPS Tunnel via ngrok (Recommended for Presentations)
Run the application locally on your laptop, and expose it through an instant secure public URL:
1. Download [ngrok](https://ngrok.com/download) (free).
2. Start Tomcat locally on port 8080.
3. In a terminal, run:
   ```bash
   ngrok http 8080
   ```
4. Copy the generated `https://xxxx.ngrok-free.app` URL and share it with your professor:
   ```text
   https://xxxx.ngrok-free.app/SkillGapAnalyzer/
   ```

---

### Method 2: Free Cloud Deployment via Render / Railway (Dockerized)
The project includes a production-grade multi-stage `Dockerfile` and `docker-compose.yml`.

1. Push your repository to **GitHub**.
2. Go to [Render.com](https://render.com) or [Railway.app](https://railway.app).
3. Connect your GitHub repository.
4. Set up a free **MySQL** instance on Render/Railway.
5. Provide the environment variables:
   - `DB_HOST`: your cloud MySQL host
   - `DB_PORT`: `3306`
   - `DB_NAME`: `skill_gap_db`
   - `DB_USER`: your cloud MySQL user
   - `DB_PASS`: your cloud MySQL password
6. Select **Docker** environment — Render/Railway will automatically run the `Dockerfile`, compiling the project and serving Tomcat on a public HTTPS URL (e.g. `https://skillgap-analyzer.onrender.com`).

---

## 🔌 8. RESTful API Endpoints Reference

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `POST` | `/api/auth/register` | Register new student profile | Public |
| `POST` | `/api/auth/login` | Authenticate student & start session | Public |
| `GET` | `/api/auth/me` | Fetch active student session | Protected |
| `GET` | `/api/auth/logout` | Invalidate session | Public |
| `GET` | `/api/dashboard` | Fetch KPI stats, readiness score & top gaps | Protected |
| `GET` | `/api/roles` | List all industry roles with skill benchmarks | Public |
| `POST` | `/api/roles/select` | Update student's target career role | Protected |
| `GET` | `/api/assessment/skills`| Fetch all skills annotated with current scores | Protected |
| `POST` | `/api/assessment/submit`| Batch submit proficiency ratings (1-5) | Protected |
| `GET` | `/api/analysis` | Generate Radar/Bar comparison report | Protected |
| `GET` | `/api/recommendations` | Get personalized learning resources | Protected |

---

## 🎓 9. College Project Viva / Presentation Q&A

**Q1: Why did you choose Java Servlets instead of Spring Boot?**  
*Answer*: Java Servlets provide direct, low-level mastery of the HTTP request-response lifecycle, servlet filters, and session management without heavy framework abstraction. It satisfies core academic computer science curricula while maintaining enterprise scalability.

**Q2: How does the application prevent SQL Injection?**  
*Answer*: All database queries in the DAO layer exclusively utilize `PreparedStatement` with parameterized placeholders (`?`), ensuring user inputs are never concatenated directly into raw SQL.

**Q3: How are passwords secured?**  
*Answer*: Passwords are never stored in plaintext. They are hashed using cryptographic **SHA-256** inside `PasswordUtil.java` before insertion into the database.

**Q4: How does the skill gap visualization work?**  
*Answer*: The backend computes benchmark discrepancies and delivers a structured JSON payload to the client. The frontend uses **Chart.js** to dynamically render an $N$-dimensional Radar chart overlaying the student's proficiency polygon directly atop the industry benchmark polygon.

---

## 📄 10. License
This project is open-source under the **MIT License**. Created for academic evaluation and career enablement.
