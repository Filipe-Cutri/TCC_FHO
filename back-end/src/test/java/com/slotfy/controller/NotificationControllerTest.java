package com.slotfy.controller;

import com.slotfy.model.Notification;
import com.slotfy.model.NotificationType;
import com.slotfy.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@Import({com.slotfy.config.SecurityConfig.class})
public class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    public void testGetClientNotifications() throws Exception {
        List<Notification> notifications = Arrays.asList(
            createMockNotification(1L, "Title 1", "Message 1", false),
            createMockNotification(2L, "Title 2", "Message 2", true)
        );
        
        when(notificationService.getClientNotifications(1L)).thenReturn(notifications);

        mockMvc.perform(get("/api/notifications/client/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser
    public void testGetClientNotificationsError() throws Exception {
        when(notificationService.getClientNotifications(1L))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/notifications/client/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser
    public void testGetUnreadNotifications() throws Exception {
        List<Notification> notifications = Arrays.asList(
            createMockNotification(1L, "Title 1", "Message 1", false)
        );
        
        when(notificationService.getUnreadNotifications(1L)).thenReturn(notifications);
        when(notificationService.countUnreadNotifications(1L)).thenReturn(1L);

        mockMvc.perform(get("/api/notifications/client/1/unread"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.unreadCount").value(1))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser
    public void testGetUnreadNotificationsError() throws Exception {
        when(notificationService.getUnreadNotifications(1L))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/notifications/client/1/unread"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testCreateNotificationSuccess() throws Exception {
        Notification notification = createMockNotification(1L, "Test Title", "Test Message", false);
        
        when(notificationService.createNotification(anyLong(), anyLong(), anyString(), anyString(), any(NotificationType.class)))
                .thenReturn(notification);

        Map<String, Object> request = new HashMap<>();
        request.put("clientId", "1");
        request.put("establishmentId", "1");
        request.put("title", "Test Title");
        request.put("message", "Test Message");
        request.put("type", "GENERAL");

        mockMvc.perform(post("/api/notifications")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Notificação criada com sucesso"))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @WithMockUser
    public void testCreateNotificationMissingTitle() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("clientId", "1");
        request.put("establishmentId", "1");
        request.put("message", "Test Message");

        mockMvc.perform(post("/api/notifications")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Título é obrigatório"));
    }

    @Test
    @WithMockUser
    public void testCreateNotificationEmptyTitle() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("clientId", "1");
        request.put("establishmentId", "1");
        request.put("title", "  ");
        request.put("message", "Test Message");

        mockMvc.perform(post("/api/notifications")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Título é obrigatório"));
    }

    @Test
    @WithMockUser
    public void testCreateNotificationMissingMessage() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("clientId", "1");
        request.put("establishmentId", "1");
        request.put("title", "Test Title");

        mockMvc.perform(post("/api/notifications")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Mensagem é obrigatória"));
    }

    @Test
    @WithMockUser
    public void testCreateNotificationEmptyMessage() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("clientId", "1");
        request.put("establishmentId", "1");
        request.put("title", "Test Title");
        request.put("message", "  ");

        mockMvc.perform(post("/api/notifications")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Mensagem é obrigatória"));
    }

    @Test
    @WithMockUser
    public void testCreateNotificationInvalidArgument() throws Exception {
        when(notificationService.createNotification(anyLong(), anyLong(), anyString(), anyString(), any(NotificationType.class)))
                .thenThrow(new IllegalArgumentException("Invalid argument"));

        Map<String, Object> request = new HashMap<>();
        request.put("clientId", "1");
        request.put("establishmentId", "1");
        request.put("title", "Test Title");
        request.put("message", "Test Message");

        mockMvc.perform(post("/api/notifications")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid argument"));
    }

    @Test
    @WithMockUser
    public void testCreateNotificationError() throws Exception {
        when(notificationService.createNotification(anyLong(), anyLong(), anyString(), anyString(), any(NotificationType.class)))
                .thenThrow(new RuntimeException("Database error"));

        Map<String, Object> request = new HashMap<>();
        request.put("clientId", "1");
        request.put("establishmentId", "1");
        request.put("title", "Test Title");
        request.put("message", "Test Message");

        mockMvc.perform(post("/api/notifications")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testMarkAsReadSuccess() throws Exception {
        Notification notification = createMockNotification(1L, "Test Title", "Test Message", true);
        
        when(notificationService.markAsRead(1L)).thenReturn(notification);

        mockMvc.perform(put("/api/notifications/1/read")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Notificação marcada como lida"))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @WithMockUser
    public void testMarkAsReadInvalidArgument() throws Exception {
        when(notificationService.markAsRead(1L))
                .thenThrow(new IllegalArgumentException("Notification not found"));

        mockMvc.perform(put("/api/notifications/1/read")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Notification not found"));
    }

    @Test
    @WithMockUser
    public void testMarkAsReadError() throws Exception {
        when(notificationService.markAsRead(1L))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(put("/api/notifications/1/read")
                        .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testMarkAllAsReadSuccess() throws Exception {
        doNothing().when(notificationService).markAllAsRead(1L);

        mockMvc.perform(put("/api/notifications/client/1/read-all")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Todas as notificações foram marcadas como lidas"));
    }

    @Test
    @WithMockUser
    public void testMarkAllAsReadError() throws Exception {
        doThrow(new RuntimeException("Database error")).when(notificationService).markAllAsRead(1L);

        mockMvc.perform(put("/api/notifications/client/1/read-all")
                        .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testDeleteNotificationSuccess() throws Exception {
        doNothing().when(notificationService).deleteNotification(1L);

        mockMvc.perform(delete("/api/notifications/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Notificação excluída com sucesso"));
    }

    @Test
    @WithMockUser
    public void testDeleteNotificationError() throws Exception {
        doThrow(new RuntimeException("Database error")).when(notificationService).deleteNotification(1L);

        mockMvc.perform(delete("/api/notifications/1")
                        .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testGetUnreadCount() throws Exception {
        when(notificationService.countUnreadNotifications(1L)).thenReturn(5L);

        mockMvc.perform(get("/api/notifications/client/1/unread/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(5));
    }

    @Test
    @WithMockUser
    public void testGetUnreadCountError() throws Exception {
        when(notificationService.countUnreadNotifications(1L))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/notifications/client/1/unread/count"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    private Notification createMockNotification(Long id, String title, String message, boolean read) {
        Notification notification = new Notification();
        notification.setId(id);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRead(read);
        notification.setType(NotificationType.GENERAL);
        notification.setCreatedAt(LocalDateTime.now());
        return notification;
    }
}
