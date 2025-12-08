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

**Nota de Segurança:** As credenciais abaixo são exemplos. Em produção, use variáveis de ambiente e nunca commite credenciais reais no código.

```properties
# Gmail SMTP Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=465
spring.mail.username=filipe.cutri18@gmail.com
spring.mail.password=hhup lovh lfue bhhl

spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.ssl.enable=true
spring.mail.properties.mail.smtp.ssl.trust=smtp.gmail.com
spring.mail.properties.mail.smtp.socketFactory.class=javax.net.ssl.SSLSocketFactory
spring.mail.properties.mail.smtp.socketFactory.port=465

# Email sender information
email.from.address=filipe.cutri18@gmail.com
email.from.name=Slotfy - Sistema de Agendamento

# Frontend URL for password reset links
frontend.url=${FRONTEND_URL:https://localhost:8443}
```

**Nota:** Esta configuração usa porta 465 com SSL direto. Alternativamente, você pode usar porta 587 com STARTTLS.

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
{FRONTEND_URL}/pages/reset-password.html?email={EMAIL}&token={TOKEN}
```

Exemplo:
```
https://localhost:8443/pages/reset-password.html?email=usuario@exemplo.com&token=a1b2c3d4e5f6...
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

1. **Verifique as credenciais do Gmail**
   - Certifique-se de usar "Senha de App" e não a senha normal
   - A senha de app tem o formato: `xxxx xxxx xxxx xxxx` (com espaços)
   - Verifique se a conta Gmail tem verificação em duas etapas habilitada

2. **Verifique os logs do backend para erros SMTP**
   - Procure por mensagens de erro no console do backend
   - Mensagens de log úteis (emails são mascarados para segurança):
     ```
     Requisição de recuperação de senha recebida: us***@example.com
     Tentando enviar email de reset para cliente: us***@example.com
     Cliente encontrado
     Token gerado e salvo para cliente
     Enviando email de reset para cliente
     Email enviado: true
     ```

3. **Erros comuns de SMTP:**
   - `AuthenticationFailedException`: Senha incorreta ou não é senha de app
   - `Connection timed out`: Firewall bloqueando porta 465
   - `Invalid Addresses`: Email de destino inválido

4. **Verifique a configuração SMTP:**
   - Porta 465 deve estar aberta
   - SSL deve estar habilitado
   - Host: smtp.gmail.com

### Link de redefinição não funciona

1. **Verifique se `FRONTEND_URL` está configurado corretamente**
   - Para desenvolvimento local: `https://localhost:8443`
   - Para produção: URL do seu domínio (ex: `https://slotfy.com`)

2. **Verifique se o token não expirou**
   - Token é válido por 1 hora
   - Após 1 hora, solicite um novo link

3. **Verifique o formato do link**
   - Deve conter: `/pages/reset-password.html?email=...&token=...`
   - Email deve corresponder ao cadastrado
   - Token deve ter 64 caracteres hexadecimais

4. **Verifique os logs do backend:**
   - Se o usuário não foi encontrado, não haverá log de "Cliente encontrado"
   - Se o email não foi enviado, o log mostrará `Email enviado: false`

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
