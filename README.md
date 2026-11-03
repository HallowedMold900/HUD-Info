# HUD Info

HUD Info is a lightweight, fully customizable on-screen display for Minecraft. It shows the information you check most often — right on your screen — and lets you arrange and style every piece of it exactly the way you like, without editing any files.

## Features

### Information shown
| Element | What it displays |
|---|---|
| **FPS** | Current frames per second, refreshed twice a second |
| **Ping** | Your latency to the server in milliseconds (0 ms in singleplayer) |
| **Biome** | The name of the biome you are standing in, in your game language |
| **Day** | The current in-game day |
| **Coordinates** | Your X / Y / Z position with one decimal place |

### Built-in HUD editor
Press **K** (changeable in *Options → Controls → HUD Info*) to open the editor. All changes are shown live.

- **Drag & drop** – grab any element with the mouse and move it anywhere on the screen. Elements are kept inside the screen automatically.
- **Per-element settings** – click an element to open its settings panel:
  - **Show / hide** the element
  - **Text size** from 0.5x to 3.0x
  - **Text color** with separate R, G and B sliders and a live color preview
  - **Background opacity** from 0% (fully transparent) to 100%
  - **Reset** the element to its defaults
- Every element is configured independently, so you can have a big yellow coordinates line and a tiny transparent FPS counter at the same time.

### Saved automatically
Your layout and styling are stored in `config/hudinfo.json` and restored on the next launch.

### Other details
- Client-side only: nothing is required on the server, and it works in singleplayer and on any server.
- Translated into **English** and **Russian**.
- Requires **Fabric Loader 0.19.5+** and **Fabric API**.

## How to use
1. Join a world.
2. Press **K** to open the editor.
3. Drag the elements where you want them, click one to change its size, color and background.
4. Press **Done** (or Esc) – your settings are saved.