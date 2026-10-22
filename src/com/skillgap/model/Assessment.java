package com.skillgap.model;

import java.sql.Timestamp;

public class Assessment {
    private int id;
    private int userId;
    private int skillId;
    private String skillName;
    private String category;
    private int proficiencyLevel;
    private String notes;
    private Timestamp assessedAt;

    public Assessment() {}

    public Assessment(int id, int userId, int skillId, String skillName, String category, int proficiencyLevel, String notes, Timestamp assessedAt) {
        this.id = id;
        this.userId = userId;
        this.skillId = skillId;
        this.skillName = skillName;
        this.category = category;
        this.proficiencyLevel = proficiencyLevel;
        this.notes = notes;
        this.assessedAt = assessedAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getSkillId() { return skillId; }
    public void setSkillId(int skillId) { this.skillId = skillId; }

    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getProficiencyLevel() { return proficiencyLevel; }
    public void setProficiencyLevel(int proficiencyLevel) { this.proficiencyLevel = proficiencyLevel; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Timestamp getAssessedAt() { return assessedAt; }
    public void setAssessedAt(Timestamp assessedAt) { this.assessedAt = assessedAt; }
}
