package com.PedroNunesDev.PromoGamer.service;

import com.PedroNunesDev.PromoGamer.client.SteamApiService;
import com.PedroNunesDev.PromoGamer.dto.SteamAppDataDTO;
import com.PedroNunesDev.PromoGamer.dto.SteamDetailsWrapper;
import com.PedroNunesDev.PromoGamer.dto.SteamPackageDataDTO;
import com.PedroNunesDev.PromoGamer.model.Deal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SteamService {

    private final SteamApiService steamApiService;

    public SteamAppDataDTO buildDetailsFromDealBySteamAPIForBaseGame(Deal deal) {

        Assert.notNull(deal, "Deal não pode ser null");
        Assert.notNull(deal.getSteamAppId(), "Steam app id não pode ser null");

        String steamAppId = deal.getSteamAppId();

        log.info("Iniciando busca de detalhes do jogo com steam app id: [{}]", steamAppId);

        Map<String, SteamDetailsWrapper<SteamAppDataDTO>> appResponse = steamApiService.getAppDetails(
                steamAppId, "br", "brazilian", "basic,price_overview"
        );

        Optional<SteamDetailsWrapper<SteamAppDataDTO>> appWrapperOpt = appResponse.values()
                .stream()
                .findFirst();

        if (appWrapperOpt.isPresent() && appWrapperOpt.get().success()) {

            SteamAppDataDTO appGame = appWrapperOpt.get().data();

            if (appGame.priceOverview().discountPercent() == 0) {
                log.info("Percetual de desconto da promoção do app com SteamAppId: [{}] é igual a 0", steamAppId);
                return null;
            }

            return appGame;
        }


        log.warn("Steam app id [{}] não é válido como app, tentando como PACKAGE.", steamAppId);

        return null;
    }

    public SteamPackageDataDTO buildDetailsFromDealBySteamAPIForPacakge(Deal deal) {

        Assert.notNull(deal, "Deal não pode ser null");
        Assert.notNull(deal.getSteamAppId(), "Steam app id não pode ser null");

        String steamAppId = deal.getSteamAppId();

        log.info("App id [{}] não é um app válido, tentando como package...", steamAppId);

        Map<String, SteamDetailsWrapper<SteamPackageDataDTO>> packageResponse = steamApiService.getPackageDetails(
                steamAppId, "br", "brazilian"
        );

        Optional<SteamDetailsWrapper<SteamPackageDataDTO>> packageWrapperOpt = packageResponse.values()
                .stream()
                .findFirst();

        if (packageWrapperOpt.isPresent() && packageWrapperOpt.get().success()) {

           SteamPackageDataDTO packageGame = packageWrapperOpt.get().data();

            if (packageGame.price().discountPercent() == 0) {
                log.info("Percetual de desconto da promoção do pacote com SteamAppId: [{}] é igual a 0", steamAppId);
                return null;
            }

            return packageGame;
        }

        log.warn("Steam app id [{}] também não é válido  como package. Deal será ignorada.", steamAppId);

        return null;
    }
}
