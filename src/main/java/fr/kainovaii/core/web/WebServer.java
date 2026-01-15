package fr.kainovaii.core.web;

import fr.kainovaii.core.security.RoleChecker;
import fr.kainovaii.unitpanel.app.controllers.GlobalAdviceController;
import fr.kainovaii.core.web.controller.ControllerLoader;
import fr.kainovaii.core.Spark;
import static spark.Spark.*;


public class WebServer
{
    public void start()
    {
        ipAddress("0.0.0.0");
        port(Spark.getWebPort());
        staticFiles.location("/");

        exception(Exception.class, (e, req, res) ->
        {
            res.status(500);
            res.type("application/json");
            res.body("{\"error\":\"Internal server error: " + e.getMessage() + "\"}");
        });
        try {
            spark.Spark.before((req, res) -> {
                GlobalAdviceController.applyGlobals(req);
                RoleChecker.checkAccess(req, res);
            });
            ControllerLoader.loadControllers();
        } catch (RuntimeException exception)
        {
            System.out.println(exception.getMessage());
            exception.printStackTrace();
        }
    }
}