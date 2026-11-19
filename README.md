# Spin — Minecraft Forge 1.20.1

Standalone Forge mod port based on the supplied Spin 1.1.2 archive. The project does not require JCraft at runtime.

## Target
- Minecraft 1.20.1
- Forge 47.x
- Java 17

## Current Steel Ball behavior
- Hold right-click to charge the Steel Ball, then release to throw it.
- The held Steel Ball rotates while it is being charged.
- The thrown Steel Ball leaves an orbiting green particle effect and spins along its flight direction.
- The ball follows a higher arc, damages the first living entity it hits, then returns exactly one Steel Ball to its owner.
- Throwing consumes one Steel Ball in all game modes, including Creative.

## Temporarily disabled
Separate keyboard abilities and the prototype SPIN meter actions have been removed for now. They can be reintroduced individually when their behavior is specified.

## Build
The GitHub Actions workflow builds with JDK 17 and Gradle 8.8. The JAR is uploaded as the Spin-Forge-1.20.1 workflow artifact after a successful run. For local builds, install JDK 17 and Gradle 8.8, then run `gradle build`.

## Porting note
The original archive contains compiled .class files rather than Java source files. The independent Forge implementation re-creates the core moves from the archive's class signatures and moveset JSON definitions; it is not a byte-for-byte source reconstruction of the original JCraft integration. Full player animation runtime integration remains separate from the standalone implementation.
