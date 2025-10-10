# Arquitetura da Solução - Seleção de Estabelecimento

## Diagrama de Fluxo

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            CLIENTE (Browser)                                 │
└──────────────────────────┬──────────────────────────────────────────────────┘
                           │
                           │ 1. Acessa página
                           ▼
        ┌──────────────────────────────────────────┐
        │   client-register.html / client-login.html│
        │                                           │
        │  - Formulário de registro/login           │
        │  - Botão "Selecionar Estabelecimento"     │
        └──────────────┬────────────────────────────┘
                       │
                       │ 2. Clica para selecionar
                       ▼
        ┌──────────────────────────────────────────┐
        │    establishment-selector.js              │
        │                                           │
        │  - loadEstablishments()                   │
        │  - showModal()                            │
        │  - Gerencia callbacks                     │
        └──────────────┬────────────────────────────┘
                       │
                       │ 3. GET /api/establishment/list
                       ▼
        ┌──────────────────────────────────────────┐
        │      BACKEND (Spring Boot)                │
        └──────────────────────────────────────────┘
                       │
                       │ 4. EstablishmentController.listActiveEstablishments()
                       ▼
        ┌──────────────────────────────────────────┐
        │      EstablishmentService                 │
        │                                           │
        │  - getByStatus(ACTIVE)                    │
        └──────────────┬────────────────────────────┘
                       │
                       │ 5. Query
                       ▼
        ┌──────────────────────────────────────────┐
        │      EstablishmentRepository              │
        │                                           │
        │  - findByStatusOrderByNameAsc()           │
        └──────────────┬────────────────────────────┘
                       │
                       │ 6. SQL Query
                       ▼
        ┌──────────────────────────────────────────┐
        │      PostgreSQL Database                  │
        │                                           │
        │  SELECT * FROM establishments             │
        │  WHERE status = 'ACTIVE'                  │
        │  ORDER BY name ASC                        │
        └──────────────┬────────────────────────────┘
                       │
                       │ 7. Retorna lista
                       ▼
        ┌──────────────────────────────────────────┐
        │      Frontend (Modal)                     │
        │                                           │
        │  ┌────────┐ ┌────────┐ ┌────────┐       │
        │  │ Est. 1 │ │ Est. 2 │ │ Est. 3 │       │
        │  │  📷    │ │  📷    │ │  📷    │       │
        │  │  Nome  │ │  Nome  │ │  Nome  │       │
        │  └────────┘ └────────┘ └────────┘       │
        └──────────────┬────────────────────────────┘
                       │
                       │ 8. Cliente seleciona
                       ▼
        ┌──────────────────────────────────────────┐
        │  Registro ou Atualização                  │
        └──────────────┬────────────────────────────┘
                       │
        ┌──────────────┴──────────────┐
        │                             │
        ▼                             ▼
┌────────────────┐         ┌──────────────────┐
│ POST /register │         │ PUT /establishment│
│ com estabId    │         │ atualiza estabId  │
└───────┬────────┘         └────────┬──────────┘
        │                           │
        │                           │
        ▼                           ▼
┌──────────────────────────────────────────┐
│   ClientService                           │
│                                           │
│  - registerClient(... establishmentId)    │
│  - updateSelectedEstablishment()          │
└──────────────┬────────────────────────────┘
               │
               │ 9. Salva no banco
               ▼
┌──────────────────────────────────────────┐
│   PostgreSQL Database                     │
│                                           │
│   INSERT INTO clients                     │
│   (..., selected_establishment_id)        │
│   VALUES (..., 1)                         │
│                                           │
│   UPDATE clients                          │
│   SET selected_establishment_id = 2       │
│   WHERE id = 1                            │
└──────────────┬────────────────────────────┘
               │
               │ 10. Confirma sucesso
               ▼
