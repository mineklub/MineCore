# Paper Platform Module

This module contains the Paper plugin entry point for MineCore, boots the shared API, and exposes Bukkit events for the request lifecycle.

## Build

```powershell
cd C:\Users\mhoff\Documents\GitHub\MinePay
.\gradlew :platform-paper:build --no-daemon
```

## Startup

The Paper plugin starts the shared API directly.
Optional integrations are split into separate hook modules, such as `hooks:skript`, and are loaded from `plugins/MineCore/hooks/`.
Each hook jar must include `minecore-hook.properties` with its bootstrap class.

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
If you build a hook module, copy its jar into `plugins/MineCore/hooks/` on the server.
