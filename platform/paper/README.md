# Paper Platform Module

This module contains the Paper plugin entry point for MineCore and a receive-only Socket.IO listener.

## Build

```powershell
cd C:\Users\mhoff\Documents\GitHub\MinePay
.\gradlew :platform-paper:build --no-daemon
```

## Socket configuration

Configure `platform/paper/src/main/resources/config.yml` defaults or override via environment variables.

### `config.yml`

```yaml
socket:
  enabled: true
  url: ""
  token: ""
  event: "store_request"
```

### Environment overrides

- `MINECORE_SOCKET_URL`
- `MINECORE_SOCKET_TOKEN`
- `MINECORE_SOCKET_EVENT`

If `socket.url` is empty and `MINECORE_SOCKET_URL` is not set, the plugin skips socket startup.

## Output

The built plugin jar will be available under `platform/paper/build/libs/`.
