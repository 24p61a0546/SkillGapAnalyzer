package com.skillgap.model;

import java.util.ArrayList;
import java.util.List;

public class SkillGapResult {
    private int skillId;
    private String skillName;
    private String category;
    private int requiredProficiency;
    private int currentProficiency;
    private int gap; // max(0, required - current)
    private String status; // "Mastered", "Target Met", "Minor Gap", "Critical Gap"
    private String importance; // "Critical", "High", "Medium"
    private List<Recommendation> recommendations = new ArrayList<>();

    public SkillGapResult() {}

    public SkillGapResult(int skillId, String skillName, String category, int requiredProficiency, int currentProficiency, String importance) {
        this.skillId = skillId;
        this.skillName = skillName;
        this.category = category;
        this.requiredProficiency = requiredProficiency;
        this.currentProficiency = currentProficiency;
        this.importance = importance;
        this.gap = Math.max(0, requiredProficiency - currentProficiency);

        if (currentProficiency >= requiredProficiency) {
            this.status = (currentProficiency > requiredProficiency) ? "Exceeds Requirement" : "Target Met";
        } else if (gap == 1) {
            this.status = "Minor Gap";
        } else {
            this.status = "Critical Gap";
        }
    }

    public int getSkillId() { return skillId; }
    public void setSkillId(int skillId) { this.skillId = skillId; }

    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getRequiredProficiency() { return requiredProficiency; }
    public void setRequiredProficiency(int requiredProficiency) { this.requiredProficiency = requiredProficiency; }

    public int getCurrentProficiency() { return currentProficiency; }
    public void setCurrentProficiency(int currentProficiency) { this.currentProficiency = currentProficiency; }

    public int getGap() { return gap; }
    public void setGap(int gap) { this.gap = gap; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getImportance() { return importance; }
    public void setImportance(String importance) { this.importance = importance; }

    public List<Recommendation> getRecommendations() { return recommendations; }
    public void setRecommendations(List<Recommendation> recommendations) { this.recommendations = recommendations; }
}
