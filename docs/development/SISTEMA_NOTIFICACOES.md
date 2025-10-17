# Sistema de Notificações e Comunicação com Cliente - Slotfy

## Visão Geral

Este documento detalha o sistema de notificações e comunicação com o cliente final do sistema Slotfy, incluindo lembretes de agendamento, confirmações online e outras funcionalidades de engajamento.

---

## 1. Tipos de Notificações

### 1.1 Notificações Automáticas

#### 1.1.1 Confirmação de Agendamento
**Trigger:** Imediatamente após criação de agendamento

**Destinatário:** Cliente

**Canais:** Email (implementado), SMS (futuro), Push (futuro)

**Conteúdo:**
```
Assunto: Agendamento Confirmado - [Nome do Estabelecimento]

Olá [Nome do Cliente],

Seu agendamento foi confirmado com sucesso!

Detalhes do Agendamento:
- Data: [Data do Agendamento]
- Horário: [Horário]
- Serviço: [Nome do Serviço]
- Profissional: [Nome do Profissional]
- Estabelecimento: [Nome e Endereço]
- Valor: R$ [Preço]

Para cancelar ou reagendar, acesse: [Link para o Painel do Cliente]

Atenciosamente,
Equipe Slotfy
```

**Implementação Backend:**
```java
@Service
public class NotificationService {
    
    @Autowired
    private EmailService emailService;
    
    public void sendAppointmentConfirmation(Appointment appointment, Client client) {
        EmailNotification notification = EmailNotification.builder()
            .to(client.getEmail())
            .subject("Agendamento Confirmado - " + appointment.getEstablishmentName())
            .template("appointment-confirmation")
            .data(Map.of(
                "clientName", client.getName(),
                "appointmentDate", appointment.getAppointmentDateTime().format(DATE_FORMATTER),
                "appointmentTime", appointment.getAppointmentDateTime().format(TIME_FORMATTER),
                "serviceName", appointment.getServiceName(),
                "professionalName", appointment.getProfessionalName(),
                "establishmentName", appointment.getEstablishmentName(),
                "price", appointment.getServicePrice(),
                "cancelUrl", generateCancelUrl(appointment.getId())
            ))
            .build();
            
        emailService.send(notification);
    }
}
```

---

#### 1.1.2 Lembrete de Agendamento (24h antes)
**Trigger:** 24 horas antes do horário agendado

**Destinatário:** Cliente

**Canais:** Email, SMS (futuro), Push (futuro)

**Conteúdo:**
```
Assunto: Lembrete: Seu agendamento é amanhã

Olá [Nome do Cliente],

Este é um lembrete de que você tem um agendamento amanhã!

Detalhes:
- Data: [Data]
- Horário: [Horário]
- Serviço: [Serviço]
- Profissional: [Profissional]
- Local: [Endereço]

Por favor, confirme sua presença clicando aqui: [Link de Confirmação]

Caso precise cancelar, acesse: [Link para Cancelamento]

Até breve!
Equipe Slotfy
```

**Implementação Backend:**
```java
@Component
public class AppointmentReminderScheduler {
    
    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired
    private NotificationService notificationService;
    
    // Executa a cada hora
    @Scheduled(cron = "0 0 * * * *")
    public void sendReminders() {
        LocalDateTime tomorrow = LocalDateTime.now().plusHours(24);
        LocalDateTime tomorrowPlus1Hour = tomorrow.plusHours(1);
        
        List<Appointment> upcomingAppointments = appointmentService
            .getAppointmentsBetween(tomorrow, tomorrowPlus1Hour);
            
        for (Appointment appointment : upcomingAppointments) {
            if (!appointment.isReminderSent()) {
                notificationService.sendAppointmentReminder(appointment);
                appointmentService.markReminderSent(appointment.getId());
            }
        }
    }
}
```

---

#### 1.1.3 Confirmação de Cancelamento
**Trigger:** Imediatamente após cancelamento

**Destinatários:** Cliente e Estabelecimento

**Canais:** Email

**Conteúdo (Cliente):**
```
Assunto: Agendamento Cancelado

Olá [Nome do Cliente],

Seu agendamento foi cancelado com sucesso.

Detalhes do Agendamento Cancelado:
- Data: [Data]
- Horário: [Horário]
- Serviço: [Serviço]
- Profissional: [Profissional]

Você pode fazer um novo agendamento a qualquer momento através do nosso site.

Atenciosamente,
Equipe Slotfy
```

