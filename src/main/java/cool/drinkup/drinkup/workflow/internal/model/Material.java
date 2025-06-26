package cool.drinkup.drinkup.workflow.internal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "materials")
@Getter
@Setter
public class Material {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "name_en", nullable = false, length = 200)
    private String nameEn;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isActive = true;

    @Column(name = "sort_order", columnDefinition = "INT DEFAULT 0")
    private Integer sortOrder = 0;

    @Column(name = "created_date", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private ZonedDateTime createdDate = ZonedDateTime.now(ZoneOffset.UTC);

    @Column(name = "updated_date", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private ZonedDateTime updatedDate = ZonedDateTime.now(ZoneOffset.UTC);

    @ManyToOne
    @JoinColumn(
            name = "category_id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "FK_materials_category"))
    private MaterialCategory category;
}
