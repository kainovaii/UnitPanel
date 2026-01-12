package fr.kainovaii.unitpanel.app.controllers;

import fr.kainovaii.unitpanel.core.web.controller.BaseController;
import fr.kainovaii.unitpanel.core.web.controller.Controller;
import spark.Request;
import spark.Response;

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
        res.redirect("/admin/services");
        return true;  // render("admin/dashboard.html", Map.of());
    }
}
