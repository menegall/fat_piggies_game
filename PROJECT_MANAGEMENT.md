# 🗂️ Project Management Guide

We use **GitHub Projects** (Kanban Board) to track who is doing what.

## The Board Columns

1.  **📋 Todo:** Ideas and tasks waiting to be picked up.
2.  **🚧 In Progress:** Tasks currently being worked on.
    * *Rule:* Max 1-2 tasks per person here.
3.  **👀 In Review:** Work finished, PR opened, waiting for approval.
4.  **✅ Done:** Merged into `develop`.

---

## How to Create a Task (Issue)

**Do not start coding without an Issue.**

1.  Go to the **Issues** tab -> **New Issue**.
2.  **Title:** Be specific (e.g., "Implement Box2D collision for walls").
3.  **Assignees:** Select yourself (or the person responsible).
4.  **Projects:** Select "Fat Piggies Dev" (this adds it to the Todo column).
5.  **Labels:** Add at least one label to categorize the work (see the guide below).

---

## 🏷️ Label Guide

Use these labels to help the team filter tasks. **Always verify you have the right label!**

### 🟥 Priority & Type (Standard GitHub)
| Label | Color | When to use it |
| :--- | :--- | :--- |
| `bug` | Red | **Something isn't working.** (e.g., Game crash, physics glitch). |
| `enhancement` | Teal | **New feature request.** (e.g., Add double jump, create new map). |
| `documentation` | Blue | **Writing docs.** (e.g., Updating README, Wiki, or comments). |
| `question` | Pink | **Need info.** (e.g., "How do we handle multiplayer sync?"). |
| `help wanted` | Green | **Stuck?** Use this to signal you need assistance from a teammate. |
| `good first issue` | Purple | **Easy task.** Perfect for warming up or for new members. |
| `wontfix` | White | **Cancelled.** The team decided not to work on this. |
| `duplicate` | Grey | **Repeat.** This issue already exists (link to the original). |

### 🎮 Game Specific Domains
Use these to identify *which part* of the game code you are touching.

* `graphics` 🎨 - Sprites, Textures, Animations, Particle Effects.
* `physics` 🧱 - Box2D, Collisions, Gravity, Movement logic.
* `ui` 🖥️ - Menus, HUD, Buttons, on-screen controls.
* `audio` 🎵 - Sound Effects (SFX), Background Music.
* `networking` 🌐 - Multiplayer code, Server/Client sync, Latency handling.

---

## Moving Cards

* **Start:** When you start coding, drag your card from **Todo** to **In Progress**.
* **Finish:** When you open a Pull Request, link the issue (e.g., write "Closes #5" in the PR description) and move the card to **In Review**.
* **Close:** When the PR is merged, the card moves to **Done**.
