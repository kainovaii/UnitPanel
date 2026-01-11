package fr.kainovaii.spark.app.services;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

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
        return status;
    }

    public static ServiceStats getStats(String unit)
    {
        try {
            String pid = exec("sudo", "systemctl", "show", unit, "--property=MainPID").trim();
            pid = pid.replace("MainPID=", "");

            if (pid.equals("0") || pid.isEmpty()) {
                return new ServiceStats(0.0, 0.0, "0 B");
            }
            String stats = exec("sudo", "ps", "-p", pid, "-o", "%cpu,%mem,rss", "--no-headers").trim();
            if (stats.isEmpty()) {
                return new ServiceStats(0.0, 0.0, "0 B");
            }
            String[] parts = stats.trim().split("\\s+");
            double cpu = Double.parseDouble(parts[0]);
            double mem = Double.parseDouble(parts[1]);
            long rssKb = Long.parseLong(parts[2]);
            String ramFormatted = formatBytes(rssKb * 1024);
            return new ServiceStats(cpu, mem, ramFormatted);
        } catch (Exception e) {
            System.err.println("Error getting stats for " + unit + ": " + e.getMessage());
            return new ServiceStats(0.0, 0.0, "0 B");
        }
    }

    public static ServiceStats getTotalStats(List<String> units)
    {
        double totalCpu = 0.0;
        double totalMemPercent = 0.0;
        long totalRamBytes = 0;

        for (String unit : units) {
            try {
                ServiceStats stats = getStats(unit);
                totalCpu += stats.cpu;
                totalMemPercent += stats.memPercent;
                totalRamBytes += parseRamToBytes(stats.ram);
            } catch (Exception e) {
                System.err.println("Error getting stats for " + unit + ": " + e.getMessage());
            }
        }

        String totalRamFormatted = formatBytes(totalRamBytes);
        return new ServiceStats(totalCpu, totalMemPercent, totalRamFormatted);
    }

    private static long parseRamToBytes(String ram)
    {
        try {
            String[] parts = ram.split(" ");
            double value = Double.parseDouble(parts[0]);
            String unit = parts[1];

            switch (unit) {
                case "B": return (long) value;
                case "KB": return (long) (value * 1024);
                case "MB": return (long) (value * 1024 * 1024);
                case "GB": return (long) (value * 1024 * 1024 * 1024);
                default: return 0;
            }
        } catch (Exception e) {
            return 0;
        }
    }

    private static String formatBytes(long bytes)
    {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
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

        public String toJson() {
            return String.format("{\"cpu\":%.2f,\"mem\":%.2f,\"ram\":\"%s\"}", cpu, memPercent, ram);
        }
    }

    public static String listFiles(String directory) throws Exception
    {
        String output = exec("sudo", "ls", "-la", directory);
        return output;
    }

    public static String readFile(String filePath) throws Exception
    {
        String content = exec("sudo", "cat", filePath);
        return content;
    }

    public static void writeFile(String filePath, String content) throws Exception
    {
        File tempFile = File.createTempFile("file-edit-", ".tmp");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(content);
        }
        exec("sudo", "cp", tempFile.getAbsolutePath(), filePath);
        tempFile.delete();
    }

    public static String[] getDirectoryTree(String directory) throws Exception
    {
        String output = exec("sudo", "find", directory, "-type", "f", "-not", "-path", "*/.*");
        return output.split("\n");
    }
}