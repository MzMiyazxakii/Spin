# Spin — Minecraft Forge 1.20.1

Standalone Forge mod port based on the supplied Spin 1.1.2 archive. The project does not require JCraft at runtime.

## Target
- Minecraft 1.20.1
- Forge 47.x
- Java 17

## Implemented in this branch
- Original Blockbench Steel Ball item model and original item texture.
- Original Steel Ball projectile texture packaged as a resource.
- Steel Ball throwable projectile, charge meter HUD and client/server action channel.
- Spin charge, Spin-Imbued Punch, standard throw, Fast Ball, Curveball, Bouncy Ball and Drill throw variants.
- Curving trajectory, bounce behavior, returning ball and catch sound event.
- Original Spin audio definitions and copied legacy assets in the standalone source archive.

## Default controls
| Key | Ability |
| --- | --- |
| V (hold/release) | Charge Spin / throw |
| C | Spin-Imbued Punch |
| R | Fast Ball |
| F | Curveball |
| B | Bouncy Ball |
| G | Drill Throw |

Abilities that throw a ball require a Steel Ball in the main or off hand. Key bindings can be changed in Minecraft Controls.

## Build
The GitHub Actions workflow builds with JDK 17 and Gradle 8.8. The JAR is uploaded as the Spin-Forge-1.20.1 workflow artifact after a successful run. For local builds, install JDK 17 and Gradle 8.8, then run gradle build.

## Porting note
The original archive contains compiled .class files rather than Java source files. The independent Forge implementation re-creates the core moves from the archive's class signatures and moveset JSON definitions; it is not a byte-for-byte source reconstruction of the original JCraft integration. Full player animation runtime integration remains separate from the standalone implementation.
