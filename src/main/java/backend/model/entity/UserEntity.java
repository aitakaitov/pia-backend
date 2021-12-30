package backend.model.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "USERS")
@Data
public class UserEntity {

	@Id
	@Column(nullable = false, name = "email")
	private String email;

	@Column(nullable = false, name = "name")
	private String name;

	@Column(nullable = false, name = "password")
	private String password;

	@Column(nullable = false, name = "online")
	private boolean online;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(
			name = "USER_ROLE",
			joinColumns = {
					@JoinColumn(name = "user_email")
			},
			inverseJoinColumns = {
					@JoinColumn(name = "role_id")
			}
	)
	private Set<RoleEntity> roles = new HashSet<>();

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(
			name = "USER_BLOCKED",
			joinColumns = {
					@JoinColumn(name = "user_blocker")
			},
			inverseJoinColumns = {
					@JoinColumn(name = "user_blocked")
			}
	)
	private Set<UserEntity> blocked = new HashSet<>();

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(
			name = "USER_REQUEST",
			joinColumns = {
					@JoinColumn(name = "user_from")
			},
			inverseJoinColumns = {
					@JoinColumn(name = "user_to")
			}
	)
	private Set<UserEntity> requests = new HashSet<>();

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(
			name = "USER_FRIEND",
			joinColumns = {
					@JoinColumn(name = "user_from")
			},
			inverseJoinColumns = {
					@JoinColumn(name = "user_to")
			}
	)
	private Set<UserEntity> friends = new HashSet<>();
}
