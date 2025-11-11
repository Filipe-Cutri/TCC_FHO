package com.slotfy.service;

import com.slotfy.model.Notification;
import com.slotfy.model.NotificationType;
import com.slotfy.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NotificationService
 */
@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    private NotificationService notificationService;

    private Notification testNotification;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationRepository);

        // Create test notification
        testNotification = new Notification(1L, 1L, "Test Title", "Test Message", NotificationType.APPOINTMENT_REMINDER);
        testNotification.setId(1L);
    }

    @Test
    void testCreateNotification_Success() {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // Act
        Notification result = notificationService.createNotification(1L, 1L, "Test Title", "Test Message", NotificationType.APPOINTMENT_REMINDER);

        // Assert
        assertNotNull(result);
        assertEquals("Test Title", result.getTitle());
        assertEquals("Test Message", result.getMessage());
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void testCreateAppointmentNotification_Success() {
        // Arrange
        Notification appointmentNotification = new Notification(1L, 1L, "Appointment Reminder", "Your appointment is tomorrow", NotificationType.APPOINTMENT_REMINDER);
        appointmentNotification.setId(1L);
        appointmentNotification.setAppointmentId(10L);
        when(notificationRepository.save(any(Notification.class))).thenReturn(appointmentNotification);

        // Act
        Notification result = notificationService.createAppointmentNotification(1L, 1L, 10L, "Appointment Reminder", "Your appointment is tomorrow", NotificationType.APPOINTMENT_REMINDER);

        // Assert
        assertNotNull(result);
        assertEquals("Appointment Reminder", result.getTitle());
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void testScheduleNotification_Success() {
        // Arrange
        LocalDateTime scheduledTime = LocalDateTime.now().plusHours(2);
        testNotification.setScheduledFor(scheduledTime);
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // Act
        Notification result = notificationService.scheduleNotification(1L, 1L, "Scheduled Title", "Scheduled Message", NotificationType.APPOINTMENT_REMINDER, scheduledTime);

        // Assert
        assertNotNull(result);
        assertEquals(scheduledTime, result.getScheduledFor());
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void testGetClientNotifications_Success() {
        // Arrange
        List<Notification> notifications = Arrays.asList(testNotification);
        when(notificationRepository.findByClientIdOrderByCreatedAtDesc(1L)).thenReturn(notifications);

        // Act
        List<Notification> result = notificationService.getClientNotifications(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(notificationRepository).findByClientIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void testGetUnreadNotifications_Success() {
        // Arrange
        List<Notification> notifications = Arrays.asList(testNotification);
        when(notificationRepository.findByClientIdAndReadOrderByCreatedAtDesc(1L, false)).thenReturn(notifications);

        // Act
        List<Notification> result = notificationService.getUnreadNotifications(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(notificationRepository).findByClientIdAndReadOrderByCreatedAtDesc(1L, false);
    }

    @Test
    void testGetEstablishmentNotifications_Success() {
        // Arrange
        List<Notification> notifications = Arrays.asList(testNotification);
        when(notificationRepository.findByEstablishmentIdOrderByCreatedAtDesc(1L)).thenReturn(notifications);

        // Act
        List<Notification> result = notificationService.getEstablishmentNotifications(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(notificationRepository).findByEstablishmentIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void testGetNotificationsByType_Success() {
        // Arrange
        List<Notification> notifications = Arrays.asList(testNotification);
        when(notificationRepository.findByClientIdAndTypeOrderByCreatedAtDesc(1L, NotificationType.APPOINTMENT_REMINDER)).thenReturn(notifications);

        // Act
        List<Notification> result = notificationService.getNotificationsByType(1L, NotificationType.APPOINTMENT_REMINDER);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(notificationRepository).findByClientIdAndTypeOrderByCreatedAtDesc(1L, NotificationType.APPOINTMENT_REMINDER);
    }

    @Test
    void testGetAppointmentNotifications_Success() {
        // Arrange
        List<Notification> notifications = Arrays.asList(testNotification);
        when(notificationRepository.findByAppointmentIdOrderByCreatedAtDesc(10L)).thenReturn(notifications);

        // Act
        List<Notification> result = notificationService.getAppointmentNotifications(10L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(notificationRepository).findByAppointmentIdOrderByCreatedAtDesc(10L);
    }

    @Test
    void testMarkAsRead_Success() {
        // Arrange
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // Act
        Notification result = notificationService.markAsRead(1L);

        // Assert
        assertNotNull(result);
        verify(notificationRepository).findById(1L);
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void testMarkAsRead_NotificationNotFound() {
        // Arrange
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            notificationService.markAsRead(999L);
        });

        assertEquals("Notificação não encontrada", exception.getMessage());
        verify(notificationRepository).findById(999L);
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void testMarkAllAsRead_Success() {
        // Arrange
        Notification notification1 = new Notification(1L, 1L, "Title 1", "Message 1", NotificationType.APPOINTMENT_REMINDER);
        Notification notification2 = new Notification(1L, 1L, "Title 2", "Message 2", NotificationType.APPOINTMENT_CONFIRMATION);
        List<Notification> notifications = Arrays.asList(notification1, notification2);
        
        when(notificationRepository.findByClientIdAndReadOrderByCreatedAtDesc(1L, false)).thenReturn(notifications);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification1);

        // Act
        notificationService.markAllAsRead(1L);

        // Assert
        verify(notificationRepository).findByClientIdAndReadOrderByCreatedAtDesc(1L, false);
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    void testMarkAsSent_Success() {
        // Arrange
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // Act
        Notification result = notificationService.markAsSent(1L);

        // Assert
        assertNotNull(result);
        verify(notificationRepository).findById(1L);
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void testMarkAsSent_NotificationNotFound() {
        // Arrange
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            notificationService.markAsSent(999L);
        });

        assertEquals("Notificação não encontrada", exception.getMessage());
        verify(notificationRepository).findById(999L);
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void testGetPendingNotifications_Success() {
        // Arrange
        List<Notification> notifications = Arrays.asList(testNotification);
        when(notificationRepository.findPendingNotifications(any(LocalDateTime.class))).thenReturn(notifications);

        // Act
        List<Notification> result = notificationService.getPendingNotifications();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(notificationRepository).findPendingNotifications(any(LocalDateTime.class));
    }

    @Test
    void testCountUnreadNotifications_Success() {
        // Arrange
        when(notificationRepository.countByClientIdAndRead(1L, false)).thenReturn(5L);

        // Act
        long result = notificationService.countUnreadNotifications(1L);

        // Assert
        assertEquals(5L, result);
        verify(notificationRepository).countByClientIdAndRead(1L, false);
    }

    @Test
    void testDeleteNotification_Success() {
        // Arrange
        doNothing().when(notificationRepository).deleteById(1L);

        // Act
        notificationService.deleteNotification(1L);

        // Assert
        verify(notificationRepository).deleteById(1L);
    }

    @Test
    void testDeleteOldReadNotifications_Success() {
        // Arrange
        doNothing().when(notificationRepository).deleteOldReadNotifications(any(LocalDateTime.class));

        // Act
        notificationService.deleteOldReadNotifications();

        // Assert
        verify(notificationRepository).deleteOldReadNotifications(any(LocalDateTime.class));
    }
}
