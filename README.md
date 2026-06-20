<a id="readme-top"></a>

<h1>
  <br>
  <a href="https://github.com/mineklub/MineCore/releases"><img src="./.github/MineClub_background_1920x1080.png" alt="MineClub Logo"></a>
</h1>

<h2 align="center">MineCore.</h2>

<h4 align="center">Plugin til håndtering af betalinger, motd osv. på Mineclub.dk.</h4>

<p>
    <a href="https://github.com/mineklub/MineCore/commits/main"></a>
    <img src="https://img.shields.io/github/last-commit/mineklub/MineCore.svg?style=flat-square&logo=github&logoColor=white"
         alt="GitHub last commit">
    <a href="https://github.com/mineklub/MineCore/issues"></a>
    <img src="https://img.shields.io/github/issues-raw/mineklub/MineCore.svg?style=flat-square&logo=github&logoColor=white"
         alt="GitHub issues">
    <a href="https://github.com/mineklub/MineCore/pulls"></a>
    <img src="https://img.shields.io/github/issues-pr-raw/mineklub/MineCore.svg?style=flat-square&logo=github&logoColor=white"
         alt="GitHub pull requests" />
</p>

<p>
  <a href="#build">Build</a> •
  <a href="https://docs.mineclub.dk">Docs</a> •
  <a href="https://discord.gg/ePxVMN5ACh">Discord</a> •
  <a href="#license">License</a>
</p>

---
<a id="build"></a>
## Build
Brug følgende Gradle-kommando til at bygge MineCore:

```gradle
gradle build
```

Du kan skifte Java target-version mellem 25 (default) og 21.
**Vigtig:** Du skal matche Java-versionen med din server:

```gradle
gradle build -PminepayJavaVersion=25
gradle build -PminepayJavaVersion=21
```

> [!NOTE]  
> - Den genererede MineCore-fil gemmes i mappen `build/libs/MineCore-Bukkit-{version}.jar`.
> - Den genererede platform-paper plugin gemmes i `platform/paper/build/libs/platform-paper-{version}-all.jar`.
> - `api` publiceres i flere Java-varianter:
>   - Default artifact (`api`) er Java 21.
>   - Ekstra classifiers: `jvm8`, `jvm11`, `jvm17`, `jvm25` (fx `dk.minecore:api:1.0.0:jvm17`).
>   - Der publiceres ogsa dedikerede artifacts med egen POM pr. Java-version:
>     - `dk.minecore:api-jvm8:1.0.0`
>     - `dk.minecore:api-jvm11:1.0.0`
>     - `dk.minecore:api-jvm17:1.0.0`
>     - `dk.minecore:api-jvm25:1.0.0`
> - Hvis du får fejlen "Unsupported class file major version", skal du matche plugin-versionen med serverens Java-version:
>   - Java 21 server → `gradle build -PminepayJavaVersion=21`
>   - Java 25 server → `gradle build -PminepayJavaVersion=25`
>

## License

[![License: GPL-3.0](https://img.shields.io/badge/License-GPL%203.0-lightgrey.svg)](https://tldrlegal.com/license/gnu-general-public-license-v3-gpl-3)