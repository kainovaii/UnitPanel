package fr.kainovaii.spark.app.models;

import org.javalite.activejdbc.Model;

public class Service extends Model
{
    public String getName() { return (String) get("name"); }
    public String getUnit() { return (String) get("unit"); }
    public String getDescription() { return (String) get("description"); }
    public Boolean getAutostart() { return get("autostart") != null && (Boolean) get("autostart"); }

    public void setName(String name) { set("name", name); }
    public void setUnit(String unit) { set("unit", unit); }
    public void setDescription(String description) { set("description", description); }
    public void setAutostart(Boolean autostart) { set("autostart", autostart); }
}
