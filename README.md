# Spin — Minecraft Forge 1.20.1

Standalone Forge mod. No JCraft runtime dependency.

- Minecraft 1.20.1 / Forge 47.2.0 / Java 17
- Steel Ball item with charge-to-throw mechanics
- Spin-enhanced melee hit and projectile damage scaling
- Client renderer and localized item name
- GitHub Actions builds the mod JAR and uploads it as an artifact

## Build locally
Install JDK 17 and Gradle 8.8, then run:

```bash
gradle build
```

The output JAR is in `build/libs/`.

## Controls
- Hold right-click with the Steel Ball to charge; release to throw.
- Left-click an entity while holding the Steel Ball for a Spin punch.

## Status
Initial standalone gameplay implementation. The legacy ZIP has the original textures, models, animations and audio; those binary resources are packaged in the local source archive but are not yet committed individually to this repository. The advanced ability set (Fast Ball, Curveball, Bouncy Ball and Drill variants), full animation integration and charge HUD are not yet implemented.