**Conteúdo (Estabelecimento):**
```
Assunto: Agendamento Cancelado - [Nome do Cliente]

Olá [Nome do Estabelecimento],

Um agendamento foi cancelado:

Detalhes:
- Cliente: [Nome do Cliente]
- Data: [Data]
- Horário: [Horário]
- Serviço: [Serviço]
- Profissional: [Profissional]

Acesse o painel administrativo para mais informações.

Equipe Slotfy
```

---

#### 1.1.4 Notificação de Reagendamento
**Trigger:** Imediatamente após reagendamento

**Destinatários:** Cliente e Estabelecimento

**Canais:** Email

**Conteúdo:**
```
Assunto: Agendamento Reagendado

Olá [Nome do Cliente],

Seu agendamento foi reagendado com sucesso!

Novo Horário:
- Data: [Nova Data]
- Horário: [Novo Horário]

Detalhes Originais:
- Data Original: [Data Original]
- Horário Original: [Horário Original]

Serviço: [Nome do Serviço]
Profissional: [Nome do Profissional]

Atenciosamente,
Equipe Slotfy
```

---

#### 1.1.5 Solicitação de Avaliação
**Trigger:** 2 horas após conclusão do serviço

**Destinatário:** Cliente

**Canais:** Email, Push (futuro)

**Conteúdo:**
```
Assunto: Como foi sua experiência?

Olá [Nome do Cliente],

Esperamos que você tenha gostado do seu atendimento!

Serviço: [Nome do Serviço]
Profissional: [Nome do Profissional]
Data: [Data]

Gostaríamos de saber sua opinião. Por favor, avalie sua experiência:

[Botões de Avaliação: ★★★★★]

Sua avaliação nos ajuda a melhorar nossos serviços.

Obrigado,
Equipe Slotfy
```

---

### 1.2 Notificações Promocionais

#### 1.2.1 Ofertas e Promoções
**Trigger:** Manual (estabelecimento envia)

**Destinatários:** Clientes selecionados

**Canais:** Email, Push (futuro)

**Conteúdo:**
```
Assunto: Oferta Especial - [Nome da Promoção]

Olá [Nome do Cliente],

Temos uma oferta especial para você!

[Descrição da Promoção]

Válido até: [Data de Expiração]

Agende agora: [Link para Agendamento]

Atenciosamente,
[Nome do Estabelecimento]
```

---

#### 1.2.2 Lembrete de Retorno
**Trigger:** 30 dias após último atendimento (configurável)

**Destinatário:** Cliente

**Canais:** Email, SMS (futuro)

**Conteúdo:**
```
Assunto: Sentimos sua falta!

Olá [Nome do Cliente],

Já faz um tempo desde seu último atendimento conosco.

Que tal agendar um novo horário?

Último Serviço: [Nome do Serviço]
Data: [Data do Último Atendimento]

Agende agora: [Link para Agendamento]

Esperamos vê-lo em breve!
[Nome do Estabelecimento]
```

---

## 2. Estrutura de Dados

### 2.1 Tabela: notifications

```sql
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    recipient_type VARCHAR(20) NOT NULL, -- CLIENT ou ESTABLISHMENT
    recipient_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    appointment_id BIGINT,
    read BOOLEAN DEFAULT false,
    sent BOOLEAN DEFAULT false,
    sent_at TIMESTAMP,
    channel VARCHAR(20), -- EMAIL, SMS, PUSH
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, SENT, FAILED
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE SET NULL
);

CREATE INDEX idx_notifications_recipient ON notifications(recipient_type, recipient_id);
CREATE INDEX idx_notifications_read ON notifications(read);
CREATE INDEX idx_notifications_sent ON notifications(sent);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
```

### 2.2 Enum: NotificationType

