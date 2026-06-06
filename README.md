# CrateReloaded

[

![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)

](https://www.gnu.org/licenses/gpl-3.0)
[

![Version](https://img.shields.io/badge/version-2.3.16-green.svg)

](https://github.com/charlyg31/Crate_plugin/releases)
[

![Minecraft](https://img.shields.io/badge/Minecraft-26.1-orange.svg)

](https://www.spigotmc.org/)
[

![Java](https://img.shields.io/badge/Java-25-blue.svg)

](https://openjdk.org/)

Fork de [CrateReloaded](https://github.com/Hazebyte/CrateReloaded) mis à jour pour **Minecraft 26.1** et **Java 25**.

A powerful and flexible crate plugin for Minecraft servers. Create customizable crates with animated openings, varied rewards, and a comprehensive claim system.

---

## ✅ Changements par rapport à l'original

- ✅ Compatible **Minecraft 26.1** (Spigot API 26.1.2)
- ✅ Compilé avec **Java 25**
- ✅ **Lombok supprimé** (remplacé par du code Java natif)
- ✅ Enchantements mis à jour via `Registry.ENCHANTMENT`
- ✅ PotionType et PotionEffectType mis à jour
- ✅ `api-version: 26.1` dans plugin.yml

---

## 🛠️ Compilation

### Prérequis
- Java 25
- Maven 3.9+

### Compiler

```bash
git clone --recurse-submodules https://github.com/charlyg31/Crate_plugin.git
cd Crate_plugin
mvn package -DskipTests -Dmaven.test.skip=true -pl bukkit -am
