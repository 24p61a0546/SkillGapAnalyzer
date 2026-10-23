package com.skillgap.util;

import com.skillgap.model.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Safe Demo Mode Catalog for Skill Gap Analyzer.
 * Provides instant in-memory fallback for Job Roles, Skills, Role-Skill Benchmarks,
 * and Learning Recommendations whenever an external database is disconnected or loading.
 * Preserves 100% functionality without requiring authentication.
 */
public class DemoCatalog {

    private static final Map<Integer, JobRole> ROLES = new LinkedHashMap<>();
    private static final Map<Integer, Skill> SKILLS = new LinkedHashMap<>();
    private static final Map<Integer, List<RoleSkill>> ROLE_SKILLS = new LinkedHashMap<>();
    private static final Map<Integer, List<Recommendation>> RECOMMENDATIONS = new LinkedHashMap<>();
    private static final Map<Integer, Map<Integer, Integer>> IN_MEMORY_RATINGS = new ConcurrentHashMap<>();

    static {
        initSkills();
        initRoles();
        initRoleSkills();
        initRecommendations();
    }

    private static void initSkills() {
        addSkill(1, "Core Java (OOP, Collections, Multithreading)", "Programming", "Deep understanding of OOP principles, Java Collections framework, Streams, and concurrency.", 5);
        addSkill(2, "Python Programming", "Programming", "Writing clean, idiomatic Python code, data structures, and script automation.", 5);
        addSkill(3, "Data Structures & Algorithms", "Core CS", "Proficiency in Big-O analysis, trees, graphs, sorting, searching, and dynamic programming.", 5);
        addSkill(4, "HTML5, CSS3 & Responsive Design", "Web Frontend", "Semantic markup, Flexbox, CSS Grid, mobile-first design, and accessibility.", 5);
        addSkill(5, "Modern JavaScript (ES6+)", "Web Frontend", "Async/await, Promises, DOM manipulation, closures, and modular JS.", 5);
        addSkill(6, "Java Servlets & JSP (MVC)", "Web Backend", "Building server-side Java web applications, session tracking, filters, and MVC architecture.", 5);
        addSkill(7, "RESTful API Design & Integration", "Web Backend", "Designing scalable HTTP/JSON endpoints, status codes, authentication, and API clients.", 5);
        addSkill(8, "Spring Boot & Microservices", "Web Backend", "Enterprise backend framework, dependency injection, Spring Data JPA, and security.", 5);
        addSkill(9, "MySQL & Relational Database Design", "Database", "ER modeling, normalization, complex SQL joins, indexing, and stored procedures.", 5);
        addSkill(10, "JDBC & Database Connectivity", "Database", "Connecting Java backends to relational databases with PreparedStatements, transactions, and pooling.", 5);
        addSkill(11, "NoSQL & Document Databases (MongoDB)", "Database", "Schema-less data modeling, aggregation pipelines, and high-throughput document storage.", 5);
        addSkill(12, "Git & Version Control", "DevOps & Tools", "Branching strategies, pull requests, merge conflict resolution, and GitHub collaboration.", 5);
        addSkill(13, "Docker & Containerization", "DevOps & Tools", "Containerizing applications, Dockerfiles, multi-stage builds, and Docker Compose.", 5);
        addSkill(14, "CI/CD Pipelines (Jenkins / GitHub Actions)", "DevOps & Tools", "Automated testing, continuous integration, and zero-downtime deployment pipelines.", 5);
        addSkill(15, "Cloud Platforms (AWS / GCP / Azure)", "Cloud", "Core cloud services (EC2, S3, RDS, IAM, VPC) and serverless deployment.", 5);
        addSkill(16, "Linux CLI & Shell Scripting", "Systems", "Bash scripting, file permissions, process management, and remote server administration.", 5);
        addSkill(17, "Data Analysis (Pandas & NumPy)", "Data Science", "Data wrangling, cleaning, transformation, and exploratory data analysis.", 5);
        addSkill(18, "Machine Learning (Scikit-Learn)", "Data Science", "Supervised & unsupervised learning, cross-validation, regression, and classification.", 5);
        addSkill(19, "Data Visualization (Matplotlib, Seaborn)", "Data Science", "Plotting distributions, correlations, and presenting analytical dashboards.", 5);
        addSkill(20, "Web Security (OWASP Top 10)", "Security", "Mitigating SQL injection, XSS, CSRF, insecure deserialization, and authentication flaws.", 5);
        addSkill(21, "Cryptography & Network Security", "Security", "HTTPS/TLS protocols, symmetric/asymmetric encryption, JWT tokens, and firewalls.", 5);
    }

