package fr.kainovaii.unitpanel.app.controllers;

import fr.kainovaii.unitpanel.app.models.Service;
import fr.kainovaii.unitpanel.app.repository.ServiceRepository;
import fr.kainovaii.unitpanel.app.services.SystemdService;
import fr.kainovaii.core.database.DB;
import fr.kainovaii.core.web.controller.BaseController;
import fr.kainovaii.core.web.controller.Controller;
import spark.Request;
import spark.Response;

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
        get("/admin/services/:id/editor", this::editor);
        post("/admin/services/create", this::create);
        post("/admin/services/update", this::update);
        post("/admin/services/delete", this::delete);
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
        try {
            String name = req.queryParams("name").toLowerCase();
            String description = req.queryParams("description");
            String execStart = req.queryParams("execStart");
            String workingDirectory = req.queryParams("workingDirectory");

            String unit = name;
            if (!name.endsWith(".service")) { unit = name + ".service"; }

            SystemdService.createService(name, description, execStart, workingDirectory, "ubuntu");
            serviceRepository.create(name, description, execStart, workingDirectory, unit, true);

            return redirectWithFlash(req, res, "success", "Service created successfully", "/admin/services");
        } catch (RuntimeException e) {
            return redirectWithFlash(req, res, "error", e.getMessage(), "/admin/services");
        }
    }

    private Object update(Request req, Response res) throws Exception
    {
        requireLogin(req, res);
        try {
            int id = Integer.parseInt(req.queryParams("id"));
            String name = req.queryParams("name").toLowerCase();
            String description = req.queryParams("description");
            String execStart = req.queryParams("execStart");
            String workingDirectory = req.queryParams("workingDirectory");

            String unit = name;
            if (!name.endsWith(".service")) { unit = name + ".service"; }
            serviceRepository.update(id, name, description, execStart, workingDirectory, unit, true);

            return redirectWithFlash(req, res, "success", "Updating successfully", "/admin/services/" + id + "/console");
        } catch (RuntimeException e) {
            return redirectWithFlash(req, res, "error", e.getMessage(), "/admin/services");
        }
    }

    private Object delete(Request req, Response res) throws Exception
    {
        requireLogin(req, res);
        try {

            int id = Integer.parseInt(req.queryParams("id"));
            String name = req.queryParams("name");

            SystemdService.deleteService(name);
            DB.withConnection(() -> serviceRepository.deleteById(id));

            return redirectWithFlash(req, res, "success", "Service deleted successfully", "/admin/services");
        } catch (RuntimeException e) {
            return redirectWithFlash(req, res, "error", e.getMessage(), "/admin/services");
        }
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
