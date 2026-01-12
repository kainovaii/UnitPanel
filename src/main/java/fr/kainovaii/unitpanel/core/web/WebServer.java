package fr.kainovaii.unitpanel.core.web;

import fr.kainovaii.unitpanel.app.controllers.GlobalAdviceController;
import fr.kainovaii.unitpanel.core.web.controller.ControllerLoader;
import fr.kainovaii.unitpanel.core.Spark;

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

