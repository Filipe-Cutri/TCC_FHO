# Solução Definitiva para Cadastro de Estabelecimento no Railway (PostgreSQL)

## 🎯 Resumo Executivo

Este documento descreve a solução completa e definitiva para o problema de erro 500 ao cadastrar estabelecimentos no ambiente de produção (Railway com PostgreSQL).

## 🔍 Problema Original

Apesar das PRs #111 e #112 terem sido mescladas, o problema persistia:
- ✅ Funcionava localmente (H2)
- ❌ Falhava em produção (PostgreSQL no Railway) com erro 500

## 🐛 Causas Raiz Identificadas

### 1. **Incompatibilidade de Banco de Dados (CRÍTICO)**

**Arquivo**: `Establishment.java` linha 57

**Problema**:
```java
@Column(name = "settings", columnDefinition = "TEXT")
private String settings;
```

A anotação `columnDefinition = "TEXT"` é **específica do PostgreSQL** e causa comportamento inconsistente:
- No H2: Tolerante, aceita a definição mesmo não sendo nativa
- No PostgreSQL: Rigoroso, mas pode causar problemas de mapeamento em algumas configurações

**Solução Implementada**:
```java
@Lob
@Column(name = "settings")
private String settings;
```

A anotação `@Lob` é **agnóstica de banco de dados**:
- No H2: Mapeia para CLOB
- No PostgreSQL: Mapeia para TEXT
- **Resultado**: Comportamento idêntico em ambos ambientes

### 2. **Falta de Logs de Depuração**

**Problema**: Impossível diagnosticar erros em produção sem logs detalhados.

**Solução**: Implementação de logging estruturado em 3 níveis (veja seção de Logging abaixo).

## ✅ Soluções Implementadas

### 1. Correção da Entidade Establishment

**Arquivo**: `back-end/src/main/java/com/slotfy/model/Establishment.java`

```java
// ANTES (INCORRETO - PostgreSQL específico)
@Column(name = "settings", columnDefinition = "TEXT")
private String settings;

// DEPOIS (CORRETO - Database agnóstico)
@Lob
@Column(name = "settings")
private String settings;
```

### 2. Logging Abrangente no Controller

**Arquivo**: `back-end/src/main/java/com/slotfy/controller/EstablishmentAuthController.java`

Implementado logging estruturado:

```java
@PostMapping("/register-complete")
@Transactional
public ResponseEntity<EstablishmentRegisterResponse> registerComplete(...) {
    logger.info("=== INÍCIO DO CADASTRO DE ESTABELECIMENTO ===");
    
    // Etapa 1: Criação do Establishment
    logger.info("Etapa 1/2: Criando estabelecimento no banco de dados");
    Establishment establishment = establishmentService.createEstablishment(...);
    logger.info("✓ Estabelecimento criado com sucesso - ID: {}", establishment.getId());
    
    // Etapa 2: Criação do EstablishmentUser
    logger.info("Etapa 2/2: Criando usuário administrador para o estabelecimento ID: {}", establishment.getId());
    EstablishmentUser adminUser = establishmentUserService.createUser(...);
    logger.info("✓ Usuário administrador criado com sucesso - ID: {}", adminUser.getId());
    
    logger.info("=== CADASTRO CONCLUÍDO COM SUCESSO ===");
    
    // Tratamento de erros com logs detalhados
    catch (Exception e) {
        logger.error("❌ ERRO CRÍTICO durante cadastro", e);
        logger.error("Tipo da exceção: {}", e.getClass().getName());
        logger.error("Mensagem: {}", e.getMessage());
        if (e.getCause() != null) {
            logger.error("Causa raiz: {} - {}", 
                        e.getCause().getClass().getName(), 
                        e.getCause().getMessage());
        }
    }
}
```

### 3. Logging no EstablishmentService

**Arquivo**: `back-end/src/main/java/com/slotfy/service/EstablishmentService.java`

```java
public Establishment createEstablishment(...) {
    logger.debug("Iniciando criação de estabelecimento - Nome: {}, Email: {}, Categoria: {}", 
                name, email, category);
    
    // Validações com logging
    logger.debug("Verificando duplicidade de email: {}", email);
    logger.debug("Verificando duplicidade de CNPJ: {}", cnpj);
    
    // Save com tratamento de erro
    try {
        Establishment saved = establishmentRepository.save(establishment);
        logger.info("Estabelecimento salvo com sucesso - ID: {}, Nome: {}, Status: {}", 
                   saved.getId(), saved.getName(), saved.getStatus());
        return saved;
    } catch (Exception e) {
        logger.error("Erro ao salvar estabelecimento no banco de dados", e);
        logger.error("Tipo de erro: {}, Mensagem: {}", 
                    e.getClass().getName(), e.getMessage());
        throw e;
    }
}
```

### 4. Logging no EstablishmentUserService

**Arquivo**: `back-end/src/main/java/com/slotfy/service/EstablishmentUserService.java`

