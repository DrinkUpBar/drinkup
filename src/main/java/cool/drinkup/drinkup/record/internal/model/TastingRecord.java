package cool.drinkup.drinkup.record.internal.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tasting_record")
@Getter
@Setter
public class TastingRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "tasting_date")
    private ZonedDateTime tastingDate;

    @Column(name = "status", columnDefinition = "TINYINT(1) DEFAULT 1")
    private Integer status = 1;

    @Column(name = "beverage_id", nullable = false)
    private Long beverageId;

    @Column(name = "beverage_type", nullable = false, length = 50)
    private String beverageType;

    @Column(name = "created_time")
    private ZonedDateTime createdTime;

    @Column(name = "updated_time")
    private ZonedDateTime updatedTime;

    @OneToMany(mappedBy = "recordId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TastingRecordImage> images;
}
