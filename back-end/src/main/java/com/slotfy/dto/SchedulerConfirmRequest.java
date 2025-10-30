package com.slotfy.dto;

/**
 * Request DTO for scheduler confirm endpoint
 */
public class SchedulerConfirmRequest {
    
    private String userId;
    private String start;
    private String end;
    
    public SchedulerConfirmRequest() {}
    
    public SchedulerConfirmRequest(String userId, String start, String end) {
        this.userId = userId;
        this.start = start;
        this.end = end;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getStart() {
        return start;
    }
    
    public void setStart(String start) {
        this.start = start;
    }
    
    public String getEnd() {
        return end;
    }
    
    public void setEnd(String end) {
        this.end = end;
    }
}