    private static void addSkill(int id, String name, String category, String description, int maxRating) {
        SKILLS.put(id, new Skill(id, name, category, description, maxRating));
    }

    private static void initRoles() {
        ROLES.put(1, new JobRole(1, "Full Stack Java Developer", "full-stack-java-developer", "Software Engineering",
                "Builds modern enterprise web applications using Java Servlets, Spring Boot, MySQL, RESTful APIs, and responsive frontend technologies.",
                "fas fa-code", "$95,000 - $130,000 / yr", "Very High", "Entry to Mid Level"));

        ROLES.put(2, new JobRole(2, "Data Scientist & AI Specialist", "data-scientist-ai", "Artificial Intelligence",
                "Leverages statistical modeling, machine learning algorithms, Python, and data visualization to uncover patterns and build predictive systems.",
                "fas fa-brain", "$105,000 - $145,000 / yr", "Very High", "Mid Level"));

        ROLES.put(3, new JobRole(3, "DevOps & Cloud Engineer", "devops-cloud-engineer", "Infrastructure & Cloud",
                "Designs continuous integration/deployment pipelines, container orchestration with Docker & Kubernetes, and automated cloud infrastructure.",
                "fas fa-cloud", "$100,000 - $140,000 / yr", "High", "Entry to Mid Level"));

        ROLES.put(4, new JobRole(4, "Cybersecurity Analyst", "cybersecurity-analyst", "Security",
                "Monitors, investigates, and neutralizes security vulnerabilities, secures web architectures, and ensures regulatory compliance.",
                "fas fa-shield-alt", "$90,000 - $135,000 / yr", "Very High", "Entry to Mid Level"));

        ROLES.put(5, new JobRole(5, "Frontend Web Developer", "frontend-web-developer", "Web Development",
                "Creates intuitive, accessible, and high-performance user interfaces using HTML5, CSS3, JavaScript, modern reactive frameworks, and API integrations.",
                "fas fa-laptop-code", "$85,000 - $120,000 / yr", "High", "Entry Level"));
    }

