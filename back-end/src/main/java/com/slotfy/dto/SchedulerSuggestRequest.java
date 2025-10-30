package com.slotfy.dto;

import java.util.List;

/**
 * Request DTO for scheduler suggest endpoint
 */
public class SchedulerSuggestRequest {
    
    private String userId;
    private String timezone;
    private Integer duration; // in minutes
    private Integer buffer; // in minutes
    private List<TimeWindow> availableWindows;
    private List<TimeWindow> busySlots;
    private String preferences;
    private Integer maxSuggestions;
    
    public SchedulerSuggestRequest() {}
    
    // Getters and setters
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getTimezone() {
        return timezone;
    }
    
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
    
    public Integer getDuration() {
        return duration;
    }
    
    public void setDuration(Integer duration) {
        this.duration = duration;
    }
    
    public Integer getBuffer() {
        return buffer;
    }
    
    public void setBuffer(Integer buffer) {
        this.buffer = buffer;
    }
    
    public List<TimeWindow> getAvailableWindows() {
        return availableWindows;
    }
    
    public void setAvailableWindows(List<TimeWindow> availableWindows) {
        this.availableWindows = availableWindows;
    }
    
    public List<TimeWindow> getBusySlots() {
        return busySlots;
    }
    
    public void setBusySlots(List<TimeWindow> busySlots) {
        this.busySlots = busySlots;
    }
    
    public String getPreferences() {
        return preferences;
    }
    
    public void setPreferences(String preferences) {
        this.preferences = preferences;
    }
    
    public Integer getMaxSuggestions() {
        return maxSuggestions != null ? maxSuggestions : 3;
    }
    
    public void setMaxSuggestions(Integer maxSuggestions) {
        this.maxSuggestions = maxSuggestions;
    }
    
    /**
     * Time window with start and end ISO timestamps
     */
    public static class TimeWindow {
        private String start;
        private String end;
        
        public TimeWindow() {}
        
        public TimeWindow(String start, String end) {
            this.start = start;
            this.end = end;
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
}
