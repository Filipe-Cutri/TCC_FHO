# Serviço de Sugestões de Agendamento com AWS Bedrock

## Visão Geral

Este módulo implementa um serviço de sugestões inteligentes de agendamento usando AWS Bedrock com modelos da Meta (Llama). O serviço analiza disponibilidades, bloqueios e preferências do usuário para gerar sugestões otimizadas de horários.

## Arquitetura

### Componentes Principais

1. **SchedulerController** (`/api/scheduler`)
   - Endpoint para gerar sugestões (`POST /suggest`)
   - Endpoint para confirmar agendamento (`POST /confirm`)

2. **SuggestionService**
   - Validação de entrada
   - Orquestração da lógica de negócio
   - Validação e filtragem de sugestões

3. **BedrockService**
   - Integração com AWS Bedrock Runtime
   - Construção de prompts
   - Parse de respostas JSON do modelo
   - Retry com exponential backoff

4. **DTOs**
   - `SchedulerSuggestRequest`: Parâmetros de entrada
   - `SchedulerSuggestResponse`: Lista de sugestões
   - `SchedulerConfirmRequest`: Confirmação de agendamento

## Configuração

### Variáveis de Ambiente

Configure as seguintes variáveis de ambiente antes de executar a aplicação:

```bash
# AWS Configuration
AWS_REGION=us-east-1
BEDROCK_MODEL_ID=meta.llama3-70b-instruct-v1:0
AWS_ACCESS_KEY_ID=sua_access_key
AWS_SECRET_ACCESS_KEY=sua_secret_key
```

**Nota**: Se estiver usando IAM roles (recomendado para produção), não é necessário configurar `AWS_ACCESS_KEY_ID` e `AWS_SECRET_ACCESS_KEY`.

### Arquivo application.properties

As configurações padrão estão em `src/main/resources/application.properties`:

```properties
# AWS Bedrock Configuration
aws.region=${AWS_REGION:us-east-1}
bedrock.model.id=${BEDROCK_MODEL_ID:meta.llama3-70b-instruct-v1:0}
aws.access.key.id=${AWS_ACCESS_KEY_ID:}
aws.secret.access.key=${AWS_SECRET_ACCESS_KEY:}
```

## API Endpoints

### POST /api/scheduler/suggest

Gera sugestões de horários para agendamento.

#### Request Body

```json
{
  "userId": "string",
  "timezone": "America/Sao_Paulo",
  "duration": 60,
  "buffer": 10,
  "availableWindows": [
    {
      "start": "2025-11-03T08:00:00-03:00",
      "end": "2025-11-03T18:00:00-03:00"
    }
  ],
  "busySlots": [
    {
      "start": "2025-11-03T12:00:00-03:00",
      "end": "2025-11-03T13:00:00-03:00"
    }
  ],
  "preferences": "Prefiro horários pela manhã",
  "maxSuggestions": 3
}
```

#### Parâmetros

- **userId** (obrigatório): ID do usuário
- **timezone** (obrigatório): Timezone IANA (ex: "America/Sao_Paulo", "UTC")
- **duration** (obrigatório): Duração da reunião em minutos
- **buffer** (opcional): Tempo de buffer antes/depois em minutos (padrão: 0)
- **availableWindows** (opcional): Janelas de disponibilidade. Se vazio, considera 00:00-23:59
- **busySlots** (opcional): Bloqueios de agenda existentes
- **preferences** (opcional): Texto livre com preferências do usuário
- **maxSuggestions** (opcional): Número máximo de sugestões (padrão: 3)

#### Response

```json
{
  "suggestions": [
    {
      "start": "2025-11-03T09:00:00-03:00",
      "end": "2025-11-03T10:00:00-03:00",
      "reason": "Horário matinal conforme preferência",
      "score": 0.95
    }
  ]
}
```

#### Exemplos de Uso

```bash
# Exemplo básico
curl -X POST https://localhost:8443/api/scheduler/suggest \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "timezone": "America/Sao_Paulo",
    "duration": 60,
    "buffer": 10,
    "preferences": "Prefiro manhã",
    "maxSuggestions": 3
  }'

# Com janelas disponíveis e bloqueios
curl -X POST https://localhost:8443/api/scheduler/suggest \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "timezone": "America/Sao_Paulo",
    "duration": 60,
    "availableWindows": [
      {
        "start": "2025-11-03T08:00:00-03:00",
        "end": "2025-11-03T12:00:00-03:00"
      }
    ],
    "busySlots": [
      {
        "start": "2025-11-03T10:00:00-03:00",
        "end": "2025-11-03T11:00:00-03:00"
      }
    ]
  }'
```

### POST /api/scheduler/confirm

Confirma um agendamento selecionado (stub - integração futura).

#### Request Body

```json
{
  "userId": "user123",
  "start": "2025-11-03T09:00:00-03:00",
  "end": "2025-11-03T10:00:00-03:00"
}
```

#### Response

```json
{
  "success": true,
  "message": "Appointment confirmed successfully"
}
```