```java
public enum NotificationType {
    APPOINTMENT_CONFIRMATION("appointment_confirmation", "Confirmação de Agendamento"),
    APPOINTMENT_REMINDER("appointment_reminder", "Lembrete de Agendamento"),
    APPOINTMENT_CANCELLED("appointment_cancelled", "Agendamento Cancelado"),
    APPOINTMENT_RESCHEDULED("appointment_rescheduled", "Agendamento Reagendado"),
    APPOINTMENT_COMPLETED("appointment_completed", "Serviço Concluído"),
    REVIEW_REQUEST("review_request", "Solicitação de Avaliação"),
    PROMOTION("promotion", "Promoção"),
    RETURN_REMINDER("return_reminder", "Lembrete de Retorno");
    
    private final String code;
    private final String description;
    
    // Constructor, getters, fromCode method...
}
```

### 2.3 Enum: NotificationChannel

```java
public enum NotificationChannel {
    EMAIL("email", "Email"),
    SMS("sms", "SMS"),
    PUSH("push", "Notificação Push"),
    IN_APP("in_app", "Notificação In-App");
    
    private final String code;
    private final String description;
    
    // Constructor, getters, fromCode method...
}
```

---

## 3. APIs de Notificação

### 3.1 Listar Notificações do Cliente

```
GET /api/client/notifications?clientId={clientId}&unreadOnly={boolean}

Response (Success - 200 OK):
{
  "success": true,
  "data": [
    {
      "id": 1,
      "type": "APPOINTMENT_REMINDER",
      "title": "Lembrete de Agendamento",
      "message": "Seu agendamento é amanhã às 14:00",
      "read": false,
      "createdAt": "2025-10-19T10:00:00",
      "appointmentId": 123
    }
  ],
  "count": 5,
  "unreadCount": 2
}
```

### 3.2 Marcar Notificação como Lida

```
PUT /api/client/notifications/{id}/read?clientId={clientId}

Response (Success - 200 OK):
{
  "success": true,
  "message": "Notificação marcada como lida"
}
```

### 3.3 Marcar Todas como Lidas

```
PUT /api/client/notifications/read-all?clientId={clientId}

Response (Success - 200 OK):
{
  "success": true,
  "message": "Todas as notificações foram marcadas como lidas",
  "count": 5
}
```

### 3.4 Deletar Notificação

```
DELETE /api/client/notifications/{id}?clientId={clientId}

Response (Success - 200 OK):
{
  "success": true,
  "message": "Notificação removida"
}
```

### 3.5 Enviar Notificação (Interno/Admin)

```
POST /api/notifications/send
Content-Type: application/json

Request Body:
{
  "type": "PROMOTION",
  "recipientType": "CLIENT",
  "recipientIds": [1, 2, 3],
  "title": "Oferta Especial",
  "message": "30% de desconto em todos os serviços!",
  "channels": ["EMAIL", "PUSH"],
  "appointmentId": null
}

Response (Success - 200 OK):
{
  "success": true,
  "message": "Notificações enviadas com sucesso",
  "data": {
    "totalSent": 3,
    "failed": 0
  }
}
```

---

## 4. Preferências de Notificação

### 4.1 Tabela: client_notification_preferences

```sql
CREATE TABLE client_notification_preferences (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL UNIQUE,
    email_enabled BOOLEAN DEFAULT true,
    sms_enabled BOOLEAN DEFAULT false,
    push_enabled BOOLEAN DEFAULT true,
    appointment_confirmation BOOLEAN DEFAULT true,
    appointment_reminder BOOLEAN DEFAULT true,
    appointment_changes BOOLEAN DEFAULT true,
    promotions BOOLEAN DEFAULT true,
    return_reminders BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE
);
```

### 4.2 API de Preferências

**Obter Preferências:**
```
GET /api/client/notifications/preferences?clientId={clientId}

Response (Success - 200 OK):
{
  "success": true,
  "data": {
    "emailEnabled": true,
    "smsEnabled": false,
    "pushEnabled": true,
    "appointmentConfirmation": true,
    "appointmentReminder": true,
    "appointmentChanges": true,
    "promotions": true,
    "returnReminders": true
  }
}
```

**Atualizar Preferências:**
```
PUT /api/client/notifications/preferences?clientId={clientId}
Content-Type: application/json

Request Body:
{
  "emailEnabled": true,
  "smsEnabled": true,
  "pushEnabled": true,
  "appointmentConfirmation": true,
  "appointmentReminder": true,
  "appointmentChanges": true,
  "promotions": false,
  "returnReminders": true
}

Response (Success - 200 OK):
{
  "success": true,
  "message": "Preferências atualizadas com sucesso"
}
```

---

