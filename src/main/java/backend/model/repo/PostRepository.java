package backend.model.repo;


import backend.model.entity.PostEntity;
import backend.model.entity.UserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface PostRepository extends CrudRepository<PostEntity, UUID> {

    @Query("SELECT p FROM PostEntity p WHERE p.user_email = :email ORDER BY p.time_posted DESC")
    Set<PostEntity> getPostsByUserNewest(@Param("email") String email);

    @Query("SELECT p FROM PostEntity p JOIN p.types t WHERE t.id = :type ORDER BY p.time_posted DESC")
    Set<PostEntity> getPostsByTypeNewest(@Param("type") UUID type);
}