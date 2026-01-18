package fr.kainovaii.unitpanel.app.controllers;

import fr.kainovaii.core.security.HasRole;
import fr.kainovaii.core.web.methods.GET;
import fr.kainovaii.core.web.methods.POST;
import fr.kainovaii.unitpanel.app.models.Service;
import fr.kainovaii.unitpanel.app.models.User;
import fr.kainovaii.unitpanel.app.repository.ServiceRepository;
import fr.kainovaii.unitpanel.app.repository.UserRepository;
import fr.kainovaii.unitpanel.app.services.SystemdService;
import fr.kainovaii.core.database.DB;
import fr.kainovaii.core.web.controller.BaseController;
import fr.kainovaii.core.web.controller.Controller;
import spark.Request;
import spark.Response;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static spark.Spark.*;

@Controller
public class ServiceController extends BaseController
{
    private final ServiceRepository serviceRepository = new ServiceRepository();
    private final UserRepository userRepository = new UserRepository();
    List<Service> services;

    @HasRole("DEFAULT")
    @GET("/admin/services")
    private Object list(Request req, Response res)
    {
        User user = getLoggedUser(req);

        if (user.getRole().equals("ADMIN")) {
             services = DB.withConnection(() -> serviceRepository.getAll().stream().toList());
        } else {
            services = DB.withConnection(() -> serviceRepository.findByUser(user.getUsername()).stream().toList());
        }
        return render("admin/service/list.html", Map.of("services", services));
    }

    @HasRole("DEFAULT")
    @GET("/admin/services/:id/console")
    private Object console(Request req, Response res)
    {
        User user = getLoggedUser(req);
        int id = Integer.parseInt(req.params("id"));
        Service service = DB.withConnection(() -> serviceRepository.findById(id));
        List<String> users = service.getUsers();
        if (!serviceRepository.userHasService(service, user.getUsername())) res.redirect("/");

        List<User> globalUsers = DB.withConnection(() -> userRepository.getAll().stream().toList());
        List<User> availableUsers = globalUsers.stream().filter(u -> !users.contains(u.getUsername())).toList();

        return render("admin/service/console.html", Map.of("service", service, "users", users, "availableUsers", availableUsers));
    }

    @HasRole("DEFAULT")
    @POST("/admin/services/create")
    private Object create(Request req, Response res) throws Exception
    {
        String name = req.queryParams("name");
        String description = req.queryParams("description");
        String execStart = req.queryParams("execStart");
        String workingDirectory = req.queryParams("workingDirectory");
        String[] users = req.queryParamsValues("users");
        String unit = name;

        if (!name.endsWith(".service")) { unit = name + ".service".toLowerCase(); }

        try {
            SystemdService.createService(name, description, execStart, workingDirectory, "ubuntu");
            serviceRepository.create(name, description, execStart, workingDirectory, unit, Arrays.toString(users), true);

            return redirectWithFlash(req, res, "success", "Service created successfully", "/admin/services");
        } catch (RuntimeException e) {
            return redirectWithFlash(req, res, "error", e.getMessage(), "/admin/services");
        }
    }

    @HasRole("DEFAULT")
    @POST("/admin/services/update")
    private Object update(Request req, Response res) throws Exception
    {
        int id = Integer.parseInt(req.queryParams("id"));
        String name = req.queryParams("name");
        String description = req.queryParams("description");
        String execStart = req.queryParams("execStart");
        String workingDirectory = req.queryParams("workingDirectory");
        String[] users = req.queryParamsValues("users");
        String unit = name;
        if (!name.endsWith(".service")) { unit = name + ".service".toLowerCase(); }

        try {
            SystemdService.updateService(name, description, execStart, workingDirectory, "ubuntu");
            serviceRepository.update(id, name, description, execStart, workingDirectory, unit, Arrays.toString(users),true);
            return redirectWithFlash(req, res, "success", "Updating successfully", "/admin/services/" + id + "/console");
        } catch (RuntimeException e) {
            return redirectWithFlash(req, res, "error", e.getMessage(), "/admin/services");
        }

    }

    @HasRole("DEFAULT")
    @POST("/admin/services/delete")
    private Object delete(Request req, Response res) throws Exception
    {
        int id = Integer.parseInt(req.queryParams("id"));
        String name = req.queryParams("name");

        try {
            SystemdService.deleteService(name);
            DB.withConnection(() -> serviceRepository.deleteById(id));
            return redirectWithFlash(req, res, "success", "Service deleted successfully", "/admin/services");
        } catch (RuntimeException e) {
            return redirectWithFlash(req, res, "error", e.getMessage(), "/admin/services");
        }
    }

    @HasRole("DEFAULT")
    @GET("/admin/services/:id/editor")
    private Object editor(Request req, Response res)
    {
        String id = req.params("id");
        try {
            List<Service> services = DB.withConnection(() -> serviceRepository.getAll().stream() .filter(s -> String.valueOf(s.getId()).equals(id)) .toList() );
            if (services.isEmpty()) { return error(res, "Service not found"); }
            Service service = services.get(0);
            return render("admin/service/editor.html", Map.of("service", service));
        } catch (Exception e) {
            e.printStackTrace();
            return error(res, "Error: " + e.getMessage());
        }
    }
}
