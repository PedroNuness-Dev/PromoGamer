package com.PedroNunesDev.PromoGamer.client;

import com.PedroNunesDev.PromoGamer.dto.SteamAppDataDTO;
import com.PedroNunesDev.PromoGamer.dto.SteamDetailsWrapper;
import com.PedroNunesDev.PromoGamer.dto.SteamPackageDataDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "SteamApi", url = "https://store.steampowered.com/api")
public interface SteamApiService {

    @GetMapping("/appdetails")
    Map<String, SteamDetailsWrapper<SteamAppDataDTO>> getAppDetails(
            @RequestParam("appids") String steamAppId,
            @RequestParam("cc") String countryCode,
            @RequestParam("l") String language,
            @RequestParam("filters") String filters
    );

    @GetMapping("/packagedetails")
    Map<String, SteamDetailsWrapper<SteamPackageDataDTO>> getPackageDetails(
            @RequestParam("packageids") String packageId,
            @RequestParam("cc") String countryCode,
            @RequestParam("l") String language
    );
}