## Regras de Negócio

O serviço implementa as seguintes regras de validação:

1. **Não conflitar com busySlots**: Sugestões não devem sobrepor slots ocupados, considerando o buffer
2. **Respeitar availableWindows**: Se especificado, sugestões devem estar completamente dentro das janelas disponíveis
3. **Duração adequada**: Cada sugestão deve ter duração >= duration especificado
4. **Horário comercial**: Por padrão, evita sugerir fora de 09:00-18:00, salvo se preferences indicar o contrário
5. **Ordenação por score**: Sugestões são ordenadas da melhor (maior score) para a pior

## Testes

### Executar Testes Unitários

```bash
cd back-end
./gradlew test
```

### Testes Específicos

```bash
# Apenas BedrockService
./gradlew test --tests BedrockServiceTest

# Apenas SuggestionService
./gradlew test --tests SuggestionServiceTest
```

### Cobertura de Testes

Os testes cobrem:

- ✅ Validação de entrada (campos obrigatórios, formatos)
- ✅ Construção de prompts
- ✅ Parsing de respostas JSON
- ✅ Filtragem de conflitos com busySlots
- ✅ Validação de availableWindows
- ✅ Ordenação por score
- ✅ Limite de maxSuggestions
- ✅ Tratamento de erros e respostas inválidas

## Frontend Demo

Uma página HTML de demonstração está disponível em:

```
/front-end/src/pages/scheduler-demo.html
```

### Como Usar o Demo

1. Inicie o backend:
   ```bash
   cd back-end
   ./gradlew bootRun
   ```

2. Abra o arquivo HTML em um navegador ou sirva via servidor web

3. Preencha os campos do formulário:
   - User ID
   - Timezone
   - Duração e buffer
   - Preferências (opcional)

4. Clique em "Buscar Sugestões"

5. Selecione uma das sugestões apresentadas

6. Clique em "Confirmar Seleção" para testar o endpoint de confirmação

## Segurança

### Tratamento de Credenciais

- ✅ Credenciais AWS são carregadas de variáveis de ambiente
- ✅ Não armazena credenciais em código ou logs
- ✅ Suporta IAM roles para ambientes de produção

### Logging

- ✅ Logs não contêm informações sensíveis (PII)
- ✅ Apenas metadados são registrados
- ✅ Níveis de log apropriados (DEBUG, INFO, WARN, ERROR)

### Validação

- ✅ Validação rigorosa de entrada
- ✅ Sanitização de dados antes de enviar ao modelo
- ✅ Validação de saída do modelo antes de retornar ao cliente

## Retry e Resiliência

O BedrockService implementa retry com exponential backoff:

- Máximo de 3 tentativas
- Backoff inicial: 1 segundo
- Backoff exponencial: 2^attempt segundos
- Timeout configurável

## Prompt Engineering

O prompt enviado ao modelo Bedrock segue as seguintes diretrizes:

1. Instruções claras para retornar apenas JSON
2. Formato de saída especificado explicitamente
3. Regras de negócio documentadas
4. Contexto completo (timezone, duração, janelas, bloqueios)
5. Exemplos de formato esperado

## Limitações Conhecidas

1. **Endpoint /confirm é stub**: A integração real com calendário será implementada em iteração futura
2. **Sem persistência**: Sugestões não são armazenadas em banco de dados
3. **Sem autenticação específica**: Usa o sistema de autenticação existente da aplicação
4. **Modelo específico**: Atualmente configurado para Meta Llama 3, mas pode ser adaptado para outros modelos

## Próximos Passos

- [ ] Integração real com Google Calendar / Microsoft Outlook
- [ ] Persistência de sugestões e confirmações
- [ ] Análise de histórico para melhorar sugestões
- [ ] Suporte a múltiplos participantes
- [ ] Notificações de agendamento
- [ ] Dashboard de métricas

## Troubleshooting

### Erro: "Failed to invoke Bedrock"

**Causa**: Credenciais AWS inválidas ou modelo não disponível

**Solução**: 
- Verifique as variáveis de ambiente AWS_ACCESS_KEY_ID e AWS_SECRET_ACCESS_KEY
- Confirme que a região AWS está correta
- Verifique se o modelo ID está correto para sua conta

### Erro: "Invalid timezone format"

**Causa**: Timezone fornecido não é um valor IANA válido

**Solução**: Use timezones IANA padrão como:
- America/Sao_Paulo
- America/New_York
- Europe/London
- UTC

### Nenhuma sugestão retornada

**Causa**: Critérios muito restritivos (muitos bloqueios ou janelas pequenas)

**Solução**:
- Verifique se availableWindows são grandes o suficiente para duration + buffers
- Verifique se busySlots não ocupam todo o período disponível
- Reduza o buffer ou duration

## Suporte

Para questões ou problemas:
- Abra uma issue no repositório
- Consulte a documentação do AWS Bedrock
- Revise os logs da aplicação para detalhes de erro

## Licença

Este código é parte do projeto TCC_FHO e segue a mesma licença do projeto principal.
