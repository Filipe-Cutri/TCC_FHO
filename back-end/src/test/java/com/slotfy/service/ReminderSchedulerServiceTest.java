package com.slotfy.service;

import com.slotfy.model.Appointment;
import com.slotfy.model.Client;
import com.slotfy.model.Notification;
import com.slotfy.model.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ReminderSchedulerService
 */
@ExtendWith(MockitoExtension.class)
public class ReminderSchedulerServiceTest {

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private EmailService emailService;

    @Mock
    private ClientService clientService;

    @InjectMocks
    private ReminderSchedulerService reminderSchedulerService;

    private Appointment testAppointment;
    private Client testClient;
    private Notification testNotification;

    @BeforeEach
    void setUp() {
        // Create test appointment
        testAppointment = new Appointment();
        testAppointment.setId(1L);
        testAppointment.setClientId(1L);
        testAppointment.setEstablishmentId(1L);
        testAppointment.setAppointmentDateTime(LocalDateTime.now().plusHours(2));
        testAppointment.setServiceName("Haircut");
        testAppointment.setProfessionalName("John Doe");
        testAppointment.setServiceDurationMinutes(60);
        testAppointment.setServicePrice(new BigDecimal("50.00"));

        // Create test client
        testClient = new Client();
        testClient.setId(1L);
        testClient.setName("Test Client");
        testClient.setEmail("client@example.com");

        // Create test notification
        testNotification = new Notification(1L, 1L, "Test", "Test Message", NotificationType.APPOINTMENT_REMINDER);
        testNotification.setId(1L);
    }

    @Test
    void testCheckAndSendReminders_NoAppointments() {
        // Arrange
        when(appointmentService.getAppointmentsNeedingReminders()).thenReturn(Collections.emptyList());

        // Act
        reminderSchedulerService.checkAndSendReminders();

        // Assert
        verify(appointmentService).getAppointmentsNeedingReminders();
        verify(notificationService, never()).createAppointmentNotification(anyLong(), anyLong(), anyLong(), anyString(), anyString(), any(NotificationType.class));
    }

    @Test
    void testCheckAndSendReminders_WithAppointmentNoExistingReminder() {
        // Arrange
        when(appointmentService.getAppointmentsNeedingReminders()).thenReturn(Arrays.asList(testAppointment));
        when(notificationService.getAppointmentNotifications(1L)).thenReturn(Collections.emptyList());
        when(notificationService.createAppointmentNotification(anyLong(), anyLong(), anyLong(), anyString(), anyString(), any(NotificationType.class)))
            .thenReturn(testNotification);
        when(clientService.findById(1L)).thenReturn(Optional.of(testClient));
        when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);

        // Act
        reminderSchedulerService.checkAndSendReminders();