## 5. Implementação de Email

### 5.1 Configuração Spring Boot

```yaml
# application.yml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${EMAIL_USERNAME}
    password: ${EMAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
```

### 5.2 Service de Email

```java
@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private TemplateEngine templateEngine;
    
    @Value("${app.email.from}")
    private String fromEmail;
    
    public void sendTemplatedEmail(String to, String subject, String template, Map<String, Object> variables) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            
            Context context = new Context();
            context.setVariables(variables);
            
            String htmlContent = templateEngine.process(template, context);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            
            log.info("Email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
```

### 5.3 Templates de Email (Thymeleaf)

**appointment-confirmation.html:**
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Confirmação de Agendamento</title>
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
        .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
        .content { padding: 20px; background-color: #f9f9f9; }
        .details { background-color: white; padding: 15px; margin: 15px 0; border-left: 4px solid #4CAF50; }
        .button { display: inline-block; padding: 10px 20px; background-color: #4CAF50; color: white; text-decoration: none; border-radius: 5px; margin-top: 15px; }
        .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>Agendamento Confirmado</h1>
        </div>
        
        <div class="content">
            <p>Olá <strong th:text="${clientName}">Cliente</strong>,</p>
            
            <p>Seu agendamento foi confirmado com sucesso!</p>
            
            <div class="details">
                <h3>Detalhes do Agendamento:</h3>
                <p><strong>Data:</strong> <span th:text="${appointmentDate}">20/10/2025</span></p>
                <p><strong>Horário:</strong> <span th:text="${appointmentTime}">14:00</span></p>
                <p><strong>Serviço:</strong> <span th:text="${serviceName}">Corte Masculino</span></p>
                <p><strong>Profissional:</strong> <span th:text="${professionalName}">Carlos Barbeiro</span></p>
                <p><strong>Estabelecimento:</strong> <span th:text="${establishmentName}">Barbearia do João</span></p>
                <p><strong>Valor:</strong> R$ <span th:text="${price}">35,00</span></p>
            </div>
            
            <p>Para cancelar ou reagendar, acesse seu painel:</p>
            <a th:href="${cancelUrl}" class="button">Acessar Painel</a>
        </div>
        
        <div class="footer">
            <p>Este é um email automático, por favor não responda.</p>
            <p>&copy; 2025 Slotfy - Sistema de Agendamento Inteligente</p>
        </div>
    </div>
</body>
</html>
```

---

## 6. Integração SMS (Futuro)

### 6.1 Configuração Twilio

```java
@Configuration
public class TwilioConfig {
    
    @Value("${twilio.account.sid}")
    private String accountSid;
    
    @Value("${twilio.auth.token}")
    private String authToken;
    
    @Value("${twilio.phone.number}")
    private String phoneNumber;
    
    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }
}

@Service
public class SmsService {
    
    @Value("${twilio.phone.number}")
    private String fromPhoneNumber;
    
    public void sendSms(String to, String message) {
        try {
            Message sms = Message.creator(
                new PhoneNumber(to),
                new PhoneNumber(fromPhoneNumber),
                message
            ).create();
            
            log.info("SMS sent successfully to: {} - SID: {}", to, sms.getSid());
        } catch (Exception e) {
            log.error("Failed to send SMS to: {}", to, e);
            throw new RuntimeException("Failed to send SMS", e);
        }
    }
}
```

---

## 7. Push Notifications (Futuro)

### 7.1 Configuração Firebase Cloud Messaging

```java
@Configuration
public class FirebaseConfig {
    
    @PostConstruct
    public void initialize() {
        try {
            FileInputStream serviceAccount = new FileInputStream("firebase-adminsdk.json");
            
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();
                
            FirebaseApp.initializeApp(options);
        } catch (IOException e) {
            log.error("Failed to initialize Firebase", e);
        }
    }
}

@Service
public class PushNotificationService {
    
    public void sendPushNotification(String deviceToken, String title, String body, Map<String, String> data) {
        try {
            Message message = Message.builder()
                .setToken(deviceToken)
                .setNotification(Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build())
                .putAllData(data)
                .build();
                
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Push notification sent successfully: {}", response);
        } catch (Exception e) {
            log.error("Failed to send push notification", e);
            throw new RuntimeException("Failed to send push notification", e);
        }
    }
}
```

---

## 8. Monitoramento e Logs

### 8.1 Logs de Notificação

```java
@Entity
@Table(name = "notification_logs")
public class NotificationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long notificationId;
    private String channel;
    private String status; // SENT, FAILED, DELIVERED, OPENED
    private String recipientEmail;
    private String recipientPhone;
    private LocalDateTime sentAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime openedAt;
    private String errorMessage;
    private String externalId; // ID do provedor (Twilio, FCM, etc.)
    
    // getters, setters
}
```

### 8.2 Métricas

```java
@Service
public class NotificationMetricsService {
    
    public NotificationMetrics getMetrics(LocalDateTime start, LocalDateTime end) {
        return NotificationMetrics.builder()
            .totalSent(countSent(start, end))
            .totalDelivered(countDelivered(start, end))
            .totalFailed(countFailed(start, end))
            .totalOpened(countOpened(start, end))
            .deliveryRate(calculateDeliveryRate(start, end))
            .openRate(calculateOpenRate(start, end))
            .failureRate(calculateFailureRate(start, end))
            .byChannel(getMetricsByChannel(start, end))
            .byType(getMetricsByType(start, end))
            .build();
    }
}
```

---

## 9. Testes

### 9.1 Testes Unitários

```java
@Test
public void testSendAppointmentConfirmation() {
    // Arrange
    Appointment appointment = createTestAppointment();
    Client client = createTestClient();
    
    // Act
    notificationService.sendAppointmentConfirmation(appointment, client);
    
    // Assert
    verify(emailService, times(1)).sendTemplatedEmail(
        eq(client.getEmail()),
        contains("Confirmado"),
        eq("appointment-confirmation"),
        any()
    );
}

@Test
public void testReminderScheduler() {
    // Arrange
    LocalDateTime tomorrow = LocalDateTime.now().plusHours(24);
    List<Appointment> appointments = Arrays.asList(
        createAppointmentAt(tomorrow)
    );
    when(appointmentService.getAppointmentsBetween(any(), any()))
        .thenReturn(appointments);
    
    // Act
    reminderScheduler.sendReminders();
    
    // Assert
    verify(notificationService, times(1)).sendAppointmentReminder(any());
}
```

### 9.2 Testes de Integração

```java
@SpringBootTest
@TestPropertySource(properties = {
    "spring.mail.host=localhost",
    "spring.mail.port=3025"
})
public class EmailServiceIntegrationTest {
    
    @Autowired
    private EmailService emailService;
    
    @Test
    public void testSendEmail() {
        // Test with GreenMail or similar
        emailService.sendTemplatedEmail(
            "test@example.com",
            "Test Subject",
            "test-template",
            Map.of("name", "Test User")
        );
        
        // Verify email was sent
        // Assert email content
    }
}
```

---

## 10. Configurações de Produção

### 10.1 Rate Limiting

```java
@Component
public class NotificationRateLimiter {
    
    private final Map<String, RateLimiter> limiters = new ConcurrentHashMap<>();
    
    public boolean tryAcquire(String key, int maxPerHour) {
        RateLimiter limiter = limiters.computeIfAbsent(
            key,
            k -> RateLimiter.create(maxPerHour / 3600.0)
        );
        return limiter.tryAcquire();
    }
}
```

### 10.2 Retry Policy

```java
@Configuration
public class NotificationRetryConfig {
    
    @Bean
    public RetryTemplate notificationRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000);
        backOffPolicy.setMultiplier(2.0);
        backOffPolicy.setMaxInterval(10000);
        
        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        
        return retryTemplate;
    }
}
```

---

## 11. Checklist de Implementação

- [x] Estrutura de dados (tabelas, enums)
- [ ] Service de notificações base
- [ ] Integração de email
- [ ] Templates de email
- [ ] Scheduler de lembretes
- [ ] APIs de notificação para cliente
- [ ] APIs de preferências
- [ ] Logs e monitoramento
- [ ] Testes unitários
- [ ] Testes de integração
- [ ] Integração SMS (futuro)
- [ ] Push notifications (futuro)
- [ ] Dashboard de métricas (futuro)
- [ ] A/B testing de templates (futuro)

---

*Documento gerado em: Outubro 2025*  
*Versão: 1.0*  
*Autor: Sistema Slotfy - Equipe de Desenvolvimento*
