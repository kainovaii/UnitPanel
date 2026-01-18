package fr.kainovaii.unitpanel.app.controllers;

import fr.kainovaii.core.database.DB;
import fr.kainovaii.core.security.HasRole;
import fr.kainovaii.core.web.controller.BaseController;
import fr.kainovaii.core.web.controller.Controller;
import fr.kainovaii.core.web.methods.GET;
import fr.kainovaii.core.web.methods.POST;
import fr.kainovaii.unitpanel.app.models.User;
import fr.kainovaii.unitpanel.app.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import spark.Request;
import spark.Response;

import java.util.List;
import java.util.Map;

import static spark.Spark.get;

@Controller
public class UserController extends BaseController
{
    private final UserRepository userRepository = new UserRepository();

    @HasRole("ADMIN")
    @GET("/admin/users")
    private Object list(Request req, Response res)
    {
        List<User> users = DB.withConnection(() -> userRepository.getAll().stream().toList());
        return render("admin/users/list.html", Map.of("users", users));
    }

    @HasRole("ADMIN")
    @POST("/admin/users/create")
    private Object create(Request req, Response res)
    {
        String username = req.queryParams("username");
        String email = req.queryParams("email");
        String role = req.queryParams("role");
        String password = BCrypt.hashpw(req.queryParams("password"), BCrypt.gensalt(12));

        try {
            userRepository.create(username, email, password, role);
        } catch (RuntimeException exception) {
            return redirectWithFlash(req, res, "error", exception.getMessage(), "/admin/users");
        }
        return redirectWithFlash(req, res, "success", "Creating users successful", "/admin/users");
    }

    @HasRole("ADMIN")
    @POST("/admin/users/update")
    private Object update(Request req, Response res)
    {
        int id = Integer.parseInt(req.queryParams("id"));
        String username = req.queryParams("username");
        String role = req.queryParams("role");

        try {
            userRepository.updateById(id, username, role);
        } catch (RuntimeException exception) {
            return redirectWithFlash(req, res, "error", exception.getMessage(), "/admin/users");
        }
        return redirectWithFlash(req, res, "success", "Updating users successful", "/admin/users");
    }

    @HasRole("ADMIN")
    @POST("/admin/users/delete")
    private Object delete(Request req, Response res)
    {
        int id = Integer.parseInt(req.queryParams("id"));
        try {
           DB.withConnection(() -> userRepository.deleteByID(id));
        } catch (RuntimeException exception) {
            return redirectWithFlash(req, res, "error", exception.getMessage(), "/admin/users");
        }
        return redirectWithFlash(req, res, "success", "Deleting users successful", "/admin/users");
    }
}
