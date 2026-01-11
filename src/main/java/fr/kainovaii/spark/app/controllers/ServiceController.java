package fr.kainovaii.spark.app.controllers;

import fr.kainovaii.spark.app.models.Service;
import fr.kainovaii.spark.app.repository.ServiceRepository;
import fr.kainovaii.spark.app.services.SystemdService;
import fr.kainovaii.spark.core.database.DB;
import fr.kainovaii.spark.core.web.controller.BaseController;
import fr.kainovaii.spark.core.web.controller.Controller;
import org.javalite.activejdbc.LazyList;
import org.javalite.activejdbc.Model;
import spark.Request;
import spark.Response;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static spark.Spark.get;
import static spark.Spark.post;

@Controller
public class ServiceController extends BaseController
{
    private final ServiceRepository serviceRepository;

    public ServiceController()
    {
        initRoutes();
        this.serviceRepository = new ServiceRepository();
    }

    private void initRoutes()
    {
        get("/admin/services", this::list);
        get("/admin/services/:id/console", this::console);
        post("/admin/services/create", this::create);
        get("/admin/services/:id/editor", this::editor);
    }

    private Object list(Request req, Response res)
    {
        requireLogin(req, res);
        List<Service> services = DB.withConnection(() -> serviceRepository.getAll().stream().toList());
        return render("admin/service/list.html", Map.of("services", services));
    }

    private Object console(Request req, Response res)
    {
        requireLogin(req, res);
        int id = Integer.parseInt(req.params("id"));
        Service service = DB.withConnection(() -> serviceRepository.findById(id));
        return render("admin/service/console.html", Map.of("service", service));
    }

    private Object create(Request req, Response res) throws Exception
    {
        requireLogin(req, res);

        String name = req.queryParams("name");
        String description = req.queryParams("description");
        String execStart = req.queryParams("execStart");
        String workingDirectory = req.queryParams("workingDirectory");

        String unit = null;
        if (!name.endsWith(".service")) { unit = name + ".service"; }

        SystemdService.createService(name, description, execStart, workingDirectory, "ubuntu");
        boolean query = serviceRepository.create(name, description, execStart, workingDirectory, unit, true);
        if (!query) redirectWithFlash(req, res, "error", "Creating error", "/admin/services");

        redirectWithFlash(req, res, "success", "Creating success", "/admin/services");
        return true;
    }
    
    private Object editor(Request req, Response res)
    {
        requireLogin(req, res);
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
