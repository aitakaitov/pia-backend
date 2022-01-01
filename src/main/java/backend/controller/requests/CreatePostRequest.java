package backend.controller.requests;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class CreatePostRequest {
    private static final long serialVersionUID = 5926468583005150L;

    private String postText;

    // default constructor for JSON Parsing
    public CreatePostRequest()
    {

    }

    public CreatePostRequest(String text) {
        this.setPostText(text);
    }
}