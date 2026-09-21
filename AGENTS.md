# Instruções para desenvolvimento

## Visão geral

API REST que recebe imagens de tabloides de supermercados e retorna ofertas estruturadas. A estratégia principal usa o Ollama com o modelo multimodal `qwen3-vl:4b-instruct`; o Tesseract é mantido como estratégia alternativa.

## Stack obrigatória

- Java 25;
- Spring Boot 4.1.1;
- Maven;
- Spring Web MVC e Spring Validation;
- `RestClient` para integrações HTTP;
- Ollama com `qwen3-vl:4b-instruct`;
- Tess4J/Tesseract para a alternativa de OCR;
- JUnit 5 e AssertJ para testes.
- Docker e Docker Compose para o ambiente reproduzível.

## Convenções

- Usar records para DTOs e objetos de configuração imutáveis;
- usar injeção de dependência por construtor;
- preservar o contrato `TabloideExtractor` para estratégias de extração;
- manter detalhes do Ollama isolados em `OllamaVisionClient`;
- não colocar regras de normalização ou deduplicação no controller;
- incluir testes unitários ao alterar sanitização, deduplicação ou normalização;
- não registrar imagens em Base64 nos logs;
- manter prompts e schemas compatíveis com respostas JSON estruturadas.
- manter `compose.yaml` funcional em CPU, sem exigir GPU.

## Verificação

Antes de concluir uma alteração, executar:

```powershell
mvn test
```
