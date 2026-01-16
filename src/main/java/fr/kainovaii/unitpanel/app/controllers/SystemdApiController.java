package fr.kainovaii.unitpanel.app.controllers;

import fr.kainovaii.core.web.methods.DELETE;
import fr.kainovaii.core.web.methods.GET;
import fr.kainovaii.core.web.methods.POST;
import fr.kainovaii.unitpanel.app.models.Service;
import fr.kainovaii.unitpanel.app.repository.ServiceRepository;
import fr.kainovaii.unitpanel.app.services.SystemdService;
import fr.kainovaii.core.database.DB;
import fr.kainovaii.core.web.controller.BaseController;
import fr.kainovaii.core.web.controller.Controller;
import spark.Request;
import spark.Response;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static spark.Spark.post;
import static spark.Spark.get;

@Controller
public class SystemdApiController extends BaseController
{
    private final ServiceRepository serviceRepository = new ServiceRepository();

    @POST("/api/systemd/:unit/start")
    private Object start(Request req, Response res)
    {
        if (isLogged(req)) {requireLogin(req, res);} else {requireToken(req, res);}
            
        String unit = req.params("unit");
        if (unit == null || unit.isEmpty()) return error(res, "Missing unit parameter");

        SystemdService.start(unit);
        return success(res);
    }

    @POST("/api/systemd/:unit/stop")
    private Object stop(Request req, Response res)
    {
        if (isLogged(req)) {requireLogin(req, res);} else {requireToken(req, res);}
        String unit = req.params("unit");
        if (unit == null || unit.isEmpty()) return error(res, "Missing unit parameter");

        SystemdService.stop(unit);
        return success(res);
    }

    @GET("/api/systemd/:unit/logs")
    private Object logs(Request req, Response res)
    {
        if (isLogged(req)) {requireLogin(req, res);} else {requireToken(req, res);}
        String unit = req.params("unit");
        if (unit == null || unit.isEmpty()) return error(res, "Missing unit parameter");

        res.type("text/plain");
        return SystemdService.logs(unit);
    }

    @GET("/api/systemd/:unit/status")
    private Object status(Request req, Response res)
    {
        if (isLogged(req)) {requireLogin(req, res);} else {requireToken(req, res);}
        String unit = req.params("unit");
        if (unit == null || unit.isEmpty()) return error(res, "Missing unit parameter");

        String status = SystemdService.getStatus(unit);
        res.type("application/json");
        return "{\"status\":\"" + status + "\"}";
    }

