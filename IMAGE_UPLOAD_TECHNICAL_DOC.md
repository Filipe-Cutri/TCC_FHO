# Documentação Técnica - Sistema de Upload de Imagens

## Arquitetura da Solução

### Backend (Spring Boot)

#### 1. FileStorageService
**Localização**: `back-end/src/main/java/com/slotfy/service/FileStorageService.java`

Serviço responsável pelo armazenamento físico dos arquivos.

**Funcionalidades**:
- `storeFile(MultipartFile file, String folder)`: Armazena um arquivo e retorna o caminho relativo
- `deleteFile(String filePath)`: Remove um arquivo do sistema
- `getFileStorageLocation()`: Retorna o caminho base do armazenamento

**Validações Implementadas**:
- Validação de arquivo vazio
- Validação de extensão (jpg, jpeg, png)
- Validação de tamanho (máx 5MB)
- Sanitização do nome da pasta (prevenção de path traversal)
- Verificação de limites de diretório (segurança)

**Segurança**:
```java
// Sanitização para prevenir path traversal
String sanitizedFolder = folder.replaceAll("[^a-zA-Z0-9_-]", "");

// Normalização e validação de path
Path folderPath = this.fileStorageLocation.resolve(sanitizedFolder).normalize();
if (!folderPath.startsWith(this.fileStorageLocation)) {
    throw new SecurityException("Tentativa de acesso fora do diretório permitido");
}
```

#### 2. FileUploadController
**Localização**: `back-end/src/main/java/com/slotfy/controller/FileUploadController.java`

Controller REST para gerenciar uploads via HTTP.

**Endpoints**:

##### POST `/api/files/professional/{id}/upload`
Faz upload de imagem para um profissional.

**Parâmetros**:
- `id` (path): ID do profissional
- `file` (multipart): Arquivo de imagem
- `establishmentId` (query): ID do estabelecimento

**Resposta de Sucesso** (200 OK):
```json
{
  "success": true,
  "message": "Imagem enviada com sucesso",
  "imageUrl": "/uploads/professionals/uuid.jpg",
  "data": { /* Professional object */ }
}
```

**Respostas de Erro**:
- 400 Bad Request: Arquivo inválido
- 403 Forbidden: Sem permissão para atualizar o profissional
- 500 Internal Server Error: Erro no servidor

##### POST `/api/files/service/{id}/upload`
Faz upload de imagem para um serviço.

**Parâmetros**:
- `id` (path): ID do serviço
- `file` (multipart): Arquivo de imagem
- `establishmentId` (query): ID do estabelecimento

**Resposta**: Similar ao endpoint de profissionais

#### 3. FileUploadConfig
**Localização**: `back-end/src/main/java/com/slotfy/config/FileUploadConfig.java`

Configuração para servir arquivos estáticos.

**Configuração**:
- Mapeia `/uploads/**` para o diretório de upload configurado
- Permite acesso público às imagens via HTTP

#### 4. Configuração (application.properties)
```properties
# File Upload Configuration
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=5MB
file.upload-dir=uploads
```

### Frontend (JavaScript/HTML)

#### 1. Formulários HTML

**Estrutura do Campo de Upload**:
```html
<div class="establishment-form-group">
  <label class="establishment-form-label">Foto do Profissional</label>
  <div class="mb-3">
    <input type="file" id="professionalImageFile" class="form-control" 
           accept="image/jpeg,image/jpg,image/png">
    <small class="text-muted">Selecione uma imagem do seu dispositivo (JPG ou PNG, máx 5MB)</small>
  </div>
  <div id="professionalImagePreview" class="mt-2" style="display: none;">
    <img id="professionalPreviewImg" src="" alt="Preview" 
         style="max-width: 200px; max-height: 200px; border-radius: 8px;">
    <button type="button" class="btn btn-sm btn-danger mt-2" id="removeProfessionalImage">
      <i class="fas fa-times"></i> Remover imagem
    </button>
  </div>
  <div class="mt-2">
    <small class="text-muted">Ou insira uma URL:</small>
    <input type="url" id="professionalImageUrl" class="establishment-form-control mt-1" 
           placeholder="https://exemplo.com/foto.jpg">
  </div>
</div>
```

