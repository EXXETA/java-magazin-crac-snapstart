# Java über den Wolken: Serverlose Enterprise-Anwendungen mit JDK CRaC und AWS SnapStart
Repository zum Java Magazin-Artikel von [Johannes Link](https://de.linkedin.com/in/link-johannes), [Yahya El Hadj Ahmed](https://www.linkedin.com/in/yahya-el-hadj-ahmed-b329892a4/) und [Jan Hauer](https://de.linkedin.com/in/jan-hauer) im August 2024.

## Ziel dieses Repos
Dieses Repository zeigt, wie eine Enterprise Spring Boot 3-Anwendung mit JDK CRaC, AWS SnapStart bzw. GraalVM Native Image und dem [serverless-java-container](https://github.com/aws/serverless-java-container) von AWS bereitgestellt werden können.

## Voraussetzungen
1. Anlegen eines technischen Users in AWS. Dessen access-key-id und secret-access-key müssen als Secrets der GitHub-Action unter den Bezeichnungen AWS_ACCESS_KEY_ID und AWS_SECRET_ACCESS_KEY übergeben werden.
2. Anlegen einer Postgres-Datenbank in AWS. Über das Deployment der Infrastruktur mit AWS SAM wird automatisch ein SecretsManager angelegt. Darin müssen die Zugangsdaten über die Secrets SPRING_DATASOURCE_PASSWORD, SPRING_DATASOURCE_URL und SPRING_DATASOURCE_USERNAME angegeben werden. AWS SAM kopiert diese Werte automatisch zu den Lambda-Funktionen.

## Erläuterung der relevanten Code-Stellen
An den Ordnern `src/main/java` und `src/test/java` ist zu erkennen, dass eine "gewöhnliche" Spring Boot-Anwendung mit Spring WebMvc und Spring Data zum Einsatz kommt. Diese wird unverändert auf AWS Lambda deployt.

### Einbindung des serverless-java-containers
Der serverless-java-container wird als Dependency in Maven eingebunden. Dieser wird benötigt, um AWS Events in HTTP-Anfragen zu transformieren und gemäß der Servlet-Spezifikation an die Spring Boot Controller zu übergeben.
```xml
<dependency>
    <groupId>com.amazonaws.serverless</groupId>
    <artifactId>aws-serverless-java-container-springboot3</artifactId>
    <version>2.0.3</version>
</dependency>
```

Beim Einsatz auf AWS Lambda muss die Umgebungsvariable MAIN_CLASS auf die Spring Boot Hauptklasse gesetzt werden.
```text
MAIN_CLASS=com.exxeta.serverless.ServerlessApplication
```

### CRaC / AWS SnapStart
Die Kompilierung für AWS SnapStart erfolgt mit dem Befehl.
```shell
mvn -Pcrac package
```

Hierbei wird die crac-dependency via Maven eingebunden.
```xml
<dependency>
    <groupId>org.crac</groupId>
    <artifactId>crac</artifactId>
</dependency>
```

Außerdem wird die JAR-Datei über das Shade-Plugin kompiliert.
```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>3.6.0</version>
            <configuration>
                <createDependencyReducedPom>false</createDependencyReducedPom>
            </configuration>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                    <configuration>
                        <finalName>${artifactId}</finalName>
                        <artifactSet>
                            <excludes>
                                <exclude>org.apache.tomcat.embed:*</exclude>
                            </excludes>
                        </artifactSet>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

Beim Deployment ist zu beachten, SnapStart auf AWS Lambda in AWS SAM zu aktivieren.
```yml
AutoPublishAlias: SnapStart
SnapStart:
  ApplyOn: PublishedVersions
```

Außerdem müssen (neben der MAIN_CLASS) folgende Umgebungsvariablen gesetzt werden.
```text
SPRING_DATASOURCE_HIKARI_ALLOW_POOL_SUSPENSION: true
contextInitTimeout: 240000
```

### GraalVM Native Image
Die Kompilierung des Native Image erfolgt mit folgendem Befehl.
```shell
mvn -Pnative native:compile
```

Die entstehende ./serverless-Datei muss zusammen mit der Bootstrap-Datei im Ordner `src/main/resources` bereitgestellt werden. AWS Lambda benötigt diese Datei zum starten der nativen Applikation.
```shell
#!/bin/sh
set -euo pipefail
./serverless
```

### Wichtiger Hinweis
Die Spring Boot-Anwendung muss in AWS Lambda mit einem apigateway-aws-proxy Event getestet werden. Andere Arten von Events führen zu einem Laufzeitfehler im serverless-java-container.