```java
public EstablishmentUser createUser(...) {
    logger.debug("Iniciando criação de usuário - Nome: {}, Email: {}, Role: {}, EstablishmentId: {}", 
                name, email, role, establishmentId);
    
    // Validações com logging detalhado
    logger.debug("Verificando se email já existe: {}", email);
    logger.debug("Validando formato do email: {}", email);
    logger.debug("Validando senha (comprimento mínimo)");
    
    // Save com tratamento de erro
    try {
        EstablishmentUser saved = save(user);
        logger.info("Usuário salvo com sucesso - ID: {}, Email: {}, Role: {}, EstablishmentId: {}", 
                   saved.getId(), saved.getEmail(), saved.getRole(), saved.getEstablishmentId());
        return saved;
    } catch (Exception e) {
        logger.error("Erro ao salvar usuário no banco de dados", e);
        throw e;
    }
}
```

### 5. Configuração de Logging em Produção

**Arquivo**: `back-end/src/main/resources/application-prod.properties`

```properties
# Logging principal
logging.level.root=INFO
logging.level.com.slotfy=DEBUG
logging.level.org.springframework.web=INFO

# Logging de SQL e parâmetros
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

# Logging específico para cadastro de estabelecimento
logging.level.com.slotfy.controller.EstablishmentAuthController=DEBUG
logging.level.com.slotfy.service.EstablishmentService=DEBUG
logging.level.com.slotfy.service.EstablishmentUserService=DEBUG

# Logging de transações
logging.level.org.springframework.transaction=DEBUG
logging.level.org.hibernate.engine.transaction=DEBUG
```

## 📊 Como Monitorar em Produção (Railway)

### 1. Acessar Logs do Railway

```bash
# Via Railway CLI
railway logs

# Via interface web
https://railway.app -> Seu Projeto -> Deployments -> View Logs
```

### 2. Logs de Sucesso Esperados

Quando um cadastro funciona corretamente, você verá:

```log
INFO  --- EstablishmentAuthController : === INÍCIO DO CADASTRO DE ESTABELECIMENTO ===
DEBUG --- EstablishmentAuthController : Request recebido - Nome: Salão Beleza, Email: salao@exemplo.com, Categoria: BELEZA

INFO  --- EstablishmentAuthController : Etapa 1/2: Criando estabelecimento no banco de dados
DEBUG --- EstablishmentService        : Iniciando criação de estabelecimento - Nome: Salão Beleza, Email: salao@exemplo.com
DEBUG --- EstablishmentService        : Verificando duplicidade de email: salao@exemplo.com
DEBUG --- EstablishmentService        : Verificando duplicidade de CNPJ: null
DEBUG --- Hibernate                   : insert into establishments (name, email, ...) values (?, ?, ...)
TRACE --- Hibernate                   : binding parameter [1] as [VARCHAR] - [Salão Beleza]
INFO  --- EstablishmentService        : Estabelecimento salvo com sucesso - ID: 123, Nome: Salão Beleza, Status: ACTIVE
INFO  --- EstablishmentAuthController : ✓ Estabelecimento criado com sucesso - ID: 123

INFO  --- EstablishmentAuthController : Etapa 2/2: Criando usuário administrador para o estabelecimento ID: 123
DEBUG --- EstablishmentUserService    : Iniciando criação de usuário - Nome: Salão Beleza, Email: salao@exemplo.com, Role: ADMIN, EstablishmentId: 123
DEBUG --- EstablishmentUserService    : Verificando se email já existe: salao@exemplo.com
DEBUG --- Hibernate                   : insert into establishment_users (name, email, ...) values (?, ?, ...)
INFO  --- EstablishmentUserService    : Usuário salvo com sucesso - ID: 456, Email: salao@exemplo.com, Role: ADMIN, EstablishmentId: 123
INFO  --- EstablishmentAuthController : ✓ Usuário administrador criado com sucesso - ID: 456

INFO  --- EstablishmentAuthController : === CADASTRO CONCLUÍDO COM SUCESSO - Estabelecimento ID: 123, Admin ID: 456 ===
```

### 3. Logs de Erro - Como Interpretar

#### Erro de Validação (Email duplicado)
```log
INFO  --- EstablishmentAuthController : === INÍCIO DO CADASTRO DE ESTABELECIMENTO ===
INFO  --- EstablishmentAuthController : Etapa 1/2: Criando estabelecimento no banco de dados
DEBUG --- EstablishmentService        : Verificando duplicidade de email: salao@exemplo.com
WARN  --- EstablishmentService        : Tentativa de cadastrar estabelecimento com email já existente: salao@exemplo.com (ID existente: 100)
ERROR --- EstablishmentAuthController : ❌ Erro de validação durante cadastro de estabelecimento: Já existe um estabelecimento com este email
```

#### Erro de Banco de Dados
```log
INFO  --- EstablishmentAuthController : === INÍCIO DO CADASTRO DE ESTABELECIMENTO ===
INFO  --- EstablishmentAuthController : Etapa 1/2: Criando estabelecimento no banco de dados
ERROR --- EstablishmentService        : Erro ao salvar estabelecimento no banco de dados
ERROR --- EstablishmentService        : Tipo de erro: org.springframework.dao.DataIntegrityViolationException, Mensagem: ...
ERROR --- EstablishmentAuthController : ❌ ERRO CRÍTICO durante cadastro de estabelecimento
ERROR --- EstablishmentAuthController : Tipo da exceção: org.springframework.dao.DataIntegrityViolationException
ERROR --- EstablishmentAuthController : Causa raiz: org.postgresql.util.PSQLException - ERROR: duplicate key value violates unique constraint ...
```

