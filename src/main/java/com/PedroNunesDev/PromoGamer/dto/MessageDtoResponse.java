package com.PedroNunesDev.PromoGamer.dto;

import com.PedroNunesDev.PromoGamer.enums.DealSourceType;
import com.PedroNunesDev.PromoGamer.enums.MessageStatus;

import java.time.LocalDateTime;

public record MessageDtoResponse(

        Long messageId,
        DealDtoResponse deal,
        LocalDateTime sendAt,
        LocalDateTime creationAt,
        MessageStatus messageStatus,
        DealSourceType sourceType,
        String number,
        String mediatype,
        String mimetype,
        String media,
        String caption
) {
}