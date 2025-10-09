package com.slotfy.service;

import com.slotfy.dto.AIRecommendationRequest;
import com.slotfy.dto.AIRecommendationResponse;
import com.slotfy.model.*;
import com.slotfy.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for AI-powered scheduling recommendations
 * This service analyzes client preferences, professional availability, and historical data
 * to recommend the best appointment slots
 */
@Service
public class AISchedulingService {
    
    @Autowired
    private ServiceService serviceService;
    
    @Autowired
    private ProfessionalService professionalService;
    
    @Autowired
    private EstablishmentService establishmentService;
    
    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    /**
     * Generate AI recommendations for a client
     */
    public List<AIRecommendationResponse> generateRecommendations(AIRecommendationRequest request) {
        List<AIRecommendationResponse> recommendations = new ArrayList<>();
        
        // Get active services for the establishment
        List<com.slotfy.model.Service> services = serviceService.getActiveByEstablishmentId(request.getEstablishmentId());
        
        if (services.isEmpty()) {
            return recommendations;
        }
        
        // Get active professionals for the establishment
        List<Professional> professionals = professionalService.getActiveByEstablishmentId(request.getEstablishmentId());
        
        if (professionals.isEmpty()) {
            return recommendations;
        }
        
        // Get establishment details
        Establishment establishment = establishmentService.findById(request.getEstablishmentId())
            .orElse(null);
        
        if (establishment == null) {
            return recommendations;
        }
        
        // Get client's appointment history to understand preferences
        List<Appointment> clientHistory = appointmentService.getByClient(request.getClientId());
        
        // Analyze preferences and generate recommendations
        recommendations = analyzeAndRecommend(
            services, 
            professionals, 
            establishment, 
            clientHistory, 
            request.getPreferences(),
            request.getClientId()
        );
        
        return recommendations;
    }
    
    /**
     * Analyze data and create recommendations
     */
    private List<AIRecommendationResponse> analyzeAndRecommend(
            List<com.slotfy.model.Service> services,
            List<Professional> professionals,
            Establishment establishment,
            List<Appointment> clientHistory,
            AIRecommendationRequest.ClientPreferences preferences,
            Long clientId) {
        
        List<AIRecommendationResponse> recommendations = new ArrayList<>();
        
        // Sort professionals by rating and satisfaction
        List<Professional> topProfessionals = professionals.stream()
            .sorted(Comparator.comparing(Professional::getRating).reversed()
                    .thenComparing(Professional::getSatisfactionRate).reversed())
            .limit(3) // Top 3 professionals
            .collect(Collectors.toList());
        
        // Analyze client's service history to predict preferred services
        List<com.slotfy.model.Service> preferredServices = analyzeServicePreferences(services, clientHistory, preferences);
        
        // Generate time recommendations based on preferences
        List<LocalDateTime> recommendedTimes = generateRecommendedTimes(preferences);
        
        // Create recommendations combining preferred services, top professionals, and optimal times
        int recommendationId = 1;
        for (com.slotfy.model.Service service : preferredServices.subList(0, Math.min(3, preferredServices.size()))) {
            for (Professional professional : topProfessionals.subList(0, Math.min(2, topProfessionals.size()))) {
                for (LocalDateTime dateTime : recommendedTimes.subList(0, Math.min(2, recommendedTimes.size()))) {
                    
                    // Check if slot is available
                    LocalDateTime endTime = dateTime.plusMinutes(service.getDurationMinutes());
                    boolean available = appointmentService.isTimeSlotAvailable(professional.getId(), dateTime, endTime);
                    
                    if (available) {
                        // Calculate confidence score
                        int confidence = calculateConfidenceScore(service, professional, dateTime, clientHistory, preferences);
                        
                        // Generate reasoning
                        String reason = generateReason(service, professional, dateTime, clientHistory, preferences, confidence);
                        
                        AIRecommendationResponse recommendation = new AIRecommendationResponse(
                            (long) recommendationId++,
                            service.getName(),
                            service.getId(),
                            professional.getName(),
                            professional.getId(),
                            establishment.getName(),
                            establishment.getId(),
                            dateTime,
                            dateTime.format(TIME_FORMATTER),
                            service.getPrice(),
                            confidence,
                            reason
                        );
                        
                        recommendations.add(recommendation);
                        
                        // Limit to top 5 recommendations
                        if (recommendations.size() >= 5) {
                            break;
                        }
                    }
                }
                if (recommendations.size() >= 5) break;
            }
            if (recommendations.size() >= 5) break;
        }
        
        // Sort by confidence score
        recommendations.sort(Comparator.comparing(AIRecommendationResponse::getConfidence).reversed());
        
        return recommendations;
    }
    
