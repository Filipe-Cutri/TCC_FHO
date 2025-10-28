# Funcionalidade de Email com SendGrid

## Visão Geral

O sistema Slotfy agora possui integração completa com o SendGrid para envio de emails transacionais. Esta funcionalidade permite o envio de emails para clientes e estabelecimentos de forma automática.

## Configuração

### Dependência

A dependência do SendGrid foi adicionada ao `build.gradle`:

```gradle
implementation 'com.sendgrid:sendgrid-java:4.10.2'
```

### Configuração no application.properties

As seguintes configurações foram adicionadas:

```properties
# SendGrid Email Configuration
sendgrid.api.key=YOUR_SENDGRID_API_KEY_HERE
sendgrid.from.email=noreply@slotfy.com
sendgrid.from.name=Slotfy - Sistema de Agendamento
```

**IMPORTANTE**: Nunca commite a chave da API real no código. Use variáveis de ambiente em produção.

## Funcionalidades Implementadas

### 1. Redefinição de Senha

O sistema envia emails para clientes e estabelecimentos quando solicitam redefinição de senha.

**Serviço responsável**: `ForgotPasswordService`

**Métodos**:
- `sendClientPasswordResetEmail(String email)` - Envia email de redefinição para clientes
- `sendEstablishmentPasswordResetEmail(String email)` - Envia email de redefinição para estabelecimentos

**Template**: Email HTML com botão de redefinição e link alternativo. O link expira em 24 horas.

### 2. Notificações de Agendamento

O sistema envia emails automáticos para clientes sobre seus agendamentos.

**Serviço responsável**: `ReminderSchedulerService`

**Tipos de emails**:

#### Lembrete de Agendamento
- Enviado automaticamente antes da data do agendamento
- Inclui: data, hora, serviço, profissional e duração
- Método: `sendAppointmentReminder(Appointment appointment)`

#### Confirmação de Agendamento
- Enviado quando um agendamento é criado
- Inclui: data, hora, serviço, profissional, duração e valor
- Método: `sendAppointmentConfirmation(Appointment appointment)`

#### Cancelamento de Agendamento
- Enviado quando um agendamento é cancelado
- Inclui: data, hora, serviço e profissional
- Método: `sendAppointmentCancellation(Appointment appointment)`

## Classe EmailService

A classe `EmailService` é o ponto central para envio de emails. Ela utiliza a API do SendGrid para enviar emails em formato HTML.

### Métodos Principais

#### `sendEmail(String to, String subject, String body)`
Envia um email genérico.

**Parâmetros**:
- `to`: Email do destinatário
- `subject`: Assunto do email
- `body`: Corpo do email em HTML

**Retorno**: `boolean` - `true` se enviado com sucesso, `false` caso contrário

#### `sendPasswordResetEmail(String to, String resetLink)`
Envia um email de redefinição de senha com template HTML formatado.

**Parâmetros**:
- `to`: Email do destinatário
- `resetLink`: Link para redefinição de senha

**Retorno**: `boolean` - `true` se enviado com sucesso, `false` caso contrário

## Tratamento de Erros

O serviço possui tratamento de erros robusto:

1. **Validação de Email**: Emails nulos ou vazios retornam `false`
2. **Exceções de IO**: Capturadas e logadas, retorna `false`
3. **Erros da API SendGrid**: Status codes diferentes de 2xx são logados e retornam `false`
4. **Logging**: Todos os erros são registrados no console para debugging

## Códigos de Status SendGrid

- **202**: Email aceito para envio (sucesso)
- **200-299**: Range de sucesso
- **400+**: Erros (validação, autenticação, etc.)

## Testes

A classe `EmailServiceTest` contém testes unitários para validar:

- Criação do serviço
- Envio de emails válidos
- Envio de emails de redefinição de senha
- Tratamento de emails nulos
- Tratamento de emails vazios

## Uso nos Controllers

Os emails são enviados automaticamente através dos serviços:

1. **ForgotPasswordController**: Utiliza `ForgotPasswordService` para emails de redefinição
2. **AppointmentController**: Utiliza `ReminderSchedulerService` para notificações de agendamento

