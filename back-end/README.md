# Slotify Backend

Sistema de agendamento inteligente para barbearias e salões - Backend Spring Boot

## Tecnologias Utilizadas

- **Java 17** (LTS)
- **Spring Boot 3.2.1**
- **Spring Data JPA**
- **Hibernate 6.4.1**
- **H2 Database** (desenvolvimento)
- **MySQL** (produção - configurado mas não ativo)
- **Maven**

## Configuração do Ambiente

### Pré-requisitos
- Java 17 ou superior
- Maven 3.6+

### Como Executar

1. Compile o projeto:
```bash
mvn clean compile
```

2. Execute os testes:
```bash
mvn test
```

3. Execute a aplicação:
```bash
mvn spring-boot:run
```

A aplicação estará disponível em: `http://localhost:8080`

### Endpoints Disponíveis

- **Health Check**: `GET /api/health` - Verifica se a aplicação está funcionando
- **H2 Console**: `http://localhost:8080/h2-console` - Console do banco H2 (desenvolvimento)

### Configuração do Banco de Dados

#### Desenvolvimento (H2)
- URL: `jdbc:h2:mem:slotifydb`
- Usuário: `sa`
- Senha: `password`
- Console H2: `http://localhost:8080/h2-console`

#### Produção (MySQL)
Para usar MySQL em produção, descomente as linhas no `application.properties` e configure:
- URL: `jdbc:mysql://localhost:3306/slotify`
- Usuário e senha conforme sua configuração

## Estrutura do Projeto

```
src/
├── main/
│   ├── java/com/slotify/
│   │   ├── SlotifyApplication.java     # Classe principal
│   │   ├── config/                     # Configurações
│   │   ├── controller/                 # Controllers REST
│   │   ├── service/                    # Lógica de negócio
│   │   ├── repository/                 # Repositórios JPA
│   │   └── entity/                     # Entidades JPA
│   └── resources/
│       └── application.properties      # Configurações da aplicação
└── test/
    └── java/com/slotify/
        └── SlotifyApplicationTests.java # Testes
```

## Configurações JPA/Hibernate

- **DDL Auto**: `update` - Atualiza o schema automaticamente
- **Show SQL**: `true` - Mostra queries SQL no log
- **Format SQL**: `true` - Formata queries SQL no log
- **Dialect**: Configurado automaticamente (H2 ou MySQL)

## Logs

Os logs estão configurados para mostrar:
- Queries SQL executadas
- Parâmetros das queries (nível TRACE)
- Logs da aplicação em nível DEBUG