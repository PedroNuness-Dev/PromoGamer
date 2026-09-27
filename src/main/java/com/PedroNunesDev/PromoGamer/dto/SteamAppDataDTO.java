package com.PedroNunesDev.PromoGamer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SteamAppDataDTO(
        String name,
        @JsonProperty("steam_appid") String steamAppId,
        @JsonProperty("header_image") String headerImage,
        @JsonProperty("short_description") String shortDescription,
        @JsonProperty("price_overview") SteamPriceOverviewDTO priceOverview
) {}