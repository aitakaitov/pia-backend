package backend.controller.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePostRequest {
    private static final long serialVersionUID = 5926468583005150L;

    private String text;

    // default constructor for JSON Parsing
    public CreatePostRequest()
    {

    }

    public CreatePostRequest(String text) {
        this.setText(text);
    }
}