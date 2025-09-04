package cool.drinkup.drinkup.wine.internal.model;

import cool.drinkup.drinkup.wine.internal.enums.PropagateTypeEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "share_mapping")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShareMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shared_id", unique = true, nullable = false, length = 64)
    private String sharedId;

    @Column(nullable = false, length = 64)
    private PropagateTypeEnum type;

    @Column(name = "record_id", nullable = false)
    private Long recordId;

    @Column(name = "user_id")
    private Long userId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP")
    private ZonedDateTime createdAt = ZonedDateTime.now(ZoneOffset.UTC);
}
