package backend.model.repo;

import backend.model.entity.PostEntity;
import backend.model.entity.RoleEntity;
import backend.model.entity.TypeEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TypeRepository extends CrudRepository<TypeEntity, UUID> {

    @Query("SELECT t FROM TypeEntity t WHERE t.name = :name")
    Optional<TypeEntity> getTypeByName(@Param("name") String name);
}