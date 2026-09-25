# Skript 2.15.x Hook Module

This module contains the Skript 2.15.x integration for MineCore, compiled against Skript 2.15.4.

## Build

Run `.\gradlew :hooks:skrip215:build --no-daemon` from the repository root.

## Runtime

MineCore's hook downloader selects this module for Skript 2.15.x.
For manual installation, copy the matching `skrip215-jvm17.jar`, `skrip215-jvm21.jar`, or `skrip215-jvm25.jar` from `build/libs/` into `plugins/MineCore/hooks/`.
The plugin loads the jar by reading `minecore-hook.properties` inside the jar.
