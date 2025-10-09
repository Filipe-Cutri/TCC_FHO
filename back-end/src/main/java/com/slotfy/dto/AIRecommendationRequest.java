package com.slotfy.dto;

/**
 * Request DTO for AI scheduling recommendations
 */
public class AIRecommendationRequest {
    
    private Long clientId;
    private Long establishmentId;
    private ClientPreferences preferences;
    
    public AIRecommendationRequest() {}
    
    public AIRecommendationRequest(Long clientId, Long establishmentId, ClientPreferences preferences) {
        this.clientId = clientId;
        this.establishmentId = establishmentId;
        this.preferences = preferences;
    }
    
    // Getters and setters
    public Long getClientId() {
        return clientId;
    }
    
    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }
    
    public Long getEstablishmentId() {
        return establishmentId;
    }
    
    public void setEstablishmentId(Long establishmentId) {
        this.establishmentId = establishmentId;
    }
    
    public ClientPreferences getPreferences() {
        return preferences;
    }
    
    public void setPreferences(ClientPreferences preferences) {
        this.preferences = preferences;
    }
    
    /**
     * Client preferences for AI recommendations
     */
    public static class ClientPreferences {
        private String[] preferredTimes; // e.g., ["morning", "afternoon", "evening"]
        private String budget; // e.g., "low", "medium", "high"
        private ServiceHistory[] serviceHistory;
        
        public ClientPreferences() {}
        
        public String[] getPreferredTimes() {
            return preferredTimes;
        }
        
        public void setPreferredTimes(String[] preferredTimes) {
            this.preferredTimes = preferredTimes;
        }
        
        public String getBudget() {
            return budget;
        }
        
        public void setBudget(String budget) {
            this.budget = budget;
        }
        
        public ServiceHistory[] getServiceHistory() {
            return serviceHistory;
        }
        
        public void setServiceHistory(ServiceHistory[] serviceHistory) {
            this.serviceHistory = serviceHistory;
        }
    }
    
    /**
     * Service history entry
     */
    public static class ServiceHistory {
        private String serviceName;
        private String category;
        private String date;
        
        public ServiceHistory() {}
        
        public String getServiceName() {
            return serviceName;
        }
        
        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }
        
        public String getCategory() {
            return category;
        }
        
        public void setCategory(String category) {
            this.category = category;
        }
        
        public String getDate() {
            return date;
        }
        
        public void setDate(String date) {
            this.date = date;
        }
    }
}
