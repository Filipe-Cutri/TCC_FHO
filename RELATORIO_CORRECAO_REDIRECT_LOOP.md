# Relatório de Correção do Loop de Redirecionamento (ERR_TOO_MANY_REDIRECTS)

## 📋 Resumo Executivo

**Problema**: Loop infinito de redirecionamento (302) no deployment Railway (tccfho-production.up.railway.app)

**Causa Raiz**: Railway termina TLS no proxy e encaminha requisições HTTP para a aplicação. O Spring Security força HTTPS mas a aplicação não reconhece os headers `X-Forwarded-Proto` do proxy, resultando em redirecionamentos contínuos.

**Solução Implementada**: Configuração de processamento de headers forwarded para que a aplicação reconheça corretamente o protocolo original da requisição.

---

## 🔧 Mudanças Implementadas

### 1. **application-prod.properties** (Solução Principal)
Adicionada configuração para processar headers de proxy:

```properties
# Forward Headers Strategy - Essential for Railway proxy
# Railway terminates TLS and forwards requests as HTTP with X-Forwarded-* headers
# This tells Spring to trust and process those headers to avoid redirect loops
server.forward-headers-strategy=native
```

**Localização**: `/back-end/src/main/resources/application-prod.properties` (linha 25-28)

**O que faz**: Instrui o Spring Boot a processar nativamente os headers `X-Forwarded-*` (X-Forwarded-Proto, X-Forwarded-Host, X-Forwarded-Port) enviados pelo proxy da Railway.

---

### 2. **ForwardedHeaderConfig.java** (Solução Robusta/Alternativa)
Criada nova classe de configuração com bean `ForwardedHeaderFilter`:

```java
@Configuration
@Profile("prod")
public class ForwardedHeaderConfig {
    @Bean
    public FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter() {
        FilterRegistrationBean<ForwardedHeaderFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new ForwardedHeaderFilter());
        filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return filterRegistrationBean;
    }
}
```

**Localização**: `/back-end/src/main/java/com/slotfy/config/ForwardedHeaderConfig.java`

**O que faz**: 
- Registra `ForwardedHeaderFilter` com precedência máxima
- Garante que os headers sejam processados ANTES de qualquer verificação de segurança
- Funciona como camada adicional de robustez junto com a configuração em properties

---

### 3. **HealthController.java** (Endpoint de Debug Temporário)
Adicionado endpoint para inspeção de headers:

```java
@GetMapping("/__headers")
public ResponseEntity<Map<String, Object>> debugHeaders(HttpServletRequest request) {
    // Coleta todos os headers HTTP e informações da requisição
}
```

**Localização**: `/back-end/src/main/java/com/slotfy/controller/HealthController.java`

**URL**: `GET /api/health/__headers`

**O que faz**:
- Retorna todos os headers HTTP recebidos
- Mostra informações como scheme, isSecure, requestURL, etc.
- Útil para verificar se `X-Forwarded-Proto: https` está sendo processado corretamente

**⚠️ NOTA**: Este endpoint é temporário e deve ser removido após validação em produção.

---

## 🧪 Instruções para Testes

### Teste 1: Verificar Loop de Redirecionamento (ANTES da correção)
```bash
# Deve mostrar múltiplos 302 e erro de "too many redirects"
curl -v --max-redirs 5 https://tccfho-production.up.railway.app
```

**Resultado Esperado (ANTES)**: Loop de 302 redirects

---

### Teste 2: Verificar Correção (DEPOIS do deploy)
```bash
# Deve retornar 200 OK com conteúdo HTML ou JSON
curl -v -L https://tccfho-production.up.railway.app

# Alternativa: Teste sem seguir redirects
curl -v --max-redirs 0 https://tccfho-production.up.railway.app
```

**Resultado Esperado (DEPOIS)**: 
- Status 200 OK
- Conteúdo HTML/JSON da aplicação
- Sem loops de redirect

---

### Teste 3: Inspecionar Headers Forwarded
```bash
# Verificar se X-Forwarded-Proto está sendo processado
curl -v https://tccfho-production.up.railway.app/api/health/__headers | jq
```

**Resultado Esperado**:
```json
{
  "headers": {
    "x-forwarded-proto": "https",
    "x-forwarded-host": "tccfho-production.up.railway.app",
    "x-forwarded-for": "...",
    ...
  },
  "scheme": "https",  // <- Deve ser "https" após a correção
  "isSecure": true,   // <- Deve ser true após a correção
  "requestURL": "https://tccfho-production.up.railway.app/api/health/__headers"
}
```

