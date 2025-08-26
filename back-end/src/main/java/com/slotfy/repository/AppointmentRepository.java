package com.slotfy.repository;

import com.slotfy.model.Appointment;
import com.slotfy.model.AppointmentStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Appointment entity
 */
@Repository
public interface AppointmentRepository extends BaseRepository<Appointment, Long> {
    
    /**
     * Find all appointments by establishment ID
     */
    List<Appointment> findByEstablishmentIdOrderByAppointmentDateTimeDesc(Long establishmentId);
    
    /**
     * Find appointments by establishment and status
     */
    List<Appointment> findByEstablishmentIdAndStatusOrderByAppointmentDateTimeAsc(Long establishmentId, AppointmentStatus status);
    
    /**
     * Find appointments by professional ID
     */
    List<Appointment> findByProfessionalIdOrderByAppointmentDateTimeAsc(Long professionalId);
    
    /**
     * Find appointments by client ID
     */
    List<Appointment> findByClientIdOrderByAppointmentDateTimeDesc(Long clientId);
    
    /**
     * Find appointments by service ID
     */
    List<Appointment> findByServiceIdOrderByAppointmentDateTimeDesc(Long serviceId);
    
    /**
     * Find appointments for a specific date range
     */
    @Query("SELECT a FROM Appointment a WHERE a.establishmentId = :establishmentId AND a.appointmentDateTime BETWEEN :startDate AND :endDate ORDER BY a.appointmentDateTime ASC")
    List<Appointment> findByEstablishmentIdAndDateRange(@Param("establishmentId") Long establishmentId, 
                                                       @Param("startDate") LocalDateTime startDate, 
                                                       @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find appointments for today
     */
    @Query("SELECT a FROM Appointment a WHERE a.establishmentId = :establishmentId AND a.appointmentDateTime >= :startOfDay AND a.appointmentDateTime < :endOfDay ORDER BY a.appointmentDateTime ASC")
    List<Appointment> findTodayAppointments(@Param("establishmentId") Long establishmentId, 
                                          @Param("startOfDay") LocalDateTime startOfDay,
                                          @Param("endOfDay") LocalDateTime endOfDay);
    
    /**
     * Find upcoming appointments
     */
    @Query("SELECT a FROM Appointment a WHERE a.establishmentId = :establishmentId AND a.appointmentDateTime > :currentDateTime AND a.status NOT IN ('CANCELLED', 'COMPLETED') ORDER BY a.appointmentDateTime ASC")
    List<Appointment> findUpcomingAppointments(@Param("establishmentId") Long establishmentId, @Param("currentDateTime") LocalDateTime currentDateTime);
    
    /**
     * Find potentially conflicting appointments for a professional (to be filtered in service layer)
     */
    @Query("SELECT a FROM Appointment a WHERE a.professionalId = :professionalId AND a.status NOT IN ('CANCELLED') AND " +
           "a.appointmentDateTime < :endTime AND a.appointmentDateTime >= :dayStart")
    List<Appointment> findPotentialConflictingAppointments(@Param("professionalId") Long professionalId, 
                                                          @Param("dayStart") LocalDateTime dayStart,
                                                          @Param("endTime") LocalDateTime endTime);
    
    /**
     * Count appointments by establishment
     */
    long countByEstablishmentId(Long establishmentId);
    
    /**
     * Count appointments by status
     */
    long countByEstablishmentIdAndStatus(Long establishmentId, AppointmentStatus status);
    
    /**
     * Count appointments for today
     */
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.establishmentId = :establishmentId AND a.appointmentDateTime >= :startOfDay AND a.appointmentDateTime < :endOfDay")
    long countTodayAppointments(@Param("establishmentId") Long establishmentId,
                               @Param("startOfDay") LocalDateTime startOfDay,
                               @Param("endOfDay") LocalDateTime endOfDay);
    
    /**
     * Count appointments this month
     */
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.establishmentId = :establishmentId AND a.appointmentDateTime >= :startOfMonth AND a.appointmentDateTime < :endOfMonth")
    long countThisMonthAppointments(@Param("establishmentId") Long establishmentId,
                                   @Param("startOfMonth") LocalDateTime startOfMonth,
                                   @Param("endOfMonth") LocalDateTime endOfMonth);
    
    /**
     * Calculate revenue for completed appointments this month
     */
    @Query("SELECT COALESCE(SUM(a.servicePrice), 0) FROM Appointment a WHERE a.establishmentId = :establishmentId AND a.status = 'COMPLETED' AND a.appointmentDateTime >= :startOfMonth AND a.appointmentDateTime < :endOfMonth")
    java.math.BigDecimal calculateMonthlyRevenue(@Param("establishmentId") Long establishmentId,
                                               @Param("startOfMonth") LocalDateTime startOfMonth,
                                               @Param("endOfMonth") LocalDateTime endOfMonth);
    
    /**
     * Find professional performance statistics
     */
    @Query("SELECT a.professionalId, a.professionalName, COUNT(a), COALESCE(SUM(a.servicePrice), 0) FROM Appointment a WHERE a.establishmentId = :establishmentId AND a.status = 'COMPLETED' AND a.appointmentDateTime >= :startOfMonth AND a.appointmentDateTime < :endOfMonth GROUP BY a.professionalId, a.professionalName ORDER BY COUNT(a) DESC")
    List<Object[]> findProfessionalPerformanceStats(@Param("establishmentId") Long establishmentId,
                                                    @Param("startOfMonth") LocalDateTime startOfMonth,
                                                    @Param("endOfMonth") LocalDateTime endOfMonth);
}