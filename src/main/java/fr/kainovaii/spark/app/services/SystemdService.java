package fr.kainovaii.spark.app.services;

import java.io.File;
import java.io.FileWriter;

public class SystemdService
{
    private static final String SYSTEMD_PATH = "/etc/systemd/system/";

    public static void start(String unit)
    {
        System.out.println("Starting service: " + unit);
        String result = exec("sudo", "systemctl", "start", unit);
        System.out.println("Start result: " + result);
    }

    public static void stop(String unit)
    {
        System.out.println("Stopping service: " + unit);
        String result = exec("sudo", "systemctl", "stop", unit);
        System.out.println("Stop result: " + result);
    }

    public static String logs(String unit)
    {
        String status = exec("sudo", "systemctl", "is-active", unit);
        String logs = exec("sudo", "journalctl", "-u", unit, "-n", "200");
        return "Status: " + status + "\n" + logs;
    }

    public static void createService(String name, String description, String execStart, String workingDirectory, String user) throws Exception
    {
        if (!name.endsWith(".service")) {
            name = name + ".service";
        }

        StringBuilder content = new StringBuilder();
        content.append("[Unit]\n");
        content.append("Description=").append(description != null ? description : name).append("\n");
        content.append("After=network.target\n\n");

        content.append("[Service]\n");
        content.append("Type=simple\n");
        content.append("ExecStart=").append(execStart).append("\n");

        if (workingDirectory != null) {
            content.append("WorkingDirectory=").append(workingDirectory).append("\n");
        }

        if (user != null) {
            content.append("User=").append(user).append("\n");
        }

        content.append("Restart=on-failure\n");
        content.append("RestartSec=10s\n\n");

        content.append("[Install]\n");
        content.append("WantedBy=multi-user.target\n");

        File tempFile = File.createTempFile("service-", ".service");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(content.toString());
        }

        exec("sudo", "cp", tempFile.getAbsolutePath(), SYSTEMD_PATH + name);
        exec("sudo", "systemctl", "daemon-reload");
        exec("sudo", "systemctl", "enable", name);

        tempFile.delete();
    }

    public static void deleteService(String name) throws Exception
    {
        if (!name.endsWith(".service")) {
            name = name + ".service";
        }

        exec("sudo", "systemctl", "stop", name);
        exec("sudo", "systemctl", "disable", name);

        exec("sudo", "rm", SYSTEMD_PATH + name);
        exec("sudo", "systemctl", "daemon-reload");
    }

    private static String exec(String... cmd)
    {
        try {
            Process p = new ProcessBuilder(cmd).start();
            p.waitFor();

            String error = new String(p.getErrorStream().readAllBytes());
            if (!error.isEmpty()) {
                System.err.println("Error: " + error);
            }

            return new String(p.getInputStream().readAllBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}