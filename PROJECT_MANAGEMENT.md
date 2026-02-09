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
4.  **Projects:** Select "Fat Piggies Board" (this adds it to the Todo column).
5.  **Labels:** Add a label to categorize the work:
    * `bug` 🐞
    * `feature` ✨
    * `graphics` 🎨
    * `physics` 🧱
    * `ui` 🖥️

---

## Moving Cards

* **Start:** When you start coding, drag your card from **Todo** to **In Progress**.
* **Finish:** When you open a Pull Request, link the issue (e.g., write "Closes #5" in the PR description) and move the card to **In Review**.
* **Close:** When the PR is merged, the card moves to **Done**.
