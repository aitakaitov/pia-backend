package backend.model.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "ROLES")
@Data
public class RoleEntity {

	@Id
	@GeneratedValue
	private UUID id;

	@Column(nullable = false, name = "name")
	private String name;

}