## Segurança

### ⚠️ IMPORTANTE: Proteção da API Key

A chave da API SendGrid é sensível e **NUNCA** deve ser commitada diretamente no código em produção. 

#### Configuração Atual (Desenvolvimento)
No arquivo `application.properties`, a chave está presente para facilitar o desenvolvimento inicial:
```properties
sendgrid.api.key=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
```

#### Configuração Recomendada (Produção)

**Método 1: Variáveis de Ambiente**
```bash
# Definir variável de ambiente
export SENDGRID_API_KEY=SG.your_real_api_key_here
export SENDGRID_FROM_EMAIL=noreply@slotfy.com
export SENDGRID_FROM_NAME="Slotfy - Sistema de Agendamento"
```

```properties
# application-prod.properties
sendgrid.api.key=${SENDGRID_API_KEY}
sendgrid.from.email=${SENDGRID_FROM_EMAIL:noreply@slotfy.com}
sendgrid.from.name=${SENDGRID_FROM_NAME:Slotfy - Sistema de Agendamento}
```

**Método 2: Arquivo .env (não commitar)**
1. Criar arquivo `.env` (adicionar ao `.gitignore`)
2. Usar bibliotecas como `dotenv` para carregar variáveis

**Método 3: Secrets Manager**
- AWS Secrets Manager
- Azure Key Vault
- Google Cloud Secret Manager
- HashiCorp Vault

### Arquivo de Exemplo para Produção
Um arquivo `application-prod.properties.example` foi criado mostrando a configuração segura usando variáveis de ambiente.

### Checklist de Segurança
- [ ] Remover chave da API do código antes de deploy em produção
- [ ] Configurar variáveis de ambiente no servidor
- [ ] Adicionar `.env` ao `.gitignore` se usado
- [ ] Rodar git secret scan para detectar chaves expostas
- [ ] Rotar a chave da API se ela foi exposta publicamente

### Rotação de Chave
Se a chave foi exposta:
1. Acesse o painel do SendGrid
2. Revogue a chave comprometida imediatamente
3. Gere uma nova chave
4. Atualize a variável de ambiente
5. Reinicie a aplicação

- A dependência `sendgrid-java:4.10.2` foi verificada e não possui vulnerabilidades conhecidas

## Próximos Passos (Recomendações)

1. **URGENTE - Segurança**: Mover a chave da API para variável de ambiente (não commitar no código)
   ```bash
   # Exemplo usando variável de ambiente
   export SENDGRID_API_KEY=SG.your_api_key_here
   ```
   ```properties
   # application.properties
   sendgrid.api.key=${SENDGRID_API_KEY}
   ```
2. Adicionar templates de email mais elaborados
3. Implementar filas para envio assíncrono em grande volume
4. Adicionar monitoramento de taxa de entrega
5. Criar dashboard para visualizar emails enviados
6. Implementar retry logic para falhas temporárias

## Exemplo de Uso

```java
@Autowired
private EmailService emailService;

// Enviar email simples
boolean sent = emailService.sendEmail(
    "cliente@example.com",
    "Bem-vindo ao Slotfy",
    "<html><body><h1>Bem-vindo!</h1></body></html>"
);

// Enviar email de redefinição de senha
boolean sent = emailService.sendPasswordResetEmail(
    "cliente@example.com",
    "https://localhost:8443/pages/reset-password.html?token=abc123"
);
```

## Troubleshooting

### Email não está sendo enviado

1. Verifique se a chave da API está correta
2. Verifique os logs do console para mensagens de erro
3. Confirme que o SendGrid está aceitando requisições (status 202)
4. Verifique se o email de destino é válido

### Email está na caixa de spam

1. Configure SPF, DKIM e DMARC no SendGrid
2. Use um domínio verificado
3. Evite palavras que ativam filtros de spam
4. Mantenha uma boa reputação de envio

## Referências

- [Documentação SendGrid Java](https://github.com/sendgrid/sendgrid-java)
- [SendGrid API Reference](https://docs.sendgrid.com/api-reference)
- [Best Practices SendGrid](https://docs.sendgrid.com/ui/sending-email/email-best-practices)
