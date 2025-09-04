package cool.drinkup.drinkup.common.log;

import com.mzt.logapi.beans.Operator;
import com.mzt.logapi.service.IOperatorGetService;
import cool.drinkup.drinkup.user.spi.AuthenticatedUserDTO;
import cool.drinkup.drinkup.user.spi.AuthenticationServiceFacade;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefaultOperatorGetServiceImpl implements IOperatorGetService {

    private final AuthenticationServiceFacade authenticationServiceFacade;

    @Override
    public Operator getUser() {
        Optional<AuthenticatedUserDTO> user = authenticationServiceFacade.getCurrentAuthenticatedUser();
        if (user.isPresent()) {
            AuthenticatedUserDTO authenticatedUserDTO = user.get();
            Operator operator = new Operator();
            operator.setOperatorId(String.valueOf(authenticatedUserDTO.userId()));
            return operator;
        }
        // 对于匿名用户，设置默认的操作者ID
        Operator operator = new Operator();
        operator.setOperatorId("anonymous");
        return operator;
    }
}
