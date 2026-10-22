package com.skillgap.model;

import java.util.ArrayList;
import java.util.List;

public class JobRole {
    private int id;
    private String title;
    private String slug;
    private String category;
    private String description;
    private String icon;
    private String avgSalary;
    private String demandLevel;
    private String experienceLevel;
    private List<RoleSkill> requiredSkills = new ArrayList<>();

    public JobRole() {}

    public JobRole(int id, String title, String slug, String category, String description, String icon, String avgSalary, String demandLevel, String experienceLevel) {
        this.id = id;
        this.title = title;
        this.slug = slug;
        this.category = category;
        this.description = description;
        this.icon = icon;
        this.avgSalary = avgSalary;
        this.demandLevel = demandLevel;
        this.experienceLevel = experienceLevel;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getAvgSalary() { return avgSalary; }
    public void setAvgSalary(String avgSalary) { this.avgSalary = avgSalary; }

    public String getDemandLevel() { return demandLevel; }
    public void setDemandLevel(String demandLevel) { this.demandLevel = demandLevel; }

    public String getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(String experienceLevel) { this.experienceLevel = experienceLevel; }

    public List<RoleSkill> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(List<RoleSkill> requiredSkills) { this.requiredSkills = requiredSkills; }
}
