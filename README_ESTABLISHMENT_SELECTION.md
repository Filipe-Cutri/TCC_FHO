# 🎯 Feature: Seleção de Estabelecimento para Clientes

## 📋 Problema Identificado

Clientes que fazem login ou cadastro na plataforma não possuem um vínculo/identificador para selecionar o estabelecimento específico que desejam utilizar. Isso dificulta a personalização dos agendamentos e a experiência do usuário.

## ✅ Solução Implementada

Implementamos uma solução completa que permite aos clientes:
- Selecionar um estabelecimento durante o registro (opcional)
- Selecionar um estabelecimento após o login (se não selecionado antes)
- Trocar de estabelecimento a qualquer momento
- Visualizar estabelecimentos disponíveis através de uma interface visual elegante

## 🎨 Interface do Usuário

### Tela de Registro
A página de registro agora inclui uma seção opcional para seleção de estabelecimento:

```
┌────────────────────────────────────────┐
│  📝 Cadastro de Cliente                │
├────────────────────────────────────────┤
│  👤 Nome: [________________]           │
│  📧 Email: [________________]          │
│  📱 Telefone: [________________]       │
│  🔒 Senha: [________________]          │
│                                        │
│  🏪 Estabelecimento (opcional)         │
│  ┌──────────────────────────────────┐ │
│  │ ➕ Selecionar Estabelecimento    │ │
│  └──────────────────────────────────┘ │
│                                        │
│  [    Criar Minha Conta     ]         │
└────────────────────────────────────────┘
```

### Modal de Seleção
Quando o usuário clica para selecionar, aparece um modal com cards visuais:

```
┌──────────────────────────────────────────────────────────┐
│  🏪 Selecione um Estabelecimento              [X]        │
├──────────────────────────────────────────────────────────┤
│                                                           │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     │
│  │   📷 Foto   │  │   📷 Foto   │  │   📷 Foto   │     │
│  │             │  │             │  │             │     │
│  │ Barbearia   │  │ Salão Belle │  │ Spa Relax   │     │
│  │ Premium     │  │             │  │             │     │
│  │ 🏷️ Barbearia│  │ 🏷️ Beleza   │  │ 🏷️ Spa      │     │
│  │ 📍 Centro   │  │ 📍 Zona Sul │  │ 📍 Zona Oeste│    │
│  └─────────────┘  └─────────────┘  └─────────────┘     │
│                                                           │
│              [Cancelar]  [Confirmar]                     │
└──────────────────────────────────────────────────────────┘
```

### Estabelecimento Selecionado
Após selecionar, o campo é atualizado mostrando o estabelecimento:

```
┌────────────────────────────────────────┐
│  🏪 Estabelecimento (opcional)         │
│  ┌──────────────────────────────────┐ │
│  │ 📷  Barbearia Premium            │ │
│  │     🏷️ Barbearia                 │ │
│  │              [🔄 Trocar]         │ │
│  └──────────────────────────────────┘ │
└────────────────────────────────────────┘
```

## 🔧 Mudanças Técnicas

### Backend (Java/Spring Boot)

#### 1. Modelo de Dados
```java
@Entity
@Table(name = "clients")
public class Client {
    // ... campos existentes
    @Column(name = "selected_establishment_id")
    private Long selectedEstablishmentId;
}
```

#### 2. Endpoints da API

**GET /api/establishment/list**
- Lista todos os estabelecimentos ativos
- Retorna informações simplificadas para exibição

**POST /api/client/register**
- Atualizado para aceitar `establishmentId` opcional
- Vincula cliente ao estabelecimento na criação

**PUT /api/client/establishment**
- Novo endpoint para atualizar estabelecimento selecionado
- Permite mudança a qualquer momento

#### 3. Migration SQL
```sql
ALTER TABLE clients 
ADD COLUMN selected_establishment_id BIGINT;

ALTER TABLE clients
ADD CONSTRAINT fk_client_selected_establishment 
    FOREIGN KEY (selected_establishment_id) 
    REFERENCES establishments(id) 
    ON DELETE SET NULL;
```

### Frontend (HTML/CSS/JavaScript)

#### 1. Componente Reutilizável
- `establishment-selector.js`: Lógica de seleção
- `establishment-selector.css`: Estilos visuais
- Modal com Bootstrap 5
- Cards interativos e responsivos

#### 2. Integração nas Páginas
- `client-register.html`: Seleção opcional durante registro
- `client-login.html`: Prompt para seleção se não definido

## 📊 Fluxo de Dados

