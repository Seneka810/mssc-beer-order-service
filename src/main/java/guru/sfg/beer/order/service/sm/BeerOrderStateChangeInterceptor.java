package guru.sfg.beer.order.service.sm;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.services.BeerOrderManagerImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class BeerOrderStateChangeInterceptor extends StateMachineInterceptorAdapter<BeerOrderStatusEnum, BeerOrderEventEnum> {

    private final BeerOrderRepository beerOrderRepository;

    @Autowired
    public BeerOrderStateChangeInterceptor(BeerOrderRepository beerOrderRepository) {
        this.beerOrderRepository = beerOrderRepository;
    }

    @Transactional
    @Override
    public void preStateChange(State<BeerOrderStatusEnum, BeerOrderEventEnum> state,
                               Message<BeerOrderEventEnum> message,
                               Transition<BeerOrderStatusEnum, BeerOrderEventEnum> transition,
                               StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachine,
                               StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> rootStateMachine) {
        log.debug("Pre-State Change");

        Optional.ofNullable(message)
                .flatMap(msg -> Optional.ofNullable((String) msg.getHeaders().getOrDefault(BeerOrderManagerImpl.ORDER_ID_HEADER, " ")))
                .ifPresent(orderId -> {
                    log.debug("Saving state for order id: " + orderId + " Status: " + state.getId());

//                    BeerOrder beerOrder = BeerOrder.builder()
//                            .id(UUID.fromString(orderId))
//                            .orderStatus(state.getId())
//                            .build();
//                    beerOrderRepository.saveAndFlush(beerOrder);
//
//                    Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(UUID.fromString(orderId));
//                    log.info("BeerOrder object is: {}", beerOrderOptional.get());

                    Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(UUID.fromString(orderId));
                    beerOrderOptional.ifPresentOrElse(beerOrder -> {
                        beerOrder.setId(UUID.fromString(orderId));
                        beerOrder.setOrderStatus(state.getId());
                        beerOrderRepository.saveAndFlush(beerOrder);
                    }, () -> {
                        BeerOrder beerOrder = BeerOrder.builder()
                                .id(UUID.fromString(orderId))
                                .orderStatus(state.getId())
                                .build();
                        beerOrderRepository.saveAndFlush(beerOrder);
                    });
                    log.info("BeerOrder object is: {}", beerOrderOptional.get());
                });
    }
}
