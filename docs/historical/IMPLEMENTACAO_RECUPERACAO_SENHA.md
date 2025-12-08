# Simplificação da Rotina de Recuperação de Senha

## O que foi implementado?

Esta implementação simplifica e melhora o sistema de recuperação de senha do Slotfy, substituindo o SendGrid por Gmail SMTP para envio de e-mails.

## Mudanças Principais

### Backend

1. **Substituição de SendGrid por Gmail SMTP**
   - Removida dependência: `com.sendgrid:sendgrid-java`
   - Adicionada dependência: `spring-boot-starter-mail`
   - Configuração SMTP do Gmail em `application.properties`

2. **Serviço de E-mail Simplificado**
   - `EmailService` agora usa `JavaMailSender` do Spring
   - Código mais simples e nativo do Spring Boot
   - Melhor integração com o ecossistema Spring

3. **Endpoint Unificado**
   - `/api/auth/forgot-password` - Para clientes e estabelecimentos
   - `/api/auth/reset-password` - Para redefinir a senha
   - Rate limiting para prevenir abuso

### Frontend

1. **Páginas de Recuperação de Senha**
   - Cliente: `client-forgot-password.html`
   - Estabelecimento: `establishment-forgot-password.html`
   - Redefinição: `reset-password.html`

2. **API Atualizada**
   - Endpoints unificados em `/api/auth/*`
   - Validação aprimorada com e-mail e token

## Credenciais Configuradas

As credenciais fornecidas foram configuradas em `application.properties`:

```properties
spring.mail.username=${GMAIL_USERNAME:filipe.cutri18@gmail.com.br}
spring.mail.password=${GMAIL_PASSWORD:sanbeohgweevaljp}
```

**Nota:** Para produção, use variáveis de ambiente para maior segurança.

## Como Funciona?

### 1. Usuário solicita recuperação de senha
- Acessa a página "Esqueci minha senha"
- Informa o e-mail cadastrado
- Sistema envia e-mail com link de recuperação

### 2. E-mail de recuperação
- Link contém token seguro (SHA-256)
- Válido por 1 hora
- Formato: `{URL}/reset-password.html?email={EMAIL}&token={TOKEN}`

### 3. Redefinição de senha
- Usuário clica no link do e-mail
- Preenche nova senha
- Sistema valida token e atualiza senha
- Token é invalidado após uso

## Segurança

✅ **Implementado:**
- Tokens seguros com SHA-256
- Expiração de 1 hora
- Rate limiting (1 req/min por IP)
- Não revela se e-mail existe
- Token invalidado após uso
- Validação de e-mail e token

## Como Testar?

### Backend
```bash
cd back-end
./gradlew clean build
./gradlew test
```

### Testar Recuperação de Senha

1. Inicie o backend:
```bash
cd back-end
./gradlew bootRun
```

2. Acesse no navegador:
   - Cliente: `https://localhost:8443/pages/client/client-forgot-password.html`
   - Estabelecimento: `https://localhost:8443/pages/establishment/establishment-forgot-password.html`

3. Digite um e-mail cadastrado

4. Verifique o e-mail recebido (filipe.cutri18@gmail.com.br)

5. Clique no link de redefinição

6. Digite e confirme a nova senha

## Configuração para Produção

Para ambiente de produção, configure as variáveis de ambiente:

```bash
export GMAIL_USERNAME=seu-email@gmail.com
export GMAIL_PASSWORD=sua-senha-de-app
export EMAIL_FROM_ADDRESS=seu-email@gmail.com
export FRONTEND_URL=https://seu-dominio.com
```

## Documentação Completa

Para mais detalhes, consulte: [PASSWORD_RECOVERY_GUIDE.md](PASSWORD_RECOVERY_GUIDE.md)

## Arquivos Modificados

### Backend
- `build.gradle` - Atualização de dependências
- `application.properties` - Configuração SMTP
- `EmailService.java` - Novo serviço de e-mail
- `ForgotPasswordService.java` - Links de recuperação atualizados
- `EmailServiceTest.java` - Testes atualizados

### Frontend
- `api-config.js` - Endpoints atualizados
- `reset-password.html` - Suporte a parâmetro de e-mail

## Status

✅ **Concluído:**
- Configuração Gmail SMTP
- Serviço de e-mail simplificado
- Endpoints de API atualizados
- Frontend atualizado
- Testes passando
- CodeQL sem alertas
- Documentação completa

## Próximos Passos

Para ativar em produção:

1. Configure as variáveis de ambiente no Railway
2. Teste o envio de e-mail em produção
3. Verifique se o `FRONTEND_URL` está correto
4. Monitore logs para erros de SMTP

## Suporte

Para problemas ou dúvidas:
- Consulte [PASSWORD_RECOVERY_GUIDE.md](PASSWORD_RECOVERY_GUIDE.md)
- Verifique os logs do backend
- Confirme configuração do Gmail App Password
