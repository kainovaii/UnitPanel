package fr.kainovaii.spark.app.models;

import org.javalite.activejdbc.Model;

public class Setting extends Model
{
    public boolean getAdminExist() {
        return getBoolean("admin_exist");
    }

    public void setAdminExist(String admin_exist) {
        set("admin_exist", admin_exist);
    }
}
