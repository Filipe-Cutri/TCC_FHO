# Amazon Bedrock - Referência Rápida de Credenciais

⚠️ **IMPORTANTE**: Este arquivo contém informações sensíveis. NÃO deve ser commitado em produção com credenciais reais.

## Credenciais Fornecidas

As credenciais foram fornecidas no seguinte formato base64:

```
Amazon_Bedrock=ABSKQmVkcm9ja0FQSUtleS1kNWhlLWF0LTcxNTIyNzg4MjczNDovT2Z3UFNhVjlxRE1iajBKZ29ZU1lxZEhXdmVaZllnRUVITys3V0RYcDhpNzhpZzJwTG5OdlNJNGxEYz0=
```

## Decodificação

Após decodificar o base64, obtemos:

```
BedrockAPIKey-d5he-at-715227882734:/OfwPSaV9qDMbj0JgoYSYqdHWveZfYgEEHO+7WDXp8i78ig2pLnNvSI4lDc=
```

## Formato das Variáveis de Ambiente

### Para Railway (Produção)

Configure as seguintes variáveis no dashboard do Railway:

```env
AWS_ACCESS_KEY_ID=BedrockAPIKey-d5he-at-715227882734
AWS_SECRET_ACCESS_KEY=/OfwPSaV9qDMbj0JgoYSYqdHWveZfYgEEHO+7WDXp8i78ig2pLnNvSI4lDc=
AWS_REGION=us-east-1
BEDROCK_MODEL_ID=meta.llama3-70b-instruct-v1:0
```

### Para Desenvolvimento Local

No Linux/Mac (bash):
```bash
export AWS_ACCESS_KEY_ID="BedrockAPIKey-d5he-at-715227882734"
export AWS_SECRET_ACCESS_KEY="/OfwPSaV9qDMbj0JgoYSYqdHWveZfYgEEHO+7WDXp8i78ig2pLnNvSI4lDc="
export AWS_REGION="us-east-1"
export BEDROCK_MODEL_ID="meta.llama3-70b-instruct-v1:0"
```

No Windows (PowerShell):
```powershell
$env:AWS_ACCESS_KEY_ID="BedrockAPIKey-d5he-at-715227882734"
$env:AWS_SECRET_ACCESS_KEY="/OfwPSaV9qDMbj0JgoYSYqdHWveZfYgEEHO+7WDXp8i78ig2pLnNvSI4lDc="
$env:AWS_REGION="us-east-1"
$env:BEDROCK_MODEL_ID="meta.llama3-70b-instruct-v1:0"
```

## Script de Configuração Rápida

### Para ambiente de desenvolvimento (criar arquivo .env local)

Crie um arquivo `.env` na raiz do projeto backend (NÃO comitar este arquivo!):

```bash
# .env - NÃO COMMITTAR ESTE ARQUIVO!
AWS_ACCESS_KEY_ID=BedrockAPIKey-d5he-at-715227882734
AWS_SECRET_ACCESS_KEY=/OfwPSaV9qDMbj0JgoYSYqdHWveZfYgEEHO+7WDXp8i78ig2pLnNvSI4lDc=
AWS_REGION=us-east-1
BEDROCK_MODEL_ID=meta.llama3-70b-instruct-v1:0
```

## Verificação da Configuração

### Teste 1: Verificar se as variáveis foram carregadas

```bash
cd back-end
./gradlew bootRun
```

Nos logs, você deve ver mensagens indicando que o BedrockService foi inicializado.

### Teste 2: Testar o endpoint

```bash
curl -X POST http://localhost:8443/api/scheduler/suggest \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "test-user",
    "timezone": "America/Sao_Paulo",
    "duration": 60,
    "buffer": 10,
    "maxSuggestions": 3,
    "preferences": "Preferência por horários de manhã"
  }'
```

## Integração Existente

O sistema já possui:

✅ **BedrockService**: Serviço implementado em `/back-end/src/main/java/com/slotfy/service/BedrockService.java`
- Suporta retry automático com backoff exponencial
- Integração com Meta Llama 3 70B
- Parsing robusto de respostas JSON
- Logging detalhado

✅ **SuggestionService**: Serviço de validação em `/back-end/src/main/java/com/slotfy/service/SuggestionService.java`
- Valida sugestões contra regras de negócio
- Verifica conflitos com slots ocupados
- Respeita janelas de disponibilidade

✅ **SchedulerController**: Endpoint REST em `/back-end/src/main/java/com/slotfy/controller/SchedulerController.java`
- Endpoint POST `/api/scheduler/suggest`
- Endpoint POST `/api/scheduler/confirm`
- Tratamento de erros robusto

✅ **Testes**: Suite completa de testes em `/back-end/src/test/java/com/slotfy/service/BedrockServiceTest.java`

## Próximos Passos

1. ✅ Configurar variáveis de ambiente (Railway ou local)
2. ✅ Reiniciar o serviço backend
3. ✅ Testar o endpoint `/api/scheduler/suggest`
4. ✅ Integrar com o frontend (se necessário)
5. ⚠️ Monitorar custos no AWS Console
6. ⚠️ Configurar alertas de gastos

## Dependências

O projeto já possui todas as dependências necessárias configuradas em `build.gradle`:

```gradle
implementation platform('software.amazon.awssdk:bom:2.21.0')
implementation 'software.amazon.awssdk:bedrockruntime'
implementation 'com.fasterxml.jackson.core:jackson-databind:2.15.2'
```

## Configuração no application.properties

As configurações já estão presentes em `/back-end/src/main/resources/application.properties`:

```properties
# AWS Bedrock Configuration
aws.region=${AWS_REGION:us-east-1}
bedrock.model.id=${BEDROCK_MODEL_ID:meta.llama3-70b-instruct-v1:0}
aws.access.key.id=${AWS_ACCESS_KEY_ID:}
aws.secret.access.key=${AWS_SECRET_ACCESS_KEY:}
```

## Segurança

⚠️ **ATENÇÃO**: 
- NUNCA commite credenciais AWS no código
- Use sempre variáveis de ambiente
- Adicione `.env` ao `.gitignore`
- Rotacione credenciais regularmente
- Configure limites de gastos no AWS

## Suporte

Para mais informações, consulte:
- [Guia Completo de Setup](./AMAZON_BEDROCK_SETUP.md)
- [Documentação AWS Bedrock](https://docs.aws.amazon.com/bedrock/)
- [Railway Configuration Guide](./deployment/RAILWAY_CONFIGURATION.md)
