package fr.kainovaii.spark.app.controllers;

import fr.kainovaii.spark.core.web.controller.BaseController;
import fr.kainovaii.spark.core.web.controller.Controller;
import spark.Request;
import spark.Response;

import java.util.Map;

import static spark.Spark.get;
import static spark.Spark.post;

@Controller
public class HomeController extends BaseController
{
    public HomeController() { initRoutes(); }

    private void initRoutes()
    {
        get("/", this::homepage);
    }

    private Object homepage(Request req, Response res)
    {
        res.redirect("/admin");
        return true;
    }
}
