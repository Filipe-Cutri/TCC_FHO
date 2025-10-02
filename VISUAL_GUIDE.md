# 🎨 Visual Guide - Establishment Selection Feature

## Overview
This guide provides visual representations of the establishment selection feature implemented for the Slotfy platform.

## User Journey

### 1️⃣ Client Registration Flow

#### Step 1: Registration Form
```
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃  📝 Cadastro de Cliente              ┃
┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫
┃                                       ┃
┃  Nome Completo                        ┃
┃  ┌─────────────────────────────────┐ ┃
┃  │ João Silva                      │ ┃
┃  └─────────────────────────────────┘ ┃
┃                                       ┃
┃  E-mail                               ┃
┃  ┌─────────────────────────────────┐ ┃
┃  │ joao@email.com                  │ ┃
┃  └─────────────────────────────────┘ ┃
┃                                       ┃
┃  Telefone                             ┃
┃  ┌─────────────────────────────────┐ ┃
┃  │ (11) 99999-9999                 │ ┃
┃  └─────────────────────────────────┘ ┃
┃                                       ┃
┃  Senha                                ┃
┃  ┌─────────────────────────────────┐ ┃
┃  │ ••••••••                        │ ┃
┃  └─────────────────────────────────┘ ┃
┃                                       ┃
┃  🏪 Estabelecimento (opcional)        ┃
┃  ┌─────────────────────────────────┐ ┃
┃  │ ➕ Selecione um estabelecimento │ ┃
┃  │    (você pode fazer depois)     │ ┃
┃  └─────────────────────────────────┘ ┃
┃                                       ┃
┃  ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓ ┃
┃  ┃   ✨ Criar Minha Conta         ┃ ┃
┃  ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛ ┃
┃                                       ┃
┃  Já tem conta? Fazer Login           ┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
```

#### Step 2: Establishment Selection Modal
```
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃  🏪 Selecione um Estabelecimento                   [✕]   ┃
┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫
┃                                                            ┃
┃  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   ┃
┃  │              │  │              │  │              │   ┃
┃  │   📸 Foto    │  │   📸 Foto    │  │   📸 Foto    │   ┃
┃  │              │  │              │  │              │   ┃
┃  │  Barbearia   │  │ Salão Belle  │  │  Spa Relax   │   ┃
┃  │   Premium    │  │              │  │              │   ┃
┃  │              │  │              │  │              │   ┃
┃  │ 🏷️ Barbearia │  │ 🏷️ Beleza    │  │ 🏷️ Spa       │   ┃
┃  │ 📍 Centro    │  │ 📍 Zona Sul  │  │ 📍 Z. Oeste  │   ┃
┃  └──────────────┘  └──────────────┘  └──────────────┘   ┃
┃                                                            ┃
┃  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   ┃
┃  │              │  │              │  │              │   ┃
┃  │   📸 Foto    │  │   📸 Foto    │  │   📸 Foto    │   ┃
┃  │              │  │              │  │              │   ┃
┃  │ Pet Shop     │  │  Academia    │  │  Clínica     │   ┃
┃  │   Amigo      │  │   Fitness    │  │   Saúde      │   ┃
┃  │              │  │              │  │              │   ┃
┃  │ 🏷️ Pets      │  │ 🏷️ Fitness   │  │ 🏷️ Saúde     │   ┃
┃  │ 📍 Centro    │  │ 📍 Zona Leste│  │ 📍 Centro    │   ┃
┃  └──────────────┘  └──────────────┘  └──────────────┘   ┃
┃                                                            ┃
┃         [Cancelar]              [Confirmar]               ┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
```

#### Step 3: Selected Establishment Display
```
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃  🏪 Estabelecimento (opcional)        ┃
┃  ╔═══════════════════════════════════╗┃
┃  ║  📸    Barbearia Premium          ║┃
┃  ║        🏷️ Barbearia                ║┃
┃  ║        📍 Rua das Flores, 123     ║┃
┃  ║                                   ║┃
┃  ║              [🔄 Trocar]          ║┃
┃  ╚═══════════════════════════════════╝┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
```

### 2️⃣ Client Login Flow

