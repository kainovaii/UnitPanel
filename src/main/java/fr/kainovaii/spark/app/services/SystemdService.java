package fr.kainovaii.spark.app.services;

public class SystemdService
{
    public static void start(String unit) {
        System.out.println("Starting service: " + unit);
        String result = exec("sudo", "systemctl", "start", unit);
        System.out.println("Start result: " + result);
    }

    public static void stop(String unit) {
        System.out.println("Stopping service: " + unit);
        String result = exec("sudo", "systemctl", "stop", unit);
        System.out.println("Stop result: " + result);
    }

    public static String logs(String unit)
    {
        String status = exec("systemctl", "is-active", unit);
        String logs = exec("journalctl", "-u", unit, "-n", "200");
        return "Status: " + status + "\n" + logs;
    }

    private static String exec(String... cmd)
    {
        try {
            Process p = new ProcessBuilder(cmd).start();
            return new String(p.getInputStream().readAllBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
