package com.PedroNunesDev.PromoGamer.schedule;

import com.PedroNunesDev.PromoGamer.dto.DealDtoResponse;
import com.PedroNunesDev.PromoGamer.service.DealService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DealScheduledService {

    private final DealService dealService;
    private static final Long STEAM_STORE_ID = 1L;

    @Scheduled(cron = "0 0 8,18 * * *", zone = "America/Sao_Paulo")
    public void registerNewDealsScheduled(){

        try{
            log.info("Execunting Scheduled for register new deals...");

            List<DealDtoResponse> deals = dealService.registerDeals(STEAM_STORE_ID);

            log.info("{} new deals registered.", deals.size());
        } catch (FeignException.NotFound e) {
            log.warn("Ocorreu um erro na busca de promoções na API do CheapShark: {}", e.getMessage());
        }
        catch (FeignException.TooManyRequests e){
            log.error("Ocorreu um erro 429 na API do CheapShark: {}", e.getMessage());
        }
        catch (Exception e){
            log.error("Ocorreu um erro inesperado: {}", e.getMessage());
        }
    }
}
