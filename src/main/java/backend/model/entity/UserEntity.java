package backend.model.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Collections;
import java.util.Set;

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

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
			name = "USER_ROLE",
			joinColumns = {
					@JoinColumn(name = "user_email")
			},
			inverseJoinColumns = {
					@JoinColumn(name = "role_id")
			}
	)
	private Set<RoleEntity> roles = Collections.emptySet();

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
			name = "USER_BLOCKED",
			joinColumns = {
					@JoinColumn(name = "user_blocker")
			},
			inverseJoinColumns = {
					@JoinColumn(name = "user_blocked")
			}
	)
	private Set<UserEntity> blocked = Collections.emptySet();

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
			name = "USER_REQUEST",
			joinColumns = {
					@JoinColumn(name = "user_from")
			},
			inverseJoinColumns = {
					@JoinColumn(name = "user_to")
			}
	)
	private Set<UserEntity> requests = Collections.emptySet();

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
			name = "USER_FRIEND",
			joinColumns = {
					@JoinColumn(name = "user_from")
			},
			inverseJoinColumns = {
					@JoinColumn(name = "user_to")
			}
	)
	private Set<UserEntity> friends = Collections.emptySet();

	/*@OneToMany(mappedBy = "user_email")
	private Set<PostEntity> posts;*/
}
