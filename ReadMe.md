## Segurança e Confiabilidade

Madalena Tomás 53464
Francisco Cardoso 57547
Martim Paraíba 56273

Como compilar
Servidor: javac src\server\TintoImarketServer.java
Cliente: javac src\client\TintoImarketClient.java

Como gerar .jar

Servidor: jar cfm Servidor.jar manifestServer *.class
Cliente: jar cfm Cliente.jar manifestClient *.class

Testar
Após o código compilado

O jar foi criado através do eclipse

Pelo Jar:

Servidor: java -jar Server.jar <port>

Cliente: java -jar Client.jar <serverAddress> <userID> [password]

Limitações do trabalho:
O End to End nas mensagens nao devolve corretamente a mensagem ao ler()
