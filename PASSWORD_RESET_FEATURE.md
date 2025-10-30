# Password Reset Feature - Documentação

## Visão Geral

O recurso de redefinição de senha ("Esqueci a Senha") foi implementado seguindo as melhores práticas de segurança, incluindo:

- Geração de tokens criptograficamente seguros usando `SecureRandom` (32 bytes)
- Armazenamento apenas do hash SHA-256 do token
- Expiração de tokens em 1 hora
- Rate limiting básico para prevenir ataques de força bruta
- Respostas genéricas para não revelar se um e-mail existe no sistema
- Invalidação automática do token após uso

## Arquitetura

### Endpoints

#### POST /api/auth/forgot-password
Solicita a redefinição de senha para um e-mail.

**Request Body:**
```json
{
  "email": "usuario@example.com"
}
```

**Response (sempre 200 OK):**
```json
{
  "success": true,
  "message": "Se o e-mail existir, as instruções de redefinição foram enviadas"
}
```

**Nota:** O endpoint sempre retorna sucesso para não revelar se o e-mail existe no sistema (segurança).

**Rate Limiting:** 1 requisição por minuto por IP.

#### POST /api/auth/reset-password
Redefine a senha usando o token recebido por e-mail.

**Request Body:**
```json
{
  "email": "usuario@example.com",
  "token": "abc123...xyz",
  "newPassword": "novaSenha123"
}
```

**Response (sucesso):**
```json
{
  "success": true,
  "message": "Senha alterada com sucesso"
}
```

**Response (token inválido/expirado):**
```json
{
  "success": false,
  "message": "Token inválido ou expirado"
}
```

### Fluxo de Funcionamento

1. **Requisição de Reset:**
   - Usuário fornece e-mail no frontend
   - Backend gera token de 32 bytes usando `SecureRandom`
   - Token é convertido para hexadecimal (64 caracteres)
   - Hash SHA-256 do token é calculado e armazenado no banco
   - Timestamp de expiração (1 hora) é armazenado
   - E-mail com link de reset é enviado (contém o token em texto plano)
   - Retorna sempre sucesso (200 OK)

2. **Reset de Senha:**
   - Usuário clica no link do e-mail
   - Frontend submete e-mail, token e nova senha
   - Backend calcula hash SHA-256 do token recebido
   - Busca no banco pelo hash do token
   - Verifica se o e-mail coincide e se não expirou
   - Se válido, atualiza senha com BCrypt
   - Remove token do banco (invalidação)
   - Retorna sucesso

### Modelo de Dados

Campos adicionados às tabelas `clients` e `establishment_users`:

- `reset_password_token_hash` (VARCHAR 255): Hash SHA-256 do token
- `reset_password_expiry` (BIGINT): Timestamp de expiração em milissegundos

### Segurança

✅ **Token Seguro:** Gerado com `SecureRandom` (32 bytes)
✅ **Armazenamento Seguro:** Apenas hash SHA-256 armazenado
✅ **Expiração:** Token expira em 1 hora
✅ **Uso Único:** Token invalidado após uso bem-sucedido
✅ **Rate Limiting:** Máximo 1 requisição/minuto por IP
✅ **Sem Vazamento de Informação:** Respostas genéricas
✅ **Sem Log de Tokens:** Token nunca é logado em texto plano

## Configuração

### Variáveis de Ambiente

Configure as seguintes variáveis de ambiente:

```bash
# SendGrid API Key (obrigatório)
SENDGRID_API_KEY=SG.xxxxxxxxxxxxxxxxxxxxx

# E-mail remetente (opcional, padrão: noreply@slotfy.com)
SENDGRID_FROM=noreply@seudominio.com

# URL do frontend para links de reset (obrigatório)
FRONTEND_URL=https://seuapp.com

# Opcional: ID do template SendGrid para personalização
SENDGRID_TEMPLATE_RESET_ID=d-xxxxxxxxxxxxxxxx
```

### Configuração no application.properties

```properties
# SendGrid Email Configuration
sendgrid.api.key=${SENDGRID_API_KEY:}
sendgrid.from.email=${SENDGRID_FROM:noreply@slotfy.com}
sendgrid.from.name=Slotfy - Sistema de Agendamento

# Frontend URL for password reset links
frontend.url=${FRONTEND_URL:http://localhost:3000}
```

### Migração do Banco de Dados