┌──────────────────────────────────────────┐
│   Frontend - Resposta                     │
│                                           │
│   - Atualiza sessão                       │
│   - Redireciona para dashboard            │
│   - Exibe mensagem de sucesso             │
└───────────────────────────────────────────┘
```

## Componentes e Responsabilidades

### Frontend

#### 1. establishment-selector.js
- **Responsabilidade**: Gerenciar UI de seleção
- **Métodos principais**:
  - `loadEstablishments()`: Busca lista da API
  - `showModal()`: Exibe modal com opções
  - `createCardGridHTML()`: Gera HTML dos cards
  - `setupModalEventListeners()`: Gerencia interações

#### 2. establishment-selector.css
- **Responsabilidade**: Estilos visuais
- **Componentes**:
  - Cards de estabelecimentos
  - Modal de seleção
  - Efeitos hover e selected
  - Responsividade

#### 3. client-register.html / client-login.html
- **Responsabilidade**: Integração da seleção no fluxo
- **Funcionalidades**:
  - Botão de seleção
  - Exibição de estabelecimento selecionado
  - Submit com establishmentId

### Backend

#### 1. Client (Model)
```java
@Entity
@Table(name = "clients")
public class Client {
    private Long id;
    private String name;
    private String email;
    private String password;
    private String phone;
    private Boolean active;
    private Long selectedEstablishmentId; // NOVO
}
```

#### 2. ClientService
```java
public Client registerClient(
    String name, 
    String email, 
    String password, 
    String phone, 
    Long establishmentId // NOVO
)

public Client updateSelectedEstablishment(
    Long clientId, 
    Long establishmentId
)
```

#### 3. ClientAuthController
```java
@PostMapping("/register")
// Aceita establishmentId no request

@PutMapping("/establishment")
// Novo endpoint para atualizar
```

#### 4. EstablishmentController
```java
@GetMapping("/list")
// Novo endpoint para listar ativos
```

### Database

#### Tabela clients (Atualizada)
```sql
CREATE TABLE clients (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    active BOOLEAN DEFAULT true,
    selected_establishment_id BIGINT, -- NOVO
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    FOREIGN KEY (selected_establishment_id) 
        REFERENCES establishments(id) 
        ON DELETE SET NULL
);
```

## Fluxo de Dados

### Registro com Estabelecimento
```
Client Form → JavaScript → API Request → Controller → Service → Repository → Database
                ↓                                                               ↑
          establishmentId                                                     Save
                                                                               ↓
Client Form ← JavaScript ← API Response ← Controller ← Service ← Repository ← Database
    ↓
Session Updated
    ↓
Redirect to Dashboard
```

### Login e Seleção Posterior
```
Login Success → Check selectedEstablishmentId
     ↓                      ↓
     ↓                   Is NULL?
     ↓                      ↓
     ↓                   Show Modal
     ↓                      ↓
     ↓                   User Selects
     ↓                      ↓
     ↓                   PUT /establishment
     ↓                      ↓
     ↓                   Update Database
     ↓                      ↓
     └──────────────────────┘
              ↓
        Dashboard
```

## Tecnologias Utilizadas

- **Backend**: Java 17, Spring Boot 3.2.0, PostgreSQL
- **Frontend**: HTML5, CSS3, JavaScript ES6+, Bootstrap 5.3
- **Padrões**: REST API, MVC, Repository Pattern
- **Segurança**: CORS habilitado, validações de entrada

## Testes Recomendados

1. ✅ Teste unitário: ClientService.registerClient com establishmentId
2. ✅ Teste unitário: ClientService.updateSelectedEstablishment
3. ✅ Teste de integração: POST /api/client/register
4. ✅ Teste de integração: PUT /api/client/establishment
5. ✅ Teste de integração: GET /api/establishment/list
6. ✅ Teste E2E: Fluxo completo de registro
7. ✅ Teste E2E: Fluxo completo de login
8. ✅ Teste de UI: Seletor visual funciona corretamente
9. ✅ Teste de responsividade: Mobile e desktop
10. ✅ Teste de acessibilidade: Navegação por teclado

## Melhorias Futuras

1. 🚀 Cache de lista de estabelecimentos no frontend
2. 🚀 Busca e filtros no seletor de estabelecimentos
3. 🚀 Histórico de estabelecimentos visitados
4. 🚀 Recomendações baseadas em localização
5. 🚀 Favoritos de estabelecimentos
6. 🚀 Notificações quando estabelecimento favorito tem promoções
7. 🚀 Integração com geolocalização para ordenar por proximidade
8. 🚀 Reviews e avaliações dos estabelecimentos
9. 🚀 Compartilhamento de estabelecimentos favoritos
10. 🚀 Sistema de pontos/fidelidade por estabelecimento
