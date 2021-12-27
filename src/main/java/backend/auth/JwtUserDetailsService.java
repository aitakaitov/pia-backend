package backend.auth;

import java.util.ArrayList;
import java.util.stream.Collectors;

import backend.model.entity.RoleEntity;
import backend.model.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtUserDetailsService implements UserDetailsService {

    private final UserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Authenticating user {}", username);

        // We use email as unique identifier
        final var userEntity = repository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User " + username + " not found"));

        final var userBuilder = User.builder().username(username).password(userEntity.getPassword());
        userBuilder.authorities(userEntity.getRoles().stream()
                .map(RoleEntity::getName)
                .map(r -> "ROLE_" + r)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet()));

        return userBuilder.build();
    }
}
