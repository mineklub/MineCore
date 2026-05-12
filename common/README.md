# Common Module

Shared Java components for MineCore modules.

## Socket.IO helper

`SocketIoClientManager` is available at:

- `dk.mineclub.minecore.common.socket.SocketIoClientManager`

### Quick usage

```java
SocketIoClientManager client = new SocketIoClientManager("https://api.mineclub.dk", token);
client.registerDefaultLogs();
client.connect();
client.on("minecore:event", args -> {
    System.out.println("Received: " + java.util.Arrays.toString(args));
});
```
