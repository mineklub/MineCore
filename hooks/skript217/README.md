# Skript 2.17.x Hook Module

This module contains the Skript 2.17.x integration for MineCore, compiled against Skript 2.17.1.

## Build

Run `.\gradlew :hooks:skript217:build --no-daemon` from the repository root.

## Runtime

MineCore's hook downloader selects this module for Skript 2.17.x.
For manual installation, copy the matching `skript217-jvm17.jar`, `skript217-jvm21.jar`, or `skript217-jvm25.jar` from `build/libs/` into `plugins/MineCore/hooks/`.
The plugin loads the jar by reading `minecore-hook.properties` inside the jar.

