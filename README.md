<h1 align="center">
    <img src="https://raw.githubusercontent.com/kainovaii/UnitPanel/refs/heads/main/src/main/resources/assets/img/logo-dark.png" alt="UnitPanel Logo" width="200"/>
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
- [X] Add user management
- [X] Add upload/delete file in editor
- [ ] Rework all route
- [X] Route role access with annotation
- [X] Route with annotation
- [X] Add delete service
- [X] Add edit service
- [X] Add edit unit file
- [ ] Add Save backup
- [X] Fix files not found
- [X] Files link replace trash in service list

---

## 💻 Technologies Used

- **[Spark Java](https://sparkjava.com/)**: lightweight web framework for creating routes and handling HTTP requests.
- **[Gson](https://github.com/google/gson)**: JSON serialization and deserialization.
- **[Java Dotenv](https://github.com/cdimascio/java-dotenv)**: secure management of environment variables.
- **[Reflections](https://github.com/ronmamo/reflections)**: scanning and reflection on classes and annotations.
- **[Pebble Templates](https://pebbletemplates.io/)**: dynamic HTML template engine.
- **[ActiveJDBC](https://javalite.io/activejdbc)**: ORM for database manipulation.
- **[SQLite JDBC](https://github.com/xerial/sqlite-jdbc)**: JDBC driver for SQLite.
- **[Tabler](https://github.com/tabler/tabler)**: HTML dashboard.

---

## 🖼️ Preview

<table>
    <tr>
        <td><img src="https://i.ibb.co/4QsjVdX/Capture-d-cran-2026-01-16-024858.png" alt="Preview 1" width="100%"/></td>
        <td><img src="https://i.ibb.co/QvkB8qSc/Capture-d-cran-2026-01-16-024921.png" alt="Preview 3" width="100%"/></td>
        <td><img src="https://i.ibb.co/hFnhpG99/Capture-d-cran-2026-01-16-024937.png" alt="Preview 4" width="100%"/></td>    
</tr>
</table>

---

## 🚀 Installation
```bash
git clone https://github.com/kainovaii/UnitPanel.git
cd UnitPanel
./build.sh
```