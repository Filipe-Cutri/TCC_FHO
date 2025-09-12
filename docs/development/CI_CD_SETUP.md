# Configuração de CI/CD - Sistema Slotfy

## Visão Geral

Este documento descreve a configuração do sistema de Integração Contínua (CI) e Entrega Contínua (CD) implementado para o projeto TCC_FHO - Sistema Slotfy.

## GitHub Actions Workflow

### Arquivo de Configuração
- **Localização**: `.github/workflows/ci.yml`
- **Triggers**: Push e Pull Requests para branches `main` e `develop`

### Funcionalidades Implementadas

#### 1. Configuração do Ambiente
- ✅ Java 17 (Temurin Distribution)
- ✅ Gradle com cache otimizado
- ✅ Permissões adequadas de segurança

#### 2. Execução de Testes
- ✅ Execução de todos os testes unitários
- ✅ Geração de relatórios de teste
- ✅ Continuação da execução mesmo em caso de falhas

#### 3. Cobertura de Testes
- ✅ **JaCoCo** configurado para geração de relatórios
- ✅ Geração de relatórios em XML e HTML
- ✅ Exclusão de classes utilitárias (DTOs, configs)
- ✅ Verificação de cobertura mínima (5% baseline)

#### 4. Integração com Codecov
- ✅ Upload automático de relatórios de cobertura
- ✅ Configuração para falha não-bloqueante
- ✅ Flags e nomeação adequada

#### 5. Artefatos
- ✅ Upload de relatórios de teste
- ✅ Upload de relatórios de cobertura
- ✅ Disponibilização para download

## Badges no README

### Badges Configurados
1. **CI Status**: Mostra o status da última execução do workflow
   ```markdown
   [![CI](https://github.com/Filipe-Cutri/TCC_FHO/actions/workflows/ci.yml/badge.svg)](https://github.com/Filipe-Cutri/TCC_FHO/actions/workflows/ci.yml)
   ```

2. **Coverage**: Mostra a porcentagem de cobertura de testes
   ```markdown
   [![codecov](https://codecov.io/gh/Filipe-Cutri/TCC_FHO/branch/main/graph/badge.svg)](https://codecov.io/gh/Filipe-Cutri/TCC_FHO)
   ```

## Configuração JaCoCo

### Arquivo build.gradle
```gradle
plugins {
    id 'jacoco'
}

tasks.named('test') {
    useJUnitPlatform()
    finalizedBy jacocoTestReport
    ignoreFailures = true
}

jacocoTestReport {
    dependsOn test
    reports {
        xml.required = true
        html.required = true
        csv.required = false
    }
    
    afterEvaluate {
        classDirectories.setFrom(files(classDirectories.files.collect {
            fileTree(dir: it, exclude: [
                '**/dto/**',
                '**/config/**',
                '**/SlotfyApplication.class'
            ])
        }))
    }
}

jacocoTestCoverageVerification {
    dependsOn jacocoTestReport
    violationRules {
        rule {
            limit {
                minimum = 0.05 // 5% baseline coverage
            }
        }
    }
}
```

## Como Funciona

### 1. Trigger Automático
- Qualquer push para `main` ou `develop` dispara o workflow
- Pull Requests para essas branches também executam o CI

### 2. Execução do Pipeline
1. **Checkout**: Baixa o código do repositório
2. **Setup Java**: Configura Java 17 com Temurin
3. **Setup Gradle**: Configura cache e ferramentas Gradle
4. **Permissões**: Garante que gradlew seja executável
5. **Testes**: Executa testes e gera relatórios de cobertura
6. **Upload Codecov**: Envia relatórios para o Codecov
7. **Artefatos**: Salva relatórios como artefatos do GitHub

### 3. Relatórios Gerados
- **XML**: Para integração com Codecov e outras ferramentas
- **HTML**: Para visualização local em `build/reports/jacoco/test/html/`
- **Test Results**: Relatórios detalhados de execução dos testes

## Melhorias Implementadas

### Segurança
- ✅ Permissões explícitas definidas
- ✅ Uso do Gradle Wrapper em vez do Gradle global
- ✅ Cache seguro com gradle/actions/setup-gradle

### Performance
- ✅ Cache inteligente do Gradle
- ✅ Uso de `--no-daemon` para builds CI
- ✅ Limpeza automática do cache

### Confiabilidade
- ✅ Continuação em caso de falhas de teste individuais
- ✅ Upload de artefatos mesmo em caso de falha
- ✅ Verificação de cobertura não-bloqueante

## Comandos Úteis

### Executar Localmente
```bash
# Executar testes com cobertura
cd back-end
./gradlew test jacocoTestReport

# Verificar cobertura
./gradlew jacocoTestCoverageVerification

# Ver relatório HTML
open build/reports/jacoco/test/html/index.html
```

### Configurações Avançadas

#### Aumentar Cobertura Mínima
```gradle
jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = 0.80 // 80% coverage
            }
        }
    }
}
```

#### Exclusões Específicas
```gradle
afterEvaluate {
    classDirectories.setFrom(files(classDirectories.files.collect {
        fileTree(dir: it, exclude: [
            '**/dto/**',
            '**/config/**',
            '**/exception/**',
            '**/*Application.class'
        ])
    }))
}
```

## Troubleshooting

### Workflow Não Aparece
- Verificar syntax do YAML
- Confirmar que está na branch correta
- Verificar permissões do repositório

### Codecov Não Funciona
- Verificar se `CODECOV_TOKEN` está configurado em Secrets
- Confirmar caminho do arquivo XML
- Verificar se o projeto está registrado no Codecov

### Falhas de Cobertura
- Revisar threshold configurado
- Verificar exclusões
- Analisar relatório HTML para detalhes

## Status Atual

- ✅ **CI/CD**: Configurado e funcional
- ✅ **Badges**: Configurados no README
- ✅ **JaCoCo**: Configurado com exclusões apropriadas
- ✅ **Codecov**: Integrado para relatórios online
- ✅ **Documentação**: Completa e atualizada

---

**Última atualização**: Janeiro 2025  
**Responsável**: GitHub Copilot  
**Versão**: 1.0.0