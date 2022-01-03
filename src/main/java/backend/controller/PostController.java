package backend.controller;

import backend.constants.Constants;
import backend.controller.requests.CreatePostRequest;
import backend.controller.responses.PostResponse;
import backend.controller.responses.PostResponseType;
import backend.model.PostFactory;
import backend.model.entity.PostEntity;
import backend.model.entity.TypeEntity;
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
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletContext;
import java.sql.Time;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@Slf4j
@CrossOrigin
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
        String postText = createPostRequest.getText();

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
        String userEmail = (String) servletContext.getAttribute(Constants.SCONTEXT_USER_EMAIL_KEY);

        var user = userRepository.findByEmail(userEmail);
        if (user.isEmpty()) {
            log.error("Could not find calling user in database");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server-side database error");
        }

        var announcementType = typeRepository.getTypeByName(Constants.ANNOUNCEMENT_TYPE_NAME);
        if (announcementType.isEmpty()) {
            log.error("Could not find type in database");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server-side database error");
        }

        // We do some fishy stuff here - the user can be an admin and some of his posts can be announcements
        // so we add all his posts first as regular posts and then we add the announcements
        // Same thing happens with friends who can be admins and their posts thus can be announcements
        // but the PostResponse has overriden hashcode and equals, where PostType is not considered
        // so the announcements added later on will replace the ones incorrectly added as regular posts
        var userPosts = postRepository.getPostsByUserNewest(userEmail);
        var postsToReturn = new HashSet<>(convertPostsToResponses(userPosts, user.get().getName(), PostResponseType.REGULAR));

        var friends = user.get().getFriends();
        for (var friend : friends) {
            postsToReturn.addAll(convertPostsToResponses(postRepository.getPostsByUserNewest(friend.getEmail()),
                    friend.getName(), PostResponseType.REGULAR));
        }

        var announcements = postRepository.getPostsByTypeNewest(announcementType.get().getId());

        for (var announcement : announcements) {
            PostResponse pr = new PostResponse();
            pr.setType(PostResponseType.ANNOUNCEMENT);
            pr.setText(announcement.getText());
            pr.setTimePostedMs(announcement.getTime_posted().getTime());

            var postingUser = userRepository.findByEmail(announcement.getUser_email());
            if (postingUser.isEmpty()){
                log.error("Could not find user in database");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server-side database error");
            }

            pr.setUserName(postingUser.get().getName());
            postsToReturn.add(pr);
        }

        // Sort the posts by time descending and limit the number to count
        var sorted = postsToReturn.stream()
                .sorted(new PostResponseTimeComparator().reversed() )
                .limit(count)
                .collect(Collectors.toSet());

        // The response is not guaranteed to be sorted
        return ResponseEntity.ok(sorted);
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
        String postText = createAnnouncementRequest.getText();

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

    private Set<PostResponse> convertPostsToResponses(Collection<PostEntity> posts, String userName, PostResponseType type) {
        Set<PostResponse> responses = new HashSet<>();
        for (var post : posts) {
            PostResponse response = new PostResponse();
            response.setType(type);
            response.setUserName(userName);
            response.setText(post.getText());
            var time = post.getTime_posted().getTime();
            response.setTimePostedMs(post.getTime_posted().getTime());
            responses.add(response);
        }

        return responses;
    }

    private class PostResponseTimeComparator implements Comparator<PostResponse> {

        @Override
        public int compare(PostResponse o1, PostResponse o2) {
            return o1.getTimePostedMs().compareTo(o2.getTimePostedMs());
        }

        @Override
        public Comparator<PostResponse> reversed() {
            return new Reversed();
        }

        private class Reversed implements Comparator<PostResponse> {

            @Override
            public int compare(PostResponse o1, PostResponse o2) {
                return -1 * o1.getTimePostedMs().compareTo(o2.getTimePostedMs());
            }
        }
    }

}
