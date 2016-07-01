## Synopsis

## Getting started

> **Förutsättning:**

> Följande programvaror måste finnas installerade för att kunna bygga och köra applikationen.
> - Java 7 eller högre
> - Maven 3.*
> - Git

### Hämta ut källkoden
```
> git clone git@github.com:skltp-incubator/cooperation.git
```

### Bygg applikationen
```
> mvn clean package
```

### Starta Applikationen
Med  Maven:
```
> mvn spring-boot:run
```

### API-dokumentationen finns på
```
http://localhost:8080/coop/doc/index.html
```

Som standalone:
```
> java -jar target/cooperation-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```
### Anropa en tjänst
```
curl -X GET 'http://localhost:8080/coop/api/v1/cooperations?include=logicalAddress,connectionPoint'
```
### Utveckling
Om IDE är Eclipse generate eclipse .classpath och .project fil genom att köra från terminal under projekt:
```
mvn eclipse:eclipse
```
Sedan importera projekt Import-> Maven project-> ...
Obs: Ladda ner plugin Spring Tools från eclipse marketplace för att köra Spring Boot från IDE.