#### Step 1: Login Form
```
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃  🔐 Login Cliente                     ┃
┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫
┃                                       ┃
┃  Acesse sua conta para agendar        ┃
┃  serviços                             ┃
┃                                       ┃
┃  E-mail                               ┃
┃  ┌─────────────────────────────────┐ ┃
┃  │ 📧 seu@email.com                │ ┃
┃  └─────────────────────────────────┘ ┃
┃                                       ┃
┃  Senha                                ┃
┃  ┌─────────────────────────────────┐ ┃
┃  │ 🔐 ••••••••                     │ ┃
┃  └─────────────────────────────────┘ ┃
┃                                       ┃
┃  ☑️ Lembrar-me   Esqueci minha senha  ┃
┃                                       ┃
┃  ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓ ┃
┃  ┃      🚀 Entrar                 ┃ ┃
┃  ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛ ┃
┃                                       ┃
┃  Ainda não tem conta? Criar Conta    ┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
```

#### Step 2: Post-Login Establishment Prompt (if not set)
```
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃  ℹ️ Seleção de Estabelecimento         ┃
┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫
┃                                       ┃
┃  Deseja selecionar um estabelecimento┃
┃  agora? Isso facilitará seus         ┃
┃  agendamentos futuros.                ┃
┃                                       ┃
┃     [Não agora]  [Sim, selecionar]   ┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
```

### 3️⃣ Dashboard View

#### Client Dashboard with Selected Establishment
```
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃  🏠 Dashboard - João Silva                      [👤] ┃
┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫
┃                                                       ┃
┃  ╔════════════════════════════════════════════════╗  ┃
┃  ║  📍 Seu Estabelecimento                        ║  ┃
┃  ║                                                 ║  ┃
┃  ║  📸  Barbearia Premium                         ║  ┃
┃  ║      🏷️ Barbearia                              ║  ┃
┃  ║      📍 Rua das Flores, 123 - Centro           ║  ┃
┃  ║      ⭐ 4.8/5.0 (234 avaliações)               ║  ┃
┃  ║                                                 ║  ┃
┃  ║      [📅 Agendar Serviço]  [🔄 Trocar]        ║  ┃
┃  ╚════════════════════════════════════════════════╝  ┃
┃                                                       ┃
┃  📅 Próximos Agendamentos                            ┃
┃  ┌─────────────────────────────────────────────┐    ┃
┃  │ 15/02 às 14:00 - Corte de Cabelo           │    ┃
┃  │ Com: Carlos Santos                          │    ┃
┃  │ 💰 R$ 35,00                                 │    ┃
┃  └─────────────────────────────────────────────┘    ┃
┃                                                       ┃
┃  📊 Histórico                                        ┃
┃  • 12 agendamentos realizados                        ┃
┃  • Última visita: 15/01/2024                         ┃
┃  • Gasto total: R$ 420,00                            ┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
```

## UI States

### Card States

#### 1. Normal State
```
┌──────────────┐
│              │
│   📸 Foto    │
│              │
│  Barbearia   │
│   Premium    │
│              │
│ 🏷️ Barbearia │
│ 📍 Centro    │
└──────────────┘
```

#### 2. Hover State
```
╔══════════════╗
║              ║  ← Sombra + Elevação
║   📸 Foto    ║
║              ║
║  Barbearia   ║
║   Premium    ║
║              ║
║ 🏷️ Barbearia ║
║ 📍 Centro    ║
╚══════════════╝
```

#### 3. Selected State
```
╔══════════════╗
║ ✓            ║  ← Background colorido
║   📸 Foto    ║     + Checkmark
║              ║
║  Barbearia   ║
║   Premium    ║
║              ║
║ 🏷️ Barbearia ║
║ 📍 Centro    ║
╚══════════════╝
```

## Responsive Design

### Desktop View (≥992px)
```
┌─────────────────────────────────────────────────────────┐
│  Modal                                                   │
│  ┌───────────┐ ┌───────────┐ ┌───────────┐            │
│  │  Card 1   │ │  Card 2   │ │  Card 3   │            │
│  └───────────┘ └───────────┘ └───────────┘            │
│  ┌───────────┐ ┌───────────┐ ┌───────────┐            │
│  │  Card 4   │ │  Card 5   │ │  Card 6   │            │
│  └───────────┘ └───────────┘ └───────────┘            │
└─────────────────────────────────────────────────────────┘
```

### Tablet View (768px - 991px)
```
┌─────────────────────────────────────┐
│  Modal                               │
│  ┌────────────┐ ┌────────────┐     │
│  │   Card 1   │ │   Card 2   │     │
│  └────────────┘ └────────────┘     │
│  ┌────────────┐ ┌────────────┐     │
│  │   Card 3   │ │   Card 4   │     │
│  └────────────┘ └────────────┘     │
└─────────────────────────────────────┘
```