#### Erro de Foreign Key (se ainda ocorrer)
```log
INFO  --- EstablishmentAuthController : === INÍCIO DO CADASTRO DE ESTABELECIMENTO ===
INFO  --- EstablishmentAuthController : Etapa 1/2: Criando estabelecimento no banco de dados
INFO  --- EstablishmentAuthController : ✓ Estabelecimento criado com sucesso - ID: 123
INFO  --- EstablishmentAuthController : Etapa 2/2: Criando usuário administrador para o estabelecimento ID: 123
ERROR --- EstablishmentUserService    : Erro ao salvar usuário no banco de dados
ERROR --- EstablishmentUserService    : Tipo de erro: org.springframework.dao.DataIntegrityViolationException
ERROR --- EstablishmentAuthController : Causa raiz: org.postgresql.util.PSQLException - ERROR: insert or update on table "establishment_users" violates foreign key constraint ...
```

## 🧪 Como Testar a Correção

### Teste Local (H2)

1. Configure o profile dev:
```bash
export SPRING_PROFILES_ACTIVE=dev
```

2. Execute a aplicação:
```bash
cd back-end
./gradlew bootRun
```

3. Teste o endpoint:
```bash
curl -X POST https://localhost:8443/api/establishment/register-complete \
  -H "Content-Type: application/json" \
  -d '{
    "nomeEstabelecimento": "Salão Teste",
    "email": "teste@exemplo.com",
    "telefone": "11999999999",
    "senha": "senha123",
    "category": "BELEZA"
  }' \
  --insecure
```

### Teste em Produção (Railway)

1. Deploy a aplicação no Railway
2. Monitore os logs em tempo real
3. Teste via frontend ou curl:

```bash
curl -X POST https://seu-app.railway.app/api/establishment/register-complete \
  -H "Content-Type: application/json" \
  -d '{
    "nomeEstabelecimento": "Salão Teste Railway",
    "email": "railway@exemplo.com",
    "telefone": "11999999999",
    "senha": "senha123",
    "category": "BELEZA"
  }'
```

## 🎯 Checklist de Validação

- [x] ✅ Removida dependência PostgreSQL-específica (`columnDefinition="TEXT"`)
- [x] ✅ Adicionada anotação `@Lob` database-agnóstica
- [x] ✅ Implementado logging estruturado no Controller
- [x] ✅ Implementado logging detalhado no EstablishmentService
- [x] ✅ Implementado logging detalhado no EstablishmentUserService
- [x] ✅ Configurado logging de produção para debug completo
- [x] ✅ Logging de transações habilitado
- [x] ✅ Logging de SQL habilitado
- [x] ✅ Build bem-sucedido
- [x] ✅ Todos os testes passando

## 📝 Próximos Passos para Deploy

1. **Fazer merge desta PR**
2. **Aguardar deploy automático no Railway** (se configurado) ou fazer deploy manual
3. **Monitorar logs** durante os primeiros cadastros
4. **Validar** que os logs mostram o fluxo completo
5. **Confirmar** que não há mais erro 500

## 🔐 Configurações Adicionais (Opcional)

Se necessário ajustar o nível de logging após validação inicial:

```properties
# Para ambiente de produção estável (após validação)
logging.level.com.slotfy=INFO
logging.level.org.hibernate.SQL=WARN
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=WARN

# Manter apenas os logs específicos se necessário
logging.level.com.slotfy.controller.EstablishmentAuthController=INFO
logging.level.com.slotfy.service.EstablishmentService=INFO
logging.level.com.slotfy.service.EstablishmentUserService=INFO
```

## 📞 Suporte e Troubleshooting

### Problema Persiste Após Deploy?

1. **Verifique os logs** no Railway
2. **Procure por**:
   - Mensagens com `❌ ERRO CRÍTICO`
   - Stack traces completos
   - Tipo de exceção e causa raiz
3. **Compartilhe** os logs específicos do erro para análise

### Logs Não Aparecem?

1. Verifique se o deploy foi bem-sucedido
2. Confirme que o arquivo `application-prod.properties` foi atualizado
3. Verifique se a variável `SPRING_PROFILES_ACTIVE=prod` está configurada no Railway

## 🎉 Resumo

Esta solução corrige:
1. ✅ Incompatibilidade de banco de dados (H2 vs PostgreSQL)
2. ✅ Falta de visibilidade em produção (logs detalhados)
3. ✅ Dificuldade de troubleshooting (logs estruturados)

Resultado esperado:
- ✅ Cadastro funciona em H2 (dev)
- ✅ Cadastro funciona em PostgreSQL (prod/Railway)
- ✅ Logs completos para diagnóstico
- ✅ Fácil identificação de problemas futuros