Execute a migração SQL para adicionar os campos necessários:

```sql
-- back-end/src/main/resources/db/migration/V001__add_password_reset_fields.sql

ALTER TABLE clients ADD COLUMN IF NOT EXISTS reset_password_token_hash VARCHAR(255);
ALTER TABLE clients ADD COLUMN IF NOT EXISTS reset_password_expiry BIGINT;

ALTER TABLE establishment_users ADD COLUMN IF NOT EXISTS reset_password_token_hash VARCHAR(255);
ALTER TABLE establishment_users ADD COLUMN IF NOT EXISTS reset_password_expiry BIGINT;

CREATE INDEX IF NOT EXISTS idx_clients_reset_token_hash ON clients(reset_password_token_hash);
CREATE INDEX IF NOT EXISTS idx_establishment_users_reset_token_hash ON establishment_users(reset_password_token_hash);
```

## Testes

### Executar Testes

```bash
cd back-end
./gradlew test --tests ForgotPasswordServiceTest --tests ForgotPasswordControllerTest
```

### Cobertura de Testes

Os testes cobrem:

✅ Geração segura de tokens
✅ Validação de expiração (1 hora)
✅ Validação de token inválido
✅ Validação de e-mail incompatível
✅ Invalidação após uso
✅ Rate limiting
✅ Respostas genéricas para segurança
✅ Tratamento de exceções

### Testes Manuais

#### 1. Teste de Requisição de Reset

```bash
curl -X POST http://localhost:8080/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email": "teste@example.com"}'
```

Esperado: Status 200 com mensagem genérica de sucesso.

#### 2. Teste de Reset de Senha

```bash
curl -X POST http://localhost:8080/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "email": "teste@example.com",
    "token": "TOKEN_DO_EMAIL",
    "newPassword": "novaSenha123"
  }'
```

Esperado: Status 200 com mensagem de sucesso (se token válido).

#### 3. Teste de Rate Limiting

Execute a requisição de forgot-password duas vezes em menos de 1 minuto:

```bash
curl -X POST http://localhost:8080/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -H "X-Real-IP: 192.168.1.100" \
  -d '{"email": "teste@example.com"}'

# Imediatamente após
curl -X POST http://localhost:8080/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -H "X-Real-IP: 192.168.1.100" \
  -d '{"email": "teste@example.com"}'
```

Esperado: Segunda requisição retorna status 429 (Too Many Requests).

#### 4. Teste de Token Expirado

1. Solicite um reset de senha
2. Aguarde 1 hora e 1 minuto
3. Tente usar o token

Esperado: Status 400 com mensagem "Token inválido ou expirado".

## Template de E-mail

O e-mail enviado contém:

- Link para redefinição de senha (válido por 1 hora)
- Instruções claras
- Aviso de segurança caso não tenha solicitado
- Design responsivo em HTML

Exemplo de link gerado:
```
https://seuapp.com/reset-password?token=a1b2c3d4e5f6...
```

## Troubleshooting

### E-mails não estão sendo enviados

1. Verifique se `SENDGRID_API_KEY` está configurada
2. Verifique se a API key tem permissões de envio
3. Confira logs do backend para erros de SendGrid
4. Teste a API key diretamente com SendGrid

### Token sempre inválido

1. Verifique se o e-mail corresponde ao da requisição de reset
2. Confirme que não passou 1 hora desde a solicitação
3. Certifique-se de que o token não foi usado antes
4. Verifique logs do backend para detalhes do erro

### Rate limiting muito restritivo

O rate limiting atual é de 1 requisição por minuto por IP. Para ajustar, modifique a constante em `ForgotPasswordController.java`:

```java
private static final long RATE_LIMIT_MS = 60000; // Altere para o valor desejado em ms
```

## Melhorias Futuras

- [ ] Implementar rate limiting mais sofisticado (Redis/Caffeine)
- [ ] Adicionar auditoria de tentativas de reset
- [ ] Implementar CAPTCHA para prevenir bots
- [ ] Notificar usuário quando senha for alterada
- [ ] Permitir múltiplos templates de e-mail
- [ ] Adicionar suporte a autenticação de dois fatores

## Referências

- [OWASP Password Storage Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html)
- [OWASP Forgot Password Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Forgot_Password_Cheat_Sheet.html)
- [SendGrid Java Documentation](https://github.com/sendgrid/sendgrid-java)
