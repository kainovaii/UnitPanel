package fr.kainovaii.spark.app.controllers;

import fr.kainovaii.spark.app.services.SystemdService;
import fr.kainovaii.spark.core.web.controller.BaseController;
import fr.kainovaii.spark.core.web.controller.Controller;
import spark.Request;
import spark.Response;

import static spark.Spark.post;
import static spark.Spark.get;

@Controller
public class SystemdController extends BaseController
{
    public SystemdController() { initRoutes(); }

    private void initRoutes()
    {
        post("/api/systemd/:unit/start", this::start);
        post("/api/systemd/:unit/stop", this::stop);
        get("/api/systemd/:unit/logs", this::logs);
        get("/api/systemd/:unit/status", this::status);
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
}