    private static void initRoleSkills() {
        // Role 1: Full Stack Java Developer
        List<RoleSkill> r1 = new ArrayList<>();
        r1.add(new RoleSkill(1, 1, "Core Java (OOP, Collections, Multithreading)", "Programming", 5, "Critical"));
        r1.add(new RoleSkill(1, 6, "Java Servlets & JSP (MVC)", "Web Backend", 5, "Critical"));
        r1.add(new RoleSkill(1, 10, "JDBC & Database Connectivity", "Database", 5, "Critical"));
        r1.add(new RoleSkill(1, 9, "MySQL & Relational Database Design", "Database", 4, "Critical"));
        r1.add(new RoleSkill(1, 7, "RESTful API Design & Integration", "Web Backend", 4, "Critical"));
        r1.add(new RoleSkill(1, 4, "HTML5, CSS3 & Responsive Design", "Web Frontend", 4, "High"));
        r1.add(new RoleSkill(1, 5, "Modern JavaScript (ES6+)", "Web Frontend", 4, "High"));
        r1.add(new RoleSkill(1, 12, "Git & Version Control", "DevOps & Tools", 4, "High"));
        r1.add(new RoleSkill(1, 3, "Data Structures & Algorithms", "Core CS", 3, "High"));
        r1.add(new RoleSkill(1, 8, "Spring Boot & Microservices", "Web Backend", 3, "Medium"));
        r1.add(new RoleSkill(1, 13, "Docker & Containerization", "DevOps & Tools", 3, "Medium"));
        r1.add(new RoleSkill(1, 20, "Web Security (OWASP Top 10)", "Security", 3, "High"));
        ROLE_SKILLS.put(1, r1);

        // Role 2: Data Scientist & AI Specialist
        List<RoleSkill> r2 = new ArrayList<>();
        r2.add(new RoleSkill(2, 2, "Python Programming", "Programming", 5, "Critical"));
        r2.add(new RoleSkill(2, 17, "Data Analysis (Pandas & NumPy)", "Data Science", 5, "Critical"));
        r2.add(new RoleSkill(2, 18, "Machine Learning (Scikit-Learn)", "Data Science", 5, "Critical"));
        r2.add(new RoleSkill(2, 3, "Data Structures & Algorithms", "Core CS", 4, "High"));
        r2.add(new RoleSkill(2, 19, "Data Visualization (Matplotlib, Seaborn)", "Data Science", 4, "High"));
        r2.add(new RoleSkill(2, 9, "MySQL & Relational Database Design", "Database", 3, "High"));
        r2.add(new RoleSkill(2, 12, "Git & Version Control", "DevOps & Tools", 3, "Medium"));
        r2.add(new RoleSkill(2, 15, "Cloud Platforms (AWS / GCP / Azure)", "Cloud", 3, "Medium"));
        ROLE_SKILLS.put(2, r2);

        // Role 3: DevOps & Cloud Engineer
        List<RoleSkill> r3 = new ArrayList<>();
        r3.add(new RoleSkill(3, 12, "Git & Version Control", "DevOps & Tools", 5, "Critical"));
        r3.add(new RoleSkill(3, 13, "Docker & Containerization", "DevOps & Tools", 5, "Critical"));
        r3.add(new RoleSkill(3, 14, "CI/CD Pipelines (Jenkins / GitHub Actions)", "DevOps & Tools", 5, "Critical"));
        r3.add(new RoleSkill(3, 15, "Cloud Platforms (AWS / GCP / Azure)", "Cloud", 5, "Critical"));
        r3.add(new RoleSkill(3, 16, "Linux CLI & Shell Scripting", "Systems", 5, "Critical"));
        r3.add(new RoleSkill(3, 20, "Web Security (OWASP Top 10)", "Security", 4, "High"));
        r3.add(new RoleSkill(3, 2, "Python Programming", "Programming", 3, "High"));
        r3.add(new RoleSkill(3, 7, "RESTful API Design & Integration", "Web Backend", 3, "Medium"));
        ROLE_SKILLS.put(3, r3);

        // Role 4: Cybersecurity Analyst
        List<RoleSkill> r4 = new ArrayList<>();
        r4.add(new RoleSkill(4, 16, "Linux CLI & Shell Scripting", "Systems", 5, "Critical"));
        r4.add(new RoleSkill(4, 20, "Web Security (OWASP Top 10)", "Security", 5, "Critical"));
        r4.add(new RoleSkill(4, 21, "Cryptography & Network Security", "Security", 5, "Critical"));
        r4.add(new RoleSkill(4, 2, "Python Programming", "Programming", 4, "High"));
        r4.add(new RoleSkill(4, 9, "MySQL & Relational Database Design", "Database", 3, "High"));
        r4.add(new RoleSkill(4, 7, "RESTful API Design & Integration", "Web Backend", 3, "Medium"));
        r4.add(new RoleSkill(4, 12, "Git & Version Control", "DevOps & Tools", 3, "Medium"));
        ROLE_SKILLS.put(4, r4);

        // Role 5: Frontend Web Developer
        List<RoleSkill> r5 = new ArrayList<>();
        r5.add(new RoleSkill(5, 4, "HTML5, CSS3 & Responsive Design", "Web Frontend", 5, "Critical"));
        r5.add(new RoleSkill(5, 5, "Modern JavaScript (ES6+)", "Web Frontend", 5, "Critical"));
        r5.add(new RoleSkill(5, 7, "RESTful API Design & Integration", "Web Backend", 4, "High"));
        r5.add(new RoleSkill(5, 12, "Git & Version Control", "DevOps & Tools", 4, "High"));
        r5.add(new RoleSkill(5, 3, "Data Structures & Algorithms", "Core CS", 3, "Medium"));
        r5.add(new RoleSkill(5, 20, "Web Security (OWASP Top 10)", "Security", 3, "Medium"));
        ROLE_SKILLS.put(5, r5);

        // Populate required skills in JobRole objects
        for (Map.Entry<Integer, JobRole> entry : ROLES.entrySet()) {
            entry.getValue().setRequiredSkills(ROLE_SKILLS.getOrDefault(entry.getKey(), Collections.emptyList()));
        }
    }

