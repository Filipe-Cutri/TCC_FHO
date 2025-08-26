# IntelliJ IDEA - Configuração Rápida do Projeto Slotfy

## Importação e Configuração Inicial

### 1. Abrir o Projeto
1. Abra o IntelliJ IDEA
2. Clique em **"Open"**
3. Navegue até `TCC_FHO/back-end/`
4. Selecione o arquivo `build.gradle`
5. Clique em **"Open as Project"**

### 2. Configurações do Projeto
- **Project SDK**: Java 17
- **Language Level**: 17 - Sealed types, always-strict floating-point semantics
- **Project Compiler Output**: `back-end/build/classes`

### 3. Gradle Settings
- **Build and run using**: Gradle
- **Run tests using**: Gradle
- **Distribution**: Gradle Wrapper

## Configurações de Execução

### Run Configuration Principal
1. Vá em **Run > Edit Configurations**
2. Clique em **"+"** > **Application**
3. Configure:
   - **Name**: `Slotfy Application`
   - **Main class**: `com.slotfy.SlotfyApplication`
   - **Program arguments**: (vazio)
   - **VM options**: `-Dspring.profiles.active=test`
   - **Working directory**: `$MODULE_WORKING_DIR$`
   - **Environment variables**: (vazio)

### Run Configuration com Profile de Produção
1. Duplique a configuração anterior
2. Configure:
   - **Name**: `Slotfy Application (Prod)`
   - **VM options**: `-Dspring.profiles.active=prod`

## Shortcuts Úteis

### Execução
- **Ctrl+Shift+F10** (Windows/Linux) / **Cmd+Shift+R** (Mac): Run
- **Ctrl+Shift+F9** (Windows/Linux) / **Cmd+Shift+D** (Mac): Debug
- **Ctrl+F2** (Windows/Linux) / **Cmd+F2** (Mac): Stop

### Gradle
- **Ctrl+Shift+A** > "Gradle" > **Reload Gradle Project**
- **View > Tool Windows > Gradle** para abrir painel do Gradle

## Plugins Recomendados

1. **Spring Boot** (geralmente já instalado)
2. **Lombok** (para annotations @Data, @Service, etc.)
3. **Database Navigator** (para visualizar H2 database)
4. **Git Integration** (geralmente já instalado)

## URLs de Teste Rápido

Após executar a aplicação:
- **Homepage**: http://localhost:8080
- **API Info**: http://localhost:8080/api/info
- **H2 Console**: http://localhost:8080/h2-console
- **Client Login**: http://localhost:8080/pages/client/client-login.html

## Estrutura de Pastas no IntelliJ

```
back-end/
├── src/main/java/com/slotfy/
│   ├── SlotfyApplication.java          # Classe principal ▶️
│   ├── config/                         # Configurações
│   ├── controller/                     # REST Controllers
│   ├── dto/                           # Data Transfer Objects  
│   ├── exception/                     # Exception Handlers
│   ├── model/                         # Entidades JPA
│   ├── repository/                    # Repositórios Spring Data
│   └── service/                       # Lógica de negócio
├── src/main/resources/
│   ├── application*.properties        # Configurações da aplicação
│   └── static/                        # Frontend (HTML, CSS, JS)
└── src/test/java/                     # Testes unitários
```

## Comandos Gradle no Terminal do IntelliJ

```bash
# Executar aplicação
./gradlew bootRun

# Executar com profile específico
./gradlew bootRun -Dspring.profiles.active=test

# Executar testes
./gradlew test

# Build completo
./gradlew build

# Limpar build
./gradlew clean
```

## Debugging

### Breakpoints
1. Clique na margem esquerda do editor para adicionar breakpoint
2. Execute em modo debug (ícone do inseto 🐛)
3. Use **F8** para step over, **F7** para step into

### Logs no Console
- Logs da aplicação aparecem no console do IntelliJ
- Use **Ctrl+F** para buscar nos logs
- Clique direito no console > **Clear All** para limpar

## Solução de Problemas no IntelliJ

### Dependências não reconhecidas
1. **File > Invalidate Caches and Restart**
2. Ou no painel Gradle: clique no ícone de refresh 🔄

### Erro de compilação
1. **Build > Rebuild Project**
2. Verificar se Java 17 está configurado corretamente

### Hot Reload não funciona
1. **File > Settings > Build > Compiler**
2. Marque **"Build project automatically"**
3. **Ctrl+Shift+A** > "Registry" > marque **"compiler.automake.allow.when.app.running"**

---
*Configuração otimizada para desenvolvimento com IntelliJ IDEA*