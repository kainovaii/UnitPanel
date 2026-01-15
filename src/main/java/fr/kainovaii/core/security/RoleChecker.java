package fr.kainovaii.core.security;

import fr.kainovaii.core.web.controller.BaseController;
import spark.Request;
import spark.Response;

import java.util.HashMap;
import java.util.Map;


public class RoleChecker extends BaseController
{
    private static final Map<String, String> pathToRole = new HashMap<>();

    public static void registerPathWithRole(String path, String role) { pathToRole.put(path, role); }

    public static void checkAccess(Request req, Response res)
    {
        String path = req.pathInfo();
        String requiredRole = pathToRole.get(path);

        if (requiredRole == null) { return; }
        if (!isLogged(req)) res.redirect("/login");

        String userRole = getLoggedUser(req).getRole();

        if (userRole == null || !userRole.equals(requiredRole)) redirectWithFlash(req,  res, "error", "Access denied - Role required : " + requiredRole, "/admin/services");
    }
}