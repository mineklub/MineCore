# Skript 2.16.x Hook Module

This module contains the Skript 2.16.x integration for MineCore, compiled against Skript 2.16.2.

## Build

Run `.\gradlew :hooks:skript216:build --no-daemon` from the repository root.

## Runtime

MineCore's hook downloader selects this module for Skript 2.16.x.
For manual installation, copy the matching `skript216-jvm17.jar`, `skript216-jvm21.jar`, or `skript216-jvm25.jar` from `build/libs/` into `plugins/MineCore/hooks/`.
The plugin loads the jar by reading `minecore-hook.properties` inside the jar.
