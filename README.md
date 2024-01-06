### This mod (especially in its current state) is more of a proof of concept than actual optimization mod!
It has many issues and greatly reduces performance of main rendering thread (even when disabled),
so for comparing performance use instance without mod installed.

# About
**Asynchronous Reprojection** is a Minecraft mod that creates second rendering context to asynchronously reproject frames from main rendering thread with new camera rotation and player position.

It was inspired by Comrade Stinger's demo, which was also base for shader code:
https://www.youtube.com/watch?v=VvFyOFacljg

Known issues:
- Fabric version works only on Windows and Linux (I haven't tested it on macos). Forge version works only on Windows.
- Screenshots (F2) and world icons don't work - they're black.
- Minor visual glitches.
- Camera bobbing is disabled.
- "Fabulous" graphics doesn't change anything compared to "Fancy".
- Camera sometimes rotates itself when opening or closing GUI.
- Forge version crashes when trying to resize window during startup.
- It sometimes just crashes (for no particular reason).

CurseForge page: https://www.curseforge.com/minecraft/mc-mods/asynchronous-reprojection

Modrinth page: https://modrinth.com/mod/asynchronous-reprojection

# Examples

![](https://raw.githubusercontent.com/mt1006/mc-ar-mod/_common/screenshots/example1.png)

![](https://raw.githubusercontent.com/mt1006/mc-ar-mod/_common/screenshots/example2.png)

![](https://raw.githubusercontent.com/mt1006/mc-ar-mod/_common/screenshots/example3.png)
