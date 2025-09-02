# JavaScript Organization - Slotfy Project

Este documento descreve a nova organização dos arquivos JavaScript do projeto Slotfy, separando o código inline em módulos reutilizáveis e bem organizados.

## 📁 Estrutura dos Arquivos JavaScript

```
front-end/src/assets/js/
├── api-config.js                    # Configuração da API e cliente HTTP
├── client-session.js                # Gerenciamento de sessão do cliente  
├── establishment-session.js         # Gerenciamento de sessão do estabelecimento
├── ui-utils.js                      # Utilitários de interface (toasts, animações, loading)
├── form-utils.js                    # Utilitários de formulários (validação, progresso)
├── client-utils.js                  # Funcionalidades específicas do cliente
├── common-utils.js                  # Funcionalidades comuns do site principal
├── client-profile.js               # Página de perfil do cliente
├── client-services.js              # Página de serviços do cliente
└── client-preferences-setup.js     # Página de configuração de preferências
```

## 🔧 Módulos Principais

### 1. UI Utilities (`ui-utils.js`)
Funcionalidades comuns de interface:

**Classes:**
- `ToastManager` - Gerenciamento de notificações toast
- `LoadingManager` - Estados de carregamento para botões
- `AnimationManager` - Animações e efeitos visuais
- `ScrollManager` - Scroll suave e navegação

**Exemplo de uso:**
```javascript
// Mostrar notificação de sucesso
ToastManager.showSuccess('Operação realizada com sucesso!');

// Adicionar estado de loading a um botão
const resetButton = LoadingManager.setButtonLoading(button, 'Salvando...');

// Configurar animação de hover em cards
AnimationManager.addCardHoverEffect(card);
```

### 2. Form Utilities (`form-utils.js`)
Utilitários para formulários:

**Classes:**
- `FormValidator` - Validação de campos
- `ProgressTracker` - Acompanhamento de progresso
- `ModalManager` - Gerenciamento de modais
- `FormDataCollector` - Coleta de dados de formulários
- `FieldFilter` - Filtros condicionais de campos

**Exemplo de uso:**
```javascript
// Validação em tempo real
FormValidator.addRealTimeValidation(emailField, FormValidator.isValidEmail, 'Email inválido');

// Tracking de progresso
const tracker = new ProgressTracker('#form', '#progressBar', '#progressText');

// Configurar modal com formulário
ModalManager.setupFormModal('#modal', '#form', async (data) => {
    // Processar dados do formulário
});
```

### 3. Client Utilities (`client-utils.js`)
Funcionalidades específicas do cliente:

**Classes:**
- `ClientPreferencesManager` - Gerenciamento de preferências
- `ClientAIManager` - Funcionalidades de IA
- `ClientServiceManager` - Agendamento de serviços
- `ClientProfileManager` - Gerenciamento de perfil
- `ClientCommonManager` - Funcionalidades comuns

**Exemplo de uso:**
```javascript
// Salvar preferências
ClientPreferencesManager.savePreferences(preferences);

// Gerar recomendação de IA
const recommendation = ClientAIManager.generateAIRecommendation('Corte Masculino');

// Inicializar página cliente
ClientCommonManager.initClientPage();
```

### 4. Common Utilities (`common-utils.js`)
Funcionalidades do site principal:

**Classes:**
- `MainSiteManager` - Gerenciamento do site principal
- `NavigationManager` - Navegação entre páginas
- `CommonFormManager` - Formulários comuns (login, registro)
- `ResponsiveManager` - Funcionalidades responsivas

**Exemplo de uso:**
```javascript
// Navegar para login do cliente
NavigationManager.goToClientLogin();

// Configurar formulário de login
CommonFormManager.setupLoginForm('#loginForm', 'client');
```

## 🎯 Páginas Específicas

### client-profile.js
- Gerenciamento de perfil do cliente
- Formulários de preferências
- Efeitos de input

### client-services.js
- Página de serviços
- Botão de agendamento por IA
- Interações de booking

### client-preferences-setup.js
- Configuração inicial de preferências
- Filtros de serviços por tipo de estabelecimento
- Tracking de progresso do setup

## 📖 Como Usar

### 1. Incluir os Módulos Base
Em todas as páginas cliente, incluir:
```html
<script src="../../assets/js/ui-utils.js"></script>
<script src="../../assets/js/form-utils.js"></script>
<script src="../../assets/js/client-utils.js"></script>
```

### 2. Incluir Módulos Específicos
Para páginas específicas, adicionar o módulo correspondente:
```html
<script src="../../assets/js/client-profile.js"></script>
```

### 3. Inicialização Automática
A maioria dos módulos se inicializa automaticamente quando carregados. Para funcionalidades específicas:
```javascript
// Inicializar funcionalidades comuns do cliente
ClientCommonManager.initClientPage();
```

## 🔄 Migração do Código Inline

### Antes (Código Inline):
```html
<script>
document.addEventListener('DOMContentLoaded', function() {
    // Código JavaScript inline específico da página
    const button = document.querySelector('.btn');
    button.addEventListener('click', function() {
        // Lógica específica
    });
});
</script>
```

### Depois (Módulos Externos):
```html
<script src="../../assets/js/ui-utils.js"></script>
<script src="../../assets/js/client-utils.js"></script>
<script src="../../assets/js/client-profile.js"></script>
```

## ✅ Benefícios

1. **Reutilização de Código**: Funcionalidades comuns podem ser reutilizadas em múltiplas páginas
2. **Manutenibilidade**: Código organizado em módulos específicos é mais fácil de manter
3. **Performance**: Cache de arquivos JavaScript externos melhora performance
4. **Separação de Responsabilidades**: Cada módulo tem uma responsabilidade específica
5. **Escalabilidade**: Fácil adicionar novas funcionalidades sem duplicar código

## 🧪 Testando

Para testar se a migração foi bem-sucedida:

1. Abrir qualquer página cliente
2. Verificar se as funcionalidades (toasts, animações, formulários) funcionam corretamente
3. Verificar no console do navegador se não há erros JavaScript
4. Testar interações específicas como salvar preferências, agendamentos, etc.

## 🔧 Desenvolvimento

Para adicionar novas funcionalidades:

1. Identificar qual módulo é mais apropriado
2. Se necessário, criar um novo módulo específico
3. Adicionar a funcionalidade seguindo os padrões estabelecidos
4. Atualizar a documentação

### Exemplo de Nova Funcionalidade:
```javascript
// Em client-utils.js
class ClientNotificationManager {
    static showBookingReminder(appointment) {
        ToastManager.showInfo(`Lembrete: ${appointment.service} amanhã às ${appointment.time}`);
    }
}

// Tornar disponível globalmente
window.ClientNotificationManager = ClientNotificationManager;
```

## 📝 Convenções

1. **Nomeação**: Classes em PascalCase, métodos em camelCase
2. **Exports**: Sempre exportar classes para `window` para compatibilidade
3. **Inicialização**: Usar `document.addEventListener('DOMContentLoaded', ...)` quando necessário
4. **Erro Handling**: Sempre implementar tratamento de erros apropriado
5. **Documentação**: Comentar funções e classes complexas