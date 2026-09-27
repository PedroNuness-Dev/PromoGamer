package com.PedroNunesDev.PromoGamer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SteamPackageDataDTO(
        String name,
        @JsonProperty("header_image") String headerImage,
        SteamPackagePriceDTO price
) {}