    /**
     * Analyze service preferences based on client history
     */
    private List<com.slotfy.model.Service> analyzeServicePreferences(
            List<com.slotfy.model.Service> availableServices,
            List<Appointment> clientHistory,
            AIRecommendationRequest.ClientPreferences preferences) {
        
        // If client has history, prioritize similar services
        if (clientHistory != null && !clientHistory.isEmpty()) {
            // Get most recent service IDs
            List<Long> recentServiceIds = clientHistory.stream()
                .sorted(Comparator.comparing(Appointment::getAppointmentDateTime).reversed())
                .map(Appointment::getServiceId)
                .distinct()
                .limit(3)
                .collect(Collectors.toList());
            
            // Prioritize these services
            List<com.slotfy.model.Service> prioritized = availableServices.stream()
                .filter(s -> recentServiceIds.contains(s.getId()))
                .collect(Collectors.toList());
            
            // Add remaining services
            availableServices.stream()
                .filter(s -> !recentServiceIds.contains(s.getId()))
                .forEach(prioritized::add);
            
            return prioritized;
        }
        
        // If no history, prioritize by price based on budget preference
        if (preferences != null && preferences.getBudget() != null) {
            String budget = preferences.getBudget().toLowerCase();
            
            if ("low".equals(budget)) {
                return availableServices.stream()
                    .sorted(Comparator.comparing(com.slotfy.model.Service::getPrice))
                    .collect(Collectors.toList());
            } else if ("high".equals(budget)) {
                return availableServices.stream()
                    .sorted(Comparator.comparing(com.slotfy.model.Service::getPrice).reversed())
                    .collect(Collectors.toList());
            }
        }
        
        // Default: sort by popularity (could be enhanced with actual booking counts)
        return new ArrayList<>(availableServices);
    }
    
    /**
     * Generate recommended appointment times based on preferences
     */
    private List<LocalDateTime> generateRecommendedTimes(AIRecommendationRequest.ClientPreferences preferences) {
        List<LocalDateTime> times = new ArrayList<>();
        
        // Start from tomorrow
        LocalDateTime baseDate = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
        
        // Determine preferred hours based on time preferences
        List<Integer> preferredHours = new ArrayList<>();
        
        if (preferences != null && preferences.getPreferredTimes() != null) {
            for (String timePreference : preferences.getPreferredTimes()) {
                if ("morning".equalsIgnoreCase(timePreference)) {
                    preferredHours.addAll(List.of(9, 10, 11));
                } else if ("afternoon".equalsIgnoreCase(timePreference)) {
                    preferredHours.addAll(List.of(14, 15, 16));
                } else if ("evening".equalsIgnoreCase(timePreference)) {
                    preferredHours.addAll(List.of(17, 18));
                }
            }
        }
        
        // If no preferences, use afternoon as default
        if (preferredHours.isEmpty()) {
            preferredHours.addAll(List.of(14, 15, 16));
        }
        
        // Generate times for the next 7 days
        for (int day = 0; day < 7; day++) {
            LocalDateTime currentDate = baseDate.plusDays(day);
            
            // Skip weekends (optional - can be configurable)
            if (currentDate.getDayOfWeek().getValue() >= 6) {
                continue;
            }
            
            for (Integer hour : preferredHours) {
                times.add(currentDate.withHour(hour).withMinute(0));
                times.add(currentDate.withHour(hour).withMinute(30));
            }
        }
        
        return times;
    }
    
