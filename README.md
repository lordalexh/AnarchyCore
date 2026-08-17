<div align="center">

# ⚡ AnarchyCore

**High-performance security, packet manipulation, and core utility engine built for Folia and Paper.**

![Minecraft](https://img.shields.io/badge/Minecraft-1.20%2B-brightgreen?style=for-the-badge&logo=minecraft)
![Platform](https://img.shields.io/badge/Platform-Folia%20%7C%20Paper-blue?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-17%2B-orange?style=for-the-badge&logo=openjdk)
![PacketEvents](https://img.shields.io/badge/PacketEvents-v2.13.0-purple?style=for-the-badge)
![Build](https://img.shields.io/badge/Build-Gradle-blueviolet?style=for-the-badge&logo=gradle)

</div>

---

## 📖 Overview

**AnarchyCore** provides essential, low-overhead security and packet-level modifications for high-throughput Minecraft anarchy servers. Built from the ground up to support multithreaded server architectures (**Folia** and **Paper**), it intercepts network packets before they ever reach the game engine.

---

## ✨ Key Features

* **🛡️ Zero-Leak Command Security (Packet-Level)**  
  Intercepts Brigadier command tree packets (`DECLARE_COMMANDS`) directly on the Netty network layer using **PacketEvents**. Blocked commands are stripped out before reaching the player client, preventing autocompletion leaks without client-side glitches.
* **🔒 Bukkit Event Protection**  
  Secondary execution barrier that catches and blocks direct command execution attempts for non-admin players.
* **👑 Permission-Based Bypass**  
  Configured admins and OPs retain full visibility and autocompletion access for server management commands.
* **🚀 Velocity Proxy Ready**  
  Fully optimized to work alongside Velocity proxy environments without packet collisions or command duplication.

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

## 🔑 Permissions

| Permission | Description | Default |
| :--- | :--- | :--- |
| `anarchycore.admin` | Bypasses command restrictions and allows full command autocompletion | `OP` |

---

## 🛠️ Building from Source

### Prerequisites
* **JDK 17** or **JDK 21**
* **Git**

### Compilation
Clone the repository and build the shadow JAR using the Gradle wrapper:

```bash
# Clone the repository
git clone [https://github.com/lordalexh/AnarchyCore.git](https://github.com/lordalexh/AnarchyCore.git)
cd AnarchyCore

# Build project with Gradle
./gradlew build
```
### The compiled plugin jar file will be located at:
build/libs/AnarchyCore-1.0-SNAPSHOT.jar
