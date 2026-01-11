package fr.kainovaii.spark.app.controllers;

import fr.kainovaii.spark.app.models.Service;
import fr.kainovaii.spark.app.repository.ServiceRepository;
import fr.kainovaii.spark.app.services.SystemdService;
import fr.kainovaii.spark.core.database.DB;
import fr.kainovaii.spark.core.web.controller.BaseController;
import fr.kainovaii.spark.core.web.controller.Controller;
import spark.Request;
import spark.Response;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static spark.Spark.post;
import static spark.Spark.get;

@Controller
public class SystemdController extends BaseController
{
    private final ServiceRepository serviceRepository;

    public SystemdController()
    {
        initRoutes();
        this.serviceRepository = new ServiceRepository();
    }

    private void initRoutes()
    {
        post("/api/systemd/:unit/start", this::start);
        post("/api/systemd/:unit/stop", this::stop);
        get("/api/systemd/:unit/logs", this::logs);
        get("/api/systemd/:unit/status", this::status);
        get("/api/systemd/:unit/stats", this::stats);
        get("/api/systemd/stats/total", this::totalStats);
        get("/api/systemd/:unit/files", this::listFiles);
        get("/api/systemd/:unit/file", this::getFile);
        post("/api/systemd/:unit/file", this::saveFile);
    }

    private Object start(Request req, Response res)
    {
        String unit = req.params("unit");
        if (unit == null || unit.isEmpty()) return error(res, "Missing unit parameter");

        SystemdService.start(unit);
        return success(res);
    }

    private Object stop(Request req, Response res)
    {
        String unit = req.params("unit");
        if (unit == null || unit.isEmpty()) return error(res, "Missing unit parameter");

        SystemdService.stop(unit);
        return success(res);
    }

    private Object logs(Request req, Response res)
    {
        String unit = req.params("unit");
        if (unit == null || unit.isEmpty()) return error(res, "Missing unit parameter");

        res.type("text/plain");
        return SystemdService.logs(unit);
    }

    private Object status(Request req, Response res)
    {
        String unit = req.params("unit");
        if (unit == null || unit.isEmpty()) return error(res, "Missing unit parameter");

        String status = SystemdService.getStatus(unit);
        res.type("application/json");
        return "{\"status\":\"" + status + "\"}";
    }

    private Object stats(Request req, Response res)
    {
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

    private Object totalStats(Request req, Response res)
    {
        try {
            String unitsParam = req.queryParams("units");
            List<String> units;

            if (unitsParam == null || unitsParam.isEmpty()) {
                List<Service> services = DB.withConnection(() ->
                        serviceRepository.getAll().stream().toList()
                );

                units = services.stream()
                        .map(Service::getUnit)
                        .toList();
            } else {
                units = Arrays.asList(unitsParam.split(","));
            }

            SystemdService.ServiceStats stats = SystemdService.getTotalStats(units);

            res.type("application/json");
            // ← AJOUTE LE COUNT ICI
            return String.format("{\"cpu\":%.2f,\"mem\":%.2f,\"ram\":\"%s\",\"count\":%d}",
                    stats.cpu, stats.memPercent, stats.ram, units.size());

        } catch (Exception e) {
            e.printStackTrace();
            res.type("application/json");
            res.status(500);
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    private Object listFiles(Request req, Response res)
    {
        String unit = req.params("unit");
        if (unit == null || unit.isEmpty()) return error(res, "Missing unit parameter");

        try {
            // Récupérer le service pour avoir le workingDirectory
            List<Service> services = DB.withConnection(() ->
                    serviceRepository.getAll().stream()
                            .filter(s -> s.getUnit().equals(unit))
                            .toList()
            );

            if (services.isEmpty()) {
                return error(res, "Service not found");
            }

            Service service = services.get(0);
            String workDir = service.getWorkingDirectory();

            if (workDir == null || workDir.isEmpty()) {
                return error(res, "No working directory configured");
            }

            String[] files = SystemdService.getDirectoryTree(workDir);

            res.type("application/json");
            return new com.google.gson.Gson().toJson(Map.of("files", files, "baseDir", workDir));
        } catch (Exception e) {
            res.status(500);
            return error(res, "Failed to list files: " + e.getMessage());
        }
    }

    private Object getFile(Request req, Response res)
    {
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

    private Object saveFile(Request req, Response res)
    {
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
}
