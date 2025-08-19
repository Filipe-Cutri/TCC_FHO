package com.slotify.controller;

import com.slotify.service.AppointmentService;
import com.slotify.service.ProfessionalService;
import com.slotify.service.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Controller for Dashboard data and statistics
 */
@RestController
@RequestMapping("/api/establishment/dashboard")
@CrossOrigin(originPatterns = "*")
public class DashboardController {
    
    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired
    private ProfessionalService professionalService;
    
    @Autowired
    private ServiceService serviceService;
    
    /**
     * Get dashboard overview statistics
     */
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getDashboardOverview(@RequestParam Long establishmentId) {
        try {
            // Appointment statistics
            long totalAppointments = appointmentService.countByEstablishment(establishmentId);
            long todayAppointments = appointmentService.countTodayAppointments(establishmentId);
            long monthlyAppointments = appointmentService.countThisMonthAppointments(establishmentId);
            BigDecimal monthlyRevenue = appointmentService.calculateMonthlyRevenue(establishmentId);
            
            // Professional statistics
            long totalProfessionals = professionalService.countByEstablishment(establishmentId);
            long activeProfessionals = professionalService.countActiveByEstablishment(establishmentId);
            
            // Service statistics
            long totalServices = serviceService.countByEstablishment(establishmentId);
            long activeServices = serviceService.countActiveByEstablishment(establishmentId);
            
            // Calculate performance metrics
            double avgAppointmentsPerDay = monthlyAppointments / 30.0;
            double revenuePerAppointment = monthlyAppointments > 0 ? 
                monthlyRevenue.divide(BigDecimal.valueOf(monthlyAppointments), 2, BigDecimal.ROUND_HALF_UP).doubleValue() : 0.0;
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "data", Map.of(
                        "appointments", Map.of(
                            "total", totalAppointments,
                            "today", todayAppointments,
                            "thisMonth", monthlyAppointments,
                            "avgPerDay", avgAppointmentsPerDay
                        ),
                        "professionals", Map.of(
                            "total", totalProfessionals,
                            "active", activeProfessionals,
                            "inactive", totalProfessionals - activeProfessionals
                        ),
                        "services", Map.of(
                            "total", totalServices,
                            "active", activeServices,
                            "inactive", totalServices - activeServices
                        ),
                        "revenue", Map.of(
                            "monthly", monthlyRevenue,
                            "perAppointment", revenuePerAppointment
                        )
                    )
                ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Erro interno do servidor"
                ));
        }
    }
    
    /**
     * Get today's appointments for dashboard
     */
    @GetMapping("/today-appointments")
    public ResponseEntity<Map<String, Object>> getTodayAppointments(@RequestParam Long establishmentId) {
        try {
            var appointments = appointmentService.getTodayAppointments(establishmentId);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "data", appointments,
                    "count", appointments.size()
                ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Erro interno do servidor"
                ));
        }
    }
    
    /**
     * Get upcoming appointments for dashboard
     */
    @GetMapping("/upcoming-appointments")
    public ResponseEntity<Map<String, Object>> getUpcomingAppointments(@RequestParam Long establishmentId) {
        try {
            var appointments = appointmentService.getUpcomingAppointments(establishmentId);
            
            // Limit to next 10 appointments for dashboard
            var limitedAppointments = appointments.stream().limit(10).toList();
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "data", limitedAppointments,
                    "count", limitedAppointments.size(),
                    "total", appointments.size()
                ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Erro interno do servidor"
                ));
        }
    }
    
    /**
     * Get top rated professionals for dashboard
     */
    @GetMapping("/top-professionals")
    public ResponseEntity<Map<String, Object>> getTopProfessionals(@RequestParam Long establishmentId) {
        try {
            var professionals = professionalService.getTopRatedProfessionals(establishmentId, new BigDecimal("4.0"));
            
            // Limit to top 5 for dashboard
            var topProfessionals = professionals.stream().limit(5).toList();
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "data", topProfessionals,
                    "count", topProfessionals.size()
                ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Erro interno do servidor"
                ));
        }
    }
    
    /**
     * Get professional performance statistics
     */
    @GetMapping("/professional-performance")
    public ResponseEntity<Map<String, Object>> getProfessionalPerformance(@RequestParam Long establishmentId) {
        try {
            List<Object[]> performanceData = appointmentService.getProfessionalPerformanceStats(establishmentId);
            
            // Transform the data for frontend consumption
            var performanceStats = performanceData.stream()
                .limit(10) // Top 10 professionals
                .map(row -> Map.of(
                    "professionalId", row[0],
                    "professionalName", row[1],
                    "appointmentsCount", row[2],
                    "revenue", row[3]
                ))
                .toList();
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "data", performanceStats,
                    "count", performanceStats.size()
                ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Erro interno do servidor"
                ));
        }
    }
    
    /**
     * Get service categories and their popularity
     */
    @GetMapping("/service-categories")
    public ResponseEntity<Map<String, Object>> getServiceCategories(@RequestParam Long establishmentId) {
        try {
            var categories = serviceService.getCategories(establishmentId);
            
            // Get count of services per category
            var categoryData = categories.stream()
                .map(category -> {
                    var servicesInCategory = serviceService.getByCategory(establishmentId, category);
                    return Map.of(
                        "category", category,
                        "servicesCount", servicesInCategory.size()
                    );
                })
                .toList();
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "data", categoryData,
                    "count", categoryData.size()
                ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Erro interno do servidor"
                ));
        }
    }
    
    /**
     * Get quick actions data for dashboard
     */
    @GetMapping("/quick-actions")
    public ResponseEntity<Map<String, Object>> getQuickActions(@RequestParam Long establishmentId) {
        try {
            long pendingAppointments = appointmentService.getByEstablishmentAndStatus(
                establishmentId, 
                com.slotify.model.AppointmentStatus.SCHEDULED
            ).size();
            
            long activeProfessionals = professionalService.countActiveByEstablishment(establishmentId);
            long activeServices = serviceService.countActiveByEstablishment(establishmentId);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "data", Map.of(
                        "pendingAppointments", pendingAppointments,
                        "activeProfessionals", activeProfessionals,
                        "activeServices", activeServices,
                        "needsAttention", pendingAppointments > 0
                    )
                ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Erro interno do servidor"
                ));
        }
    }
    
    /**
     * Get monthly trends data
     */
    @GetMapping("/monthly-trends")
    public ResponseEntity<Map<String, Object>> getMonthlyTrends(@RequestParam Long establishmentId) {
        try {
            // For now, return basic monthly data
            // In a real implementation, you'd calculate trends over multiple months
            long monthlyAppointments = appointmentService.countThisMonthAppointments(establishmentId);
            BigDecimal monthlyRevenue = appointmentService.calculateMonthlyRevenue(establishmentId);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "data", Map.of(
                        "thisMonth", Map.of(
                            "appointments", monthlyAppointments,
                            "revenue", monthlyRevenue
                        ),
                        "trend", Map.of(
                            "appointmentsTrend", "up", // This would be calculated based on previous months
                            "revenueTrend", "up",
                            "trendPercentage", 15.2 // Example percentage
                        )
                    )
                ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Erro interno do servidor"
                ));
        }
    }
}