    /**
     * Calculate confidence score for a recommendation
     */
    private int calculateConfidenceScore(
            com.slotfy.model.Service service,
            Professional professional,
            LocalDateTime dateTime,
            List<Appointment> clientHistory,
            AIRecommendationRequest.ClientPreferences preferences) {
        
        int score = 50; // Base score
        
        // Professional rating boost (up to +25 points)
        if (professional.getRating() != null) {
            score += (int) (professional.getRating().doubleValue() * 5);
        }
        
        // Client history match (up to +20 points)
        if (clientHistory != null && !clientHistory.isEmpty()) {
            boolean hasBookedThisService = clientHistory.stream()
                .anyMatch(a -> service.getId().equals(a.getServiceId()));
            
            if (hasBookedThisService) {
                score += 20;
            }
        }
        
        // Time preference match (up to +15 points)
        if (preferences != null && preferences.getPreferredTimes() != null) {
            int hour = dateTime.getHour();
            for (String timePreference : preferences.getPreferredTimes()) {
                if ("morning".equalsIgnoreCase(timePreference) && hour >= 9 && hour < 12) {
                    score += 15;
                    break;
                } else if ("afternoon".equalsIgnoreCase(timePreference) && hour >= 14 && hour < 17) {
                    score += 15;
                    break;
                } else if ("evening".equalsIgnoreCase(timePreference) && hour >= 17 && hour < 19) {
                    score += 15;
                    break;
                }
            }
        }
        
        // Professional satisfaction rate boost (up to +10 points)
        if (professional.getSatisfactionRate() != null) {
            score += (int) (professional.getSatisfactionRate().doubleValue() / 10);
        }
        
        // Ensure score is between 0 and 100
        return Math.min(100, Math.max(0, score));
    }
    
    /**
     * Generate explanation for recommendation
     */
    private String generateReason(
            com.slotfy.model.Service service,
            Professional professional,
            LocalDateTime dateTime,
            List<Appointment> clientHistory,
            AIRecommendationRequest.ClientPreferences preferences,
            int confidence) {
        
        List<String> reasons = new ArrayList<>();
        
        // Check if client has history with this service
        if (clientHistory != null && !clientHistory.isEmpty()) {
            boolean hasBookedThisService = clientHistory.stream()
                .anyMatch(a -> service.getId().equals(a.getServiceId()));
            
            if (hasBookedThisService) {
                reasons.add("você já utilizou este serviço antes");
            }
        }
        
        // Professional rating
        if (professional.getRating() != null && professional.getRating().compareTo(new BigDecimal("4.0")) >= 0) {
            reasons.add("profissional com excelente avaliação (" + professional.getRating() + "/5.0)");
        }
        
        // Time preference match
        if (preferences != null && preferences.getPreferredTimes() != null) {
            int hour = dateTime.getHour();
            for (String timePreference : preferences.getPreferredTimes()) {
                if ("morning".equalsIgnoreCase(timePreference) && hour >= 9 && hour < 12) {
                    reasons.add("horário matinal conforme sua preferência");
                    break;
                } else if ("afternoon".equalsIgnoreCase(timePreference) && hour >= 14 && hour < 17) {
                    reasons.add("horário vespertino conforme sua preferência");
                    break;
                } else if ("evening".equalsIgnoreCase(timePreference) && hour >= 17 && hour < 19) {
                    reasons.add("horário noturno conforme sua preferência");
                    break;
                }
            }
        }
        
        // Budget match
        if (preferences != null && preferences.getBudget() != null) {
            String budget = preferences.getBudget().toLowerCase();
            if ("low".equals(budget) && service.getPrice().compareTo(new BigDecimal("50")) <= 0) {
                reasons.add("opção econômica");
            } else if ("high".equals(budget) && service.getPrice().compareTo(new BigDecimal("100")) >= 0) {
                reasons.add("serviço premium");
            }
        }
        
        // Default reason
        if (reasons.isEmpty()) {
            reasons.add("disponibilidade confirmada e profissional qualificado");
        }
        
        return String.join(", ", reasons);
    }
}
