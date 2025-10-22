# Guia de Upload de Imagens - Sistema Slotfy

## Visão Geral

O sistema Slotfy agora suporta o upload direto de imagens do dispositivo do usuário (computador, celular, tablet) para fotos de profissionais e serviços. Esta funcionalidade substitui a necessidade de usar URLs externas de imagens.

## Funcionalidades Implementadas

### 1. Upload de Imagens de Profissionais
- Upload direto de fotos de profissionais
- Suporte para formatos JPG e PNG
- Tamanho máximo: 5MB
- Preview da imagem antes de salvar
- Alternativa: ainda é possível usar URLs de imagens

### 2. Upload de Imagens de Serviços
- Upload direto de fotos de serviços
- Suporte para formatos JPG e PNG
- Tamanho máximo: 5MB
- Preview da imagem antes de salvar
- Alternativa: ainda é possível usar URLs de imagens

## Como Usar

### Para Estabelecimentos - Upload de Foto de Profissional

1. Acesse a página de **Profissionais** no painel do estabelecimento
2. Clique em **"Novo Profissional"** ou edite um profissional existente
3. No formulário, localize o campo **"Foto do Profissional"**
4. Clique no botão **"Escolher arquivo"** ou **"Browse"**
5. Selecione uma imagem JPG ou PNG do seu dispositivo (máx 5MB)
6. Você verá um preview da imagem selecionada
7. Para remover a imagem, clique no botão **"Remover imagem"**
8. Alternativamente, você pode inserir uma URL de imagem no campo abaixo
9. Preencha os demais campos do formulário
10. Clique em **"Salvar Profissional"**

### Para Estabelecimentos - Upload de Foto de Serviço

1. Acesse a página de **Serviços** no painel do estabelecimento
2. Clique em **"Novo Serviço"** ou edite um serviço existente
3. No formulário, localize o campo **"Imagem do Serviço"**
4. Clique no botão **"Escolher arquivo"** ou **"Browse"**
5. Selecione uma imagem JPG ou PNG do seu dispositivo (máx 5MB)
6. Você verá um preview da imagem selecionada
7. Para remover a imagem, clique no botão **"Remover imagem"**
8. Alternativamente, você pode inserir uma URL de imagem no campo abaixo
9. Preencha os demais campos do formulário
10. Clique em **"Salvar Serviço"**

## Especificações Técnicas

### Formatos Suportados
- JPEG (.jpg, .jpeg)
- PNG (.png)

### Limitações
- Tamanho máximo do arquivo: 5MB
- Apenas imagens são aceitas
- Um arquivo por vez

### Validações
- **Tipo de arquivo**: O sistema valida se o arquivo é JPG ou PNG
- **Tamanho**: Arquivos maiores que 5MB são rejeitados
- **Segurança**: O sistema sanitiza os nomes dos arquivos e previne ataques de path traversal

### Armazenamento
- As imagens são armazenadas no servidor em pastas dedicadas:
  - Profissionais: `/uploads/professionals/`
  - Serviços: `/uploads/services/`
- Cada arquivo recebe um nome único (UUID) para evitar conflitos
- As imagens são acessíveis via URL: `https://seu-servidor/uploads/[tipo]/[arquivo]`

## Mensagens de Erro Comuns

| Erro | Causa | Solução |
|------|-------|---------|
| "Apenas arquivos JPG e PNG são permitidos" | Formato de arquivo inválido | Selecione um arquivo JPG ou PNG |
| "Arquivo muito grande. Tamanho máximo: 5MB" | Arquivo excede 5MB | Redimensione a imagem ou escolha outra |
| "Arquivo vazio não pode ser enviado" | Arquivo corrompido ou vazio | Selecione outro arquivo |
| "Erro ao enviar imagem" | Problema de conexão ou servidor | Tente novamente ou contate o suporte |

## Boas Práticas

### Para Melhores Resultados:

1. **Fotos de Profissionais**:
   - Use fotos profissionais e bem iluminadas
   - Prefira fotos com fundo neutro
   - Formato quadrado ou retrato
   - Resolução recomendada: 400x400 pixels ou maior

2. **Fotos de Serviços**:
   - Use imagens que representem claramente o serviço
   - Boa iluminação e foco
   - Formato landscape ou quadrado
   - Resolução recomendada: 800x600 pixels ou maior

3. **Otimização**:
   - Comprima as imagens antes do upload para melhor performance
   - Use ferramentas online de compressão de imagens
   - Mantenha o tamanho abaixo de 1MB quando possível

## Compatibilidade

### Navegadores Suportados:
- Google Chrome (versão 90+)
- Mozilla Firefox (versão 88+)
- Safari (versão 14+)
- Microsoft Edge (versão 90+)

### Dispositivos:
- Desktop (Windows, macOS, Linux)
- Tablets (iOS, Android)
- Smartphones (iOS, Android)

## Solução de Problemas

### A imagem não aparece após o upload
1. Verifique se o formato é JPG ou PNG
2. Certifique-se de que o arquivo não está corrompido
3. Tente fazer upload novamente
4. Limpe o cache do navegador

### O preview não funciona
1. Certifique-se de que JavaScript está habilitado
2. Tente usar outro navegador
3. Verifique a console do navegador para erros

### Upload falha repetidamente
1. Verifique sua conexão com a internet
2. Tente reduzir o tamanho da imagem
3. Tente usar outro formato (JPG em vez de PNG ou vice-versa)
4. Entre em contato com o suporte técnico

## Segurança

O sistema implementa as seguintes medidas de segurança:

1. **Validação de tipo de arquivo**: Apenas imagens JPG e PNG são aceitas
2. **Validação de tamanho**: Limite de 5MB por arquivo
3. **Sanitização de nomes**: Nomes de arquivo são sanitizados para prevenir ataques
4. **Proteção contra path traversal**: O sistema previne tentativas de acesso a diretórios não autorizados
5. **Nomes únicos**: Cada arquivo recebe um nome único (UUID) para evitar substituições acidentais
6. **Isolamento de estabelecimentos**: As imagens são associadas ao estabelecimento correto

## Suporte

Para dúvidas ou problemas, entre em contato com o suporte técnico através do email: suporte@slotfy.com
