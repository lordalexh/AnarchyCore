<div align="center">

# ⚡ AnarchyCore

**High-performance security, moderation, and core utility engine built for Folia and Paper.**

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1+-brightgreen?style=for-the-badge&logo=minecraft)
![Platform](https://img.shields.io/badge/Platform-Paper%20%7C%20Folia-blue?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![PacketEvents](https://img.shields.io/badge/PacketEvents-v2.13.0-purple?style=for-the-badge)
![Build](https://img.shields.io/badge/Build-Gradle-blueviolet?style=for-the-badge&logo=gradle)

</div>

---

## 📖 Overview

**AnarchyCore** is a comprehensive, low-overhead core plugin tailored for high-throughput Minecraft anarchy servers. Designed natively for **Paper** and **Folia**, it replaces traditional bloated "essentials" plugins with an optimized, multithread-friendly feature set. It handles everything from raw packet security and offline inventory editing to advanced moderation and combat tagging.

---

## ✨ Key Features

* **🧰 Full Essentials Replacement**  
  A completely optimized suite of core commands including `/ping`, `/tps`, `/msg`, `/reply`, `/suicide`, `/heal`, `/feed`, and `/gamemode`.
* **🛠️ Advanced Moderation Suite**  
  High-performance moderation tools including SQLite-backed `/ban`, `/mute`, `/kick`, `/freeze`, `/vanish`, and `/socialspy`. Server chat management with `/clearchat` and `/slowchat`.
* **🎒 Offline Inventory & Enderchest Management**  
  Fully bundled and autonomous `.dat` parsing system. Use `/invsee <player>` and `/enderchest <player>` on completely offline players. Interacts with modern 1.21+ Data Components without requiring external dependency plugins.
* **⚔️ Combat Tagging System**  
  Built-in combat system preventing players from safely logging out or executing specific commands while engaged in PvP.
* **🛡️ Zero-Leak Command Security (Packet-Level)**  
  Intercepts Brigadier command tree packets (`DECLARE_COMMANDS`) directly on the Netty network layer using **PacketEvents**. Blocked commands are stripped out before reaching the player client, preventing autocompletion leaks completely.

---

## 🛡️ Security Architecture

AnarchyCore implements a dual-layer defense system:

| Layer | Component | Mechanism | Result |
| :--- | :--- | :--- | :--- |
| **Network (Packet)** | `PacketSecurityListener` | Modifies raw `DECLARE_COMMANDS` packets via PacketEvents. | Prevents client tab autocompletion completely. |
| **Server (Bukkit)** | `SecurityListener` | Intercepts `PlayerCommandPreprocessEvent`. | Prevents direct manual execution of commands. |

### Blocked Commands by Default
* Core Info: `/plugins`, `/pl`, `/version`, `/ver`, `/about`
* Server Engine Info: `/paper`, `/folia`, `/bukkit`, `/icanhasbukkit`

---

## 🔑 Key Permissions

| Permission | Description |
| :--- | :--- |
| `anarchycore.admin` | Bypasses command restrictions and allows full command autocompletion. |
| `anarchycore.mod` | Grants access to moderation tools (kick, ban, mute, freeze, invsee, etc). |

---

## 🛠️ Building from Source

### Prerequisites
* **JDK 21**
* **Git**

### Compilation
Clone the repository and build the fat JAR using the Gradle wrapper:

```bash
# Clone the repository
git clone https://github.com/lordalexh/AnarchyCore.git
cd AnarchyCore

# Build project with Gradle
./gradlew build
```
### The compiled plugin jar file will be located at:
`build/libs/AnarchyCore-[version].jar`

