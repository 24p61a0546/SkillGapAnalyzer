package com.skillgap.model;

public class Recommendation {
    private int id;
    private int skillId;
    private String skillName;
    private String title;
    private String type;
    private String provider;
    private String url;
    private String estimatedHours;
    private String difficultyLevel;

    public Recommendation() {}

    public Recommendation(int id, int skillId, String skillName, String title, String type, String provider, String url, String estimatedHours, String difficultyLevel) {
        this.id = id;
        this.skillId = skillId;
        this.skillName = skillName;
        this.title = title;
        this.type = type;
        this.provider = provider;
        this.url = url;
        this.estimatedHours = estimatedHours;
        this.difficultyLevel = difficultyLevel;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSkillId() { return skillId; }
    public void setSkillId(int skillId) { this.skillId = skillId; }

    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getEstimatedHours() { return estimatedHours; }
    public void setEstimatedHours(String estimatedHours) { this.estimatedHours = estimatedHours; }

    public String getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(String difficultyLevel) { this.difficultyLevel = difficultyLevel; }
}
