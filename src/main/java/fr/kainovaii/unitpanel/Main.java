package fr.kainovaii.unitpanel;

import fr.kainovaii.core.Spark;
public class Main
{
    public static void main(String[] args) throws Exception
    {
        Spark app = new Spark();
        app.registerMotd();
        app.loadConfigAndEnv();
        app.connectDatabase();
        app.startWebServer();
        app.initWebsite();
    }
}