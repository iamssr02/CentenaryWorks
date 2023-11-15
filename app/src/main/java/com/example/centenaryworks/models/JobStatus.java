package com.example.centenaryworks.models;

public class JobStatus {

    private String jobId;
    private String jobTitle;
    private String jobDescription;

    // Required default constructor for Firebase
    public JobStatus() {
    }

    public JobStatus(String jobId, String jobTitle, String jobDescription) {
        this.jobId = jobId;
        this.jobTitle = jobTitle;
        this.jobDescription = jobDescription;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

}

