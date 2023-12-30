package com.compica.pdfrenamer;

import java.util.Collections;
import java.util.List;

public class Config {
    private List<String> projectList;
    private List<String> supplierList;
    private List<String> documentTypeList;
    private String sourceFolder;

    // Getters and setters

    public List<String> getProjectList() {
        synchronized (projectList) {
            Collections.sort(projectList);
            return projectList;
        }
    }

    public void setProjectList(List<String> projectList) {
        synchronized (projectList) {
            Collections.sort(projectList);
            this.projectList = projectList;
        }

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

    public List<String> getDocumentTypeList() {
        return documentTypeList;
    }

    public void setDocumentTypeList(List<String> documentTypeList) {
        this.documentTypeList = documentTypeList;
    }
}