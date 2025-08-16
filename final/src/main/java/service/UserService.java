package service;

import content.User;
import org.hibernate.query.Query;
import framework.SingletonSessionFactory;

import java.util.concurrent.atomic.AtomicReference;

public class UserService {
    private UserService() {}

    public static User login(String email, String password) {
        email = email.trim();
        if (email.contains(" ")) {
            throw new IllegalArgumentException("Email cannot include spaces");
        }
        if (!email.contains("@milou.com")) {
            email += "@milou.com";
        }
        User user = getByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("Invalid email");
        }
        if (!user.getPassword().equals(password)) {
            throw new IllegalArgumentException("Invalid password");
        }
        return user;
    }

    public static void signUp(String name, String password, String email) {
        SingletonSessionFactory.get().inTransaction(session -> {
            User user = new User(name, password, email);
            session.persist(user);
        });
    }

    public static User getByEmail(String e) {
        AtomicReference<User> user = new AtomicReference<>();
        SingletonSessionFactory.get().inTransaction(session -> {
            String hql = "FROM User WHERE email = :email";
            Query<User> query = session.createQuery(hql, User.class);
            query.setParameter("email", e);

            user.set(query.uniqueResult());
        });

        return user.get();
    }

}