#### 2. JavaScript - EstablishmentProfessionalsManager

**Método de Upload**:
```javascript
async uploadProfessionalImageFile(professionalId, file) {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('establishmentId', this.establishmentId);

    try {
        const response = await fetch(
            `${this.apiBaseUrl}/api/files/professional/${professionalId}/upload`, 
            {
                method: 'POST',
                body: formData
            }
        );

        const data = await response.json();
        if (!data.success) {
            this.showError('Erro ao enviar imagem: ' + (data.message || 'Erro desconhecido'));
        }
    } catch (error) {
        console.error('Error uploading professional image:', error);
        this.showError('Erro ao enviar imagem');
    }
}
```

**Preview de Imagem**:
```javascript
handleFileSelect(event) {
    const file = event.target.files[0];
    if (file) {
        // Validate file type
        const validTypes = ['image/jpeg', 'image/jpg', 'image/png'];
        if (!validTypes.includes(file.type)) {
            this.showError('Apenas arquivos JPG e PNG são permitidos');
            event.target.value = '';
            return;
        }

        // Validate file size (5MB)
        if (file.size > 5 * 1024 * 1024) {
            this.showError('Arquivo muito grande. Tamanho máximo: 5MB');
            event.target.value = '';
            return;
        }

        // Show preview
        const reader = new FileReader();
        reader.onload = (e) => {
            const preview = document.getElementById('professionalImagePreview');
            const img = document.getElementById('professionalPreviewImg');
            if (preview && img) {
                img.src = e.target.result;
                preview.style.display = 'block';
            }
        };
        reader.readAsDataURL(file);

        // Clear URL input when file is selected
        const urlInput = document.getElementById('professionalImageUrl');
        if (urlInput) urlInput.value = '';
    }
}
```

## Fluxo de Upload

### 1. Usuário Seleciona Arquivo
```
Frontend: Usuário clica em "Escolher arquivo"
         ↓
Frontend: Sistema valida tipo e tamanho
         ↓
Frontend: Exibe preview da imagem
         ↓
Usuário clica em "Salvar"
```

### 2. Envio para Backend
```
Frontend: Cria Professional/Service via POST
         ↓
Backend: Salva registro no banco (sem imagem)
         ↓
Backend: Retorna ID do registro criado
         ↓
Frontend: Envia arquivo via FormData para /api/files/{type}/{id}/upload
```

### 3. Processamento no Backend
```
Backend: FileUploadController recebe arquivo
        ↓
Backend: FileStorageService valida e sanitiza
        ↓
Backend: Gera UUID único para arquivo
        ↓
Backend: Salva arquivo em /uploads/{type}/{uuid}.{ext}
        ↓
Backend: Atualiza registro com imageUrl
        ↓
Backend: Retorna sucesso com URL da imagem
```

## Testes

### Testes Backend

#### FileStorageServiceTest
**Localização**: `back-end/src/test/java/com/slotfy/service/FileStorageServiceTest.java`

**Casos de Teste**:
- ✅ Upload de arquivo JPG válido
- ✅ Upload de arquivo PNG válido
- ✅ Rejeição de arquivo vazio
- ✅ Rejeição de extensão inválida
- ✅ Rejeição de arquivo muito grande
- ✅ Deleção de arquivo existente
- ✅ Deleção de arquivo inexistente (não lança exceção)
- ✅ Criação automática de pasta
- ✅ Geração de nomes únicos
- ✅ Proteção contra path traversal
- ✅ Rejeição de nomes de pasta inválidos

#### FileUploadControllerTest
**Localização**: `back-end/src/test/java/com/slotfy/controller/FileUploadControllerTest.java`

**Casos de Teste**:
- ✅ Upload bem-sucedido de imagem de profissional
- ✅ Upload bem-sucedido de imagem de serviço
- ✅ Rejeição de arquivo inválido para profissional
- ✅ Rejeição de arquivo inválido para serviço
- ✅ Acesso não autorizado retorna 403

