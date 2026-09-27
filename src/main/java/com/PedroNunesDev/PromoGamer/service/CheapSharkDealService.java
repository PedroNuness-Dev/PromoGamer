package com.PedroNunesDev.PromoGamer.service;

import com.PedroNunesDev.PromoGamer.client.CheapSharkApiService;
import com.PedroNunesDev.PromoGamer.dto.CheapSharkDealDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheapSharkDealService {

    private final CheapSharkApiService cheapSharkApiService;
    @Value("${spring.address.email}")
    private String addressEmail;

    public List<CheapSharkDealDTO> getDealsByStore(Long storeId, Long pageNumber) {

        Assert.notNull(storeId, "Store id cannot it null");
        Assert.notNull(pageNumber, "Page number cannot it null");

        log.info("Searching deals in store [{}], page [{}]", storeId, pageNumber);

        List<CheapSharkDealDTO> deals = cheapSharkApiService.getDealsByStoreId(
                "PromoGamer/1.0 (" + addressEmail + ")",
                storeId,
                pageNumber
        );

        // Remove Deals where steamAppId = null
        deals.removeIf(deal -> deal.steamAppId() == null);

        log.info("Search completed. {} deals found.", deals.size());

        return deals;
    }
}
