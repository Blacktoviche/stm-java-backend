# stm-java-backend

Backend for simple task manager using the power of spring boot, rest, jpa and spring security using jsonwebtoken and PostgresSQL as database

Download the frontend you prefer which designed for 
You should read [stm](https://github.com/blacktoviche/stm) before start using this backend


## Installation

```bash
# Clone this repository
git clone https://github.com/blacktoviche/stm-java-backend
# Go into the repository
cd stm-java-backend
# Install dependencies
mvn install
# Compile the app
mvn compile
# Package the app
mvn package
# Run the app
mvn spring-boot:run
``` 

## Note
If you want to edit this project use any prefered ide eclipse, netbeans ( I'm using IntelliJ IDEA Community Edition )
Use import project and select the pom.xml not the folder at least if you use IntelliJ

## Note
CORS ( Cross-Origin Resource Sharing ) is enabled in this backend so I could connect stm-web from another server
In production mode this should not be enabled and stm-web must be deployed within this backend

## Note
When deploy this backend it must has at leats one admin user. in data.sql there are two users
admin with password admin and user with password user
For the passwords I'm using BCrypt to encrypt passwords in databas. BCrypt is part of spring security


Thank for [Stephan Zerhusen](https://github.com/szerhusenBC) I used his [https://github.com/szerhusenBC/jwt-spring-security-demo](https://github.com/szerhusenBC/jwt-spring-security-demo) It's well done
I had to edit it to suite my needs for storing and retriving token from database



## License
- [MIT](LICENSE)

Twitter [@SyrianDeveloper](https://www.twitter.com/SyrianDeveloper)