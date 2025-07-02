package cool.drinkup.drinkup.record.internal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.ZonedDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tasting_record_image")
@Getter
@Setter
public class TastingRecordImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "record_id", nullable = false)
    private Long recordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", insertable = false, updatable = false)
    private TastingRecord tastingRecord;

    @Column(name = "image", nullable = false, length = 500)
    private String image;

    @Column(name = "is_cover", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Integer isCover = 0;

    @Column(name = "sort")
    private Integer sort = 0;

    @Column(name = "created_time")
    private ZonedDateTime createdTime;
}
