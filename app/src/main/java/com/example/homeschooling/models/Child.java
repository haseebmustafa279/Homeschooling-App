package com.example.homeschooling.models;

public class Child {
    private String name;
    private String childClass;
    private String subjectsNeeded;
    private String budget;

    public Child() {}

    public Child(String name, String childClass, String subjectsNeeded, String budget) {
        this.name = name;
        this.childClass = childClass;
        this.subjectsNeeded = subjectsNeeded;
        this.budget = budget;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getChildClass() { return childClass; }
    public void setChildClass(String childClass) { this.childClass = childClass; }

    public String getSubjectsNeeded() { return subjectsNeeded; }
    public void setSubjectsNeeded(String subjectsNeeded) { this.subjectsNeeded = subjectsNeeded; }

    public String getBudget() { return budget; }
    public void setBudget(String budget) { this.budget = budget; }
}
