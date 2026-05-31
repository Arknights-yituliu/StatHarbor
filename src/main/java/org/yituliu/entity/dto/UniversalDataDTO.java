package org.yituliu.entity.dto;



/**
 * 通用数据传输对象
 * 用于前端请求和响应
 */
public class UniversalDataDTO {
   
    private String projectKey;
    private String category;
    private String version;
    private String source;
    private String capturedAt;
    private Object payload;
    private String note;

    public UniversalDataDTO() {
    }

    public UniversalDataDTO(String projectKey, String category, String version, String source, String capturedAt, Object payload, String note) {
        this.projectKey = projectKey;
        this.category = category;
        this.version = version;
        this.source = source;
        this.capturedAt = capturedAt;
        this.payload = payload;
        this.note = note;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getCapturedAt() {
        return capturedAt;
    }

    public void setCapturedAt(String capturedAt) {
        this.capturedAt = capturedAt;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public String toString() {
        return "UniversalDataDTO{" +
                "projectKey='" + projectKey + '\'' +
                ", category='" + category + '\'' +
                ", version='" + version + '\'' +
                ", source='" + source + '\'' +
                ", capturedAt='" + capturedAt + '\'' +
                ", payload='" + payload + '\'' +
                ", note='" + note + '\'' +
                '}';
    }
}
