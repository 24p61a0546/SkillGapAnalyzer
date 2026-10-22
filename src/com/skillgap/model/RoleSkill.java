package com.skillgap.model;

public class RoleSkill {
    private int roleId;
    private int skillId;
    private String skillName;
    private String category;
    private int requiredProficiency;
    private String importance;

    public RoleSkill() {}

    public RoleSkill(int roleId, int skillId, String skillName, String category, int requiredProficiency, String importance) {
        this.roleId = roleId;
        this.skillId = skillId;
        this.skillName = skillName;
        this.category = category;
        this.requiredProficiency = requiredProficiency;
        this.importance = importance;
    }

    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) { this.roleId = roleId; }

    public int getSkillId() { return skillId; }
    public void setSkillId(int skillId) { this.skillId = skillId; }

    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getRequiredProficiency() { return requiredProficiency; }
    public void setRequiredProficiency(int requiredProficiency) { this.requiredProficiency = requiredProficiency; }

    public String getImportance() { return importance; }
    public void setImportance(String importance) { this.importance = importance; }
}
