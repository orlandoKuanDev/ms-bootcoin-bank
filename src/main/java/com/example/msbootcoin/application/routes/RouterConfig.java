package com.example.msbootcoin.application.routes;

import com.example.msbootcoin.application.handler.BootCoinHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterConfig {
    @Bean
    public RouterFunction<ServerResponse> rutas(BootCoinHandler handler){
        return route(GET("/bootcoin"), handler::findAll)
                .andRoute(GET("/transferees"),handler::findAllTransferees)
                .andRoute(GET("/bootcoin/{phone}"), handler::findByPhone)
                .andRoute(GET("/bootcoin/{code}"), handler::findByVerificationCode)
                .andRoute(POST("/bootcoin/transference"), handler::transferenceBootCoinPending)
                .andRoute(GET("/bootcoin/approved/{mode}/{code}"), handler::transferenceApproved)
                .andRoute(GET("/bootcoin/approved/account"), handler::accountBootCoinApproved)
                .andRoute(POST("/bootcoin/create"), handler::createBootCoin);
    }
}