package fr.kainovaii.unitpanel.app.controllers;

import fr.kainovaii.core.web.methods.GET;
import fr.kainovaii.core.web.methods.POST;
import fr.kainovaii.unitpanel.app.models.User;
import fr.kainovaii.unitpanel.app.repository.UserRepository;
import fr.kainovaii.core.database.DB;
import fr.kainovaii.core.web.controller.BaseController;
import fr.kainovaii.core.web.controller.Controller;
import org.mindrot.jbcrypt.BCrypt;
import spark.Request;
import spark.Response;
import spark.Session;

import java.util.Map;

import static spark.Spark.get;
import static spark.Spark.post;

@Controller
public class LoginController extends BaseController
{
    private final UserRepository userRepository;

    public LoginController() { this.userRepository = new UserRepository(); }

    @GET("login")
    private Object front(Request req, Response res)
    {
        if (!isLogged(req)) {
            return render("account/login.html", Map.of());
        } else {
            redirectWithFlash(req,  res, "error", "Your are already logged in", "/");
        }
        return null;
    }

    @POST("login")
    private Object back(Request req, Response res)
    {
        String usernameParam = req.queryParams("username");
        String passwordParam = req.queryParams("password");
        Session session = req.session(true);

        return DB.withConnection(() ->
        {
            if (!UserRepository.userExist(usernameParam)) redirectWithFlash(req,  res, "error", "User not found", "/login");

            User user = userRepository.findByUsername(usernameParam);

            if (BCrypt.checkpw(passwordParam, user.getPassword()))
            {
                session.attribute("logged", true);
                session.attribute("id", user.getId());
                res.redirect("/admin/services");
                return true;
            }
            return redirectWithFlash(req,  res, "error", "Incorect login", "/login");
        });
    }

    @GET("logout")
    private Object logout(Request req, Response res)
    {
        Session session = req.session(true);
        if (isLogged(req)) { session.invalidate(); }
        return redirectWithFlash(req,  res, "success", "Success logout", "/login");
    }
}
