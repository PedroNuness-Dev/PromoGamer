package com.PedroNunesDev.PromoGamer.service;

import com.PedroNunesDev.PromoGamer.client.SteamApiService;
import com.PedroNunesDev.PromoGamer.dto.*;
import com.PedroNunesDev.PromoGamer.model.Deal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SteamServiceTest {

    @Mock
    private SteamApiService steamApiService;

    @InjectMocks
    private SteamService steamService;

    // ---------- buildDetailsFromDealBySteamAPIForBaseGame ----------

    @Test
    void shouldReturnAppDataWhenAppIsValidAndHasDiscount() {

        // arrange

        Deal deal = createDeal();
        SteamAppDataDTO appDataDTO = createSteamAppDTO(50);

        when(steamApiService.getAppDetails("1234", "br", "brazilian", "basic,price_overview"))
                .thenReturn(Map.of("1234", new SteamDetailsWrapper<>(true, appDataDTO)));

        // act

        SteamAppDataDTO result = steamService.buildDetailsFromDealBySteamAPIForBaseGame(deal);

        // assert

        assertThat(result).isEqualTo(appDataDTO);

        verify(steamApiService, times(1))
                .getAppDetails("1234", "br", "brazilian", "basic,price_overview");
        verifyNoMoreInteractions(steamApiService);
    }

    @Test
    void shouldReturnNullWhenAppDiscountIsZero() {

        // arrange

        Deal deal = createDeal();
        SteamAppDataDTO appDataDTO = createSteamAppDTO(0);

        when(steamApiService.getAppDetails("1234", "br", "brazilian", "basic,price_overview"))
                .thenReturn(Map.of("1234", new SteamDetailsWrapper<>(true, appDataDTO)));

        // act

        SteamAppDataDTO result = steamService.buildDetailsFromDealBySteamAPIForBaseGame(deal);

        // assert

        assertThat(result).isNull();

        verify(steamApiService, times(1))
                .getAppDetails("1234", "br", "brazilian", "basic,price_overview");
    }

    @Test
    void shouldReturnNullWhenAppIsNotValid() {

        // arrange

        Deal deal = createDeal();

        when(steamApiService.getAppDetails("1234", "br", "brazilian", "basic,price_overview"))
                .thenReturn(Map.of("1234", new SteamDetailsWrapper<>(false, null)));

        // act

        SteamAppDataDTO result = steamService.buildDetailsFromDealBySteamAPIForBaseGame(deal);

        // assert

        assertThat(result).isNull();

        verify(steamApiService, times(1))
                .getAppDetails("1234", "br", "brazilian", "basic,price_overview");
        verify(steamApiService, never())
                .getPackageDetails(anyString(), anyString(), anyString());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenDealIsNullOnBaseGame() {

        // act & assert

        assertThatThrownBy(() -> steamService.buildDetailsFromDealBySteamAPIForBaseGame(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Deal não pode ser null");

        verifyNoInteractions(steamApiService);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenSteamAppIdIsNullOnBaseGame() {

        // arrange

        Deal deal = createDeal();
        deal.setSteamAppId(null);

        // act & assert

        assertThatThrownBy(() -> steamService.buildDetailsFromDealBySteamAPIForBaseGame(deal))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Steam app id não pode ser null");

        verifyNoInteractions(steamApiService);
    }

    // ---------- buildDetailsFromDealBySteamAPIForPacakge ----------

    @Test
    void shouldReturnPackageDataWhenPackageIsValidAndHasDiscount() {

        // arrange

        Deal deal = createDeal();
        SteamPackageDataDTO packageDataDTO = createSteamPackageDTO(50);

        when(steamApiService.getPackageDetails("1234", "br", "brazilian"))
                .thenReturn(Map.of("1234", new SteamDetailsWrapper<>(true, packageDataDTO)));

        // act

        SteamPackageDataDTO result = steamService.buildDetailsFromDealBySteamAPIForPacakge(deal);

        // assert

        assertThat(result).isEqualTo(packageDataDTO);

        verify(steamApiService, times(1))
                .getPackageDetails("1234", "br", "brazilian");
        verifyNoMoreInteractions(steamApiService);
    }

    @Test
    void shouldReturnNullWhenPackageDiscountIsZero() {

        // arrange

        Deal deal = createDeal();
        SteamPackageDataDTO packageDataDTO = createSteamPackageDTO(0);

        when(steamApiService.getPackageDetails("1234", "br", "brazilian"))
                .thenReturn(Map.of("1234", new SteamDetailsWrapper<>(true, packageDataDTO)));

        // act

        SteamPackageDataDTO result = steamService.buildDetailsFromDealBySteamAPIForPacakge(deal);

        // assert

        assertThat(result).isNull();

        verify(steamApiService, times(1))
                .getPackageDetails("1234", "br", "brazilian");
    }

    @Test
    void shouldReturnNullWhenPackageIsNotValid() {

        // arrange

        Deal deal = createDeal();

        when(steamApiService.getPackageDetails("1234", "br", "brazilian"))
                .thenReturn(Map.of("1234", new SteamDetailsWrapper<>(false, null)));

        // act

        SteamPackageDataDTO result = steamService.buildDetailsFromDealBySteamAPIForPacakge(deal);

        // assert

        assertThat(result).isNull();

        verify(steamApiService, times(1))
                .getPackageDetails("1234", "br", "brazilian");
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenDealIsNullOnPackage() {

        // act & assert

        assertThatThrownBy(() -> steamService.buildDetailsFromDealBySteamAPIForPacakge(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Deal não pode ser null");

        verifyNoInteractions(steamApiService);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenSteamAppIdIsNullOnPackage() {

        // arrange

        Deal deal = createDeal();
        deal.setSteamAppId(null);

        // act & assert

        assertThatThrownBy(() -> steamService.buildDetailsFromDealBySteamAPIForPacakge(deal))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Steam app id não pode ser null");

        verifyNoInteractions(steamApiService);
    }

    // ---------- helpers ----------

    private Deal createDeal() {
        return Deal.builder()
                .dealId("1234")
                .title("Crazy game")
                .steamAppId("1234")
                .steamRatingPercent("9876")
                .creationDate(LocalDateTime.now())
                .build();
    }

    private SteamAppDataDTO createSteamAppDTO(int discountPercent) {
        return new SteamAppDataDTO(
                "Crazy game",
                "1234",
                "https://",
                "Short description",
                new SteamPriceOverviewDTO(
                        "BRL",
                        2599,
                        1299,
                        discountPercent,
                        "R$ 25,99",
                        "R$ 12,99"
                ));
    }

    private SteamPackageDataDTO createSteamPackageDTO(int discountPercent) {
        return new SteamPackageDataDTO(
                "Crazy game",
                "https://",
                new SteamPackagePriceDTO(
                        "1000",
                        100,
                        50,
                        discountPercent,
                        50
                )
        );
    }
}