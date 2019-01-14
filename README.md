# Chat application

## Description
In this console chat, all users are in the same space and see each other's messages. The users can also send commands to the server and get the results. 

All messages that start with **/** are interpreted as commands just like in Slack.
The **/help** command returns the commands' descriptions. 
A username is unique and set during authentication. It can be changed using the **/rename** command with a parameter.

## A chatting example
```
Welcome to the chat! Please, enter a name to start chatting.
Paul
Congratulations! You have successfully logged as Paul. Type /help to show the list of commands.
User Paul is ready to chatting.
Hello, is here anyone?
[2019-01-15 01:43:35] Paul: Hello, is here anyone?
[2019-01-15 01:43:51] Katie: Yes, i'm here. What do you want to do here??
I want to chat more.
[2019-01-15 01:44:28] Paul: I want to chat more.
[2019-01-15 01:44:54] Katie: But not me.
User Katie has left the chat.
/users
You are alone in the chat.
```
## Key features
- The chat contains client and server as two separated JAR files which can run independently on different machines.
- All clients and a server interacts through TCP sockets.
- The server is implemented in a non-blocking way. It has a single thread that accepts connections, receives and sends data, and several threads that process input messages and commands.
- Both the client and the server have extensible architecture. The transport layer can be replaced without changing the core business logic.
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
