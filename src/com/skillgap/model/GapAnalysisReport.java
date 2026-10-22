package com.skillgap.model;

import java.util.ArrayList;
import java.util.List;

public class GapAnalysisReport {
    private int roleId;
    private String roleTitle;
    private String roleCategory;
    private double readinessPercentage;
    private int totalSkillsRequired;
    private int skillsMetCount;
    private int skillsWithGapCount;
    private int criticalGapCount;
    private String readinessLevel; // "High Readiness", "Moderate Readiness", "Foundational Phase"
    private List<SkillGapResult> skillGaps = new ArrayList<>();

    public GapAnalysisReport() {}

    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) { this.roleId = roleId; }

    public String getRoleTitle() { return roleTitle; }
    public void setRoleTitle(String roleTitle) { this.roleTitle = roleTitle; }

    public String getRoleCategory() { return roleCategory; }
    public void setRoleCategory(String roleCategory) { this.roleCategory = roleCategory; }

    public double getReadinessPercentage() { return readinessPercentage; }
    public void setReadinessPercentage(double readinessPercentage) { this.readinessPercentage = readinessPercentage; }

    public int getTotalSkillsRequired() { return totalSkillsRequired; }
    public void setTotalSkillsRequired(int totalSkillsRequired) { this.totalSkillsRequired = totalSkillsRequired; }

    public int getSkillsMetCount() { return skillsMetCount; }
    public void setSkillsMetCount(int skillsMetCount) { this.skillsMetCount = skillsMetCount; }

    public int getSkillsWithGapCount() { return skillsWithGapCount; }
    public void setSkillsWithGapCount(int skillsWithGapCount) { this.skillsWithGapCount = skillsWithGapCount; }

    public int getCriticalGapCount() { return criticalGapCount; }
    public void setCriticalGapCount(int criticalGapCount) { this.criticalGapCount = criticalGapCount; }

    public String getReadinessLevel() { return readinessLevel; }
    public void setReadinessLevel(String readinessLevel) { this.readinessLevel = readinessLevel; }

    public List<SkillGapResult> getSkillGaps() { return skillGaps; }
    public void setSkillGaps(List<SkillGapResult> skillGaps) { this.skillGaps = skillGaps; }
}
