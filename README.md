# Colorful Subtitles
[![GitHub Release](https://img.shields.io/github/v/release/awes1000/Colorful-Subtitles)](https://github.com/awes1000/Colorful-Subtitles/releases/latest)

Forked for personal use

Most code in this fork has been updated for personal use, so please test carefully before publishing or sharing builds.

Changes the color of subtitles based on their sound category.

## Requirements
- Minecraft 26.2
- Fabric Loader 0.19.3+
- Fabric API 0.152.2+
- [Cloth Config](https://modrinth.com/mod/cloth-config) 26.2.155+
- [Mod Menu](https://modrinth.com/mod/modmenu) 20.0.0+ recommended for in-game configuration

## Compatibility notes
- The subtitle rendering mixin targets Minecraft 26.2 internals. Re-test subtitles in game after every Minecraft update.
- Missing sound categories inherit the default color. Explicit category entries without a background color render without a custom background.


## Usage

When this mod is installed, subtitles will automatically have a different color depending on their sound category:

* Music: dark purple
* Records: dark red
* Weather: aqua
* Blocks: green
* Hostile: red
* Neutral: yellow
* Players: gold
* Ambient: gray
* Voice: light purple
