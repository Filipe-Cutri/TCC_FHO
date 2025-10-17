package com.slotfy.controller;

import com.slotfy.model.Notification;
import com.slotfy.model.NotificationType;
import com.slotfy.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controller for Notification operations
 */
@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(originPatterns = "*")
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * Get all notifications for a client
     */
    @GetMapping("/client/{clientId}")
    public ResponseEntity<Map<String, Object>> getClientNotifications(@PathVariable Long clientId) {
        try {
            List<Notification> notifications = notificationService.getClientNotifications(clientId);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "data", notifications,
                    "count", notifications.size()
                ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Erro ao buscar notificações: " + e.getMessage()
                ));
        }
    }
    
    /**
     * Get unread notifications for a client
     */
    @GetMapping("/client/{clientId}/unread")
    public ResponseEntity<Map<String, Object>> getUnreadNotifications(@PathVariable Long clientId) {
        try {
            List<Notification> notifications = notificationService.getUnreadNotifications(clientId);
            long unreadCount = notificationService.countUnreadNotifications(clientId);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "data", notifications,
                    "count", notifications.size(),
                    "unreadCount", unreadCount
                ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Erro ao buscar notificações: " + e.getMessage()
                ));
        }
    }
    
    /**
     * Create a notification
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createNotification(@RequestBody Map<String, Object> request) {
        try {
            Long clientId = Long.parseLong(request.get("clientId").toString());
            Long establishmentId = Long.parseLong(request.get("establishmentId").toString());
            String title = (String) request.get("title");
            String message = (String) request.get("message");
            String typeStr = (String) request.get("type");
            
            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "message", "Título é obrigatório"
                    ));
            }
            
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "message", "Mensagem é obrigatória"
                    ));
            }
            
            NotificationType type = typeStr != null ? NotificationType.fromCode(typeStr) : NotificationType.GENERAL;
            
            Notification notification = notificationService.createNotification(clientId, establishmentId, title, message, type);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "message", "Notificação criada com sucesso",
                    "data", notification
                ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "message", e.getMessage()
                ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Erro ao criar notificação: " + e.getMessage()
                ));
        }
    }
    
    /**
     * Mark notification as read
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable Long id) {
        try {
            Notification notification = notificationService.markAsRead(id);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "message", "Notificação marcada como lida",
                    "data", notification
                ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "message", e.getMessage()
                ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Erro ao marcar notificação: " + e.getMessage()
                ));
        }
    }
    
    /**
     * Mark all notifications as read for a client
     */
    @PutMapping("/client/{clientId}/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead(@PathVariable Long clientId) {
        try {
            notificationService.markAllAsRead(clientId);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "message", "Todas as notificações foram marcadas como lidas"
                ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Erro ao marcar notificações: " + e.getMessage()
                ));
        }
    }
    
    /**
     * Delete a notification
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteNotification(@PathVariable Long id) {
        try {
            notificationService.deleteNotification(id);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "message", "Notificação excluída com sucesso"
                ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Erro ao excluir notificação: " + e.getMessage()
                ));
        }
    }
    
    /**
     * Get count of unread notifications
     */
    @GetMapping("/client/{clientId}/unread/count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(@PathVariable Long clientId) {
        try {
            long count = notificationService.countUnreadNotifications(clientId);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "count", count
                ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Erro ao contar notificações: " + e.getMessage()
                ));
        }
    }
}
