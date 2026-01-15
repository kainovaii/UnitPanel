package fr.kainovaii.unitpanel.app.controllers;

import fr.kainovaii.core.web.controller.BaseController;
import fr.kainovaii.core.web.controller.Controller;
import fr.kainovaii.core.web.methods.GET;
import spark.Request;
import spark.Response;

import java.util.Map;

import static spark.Spark.get;

@Controller
public class DocApiController extends BaseController
{
    @GET("/admin/api")
    private Object home(Request req, Response res)
    {
        requireLogin(req, res);
        return render("admin/api/home.html", Map.of());
    }
}
