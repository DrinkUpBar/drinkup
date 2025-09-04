package cool.drinkup.drinkup.record.internal.mapper;

import cool.drinkup.drinkup.record.internal.controller.resp.TastingRecordImageResp;
import cool.drinkup.drinkup.record.internal.controller.resp.TastingRecordResp;
import cool.drinkup.drinkup.record.internal.model.TastingRecord;
import cool.drinkup.drinkup.record.internal.model.TastingRecordImage;
import cool.drinkup.drinkup.shared.spi.CommonMapper;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(
        componentModel = "spring",
        uses = CommonMapper.class,
        injectionStrategy = org.mapstruct.InjectionStrategy.CONSTRUCTOR)
public interface TastingRecordMapper {

    @Mapping(target = "tastingDate", source = "tastingDate", qualifiedByName = "zonedDateTimeToString")
    @Mapping(target = "createdTime", source = "createdTime", qualifiedByName = "zonedDateTimeToString")
    TastingRecordResp toTastingRecordResp(TastingRecord tastingRecord);

    @Mapping(target = "url", source = "image", qualifiedByName = "imageToUrl")
    TastingRecordImageResp toTastingRecordImageResp(TastingRecordImage image);

    @Named("zonedDateTimeToString")
    default String zonedDateTimeToString(ZonedDateTime zonedDateTime) {
        if (zonedDateTime == null) {
            return null;
        }
        // 转换为UTC+8时区
        ZonedDateTime beijingTime = zonedDateTime.withZoneSameInstant(ZoneId.of("Asia/Shanghai"));
        return beijingTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
