![Mantle logo](src/main/resources/Mantle.png)

# Mantle

![MIT](https://img.shields.io/badge/license-MIT-blue.svg?longCache=true&style=flat)

Unofficial NeoForge 1.21.1 port of [Mantle](https://github.com/SlimeKnights/Mantle),
originally by [Slime Knights](https://github.com/SlimeKnights).

Mantle is the shared library required by
[Tinkers' Construct](https://github.com/Maf1sher/TinkersConstruct-NeoForge) and
the add-ons built on it. Place it in the `mods` folder alongside them; it
provides data loading and serialization, registration helpers, recipe builders,
fluid and loot utilities, and shared client and guidebook code.

## Building

Requires JDK 21. Build from the repository root.

Linux / macOS:

```bash
./gradlew build
```

Windows (Command Prompt or PowerShell):

```bat
gradlew.bat build
```

The jar is written to `build/libs/Mantle-1.21.1-*.jar`.

## Credits

- Original mod and author: [Slime Knights](https://github.com/SlimeKnights/Mantle)
- NeoForge 1.21.1 port: [Maf1sher/Mantle-NeoForge](https://github.com/Maf1sher/Mantle-NeoForge)
- License: MIT (see [LICENSE](LICENSE))
