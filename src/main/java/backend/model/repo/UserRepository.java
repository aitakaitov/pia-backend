package backend.model.repo;

import backend.model.entity.RoleEntity;
import backend.model.entity.UserEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, String> {

	Optional<UserEntity> findByEmail(String email);

	@Query("SELECT u FROM UserEntity u JOIN u.requests WHERE u.user_to = :email")
	Set<UserEntity> getRequestsToUser(String email);
}
