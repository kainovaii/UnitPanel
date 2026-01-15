package fr.kainovaii.unitpanel.app.controllers;

import fr.kainovaii.core.web.methods.GET;
import fr.kainovaii.core.web.methods.POST;
import fr.kainovaii.unitpanel.app.models.ApiToken;
import fr.kainovaii.unitpanel.app.models.User;
import fr.kainovaii.unitpanel.app.repository.ApiTokenRepository;
import fr.kainovaii.unitpanel.app.repository.UserRepository;
import fr.kainovaii.core.database.DB;
import fr.kainovaii.core.web.controller.BaseController;
import fr.kainovaii.core.web.controller.Controller;
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
        this.userRepository = new UserRepository();
        this.apiTokenRepository = new ApiTokenRepository();
    }

    @GET("/account")
    private Object settings(Request req, Response res)
    {
        requireLogin(req, res);
        List<ApiToken> apiTokens = DB.withConnection(() -> apiTokenRepository.findByUserId(1L).stream().toList());
        return render("account/settings.html", Map.of("api_tokens", apiTokens));
    }

    @POST("/account")
    private Object updateUser(Request req, Response res)
    {
        Session session = req.session(true);
        String newUsername = req.queryParams("username");
        String newPassword = req.queryParams("password");
        String currentUsername = getLoggedUser(req).getUsername();

        User user = DB.withConnection(() -> userRepository.findByUsername((currentUsername)));

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