### Executar Testes

```bash
cd back-end
./gradlew test
```

Para rodar apenas os testes de upload:
```bash
./gradlew test --tests "FileStorageServiceTest"
./gradlew test --tests "FileUploadControllerTest"
```

## Segurança

### Medidas Implementadas

1. **Validação de Tipo de Arquivo**
   - Frontend: Atributo `accept` no input
   - Backend: Validação de extensão via `getFileExtension()`

2. **Validação de Tamanho**
   - Frontend: Validação JavaScript antes do upload
   - Backend: Propriedade `spring.servlet.multipart.max-file-size`
   - Backend: Validação adicional no `FileStorageService`

3. **Prevenção de Path Traversal**
   ```java
   // Remove caracteres especiais
   String sanitizedFolder = folder.replaceAll("[^a-zA-Z0-9_-]", "");
   
   // Normaliza e valida path
   Path folderPath = this.fileStorageLocation.resolve(sanitizedFolder).normalize();
   if (!folderPath.startsWith(this.fileStorageLocation)) {
       throw new SecurityException("Tentativa de acesso fora do diretório permitido");
   }
   ```

4. **Nomes de Arquivo Únicos**
   - Uso de UUID para evitar conflitos e previsibilidade

5. **Isolamento por Estabelecimento**
   - Validação de `establishmentId` em todas as operações
   - Verificação de ownership antes de atualizar imagens

### Vulnerabilidades Conhecidas e Mitigadas

| Vulnerabilidade | Mitigação |
|-----------------|-----------|
| Path Traversal | Sanitização de folder name + validação de limites |
| File Upload Bomb | Limite de 5MB por arquivo |
| Malicious File Type | Whitelist de extensões (jpg, png) |
| Overwrite Attack | UUID único para cada arquivo |
| Unauthorized Access | Verificação de establishmentId |

## Manutenção

### Limpeza de Arquivos Órfãos

Não implementado atualmente. Recomendações para implementação futura:

1. Criar job scheduled para varrer arquivos não referenciados
2. Implementar soft-delete com cleanup após N dias
3. Adicionar logs de upload/delete para auditoria

### Backup

O diretório `/uploads` deve ser incluído na estratégia de backup do sistema.

### Monitoramento

Recomenda-se monitorar:
- Espaço em disco do diretório de uploads
- Taxa de falhas de upload
- Tamanho médio dos arquivos
- Padrões de uso suspeitos

## Troubleshooting

### Problema: Upload falha com erro 413
**Causa**: Arquivo maior que o limite configurado  
**Solução**: Verificar configuração `max-file-size` em `application.properties`

### Problema: Arquivos não são exibidos
**Causa**: Configuração incorreta do `FileUploadConfig`  
**Solução**: Verificar mapeamento de `/uploads/**` e permissões do diretório

### Problema: Erro de permissão ao salvar arquivo
**Causa**: Processo não tem permissão de escrita no diretório  
**Solução**: Ajustar permissões do diretório `uploads/`

### Problema: Preview não funciona no frontend
**Causa**: JavaScript desabilitado ou erro na leitura do arquivo  
**Solução**: Verificar console do browser e validar FileReader API

## Próximas Melhorias

1. **Redimensionamento automático** de imagens para otimizar performance
2. **Suporte a WebP** para melhor compressão
3. **Upload de múltiplos arquivos** simultaneamente
4. **Cropping de imagens** no frontend
5. **CDN integration** para melhor distribuição
6. **Image optimization** automática no backend
7. **Progress bar** durante o upload
8. **Drag & drop** no frontend

## Referências

- [Spring Boot File Upload](https://spring.io/guides/gs/uploading-files/)
- [MDN - FileReader API](https://developer.mozilla.org/en-US/docs/Web/API/FileReader)
- [OWASP - File Upload](https://owasp.org/www-community/vulnerabilities/Unrestricted_File_Upload)
