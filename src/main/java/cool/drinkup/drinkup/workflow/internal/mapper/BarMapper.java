package cool.drinkup.drinkup.workflow.internal.mapper;

import cool.drinkup.drinkup.shared.spi.CommonMapper;
import cool.drinkup.drinkup.workflow.internal.controller.bar.req.BarCreateReq;
import cool.drinkup.drinkup.workflow.internal.controller.bar.resp.BarVo;
import cool.drinkup.drinkup.workflow.internal.model.Bar;
import java.util.Random;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        uses = {CommonMapper.class, BarProcurementMapper.class, BatStockMapper.class},
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
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
