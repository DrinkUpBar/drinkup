package cool.drinkup.drinkup.workflow.internal.model;

import cool.drinkup.drinkup.workflow.internal.service.theme.ThemeEnum;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "theme_settings")
@Getter
@Setter
public class ThemeSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ThemeEnum type;

    private String themeContent;

    private String imageConfig;
}
