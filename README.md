<h1 align="center">
    UnitPanel
    <br />
</h1>

<p align="center">
    <img src="https://img.shields.io/badge/Version-1.0-orange.svg" />
    <img style="margin-left: 10px;" src="https://img.shields.io/badge/License-MIT-orange.svg" />
</p>

🌟 UnitPanel

UnitPanel is a web-based control panel for managing systemd services, with real-time logs and simple application lifecycle management.

![Java](https://img.shields.io/badge/Java-17-blue?logo=java) ![Maven](https://img.shields.io/badge/Maven-3.9.0-red?logo=apache-maven) ![Spark](https://img.shields.io/badge/Spark-2.9.4-orange) ![SQLite](https://img.shields.io/badge/SQLite-3.41.2.1-lightgrey)

---

## TODO
- [X] Add delete service
- [X] Add edit service
- [ ] Add edit unit file
- [ ] Add Save backup
- [X] Fix files not found
- [X] Files link replace trash in service list

---

## 💻 Technologies utilisées

- **[Spark Java](https://sparkjava.com/)** : framework web léger pour créer des routes et gérer les requêtes HTTP.
- **[Gson](https://github.com/google/gson)** : sérialisation et désérialisation JSON.
- **[Java Dotenv](https://github.com/cdimascio/java-dotenv)** : gestion sécurisée des variables d’environnement.
- **[Reflections](https://github.com/ronmamo/reflections)** : scan et réflexion sur les classes et annotations.
- **[Pebble Templates](https://pebbletemplates.io/)** : moteur de templates HTML dynamique.
- **[ActiveJDBC](https://javalite.io/activejdbc)** : ORM pour manipuler la base de données.
- **[SQLite JDBC](https://github.com/xerial/sqlite-jdbc)** : driver JDBC pour SQLite.
- **[Tabler](https://github.com/tabler/tabler)** : html dashboard.

---

## 🖼️ Aperçu


<table>
    <tr>
        <td><img src="https://i.ibb.co/fYx72yLy/Capture-d-cran-2026-01-11-191413.png" alt="Aperçu 1" width="100%"/></td>
        <td><img src="https://i.ibb.co/Vpq1TzpQ/Capture-d-cran-2026-01-11-164230.png" alt="Aperçu 3" width="100%"/></td>
        <td><img src="https://i.ibb.co/Zp4mW9fb/Capture-d-cran-2026-01-11-164323.png" alt="Aperçu 4" width="100%"/></td>    
</tr>
</table>

---

## 🚀 Installation

```bash
git clone https://github.com/kainovaii/UnitPanel.git
cd UnitPanel
./build.sh
```