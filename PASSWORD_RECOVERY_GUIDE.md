# Guia de Recuperação de Senha - Slotfy

## Visão Geral

Este guia documenta o sistema de recuperação de senha implementado no Slotfy, que foi simplificado para usar Gmail SMTP ao invés de SendGrid.

## Arquitetura

### Backend

#### Componentes Principais

1. **EmailService** (`com.slotfy.service.EmailService`)
   - Serviço responsável pelo envio de e-mails
   - Usa `JavaMailSender` do Spring Boot
   - Configurado para Gmail SMTP

2. **ForgotPasswordService** (`com.slotfy.service.ForgotPasswordService`)
   - Gerencia todo o fluxo de recuperação de senha
   - Gera tokens seguros (SHA-256)
   - Valida tokens e expirações
   - Suporta tanto clientes quanto estabelecimentos

3. **ForgotPasswordController** (`com.slotfy.controller.ForgotPasswordController`)
   - Endpoint unificado: `/api/auth/forgot-password`
   - Endpoint de redefinição: `/api/auth/reset-password`
   - Proteção contra rate limiting

#### Configuração SMTP

```properties
# Gmail SMTP Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${GMAIL_USERNAME:filipe.cutri18@gmail.com.br}
spring.mail.password=${GMAIL_PASSWORD:sanbeohgweevaljp}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
spring.mail.properties.mail.smtp.connectiontimeout=5000
spring.mail.properties.mail.smtp.timeout=5000
spring.mail.properties.mail.smtp.writetimeout=5000

# Email sender information
email.from.address=${EMAIL_FROM_ADDRESS:filipe.cutri18@gmail.com.br}
email.from.name=Slotfy - Sistema de Agendamento
```

### Frontend

#### Páginas

1. **Cliente**
   - Página de esqueci a senha: `/front-end/src/pages/client/client-forgot-password.html`
   - Endpoint API: `/api/auth/forgot-password`

2. **Estabelecimento**
   - Página de esqueci a senha: `/front-end/src/pages/establishment/establishment-forgot-password.html`
   - Endpoint API: `/api/auth/forgot-password`

3. **Redefinir Senha**
   - Página comum: `/front-end/src/pages/reset-password.html`
   - Endpoint API: `/api/auth/reset-password`

## Fluxo de Recuperação de Senha

### 1. Solicitação de Recuperação

```
Usuario → Frontend (client/establishment-forgot-password.html)
       → POST /api/auth/forgot-password { email }
       → Backend (ForgotPasswordController)
       → ForgotPasswordService
       → Gera token seguro
       → Salva hash do token no banco
       → EmailService envia e-mail com link
```

### 2. Link de Recuperação

O link gerado tem o formato:
```
{FRONTEND_URL}/reset-password.html?email={EMAIL}&token={TOKEN}
```

Exemplo:
```
http://localhost:3000/reset-password.html?email=usuario@exemplo.com&token=a1b2c3d4e5f6...
```

### 3. Redefinição de Senha

```
Usuario → Frontend (reset-password.html)
       → Preenche nova senha
       → POST /api/auth/reset-password { email, token, newPassword }
       → Backend valida token e expiraçao
       → Atualiza senha
       → Invalida token
```

## Segurança

### Características de Segurança Implementadas

1. **Token Seguro**
   - Gerado com `SecureRandom` (32 bytes)
   - Hash SHA-256 armazenado no banco
   - Token original enviado apenas por e-mail

2. **Expiração**
   - Tokens válidos por 1 hora
   - Verificação automática de expiração
   - Token invalidado após uso

3. **Rate Limiting**
   - 1 requisição por IP a cada 1 minuto
   - Proteção contra abuso

4. **Privacidade**
   - Não revela se e-mail existe ou não
   - Mensagem genérica de sucesso sempre retornada

5. **Validação**
   - E-mail e token obrigatórios
   - Senha deve ter no mínimo 6 caracteres
   - Confirmação de senha no frontend

## Configuração de Ambiente

### Variáveis de Ambiente (Produção)

Para produção, configure as seguintes variáveis de ambiente:

```bash
GMAIL_USERNAME=seu-email@gmail.com
GMAIL_PASSWORD=sua-senha-de-app
EMAIL_FROM_ADDRESS=seu-email@gmail.com
FRONTEND_URL=https://seu-dominio.com
```

### App Password do Gmail

Para usar Gmail SMTP, você precisa gerar uma "Senha de App":

1. Acesse sua Conta Google
2. Vá em "Segurança"
3. Ative "Verificação em duas etapas"
4. Gere uma "Senha de app" para "Email"
5. Use essa senha no `GMAIL_PASSWORD`

## Testes

### Testes Unitários

Os testes estão em:
- `back-end/src/test/java/com/slotfy/service/EmailServiceTest.java`

Para rodar os testes:

```bash
cd back-end
./gradlew test --tests "*EmailService*"
```

### Teste Manual

1. Inicie o backend:
```bash
cd back-end
./gradlew bootRun
```

2. Acesse a página de esqueci a senha:
   - Cliente: `http://localhost:8443/pages/client/client-forgot-password.html`
   - Estabelecimento: `http://localhost:8443/pages/establishment/establishment-forgot-password.html`

3. Digite um e-mail cadastrado e clique em "Enviar Instruções"

4. Verifique o e-mail recebido

5. Clique no link de redefinição

6. Digite a nova senha e confirme

## Troubleshooting

### E-mail não está sendo enviado

1. Verifique as credenciais do Gmail
2. Certifique-se de usar "Senha de App" e não a senha normal
3. Verifique os logs do backend para erros SMTP
4. Verifique se a conta Gmail tem 2FA habilitado

### Link de redefinição não funciona

1. Verifique se `FRONTEND_URL` está configurado corretamente
2. Verifique se o token não expirou (válido por 1 hora)
3. Verifique se o e-mail na URL corresponde ao e-mail cadastrado

### Erro 429 (Too Many Requests)

- Aguarde 1 minuto entre requisições do mesmo IP
- Rate limiting está ativo para prevenir abuso

## Melhorias Futuras

Possíveis melhorias para o sistema:

1. **Templates de E-mail**
   - Usar templates mais elaborados
   - Suporte a internacionalização

2. **Notificações**
   - Notificar usuário quando senha for alterada
   - E-mail de confirmação de alteração

3. **Auditoria**
   - Log de tentativas de redefinição
   - Histórico de alterações de senha

4. **Multi-fator**
   - Código SMS adicional
   - Autenticação por aplicativo

## Referências

- [Spring Boot Mail](https://docs.spring.io/spring-boot/docs/current/reference/html/messaging.html#messaging.email)
- [JavaMailSender](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/mail/javamail/JavaMailSender.html)
- [Gmail SMTP Settings](https://support.google.com/mail/answer/7126229)
