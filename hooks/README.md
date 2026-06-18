# Hooks Module

This module contains the shared hooks API used by MineCore hook submodules.
Individual hook implementations live in child modules such as `hooks/skript`.

## Build

```powershell
cd C:\Users\mhoff\Documents\GitHub\MinePay
.\gradlew :hooks:build --no-daemon
```

## Runtime

This module is a shared library and is bundled into the Paper plugin.



