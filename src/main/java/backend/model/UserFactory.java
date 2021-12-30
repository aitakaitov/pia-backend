package backend.model;

import backend.model.entity.UserEntity;
import backend.model.repo.UserRepository;

public class UserFactory {
    public static UserEntity createUser(String password, String email, String name) {
        UserEntity e = new UserEntity();
        e.setPassword(password);
        e.setEmail(email);
        e.setName(name);
        e.setOnline(false);

        return e;
    }

}
