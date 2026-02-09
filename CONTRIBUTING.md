# 💻 Contributing & Git Workflow

This document outlines the strict rules for contributing code to **Fat Piggies**.

## 1. Branching Strategy

We follow a simplified Git Flow. **NEVER push directly to `main` or `develop`.**

* **`main`**: Production code only. Stable and playable.
* **`develop`**: Integration branch. All features are merged here first.
* **`feature/name-of-feature`**: Your personal workspace.

### Naming Convention
When creating a branch, use these prefixes:
* `feature/` (e.g., `feature/jump-mechanic`)
* `fix/` (e.g., `fix/menu-crash`)
* `assets/` (e.g., `assets/player-sprites`)

---

## 2. Daily Workflow

1.  **Sync:** Always pull `develop` before starting: `git pull origin develop`.
2.  **Branch:** Create your branch: `git checkout -b feature/my-new-feature`.
3.  **Commit:** Save often. Use clear messages (e.g., "Added jump sound", not "update").
4.  **Push:** Push your branch to GitHub.
5.  **PR:** Open a **Pull Request** from your branch to `develop`.
    * *Requirement:* At least 1 team member must review and approve the PR.

---

## 3. LibGDX Specific Rules

### ⛔ The `.gitignore`
Ensure your local environment respects the `.gitignore`.
**NEVER commit:**
* `local.properties` (Contains your personal SDK path)
* `.idea/` folder (Your personal editor settings)
* `build/` folders (Temporary compiled files)

### 🖼️ Assets
* Place all images, sounds, and skins in the `android/assets/` folder.
* **Warning:** If adding large files (>50MB), verify if Git LFS is active.

### 💥 Conflict Resolution
If you encounter a Merge Conflict in `gameworld.java` (or any file):
1.  Don't panic.
2.  Contact the person who edited the file recently.
3.  Decide together which lines of code to keep.
