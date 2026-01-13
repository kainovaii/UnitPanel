package fr.kainovaii.core.web;

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
            System.err.println("=== UNHANDLED EXCEPTION ===");
            System.err.println("Request: " + req.requestMethod() + " " + req.pathInfo());
            System.err.println("Error message: " + e.getMessage());
            System.err.println("Stack trace:");
            e.printStackTrace();

            res.status(500);
            res.type("application/json");
            res.body("{\"error\":\"Internal server error: " + e.getMessage() + "\"}");
        });
        try
        {
            before((req, res) -> { GlobalAdviceController.applyGlobals(req); });
            ControllerLoader.loadControllers();
        } catch (RuntimeException exception)
        {
            System.out.println(exception.getMessage());
            exception.printStackTrace();
        }
    }
}