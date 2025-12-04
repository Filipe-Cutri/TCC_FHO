# ==============================================================================
# Script de Configuração do Amazon Bedrock para Slotfy (Windows PowerShell)
# ==============================================================================
# 
# Este script ajuda a configurar as variáveis de ambiente do Amazon Bedrock
# decodificando as credenciais fornecidas no formato base64.
#
# Uso:
#   .\setup_bedrock.ps1
#
# ==============================================================================

# Cores para output
function Write-ColorOutput($ForegroundColor) {
    $fc = $host.UI.RawUI.ForegroundColor
    $host.UI.RawUI.ForegroundColor = $ForegroundColor
    if ($args) {
        Write-Output $args
    }
    $host.UI.RawUI.ForegroundColor = $fc
}

# Banner
Write-Host ""
Write-ColorOutput Cyan "╔════════════════════════════════════════════════════════════╗"
Write-ColorOutput Cyan "║                                                            ║"
Write-ColorOutput Cyan "║     Configuração do Amazon Bedrock para Slotfy            ║"
Write-ColorOutput Cyan "║                                                            ║"
Write-ColorOutput Cyan "╚════════════════════════════════════════════════════════════╝"
Write-Host ""

# Verificar se está no diretório correto
if (!(Test-Path "build.gradle")) {
    Write-ColorOutput Red "❌ Erro: Este script deve ser executado no diretório back-end"
    Write-Host "   Navegue para o diretório back-end e execute novamente."
    exit 1
}

# Credenciais fornecidas (base64) - FORNECIDAS PELO USUÁRIO
# 
# IMPORTANTE: Este valor padrão contém as credenciais fornecidas no problema original.
# Se você é o proprietário original dessas credenciais, use-as diretamente.
# Caso contrário, substitua pela variável de ambiente BEDROCK_ENCODED_KEY com suas próprias credenciais.
#
# Para usar suas próprias credenciais:
#   $env:BEDROCK_ENCODED_KEY="sua-chave-base64"; .\setup_bedrock.ps1
#
# Para usar credenciais de teste/desenvolvimento:
#   Deixe vazio e o script usará as credenciais fornecidas no projeto
#
$EncodedKey = if ($env:BEDROCK_ENCODED_KEY) { $env:BEDROCK_ENCODED_KEY } else { "ABSKQmVkcm9ja0FQSUtleS1kNWhlLWF0LTcxNTIyNzg4MjczNDovT2Z3UFNhVjlxRE1iajBKZ29ZU1lxZEhXdmVaZllnRUVITys3V0RYcDhpNzhpZzJwTG5OdlNJNGxEYz0=" }

Write-ColorOutput Yellow "📦 Decodificando credenciais do Amazon Bedrock..."

# Decodificar base64
$DecodedBytes = [System.Convert]::FromBase64String($EncodedKey)
$Decoded = [System.Text.Encoding]::UTF8.GetString($DecodedBytes)

# Remover caracteres não imprimíveis
$DecodedClean = $Decoded -replace '[^\x20-\x7E]', ''

# Extrair Access Key ID e Secret Access Key
$Parts = $DecodedClean.Split(':')
$AwsAccessKeyId = $Parts[0].Trim()
$AwsSecretAccessKey = $Parts[1].Trim()

# Configurações padrão
$AwsRegion = "us-east-1"
$BedrockModelId = "meta.llama3-70b-instruct-v1:0"

Write-ColorOutput Green "✅ Credenciais decodificadas com sucesso!"
Write-Host ""

# Mostrar informações (parcialmente ocultas)
Write-ColorOutput Cyan "📋 Informações extraídas:"
Write-Host "   AWS Access Key ID: $($AwsAccessKeyId.Substring(0, 20))..."
Write-Host "   AWS Secret Access Key: $($AwsSecretAccessKey.Substring(0, 10))...$($AwsSecretAccessKey.Substring($AwsSecretAccessKey.Length - 5))"
Write-Host "   AWS Region: $AwsRegion"
Write-Host "   Bedrock Model ID: $BedrockModelId"
Write-Host ""

# Perguntar ao usuário o que fazer
Write-ColorOutput Yellow "🔧 Como deseja configurar as variáveis?"
Write-Host "   1) Criar arquivo .env (para desenvolvimento local)"
Write-Host "   2) Mostrar comandos PowerShell (para terminal atual)"
Write-Host "   3) Mostrar configuração Railway (copiar/colar no Railway)"
Write-Host "   4) Sair sem configurar"
Write-Host ""
$choice = Read-Host "Escolha uma opção (1-4)"

