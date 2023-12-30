package com.compica.pdfrenamer;

import java.util.List;

public class Config {
    private List<String> projectList;
    private List<String> supplierList;
    private String sourceFolder;

    // Getters and setters

    public List<String> getProjectList() {
        return projectList;
    }

    public void setProjectList(List<String> projectList) {
        this.projectList = projectList;
    }

    public String getSourceFolder() {
        return sourceFolder;
    }

    public void setSourceFolder(String sourceFolder) {
        this.sourceFolder = sourceFolder;
    }

    public List<String> getSupplierList() {
        return supplierList;
    }

    public void setSupplierList(List<String> supplierList) {
        this.supplierList = supplierList;
    }

}