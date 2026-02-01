# 🎮 OX Event Plugin

Minecraft Paper 1.21.11 plugin to run automated **True/False (OX) Events**.

## ✨ Features

-   **⚡ Event Management**: Easy commands to init, start, and end the event.
-   **❓ Question System**: Create and save questions to `questions.yml`.
-   **🤖 Automation**:
    -   ⏳ 20s countdown timer.
    -   📢 Action Bar & Boss Bar notifications.
    -   🧹 Auto-clearing of wrong zones (floor disappears!).
    -   🏗️ Zone restoration after each round.
-   **🛡️ Fail-safe & Protections**:
    -   🦅 Falling players are safely teleported to the spectator area.
    -   🚫 PvP, Block Break/Place, and Command blocks during event.

## 🛠️ Commands (Admin Only)

-   `/ox setspawn` 📍 - Set the starting location (lobby/platform).
-   `/ox setwidownia` 👀 - Set the spectator area for eliminated players.
-   `/ox set <o|x>` 🟩🟥 - Set the **TRUE (O)** or **FALSE (X)** zone (requires selection with wooden shovel 🪵).
-   `/ox setteleport` 🌀 - Set the fail-safe teleport zone (players entering this are eliminated).
-   `/ox createquestion <id> <true/false> <text>` 📝 - Create a new question.
-   `/ox init` 🚀 - Teleport all players to the event spawn.
-   `/ox start` ▶️ - Start the event (intro sequence).
-   `/ox question <id>` 🎤 - Ask a specific question and start the timer.
-   `/ox end` 🏁 - End the event and teleport players to the world exit.

## 📥 Installation

1.  Drop the `.jar` file into your server's `plugins/` folder.
2.  Restart the server.
3.  Configure the arena zones using a **Wooden Shovel** and `/ox set...` commands.

## 📄 License

MIT
