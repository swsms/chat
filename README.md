# Chat application

## Description
In this console chat, all users are in the same space and can see each other's messages. The users can also send commands to the server and get the results. 

All messages that start with **/** are interpreted as commands just like in Slack.
The **/help** command returns the commands' descriptions. 
A username is unique and set during authentication. It can be changed using the **/rename** command with a parameter.

## Key features
- The chat contains client and server as two separated JAR files which can run independently on different machines.
- All clients and a server interacts through TCP sockets.
- The server is implemented in a non-blocking way. It has a single thread that accepts connections, receives and sends data, and several threads that process input messages and commands.
- Both the client and the server have extensible architecture, and the transport layer can be replaced without changing the core business logic.
- All data is stored in RAM.
- The project is built by Maven. Java 8+ is required.

## Building and running

- **Build artifacts** (server, client and other modules).
```
mvn clean package
```
- **Running server** (it will use the port 8999 on the localhost)
```
java -jar <path-to-project>/server/target/chat-server-*.jar
```
- **Running client** (it will try to connect to the port 8999 on the localhost).
```
java -jar <path-to-project>/client/target/chat-client-*.jar
```
It is also possible to specify host or/and port for both clients and servers using **-host** and **-port**:
```
java -jar <path-to-project>/server/target/chat-server-*.jar -host 0.0.0.0 -port 20001
java -jar <path-to-project>/client/target/chat-client-*.jar -host 0.0.0.0 -port 20001
```
