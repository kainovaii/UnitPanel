package fr.kainovaii.unitpanel.app.controllers;

import fr.kainovaii.core.database.DB;
import fr.kainovaii.core.web.controller.BaseController;
import fr.kainovaii.core.web.controller.Controller;
import fr.kainovaii.core.web.methods.GET;
import fr.kainovaii.unitpanel.app.models.User;
import fr.kainovaii.unitpanel.app.repository.UserRepository;
import spark.Request;
import spark.Response;

import java.util.List;
import java.util.Map;

import static spark.Spark.get;

@Controller
public class UserController extends BaseController
{
    private final UserRepository userRepository;

    public UserController() { this.userRepository = new UserRepository(); }

    @GET("/admin/users")
    private Object list(Request req, Response res)
    {
        requireLogin(req, res);
        List<User> users = DB.withConnection(() -> userRepository.getAll().stream().toList());
        return render("admin/users/list.html", Map.of("users", users));
    }
}