        // Assert
        verify(appointmentService).getAppointmentsNeedingReminders();
        verify(notificationService).getAppointmentNotifications(1L);
        verify(notificationService).createAppointmentNotification(anyLong(), anyLong(), anyLong(), anyString(), anyString(), any(NotificationType.class));
        verify(notificationService).markAsSent(anyLong());
    }

    @Test
    void testCheckAndSendReminders_WithAppointmentExistingSentReminder() {
        // Arrange
        testNotification.markAsSent();
        when(appointmentService.getAppointmentsNeedingReminders()).thenReturn(Arrays.asList(testAppointment));
        when(notificationService.getAppointmentNotifications(1L)).thenReturn(Arrays.asList(testNotification));

        // Act
        reminderSchedulerService.checkAndSendReminders();

        // Assert
        verify(appointmentService).getAppointmentsNeedingReminders();
        verify(notificationService).getAppointmentNotifications(1L);
        verify(notificationService, never()).createAppointmentNotification(anyLong(), anyLong(), anyLong(), anyString(), anyString(), any(NotificationType.class));
    }

    @Test
    void testCheckAndSendReminders_HandlesException() {
        // Arrange
        when(appointmentService.getAppointmentsNeedingReminders()).thenThrow(new RuntimeException("Database error"));

        // Act - should not throw exception
        assertDoesNotThrow(() -> reminderSchedulerService.checkAndSendReminders());

        // Assert
        verify(appointmentService).getAppointmentsNeedingReminders();
    }

    @Test
    void testSendAppointmentReminder_Success() {
        // Arrange
        when(notificationService.createAppointmentNotification(anyLong(), anyLong(), anyLong(), anyString(), anyString(), any(NotificationType.class)))
            .thenReturn(testNotification);
        when(clientService.findById(1L)).thenReturn(Optional.of(testClient));
        when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);

        // Act
        reminderSchedulerService.sendAppointmentReminder(testAppointment);

        // Assert
        verify(notificationService).createAppointmentNotification(eq(1L), eq(1L), eq(1L), eq("Lembrete de Agendamento"), anyString(), eq(NotificationType.APPOINTMENT_REMINDER));
        verify(clientService).findById(1L);
        verify(emailService).sendEmail(eq("client@example.com"), eq("Lembrete de Agendamento"), anyString());
        verify(notificationService).markAsSent(1L);
    }

    @Test
    void testSendAppointmentReminder_ClientNotFound() {
        // Arrange
        when(notificationService.createAppointmentNotification(anyLong(), anyLong(), anyLong(), anyString(), anyString(), any(NotificationType.class)))
            .thenReturn(testNotification);
        when(clientService.findById(1L)).thenReturn(Optional.empty());

        // Act - should not throw exception
        assertDoesNotThrow(() -> reminderSchedulerService.sendAppointmentReminder(testAppointment));

        // Assert
        verify(notificationService).createAppointmentNotification(anyLong(), anyLong(), anyLong(), anyString(), anyString(), any(NotificationType.class));
        verify(clientService).findById(1L);
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
        verify(notificationService).markAsSent(1L);
    }

    @Test
    void testSendAppointmentReminder_EmailSendFails() {
        // Arrange
        when(notificationService.createAppointmentNotification(anyLong(), anyLong(), anyLong(), anyString(), anyString(), any(NotificationType.class)))
            .thenReturn(testNotification);
        when(clientService.findById(1L)).thenReturn(Optional.of(testClient));
        when(emailService.sendEmail(anyString(), anyString(), anyString())).thenThrow(new RuntimeException("Email service error"));

        // Act - should not throw exception
        assertDoesNotThrow(() -> reminderSchedulerService.sendAppointmentReminder(testAppointment));

        // Assert
        verify(notificationService).createAppointmentNotification(anyLong(), anyLong(), anyLong(), anyString(), anyString(), any(NotificationType.class));
        verify(notificationService).markAsSent(1L);
    }

    @Test
    void testSendAppointmentConfirmation_Success() {
        // Arrange
        when(notificationService.createAppointmentNotification(anyLong(), anyLong(), anyLong(), anyString(), anyString(), any(NotificationType.class)))
            .thenReturn(testNotification);
        when(clientService.findById(1L)).thenReturn(Optional.of(testClient));
        when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);

        // Act
        reminderSchedulerService.sendAppointmentConfirmation(testAppointment);

        // Assert
        verify(notificationService).createAppointmentNotification(eq(1L), eq(1L), eq(1L), eq("Agendamento Confirmado"), anyString(), eq(NotificationType.APPOINTMENT_CONFIRMATION));
        verify(clientService).findById(1L);
        verify(emailService).sendEmail(eq("client@example.com"), eq("Agendamento Confirmado"), anyString());
        verify(notificationService).markAsSent(1L);
    }

    @Test
    void testSendAppointmentConfirmation_ClientNotFound() {
        // Arrange
        when(notificationService.createAppointmentNotification(anyLong(), anyLong(), anyLong(), anyString(), anyString(), any(NotificationType.class)))
            .thenReturn(testNotification);
        when(clientService.findById(1L)).thenReturn(Optional.empty());

        // Act - should not throw exception
        assertDoesNotThrow(() -> reminderSchedulerService.sendAppointmentConfirmation(testAppointment));

        // Assert
        verify(notificationService).createAppointmentNotification(anyLong(), anyLong(), anyLong(), anyString(), anyString(), any(NotificationType.class));
        verify(clientService).findById(1L);
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
        verify(notificationService).markAsSent(1L);
    }

    @Test
    void testSendAppointmentCancellation_Success() {
        // Arrange
        when(notificationService.createAppointmentNotification(anyLong(), anyLong(), anyLong(), anyString(), anyString(), any(NotificationType.class)))
            .thenReturn(testNotification);
        when(clientService.findById(1L)).thenReturn(Optional.of(testClient));
        when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);

        // Act
        reminderSchedulerService.sendAppointmentCancellation(testAppointment);

        // Assert
        verify(notificationService).createAppointmentNotification(eq(1L), eq(1L), eq(1L), eq("Agendamento Cancelado"), anyString(), eq(NotificationType.APPOINTMENT_CANCELLED));
        verify(clientService).findById(1L);
        verify(emailService).sendEmail(eq("client@example.com"), eq("Agendamento Cancelado"), anyString());
        verify(notificationService).markAsSent(1L);
    }

    @Test
    void testSendAppointmentCancellation_ClientNotFound() {
        // Arrange
        when(notificationService.createAppointmentNotification(anyLong(), anyLong(), anyLong(), anyString(), anyString(), any(NotificationType.class)))
            .thenReturn(testNotification);
        when(clientService.findById(1L)).thenReturn(Optional.empty());

        // Act - should not throw exception
        assertDoesNotThrow(() -> reminderSchedulerService.sendAppointmentCancellation(testAppointment));

        // Assert
        verify(notificationService).createAppointmentNotification(anyLong(), anyLong(), anyLong(), anyString(), anyString(), any(NotificationType.class));
        verify(clientService).findById(1L);
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
        verify(notificationService).markAsSent(1L);
    }

    @Test
    void testSendAppointmentReminder_WithNullServiceInfo() {
        // Arrange
        testAppointment.setServiceName(null);
        testAppointment.setProfessionalName(null);
        testAppointment.setServiceDurationMinutes(null);
        
        when(notificationService.createAppointmentNotification(anyLong(), anyLong(), anyLong(), anyString(), anyString(), any(NotificationType.class)))
            .thenReturn(testNotification);
        when(clientService.findById(1L)).thenReturn(Optional.of(testClient));
        when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);

        // Act
        reminderSchedulerService.sendAppointmentReminder(testAppointment);

        // Assert
        verify(notificationService).createAppointmentNotification(anyLong(), anyLong(), anyLong(), anyString(), contains("Não especificado"), any(NotificationType.class));
        verify(notificationService).markAsSent(1L);
    }

    @Test
    void testSendAppointmentConfirmation_WithNullServicePrice() {
        // Arrange
        testAppointment.setServicePrice(null);
        
        when(notificationService.createAppointmentNotification(anyLong(), anyLong(), anyLong(), anyString(), anyString(), any(NotificationType.class)))
            .thenReturn(testNotification);
        when(clientService.findById(1L)).thenReturn(Optional.of(testClient));
        when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);

        // Act
        reminderSchedulerService.sendAppointmentConfirmation(testAppointment);

        // Assert
        verify(notificationService).createAppointmentNotification(anyLong(), anyLong(), anyLong(), anyString(), anyString(), any(NotificationType.class));
        verify(notificationService).markAsSent(1L);
    }
}
