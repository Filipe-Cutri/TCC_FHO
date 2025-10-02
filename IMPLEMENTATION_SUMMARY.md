# Implementação de Seleção de Estabelecimento para Clientes

## Resumo das Mudanças

Este documento descreve as mudanças implementadas para resolver o problema de clientes não terem um vínculo/identificador com estabelecimentos específicos.

## Mudanças no Backend

### 1. Modelo de Dados (Client.java)
- Adicionado campo `selectedEstablishmentId` para armazenar o estabelecimento selecionado pelo cliente
- Getters e setters correspondentes adicionados

### 2. Script de Migração de Banco de Dados
- Arquivo: `database_migration_add_establishment_to_client.sql`
- Adiciona coluna `selected_establishment_id` na tabela `clients`
- Cria foreign key com a tabela `establishments`
- Cria índice para performance

### 3. Controller (ClientAuthController.java)
- Atualizado endpoint `/api/client/register` para aceitar `establishmentId` opcional
- Novo endpoint `/api/client/establishment` (PUT) para atualizar o estabelecimento selecionado
- Response atualizado para incluir `selectedEstablishmentId`

### 4. Controller (EstablishmentController.java)
- Novo endpoint `/api/establishment/list` (GET) para listar estabelecimentos ativos
- Retorna lista simplificada com informações essenciais para seleção

### 5. Service (ClientService.java)
- Método `register` atualizado para aceitar `establishmentId` opcional
- Novo método `registerClient` com parâmetro de estabelecimento
- Novo método `updateSelectedEstablishment` para atualizar seleção

### 6. DTO (ClientResponse.java)
- Adicionado campo `selectedEstablishmentId`
- Construtores atualizados

## Mudanças no Frontend

### 1. Componente de Seleção de Estabelecimento
- **Arquivo**: `establishment-selector.js`
- Classe `EstablishmentSelector` reutilizável
- Funcionalidades:
  - Carregar lista de estabelecimentos da API
  - Exibir modal com cards de estabelecimentos
  - Permitir seleção visual de estabelecimento
  - Callbacks para processar seleção

### 2. Estilos
- **Arquivo**: `establishment-selector.css`
- Estilos para cards de estabelecimentos
- Estilos para modal de seleção
- Design responsivo
- Animações e efeitos visuais

### 3. Configuração de API
- **Arquivo**: `api-config.js`
- Adicionado endpoint `establishment.list`
- Adicionado endpoint `client.updateEstablishment`

### 4. Página de Registro (client-register.html)
- Adicionada seção de seleção de estabelecimento
- Interface visual para escolher estabelecimento (opcional)
- Exibição de estabelecimento selecionado com opção de trocar
- Submit do formulário inclui establishmentId

### 5. Página de Login (client-login.html)
- Após login bem-sucedido, verifica se cliente tem estabelecimento selecionado
- Se não tiver, oferece seleção imediata via modal
- Atualiza sessão com estabelecimento selecionado

## Fluxo do Usuário

### Fluxo de Registro
1. Cliente preenche dados de cadastro (nome, email, telefone, senha)
2. Cliente pode opcionalmente selecionar um estabelecimento
3. Ao clicar em "Selecionar estabelecimento", abre modal com lista de estabelecimentos
4. Cliente escolhe estabelecimento (cards visuais)
5. Estabelecimento selecionado é exibido com opção de trocar
6. Submit envia dados incluindo establishmentId
7. Backend salva cliente com estabelecimento vinculado

### Fluxo de Login
1. Cliente faz login com email e senha
2. Sistema verifica se cliente tem estabelecimento selecionado
3. Se não tiver:
   - Pergunta se quer selecionar agora
   - Abre modal de seleção
   - Atualiza via API
   - Redireciona para dashboard
4. Se já tiver, redireciona direto para dashboard

