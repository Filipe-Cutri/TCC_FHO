package com.slotfy.service;

import com.slotfy.model.Appointment;
import com.slotfy.model.Notification;
import com.slotfy.model.NotificationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for scheduling and sending appointment reminders
 */
@Service
public class ReminderSchedulerService {
    
    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private ClientService clientService;
    
    /**
     * Check for appointments that need reminders every hour
     * This method is scheduled to run every hour
     */
    @Scheduled(cron = "0 0 * * * *") // Run every hour at the start of the hour
    public void checkAndSendReminders() {
        try {
            List<Appointment> appointmentsNeedingReminders = appointmentService.getAppointmentsNeedingReminders();
            
            for (Appointment appointment : appointmentsNeedingReminders) {
                // Check if reminder was already sent
                List<Notification> existingReminders = notificationService.getAppointmentNotifications(appointment.getId());
                boolean reminderSent = existingReminders.stream()
                    .anyMatch(n -> n.getType() == NotificationType.APPOINTMENT_REMINDER && n.isSent());
                
                if (!reminderSent) {
                    sendAppointmentReminder(appointment);
                }
            }
        } catch (Exception e) {
            System.err.println("Error checking reminders: " + e.getMessage());
        }
    }
    
    /**
     * Send appointment reminder notification
     */
    public void sendAppointmentReminder(Appointment appointment) {
        try {
            String title = "Lembrete de Agendamento";
            String message = buildReminderMessage(appointment);
            
            // Create notification
            Notification notification = notificationService.createAppointmentNotification(
                appointment.getClientId(),
                appointment.getEstablishmentId(),
                appointment.getId(),
                title,
                message,
                NotificationType.APPOINTMENT_REMINDER
            );
            
            // Try to send email
            try {
                var client = clientService.findById(appointment.getClientId());
                if (client.isPresent() && client.get().getEmail() != null) {
                    emailService.sendEmail(
                        client.get().getEmail(),
                        title,
                        message
                    );
                }
            } catch (Exception e) {
                System.err.println("Error sending reminder email: " + e.getMessage());
            }
            
            // Mark notification as sent
            notificationService.markAsSent(notification.getId());
            
        } catch (Exception e) {
            System.err.println("Error sending appointment reminder: " + e.getMessage());
        }
    }
    
    /**
     * Send appointment confirmation notification
     */
    public void sendAppointmentConfirmation(Appointment appointment) {
        try {
            String title = "Agendamento Confirmado";
            String message = buildConfirmationMessage(appointment);
            
            // Create notification
            Notification notification = notificationService.createAppointmentNotification(
                appointment.getClientId(),
                appointment.getEstablishmentId(),
                appointment.getId(),
                title,
                message,
                NotificationType.APPOINTMENT_CONFIRMATION
            );
            
            // Try to send email
            try {
                var client = clientService.findById(appointment.getClientId());
                if (client.isPresent() && client.get().getEmail() != null) {
                    emailService.sendEmail(
                        client.get().getEmail(),
                        title,
                        message
                    );
                }
            } catch (Exception e) {
                System.err.println("Error sending confirmation email: " + e.getMessage());
            }
            
            // Mark notification as sent
            notificationService.markAsSent(notification.getId());
            
        } catch (Exception e) {
            System.err.println("Error sending appointment confirmation: " + e.getMessage());
        }
    }
    
    /**
     * Send appointment cancellation notification
     */
    public void sendAppointmentCancellation(Appointment appointment) {
        try {
            String title = "Agendamento Cancelado";
            String message = buildCancellationMessage(appointment);
            
            // Create notification
            Notification notification = notificationService.createAppointmentNotification(
                appointment.getClientId(),
                appointment.getEstablishmentId(),
                appointment.getId(),
                title,
                message,
                NotificationType.APPOINTMENT_CANCELLED
            );
            
            // Try to send email
            try {
                var client = clientService.findById(appointment.getClientId());
                if (client.isPresent() && client.get().getEmail() != null) {
                    emailService.sendEmail(
                        client.get().getEmail(),
                        title,
                        message
                    );
                }
            } catch (Exception e) {
                System.err.println("Error sending cancellation email: " + e.getMessage());
            }
            
            // Mark notification as sent
            notificationService.markAsSent(notification.getId());
            
        } catch (Exception e) {
            System.err.println("Error sending appointment cancellation: " + e.getMessage());
        }
    }
    
    /**
     * Build reminder message
     */
    private String buildReminderMessage(Appointment appointment) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
        String dateTime = appointment.getAppointmentDateTime().format(formatter);
        
        return String.format(
            "Olá! Este é um lembrete do seu agendamento marcado para %s.\n\n" +
            "Serviço: %s\n" +
            "Profissional: %s\n" +
            "Duração: %d minutos\n\n" +
            "Aguardamos você!",
            dateTime,
            appointment.getServiceName() != null ? appointment.getServiceName() : "Não especificado",
            appointment.getProfessionalName() != null ? appointment.getProfessionalName() : "Não especificado",
            appointment.getServiceDurationMinutes() != null ? appointment.getServiceDurationMinutes() : 0
        );
    }
    
    /**
     * Build confirmation message
     */
    private String buildConfirmationMessage(Appointment appointment) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
        String dateTime = appointment.getAppointmentDateTime().format(formatter);
        
        return String.format(
            "Seu agendamento foi confirmado com sucesso!\n\n" +
            "Data e Hora: %s\n" +
            "Serviço: %s\n" +
            "Profissional: %s\n" +
            "Duração: %d minutos\n" +
            "Valor: R$ %.2f\n\n" +
            "Aguardamos você!",
            dateTime,
            appointment.getServiceName() != null ? appointment.getServiceName() : "Não especificado",
            appointment.getProfessionalName() != null ? appointment.getProfessionalName() : "Não especificado",
            appointment.getServiceDurationMinutes() != null ? appointment.getServiceDurationMinutes() : 0,
            appointment.getServicePrice() != null ? appointment.getServicePrice() : java.math.BigDecimal.ZERO
        );
    }
    
    /**
     * Build cancellation message
     */
    private String buildCancellationMessage(Appointment appointment) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
        String dateTime = appointment.getAppointmentDateTime().format(formatter);
        
        return String.format(
            "Seu agendamento foi cancelado.\n\n" +
            "Data e Hora: %s\n" +
            "Serviço: %s\n" +
            "Profissional: %s\n\n" +
            "Se precisar reagendar, entre em contato conosco.",
            dateTime,
            appointment.getServiceName() != null ? appointment.getServiceName() : "Não especificado",
            appointment.getProfessionalName() != null ? appointment.getProfessionalName() : "Não especificado"
        );
    }
}
