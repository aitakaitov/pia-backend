package backend.controller;

import backend.auth.requests.EmailAvailableRequest;
import backend.constants.Constants;
import backend.controller.requests.CreatePostRequest;
import backend.model.PostFactory;
import backend.model.entity.PostEntity;
import backend.model.entity.RoleEntity;
import backend.model.entity.TypeEntity;
import backend.model.entity.UserEntity;
import backend.model.repo.PostRepository;
import backend.model.repo.TypeRepository;
import backend.model.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletContext;
import java.util.HashSet;
import java.util.Set;

@RestController
@Slf4j
@RequiredArgsConstructor
public class PostController {

    final PostRepository postRepository;
    final UserRepository userRepository;
    final TypeRepository typeRepository;

    @Autowired
    private ServletContext servletContext;

    @RequestMapping(value = "/api/post", method = RequestMethod.POST)
    public ResponseEntity<?> createPost(@RequestBody CreatePostRequest createPostRequest) throws Exception {
        String userEmail = (String)servletContext.getAttribute(Constants.SCONTEXT_USER_EMAIL_KEY);
        String postText = createPostRequest.getPostText();

        // posts cannot be empty
        if (postText.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        PostEntity post = PostFactory.createPost(postText, userEmail);

        var typeOptional = typeRepository.getTypeByName(Constants.REGULAR_TYPE_NAME);
        if (typeOptional.isEmpty()) {
           return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Set<TypeEntity> types = new HashSet<>();
        types.add(typeOptional.get());
        post.setTypes(types);
        postRepository.save(post);

        return new ResponseEntity<>(HttpStatus.OK);
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
