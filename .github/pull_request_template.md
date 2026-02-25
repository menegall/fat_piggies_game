# 📥 Pull Request Description

## 🔗 Related Issue
Closes #

## 🛠️ Type of Change
- [ ] 🐛 **Bug fix** (non-breaking change which fixes an issue)
- [ ] ✨ **New feature** (non-breaking change which adds functionality)
- [ ] 💥 **Breaking change** (fix or feature that would cause existing functionality to not work as expected)
- [ ] 📦 **Assets** (adding images, audio, or prefabs)

## 📝 Summary of Changes
* Implemented Box2D body for the "Heavy Pig" class.
* Updated the `assets/sounds` folder with new impact SFX.

---

## ✅ Self-Review Checklist

### Code Quality
- [ ] My code follows the **Naming Conventions** in `CONTRIBUTING.md`.
- [ ] I have added **Javadoc** for all new public methods.
- [ ] I have deleted all `System.out.println` and unused imports.

### Assets & Resources
- [ ] I have added all new assets to the correct `android/assets` folder.
- [ ] I have verified that no `new Texture()` or similar objects are created in the `render()` loop.
- [ ] I have verified that I am NOT committing large binary files (>100MB) without LFS.

### Testing
- [ ] I have tested this on **Desktop**.
- [ ] I have tested this on **Android Emulator** or **Real Device**.
