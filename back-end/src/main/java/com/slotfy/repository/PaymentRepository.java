package com.slotfy.repository;

import com.slotfy.model.Payment;
import com.slotfy.model.PaymentStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Payment entity
 */
@Repository
public interface PaymentRepository extends BaseRepository<Payment, Long> {
    
    /**
     * Find all payments for a client
     */
    List<Payment> findByClientIdOrderByCreatedAtDesc(Long clientId);
    
    /**
     * Find all payments for an establishment
     */
    List<Payment> findByEstablishmentIdOrderByCreatedAtDesc(Long establishmentId);
    
    /**
     * Find payments by status for an establishment
     */
    List<Payment> findByEstablishmentIdAndStatusOrderByCreatedAtDesc(Long establishmentId, PaymentStatus status);
    
    /**
     * Find payments by appointment
     */
    List<Payment> findByAppointmentId(Long appointmentId);
    
    /**
     * Find payments in date range for an establishment
     */
    @Query("SELECT p FROM Payment p WHERE p.establishmentId = :establishmentId AND p.createdAt BETWEEN :startDate AND :endDate ORDER BY p.createdAt DESC")
    List<Payment> findByEstablishmentIdAndDateRange(@Param("establishmentId") Long establishmentId, 
                                                     @Param("startDate") LocalDateTime startDate, 
                                                     @Param("endDate") LocalDateTime endDate);
    
    /**
     * Calculate total revenue for an establishment
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.establishmentId = :establishmentId AND p.status = 'COMPLETED'")
    BigDecimal calculateTotalRevenue(@Param("establishmentId") Long establishmentId);
    
    /**
     * Calculate revenue for a specific period
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.establishmentId = :establishmentId AND p.status = 'COMPLETED' AND p.paymentDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateRevenueForPeriod(@Param("establishmentId") Long establishmentId,
                                        @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);
    
    /**
     * Count payments by status
     */
    long countByEstablishmentIdAndStatus(Long establishmentId, PaymentStatus status);
}
