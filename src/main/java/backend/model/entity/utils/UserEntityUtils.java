package backend.model.entity.utils;

import backend.model.entity.UserEntity;

public class UserEntityUtils {
    public static boolean hasBlocked(UserEntity source, UserEntity target) {
        return source.getBlocked().contains(target);
    }

    public static boolean hasRequested(UserEntity source, UserEntity target) {
        return source.getRequests().contains(target);
    }
}