**⚠️ Campos Importantes**:
- `scheme` deve ser `"https"` (não `"http"`)
- `isSecure` deve ser `true` (não `false`)
- `headers["x-forwarded-proto"]` deve existir e ser `"https"`

---

### Teste 4: Verificar com Header Manual
```bash
# Simular o comportamento do proxy Railway
curl -v -I -H "X-Forwarded-Proto: https" https://tccfho-production.up.railway.app
```

**Resultado Esperado**: Status 200 (sem redirect)

---

### Teste 5: Navegador
1. Abrir navegador em modo anônimo/privado
2. Limpar cache e cookies
3. Acessar: `https://tccfho-production.up.railway.app`
4. Verificar que a página carrega normalmente (sem erro ERR_TOO_MANY_REDIRECTS)

---

## ✅ Checklist de Validação Pós-Deploy

- [ ] `curl -v -L https://tccfho-production.up.railway.app` retorna 200 OK
- [ ] `/api/health/__headers` mostra `"scheme": "https"` e `"isSecure": true`
- [ ] Navegador acessa a aplicação sem loop de redirect
- [ ] Logs da aplicação não mostram múltiplos redirects
- [ ] Funcionalidades da aplicação continuam funcionando normalmente

---

## 📊 Saída Esperada dos Testes

### ANTES da Correção:
```
$ curl -v --max-redirs 2 https://tccfho-production.up.railway.app
< HTTP/2 302
< location: https://tccfho-production.up.railway.app/
...
< HTTP/2 302
< location: https://tccfho-production.up.railway.app/
...
curl: (47) Maximum (2) redirects followed
```

### DEPOIS da Correção:
```
$ curl -v -L https://tccfho-production.up.railway.app
< HTTP/2 200
< content-type: application/json
...
{"status":"UP","application":"Slotfy Backend","version":"1.0.0"}
```

---

## 🔍 Troubleshooting

### Se o problema persistir:

1. **Verificar logs da Railway**:
   ```bash
   # Nos logs da Railway, verificar:
   # - Se o perfil "prod" está ativo
   # - Se não há erros de inicialização
   # - Se ForwardedHeaderFilter está sendo carregado
   ```

2. **Verificar variáveis de ambiente**:
   - `SPRING_PROFILES_ACTIVE=prod` deve estar definida
   - Verificar se não há override de `server.forward-headers-strategy`

3. **Habilitar logs de debug temporariamente**:
   Adicionar em `application-prod.properties`:
   ```properties
   logging.level.org.springframework.security=DEBUG
   logging.level.org.springframework.web.filter.ForwardedHeaderFilter=DEBUG
   ```

4. **Verificar se SSLConfig está ativo**:
   ```bash
   # Nos logs, procurar por:
   # "ServletWebServerFactory" ou "TomcatServletWebServerFactory"
   ```

---

## 📝 Notas Técnicas

### Por que `server.forward-headers-strategy=native`?

- **Opção `native`**: Usa suporte nativo do Servlet container (Tomcat) para processar headers
- **Alternativas**: 
  - `framework`: Usa filtro do Spring Framework (similar ao nosso ForwardedHeaderFilter)
  - `none`: Desabilita processamento (padrão)

### Por que ForwardedHeaderFilter com HIGHEST_PRECEDENCE?

- Garante que headers sejam processados ANTES de qualquer filtro de segurança
- SSLConfig depende de `request.isSecure()` que só retorna `true` após processar `X-Forwarded-Proto`
- Sem isso, SSLConfig vê HTTP e redireciona para HTTPS infinitamente

### Compatibilidade

- Spring Boot 3.x: `server.forward-headers-strategy=native` (recomendado)
- Spring Boot 2.x mais antigos: `server.use-forward-headers=true` (deprecated)

---

## 🚀 Próximos Passos

1. **Deploy para Railway**: As mudanças já estão commitadas e podem ser deployadas
2. **Executar testes de validação**: Usar os comandos curl acima
3. **Validar endpoint `/__headers`**: Verificar se scheme é "https"
4. **Testar no navegador**: Confirmar que não há loop de redirect
5. **Remover endpoint debug**: Após validação, remover `/api/health/__headers`
6. **Monitorar**: Verificar logs por 24-48h para garantir estabilidade

---

## 📚 Referências

- [Spring Boot - Forward Headers](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.webserver.use-behind-a-proxy-server)
- [Railway Proxy Documentation](https://docs.railway.app/deploy/deployments#networking)
- [ForwardedHeaderFilter Documentation](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/filter/ForwardedHeaderFilter.html)

---

**Data**: 2025-10-30  
**Ambiente**: Railway Production (tccfho-production.up.railway.app)  
**Perfil**: prod  
**Spring Boot Version**: 3.2.0
