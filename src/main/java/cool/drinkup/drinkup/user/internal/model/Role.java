package cool.drinkup.drinkup.user.internal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
@Getter
@Setter
public class Role {

    @Id
    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;
}
