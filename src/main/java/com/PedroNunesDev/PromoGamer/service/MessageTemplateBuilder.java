package com.PedroNunesDev.PromoGamer.service;

import com.PedroNunesDev.PromoGamer.dto.SteamAppDataDTO;
import com.PedroNunesDev.PromoGamer.dto.SteamPackageDataDTO;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.util.Locale;

@Component
public class MessageTemplateBuilder {

    private static final Locale PT_BR = Locale.of("pt", "BR");

    public String buildCaptionForApp(SteamAppDataDTO data, String storeUrl, String description) {

        StringBuilder sb = new StringBuilder();

        sb.append("*🎮 ").append(data.name()).append("*\n\n");
        sb.append(description).append("\n\n");

        if (data.priceOverview() != null) {
            sb.append("🏷️ De ").append(data.priceOverview().initialFormatted())
                    .append(" por ").append(data.priceOverview().finalFormatted())
                    .append(" (").append(data.priceOverview().discountPercent()).append("% OFF)\n\n");
        }

        sb.append("🛒 Ver oferta na Steam:\n").append(storeUrl);

        return sb.toString();
    }

    public String buildCaptionForPackage(SteamPackageDataDTO data, String storeUrl, String description) {

        StringBuilder sb = new StringBuilder();

        sb.append("*🎮 ").append(data.name()).append("*\n\n");
        sb.append("📦 *PACOTE ESPECIAL*\n\n");
        sb.append(description).append("\n\n");

        if (data.price() != null) {
            String initialFormatted = formatCentsToBrl(data.price().initial());
            String finalFormatted = formatCentsToBrl(data.price().finalPrice());

            sb.append("🏷️ De ").append(initialFormatted)
                    .append(" por ").append(finalFormatted)
                    .append(" (").append(data.price().discountPercent()).append("% OFF)\n\n");
        }

        sb.append("🛒 Ver oferta na Steam:\n").append(storeUrl);

        return sb.toString();
    }

    private String formatCentsToBrl(Integer cents) {
        if (cents == null) return "";
        double value = cents / 100.0;
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(PT_BR);
        return currencyFormat.format(value);
    }
}