package fr.kainovaii.unitpanel.app.repository;

import fr.kainovaii.core.database.DB;
import fr.kainovaii.unitpanel.app.models.User;
import org.javalite.activejdbc.LazyList;

import java.util.List;

public class UserRepository {

    public Boolean create(String username, String email,  String password, String role)
    {
        return DB.withConnection(() ->
        {
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(password);
            user.setRole(role);
            return user.saveIt();
        });
    }

    public Boolean updateById(int id, String newUsername, String newRole)
    {
        return DB.withConnection(() ->
        {
            User user = this.findById(id);
            user.setUsername(newUsername);
            user.setRole(newRole);
            return user.saveIt();
        });
    }

    public boolean updateByUsername(String username, String newUsername, String newRole)
    {
        return DB.withConnection(() ->
        {
            User user = this.findByUsername(username);
            user.setUsername(newUsername);
            user.setRole(newRole);
            return user.saveIt();
        });
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

    public List<User> gr() {
        return null;
    }
}
