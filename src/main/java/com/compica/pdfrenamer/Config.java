package com.compica.pdfrenamer;

import java.util.List;

public class Config {
    private List<String> projectList;
    private List<String> vendors;

    // Getters and setters

    public List<String> getProjectList() {
        return projectList;
    }

    public void setProjectList(List<String> projectList) {
        this.projectList = projectList;
    }

    public List<String> getVendors() {
        return vendors;
    }

    public void setVendors(List<String> vendors) {
        this.vendors = vendors;
    }
}