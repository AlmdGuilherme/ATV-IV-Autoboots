# AutoManager API

## 🚀 Tecnologias Utilizadas

* **Java 17**
* **Spring Boot 2.6.7**
* **Spring Security** (Autenticação e Autorização)
* **JWT (JSON Web Token)** (Auth0)
* **Spring Data JPA** (Persistência de dados)
* **MySQL** (Banco de dados)
* **Lombok**
* **Maven**

---

## 📋 Pré-requisitos

Para rodar este projeto, você precisará ter instalado em sua máquina:

1.  **JDK 17**
2.  **Maven**
3.  **MySQL Server**

---

## ⚙️ Configuração do Banco de Dados

Antes de rodar, verifique o arquivo `src/main/resources/application.properties`. Certifique-se de que as credenciais do banco estão corretas para o seu ambiente.

```properties
# Conexão com o Banco
spring.datasource.url=jdbc:mysql://localhost:3306/automanager
spring.datasource.username=root
spring.datasource.password=SUA_SENHA_AQUI

# Comportamento do Hibernate
# Use 'create' na primeira execução para gerar as tabelas e usuários padrão.
# Depois, mude para 'update' para não perder os dados.
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Segredo do Token JWT
api.security.token.secret=${JWT_SECRET:12345678-padrao-desenvolvimento}