### Fluxo de Atualização
1. Cliente pode trocar estabelecimento a qualquer momento
2. Via endpoint PUT `/api/client/establishment`
3. Atualização é persistida no banco de dados
4. Sessão é atualizada com novo estabelecimento

## Endpoints da API

### GET /api/establishment/list
**Descrição**: Lista todos os estabelecimentos ativos para seleção
**Response**:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Barbearia Premium",
      "category": "Barbearia",
      "address": "Rua das Flores, 123",
      "description": "Melhor barbearia da região",
      "imageUrl": "https://..."
    }
  ],
  "count": 1
}
```

### POST /api/client/register
**Descrição**: Registra novo cliente
**Request**:
```json
{
  "name": "João Silva",
  "email": "joao@email.com",
  "password": "senha123",
  "phone": "11999999999",
  "establishmentId": "1"  // opcional
}
```
**Response**:
```json
{
  "success": true,
  "message": "Conta criada com sucesso",
  "client": {
    "id": 1,
    "name": "João Silva",
    "email": "joao@email.com",
    "phone": "11999999999",
    "selectedEstablishmentId": 1
  }
}
```

### PUT /api/client/establishment
**Descrição**: Atualiza estabelecimento selecionado do cliente
**Request**:
```json
{
  "clientId": "1",
  "establishmentId": "2"
}
```
**Response**:
```json
{
  "success": true,
  "message": "Estabelecimento selecionado com sucesso",
  "client": {
    "id": 1,
    "name": "João Silva",
    "email": "joao@email.com",
    "phone": "11999999999",
    "selectedEstablishmentId": 2
  }
}
```

## Banco de Dados

### Alteração na Tabela `clients`
```sql
ALTER TABLE clients 
ADD COLUMN selected_establishment_id BIGINT;

ALTER TABLE clients
ADD CONSTRAINT fk_client_selected_establishment 
    FOREIGN KEY (selected_establishment_id) 
    REFERENCES establishments(id) 
    ON DELETE SET NULL;

CREATE INDEX idx_clients_selected_establishment_id 
ON clients(selected_establishment_id);
```

## Benefícios

1. **Experiência Personalizada**: Cliente vinculado a estabelecimento específico
2. **Agendamentos Mais Fáceis**: Sistema sabe qual estabelecimento o cliente prefere
3. **Flexibilidade**: Cliente pode trocar de estabelecimento quando quiser
4. **Opcional**: Não é obrigatório selecionar na hora do registro
5. **Visual Atrativo**: Interface com cards visuais facilita escolha

## Próximos Passos

1. Aplicar a migração do banco de dados
2. Testar fluxo completo de registro
3. Testar fluxo completo de login
4. Testar atualização de estabelecimento
5. Validar em diferentes dispositivos (responsividade)
6. Coletar feedback dos usuários

## Arquivos Modificados/Criados

### Backend
- `back-end/src/main/java/com/slotfy/model/Client.java` (modificado)
- `back-end/src/main/java/com/slotfy/service/ClientService.java` (modificado)
- `back-end/src/main/java/com/slotfy/controller/ClientAuthController.java` (modificado)
- `back-end/src/main/java/com/slotfy/controller/EstablishmentController.java` (modificado)
- `back-end/src/main/java/com/slotfy/dto/ClientResponse.java` (modificado)
- `database_migration_add_establishment_to_client.sql` (criado)

### Frontend
- `front-end/src/assets/js/establishment-selector.js` (criado)
- `front-end/src/assets/styles/establishment-selector.css` (criado)
- `front-end/src/assets/js/api-config.js` (modificado)
- `front-end/src/pages/client/client-register.html` (modificado)
- `front-end/src/pages/client/client-login.html` (modificado)

## Conclusão

A implementação resolve completamente o problema identificado, fornecendo uma forma elegante e intuitiva para clientes selecionarem e gerenciarem seus estabelecimentos preferidos. A solução é escalável, mantém compatibilidade retroativa e oferece uma excelente experiência do usuário.