### Mobile View (<768px)
```
┌───────────────────┐
│  Modal            │
│  ┌─────────────┐ │
│  │   Card 1    │ │
│  └─────────────┘ │
│  ┌─────────────┐ │
│  │   Card 2    │ │
│  └─────────────┘ │
│  ┌─────────────┐ │
│  │   Card 3    │ │
│  └─────────────┘ │
└───────────────────┘
```

## Color Scheme

### Primary Colors
- **Primary**: `#6366f1` (Indigo) - Main actions, selected states
- **Secondary**: `#10b981` (Green) - Success states, confirmations
- **Accent**: `#f59e0b` (Amber) - Highlights, notifications

### Neutral Colors
- **Background**: `#f9fafb` (Gray 50)
- **Surface**: `#ffffff` (White)
- **Border**: `#e5e7eb` (Gray 200)
- **Text Primary**: `#1a1a1a` (Gray 900)
- **Text Secondary**: `#6b7280` (Gray 500)

### State Colors
- **Hover Border**: `#6366f1` (Primary)
- **Selected Background**: `linear-gradient(135deg, #f8f9ff 0%, #f0f1ff 100%)`
- **Error**: `#ef4444` (Red 500)
- **Success**: `#10b981` (Green 500)

## Animations

### Card Hover
```css
.establishment-card:hover {
  transform: translateY(-2px);
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}
```

### Modal Enter
```css
.modal.fade {
  opacity: 0;
  transform: scale(0.95);
  transition: all 0.3s ease;
}

.modal.show {
  opacity: 1;
  transform: scale(1);
}
```

### Button Hover
```css
.test-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
  transition: all 0.3s ease;
}
```

## Accessibility Features

### Keyboard Navigation
- ✅ Tab through all interactive elements
- ✅ Enter to select establishment card
- ✅ Escape to close modal
- ✅ Arrow keys to navigate between cards

### Screen Reader Support
- ✅ ARIA labels on all buttons
- ✅ Alt text on images
- ✅ Semantic HTML structure
- ✅ Focus indicators
- ✅ Descriptive error messages

### Visual Accessibility
- ✅ High contrast ratios (WCAG AA compliant)
- ✅ Clear focus states
- ✅ Icons with text labels
- ✅ Consistent spacing
- ✅ Responsive font sizes

## Loading States

### Establishments Loading
```
┌─────────────────────────────────┐
│  🏪 Selecione um Estabelecimento │
│                                  │
│     ⏳ Carregando...             │
│                                  │
│  ┌──────────┐  ┌──────────┐    │
│  │ ░░░░░░░░ │  │ ░░░░░░░░ │    │
│  │ ░░░░░░░░ │  │ ░░░░░░░░ │    │
│  └──────────┘  └──────────┘    │
└─────────────────────────────────┘
```

### Registration Processing
```
┌─────────────────────────┐
│  ⏳ Criando conta...    │
│  Por favor, aguarde...  │
└─────────────────────────┘
```

## Error States

### API Error
```
┌─────────────────────────────────┐
│  ❌ Erro ao Carregar             │
│                                  │
│  Não foi possível carregar os    │
│  estabelecimentos. Por favor,    │
│  tente novamente.                │
│                                  │
│     [Tentar Novamente]           │
└─────────────────────────────────┘
```

### Validation Error
```
┌─────────────────────────────────┐
│  ⚠️ Campos Obrigatórios          │
│                                  │
│  Por favor, preencha todos os    │
│  campos obrigatórios.            │
└─────────────────────────────────┘
```

## Success States

### Registration Success
```
┌─────────────────────────────────┐
│  ✅ Conta Criada!                │
│                                  │
│  Bem-vindo, João Silva!          │
│                                  │
│  Redirecionando para o           │
│  dashboard...                    │
└─────────────────────────────────┘
```

### Establishment Updated
```
┌─────────────────────────────────┐
│  ✅ Estabelecimento Atualizado!  │
│                                  │
│  Você agora está vinculado à     │
│  Barbearia Premium               │
└─────────────────────────────────┘
```

---

**Esta interface foi projetada para ser intuitiva, acessível e visualmente atraente! 🎨✨**
