package cool.drinkup.drinkup.shared.spi;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CommonMapper {
    
    private final ImageServiceFacade imageServiceFacade;
    
    @Named("imageToUrl")
    public String imageToUrl(String imageId) {
        return imageServiceFacade.getImageUrl(imageId);
    }

    @Named("dateToString")
    public String dateToString(ZonedDateTime date) {
        if (date == null) {
            return null;
        }
        // Convert UTC time to UTC+8 (Asia/Shanghai timezone)
        ZonedDateTime shanghaiTime = date.withZoneSameInstant(ZoneId.of("Asia/Shanghai"));
        return shanghaiTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
} 