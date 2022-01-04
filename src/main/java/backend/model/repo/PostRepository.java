package backend.model.repo;


import backend.model.entity.PostEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Set;
import java.util.UUID;

@Repository
public interface PostRepository extends CrudRepository<PostEntity, UUID> {

    @Query("SELECT p FROM PostEntity p WHERE p.user_email = :email")
    Set<PostEntity> getPostsByUser(@Param("email") String email);

    @Query("SELECT p FROM PostEntity p JOIN p.types t WHERE t.id = :type")
    Set<PostEntity> getPostsByType(@Param("type") UUID type);

    @Query("SELECT p FROM PostEntity p WHERE p.user_email = :email AND p.time_posted < :timestamp")
    Set<PostEntity> getPostsByUserOlderThan(@Param("email") String email, @Param("timestamp") Timestamp timestamp);

    @Query("SELECT p FROM PostEntity p JOIN p.types t WHERE t.id = :type AND p.time_posted < :timestamp")
    Set<PostEntity> getPostsByTypeOlderThan(@Param("type") UUID type, @Param("timestamp") Timestamp timestamp);

    @Query("SELECT p FROM PostEntity p WHERE p.user_email = :email AND p.time_posted > :timestamp")
    Set<PostEntity> getPostsByUserNewerThan(@Param("email") String email, @Param("timestamp") Timestamp timestamp);

    @Query("SELECT p FROM PostEntity p JOIN p.types t WHERE t.id = :type AND p.time_posted > :timestamp")
    Set<PostEntity> getPostsByTypeNewerThan(@Param("type") UUID type, @Param("timestamp") Timestamp timestamp);
}