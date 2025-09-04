package cool.drinkup.drinkup.workflow.internal.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bar")
@Getter
@Setter
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id") // "id" 是 Bar 实体中 ID 字段的名称
public class Bar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String name;

    private String description;

    private Integer barImageType;

    private String image;

    @JsonManagedReference
    @OneToMany(mappedBy = "bar", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BarStock> barStocks;

    @JsonManagedReference
    @OneToMany(mappedBy = "bar", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BarProcurement> barProcurements;

    @JsonIgnore
    public String getBarDescription() {
        StringBuilder description = new StringBuilder();
        description.append("Bar Name: ").append(name).append("\n");
        description
                .append("Bar Stocks: ")
                .append(barStocks.stream().map(BarStock::getBarStockDescription).collect(Collectors.joining(", ")));
        return description.toString();
    }
}
