# Periodic v2 — Filament 3D

This branch replaces the Canvas-rendered periodic table with a true 3D glTF scene rendered by Google's **Filament 1.74.0** physically based rendering engine.

## Included
- 118 real 3D element blocks in one optimized GLB scene
- PBR metallic / roughness materials with category colors
- Textured element faces with atomic number, symbol, and family
- Real perspective camera, orbit rotation, and pinch zoom
- Physical platform geometry plus a 3D Saturn and ring system
- Filament post-processing and exposure controls
- Existing animated oxygen splash and detailed atomic exhibit pages
- GitHub Actions APK build for Android 15

## Interaction
- Drag: orbit the 3D scene
- Pinch: zoom
- Double-tap: reset framing
- Tap a tile from the initial front view: open its element exhibit

The GLB is generated as an offline asset, so the application does not need internet access while running.
