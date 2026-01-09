package fr.kainovaii.spark.core;

import fr.kainovaii.spark.app.models.Setting;
import fr.kainovaii.spark.app.repository.SettingRepository;
import fr.kainovaii.spark.app.repository.UserRepository;
import fr.kainovaii.spark.core.database.SQLite;
import fr.kainovaii.spark.core.web.WebServer;
import org.javalite.activejdbc.LazyList;

import java.util.logging.Logger;

public class Spark
{
    public final static Logger LOGGER =  Logger.getLogger("Spark");;
    private final SQLite sqlite;
    private static String webPort;

    public Spark()
    {
        this.sqlite =  new SQLite(Spark.LOGGER);
    }

    public void connectDatabase()
    {
        System.out.println("Loading database");
        sqlite.connectDatabaseForCurrentThread();
        sqlite.ensureTablesExist();
    }

    public void loadConfigAndEnv()
    {
        EnvLoader env = new EnvLoader();
        env.load();
        webPort = env.get("PORT_WEB");
    }

    public void startWebServer() { new WebServer().start(); }

    public static int getWebPort() { return Integer.parseInt(webPort); }

    public void registerMotd()
    {
        EnvLoader env = new EnvLoader();

        env.load();
        final String RESET = "\u001B[0m";
        final String CYAN = "\u001B[36m";
        final String YELLOW = "\u001B[33m";
        final String GREEN = "\u001B[32m";
        final String MAGENTA = "\u001B[35m";

        System.out.println(CYAN + "+--------------------------------------+" + RESET);
        System.out.println(CYAN + "|          Spark 1.0            |" + RESET);
        System.out.println(CYAN + "+--------------------------------------+" + RESET);
        System.out.println(GREEN + "| Developpeur       : KainoVaii        |" + RESET);
        System.out.println(GREEN + "| Version           : 1.0              |" + RESET);
        System.out.println(GREEN + "| Environnement     : " + env.get("ENVIRONMENT") + "              |" + RESET);
        System.out.println(CYAN + "+--------------------------------------+" + RESET);
        System.out.println(CYAN + "|      Chargement des modules...       |" + RESET);
        System.out.println(CYAN + "+--------------------------------------+" + RESET);
        System.out.println();
    }

    public void initWebsite()
    {
        UserRepository userRepository = new UserRepository();
        SettingRepository settingRepository = new SettingRepository();

        LazyList<Setting> settings = settingRepository.getAll();

        boolean adminExist = false;
        if (!settings.isEmpty()) {
            Setting setting = settings.get(0);
            adminExist = setting.getBoolean("admin_exist");
        }

        if (!adminExist && !UserRepository.userExist("admin")) {
            userRepository.create("admin", "$2a$12$8oYepa4rQw2xixu1KpvTbeg9aVAifZCUZGhn5/rfE7ugjqk9SXi5q", "ADMIN");
            if (settings.isEmpty()) {
                settingRepository.create(true);
            } else {
                Setting setting = settings.get(0);
                setting.set("admin_exist", true);
                setting.saveIt();
            }
        }
    }
}