### Registro com Estabelecimento
```
1. Cliente preenche formulário
2. Cliente clica "Selecionar Estabelecimento"
3. Sistema carrega lista de estabelecimentos (GET /api/establishment/list)
4. Cliente escolhe estabelecimento no modal
5. UI atualiza mostrando seleção
6. Submit envia dados + establishmentId (POST /api/client/register)
7. Backend salva cliente com vínculo
8. Sistema redireciona para dashboard
```

### Login sem Estabelecimento
```
1. Cliente faz login (POST /api/client/login)
2. Sistema verifica se tem selectedEstablishmentId
3. Se não tiver, pergunta se quer selecionar
4. Se sim, abre modal de seleção
5. Cliente seleciona estabelecimento
6. Sistema atualiza via API (PUT /api/client/establishment)
7. Sistema redireciona para dashboard
```

## 🧪 Como Testar

### Método 1: Teste Manual
1. Inicie o backend: `cd back-end && ./gradlew bootRun`
2. Abra `front-end/src/pages/client/client-register.html` no navegador
3. Clique em "Selecionar Estabelecimento"
4. Escolha um estabelecimento
5. Complete o registro
6. Verifique no banco de dados que o cliente tem `selected_establishment_id`

### Método 2: Página de Teste Automatizada
1. Inicie o backend
2. Abra `test-establishment-selection.html` no navegador
3. Execute os 4 testes disponíveis
4. Veja resultados em tempo real

### Método 3: Via API diretamente
```bash
# 1. Listar estabelecimentos
curl http://localhost:8080/api/establishment/list

# 2. Registrar cliente com estabelecimento
curl -X POST http://localhost:8080/api/client/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "João Silva",
    "email": "joao@test.com",
    "password": "senha123",
    "phone": "11999999999",
    "establishmentId": "1"
  }'

# 3. Atualizar estabelecimento do cliente
curl -X PUT http://localhost:8080/api/client/establishment \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": "1",
    "establishmentId": "2"
  }'
```

## 📦 Arquivos Modificados/Criados

### Backend
- ✅ `Client.java` - Modelo atualizado
- ✅ `ClientService.java` - Métodos de registro/atualização
- ✅ `ClientAuthController.java` - Endpoints atualizados
- ✅ `EstablishmentController.java` - Novo endpoint de lista
- ✅ `ClientResponse.java` - DTO atualizado
- ✅ `database_migration_add_establishment_to_client.sql` - Migration

### Frontend
- ✅ `establishment-selector.js` - Componente novo
- ✅ `establishment-selector.css` - Estilos novos
- ✅ `api-config.js` - Endpoints atualizados
- ✅ `client-register.html` - Página atualizada
- ✅ `client-login.html` - Página atualizada

### Documentação
- ✅ `IMPLEMENTATION_SUMMARY.md` - Resumo completo
- ✅ `ARCHITECTURE.md` - Diagrama e arquitetura
- ✅ `test-establishment-selection.html` - Página de testes
- ✅ `README_ESTABLISHMENT_SELECTION.md` - Este arquivo

## ✨ Funcionalidades

- ✅ Lista de estabelecimentos disponíveis
- ✅ Seleção visual com cards
- ✅ Registro com estabelecimento opcional
- ✅ Login com prompt de seleção se necessário
- ✅ Atualização de estabelecimento a qualquer momento
- ✅ Interface responsiva (mobile e desktop)
- ✅ Backward compatible (não quebra funcionalidades existentes)
- ✅ Validações de entrada
- ✅ Tratamento de erros
- ✅ Loading states

## 🎯 Benefícios

1. **Experiência Personalizada**: Cliente vinculado a estabelecimento específico
2. **Agendamentos Facilitados**: Sistema sabe preferência do cliente
3. **Flexibilidade**: Cliente pode trocar quando quiser
4. **Não Intrusivo**: Seleção é opcional no registro
5. **Visual Atraente**: Interface moderna e intuitiva
6. **Escalável**: Fácil adicionar mais funcionalidades

## 🚀 Próximos Passos Sugeridos

1. **Aplicar Migration**: Execute o script SQL no banco
2. **Deploy Backend**: Suba a nova versão com as mudanças
3. **Deploy Frontend**: Atualize os arquivos estáticos
4. **Testes E2E**: Execute testes completos
5. **Monitoramento**: Acompanhe adoção da funcionalidade
6. **Feedback**: Colete opinião dos usuários

## 📞 Suporte

Para dúvidas ou problemas:
1. Consulte `IMPLEMENTATION_SUMMARY.md` para detalhes técnicos
2. Consulte `ARCHITECTURE.md` para fluxos e diagramas
3. Use `test-establishment-selection.html` para testes rápidos
4. Verifique os logs do backend para debugging

---

**Implementado com ❤️ para melhorar a experiência dos clientes no Slotfy**
