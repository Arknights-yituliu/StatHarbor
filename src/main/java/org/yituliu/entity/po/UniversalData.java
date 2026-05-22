package org.yituliu.entity.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

@TableName("universal_data")
public class UniversalData {
    @TableId
    private Long id;
    private String projectKey;
    private String category;
    private String version;
    private String source;
    private Date capturedAt;
    private Date receivedAt;
    private Long payloadSizeBytes;
    private String note;
    private String payload;

    public UniversalData() {
    }

    public UniversalData(Long id, String projectKey, String category, String version, String source,
                         Date capturedAt, Date receivedAt, Long payloadSizeBytes, String note, String payload) {
        this.id = id;
        this.projectKey = projectKey;
        this.category = category;
        this.version = version;
        this.source = source;
        this.capturedAt = capturedAt;
        this.receivedAt = receivedAt;
        this.payloadSizeBytes = payloadSizeBytes;
        this.note = note;
        this.payload = payload;
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

    public Date getCapturedAt() {
        return capturedAt;
    }

    public void setCapturedAt(Date capturedAt) {
        this.capturedAt = capturedAt;
    }

    public Date getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Date receivedAt) {
        this.receivedAt = receivedAt;
    }

    public Long getPayloadSizeBytes() {
        return payloadSizeBytes;
    }

    public void setPayloadSizeBytes(Long payloadSizeBytes) {
        this.payloadSizeBytes = payloadSizeBytes;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "UniversalData{" +
                "id=" + id +
                ", projectKey='" + projectKey + '\'' +
                ", category='" + category + '\'' +
                ", version='" + version + '\'' +
                ", source='" + source + '\'' +
                ", capturedAt=" + capturedAt +
                ", receivedAt=" + receivedAt +
                ", payloadSizeBytes=" + payloadSizeBytes +
                ", note='" + note + '\'' +
                ", payload='" + payload + '\'' +
                '}';
    }
}
