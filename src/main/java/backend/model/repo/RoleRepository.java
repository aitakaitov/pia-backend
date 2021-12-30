package backend.model.repo;

import backend.model.entity.PostEntity;
import backend.model.entity.RoleEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends CrudRepository<RoleEntity, UUID> {

    @Query("SELECT r FROM RoleEntity r WHERE r.name = :name")
    Optional<RoleEntity> getRoleByName(@Param("name") String name);
}
