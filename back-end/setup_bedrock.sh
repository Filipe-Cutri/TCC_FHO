#!/bin/bash

# ==============================================================================
# Script de Configuração do Amazon Bedrock para Slotfy
# ==============================================================================
# 
# Este script ajuda a configurar as variáveis de ambiente do Amazon Bedrock
# decodificando as credenciais fornecidas no formato base64.
#
# Uso:
#   ./setup_bedrock.sh
#
# ==============================================================================

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Banner
echo ""
echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║                                                            ║${NC}"
echo -e "${BLUE}║     Configuração do Amazon Bedrock para Slotfy            ║${NC}"
echo -e "${BLUE}║                                                            ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

# Verificar se está no diretório correto
if [ ! -f "build.gradle" ]; then
    echo -e "${RED}❌ Erro: Este script deve ser executado no diretório back-end${NC}"
    echo "   Navegue para o diretório back-end e execute novamente."
    exit 1
fi

# Credenciais fornecidas (base64) - PADRÃO
# Este valor pode ser substituído pela variável de ambiente BEDROCK_ENCODED_KEY
# Exemplo de uso: BEDROCK_ENCODED_KEY="sua-chave-aqui" ./setup_bedrock.sh
ENCODED_KEY="${BEDROCK_ENCODED_KEY:-ABSKQmVkcm9ja0FQSUtleS1kNWhlLWF0LTcxNTIyNzg4MjczNDovT2Z3UFNhVjlxRE1iajBKZ29ZU1lxZEhXdmVaZllnRUVITys3V0RYcDhpNzhpZzJwTG5OdlNJNGxEYz0=}"

echo -e "${YELLOW}📦 Decodificando credenciais do Amazon Bedrock...${NC}"

# Decodificar base64
DECODED=$(echo "$ENCODED_KEY" | base64 -d)

# Remover caracteres não imprimíveis
DECODED_CLEAN=$(echo "$DECODED" | tr -d '\000-\037' | tr -d '\177-\377')

# Extrair Access Key ID e Secret Access Key
AWS_ACCESS_KEY_ID=$(echo "$DECODED_CLEAN" | cut -d':' -f1)
AWS_SECRET_ACCESS_KEY=$(echo "$DECODED_CLEAN" | cut -d':' -f2)

# Configurações padrão
AWS_REGION="us-east-1"
BEDROCK_MODEL_ID="meta.llama3-70b-instruct-v1:0"

echo -e "${GREEN}✅ Credenciais decodificadas com sucesso!${NC}"
echo ""

# Mostrar informações (parcialmente ocultas)
echo -e "${BLUE}📋 Informações extraídas:${NC}"
echo "   AWS Access Key ID: ${AWS_ACCESS_KEY_ID:0:20}..."
echo "   AWS Secret Access Key: ${AWS_SECRET_ACCESS_KEY:0:10}...${AWS_SECRET_ACCESS_KEY: -5}"
echo "   AWS Region: $AWS_REGION"
echo "   Bedrock Model ID: $BEDROCK_MODEL_ID"
echo ""

# Perguntar ao usuário o que fazer
echo -e "${YELLOW}🔧 Como deseja configurar as variáveis?${NC}"
echo "   1) Criar arquivo .env (para desenvolvimento local)"
echo "   2) Mostrar comandos export (para terminal atual)"
echo "   3) Mostrar configuração Railway (copiar/colar no Railway)"
echo "   4) Sair sem configurar"
echo ""
read -p "Escolha uma opção (1-4): " choice

case $choice in
    1)
        ENV_FILE=".env"
        
        # Verificar se .env já existe
        if [ -f "$ENV_FILE" ]; then
            echo ""
            read -p "⚠️  Arquivo .env já existe. Sobrescrever? (s/N): " overwrite
            if [[ ! $overwrite =~ ^[Ss]$ ]]; then
                echo -e "${YELLOW}ℹ️  Operação cancelada.${NC}"
                exit 0
            fi
        fi
        
        # Criar arquivo .env
        cat > "$ENV_FILE" << EOF
# Amazon Bedrock Configuration
# Gerado automaticamente em $(date)
# ⚠️  NÃO COMMITE ESTE ARQUIVO!

AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY
AWS_REGION=$AWS_REGION
BEDROCK_MODEL_ID=$BEDROCK_MODEL_ID

# Outras configurações necessárias
# Descomente e preencha conforme necessário:

# SENDGRID_API_KEY=SG.your-sendgrid-api-key
# SENDGRID_FROM=noreply@slotfy.com
# FRONTEND_URL=http://localhost:3000
# SPRING_PROFILES_ACTIVE=dev
EOF
        
        echo ""
        echo -e "${GREEN}✅ Arquivo .env criado com sucesso!${NC}"
        echo -e "${YELLOW}📝 Próximos passos:${NC}"
        echo "   1. Edite o arquivo .env e adicione outras credenciais (SendGrid, etc.)"
        echo "   2. Verifique se .env está no .gitignore"
        echo "   3. Execute: ./gradlew bootRun"
        echo ""
        ;;
    
    2)
        echo ""
        echo -e "${GREEN}📋 Copie e execute os seguintes comandos no seu terminal:${NC}"
        echo ""
        echo "export AWS_ACCESS_KEY_ID=\"$AWS_ACCESS_KEY_ID\""
        echo "export AWS_SECRET_ACCESS_KEY=\"$AWS_SECRET_ACCESS_KEY\""
        echo "export AWS_REGION=\"$AWS_REGION\""
        echo "export BEDROCK_MODEL_ID=\"$BEDROCK_MODEL_ID\""
        echo ""
        echo -e "${YELLOW}ℹ️  Nota: Estas variáveis estarão disponíveis apenas na sessão atual do terminal.${NC}"
        echo ""
        ;;
    
    3)
        echo ""
        echo -e "${GREEN}📋 Configure as seguintes variáveis no Railway:${NC}"
        echo ""
        echo "AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID"
        echo "AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY"
        echo "AWS_REGION=$AWS_REGION"
        echo "BEDROCK_MODEL_ID=$BEDROCK_MODEL_ID"
        echo ""
        echo -e "${YELLOW}📝 Como configurar no Railway:${NC}"
        echo "   1. Acesse: https://railway.app/dashboard"
        echo "   2. Selecione seu projeto"
        echo "   3. Vá para a aba 'Variables'"
        echo "   4. Adicione cada variável acima"
        echo "   5. Faça redeploy do serviço"
        echo ""
        ;;
    
    4)
        echo ""
        echo -e "${YELLOW}ℹ️  Operação cancelada.${NC}"
        exit 0
        ;;
    
    *)
        echo ""
        echo -e "${RED}❌ Opção inválida.${NC}"
        exit 1
        ;;
esac

# Verificar se .gitignore contém .env
if [ -f "../.gitignore" ]; then
    if ! grep -q "^\.env$" ../.gitignore; then
        echo -e "${YELLOW}⚠️  ATENÇÃO: .env não está no .gitignore!${NC}"
        echo "   Adicione '.env' ao arquivo .gitignore para evitar commit acidental."
        echo ""
    fi
fi

echo -e "${BLUE}📚 Para mais informações, consulte:${NC}"
echo "   - docs/AMAZON_BEDROCK_SETUP.md (Guia completo)"
echo "   - docs/BEDROCK_CREDENTIALS_REFERENCE.md (Referência rápida)"
echo ""

echo -e "${GREEN}✨ Configuração concluída!${NC}"
echo ""