    private static void initRecommendations() {
        addRec(1, 1, "Java Programming Masterclass for Software Developers", "Course", "Coursera / Duke University", "https://www.coursera.org/specializations/java-programming", "40 hours", "Intermediate");
        addRec(2, 1, "Oracle Official Java Documentation & Tutorials", "Documentation", "Oracle Java Docs", "https://docs.oracle.com/javase/tutorial/", "20 hours", "Beginner");
        addRec(3, 1, "Java OOP & Concurrency in Practice", "Hands-on Project", "Hyperskill / JetBrains Academy", "https://hyperskill.org/tracks/1", "25 hours", "Advanced");

        addRec(4, 6, "Java Servlets & JSP Full Course: Build Real Applications", "Course", "freeCodeCamp / YouTube", "https://www.youtube.com/watch?v=b42CJ0r-1to", "18 hours", "Intermediate");
        addRec(5, 6, "Jakarta Servlet Specification & Developer Guide", "Documentation", "Eclipse Jakarta EE", "https://jakarta.ee/specifications/servlet/", "10 hours", "Intermediate");

        addRec(6, 10, "JDBC Complete Tutorial: Connecting Java with Databases", "Course", "Baeldung / Java Guides", "https://www.baeldung.com/java-jdbc", "12 hours", "Intermediate");
        addRec(7, 10, "Building Production-Ready CRUD Apps with MySQL & JDBC", "Hands-on Project", "GitHub Educational Repos", "https://github.com/topics/jdbc-project", "15 hours", "Intermediate");

        addRec(8, 9, "SQL & Relational Database Design Masterclass", "Course", "Khan Academy / Stanford Online", "https://www.khanacademy.org/computing/computer-programming/sql", "20 hours", "Beginner");
        addRec(9, 9, "MySQL 8.0 Reference Manual: Query Optimization & Indexing", "Documentation", "Oracle MySQL Docs", "https://dev.mysql.com/doc/refman/8.0/en/", "15 hours", "Advanced");

        addRec(10, 7, "RESTful API Design Best Practices & JSON Architecture", "Documentation", "RESTfulAPI.net", "https://restfulapi.net/", "8 hours", "Intermediate");
        addRec(11, 4, "Responsive Web Design Certification", "Course", "freeCodeCamp", "https://www.freecodecamp.org/learn/2022/responsive-web-design/", "30 hours", "Beginner");
        addRec(12, 5, "JavaScript: The Modern JavaScript Tutorial", "Documentation", "javascript.info", "https://javascript.info/", "35 hours", "Intermediate");

        addRec(13, 13, "Docker for Beginners: DevOps Containerization", "Course", "Docker Labs & freeCodeCamp", "https://docker-curriculum.com/", "15 hours", "Beginner");
        addRec(14, 15, "AWS Cloud Practitioner & Architecture Foundations", "Course", "AWS Skill Builder", "https://explore.skillbuilder.aws/", "25 hours", "Beginner");
        addRec(15, 20, "OWASP Top 10 Web Application Security Vulnerabilities", "Documentation", "OWASP Foundation", "https://owasp.org/www-project-top-ten/", "12 hours", "Intermediate");
    }

    private static void addRec(int id, int skillId, String title, String type, String provider, String url, String hours, String diff) {
        Skill s = SKILLS.get(skillId);
        String sName = (s != null) ? s.getName() : "Skill";
        Recommendation r = new Recommendation(id, skillId, sName, title, type, provider, url, hours, diff);
        RECOMMENDATIONS.computeIfAbsent(skillId, k -> new ArrayList<>()).add(r);
    }

    public static List<JobRole> getAllRoles() {
        return new ArrayList<>(ROLES.values());
    }

    public static JobRole getRoleById(int id) {
        return ROLES.getOrDefault(id, ROLES.get(1));
    }

    public static List<RoleSkill> getSkillsForRole(int roleId) {
        return ROLE_SKILLS.getOrDefault(roleId, ROLE_SKILLS.get(1));
    }

    public static List<Skill> getAllSkills() {
        return new ArrayList<>(SKILLS.values());
    }

    public static Skill getSkillById(int id) {
        return SKILLS.get(id);
    }

    public static List<Recommendation> getRecommendationsBySkillId(int skillId) {
        return RECOMMENDATIONS.getOrDefault(skillId, Collections.emptyList());
    }

    public static List<Recommendation> getRecommendationsForSkillList(List<Integer> skillIds) {
        List<Recommendation> list = new ArrayList<>();
        if (skillIds != null) {
            for (Integer sid : skillIds) {
                list.addAll(getRecommendationsBySkillId(sid));
            }
        }
        return list;
    }

    public static void saveUserRatings(int userId, Map<Integer, Integer> ratings) {
        if (ratings != null) {
            IN_MEMORY_RATINGS.computeIfAbsent(userId, k -> new ConcurrentHashMap<>()).putAll(ratings);
        }
    }

    public static Map<Integer, Integer> getUserRatings(int userId) {
        return new HashMap<>(IN_MEMORY_RATINGS.getOrDefault(userId, Collections.emptyMap()));
    }
}
