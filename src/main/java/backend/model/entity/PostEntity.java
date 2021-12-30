package backend.model.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.*;
import java.sql.Time;

@Entity
@Table(name = "POST")
@Data
public class PostEntity {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, name = "user_email")
    private String user_email;

    @Column(nullable = false, name = "time_posted")
    private Time time_posted;

    @Column(nullable = false, name = "text")
    private String text;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "POST_TYPE",
            joinColumns = {
                    @JoinColumn(name = "post_id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "type_id")
            }
    )
    private Set<TypeEntity> types = new HashSet<>();

    /*@ManyToOne
    @JoinColumn(name = "email")
    private UserEntity user;
    */
}
