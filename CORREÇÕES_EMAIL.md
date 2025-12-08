# Correções do Sistema de Recuperação de Senha

## Problemas Identificados e Corrigidos

### 1. **URL do Frontend Incorreta**
- **Problema:** A URL padrão estava configurada como `http://localhost:3000`
- **Solução:** Alterada para `https://localhost:8443` que é onde o backend serve os arquivos estáticos
- **Arquivo:** `back-end/src/main/resources/application.properties`

### 2. **Caminho do Link de Reset Incorreto**
- **Problema:** O link gerado era `/reset-password.html` mas o arquivo está em `/pages/reset-password.html`
- **Solução:** Atualizado para incluir o diretório `/pages/`
- **Arquivo:** `back-end/src/main/java/com/slotfy/service/ForgotPasswordService.java`

### 3. **Falta de Validação de Entrada**
- **Problema:** O EmailService não validava parâmetros nulos ou vazios
- **Solução:** Adicionada validação completa de todos os parâmetros
- **Arquivo:** `back-end/src/main/java/com/slotfy/service/EmailService.java`

### 4. **Falta de Logging para Diagnóstico**
- **Problema:** Difícil identificar onde o processo estava falando
- **Solução:** Adicionados logs detalhados em cada etapa do processo
- **Arquivos:** 
  - `EmailService.java`
  - `ForgotPasswordService.java`
  - `ForgotPasswordController.java`

## Como Testar

### Pré-requisitos
1. Backend rodando na porta 8443
2. Credenciais do Gmail configuradas corretamente
3. Usuário (cliente ou estabelecimento) cadastrado no sistema

### Teste Passo a Passo

#### 1. Iniciar o Backend
```bash
cd back-end
./gradlew bootRun
```

#### 2. Acessar a Página de Recuperação de Senha

**Para Cliente:**
```
https://localhost:8443/pages/client/client-forgot-password.html
```

**Para Estabelecimento:**
```
https://localhost:8443/pages/establishment/establishment-forgot-password.html
```

#### 3. Verificar os Logs

Ao submeter o formulário, você deve ver no console do backend:

```
Requisição de recuperação de senha recebida para: [seu-email]
IP do cliente: [seu-ip]
Tentando enviar email de reset para cliente: [seu-email]
Cliente encontrado: [seu-email]
Token gerado e salvo para cliente: [seu-email]
Link de reset gerado: https://localhost:8443/pages/reset-password.html?email=[seu-email]&token=[token]
Email enviado com sucesso para: [seu-email]
Email enviado para cliente [seu-email]: true
Processamento de recuperação de senha concluído para: [seu-email]
```

#### 4. Verificar o Email

1. Acesse a caixa de entrada do email cadastrado
2. Procure por um email com assunto "Redefinição de Senha - Slotfy"
3. Clique no link de redefinição

#### 5. Redefinir a Senha

1. Você será redirecionado para `/pages/reset-password.html`
2. Digite a nova senha
3. Confirme a nova senha
4. Clique em "Alterar Senha"

## Verificando Problemas

### Se o Email Não For Enviado

1. **Verifique as credenciais no `application.properties`:**
   ```properties
   spring.mail.username=filipe.cutri18@gmail.com
   spring.mail.password=hhup lovh lfue bhhl
   ```

2. **Verifique se a senha é uma "Senha de App" do Gmail:**
   - Acesse: https://myaccount.google.com/apppasswords
   - Crie uma nova senha de app se necessário
   - Use a senha gerada (com espaços) no `application.properties`

3. **Verifique os logs de erro:**
   - Procure por `Erro ao enviar email:` no console
   - Verifique se há erros de autenticação SMTP

### Se o Link Não Funcionar

1. **Verifique se o link está correto:**
   - Deve começar com `https://localhost:8443/pages/reset-password.html`
   - Deve conter `?email=...&token=...`

2. **Verifique se o token não expirou:**
   - Tokens são válidos por 1 hora
   - Se expirou, solicite um novo link

3. **Verifique se o usuário existe:**
   - O email deve estar cadastrado no sistema
   - Verifique nos logs se aparece "Cliente encontrado" ou "Estabelecimento encontrado"

## Configuração para Produção

Para produção, configure a variável de ambiente `FRONTEND_URL`:

```bash
export FRONTEND_URL=https://seu-dominio.com
```

Ou no Railway/serviço de hospedagem, adicione a variável de ambiente:
```
FRONTEND_URL=https://seu-dominio.com
```

## Próximos Passos

Se após estas correções o email ainda não estiver sendo enviado:

1. Verifique se o firewall permite conexões na porta 465
2. Teste as credenciais SMTP usando um cliente de email
3. Verifique se a conta Gmail não está bloqueada
4. Considere usar um serviço de email dedicado (SendGrid, Mailgun, etc.)

## Suporte

Para mais informações, consulte:
- `docs/PASSWORD_RECOVERY_GUIDE.md` - Guia completo do sistema
- Logs do backend - Mensagens detalhadas de cada etapa
