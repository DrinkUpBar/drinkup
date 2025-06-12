package cool.drinkup.drinkup.workflow.internal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.InjectionStrategy;

import java.util.Random;

import cool.drinkup.drinkup.shared.spi.ImageServiceMapper;
import cool.drinkup.drinkup.workflow.internal.controller.req.BarCreateReq;
import cool.drinkup.drinkup.workflow.internal.controller.resp.BarVo;
import cool.drinkup.drinkup.workflow.internal.model.Bar;

@Mapper(componentModel = "spring", uses = ImageServiceMapper.class, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface BarMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "barStocks", ignore = true)
    @Mapping(target = "barProcurements", ignore = true)
    @Mapping(target = "barImageType", expression = "java(generateRandomBarImageType())")
    Bar toBar(BarCreateReq barCreateReq);

    default Integer generateRandomBarImageType() {
        return new Random().nextInt(4);
    }

    @Mapping(source = "image", target = "image", qualifiedByName = "imageToUrl")
    BarVo toBarVo(Bar bar);
}
