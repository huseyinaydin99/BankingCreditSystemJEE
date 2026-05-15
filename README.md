# Enterprise Java Template

Kurumsal ölçekli, multi-module, monolith Java proje şablonu.

## Teknolojiler

- Spring Framework 6
- Spring MVC
- Hibernate ORM 6
- JPA
- JSF 4
- Servlet API 6
- Oracle JDBC
- Maven Multi Module
- Clean Architecture yaklaşımı

## Modüller

- shared-kernel
- user-domain
- user-application
- user-infrastructure
- user-presentation

## Mimari

Her bounded context aşağıdaki katmanlara ayrılır:

- Domain
- Application
- Infrastructure
- Presentation

## Oracle Yapısı

- HikariCP DataSource
- Hibernate JPA
- Production-ready persistence.xml
- Reusable configuration

## Build

```bash
mvn clean install
```