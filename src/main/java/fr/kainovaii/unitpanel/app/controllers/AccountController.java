package fr.kainovaii.unitpanel.app.controllers;

import fr.kainovaii.unitpanel.app.models.ApiToken;
import fr.kainovaii.unitpanel.app.models.User;
import fr.kainovaii.unitpanel.app.repository.ApiTokenRepository;
import fr.kainovaii.unitpanel.app.repository.UserRepository;
import fr.kainovaii.core.database.DB;
import fr.kainovaii.core.web.controller.BaseController;
import fr.kainovaii.core.web.controller.Controller;
import org.javalite.activejdbc.LazyList;
import org.mindrot.jbcrypt.BCrypt;
import spark.Request;
import spark.Response;
import spark.Session;

import java.util.List;
import java.util.Map;

import static spark.Spark.get;
import static spark.Spark.post;

@Controller
public class AccountController extends BaseController
{
    private final UserRepository userRepository;
    private final ApiTokenRepository apiTokenRepository;

    public AccountController()
    {
        initRoutes();
        this.userRepository = new UserRepository();
        this.apiTokenRepository = new ApiTokenRepository();
    }

    private void initRoutes()
    {
        get("/account", this::settings);
        post("/account", this::settings_back);
    }

    private Object settings(Request req, Response res)
    {
        requireLogin(req, res);
        List<ApiToken> apiTokens = DB.withConnection(() -> apiTokenRepository.findByUserId(1L).stream().toList());
        return render("account/settings.html", Map.of("api_tokens", apiTokens));
    }

    private Object settings_back(Request req, Response res)
    {
        Session session = req.session(true);
        String currentUsername = session.attribute("username");

        String newUsername = req.queryParams("username");
        String newPassword = req.queryParams("password");

        User user = DB.withConnection(() -> userRepository.findByUsername(currentUsername));

        final String finalUsername = (newUsername == null || newUsername.isEmpty()) ? user.getUsername() : newUsername;
        final String finalPassword = (newPassword == null || newPassword.isEmpty()) ? user.getPassword() : BCrypt.hashpw(newPassword, BCrypt.gensalt());

        boolean updateUser = DB.withConnection(() -> userRepository.updateByUsername(currentUsername, finalUsername, finalPassword));

        if (updateUser) {
            session.attribute("username", finalUsername);
            setFlash(req, "success", "Update success");
        } else {setFlash(req, "error", "Update error");}

        res.redirect("/account");
        return null;
    }
}
