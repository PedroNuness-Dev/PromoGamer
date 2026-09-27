package com.PedroNunesDev.PromoGamer.dto;

public record WhatsappMessagePayloadDTO(
        String number,
        String mediatype,
        String mimetype,
        String media,
        String caption
) {}
