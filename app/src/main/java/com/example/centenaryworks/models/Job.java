package com.example.centenaryworks.models;

public class Job {

    private String jobId;
    private String jobTitle;
    private String jobDescription;
    private String officialUid;
    private String numberOfOpenings;
    private String salary;
    private String date;

    // Required default constructor for Firebase
    public Job() {
    }

    public Job(String jobId, String jobTitle, String jobDescription, String officialUid, String numberOfOpenings, String salary, String date) {
        this.jobId = jobId;
        this.jobTitle = jobTitle;
        this.jobDescription = jobDescription;
        this.officialUid = officialUid;
        this.numberOfOpenings = numberOfOpenings;
        this.salary = salary;
        this.date = date;
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

    public String getOfficialUid() {
        return officialUid;
    }

    public void setOfficialUid(String officialUid) {
        this.officialUid = officialUid;
    }

    public String getNumberOfOpenings() {
        return numberOfOpenings;
    }

    public void setNumberOfOpenings(String numberOfOpenings) {
        this.numberOfOpenings = numberOfOpenings;
    }
    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}

