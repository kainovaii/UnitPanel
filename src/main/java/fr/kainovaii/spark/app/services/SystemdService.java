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

    public static String getStatus(String unit)
    {
        String status = exec("sudo", "systemctl", "is-active", unit).trim();
        return status; // "active", "inactive", "failed", etc.
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

    public static ServiceStats getStats(String unit)
    {
        try {
            // Récupérer le PID principal du service
            String pid = exec("sudo", "systemctl", "show", unit, "--property=MainPID").trim();
            pid = pid.replace("MainPID=", "");

            if (pid.equals("0") || pid.isEmpty()) {
                return new ServiceStats(0.0, 0.0, "0 B");
            }

            // Récupérer CPU et RAM via ps
            String stats = exec("sudo", "ps", "-p", pid, "-o", "%cpu,%mem,rss", "--no-headers").trim();

            if (stats.isEmpty()) {
                return new ServiceStats(0.0, 0.0, "0 B");
            }

            String[] parts = stats.trim().split("\\s+");
            double cpu = Double.parseDouble(parts[0]);
            double mem = Double.parseDouble(parts[1]);
            long rssKb = Long.parseLong(parts[2]); // RSS en KB

            String ramFormatted = formatBytes(rssKb * 1024); // Convertir en bytes puis formater

            return new ServiceStats(cpu, mem, ramFormatted);
        } catch (Exception e) {
            System.err.println("Error getting stats for " + unit + ": " + e.getMessage());
            return new ServiceStats(0.0, 0.0, "0 B");
        }
    }

    private static String formatBytes(long bytes)
    {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    public static class ServiceStats
    {
        public final double cpu;
        public final double memPercent;
        public final String ram;

        public ServiceStats(double cpu, double memPercent, String ram)
        {
            this.cpu = cpu;
            this.memPercent = memPercent;
            this.ram = ram;
        }

        public String toJson() { return String.format("{\"cpu\":%.2f,\"mem\":%.2f,\"ram\":\"%s\"}", cpu, memPercent, ram); }
    }
}

