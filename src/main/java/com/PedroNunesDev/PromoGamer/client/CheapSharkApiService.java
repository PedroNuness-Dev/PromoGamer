package com.PedroNunesDev.PromoGamer.client;

import com.PedroNunesDev.PromoGamer.dto.CheapSharkDealDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "CheapShark",
        url = "https://www.cheapshark.com/api/1.0"
)
public interface CheapSharkApiService {

    @GetMapping("/deals?storeID={storeId}&onSale=1&steamRating=80&minimumReviewCount=5000&sortBy=ReviewCount&desc=1&pageSize=60&pageNumber={pageNumber}")
    List<CheapSharkDealDTO> getDealsByStoreId(
            @RequestHeader("User-Agent") String userAgent,
            @PathVariable(name = "storeId") Long storeId,
            @PathVariable(name = "pageNumber") Long pageNumber
    );
}
