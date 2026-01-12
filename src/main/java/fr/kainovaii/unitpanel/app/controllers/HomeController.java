package fr.kainovaii.unitpanel.app.controllers;

import fr.kainovaii.core.web.controller.BaseController;
import fr.kainovaii.core.web.controller.Controller;
import spark.Request;
import spark.Response;

import static spark.Spark.get;
import static spark.Spark.post;

@Controller
public class HomeController extends BaseController
{
    public HomeController() { initRoutes(); }

    private void initRoutes()
    {
        get("/", this::homepage);
        get("/admin", this::root);
    }

    private Object homepage(Request req, Response res)
    {
        res.redirect("/admin/services");
        return true;
    }
    private Object root(Request req, Response res)
    {
        res.redirect("/admin/services");
        return true;
    }

}
