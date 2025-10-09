# Guia de Uso - Agendamento com IA e Manual

## 🎯 Visão Geral

O sistema Slotfy agora possui duas formas completas e funcionais de agendamento:

1. **Agendamento com IA** - Recomendações inteligentes e automáticas
2. **Agendamento Manual** - Controle total pelo usuário

Ambos os métodos estão **totalmente implementados** e **funcionais** com integração backend completa.

---

## 🤖 Agendamento com IA

### Como Funciona

A IA analisa automaticamente:
- ✅ Seu histórico de agendamentos
- ✅ Suas preferências de horário (manhã, tarde, noite)
- ✅ Seu orçamento preferido
- ✅ Avaliações dos profissionais
- ✅ Disponibilidade real de horários
- ✅ Taxa de satisfação dos profissionais

### Passo a Passo

#### 1. Acesse a Página de Serviços
```
URL: https://localhost:8443/pages/client/client-services.html
```

#### 2. Localize a Seção de IA
Procure pelo card destacado no topo da página:
```
┌─────────────────────────────────────────────┐
│  🤖 Agendamento Inteligente           [IA]  │
│                                              │
│  Nossa IA analisa sua disponibilidade,      │
│  histórico e horários dos profissionais     │
│                                              │
│  ┌──────────────────────────────────────┐  │
│  │  ✨ Deixe a IA Escolher              │  │
│  │     O Melhor Horário Para Você       │  │
│  └──────────────────────────────────────┘  │
└─────────────────────────────────────────────┘
```

#### 3. Clique no Botão da IA
- O botão mostrará: "IA Analisando..."
- A IA processará suas preferências (leva ~2 segundos)

#### 4. Revise a Recomendação
Você verá algo como:
```
🤖 IA do Slotfy encontrou a melhor opção!

✂️ Corte + Barba com João Silva
📅 20/01/2024 às 14:00
💰 R$ 65,00 (95% confiança)

💡 profissional com excelente avaliação (4.8/5.0), 
   horário vespertino conforme sua preferência

Aceitar esta recomendação?
```

#### 5. Confirme ou Recuse
- **Aceitar**: Agendamento é criado automaticamente
- **Recusar**: Tente novamente ou use o agendamento manual

#### 6. Confirmação
Se aceitar, você verá:
```
✅ Perfeito! Agendamento confirmado:
Corte + Barba com João Silva
20/01/2024 às 14:00
```

E será redirecionado para sua página de agendamentos.

---

## 📋 Agendamento Manual

### Como Funciona

Você tem controle total sobre:
- ✅ Escolha do serviço
- ✅ Escolha do profissional
- ✅ Escolha da data
- ✅ Escolha do horário
- ✅ Observações especiais

### Passo a Passo

#### 1. Navegue até a Seção Manual
Role a página até encontrar:
```
┌─────────────────────────────────────────────┐
│  📅 Agendamento Manual                      │
│                                              │
│  Escolha seu serviço, profissional e        │
│  horário preferido                          │
└─────────────────────────────────────────────┘
```

#### 2. Visualize os Serviços Disponíveis
Você verá cards como:
```
┌──────────────────────────┐
│  Corte Masculino         │
│  Corte tradicional       │
│  💰 R$ 40,00             │
│  ⏱️ 30 minutos           │
│  ┌────────────────────┐ │
│  │    🗓️ Agendar      │ │
│  └────────────────────┘ │
└──────────────────────────┘
```

#### 3. Clique em "Agendar" no Serviço Desejado
Um modal será aberto mostrando:

```
┌─────────────────────────────────────────────┐
│  📅 Agendar Serviço                    [X]  │
├─────────────────────────────────────────────┤
│                                              │
│  ✂️ Serviço Selecionado                     │
│  ┌────────────────────────────────────────┐│
│  │ Corte Masculino                        ││
│  │ Duração: 30 minutos | Preço: R$ 40,00 ││
│  └────────────────────────────────────────┘│
│                                              │
│  👔 Escolha o Profissional                  │
│  ┌────────────────────────────────────────┐│
│  │ ▼ Selecione um profissional            ││
│  └────────────────────────────────────────┘│
│                                              │
│  📅 Escolha a Data                          │
│  ┌────────────────────────────────────────┐│
│  │ [Data Picker]                          ││
│  └────────────────────────────────────────┘│
│                                              │
│  🕐 Escolha o Horário                       │
│  ┌────────────────────────────────────────┐│
│  │ ▼ Selecione um horário                 ││
│  └────────────────────────────────────────┘│
│                                              │
│  💬 Observações (opcional)                  │
│  ┌────────────────────────────────────────┐│
│  │ Alguma observação especial?            ││
│  │                                        ││
│  └────────────────────────────────────────┘│
│                                              │
│  [Cancelar]          [✓ Confirmar]         │
└─────────────────────────────────────────────┘
```

#### 4. Preencha os Campos

**a) Profissional:**
```
▼ João Silva - Especialista
  Carlos Santos - Especialista
  Maria Oliveira - Especialista
```

**b) Data:**
- Escolha qualquer data a partir de hoje
- Não é possível selecionar datas passadas

**c) Horário:**
```
▼ 08:00
  08:30
  09:00
  09:30
  ...
  17:30
  18:00
```
*Horários disponíveis: 8h às 18h, em intervalos de 30 minutos*

**d) Observações (opcional):**
```
Ex: "Prefiro um corte mais curto nas laterais"
    "Tenho pouco tempo, preciso ser rápido"
```

