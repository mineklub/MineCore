# Skript Hook Module

This module contains the Skript integration for MineCore.

## Build

```powershell
cd C:\Users\mhoff\Documents\GitHub\MinePay
.\gradlew :hooks:skript:build --no-daemon
```

## Runtime

Copy the built jar into `plugins/MineCore/hooks/`.
The Paper plugin loads the jar by reading `minecore-hook.properties` inside the jar.

