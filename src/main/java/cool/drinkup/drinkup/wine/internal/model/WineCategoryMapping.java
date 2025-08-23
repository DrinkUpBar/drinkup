package cool.drinkup.drinkup.wine.internal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "wine_category_mapping")
@Getter
@Setter
public class WineCategoryMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "wine_id", nullable = false)
    private Long wineId;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    private BigDecimal weight = BigDecimal.ONE;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
