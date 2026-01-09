package fr.kainovaii.spark.app.controllers;

import fr.kainovaii.spark.core.web.controller.BaseController;
import fr.kainovaii.spark.core.web.controller.Controller;
import spark.Request;
import spark.Response;

import java.util.Map;

import static spark.Spark.get;

@Controller
public class ServiceController extends BaseController
{
    public ServiceController() { initRoutes(); }

    private void initRoutes()
    {
        get("/services/console", this::console);
    }

    private Object console(Request req, Response res)
    {
        requireLogin(req, res);
        return render("admin/console.html", Map.of("title", "Bot"));
    }
}