switch ($choice) {
    "1" {
        $EnvFile = ".env"
        
        # Verificar se .env já existe
        if (Test-Path $EnvFile) {
            Write-Host ""
            $overwrite = Read-Host "⚠️  Arquivo .env já existe. Sobrescrever? (s/N)"
            if ($overwrite -notmatch "^[Ss]$") {
                Write-ColorOutput Yellow "ℹ️  Operação cancelada."
                exit 0
            }
        }
        
        # Criar arquivo .env
        $envContent = @"
# Amazon Bedrock Configuration
# Gerado automaticamente em $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")
# ⚠️  NÃO COMMITE ESTE ARQUIVO!

AWS_ACCESS_KEY_ID=$AwsAccessKeyId
AWS_SECRET_ACCESS_KEY=$AwsSecretAccessKey
AWS_REGION=$AwsRegion
BEDROCK_MODEL_ID=$BedrockModelId

# Outras configurações necessárias
# Descomente e preencha conforme necessário:

# SENDGRID_API_KEY=SG.your-sendgrid-api-key
# SENDGRID_FROM=noreply@slotfy.com
# FRONTEND_URL=http://localhost:3000
# SPRING_PROFILES_ACTIVE=dev
"@
        
        $envContent | Out-File -FilePath $EnvFile -Encoding UTF8
        
        Write-Host ""
        Write-ColorOutput Green "✅ Arquivo .env criado com sucesso!"
        Write-ColorOutput Yellow "📝 Próximos passos:"
        Write-Host "   1. Edite o arquivo .env e adicione outras credenciais (SendGrid, etc.)"
        Write-Host "   2. Verifique se .env está no .gitignore"
        Write-Host "   3. Execute: .\gradlew.bat bootRun"
        Write-Host ""
    }
    
    "2" {
        Write-Host ""
        Write-ColorOutput Green "📋 Copie e execute os seguintes comandos no PowerShell:"
        Write-Host ""
        Write-Host "`$env:AWS_ACCESS_KEY_ID=`"$AwsAccessKeyId`""
        Write-Host "`$env:AWS_SECRET_ACCESS_KEY=`"$AwsSecretAccessKey`""
        Write-Host "`$env:AWS_REGION=`"$AwsRegion`""
        Write-Host "`$env:BEDROCK_MODEL_ID=`"$BedrockModelId`""
        Write-Host ""
        Write-ColorOutput Yellow "ℹ️  Nota: Estas variáveis estarão disponíveis apenas na sessão atual do PowerShell."
        Write-Host ""
    }
    
    "3" {
        Write-Host ""
        Write-ColorOutput Green "📋 Configure as seguintes variáveis no Railway:"
        Write-Host ""
        Write-Host "AWS_ACCESS_KEY_ID=$AwsAccessKeyId"
        Write-Host "AWS_SECRET_ACCESS_KEY=$AwsSecretAccessKey"
        Write-Host "AWS_REGION=$AwsRegion"
        Write-Host "BEDROCK_MODEL_ID=$BedrockModelId"
        Write-Host ""
        Write-ColorOutput Yellow "📝 Como configurar no Railway:"
        Write-Host "   1. Acesse: https://railway.app/dashboard"
        Write-Host "   2. Selecione seu projeto"
        Write-Host "   3. Vá para a aba 'Variables'"
        Write-Host "   4. Adicione cada variável acima"
        Write-Host "   5. Faça redeploy do serviço"
        Write-Host ""
    }
    
    "4" {
        Write-Host ""
        Write-ColorOutput Yellow "ℹ️  Operação cancelada."
        exit 0
    }
    
    default {
        Write-Host ""
        Write-ColorOutput Red "❌ Opção inválida."
        exit 1
    }
}

# Verificar se .gitignore contém .env
if (Test-Path "../.gitignore") {
    $gitignoreContent = Get-Content "../.gitignore"
    if ($gitignoreContent -notcontains ".env") {
        Write-ColorOutput Yellow "⚠️  ATENÇÃO: .env não está no .gitignore!"
        Write-Host "   Adicione '.env' ao arquivo .gitignore para evitar commit acidental."
        Write-Host ""
    }
}

Write-ColorOutput Cyan "📚 Para mais informações, consulte:"
Write-Host "   - docs/AMAZON_BEDROCK_SETUP.md (Guia completo)"
Write-Host "   - docs/BEDROCK_CREDENTIALS_REFERENCE.md (Referência rápida)"
Write-Host ""

Write-ColorOutput Green "✨ Configuração concluída!"
Write-Host ""
