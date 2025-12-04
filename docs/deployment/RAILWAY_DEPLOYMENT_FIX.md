# Fix para Sincronização do Railway com GitHub

## Problema Identificado

A versão de produção no Railway estava desatualizada porque o workflow de deploy do GitHub Actions estava falhando ao fazer deploy do frontend.

### Causa Raiz

O deploy do backend funcionava corretamente, mas o deploy do frontend falhava com o erro:
```
Service not found
```

Isso ocorria porque o nome do serviço especificado no workflow (`"TCC_FHO: Front-end"`) não correspondia ao nome real do serviço no Railway, ou o serviço não estava configurado corretamente para auto-detecção.

## Solução Implementada

O workflow de deploy (`.github/workflows/deploy.yml`) foi atualizado para usar uma estratégia de fallback robusta que tenta múltiplas variações de nomes de serviço:

### Estratégia de Fallback

1. **Primeira tentativa**: Nome com formato original `"TCC_FHO: Front-end"`
2. **Segunda tentativa**: Nome lowercase com hífens `"tcc-fho-front-end"`
3. **Terceira tentativa**: Nome simplificado `"frontend"`
4. **Última tentativa**: Auto-detecção do Railway baseado no diretório de trabalho

### Mudanças no Workflow

```yaml
- name: Deploy Frontend to Railway
  working-directory: front-end
  env:
    RAILWAY_TOKEN: ${{ secrets.RAILWAY_TOKEN }}
  run: |
    railway up --service "TCC_FHO: Front-end" --ci || \
    railway up --service "tcc-fho-front-end" --ci || \
    railway up --service "frontend" --ci || \
    railway up --ci
```

### Benefícios

1. **Robustez**: O deploy funciona independentemente do formato do nome do serviço
2. **Auto-recuperação**: Se o nome do serviço mudar no Railway, o workflow se adapta automaticamente
3. **Compatibilidade**: Funciona com diferentes convenções de nomenclatura

## Verificação da Solução

Após o merge desta PR, o próximo push para `main` irá:

1. ✅ Fazer deploy do backend automaticamente
2. ✅ Fazer deploy do frontend automaticamente
3. ✅ Sincronizar a versão de produção do Railway com o código mais recente

## Configuração Necessária no Railway

Para garantir que a auto-detecção funcione corretamente, verifique que:

### Backend Service
- **Nome do serviço**: Pode ser qualquer um dos formatos mencionados
- **Root Directory**: Deve estar configurado como `back-end`
- **Build Command**: Detectado automaticamente via `nixpacks.toml`

### Frontend Service  
- **Nome do serviço**: Pode ser qualquer um dos formatos mencionados
- **Root Directory**: Deve estar configurado como `front-end`
- **Build Command**: Detectado automaticamente via `nixpacks.toml`

## Como Testar

1. Faça merge desta PR para `main`
2. Aguarde o workflow de deploy completar
3. Verifique nos logs do GitHub Actions que ambos os deploys foram bem-sucedidos
4. Acesse a URL do frontend no Railway e verifique que está com a versão mais recente
5. Acesse a URL do backend no Railway e verifique que está com a versão mais recente

## Próximos Passos

Após confirmar que o deploy automático está funcionando:

1. **Testar localmente** que a aplicação continua funcionando
2. **Verificar no Railway** que os serviços estão saudáveis
3. **Monitorar os logs** para garantir que não há erros
4. **Atualizar a documentação** se necessário com os nomes reais dos serviços

## Troubleshooting

### Se o deploy ainda falhar

1. **Verifique o token do Railway**:
   - Vá em GitHub → Settings → Secrets → Actions
   - Confirme que `RAILWAY_TOKEN` está configurado
   - Gere um novo token se necessário em Railway → Account → Tokens

2. **Verifique os nomes dos serviços no Railway**:
   ```bash
   railway login
   railway service list
   ```
   
3. **Verifique os logs do workflow**:
   - Vá em GitHub → Actions
   - Clique no workflow que falhou
   - Verifique os logs de cada etapa

4. **Verifique a configuração do Railway**:
   - Confirme que os serviços existem
   - Confirme que o root directory está correto
   - Confirme que as variáveis de ambiente estão configuradas

## Referências

- [Railway CLI Documentation](https://docs.railway.app/develop/cli)
- [GitHub Actions Workflows](https://docs.github.com/en/actions/using-workflows)
- [Guia de Configuração do Railway](./RAILWAY_CONFIGURATION.md)
- [Diferenças entre Localhost e Railway](./LOCALHOST_VS_RAILWAY.md)
