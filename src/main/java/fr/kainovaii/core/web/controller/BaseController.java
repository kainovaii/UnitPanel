package fr.kainovaii.core.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.kainovaii.core.database.DB;
import fr.kainovaii.core.security.RoleChecker;
import fr.kainovaii.core.web.ApiResponse;
import fr.kainovaii.core.web.template.TemplateManager;
import fr.kainovaii.unitpanel.app.models.ApiToken;
import fr.kainovaii.unitpanel.app.models.User;
import fr.kainovaii.unitpanel.app.repository.ApiTokenRepository;
import spark.*;

import java.lang.reflect.Method;
import java.util.*;

import static spark.Spark.halt;

public class BaseController extends ApiResponse
{
    private static final ObjectMapper mapper = new ObjectMapper();
    private final Queue<String> methodQueue = new LinkedList<>();

    protected static boolean isLogged(Request req)
    {
        Session session = req.session(false);
        if (session == null) return false;

        Boolean logged = session.attribute("logged");
        return Boolean.TRUE.equals(logged);
    }

    protected static User getLoggedUser(Request req)
    {
        Session session = req.session(false);
        return DB.withConnection(() -> User.findById(session.attribute("id")));
    }

    protected static void requireLogin(Request req, Response res)
    {
        if (!isLogged(req)) {
            res.redirect("/login");
            halt();
        }
    }

    protected void requireToken(Request req, Response res)
    {
        ApiTokenRepository apiTokenRepository = new ApiTokenRepository();
        String token = extractToken(req);

        if (token == null || token.isEmpty()) {
            res.status(401);
            res.type("application/json");
            halt(401, "{\"error\":\"Missing authentication token\"}");
        }

        try {
            ApiToken apiToken = DB.withConnection(() -> apiTokenRepository.findByToken(token));

            if (apiToken == null) {
                res.status(401);
                res.type("application/json");
                halt(401, "{\"error\":\"Invalid token\"}");
            }

            /*
            if (apiToken.isExpired()) {
                res.status(401);
                res.type("application/json");
                halt(401, "{\"error\":\"Token expired\"}");
            }
             */

            req.attribute("userId", apiToken.getUserId());
            req.attribute("token", apiToken.getToken());

        } catch (spark.HaltException e) {
            throw e;
        } catch (Exception e) {
            res.status(500);
            res.type("application/json");
            halt(500, "{\"error\":\"Authentication error: " + e.getMessage() + "\"}");
        }
    }

    private String extractToken(Request req)
    {
        String authHeader = req.headers("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    protected static void setFlash(Request req, String key, String message)
    {
        Session session = req.session();
        session.attribute("flash_" + key, message);
    }

    public static Map<String, String> collectFlashes(Request req)
    {
        Session session = req.session(false);
        if (session == null) return Map.of();

        Map<String, String> flashes = new HashMap<>();
        for (String attr : session.attributes()) {
            if (attr.startsWith("flash_")) {
                String key = attr.substring(6);
                flashes.put(key, session.attribute(attr));
                session.removeAttribute(attr);
            }
        }
        return flashes;
    }

    protected static Object redirectWithFlash(Request req, Response res, String type, String message, String location)
    {
        setFlash(req, type, message);
        res.redirect(location);
        halt();
        return null;
    }

    protected String render(String template, Map<String, Object> model) {
        try {
            Map<String, Object> merged = new HashMap<>(TemplateManager.getGlobals());
            if (model != null) merged.putAll(model);

            return TemplateManager.get().render(template, merged);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}