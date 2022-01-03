package backend.model;

import backend.model.entity.PostEntity;
import backend.model.entity.TypeEntity;
import backend.model.entity.UserEntity;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class PostFactory {
    public static PostEntity createPost(String text, String userEmail) {
        PostEntity e = new PostEntity();
        e.setText(text);
        e.setUser_email(userEmail);
        e.setTime_posted(Timestamp.valueOf(LocalDateTime.now()));

        return e;
    }
}
