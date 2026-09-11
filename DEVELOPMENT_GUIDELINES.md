# Development Guidelines and Contribution Standards: `PICC-PP-ENV-Replication-Engine`

This document defines the architectural standards, development workflows, coding conventions, and security requirements for contributors to **`PICC-PP-ENV-Replication-Engine`**.

---

## Table of Contents

1. [Architecture & Design Principles](#1-architecture--design-principles)
2. [Development Environment Setup](#2-development-environment-setup)
3. [Package Structure & Code Navigation](#3-package-structure--code-navigation)
4. [Coding Standards & Best Practices](#4-coding-standards--best-practices)
   - [Event-Driven JMS Messaging](#event-driven-jms-messaging)
   - [GitOps Manifest Generation via FreeMarker](#gitops-manifest-generation-via-freemarker)
   - [Spring Cloud OpenFeign Conventions](#spring-cloud-openfeign-conventions)
   - [Git Automation with Eclipse JGit](#git-automation-with-eclipse-jgit)
   - [Dynamic Ingress Registration with HAProxy](#dynamic-ingress-registration-with-haproxy)
   - [Exception Handling & Transactional Boundaries](#exception-handling--transactional-boundaries)
   - [Logging & Sensitive Data Masking](#logging--sensitive-data-masking)
5. [Security, Code Quality & Compliance Tooling](#5-security-code-quality--compliance-tooling)
   - [SAST: SpotBugs & FindSecBugs](#sast-spotbugs--findsecbugs)
   - [SCA: OWASP Dependency-Check](#sca-owasp-dependency-check)
   - [SBOM: CycloneDX Aggregate Generation](#sbom-cyclonedx-aggregate-generation)
   - [Checkstyle: Google Java Style](#checkstyle-google-java-style)
6. [Git Workflow & Branching Strategy](#6-git-workflow--branching-strategy)
   - [Branch Naming Conventions](#branch-naming-conventions)
   - [Conventional Commits](#conventional-commits)
7. [Pull Request (PR) Checklist](#7-pull-request-pr-checklist)
8. [Release Lifecycle & Versioning](#8-release-lifecycle--versioning)

---

## 1. Architecture & Design Principles

`PICC-PP-ENV-Replication-Engine` is the core replication and GitOps orchestration engine of the **Platform Portal (PP)** in the **Nubo Native Platform (NNP)**. It automates end-to-end environment provisioning by converting database configurations into declarative Kubernetes manifests, pushing to GitOps repositories, and coordinating with ArgoCD and HAProxy.

Key design principles:

1. **Event-Driven Asynchronous Lifecycle**: Provisioning requests are received via Apache ActiveMQ Artemis JMS queue (`env-rep::env_replication`). Operations execute asynchronously without blocking upstream portals.
2. **Declarative GitOps Pipelines**: All environment specifications are rendered into immutable YAML manifests and tracked in Git before being synchronized to Kubernetes by ArgoCD.
3. **Fail-Safe Working Directory Management**: Local clone directories are namespaced by request ID (`gitops/{reqId}`) and cleaned up in `finally` blocks to prevent disk exhaustion.
4. **Resilient Outbound Integrations**: Calls to GitLab, ArgoCD, HAProxy, and Redmine use defensive timeouts, safe fallbacks, and transactional boundaries.
5. **Zero-Trust Security**: No passwords, tokens, or infrastructure secrets are logged or hardcoded. Docker image pull secrets are computed dynamically and written only to transient YAML manifests.

---

## 2. Development Environment Setup

### Prerequisites

- **Java JDK**: Version `21` (Eclipse Temurin LTS recommended).
- **Maven**: Version `3.9+` (or use the bundled `./mvnw`).
- **PostgreSQL**: Version `15+` or `16+` (for storing environment metadata).
- **Apache ActiveMQ Artemis**: Version `2.30+`.
- **Git**: Installed and accessible in system path.

### Configuration

Copy `.env.example` to `.env` and set environment variables:

```bash
cp .env.example .env
```

To compile and verify locally:

```bash
./mvnw clean compile
./mvnw test
```

---

## 3. Package Structure & Code Navigation

```
src/main/java/com/nnp/envrep/
├── EnvrepEngineApplication.java       # Spring Boot main entrypoint
├── argoint/api/client/               # ArgoCD REST API OpenFeign client
│   └── ArgoIntegrationClient.java
├── config/                            # Spring Configuration Beans
│   ├── ArgoFeignConfig.java          # Feign client SSL & ErrorDecoder setup
│   ├── AsyncConfig.java              # Async task executor configuration
│   ├── EngineStartupComp.java        # Workspace directory initialization
│   ├── GitOpsProperties.java         # GitOps paths & repository properties
│   ├── JMSConfig.java                # ActiveMQ Artemis connection & templates
│   ├── OpenApiConfig.java            # Swagger / OpenAPI 3.0 configuration
│   ├── TemplateConfig.java           # FreeMarker template configuration
│   └── WebClientConfig.java          # Reactive WebClient configurations
├── controller/                        # REST Controllers
│   └── GitOpsController.java         # Management & manual trigger endpoints
├── event/                             # Application internal events
│   ├── GitOpsEvent.java              # Event payload
│   └── GitopsEventListener.java      # Asynchronous event listener
├── exception/                         # Domain & client exceptions
│   ├── ArgoApiExceptionMessage.java
│   ├── ArgoClientErrorDecoder.java
│   ├── ArgoException.java
│   ├── EnvReplEngineEx.java
│   ├── GitException.java
│   ├── HAProxyException.java
│   └── RedmineException.java
├── model/                             # Domain Entities & DTOs
│   ├── Environment.java              # Environment specification
│   ├── EnvRequest.java               # Replication request entity
│   ├── EnvReqComponent.java          # Component requirements
│   ├── EnvProxyConfig.java           # Ingress proxy routing configuration
│   ├── HostPlan.java                 # Compute/storage allocation plan
│   └── argo/, haproxy/, rm/          # Integration DTOs
├── repo/                              # Spring Data JPA Repositories
│   ├── EnvRepo.java
│   ├── EnvReqRepo.java
│   ├── EnvProxyConfigRepo.java
│   └── HostPlanRepo.java
├── service/                           # Business logic & orchestration
│   ├── EngineStartupService.java     # Primary replication lifecycle handler
│   ├── GitOpsService.java            # Manifest generation & ArgoCD sync
│   ├── GitService.java               # Docker secret encoding & PAT generation
│   ├── HAProxyService.java           # HAProxy service exposure
│   ├── RedmineService.java           # Support ticketing integration
│   └── SharedCompService.java        # Shared component initialization
└── util/                              # Utility classes
    ├── CustomFileUtil.java           # File manipulation & cleanup
    └── GitlabActionUtil.java         # JGit clone, commit, and push operations
```

---

## 4. Coding Standards & Best Practices

### Event-Driven JMS Messaging
- JMS listeners in `EngineStartupService` must process incoming requests with request ID correlation in `ThreadContext`.
- Ensure message parsing is defensive against missing or corrupted tokens in the pipe-delimited payload (`reqId|user|password|email`).

### GitOps Manifest Generation via FreeMarker
- Templates must use square bracket interpolation syntax `[=var]` to avoid conflicting with Kubernetes standard `${...}` expressions.
- Always clean up FreeMarker template loaders (`templateCfg.unsetTemplateLoader()`) after processing to avoid thread-safety leaks.

### Spring Cloud OpenFeign Conventions
- All Feign clients must specify fallback defaults in annotations:
  `@FeignClient(name = "${feign.name:argo-client}", url = "${feign.url:http://localhost:8080}${feign.url.api:/api/v1}")`
- Feign custom error decoders (`ArgoClientErrorDecoder`) must handle non-2xx statuses and map them to domain `ArgoException`.

### Git Automation with Eclipse JGit
- All JGit operations (`GitlabActionUtil.cloneFromGitOpsRepo`, `pushProject`) must use configured bot credentials via `UsernamePasswordCredentialsProvider`.
- Always delete transient clone directories in `finally` blocks using `CustomFileUtil.deleteFile(baseDir)`.

### Dynamic Ingress Registration with HAProxy
- Route names and domains must use the configurable `haproxy.base.domain`:
  `String domainName = String.format("%s-%s.%s", compName, envName, baseDomain);`
- Ingress definitions must be persisted to `EnvProxyConfig` with appropriate backend type (`K8S_DNS`).

### Exception Handling & Transactional Boundaries
- Service methods interacting with JPA must be annotated with `@Transactional`.
- Throw domain-specific exceptions (`EnvReplEngineEx`, `GitException`, `ArgoException`) with descriptive context.

### Logging & Sensitive Data Masking
- **Strictly prohibit** logging passwords, personal access tokens (PATs), or raw Docker config JSON.
- Use parameterized SLF4J logging (`log.info("Provisioned app {}", appName);`).

---

## 5. Security, Code Quality & Compliance Tooling

Contributors must verify changes against all automated quality gates before submitting PRs:

### SAST: SpotBugs & FindSecBugs
Scans bytecode for security vulnerabilities (injection, insecure randomness, resource leaks):
```bash
./mvnw spotbugs:check
```

### SCA: OWASP Dependency-Check
Scans dependencies for known CVEs. Enforces build failure if CVSS >= 7.0:
```bash
./mvnw dependency-check:check
```

### SBOM: CycloneDX Aggregate Generation
Generates immutable Software Bill of Materials in JSON format:
```bash
./mvnw cyclonedx:makeAggregateBom
```

### Checkstyle: Google Java Style
Ensures uniform formatting and code style compliance:
```bash
./mvnw checkstyle:check
```

---

## 6. Git Workflow & Branching Strategy

### Branch Naming Conventions
- `feat/<feature-name>`: New feature or capability.
- `fix/<bug-description>`: Defect fix.
- `docs/<doc-change>`: Documentation updates.
- `refactor/<scope>`: Code refactoring without behavior change.

### Conventional Commits
All commits must follow the [Conventional Commits specification](https://www.conventionalcommits.org/):
```
feat(gitops): add support for custom helm values in child applications
fix(jgit): handle authentication retry on transient gitlab timeouts
docs(deployment): update kubernetes resource quotas in deployment guide
```

---

## 7. Pull Request (PR) Checklist

Before submitting a PR, verify:
- [ ] Code compiles cleanly with `./mvnw clean compile`.
- [ ] Unit tests pass with `./mvnw test`.
- [ ] SpotBugs reports zero high-priority findings (`./mvnw spotbugs:check`).
- [ ] No hardcoded passwords, tokens, or internal IP addresses are present.
- [ ] All new configuration keys are documented in `.env.example`.
- [ ] Documentation and user manuals are updated if APIs or behaviors changed.

---

## 8. Release Lifecycle & Versioning

Releases follow **Semantic Versioning (`vMAJOR.MINOR.PATCH`)**:
- Automated GitHub Actions build and publish release artifacts upon tag creation (`v*.*.*`).
- Distribution artifacts are published to **GitHub Packages Maven Repository**.
