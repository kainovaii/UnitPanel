package fr.kainovaii.unitpanel.app.services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;

public class SystemdService
{
    private static final String SYSTEMD_PATH = "/etc/systemd/system/";
    private static final Pattern UNIT_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9@._-]+$");
    private static final Pattern PATH_TRAVERSAL_PATTERN = Pattern.compile(".*(\\.\\./|\\.\\.\\\\).*");

    private static void validateUnitName(String unitName) throws SecurityException
    {
        if (unitName == null || unitName.trim().isEmpty()) { throw new SecurityException("Unit name cannot be null or empty"); }
        String cleanName = unitName.replace(".service", "");
        if (!UNIT_NAME_PATTERN.matcher(cleanName).matches()) { throw new SecurityException("Invalid unit name: " + unitName); }
        if (cleanName.length() > 255) { throw new SecurityException("Unit name too long"); }
    }

    private static void validatePath(String path) throws SecurityException
    {
        if (path == null || path.trim().isEmpty()) { throw new SecurityException("Path cannot be null or empty"); }
        if (PATH_TRAVERSAL_PATTERN.matcher(path).matches()) { throw new SecurityException("Path traversal detected: " + path); }
        try {
            Path normalized = Paths.get(path).normalize();
            if (!normalized.toString().equals(path)) {
                System.err.println("Warning: Path was normalized from " + path + " to " + normalized);
            }
        } catch (Exception e) {
            throw new SecurityException("Invalid path: " + path);
        }
    }

    private static void validateCommand(String command) throws SecurityException
    {
        if (command == null || command.trim().isEmpty()) { throw new SecurityException("Command cannot be null or empty"); }

        if (command.contains(";") || command.contains("|") || command.contains("&") || command.contains("`") || command.contains("$(") || command.contains(">") || command.contains("<"))
        {
            throw new SecurityException("Dangerous characters detected in command");
        }
    }

    public static void start(String unit) throws SecurityException
    {
        validateUnitName(unit);
        exec("sudo", "systemctl", "start", unit);
    }

    public static void stop(String unit) throws SecurityException
    {
        validateUnitName(unit);
        exec("sudo", "systemctl", "stop", unit);
    }

    public static void restart(String unit) throws SecurityException
    {
        validateUnitName(unit);
        exec("sudo", "systemctl", "restart", unit);
    }

    public static void enable(String unit) throws SecurityException
    {
        validateUnitName(unit);
        exec("sudo", "systemctl", "enable", unit);
    }

    public static void disable(String unit) throws SecurityException
    {
        validateUnitName(unit);
        exec("sudo", "systemctl", "disable", unit);
    }

    public static String logs(String unit) throws SecurityException
    {
        validateUnitName(unit);
        String status = exec("sudo", "systemctl", "is-active", unit);
        String logs = exec("sudo", "journalctl", "-u", unit, "-n", "200", "--no-pager");
        return "Status: " + status + "\n" + logs;
    }

    public static void createService(String name, String description, String execStart, String workingDirectory, String user) throws Exception
    {
        validateUnitName(name);
        validateCommand(execStart);
        if (workingDirectory != null) { validatePath(workingDirectory); }
        if (user != null && !user.matches("^[a-z_][a-z0-9_-]*[$]?$")) { throw new SecurityException("Invalid user name: " + user); }
        if (!name.endsWith(".service")) {  name = name + ".service"; }
        String serviceContent = buildServiceContent(name, description, execStart, workingDirectory, user);
        File tempFile = File.createTempFile("service-", ".service");
        tempFile.deleteOnExit();
        try (FileWriter writer = new FileWriter(tempFile)) {  writer.write(serviceContent); }
        exec("sudo", "cp", tempFile.getAbsolutePath(), SYSTEMD_PATH + name);
        exec("sudo", "chmod", "644", SYSTEMD_PATH + name);
        exec("sudo", "systemctl", "daemon-reload");
        exec("sudo", "systemctl", "enable", name);
        Files.deleteIfExists(tempFile.toPath());
    }

    public static void updateService(String name, String description, String execStart, String workingDirectory, String user) throws Exception
    {
        validateUnitName(name);

        if (!name.endsWith(".service")) {  name = name + ".service"; }

        File serviceFile = new File(SYSTEMD_PATH + name);
        if (!serviceFile.exists()) { throw new IllegalStateException("Service does not exist: " + name); }

        String existingContent = readFile(SYSTEMD_PATH + name);

        if (description == null) { description = extractValue(existingContent, "Description"); }
        if (execStart == null) { execStart = extractValue(existingContent, "ExecStart"); }
        if (workingDirectory == null) {  workingDirectory = extractValue(existingContent, "WorkingDirectory"); }
        if (user == null) { user = extractValue(existingContent, "User"); }
        if (execStart != null) { validateCommand(execStart); }
        if (workingDirectory != null && !workingDirectory.isEmpty()) { validatePath(workingDirectory); }
        if (user != null && !user.isEmpty() && !user.matches("^[a-z_][a-z0-9_-]*[$]?$")) { throw new SecurityException("Invalid user name: " + user); }

        try {
            stop(name);
        } catch (Exception e) {
            System.err.println("Warning: Could not stop service before update: " + e.getMessage());
        }

        String serviceContent = buildServiceContent(name, description, execStart, workingDirectory, user);

        File tempFile = File.createTempFile("service-update-", ".service");
        tempFile.deleteOnExit();

        try (FileWriter writer = new FileWriter(tempFile)) { writer.write(serviceContent); }

        exec("sudo", "cp", tempFile.getAbsolutePath(), SYSTEMD_PATH + name);
        exec("sudo", "chmod", "644", SYSTEMD_PATH + name);
        exec("sudo", "systemctl", "daemon-reload");

        Files.deleteIfExists(tempFile.toPath());
    }

    private static String buildServiceContent(String name, String description, String execStart, String workingDirectory, String user)
    {
        StringBuilder content = new StringBuilder();
        content.append("[Unit]\n");
        content.append("Description=").append(description != null ? description : name).append("\n");
        content.append("After=network.target\n\n");

        content.append("[Service]\n");
        content.append("Type=simple\n");
        content.append("ExecStart=").append(execStart).append("\n");

        if (workingDirectory != null && !workingDirectory.isEmpty()) { content.append("WorkingDirectory=").append(workingDirectory).append("\n"); }

        if (user != null && !user.isEmpty()) { content.append("User=").append(user).append("\n"); }

        content.append("Restart=on-failure\n");
        content.append("RestartSec=10s\n");
        content.append("StandardOutput=journal\n");
        content.append("StandardError=journal\n\n");

        content.append("[Install]\n");
        content.append("WantedBy=multi-user.target\n");

        return content.toString();
    }

    private static String extractValue(String content, String key)
    {
        try {
            Pattern pattern = Pattern.compile(key + "=(.+)");
            java.util.regex.Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        } catch (Exception e) {
            System.err.println("Error extracting " + key + ": " + e.getMessage());
        }
        return null;
    }

    public static void deleteService(String name) throws Exception
    {
        validateUnitName(name);

        if (!name.endsWith(".service")) { name = name + ".service"; }

        exec("sudo", "systemctl", "stop", name);
        exec("sudo", "systemctl", "disable", name);
        exec("sudo", "rm", "-f", SYSTEMD_PATH + name);
        exec("sudo", "systemctl", "daemon-reload");
    }

    public static String getStatus(String unit) throws SecurityException
    {
        validateUnitName(unit);
        String status = exec("sudo", "systemctl", "is-active", unit).trim();
        return status;
    }

    public static ServiceStats getStats(String unit)
    {
        try {
            validateUnitName(unit);

            String pid = exec("sudo", "systemctl", "show", unit, "--property=MainPID").trim();
            pid = pid.replace("MainPID=", "");

            if (pid.equals("0") || pid.isEmpty()) { return new ServiceStats(0.0, 0.0, "0 B"); }

            String stats = exec("sudo", "ps", "-p", pid, "-o", "%cpu,%mem,rss", "--no-headers").trim();
            if (stats.isEmpty()) { return new ServiceStats(0.0, 0.0, "0 B"); }

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

        public String toJson() {  return String.format("{\"cpu\":%.2f,\"mem\":%.2f,\"ram\":\"%s\"}", cpu, memPercent, ram); }
    }

    public static String listFiles(String directory) throws Exception
    {
        validatePath(directory);
        String output = exec("sudo", "ls", "-la", directory);
        return output;
    }

    public static String readFile(String filePath) throws Exception
    {
        validatePath(filePath);
        String content = exec("sudo", "cat", filePath);
        return content;
    }

    public static void writeFile(String filePath, String content) throws Exception
    {
        validatePath(filePath);

        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null");
        }

        File tempFile = File.createTempFile("file-edit-", ".tmp");
        tempFile.deleteOnExit();

        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(content);
        }

        exec("sudo", "cp", tempFile.getAbsolutePath(), filePath);
        Files.deleteIfExists(tempFile.toPath());
    }

    public static String[] getDirectoryTree(String directory) throws Exception
    {
        validatePath(directory);
        String output = exec("sudo", "find", directory, "-type", "f", "-not", "-path", "*/.*");
        return output.split("\n");
    }

    public static String exec(String... cmd)
    {
        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(false);

            Process p = pb.start();

            boolean finished = p.waitFor(30, java.util.concurrent.TimeUnit.SECONDS);

            if (!finished) {
                p.destroyForcibly();
                throw new RuntimeException("Command timed out after 30 seconds");
            }

            String error = new String(p.getErrorStream().readAllBytes());
            if (!error.isEmpty()) {
                System.err.println("Error executing " + String.join(" ", cmd) + ": " + error);
            }

            return new String(p.getInputStream().readAllBytes());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to execute command: " + String.join(" ", cmd), e);
        }
    }

    public static void deleteFile(String filePath) throws Exception
    {
        validatePath(filePath);

        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist: " + filePath);
        }

        if (file.isDirectory()) {
            throw new IllegalArgumentException("Cannot delete directories");
        }

        exec("sudo", "rm", "-f", filePath);
    }

    public static void uploadFile(String filePath, String content) throws Exception
    {
        validatePath(filePath);

        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null");
        }

        File file = new File(filePath);
        File parentDir = file.getParentFile();

        if (parentDir != null && !parentDir.exists()) {
            exec("sudo", "mkdir", "-p", parentDir.getAbsolutePath());
        }

        writeFile(filePath, content);
    }

    public static String[] getDirectoryTreeWithFolders(String directory) throws Exception
    {
        validatePath(directory);
        // Get all files and directories, excluding hidden ones
        String output = exec("sudo", "find", directory, "-not", "-path", "*/.*", "-printf", "%y|%p\n");
        return output.split("\n");
    }
}