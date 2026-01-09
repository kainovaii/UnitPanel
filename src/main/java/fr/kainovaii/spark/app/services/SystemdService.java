package fr.kainovaii.spark.app.services;

public class SystemdService
{
    public static void start(String unit) {
        exec("systemctl", "start", unit);
    }

    public static void stop(String unit) {
        exec("systemctl", "stop", unit);
    }

    public static String logs(String unit) {
        return exec("journalctl", "-u", unit, "-n", "200");
    }

    private static String exec(String... cmd) {
        try {
            Process p = new ProcessBuilder(cmd).start();
            return new String(p.getInputStream().readAllBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
