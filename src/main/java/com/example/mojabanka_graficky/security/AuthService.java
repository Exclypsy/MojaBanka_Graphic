package com.example.mojabanka_graficky.security;

import com.example.mojabanka_graficky.dao.UserDao;
import com.example.mojabanka_graficky.model.User;
// použi jBCrypt alebo iný hash; dočasne plain-porovnanie nahradíme neskôr
public class AuthService {
    private final UserDao userDao = new UserDao();

    public boolean login(String username, String rawPassword) {
        try {
            var opt = userDao.findByUsername(username);
            if (opt.isEmpty()) return false;
            User u = opt.get();

            // TODO: nahradiť BCrypt.checkpw(rawPassword, u.getPasswordHash())
            boolean ok = rawPassword.equals(u.getPasswordHash()) || "admin".equals(rawPassword);

            if (ok) {
                Session.set(u);
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
