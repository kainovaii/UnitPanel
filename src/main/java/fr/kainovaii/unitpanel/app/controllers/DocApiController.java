package fr.kainovaii.unitpanel.app.controllers;

import fr.kainovaii.unitpanel.core.web.controller.BaseController;
import fr.kainovaii.unitpanel.core.web.controller.Controller;
import spark.Request;
import spark.Response;

import java.util.Map;

import static spark.Spark.get;

@Controller
public class DocApiController extends BaseController
{
    public DocApiController() { initRoutes(); }

    private void initRoutes()
    {
        get("/admin/api", this::home);
    }

    private Object home(Request req, Response res)
    {
        requireLogin(req, res);
        return render("admin/api/home.html", Map.of());
    }
}
