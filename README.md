
# 💈 BarberApp

**BarberApp** é uma aplicação móvel desenvolvida com **Flutter** e **Firebase**, com foco em **agendamento inteligente de serviços** para **barbearias e salões de beleza independentes**. O objetivo é melhorar a gestão de horários, reduzir ociosidade e oferecer uma experiência mais prática e personalizada para profissionais e clientes.

---

## 📱 Funcionalidades

### Para Clientes:
- 📆 Agendamento de serviços de forma rápida e intuitiva  
- 🔔 Notificações inteligentes com sugestões de horários baseadas no histórico  
- 🧠 Recomendação automática de serviços com base em preferências e uso anterior  
- 💇‍♂️ Visualização do perfil dos profissionais e serviços oferecidos  

### Para Profissionais:
- 🗓️ Controle de agenda em tempo real  
- 📊 Painel de desempenho com histórico de agendamentos  
- 🚀 Otimização de horários vagos com base em períodos de baixa demanda  
- 🧠 Sistema de CRM inteligente com sugestões automatizadas de agendamento  

---

## 🧠 Inteligência Artificial

O sistema utiliza **Machine Learning** (inicialmente com o algoritmo **Random Forest**) para:

- Prever os melhores horários de agendamento com base em dados históricos  
- Reduzir falhas na agenda (horários ociosos)  
- Aumentar a fidelização de clientes com recomendações personalizadas  

---

## 🔧 Tecnologias Utilizadas

- **Flutter**: Framework para desenvolvimento multiplataforma (Android/iOS)  
- **Firebase**:
  - Authentication (login com email/senha)
  - Firestore (banco de dados em tempo real)
  - Cloud Messaging (notificações push)
- **Spring (futuro backend)** para integração com recursos avançados e automações  
- **Python (ML API)** para os algoritmos de recomendação e previsão (em versões futuras)  

---

## 📦 Estrutura do Projeto

```bash
barberapp/
├── android/
├── ios/
├── lib/
│   ├── models/
│   ├── screens/
│   ├── services/
│   ├── utils/
│   └── main.dart
├── assets/
├── pubspec.yaml
└── README.md
```

---

## 🚀 Como Rodar o Projeto

1. **Clone o repositório:**

   ```bash
   git clone https://github.com/seu-usuario/barberapp.git
   cd barberapp
   ```

2. **Instale as dependências:**

   ```bash
   flutter pub get
   ```

3. **Adicione o arquivo `google-services.json`** (do Firebase) na pasta:

   ```
   android/app
   ```

4. **Rode o app:**

   ```bash
   flutter run
   ```
---

## 👨‍💻 Autor

**Filipe Alberto Cutri**   
Este projeto foi desenvolvido como parte do **Trabalho de Conclusão de Curso (TCC)** na  
**Faculdade Fundação Hermínio Ometto (FHO)** – Curso de **Bacharelado em Sistemas de Informação**.

