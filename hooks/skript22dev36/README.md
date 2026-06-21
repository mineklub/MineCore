# Skript 2.2-dev36 Hook Module

This module contains the legacy Skript 2.2-dev36 integration for MineCore.

> **Note:** Skript 2.2-dev36 does not have the newer `Section` API, but this
> hook provides a compatibility layer so `create minecore request for %player%:`
> can still own an indented code block directly.

## Example

```skript
create a minecore request for player:
    add product "my-product" named "My Product" price 10
```

## Build

```powershell
cd C:\Users\mhoff\Documents\GitHub\MinePay
.\gradlew :hooks:skript22dev36:build --no-daemon
```

## Runtime

Copy the built jar into `plugins/MineCore/hooks/`.
The Paper plugin loads the jar by reading `minecore-hook.properties` inside the jar.
