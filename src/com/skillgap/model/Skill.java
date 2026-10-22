package com.skillgap.model;

public class Skill {
    private int id;
    private String name;
    private String category;
    private String description;
    private int maxRating = 5;

    public Skill() {}

    public Skill(int id, String name, String category, String description, int maxRating) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.maxRating = maxRating;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getMaxRating() { return maxRating; }
    public void setMaxRating(int maxRating) { this.maxRating = maxRating; }
}