#### 5. Confirme o Agendamento
- Clique em "Confirmar Agendamento"
- O botão mostrará: "Processando..."

#### 6. Confirmação
Você verá:
```
✅ Agendamento realizado com sucesso!

Serviço: Corte Masculino
Data: 20/01/2024
Horário: 14:00
```

E será redirecionado para sua página de agendamentos.

---

## 🔄 Comparação: IA vs Manual

| Característica | IA | Manual |
|----------------|----|----|
| **Velocidade** | ⚡⚡⚡ Muito rápido (2 cliques) | ⚡⚡ Moderado (5+ cliques) |
| **Controle** | 🎯 Limitado | 🎯🎯🎯 Total |
| **Personalização** | 🤖 Baseada em histórico | 👤 100% do usuário |
| **Melhor para** | Quem confia na IA e quer rapidez | Quem quer escolher tudo |
| **Recomendações** | ✅ Sim | ❌ Não |
| **Flexibilidade** | 🔒 Aceitar ou recusar | 🔓 Escolha livre |

---

## 🎓 Dicas de Uso

### Para Obter Melhores Recomendações da IA

1. **Configure suas preferências:**
   ```javascript
   // No localStorage do navegador (F12 → Console)
   localStorage.setItem('clientPreferences', JSON.stringify({
     preferredTimes: ['afternoon', 'evening'],
     budget: 'medium',
     serviceHistory: []
   }));
   ```

2. **Use o sistema regularmente:**
   - Quanto mais você usa, melhor a IA entende suas preferências
   - A IA aprende com seu histórico de agendamentos

3. **Aceite recomendações que fazem sentido:**
   - A IA melhora conforme você fornece feedback implícito

### Para Agendamento Manual Eficiente

1. **Seja específico nas observações:**
   - Isso ajuda o profissional a se preparar melhor

2. **Agende com antecedência:**
   - Mais opções de horário disponíveis

3. **Considere horários alternativos:**
   - Se seu horário preferido está ocupado

---

## 🛠️ Solução de Problemas

### Problema: "Nenhuma recomendação disponível"

**Possíveis causas:**
- Não há profissionais disponíveis no estabelecimento
- Todos os horários estão ocupados
- Estabelecimento não tem serviços ativos

**Solução:**
1. Tente novamente mais tarde
2. Use o agendamento manual
3. Selecione outro estabelecimento

### Problema: "Horário não disponível"

**Causa:**
- Outro cliente agendou no mesmo horário entre a recomendação e a confirmação

**Solução:**
1. Tente obter nova recomendação da IA
2. Use agendamento manual e escolha outro horário

### Problema: "Sessão expirada"

**Causa:**
- Seu login expirou

**Solução:**
1. Faça login novamente
2. Tente o agendamento novamente

### Problema: Modal não abre

**Causa:**
- JavaScript não carregado ou erro no navegador

**Solução:**
1. Recarregue a página (F5)
2. Limpe o cache do navegador (Ctrl+Shift+Del)
3. Tente em modo anônimo

---

## 📱 Compatibilidade

### Navegadores Suportados
- ✅ Chrome 90+
- ✅ Firefox 88+
- ✅ Safari 14+
- ✅ Edge 90+

### Dispositivos
- ✅ Desktop
- ✅ Tablet
- ✅ Mobile

---

## 🔐 Segurança

### Validações Implementadas

**Backend:**
- ✅ Autenticação de usuário
- ✅ Validação de campos obrigatórios
- ✅ Verificação de disponibilidade real
- ✅ Proteção contra agendamentos duplicados

**Frontend:**
- ✅ Validação de sessão
- ✅ Validação de datas (não permite passado)
- ✅ Sanitização de inputs
- ✅ Feedback de erros

---

## 📊 Dados Enviados

### Agendamento com IA

**Requisição para `/api/client/ai/recommendations`:**
```json
{
  "clientId": 1,
  "establishmentId": 2,
  "preferences": {
    "preferredTimes": ["afternoon"],
    "budget": "medium",
    "serviceHistory": [...]
  }
}
```

**Resposta:**
```json
{
  "success": true,
  "data": [{
    "service": "Corte + Barba",
    "professional": "João Silva",
    "date": "2024-01-20T14:00:00",
    "time": "14:00",
    "price": 65.00,
    "confidence": 95,
    "reason": "..."
  }],
  "count": 1
}
```

### Agendamento Manual/Confirmação IA

**Requisição para `/api/client/appointments/book`:**
```json
{
  "clientId": 1,
  "professionalId": 3,
  "serviceId": 5,
  "establishmentId": 2,
  "appointmentDateTime": "2024-01-20T14:00:00",
  "notes": "Observações especiais"
}
```

**Resposta:**
```json
{
  "success": true,
  "message": "Agendamento criado com sucesso",
  "data": {
    "id": 123,
    "status": "CONFIRMED",
    ...
  }
}
```

---

## ✨ Conclusão

Agora você tem duas formas poderosas de agendar serviços no Slotfy:

1. **Use a IA** quando quiser rapidez e confiar em recomendações inteligentes
2. **Use o Manual** quando quiser controle total sobre suas escolhas

Ambos os métodos são totalmente funcionais e integrados com o backend!

---

**Versão do Guia**: 1.0.0  
**Última Atualização**: Janeiro 2025  
**Sistema**: Slotfy - Agendamento Inteligente
