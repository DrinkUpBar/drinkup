package cool.drinkup;

import cool.drinkup.drinkup.DrinkupApplication;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ModuleDependencyTest {

    @Test
    void verifyModuleDependencies() {
        ApplicationModules modules = ApplicationModules.of(DrinkupApplication.class);
        modules.verify();
    }

    @Test
    void createModuleDocumentation() {
        ApplicationModules modules = ApplicationModules.of(DrinkupApplication.class);
        new Documenter(modules).writeDocumentation();
    }
}
