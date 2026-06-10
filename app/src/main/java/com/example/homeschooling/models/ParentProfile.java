package com.example.homeschooling.models;

import java.util.ArrayList;
import java.util.List;

public class ParentProfile {

    private List<Child> children;

    public ParentProfile() {
        this.children = new ArrayList<>();
    }

    public ParentProfile(List<Child> children) {
        this.children = children;
    }

    public List<Child> getChildren() {
        if (children == null) children = new ArrayList<>();
        return children;
    }

    public void setChildren(List<Child> children) {
        this.children = children;
    }
}
