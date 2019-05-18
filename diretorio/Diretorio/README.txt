build:
// --- Só serve para criar o jar
mvn package

run:
java -jar target/Hello-1.0-SNAPSHOT.jar server hello.yml

correr dentro do IntelliJ:

Correr a main do DiretorioApplication com os argumentos "server hello.yml"

