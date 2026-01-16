package fr.kainovaii.unitpanel.app.repository;

import fr.kainovaii.core.database.DB;
import fr.kainovaii.unitpanel.app.models.User;
import org.javalite.activejdbc.LazyList;

public class UserRepository {

    public Boolean create(String username, String password, String role)
    {
        return DB.withConnection(() ->
        {
            User user = new User();
            user.set("username", username, "password", password, "role", role);
            return user.saveIt();
        });
    }

    public boolean updateById(int id, String newUsername, String newRole)
    {
        return DB.withConnection(() ->
        {
            User user = this.findById(id);
            user.set("username", newUsername, "role", newRole);
            return user.saveIt();
        });
    }

    public boolean updateByUsername(String username, String newUsername, String newPassword)
    {
        User user = this.findByUsername(username);
        user.set("username", newUsername, "password", newPassword);
        return user.saveIt();
    }

    public LazyList<User> getAll() {
        return User.findAll();
    }

    public User findById(int id) {
        return User.findFirst("id = ?", id);
    }

    public int deleteByID(int id) { return User.delete("id = ?", id); }

    public User findByUsername(String username) {
        return User.findFirst("username = ?", username);
    }

    public static boolean userExist(String username) {
        return User.findFirst("username = ?", username) != null;
    }
}
