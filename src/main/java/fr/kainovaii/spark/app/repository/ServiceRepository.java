package fr.kainovaii.spark.app.repository;

import fr.kainovaii.spark.app.models.Service;
import fr.kainovaii.spark.core.database.DB;
import org.javalite.activejdbc.LazyList;

public class ServiceRepository
{
    public boolean create(String name, String unit, String description, boolean autostart) {
        return DB.withConnection(() -> {
            Service service = new Service();
            service.setName(name);
            service.setUnit(unit);
            service.setDescription(description);
            service.setAutostart(autostart);
            return service.saveIt();
        });
    }

    public LazyList<Service> getAll() { return Service.findAll(); }

    public Service findById(int id) { return Service.findById(id); }

    public Service findByUnit(String unit) { return Service.findFirst("unit = ?", unit); }
}
