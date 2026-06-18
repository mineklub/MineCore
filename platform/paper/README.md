# Paper Platform Module

This module contains the Paper plugin entry point for MineCore, boots the shared API, and exposes Bukkit events for the request lifecycle.

## Build

```powershell
cd C:\Users\mhoff\Documents\GitHub\MinePay
.\gradlew :platform-paper:build --no-daemon
```

## Startup

The Paper plugin starts the shared API directly.

### Optional token

You may set `socket.token` in `platform/paper/src/main/resources/config.yml`.
If the `TOKEN` environment variable is present, it overrides the config value.

## Bukkit events

The Paper bridge forwards the API bus into Bukkit events:

- `MineCorePreCreateRequestEvent` — cancellable, contains `StoreRequest`
- `MineCorePostCreateRequestEvent` — contains `StoreCreatedRequest`
- `MineCoreReceiveRequestEvent` — contains `SocketRequestType` and `StoreCreatedRequest`

Listen for them with `@EventHandler` like any other Bukkit event.

## Output

The built plugin jar will be available under `platform/paper/build/libs/`.
