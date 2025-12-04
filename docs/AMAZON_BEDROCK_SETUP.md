# Amazon Bedrock API Setup Guide

Este guia explica como configurar e implementar a API do Amazon Bedrock no sistema Slotfy para funcionalidades de IA em agendamento inteligente.

## 📋 Pré-requisitos

- Conta AWS com acesso ao Amazon Bedrock
- Credenciais AWS (Access Key ID e Secret Access Key)
- Permissões para usar o Amazon Bedrock Runtime

## 🔑 Credenciais AWS

O sistema requer as seguintes credenciais AWS para funcionar:

1. **AWS Access Key ID**: Identificador da chave de acesso
2. **AWS Secret Access Key**: Chave secreta de acesso
3. **AWS Region**: Região onde o Bedrock está disponível (padrão: `us-east-1`)
4. **Bedrock Model ID**: ID do modelo a ser usado (padrão: `meta.llama3-70b-instruct-v1:0`)

### Como obter as credenciais

1. Acesse o [AWS Console](https://console.aws.amazon.com/)
2. Navegue até IAM (Identity and Access Management)
3. Crie um novo usuário ou use um existente
4. Gere credenciais de acesso (Access Key)
5. Anote o **Access Key ID** e o **Secret Access Key**

**⚠️ IMPORTANTE**: Nunca compartilhe ou commite suas credenciais no código fonte!

## 🔧 Configuração do Ambiente

### Desenvolvimento Local

Para desenvolvimento local, configure as variáveis de ambiente no seu sistema ou no arquivo de configuração do Spring Boot.

#### Opção 1: Variáveis de Ambiente do Sistema

No Linux/Mac:
```bash
export AWS_ACCESS_KEY_ID="sua-access-key-id"
export AWS_SECRET_ACCESS_KEY="sua-secret-access-key"
export AWS_REGION="us-east-1"
export BEDROCK_MODEL_ID="meta.llama3-70b-instruct-v1:0"
```

No Windows (PowerShell):
```powershell
$env:AWS_ACCESS_KEY_ID="sua-access-key-id"
$env:AWS_SECRET_ACCESS_KEY="sua-secret-access-key"
$env:AWS_REGION="us-east-1"
$env:BEDROCK_MODEL_ID="meta.llama3-70b-instruct-v1:0"
```

#### Opção 2: application-dev.properties

Adicione as seguintes linhas ao arquivo `back-end/src/main/resources/application-dev.properties`:

```properties
# AWS Bedrock Configuration (NÃO COMMITTAR ESTAS CREDENCIAIS!)
aws.access.key.id=sua-access-key-id
aws.secret.access.key=sua-secret-access-key
aws.region=us-east-1
bedrock.model.id=meta.llama3-70b-instruct-v1:0
```

**⚠️ ATENÇÃO**: Certifique-se de que `application-dev.properties` está no `.gitignore` se você adicionar credenciais reais.

### Produção (Railway)

No Railway, configure as variáveis de ambiente através do dashboard:

1. Acesse o projeto no Railway
2. Selecione o serviço do backend
3. Vá para a aba "Variables"
4. Adicione as seguintes variáveis:

```
AWS_ACCESS_KEY_ID=sua-access-key-id
AWS_SECRET_ACCESS_KEY=sua-secret-access-key
AWS_REGION=us-east-1
BEDROCK_MODEL_ID=meta.llama3-70b-instruct-v1:0
```

## 📝 Formato das Credenciais Fornecidas

Se você recebeu credenciais no formato base64 (como `Amazon_Bedrock=ABSKQm...`), você precisa decodificá-las primeiro:

### Como decodificar

```bash
echo "SUA_CREDENCIAL_BASE64_AQUI" | base64 -d
```

Exemplo de formato após decodificação:
```
AccessKeyId:SecretAccessKey
```

As credenciais originais fornecidas foram:
```
Amazon_Bedrock=ABSKQmVkcm9ja0FQSUtleS1kNWhlLWF0LTcxNTIyNzg4MjczNDovT2Z3UFNhVjlxRE1iajBKZ29ZU1lxZEhXdmVaZllnRUVITys3V0RYcDhpNzhpZzJwTG5OdlNJNGxEYz0=
```

Que ao serem decodificadas resultam em:
- **Access Key ID**: `BedrockAPIKey-d5he-at-715227882734`
- **Secret Access Key**: `/OfwPSaV9qDMbj0JgoYSYqdHWveZfYgEEHO+7WDXp8i78ig2pLnNvSI4lDc=`

⚠️ **NOTA DE SEGURANÇA**: Estas credenciais são fornecidas como exemplo. Para produção, use suas próprias credenciais AWS válidas obtidas através do IAM Console.

### Configurando no Railway com credenciais personalizadas

No dashboard do Railway, configure com suas credenciais AWS reais:

```
AWS_ACCESS_KEY_ID=your-aws-access-key-id
AWS_SECRET_ACCESS_KEY=your-aws-secret-access-key
AWS_REGION=us-east-1
BEDROCK_MODEL_ID=meta.llama3-70b-instruct-v1:0
```

## 🧪 Testando a Configuração

### Verificar se as variáveis estão configuradas

No backend, você pode verificar se as variáveis foram carregadas corretamente através dos logs:

```bash
./gradlew bootRun
```

Procure por mensagens indicando que o BedrockService foi inicializado.

### Testar o endpoint de sugestões

Faça uma requisição POST para o endpoint de sugestões:

```bash
curl -X POST https://seu-backend.railway.app/api/scheduler/suggest \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "test-user",
    "timezone": "America/Sao_Paulo",
    "duration": 60,
    "buffer": 10,
    "maxSuggestions": 3,
    "availableWindows": [
      {
        "start": "2025-12-05T08:00:00-03:00",
        "end": "2025-12-05T18:00:00-03:00"
      }
    ],
    "busySlots": [],
    "preferences": "Preferência por horários de manhã"
  }'
```

## 🔍 Troubleshooting

### Erro: "AWS credentials not configured"

**Solução**: Verifique se as variáveis `AWS_ACCESS_KEY_ID` e `AWS_SECRET_ACCESS_KEY` estão configuradas corretamente.

### Erro: "AccessDeniedException"

**Solução**: Verifique se o usuário IAM tem permissões para acessar o Amazon Bedrock. A política deve incluir:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "bedrock:InvokeModel",
        "bedrock:InvokeModelWithResponseStream"
      ],
      "Resource": "*"
    }
  ]
}
```

### Erro: "Model not found"

**Solução**: Verifique se o `BEDROCK_MODEL_ID` está correto e se o modelo está disponível na região configurada.

### Erro: "Region not available"

**Solução**: Amazon Bedrock não está disponível em todas as regiões. Regiões suportadas incluem:
- `us-east-1` (N. Virginia)
- `us-west-2` (Oregon)
- `ap-southeast-1` (Singapore)
- `eu-central-1` (Frankfurt)

## 📊 Monitoramento

### Logs importantes

O sistema registra as seguintes informações nos logs:

- Início de invocação do Bedrock
- Tentativas de retry em caso de falha
- Tempo de resposta
- Erros e exceções

### Métricas

Monitore as seguintes métricas no AWS CloudWatch:

- Número de invocações
- Latência
- Erros
- Custos

## 💰 Custos

Amazon Bedrock cobra por:
- Número de tokens processados (input + output)
- Modelo utilizado

**Modelos disponíveis e custos estimados:**
- Meta Llama 3 70B: ~$0.00195/1K tokens input, ~$0.00256/1K tokens output
- Claude 3 Sonnet: ~$0.003/1K tokens input, ~$0.015/1K tokens output

**⚠️ IMPORTANTE**: Configure limites de gastos no AWS para evitar custos inesperados.

## 🔐 Segurança

### Boas práticas

1. **Nunca commite credenciais no código**
2. Use variáveis de ambiente para todas as credenciais
3. Rotacione as credenciais regularmente
4. Use IAM roles com permissões mínimas necessárias
5. Monitore o uso através do CloudTrail
6. Configure alertas de gastos no AWS Billing

### Rotação de credenciais

Para rotacionar credenciais:

1. Crie um novo Access Key no IAM
2. Atualize as variáveis de ambiente no Railway
3. Teste a nova configuração
4. Desative a chave antiga
5. Após confirmação, delete a chave antiga

## 📚 Recursos Adicionais

- [Documentação AWS Bedrock](https://docs.aws.amazon.com/bedrock/)
- [AWS SDK for Java](https://docs.aws.amazon.com/sdk-for-java/)
- [Meta Llama 3 Model Documentation](https://docs.aws.amazon.com/bedrock/latest/userguide/model-parameters-meta.html)
- [Railway Documentation](https://docs.railway.app/)

## 🆘 Suporte

Em caso de dúvidas ou problemas:

1. Verifique os logs do aplicativo
2. Consulte a documentação da AWS
3. Verifique as issues no GitHub do projeto
4. Entre em contato com o suporte da AWS se necessário
