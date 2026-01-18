package fr.kainovaii.unitpanel.app.controllers;

import fr.kainovaii.core.database.DB;
import fr.kainovaii.core.web.controller.BaseController;
import fr.kainovaii.unitpanel.app.models.User;
import fr.kainovaii.unitpanel.app.repository.UserRepository;
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
        UserRepository userRepository = new UserRepository();

        setGlobal("isLogged", isLogged(req));
        if (isLogged(req)) setGlobal("loggedUser", getLoggedUser(req));

        List<User> users = DB.withConnection(() -> userRepository.getAll().stream().toList());

        Map<String, String> flashes = collectFlashes(req);
        setGlobal("flashes", flashes);
        setGlobal("globalUsers", users);
    }
}