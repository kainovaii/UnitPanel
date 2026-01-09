package fr.kainovaii.spark.app.repository;

import fr.kainovaii.spark.app.models.Setting;
import fr.kainovaii.spark.core.database.DB;
import org.javalite.activejdbc.LazyList;

public class SettingRepository
{
    public boolean create(boolean admin_exist)
    {
        return DB.withConnection(() -> {
            Setting setting = new Setting();
            setting.set("admin_exist", admin_exist);
            return setting.saveIt();
        });
    }

    public LazyList<Setting> getAll() { return Setting.findAll(); }

    public Setting findById(int id) { return Setting.findById(id); }
}