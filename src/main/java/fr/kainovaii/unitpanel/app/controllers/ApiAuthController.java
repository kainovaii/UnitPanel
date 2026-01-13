package fr.kainovaii.unitpanel.app.controllers;

import fr.kainovaii.core.database.DB;
import fr.kainovaii.core.web.controller.BaseController;
import fr.kainovaii.core.web.controller.Controller;
import fr.kainovaii.unitpanel.app.models.ApiToken;
import fr.kainovaii.unitpanel.app.repository.ApiTokenRepository;
import spark.*;

import java.util.Map;

import static spark.Spark.*;

@Controller
public class ApiAuthController extends BaseController
{
    private final ApiTokenRepository apiTokenRepository;

    public ApiAuthController()
    {
        this.apiTokenRepository = new ApiTokenRepository();
        initRoutes();
    }

    private void initRoutes()
    {
        post("/api/auth/token", this::generateToken);
        delete("/api/auth/token", this::removeToken);
        post("/api/auth/token/:token/revoke", this::revokeToken);
    }

    private Object generateToken(Request req, Response res)
    {
        requireLogin(req, res);

        try {
            Long userId = getLoggedUser(req).getLongId();

            ApiToken token = DB.withConnection(() -> apiTokenRepository.createToken(userId, 365));

            res.type("application/json");
            res.redirect("/account");
            return new com.google.gson.Gson().toJson(Map.of(
                    "success", true,
                    "token", token.getToken(),
                    "expiresAt", token.getExpiresAt().toString()
            ));
        } catch (Exception e) {
            res.status(500);
            return error(res, "Failed to generate token: " + e.getMessage());
        }
    }

    private Object removeToken(Request req, Response res)
    {
        try {
            String token = req.attribute("token");
            DB.withConnection(() -> {
                apiTokenRepository.revokeToken(token);
                return null;
            });

            res.type("application/json");
            return "{\"success\":true,\"message\":\"Token revoked successfully\"}";
        } catch (Exception e) {
            res.status(500);
            return error(res, "Failed to revoke token: " + e.getMessage());
        }
    }

    private Object revokeToken(Request req, Response res)
    {
        try {
            String token = req.params(":token");

            DB.withConnection(() -> {
                apiTokenRepository.revokeToken(token);
                return null;
            });

            res.redirect("/account");
            return null;
        } catch (Exception e) {
            res.status(500);
            return error(res, "Failed to revoke token: " + e.getMessage());
        }
    }
}
