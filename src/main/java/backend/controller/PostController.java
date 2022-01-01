package backend.controller;

import backend.auth.requests.EmailAvailableRequest;
import backend.constants.Constants;
import backend.controller.requests.CreatePostRequest;
import backend.model.repo.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletContext;

@RestController
@Slf4j
@RequiredArgsConstructor
public class PostController {
    final PostRepository postRepository;

    @Autowired
    private ServletContext servletContext;

    @RequestMapping(value = "/api/post", method = RequestMethod.POST)
    public ResponseEntity<?> createPost(@RequestBody CreatePostRequest createPostRequest) throws Exception {
        var a = (UserDetails)servletContext.getAttribute(Constants.SCONTEXT_USER_EMAIL_KEY);
        return null;
    }

    @RequestMapping(value = "/api/posts/new", method = RequestMethod.GET)
    public ResponseEntity<?> getNewPosts(@RequestBody CreatePostRequest createPostRequest) throws Exception {
        return null;
    }

    @RequestMapping(value = "/api/posts/old", method = RequestMethod.GET)
    public ResponseEntity<?> getOlderPosts(@RequestBody CreatePostRequest createPostRequest) throws Exception {
        return null;
    }

}
