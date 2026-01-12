package fr.kainovaii.unitpanel.app.repository;

import fr.kainovaii.unitpanel.app.models.Service;
import fr.kainovaii.core.database.DB;
import org.javalite.activejdbc.LazyList;

public class ServiceRepository
{
    public boolean create(String name, String description, String execStart, String workingDirectory, String unit, boolean autostart)
    {
        return DB.withConnection(() ->
        {
            Service service = new Service();
            service.setName(name);
            service.setDescription(description);
            service.setExecStart(execStart);
            service.setWorkingDirectory(workingDirectory);
            service.setUnit(unit);
            service.setAutostart(autostart);
            return service.saveIt();
        });
    }

    public boolean update(int id, String name, String description, String execStart, String workingDirectory, String unit, boolean autostart)
    {
        return DB.withConnection(() ->
        {
            Service service = this.findById(id);
            if (service == null) return false;
            service.setName(name);
            service.setDescription(description);
            service.setExecStart(execStart);
            service.setWorkingDirectory(workingDirectory);
            service.setUnit(unit);
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

}
