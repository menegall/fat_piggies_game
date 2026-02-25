# 💻 Contributing & Git Workflow

This document outlines the strict rules for contributing code to **Fat Piggies**.

## 1. Branching Strategy

We follow a simplified Git Flow. **NEVER push directly to `main` or `develop`.**

* **`main`**: **STABLE.** Production code only. Stable and playable.
* **`develop`**: **INTEGRATION.** All features are merged here first.
* **`feature/name-of-feature`**: **DEVELOPMENT.** Your personal workspace.

### Naming Convention
When creating a branch, use these prefixes:
* `feature/` (e.g., `feature/jump-mechanic`)
* `fix/` (e.g., `fix/menu-crash`)
* `assets/` (e.g., `assets/player-sprites`)

---

## 2. Code Standards & Naming (TEAM MUST FOLLOW)

To ensure consistency and simplify peer reviews, we adhere to standard Java conventions:

| Element | Convention | Example |
| :--- | :--- | :--- |
| **Classes / Interfaces** | `UpperCamelCase` (Nouns) | `PlayerPig`, `PhysicsManager` |
| **Methods** | `lowerCamelCase` (Verbs) | `calculateMomentum()`, `spawnPig()` |
| **Variables / Fields** | `lowerCamelCase` | `isBouncing`, `playerScore` |
| **Constants** | `UPPER_SNAKE_CASE` | `MAX_PIG_WEIGHT`, `GRAVITY_CONSTANT` |
| **Packages** | `lower.case.with.dots` | `com.fatpiggies.game.physics` |

> [!IMPORTANT]
> * **Language:** All code, comments, and commit messages must be in **English**.
> * **Booleans:** Use prefixes like `is`, `has`, or `can` (e.g., `isAlive`, `hasPowerUp`).
> * **Clean Up:** Remove unused imports and `System.out.println` calls before opening a PR. Use `Gdx.app.log` for debugging.

---

## 3. Javadoc & Documentation

Since this is an Architecture course, documenting the *why* and *how* is crucial. Use Javadoc for all **public** methods and classes.

**Example of required format:**
```java
/**
 * Calculates the impact force when two pigs collide.
 * * @param velocity The current speed of the attacking pig.
 * @param weight The mass of the pig.
 * @return The calculated force to be applied to the target.
 */
public float calculateImpact(float velocity, float weight) {
    return velocity * weight;
}
```
* **Briefly explain** complex logic inside methods with single-line comments (//).
* **Keep it updated:** If you change a method's signature, update its Javadoc!

---

## 4. Daily Workflow

1.  **Sync:** Always pull `develop` before starting: `git pull origin develop`.
2.  **Branch:** Create your branch: `git checkout -b feature/my-new-feature`.
3.  **Commit:** Use **COnventional Commits** for clarity:
   * `feat: add pig dash ability`
   * `fix: resolution scaling on android`
   * `docs: update readme instructions`
   * `refactor: optimize collision loop`
4.  **Push:** Push your branch to GitHub.
5.  **PR:** Open a **Pull Request** from your branch to `develop`.
    * *Requirement:* At least 1 team member must review and approve the PR.
  
When **`develop`** reached a milestone and is tested and safe, it can be merged to main.

---

## 5. Releases & Tagging (Milestones)
We use **Tags** to mark specific versions of the game (e.g., for project deliveries). A Tag is a fixed snapshot of the code at a specific moment.

**How to Tag a Version**
When we reach a milestone on **`main`**, the Team Leader will create a tag:

1. `git checkout main`
2. `git pull origin main`
3. `git tag -a v0.1-alpha -m "Description of the milestone"`
4. `git push origin v0.1-alpha`

**Naming Convention for Tags:**
* `v0.x-alpha`: Early development/Prototype.
* `v0.x-beta`: Feature complete, bug fixing phase.
* `v1.0-final`: Final delivery for the course.

---

## 6. Code Quality & Performance (LibGDX Specifics)
* **No Breaking Builds:** Never push code to `develop` that doesn't compile. Run the `Desktop` launcher locally before committing.
* **Memory Managemnet:** LibGDX objects (Texture, SpriteBatch, Sound, Stage) use native memory. You **MUST** call `.dispose()` in the appropriate method to prevent memory leaks.
* **Assets Management:**
   * **No `new Texture()` in render:** Do not instantiate textures, sounds, or fonts inside the game loop.
   * **Use `AssetManager`:** Centralize all assets in a single manager class. 
   * **Static references:** Access assets via our `Assets` helper class to ensure we don't load the same file twice.
   * **Disposal:** Every asset loaded must be disposed of when the game closes to prevent memory leaks on Android/Desktop.
   
**Rule:** All assets must be registered in `Assets.java`. Use static references to access textures/sounds to ensure memory efficiency and avoid hardcoded strings in game logic.

---

## 7. LibGDX Specific Rules

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
