#!/bin/bash

# Script de Verificação do Sistema de Upload de Imagens

echo "==================================="
echo "Verificação do Sistema de Upload"
echo "==================================="
echo ""

# 1. Verificar estrutura de diretórios
echo "1. Verificando estrutura de arquivos..."
echo "   Backend:"
ls -la back-end/src/main/java/com/slotfy/service/FileStorageService.java 2>/dev/null && echo "   ✓ FileStorageService.java" || echo "   ✗ FileStorageService.java"
ls -la back-end/src/main/java/com/slotfy/controller/FileUploadController.java 2>/dev/null && echo "   ✓ FileUploadController.java" || echo "   ✗ FileUploadController.java"
ls -la back-end/src/main/java/com/slotfy/config/FileUploadConfig.java 2>/dev/null && echo "   ✓ FileUploadConfig.java" || echo "   ✗ FileUploadConfig.java"

echo ""
echo "   Frontend:"
ls -la front-end/src/pages/establishment/establishment-services.html 2>/dev/null && echo "   ✓ establishment-services.html" || echo "   ✗ establishment-services.html"
ls -la front-end/src/pages/establishment/establishment-professionals.html 2>/dev/null && echo "   ✓ establishment-professionals.html" || echo "   ✗ establishment-professionals.html"
ls -la front-end/src/assets/js/establishment-services.js 2>/dev/null && echo "   ✓ establishment-services.js" || echo "   ✗ establishment-services.js"
ls -la front-end/src/assets/js/establishment-professionals.js 2>/dev/null && echo "   ✓ establishment-professionals.js" || echo "   ✗ establishment-professionals.js"

echo ""
echo "   Testes:"
ls -la back-end/src/test/java/com/slotfy/service/FileStorageServiceTest.java 2>/dev/null && echo "   ✓ FileStorageServiceTest.java" || echo "   ✗ FileStorageServiceTest.java"
ls -la back-end/src/test/java/com/slotfy/controller/FileUploadControllerTest.java 2>/dev/null && echo "   ✓ FileUploadControllerTest.java" || echo "   ✗ FileUploadControllerTest.java"

echo ""
echo "   Documentação:"
ls -la IMAGE_UPLOAD_USER_GUIDE.md 2>/dev/null && echo "   ✓ IMAGE_UPLOAD_USER_GUIDE.md" || echo "   ✗ IMAGE_UPLOAD_USER_GUIDE.md"
ls -la IMAGE_UPLOAD_TECHNICAL_DOC.md 2>/dev/null && echo "   ✓ IMAGE_UPLOAD_TECHNICAL_DOC.md" || echo "   ✗ IMAGE_UPLOAD_TECHNICAL_DOC.md"

# 2. Verificar configuração
echo ""
echo "2. Verificando configuração do backend..."
if grep -q "spring.servlet.multipart.enabled=true" back-end/src/main/resources/application.properties; then
    echo "   ✓ Multipart upload habilitado"
else
    echo "   ✗ Multipart upload não configurado"
fi

if grep -q "spring.servlet.multipart.max-file-size=5MB" back-end/src/main/resources/application.properties; then
    echo "   ✓ Limite de tamanho configurado (5MB)"
else
    echo "   ✗ Limite de tamanho não configurado"
fi

if grep -q "file.upload-dir=uploads" back-end/src/main/resources/application.properties; then
    echo "   ✓ Diretório de upload configurado"
else
    echo "   ✗ Diretório de upload não configurado"
fi

# 3. Verificar .gitignore
echo ""
echo "3. Verificando .gitignore..."
if grep -q "uploads/" .gitignore; then
    echo "   ✓ Diretório uploads/ ignorado no git"
else
    echo "   ✗ Diretório uploads/ não está no .gitignore"
fi

# 4. Verificar formulários HTML
echo ""
echo "4. Verificando formulários HTML..."
if grep -q 'type="file"' front-end/src/pages/establishment/establishment-services.html; then
    echo "   ✓ Input de arquivo presente em services.html"
else
    echo "   ✗ Input de arquivo não encontrado em services.html"
fi

if grep -q 'type="file"' front-end/src/pages/establishment/establishment-professionals.html; then
    echo "   ✓ Input de arquivo presente em professionals.html"
else
    echo "   ✗ Input de arquivo não encontrado em professionals.html"
fi

if grep -q 'accept="image/jpeg,image/jpg,image/png"' front-end/src/pages/establishment/establishment-services.html; then
    echo "   ✓ Restrição de tipo de arquivo configurada (services)"
else
    echo "   ✗ Restrição de tipo não configurada (services)"
fi

if grep -q 'accept="image/jpeg,image/jpg,image/png"' front-end/src/pages/establishment/establishment-professionals.html; then
    echo "   ✓ Restrição de tipo de arquivo configurada (professionals)"
else
    echo "   ✗ Restrição de tipo não configurada (professionals)"
fi

# 5. Verificar JavaScript
echo ""
echo "5. Verificando JavaScript..."
if grep -q "uploadProfessionalImageFile" front-end/src/assets/js/establishment-professionals.js; then
    echo "   ✓ Função de upload implementada (professionals)"
else
    echo "   ✗ Função de upload não encontrada (professionals)"
fi

if grep -q "uploadServiceImageFile" front-end/src/assets/js/establishment-services.js; then
    echo "   ✓ Função de upload implementada (services)"
else
    echo "   ✗ Função de upload não encontrada (services)"
fi

if grep -q "handleFileSelect" front-end/src/assets/js/establishment-professionals.js; then
    echo "   ✓ Preview de imagem implementado (professionals)"
else
    echo "   ✗ Preview de imagem não encontrado (professionals)"
fi

if grep -q "handleFileSelect" front-end/src/assets/js/establishment-services.js; then
    echo "   ✓ Preview de imagem implementado (services)"
else
    echo "   ✗ Preview de imagem não encontrado (services)"
fi

# 6. Compilar backend
echo ""
echo "6. Compilando backend..."
cd back-end
./gradlew build -x test > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo "   ✓ Backend compilado com sucesso"
else
    echo "   ✗ Erro ao compilar backend"
fi
cd ..

# 7. Executar testes
echo ""
echo "7. Executando testes..."
cd back-end
./gradlew test --tests "FileStorageServiceTest" --tests "FileUploadControllerTest" > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo "   ✓ Todos os testes passaram"
else
    echo "   ✗ Alguns testes falharam"
fi
cd ..

echo ""
echo "==================================="
echo "Verificação Concluída"
echo "==================================="
echo ""
echo "Próximos passos:"
echo "1. Iniciar o backend: cd back-end && ./gradlew bootRun"
echo "2. Abrir o frontend em um navegador"
echo "3. Testar upload de imagens manualmente"
echo ""
