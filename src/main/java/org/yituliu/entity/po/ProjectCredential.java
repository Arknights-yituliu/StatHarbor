package org.yituliu.entity.po;

public class ProjectCredential {
    private Long id;
    private String projectKey;
    private String secretKey;
    private String note;

    public ProjectCredential() {
    }
    
    public ProjectCredential(Long id, String projectKey, String secretKey, String note) {
        this.id = id;
        this.projectKey = projectKey;
        this.secretKey = secretKey;
        this.note = note;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
    

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    
}
