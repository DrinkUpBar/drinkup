package cool.drinkup.drinkup.workflow.internal.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bar")
@Getter
@Setter
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
        description.append("Bar Stocks: ")
                .append(barStocks.stream().map(BarStock::getBarStockDescription).collect(Collectors.joining(", ")));
        return description.toString();
    }
}