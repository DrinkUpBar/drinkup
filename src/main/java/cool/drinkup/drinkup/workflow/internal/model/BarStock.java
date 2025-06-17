package cool.drinkup.drinkup.workflow.internal.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bar_stock")
@Getter
@Setter
public class BarStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "bar_id") // 注意
    private Bar bar;

    private String name;

    private String nameEn;

    private String type;

    private String iconType;

    private String description;

    @JsonIgnore
    public String getBarStockDescription() {
        return "[库存类型: " + type + ", 库存名称: " + name + ", 库存描述:" + description + "]";
    }
}