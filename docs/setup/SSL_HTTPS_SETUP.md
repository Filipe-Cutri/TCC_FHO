# SSL/HTTPS Configuration Guide - Slotfy Backend

## 📋 Overview

O backend do Slotfy foi configurado para usar HTTPS (SSL/TLS) em todas as comunicações, proporcionando segurança e criptografia para a aplicação.

## 🔐 Implementação Atual

### Características SSL/HTTPS:
- **Porta HTTPS**: 8443 (principal)
- **Porta HTTP**: 8080 (redirecionamento automático para HTTPS)
- **Certificado**: Auto-assinado para desenvolvimento (`slotfy.p12`)
- **Criptografia**: TLS 1.3 com AES 256 GCM SHA384
- **Redirecionamento**: HTTP → HTTPS automático

### Configuração Implementada:

#### 1. Certificado SSL
```bash
# Certificado gerado automaticamente
Arquivo: src/main/resources/slotfy.p12
Validade: 10 anos (2025-2035)
Tipo: PKCS12
Alias: slotfy
```

#### 2. Configuração do Servidor
```properties
# application.properties
server.port=8443
server.ssl.enabled=true
server.ssl.key-store=classpath:slotfy.p12
server.ssl.key-store-password=slotfypass
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=slotfy
```

#### 3. Redirecionamento HTTP → HTTPS
- Configurado via `SSLConfig.java`
- Porta 8080 (HTTP) redireciona automaticamente para 8443 (HTTPS)
- Constraint de segurança força HTTPS para todas as rotas

## 🌐 URLs de Acesso

### Desenvolvimento Local:
- **HTTPS (Principal)**: `https://localhost:8443`
- **HTTP (Redirecionamento)**: `http://localhost:8080` → redireciona para HTTPS
- **API Health Check**: `https://localhost:8443/api/health`
- **H2 Console**: `https://localhost:8443/h2-console`

### Frontend:
```javascript
// Configuração atualizada em api-config.js
baseUrl: 'https://localhost:8443'
```

## ✅ Verificação da Implementação

### Teste de Conexão HTTPS:
```bash
curl -k https://localhost:8443/api/health
# Retorna: {"application":"Slotfy Backend","version":"1.0.0","status":"UP"}
```

### Teste de Redirecionamento HTTP:
```bash
curl -v http://localhost:8080/api/health
# Retorna: 302 redirect para https://localhost:8443/api/health
```

### Detalhes de Segurança:
- **Protocolo**: TLS 1.3
- **Cipher Suite**: TLS_AES_256_GCM_SHA384
- **Headers de Segurança**: Strict-Transport-Security incluído
- **Certificado**: Auto-assinado (desenvolvimento)

## 🔧 Configuração por Ambiente

### Desenvolvimento (`application-dev.properties`):
```properties
server.port=8443
server.ssl.enabled=true
server.ssl.key-store=classpath:slotfy.p12
server.ssl.key-store-password=slotfypass
```

### Produção (`application-prod.properties`):
```properties
server.port=${HTTPS_PORT:8443}
server.ssl.enabled=${SSL_ENABLED:true}
server.ssl.key-store=${SSL_KEYSTORE:classpath:slotfy.p12}
server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD:slotfypass}
```

### Teste (`application-test.properties`):
```properties
# Mesmas configurações SSL para consistência
server.port=8443
server.ssl.enabled=true
```

## 🚀 Configuração para Produção

### 1. Certificado SSL Real (Produção):

Para produção, substitua o certificado auto-assinado por um certificado válido:

#### Opção A: Let's Encrypt (Gratuito)
```bash
# Instalar certbot
sudo apt install certbot

# Gerar certificado
sudo certbot certonly --standalone -d seudominio.com

# Converter para PKCS12
openssl pkcs12 -export -in /etc/letsencrypt/live/seudominio.com/fullchain.pem \
  -inkey /etc/letsencrypt/live/seudominio.com/privkey.pem \
  -out slotfy-prod.p12 -name slotfy
```

#### Opção B: Certificado Comercial
```bash
# Substituir o arquivo slotfy.p12 pelo certificado comercial
# Atualizar variáveis de ambiente em produção:
export SSL_KEYSTORE=file:/caminho/para/certificado-prod.p12
export SSL_KEYSTORE_PASSWORD=senha_segura_prod
```

### 2. Variáveis de Ambiente (Produção):
```bash
export HTTPS_PORT=443
export SSL_ENABLED=true
export SSL_KEYSTORE=file:/etc/ssl/slotfy/slotfy-prod.p12
export SSL_KEYSTORE_PASSWORD=senha_muito_segura
```

### 3. Configuração de Firewall:
```bash
# Abrir portas necessárias
sudo ufw allow 80/tcp   # HTTP (redirecionamento)
sudo ufw allow 443/tcp  # HTTPS
```

## 🛡️ Benefícios de Segurança

1. **Criptografia End-to-End**: Toda comunicação criptografada
2. **Proteção contra Man-in-the-Middle**: Certificados SSL
3. **Headers de Segurança**: HSTS, XSS Protection, etc.
4. **Redirecionamento Automático**: Força uso de HTTPS
5. **TLS 1.3**: Protocolo mais seguro e rápido

## 🔍 Troubleshooting

### Problema: "Certificate not trusted"
```bash
# Desenvolvimento: usar -k com curl ou adicionar exceção no browser
curl -k https://localhost:8443/api/health
```

### Problema: "Connection refused" na porta 8080
```bash
# Verificar se a aplicação está rodando com o perfil correto
./gradlew bootRun -Dspring.profiles.active=dev
```

### Problema: Certificado expirado
```bash
# Regenerar certificado (desenvolvimento)
keytool -genkeypair -alias slotfy -keyalg RSA -keysize 2048 \
  -storetype PKCS12 -keystore slotfy.p12 -validity 3650 \
  -storepass slotfypass -keypass slotfypass \
  -dname "CN=localhost,OU=Slotfy,O=Slotfy Organization,L=Sao Paulo,ST=SP,C=BR"
```

## ✨ Resumo da Implementação

✅ **SSL/HTTPS Funcional**  
✅ **Certificado Auto-assinado (Desenvolvimento)**  
✅ **Redirecionamento HTTP → HTTPS**  
✅ **TLS 1.3 com Criptografia Forte**  
✅ **Configuração Multi-ambiente**  
✅ **Frontend Atualizado para HTTPS**  
✅ **Headers de Segurança Incluídos**  

A implementação SSL está completa e funcional para desenvolvimento. Para produção, apenas substitua o certificado auto-assinado por um certificado válido.