    @GET("/api/systemd/:unit/stats")
    private Object stats(Request req, Response res)
    {
        if (isLogged(req)) {requireLogin(req, res);} else {requireToken(req, res);}
        String unit = req.params("unit");
        if (unit == null || unit.isEmpty()) return error(res, "Missing unit parameter");

        try {
            SystemdService.ServiceStats stats = SystemdService.getStats(unit);
            res.type("application/json");
            return stats.toJson();
        } catch (Exception e) {
            res.type("application/json");
            res.status(500);
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    @GET("/api/systemd/stats/total")
    private Object totalStats(Request req, Response res)
    {
        if (isLogged(req)) {requireLogin(req, res);} else {requireToken(req, res);}
        try {
            String unitsParam = req.queryParams("units");
            List<String> units;

            if (unitsParam == null || unitsParam.isEmpty())
            {
                List<Service> services = DB.withConnection(() -> serviceRepository.getAll().stream().toList());

                units = services.stream().map(Service::getUnit).toList();
            } else {
                units = Arrays.asList(unitsParam.split(","));
            }

            SystemdService.ServiceStats stats = SystemdService.getTotalStats(units);

            res.type("application/json");
            return String.format("{\"cpu\":%.2f,\"mem\":%.2f,\"ram\":\"%s\",\"count\":%d}", stats.cpu, stats.memPercent, stats.ram, units.size());

        } catch (Exception e)
        {
            e.printStackTrace();
            res.type("application/json");
            res.status(500);
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    @GET("/api/systemd/:unit/files")
    private Object listFiles(Request req, Response res)
    {
        if (isLogged(req)) {requireLogin(req, res);} else {requireToken(req, res);}
        String unit = req.params("unit");
        if (unit == null || unit.isEmpty()) return error(res, "Missing unit parameter");

        try {
            List<Service> services = DB.withConnection(() -> serviceRepository.getAll().stream()
                    .filter(s -> s.getUnit().equals(unit))
                    .toList()
            );

            if (services.isEmpty()) { return error(res, "Service not found"); }

            Service service = services.get(0);
            String workDir = service.getWorkingDirectory();

            if (workDir == null || workDir.isEmpty()) {
                return error(res, "No working directory configured");
            }

            String[] items = SystemdService.getDirectoryTreeWithFolders(workDir);

            // Parse the output into a structured format
            List<Map<String, Object>> tree = new java.util.ArrayList<>();
            for (String item : items) {
                if (item.trim().isEmpty()) continue;
                String[] parts = item.split("\\|", 2);
                if (parts.length != 2) continue;

                String type = parts[0].equals("f") ? "file" : "directory";
                String path = parts[1];

                Map<String, Object> node = new java.util.HashMap<>();
                node.put("path", path);
                node.put("type", type);
                node.put("name", new File(path).getName());
                tree.add(node);
            }

            res.type("application/json");
            return new com.google.gson.Gson().toJson(Map.of(
                    "tree", tree,
                    "baseDir", workDir
            ));
        } catch (Exception e) {
            res.status(500);
            return error(res, "Failed to list files: " + e.getMessage());
        }
    }

    @DELETE("/api/systemd/:unit/file")
    private Object deleteFile(Request req, Response res)
    {
        if (isLogged(req)) {requireLogin(req, res);} else {requireToken(req, res);}
        String filePath = req.queryParams("path");
        if (filePath == null || filePath.isEmpty()) return error(res, "Missing file path");

        try {
            SystemdService.deleteFile(filePath);
            res.type("application/json");
            return "{\"success\":true,\"message\":\"File deleted successfully\"}";
        } catch (Exception e) {
            res.status(500);
            return "{\"success\":false,\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    @GET("/api/systemd/:unit/file")
    private Object getFile(Request req, Response res)
    {
        if (isLogged(req)) {requireLogin(req, res);} else {requireToken(req, res);}
        String filePath = req.queryParams("path");
        if (filePath == null || filePath.isEmpty()) return error(res, "Missing file path");

        try {
            String content = SystemdService.readFile(filePath);
            res.type("text/plain");
            return content;
        } catch (Exception e) {
            res.status(500);
            return error(res, "Failed to read file: " + e.getMessage());
        }
    }

    @POST("/api/systemd/:unit/file")
    private Object saveFile(Request req, Response res)
    {
        if (isLogged(req)) {requireLogin(req, res);} else {requireToken(req, res);}
        String filePath = req.queryParams("path");
        if (filePath == null || filePath.isEmpty()) return error(res, "Missing file path");

        String content = req.body();

        try {
            SystemdService.writeFile(filePath, content);

            res.type("application/json");
            return "{\"success\":true,\"message\":\"File saved successfully\"}";
        } catch (Exception e) {
            res.type("application/json");
            res.status(500);
            return "{\"success\":false,\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    @GET("/api/systemd/:unit/service-file")
    private Object getServiceFile(Request req, Response res)
    {
        if (isLogged(req)) {requireLogin(req, res);} else {requireToken(req, res);}
        String unit = req.params("unit");
        if (unit == null || unit.isEmpty()) return error(res, "Missing unit parameter");

        if (!unit.endsWith(".service")) {
            unit = unit + ".service";
        }

        try {
            String content = SystemdService.readFile("/etc/systemd/system/" + unit);
            res.type("text/plain");
            return content;
        } catch (Exception e) {
            res.status(500);
            return error(res, "Failed to read service file: " + e.getMessage());
        }
    }

    @POST("/api/systemd/:unit/service-file")
    private Object updateServiceFile(Request req, Response res)
    {
        
        String unit = req.params("unit");
        if (unit == null || unit.isEmpty()) return error(res, "Missing unit parameter");

        if (!unit.endsWith(".service")) {
            unit = unit + ".service";
        }

        String content = req.body();

        try {
            SystemdService.writeFile("/etc/systemd/system/" + unit, content);

            SystemdService.exec("sudo", "systemctl", "daemon-reload");

            res.type("application/json");
            return "{\"success\":true,\"message\":\"Service file updated successfully\"}";
        } catch (Exception e) {
            res.status(500);
            return error(res, "Failed to update service file: " + e.getMessage());
        }
    }

    @POST("/api/systemd/:unit/upload")
    private Object uploadFile(Request req, Response res)
    {
        if (isLogged(req)) {requireLogin(req, res);} else {requireToken(req, res);}

        try {
            com.google.gson.JsonObject json = new com.google.gson.JsonParser()
                    .parse(req.body())
                    .getAsJsonObject();

            String filePath = json.get("path").getAsString();
            String content = json.get("content").getAsString();

            if (filePath == null || filePath.isEmpty()) {
                return error(res, "Missing file path");
            }

            SystemdService.uploadFile(filePath, content);

            res.type("application/json");
            return "{\"success\":true,\"message\":\"File uploaded successfully\"}";
        } catch (Exception e) {
            res.status(500);
            return "{\"success\":false,\"error\":\"" + e.getMessage() + "\"}";
        }
    }
}