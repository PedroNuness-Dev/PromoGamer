package com.PedroNunesDev.PromoGamer.client;

import com.PedroNunesDev.PromoGamer.dto.WhatsappMessagePayloadDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "EvolutionAPI" ,url = "http://evolution-api:8082/message/sendMedia/PromoGamer")
public interface EvolutionApiService {

    @PostMapping
    public void sendWhatsappMessage(
            @RequestHeader("apikey") String apikey,
            @RequestBody WhatsappMessagePayloadDTO whatsappMessagePayloadDTO
    );
}
