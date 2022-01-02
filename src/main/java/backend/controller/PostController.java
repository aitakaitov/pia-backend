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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.spi.service.contexts.SecurityContext;

import javax.servlet.ServletContext;
import java.sql.Time;
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

    @RequestMapping(value = "/api/posts", method = RequestMethod.POST)
    public ResponseEntity<?> createPost(@RequestBody CreatePostRequest createPostRequest) throws Exception {
        String userEmail = (String)servletContext.getAttribute(Constants.SCONTEXT_USER_EMAIL_KEY);
        String postText = createPostRequest.getPostText();

        // posts cannot be empty
        if (postText.isEmpty()) {
            log.info("Post text is empty");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Post text is empty");
        }

        PostEntity post = PostFactory.createPost(postText, userEmail);

        var typeOptional = typeRepository.getTypeByName(Constants.REGULAR_TYPE_NAME);
        if (typeOptional.isEmpty()) {
            log.error("Error fetching post type from database");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching post type from database");
        }

        Set<TypeEntity> types = new HashSet<>();
        types.add(typeOptional.get());
        post.setTypes(types);
        postRepository.save(post);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/api/posts", method = RequestMethod.GET)
    public ResponseEntity<?> getPosts(@RequestParam Integer count) throws Exception {
        // TODO return count of newest posts
        return null;
    }

    @RequestMapping(value = "/api/posts/new", method = RequestMethod.GET)
    public ResponseEntity<?> getNewPosts(@RequestParam String timeStamp) throws Exception {
        Time time = Time.valueOf(timeStamp);
        // TODO return all posts newer than the timestamp

        return null;
    }

    @RequestMapping(value = "/api/posts/old", method = RequestMethod.GET)
    public ResponseEntity<?> getOlderPosts(@RequestParam String timeStamp, @RequestParam Integer count) throws Exception {
        // TODO return count of posts older than the timestamp

        return null;
    }

    @RequestMapping(value = "/api/announcements", method = RequestMethod.POST)
    public ResponseEntity<?> createAnnouncement(@RequestBody CreatePostRequest createAnnouncementRequest) throws Exception {
        String userEmail = (String)servletContext.getAttribute(Constants.SCONTEXT_USER_EMAIL_KEY);
        String postText = createAnnouncementRequest.getPostText();

        var authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        boolean isAdmin = false;
        for (var sga : authorities) {
            if (sga instanceof SimpleGrantedAuthority) {
                if (sga.getAuthority().equals("ROLE_" + Constants.ADMIN_ROLE_NAME)) {
                    isAdmin = true;
                }
            }
        }

        if (!isAdmin) {
            log.info("Attempt to access announcement endpoint by non-admin user");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Endpoint access restricted to admin role users");
        }

        // posts cannot be empty
        if (postText.isEmpty()) {
            log.info("Announcement text is empty");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Announcements cannot be empty");
        }

        PostEntity post = PostFactory.createPost(postText, userEmail);

        var typeOptional = typeRepository.getTypeByName(Constants.ANNOUNCEMENT_TYPE_NAME);
        if (typeOptional.isEmpty()) {
            log.error("Error fetching post type from database");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching post type from database");
        }

        Set<TypeEntity> types = new HashSet<>();
        types.add(typeOptional.get());
        post.setTypes(types);
        postRepository.save(post);

        return new ResponseEntity<>(HttpStatus.OK);
    }

}
