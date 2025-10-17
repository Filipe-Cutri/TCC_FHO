package com.slotfy.service;

import com.slotfy.model.Notification;
import com.slotfy.model.NotificationType;
import com.slotfy.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing notifications
 */
@Service
public class NotificationService extends BaseService<Notification, Long> {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    public NotificationService(NotificationRepository repository) {
        super(repository);
        this.notificationRepository = repository;
    }
    
    /**
     * Create a notification
     */
    public Notification createNotification(Long clientId, Long establishmentId, String title, String message, NotificationType type) {
        Notification notification = new Notification(clientId, establishmentId, title, message, type);
        return save(notification);
    }
    
    /**
     * Create a notification for an appointment
     */
    public Notification createAppointmentNotification(Long clientId, Long establishmentId, Long appointmentId, 
                                                     String title, String message, NotificationType type) {
        Notification notification = new Notification(clientId, establishmentId, title, message, type);
        notification.setAppointmentId(appointmentId);
        return save(notification);
    }
    
    /**
     * Schedule a notification
     */
    public Notification scheduleNotification(Long clientId, Long establishmentId, String title, String message, 
                                           NotificationType type, LocalDateTime scheduledFor) {
        Notification notification = new Notification(clientId, establishmentId, title, message, type);
        notification.setScheduledFor(scheduledFor);
        return save(notification);
    }
    
    /**
     * Get all notifications for a client
     */
    public List<Notification> getClientNotifications(Long clientId) {
        return notificationRepository.findByClientIdOrderByCreatedAtDesc(clientId);
    }
    
    /**
     * Get unread notifications for a client
     */
    public List<Notification> getUnreadNotifications(Long clientId) {
        return notificationRepository.findByClientIdAndReadOrderByCreatedAtDesc(clientId, false);
    }
    
    /**
     * Get notifications by establishment
     */
    public List<Notification> getEstablishmentNotifications(Long establishmentId) {
        return notificationRepository.findByEstablishmentIdOrderByCreatedAtDesc(establishmentId);
    }
    
    /**
     * Get notifications by type
     */
    public List<Notification> getNotificationsByType(Long clientId, NotificationType type) {
        return notificationRepository.findByClientIdAndTypeOrderByCreatedAtDesc(clientId, type);
    }
    
    /**
     * Get notifications for an appointment
     */
    public List<Notification> getAppointmentNotifications(Long appointmentId) {
        return notificationRepository.findByAppointmentIdOrderByCreatedAtDesc(appointmentId);
    }
    
    /**
     * Mark notification as read
     */
    public Notification markAsRead(Long notificationId) {
        Optional<Notification> notificationOpt = findById(notificationId);
        if (notificationOpt.isEmpty()) {
            throw new IllegalArgumentException("Notificação não encontrada");
        }
        
        Notification notification = notificationOpt.get();
        notification.markAsRead();
        return save(notification);
    }
    
    /**
     * Mark all notifications as read for a client
     */
    public void markAllAsRead(Long clientId) {
        List<Notification> notifications = getUnreadNotifications(clientId);
        for (Notification notification : notifications) {
            notification.markAsRead();
            save(notification);
        }
    }
    
    /**
     * Mark notification as sent
     */
    public Notification markAsSent(Long notificationId) {
        Optional<Notification> notificationOpt = findById(notificationId);
        if (notificationOpt.isEmpty()) {
            throw new IllegalArgumentException("Notificação não encontrada");
        }
        
        Notification notification = notificationOpt.get();
        notification.markAsSent();
        return save(notification);
    }
    
    /**
     * Get pending notifications to be sent
     */
    public List<Notification> getPendingNotifications() {
        return notificationRepository.findPendingNotifications(LocalDateTime.now());
    }
    
    /**
     * Count unread notifications
     */
    public long countUnreadNotifications(Long clientId) {
        return notificationRepository.countByClientIdAndRead(clientId, false);
    }
    
    /**
     * Delete notification
     */
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }
    
    /**
     * Delete old read notifications (older than 30 days)
     */
    public void deleteOldReadNotifications() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        notificationRepository.deleteOldReadNotifications(cutoffDate);
    }
}
