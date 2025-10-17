package com.slotfy.repository;

import com.slotfy.model.Notification;
import com.slotfy.model.NotificationType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Notification entity
 */
@Repository
public interface NotificationRepository extends BaseRepository<Notification, Long> {
    
    /**
     * Find all notifications for a client
     */
    List<Notification> findByClientIdOrderByCreatedAtDesc(Long clientId);
    
    /**
     * Find unread notifications for a client
     */
    List<Notification> findByClientIdAndReadOrderByCreatedAtDesc(Long clientId, Boolean read);
    
    /**
     * Find notifications by establishment
     */
    List<Notification> findByEstablishmentIdOrderByCreatedAtDesc(Long establishmentId);
    
    /**
     * Find notifications by type
     */
    List<Notification> findByClientIdAndTypeOrderByCreatedAtDesc(Long clientId, NotificationType type);
    
    /**
     * Find notifications by appointment
     */
    List<Notification> findByAppointmentIdOrderByCreatedAtDesc(Long appointmentId);
    
    /**
     * Find pending notifications to be sent
     */
    @Query("SELECT n FROM Notification n WHERE n.sent = false AND (n.scheduledFor IS NULL OR n.scheduledFor <= :currentDateTime)")
    List<Notification> findPendingNotifications(@Param("currentDateTime") LocalDateTime currentDateTime);
    
    /**
     * Count unread notifications for a client
     */
    long countByClientIdAndRead(Long clientId, Boolean read);
    
    /**
     * Delete old read notifications
     */
    @Query("DELETE FROM Notification n WHERE n.read = true AND n.createdAt < :cutoffDate")
    void deleteOldReadNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);
}
