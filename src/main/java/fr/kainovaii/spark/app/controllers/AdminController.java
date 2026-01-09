package fr.kainovaii.spark.app.controllers;

import fr.kainovaii.spark.core.web.controller.BaseController;
import fr.kainovaii.spark.core.web.controller.Controller;
import spark.Request;
import spark.Response;

import java.util.Map;

import static spark.Spark.get;

@Controller
public class AdminController extends BaseController
{
    public AdminController() { initRoutes(); }

    private void initRoutes()
    {
        get("/admin", this::list);
    }

    private Object list(Request req, Response res)
    {
        requireLogin(req, res);
        return render("admin/dashboard.html", Map.of());
    }
}
