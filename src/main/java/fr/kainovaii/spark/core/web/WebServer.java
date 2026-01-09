package fr.kainovaii.spark.core.web;

import fr.kainovaii.spark.app.controllers.GlobalAdviceController;
import fr.kainovaii.spark.core.web.controller.ControllerLoader;
import fr.kainovaii.spark.core.Spark;

import static spark.Spark.*;


public class WebServer
{
    public void start()
    {
        ipAddress("0.0.0.0");
        port(Spark.getWebPort());
        staticFiles.location("/");
        before((req, res) -> { GlobalAdviceController.applyGlobals(req); });
        ControllerLoader.loadControllers();
    }
}

