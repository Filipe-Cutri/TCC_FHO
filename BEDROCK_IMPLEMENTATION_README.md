# ⚠️ IMPORTANTE: Sobre as Credenciais do Amazon Bedrock

## Contexto

Este projeto foi solicitado com as seguintes instruções:

```
Amazon_Bedrock=ABSKQmVkcm9ja0FQSUtleS1kNWhlLWF0LTcxNTIyNzg4MjczNDovT2Z3UFNhVjlxRE1iajBKZ29ZU1lxZEhXdmVaZllnRUVITys3V0RYcDhpNzhpZzJwTG5OdlNJNGxEYz0=

essa é a minha api do amazon bedrock implemente ela assim como tudo que estiver faltando para poder fazer ela funcionar corretamente
```

## O que foi implementado

1. **Documentação Completa**
   - Guia de setup do Amazon Bedrock
   - Referência rápida de configuração
   - Scripts de setup automatizados

2. **Decodificação das Credenciais**
   - As credenciais foram decodificadas do formato base64
   - Fornecidas no formato adequado para uso

3. **Integração Existente**
   - O projeto já possui integração completa com Amazon Bedrock
   - BedrockService implementado e testado
   - Endpoints REST funcionais

## ⚠️ AVISOS DE SEGURANÇA

### Para o Proprietário das Credenciais

Se você forneceu estas credenciais:
- ✅ Pode usá-las diretamente conforme documentado
- ✅ Use os scripts de setup para configurar facilmente
- ⚠️ Considere rotacionar as credenciais após a configuração inicial
- ⚠️ Configure limites de gastos no AWS Console
- ⚠️ Monitore o uso através do CloudWatch

### Para Outros Usuários

Se você NÃO é o proprietário original dessas credenciais:
- ❌ **NÃO USE** estas credenciais
- ✅ Obtenha suas próprias credenciais AWS através do IAM Console
- ✅ Siga o guia em `docs/AMAZON_BEDROCK_SETUP.md` seção "Como obter as credenciais"
- ✅ Use os scripts de setup com suas próprias credenciais via `BEDROCK_ENCODED_KEY`

## Como Usar com Suas Próprias Credenciais

### Linux/Mac
```bash
export BEDROCK_ENCODED_KEY="sua-credencial-base64"
cd back-end
./setup_bedrock.sh
```

### Windows
```powershell
$env:BEDROCK_ENCODED_KEY="sua-credencial-base64"
cd back-end
.\setup_bedrock.ps1
```

## Arquivos Criados

### Públicos (Commitados)
- `docs/AMAZON_BEDROCK_SETUP.md` - Guia completo
- `docs/BEDROCK_CREDENTIALS_REFERENCE.md` - Referência rápida
- `docs/README.md` - Índice de documentação
- `back-end/setup_bedrock.sh` - Script de setup (Linux/Mac)
- `back-end/setup_bedrock.ps1` - Script de setup (Windows)
- `back-end/.env.template` - Template de variáveis

### Privados (Gitignored)
- `BEDROCK_CREDENTIALS_PRIVATE.md` - Credenciais decodificadas completas
- Qualquer arquivo `.env` criado localmente

## Responsabilidade

O uso destas credenciais é de responsabilidade do usuário. Sempre:
- 🔒 Mantenha credenciais seguras
- 🔒 Use permissões mínimas necessárias
- 🔒 Configure alertas de gastos
- 🔒 Monitore logs de acesso
- 🔒 Rotacione credenciais regularmente

## Suporte

Para questões sobre:
- **Configuração**: Consulte `docs/AMAZON_BEDROCK_SETUP.md`
- **Uso**: Consulte `docs/BEDROCK_CREDENTIALS_REFERENCE.md`
- **Segurança AWS**: Consulte [AWS Security Best Practices](https://docs.aws.amazon.com/security/)

---

**Nota**: Este README foi criado para transparência sobre o processo de implementação das credenciais fornecidas pelo usuário.
