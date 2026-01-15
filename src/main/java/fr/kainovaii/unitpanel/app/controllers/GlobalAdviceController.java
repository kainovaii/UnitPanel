package fr.kainovaii.unitpanel.app.controllers;

import fr.kainovaii.core.web.controller.BaseController;
import spark.Request;
import spark.Response;
import spark.Session;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static fr.kainovaii.core.web.template.TemplateManager.setGlobal;

public class GlobalAdviceController extends BaseController
{
    public static void applyGlobals(Request req)
    {
        setGlobal("isLogged", isLogged(req));
        if (isLogged(req)) setGlobal("loggedUser", getLoggedUser(req));

        Map<String, String> flashes = collectFlashes(req);
        setGlobal("flashes", flashes);
    }
}