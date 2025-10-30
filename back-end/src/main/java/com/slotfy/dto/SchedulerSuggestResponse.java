package com.slotfy.dto;

import java.util.List;

/**
 * Response DTO for scheduler suggest endpoint
 */
public class SchedulerSuggestResponse {
    
    private List<Suggestion> suggestions;
    
    public SchedulerSuggestResponse() {}
    
    public SchedulerSuggestResponse(List<Suggestion> suggestions) {
        this.suggestions = suggestions;
    }
    
    public List<Suggestion> getSuggestions() {
        return suggestions;
    }
    
    public void setSuggestions(List<Suggestion> suggestions) {
        this.suggestions = suggestions;
    }
    
    /**
     * Individual time slot suggestion
     */
    public static class Suggestion {
        private String start;
        private String end;
        private String reason;
        private Double score;
        
        public Suggestion() {}
        
        public Suggestion(String start, String end, String reason, Double score) {
            this.start = start;
            this.end = end;
            this.reason = reason;
            this.score = score;
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
        
        public String getReason() {
            return reason;
        }
        
        public void setReason(String reason) {
            this.reason = reason;
        }
        
        public Double getScore() {
            return score;
        }
        
        public void setScore(Double score) {
            this.score = score;
        }
    }
}
