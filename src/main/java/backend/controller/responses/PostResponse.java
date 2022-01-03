package backend.controller.responses;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class PostResponse {
    private static final long serialVersionUID = 5926468583005151L;

    private String userName;
    private Long timePostedMs;
    private String text;
    private PostResponseType type;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PostResponse that = (PostResponse) o;
        return userName.equals(that.userName) && timePostedMs.equals(that.timePostedMs) && text.equals(that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, timePostedMs, text);
    }
}
