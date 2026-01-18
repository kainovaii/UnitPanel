package fr.kainovaii.unitpanel.app.controllers;

import fr.kainovaii.core.database.DB;
import fr.kainovaii.core.security.HasRole;
import fr.kainovaii.core.web.methods.DELETE;
import fr.kainovaii.core.web.methods.POST;
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
    private final ApiTokenRepository apiTokenRepository = new ApiTokenRepository();
    private final String userAccountUrl = "/users/my-account";

    @HasRole("DEFAULT")
    @POST("/api/auth/token")
    private Object generateToken(Request req, Response res)
    {
        try {
            Long userId = getLoggedUser(req).getLongId();
            DB.withConnection(() -> apiTokenRepository.createToken(userId, 365));
            return redirectWithFlash(req, res, "success", "Successful create token", userAccountUrl);
        } catch (Exception exception) {
            return redirectWithFlash(req, res, "error", exception.getMessage(), userAccountUrl);
        }
    }

    @HasRole("DEFAULT")
    @DELETE("/api/auth/token")
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

    @HasRole("DEFAULT")
    @POST("/api/auth/token/:token/revoke")
    private Object revokeToken(Request req, Response res)
    {
        try {
            String token = req.params(":token");
            DB.withConnection(() -> { apiTokenRepository.revokeToken(token); return null; });
            return redirectWithFlash(req, res, "success", "Successful revoke token", userAccountUrl);
        } catch (Exception exception) {
            return redirectWithFlash(req, res, "error", exception.getMessage(), userAccountUrl);
        }
    }
}
