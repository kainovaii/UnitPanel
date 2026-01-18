package fr.kainovaii.unitpanel.app.repository;

import fr.kainovaii.unitpanel.app.models.Service;
import fr.kainovaii.core.database.DB;
import org.javalite.activejdbc.LazyList;
import org.javalite.activejdbc.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ServiceRepository
{
    public boolean create(String name, String description, String execStart, String workingDirectory, String unit, String user, boolean autostart)
    {
        return DB.withConnection(() ->
        {
            Service service = new Service();
            service.setName(name);
            service.setDescription(description);
            service.setExecStart(execStart);
            service.setWorkingDirectory(workingDirectory);
            service.setUnit(unit);
            service.setUsers(user);
            service.setAutostart(autostart);
            return service.saveIt();
        });
    }

    public boolean update(int id, String name, String description, String execStart, String workingDirectory, String unit, String user, boolean autostart)
    {
        return DB.withConnection(() ->
        {
            Service service = this.findById(id);
            service.setName(name);
            service.setDescription(description);
            service.setExecStart(execStart);
            service.setWorkingDirectory(workingDirectory);
            service.setUnit(unit);
            service.setUsers(user);
            service.setAutostart(autostart);
            return service.saveIt();
        });
    }

    public LazyList<Service> getAll() { return Service.findAll(); }

    public Service findById(int id) { return Service.findById(id); }

    public Service findByUnit(String unit) { return Service.findFirst("unit = ?", unit); }

    public boolean deleteById(int id)
    {
        Service service = this.findById(id);
        if (service == null) return false;
        return service.delete();
    }

    public boolean userHasService(Service service, String username) {
        return service.getUsers().stream().anyMatch(u -> u.equalsIgnoreCase(username));
    }

    public List<Service> findByUser(String username) {
        List<Service> allServices = Service.findAll();
        return allServices.stream().filter(s -> userHasService(s, username)).collect(Collectors.toList());
    }

}
