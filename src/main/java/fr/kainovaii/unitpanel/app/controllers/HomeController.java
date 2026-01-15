package fr.kainovaii.unitpanel.app.controllers;

import fr.kainovaii.core.security.HasRole;
import fr.kainovaii.core.web.methods.GET;
import fr.kainovaii.core.web.controller.BaseController;
import fr.kainovaii.core.web.controller.Controller;
import spark.Request;
import spark.Response;

@Controller
public class HomeController extends BaseController
{
    @GET("/")
    private Object homepage(Request req, Response res)
    {
        res.redirect("/admin/services");
        return true;
    }

    @GET("/admin")
    private Object root(Request req, Response res)
    {
        res.redirect("/admin/services");
        return true;
    }

}
