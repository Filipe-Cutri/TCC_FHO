# TCC_FHO - Slotify

Sistema de agendamento inteligente para barbearias e salões.

## Estrutura do Projeto

### Estrutura Melhorada (src/)
```
src/
├── index.html                          # Página principal
├── assets/
│   ├── styles/
│   │   ├── common.css                  # Estilos comuns
│   │   └── welcome.css                 # Estilos da homepage
│   ├── scripts/                        # JavaScript files (futuro)
│   ├── favicon.png                     # Ícone do site
│   └── logo-slotify.png               # Logo do projeto
├── pages/
│   ├── client/                         # Páginas do cliente
│   │   ├── client-login.html          # Login do cliente
│   │   ├── client-register.html       # Cadastro do cliente
│   │   ├── client-dashboard.html      # Dashboard do cliente
│   │   ├── client-bookings.html       # Agendamentos do cliente
│   │   ├── client-services.html       # Serviços disponíveis
│   │   ├── client-profile.html        # Perfil do cliente
│   │   ├── client-notifications.html  # Notificações do cliente
│   │   ├── client-payments.html       # Pagamentos do cliente
│   │   └── client-professionals.html  # Profissionais disponíveis
│   └── establishment/                  # Páginas do estabelecimento
│       ├── establishment-login.html           # Login do estabelecimento
│       ├── establishment-register.html        # Cadastro do estabelecimento
│       ├── establishment-dashboard.html       # Dashboard do estabelecimento
│       ├── establishment-appointments.html    # Agendamentos do estabelecimento
│       ├── establishment-services.html        # Serviços do estabelecimento
│       ├── establishment-professionals.html   # Profissionais do estabelecimento
│       ├── establishment-payments.html        # Pagamentos do estabelecimento
│       ├── establishment-notifications.html   # Notificações do estabelecimento
│       ├── establishment-admin.html           # Administração do estabelecimento
│       └── establishment-reports.html         # Relatórios do estabelecimento
└── components/                         # Componentes reutilizáveis (futuro)
```

## Melhorias Implementadas

### 1. Estilização Melhorada
- ✅ Cores vibrantes com gradientes modernos
- ✅ Header/navbar com gradiente roxo-azul
- ✅ Footer com gradiente atrativo (roxo para azul)
- ✅ Botões com efeitos de hover e animações
- ✅ Cards com sombras e efeitos modernos
- ✅ Título com gradiente colorido

### 2. Estrutura de Pastas Aprimorada
- ✅ Organização lógica em `src/`
- ✅ Separação clara entre assets, páginas e componentes
- ✅ Estrutura preparada para integração com banco de dados
- ✅ Agrupamento por tipo de usuário (client/establishment)

### 3. Nomes de Arquivos Melhorados
- ✅ Nomenclatura descritiva e consistente
- ✅ Prefixos para identificar tipo de usuário
- ✅ Nomes em inglês para facilitar integração
- ✅ Estrutura preparada para APIs RESTful

## Convenções de Nomenclatura

### Páginas do Cliente
- `client-login.html` - Login do cliente
- `client-dashboard.html` - Dashboard do cliente
- `client-bookings.html` - Agendamentos do cliente

### Páginas do Estabelecimento
- `establishment-login.html` - Login do estabelecimento
- `establishment-dashboard.html` - Dashboard do estabelecimento
- `establishment-appointments.html` - Agendamentos do estabelecimento

### Assets
- `common.css` - Estilos globais
- `welcome.css` - Estilos específicos da homepage

## Próximos Passos para Integração com Banco

1. **API Endpoints** - Estrutura preparada para:
   - `/api/client/login`
   - `/api/client/dashboard`
   - `/api/establishment/login`
   - `/api/establishment/appointments`

2. **JavaScript Modules** - Estrutura para:
   - `src/assets/scripts/api-client.js`
   - `src/assets/scripts/auth.js`
   - `src/assets/scripts/utils.js`

3. **Componentes** - Estrutura para:
   - `src/components/header.html`
   - `src/components/footer.html`
   - `src/components/booking-form.html`