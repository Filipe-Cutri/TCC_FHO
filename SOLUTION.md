# Fix para Erro HTTP 500 no Endpoint Raiz

## Problema
Ao acessar `http://localhost:8080/` após criar o banco e executar o Spring Boot, era retornado um erro HTTP 500:

```json
{
  "error": "Internal Server Error",
  "message": "An unexpected error occurred",
  "timestamp": "2025-08-18T17:14:58.3927357",
  "status": 500
}
```

## Causa Raiz
O problema era que não havia nenhum controller mapeado para tratar requisições ao caminho raiz `/`. Quando o Spring Boot recebia uma requisição para esse endpoint, não encontrava um handler apropriado e lançava uma exceção, que era capturada pelo GlobalExceptionHandler retornando o erro 500.

## Solução
1. **Criado RootController**: Adicionado um novo controller (`RootController.java`) que mapeia o endpoint raiz `/` e retorna uma resposta JSON informativa com:
   - Status da aplicação
   - Informações sobre os endpoints disponíveis
   - Mensagem de boas-vindas

2. **Corrigido problema de build**: Atualizado o Gradle de versão 9.0.0 para 8.4 para compatibilidade com Spring Boot 3.2.0

## Resultado
Agora ao acessar `http://localhost:8080/`, é retornada uma resposta JSON válida:

```json
{
  "application": "Slotfy Backend",
  "version": "1.0.0", 
  "status": "running",
  "message": "Bem-vindo ao Slotfy! Use os endpoints da API para interagir com o sistema.",
  "endpoints": {
    "health": "/api/health",
    "establishment_login": "/api/establishment/login",
    "client_forgot_password": "/api/client/forgot-password",
    "establishment_forgot_password": "/api/establishment/forgot-password"
  }
}
```

## Arquivos Modificados
- `back-end/src/main/java/com/slotfy/controller/RootController.java` (novo)
- `back-end/gradle/wrapper/gradle-wrapper.properties` (versão do Gradle)
- `back-end/build.gradle` (adicionado H2 como dependência runtime para testes)
- `back-end/src/main/resources/application-test.properties` (novo profile de teste)