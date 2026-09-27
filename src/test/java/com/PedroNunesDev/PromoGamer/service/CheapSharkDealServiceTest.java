package com.PedroNunesDev.PromoGamer.service;

import com.PedroNunesDev.PromoGamer.client.CheapSharkApiService;
import com.PedroNunesDev.PromoGamer.dto.CheapSharkDealDTO;
import static org.junit.jupiter.api.Assertions.*;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class CheapSharkDealServiceTest {

    @Mock
    private CheapSharkApiService cheapSharkApiService;

    @InjectMocks
    private CheapSharkDealService cheapSharkDealService;

    @BeforeEach
    void setupValue(){
        ReflectionTestUtils.setField(
                cheapSharkDealService,
                "addressEmail",
                "teste@teste.com"
        );
    }

    @Test
    void shouldReturnDealsWhenApiReturnsDeals() {

        // Arrange

        CheapSharkDealDTO deal = new CheapSharkDealDTO(
                "ELDEN RING",
                "12345",
                "1245620",
                "97",
                1710000000L
        );

        List<CheapSharkDealDTO> expectedDeals = new ArrayList<>();
        expectedDeals.add(deal);

        when(cheapSharkApiService.getDealsByStoreId(
                "PromoGamer/1.0 (teste@teste.com)",
                1L,
                0L
        )).thenReturn(expectedDeals);

        // Act

        List<CheapSharkDealDTO> result =
                cheapSharkDealService.getDealsByStore(1L, 0L);

        // Assert

        assertNotNull(result);
        assertEquals(1, result.size());

        CheapSharkDealDTO returnedDeal = result.getFirst();

        assertEquals("ELDEN RING", returnedDeal.title());
        assertEquals("97", returnedDeal.steamRatingPercent());

        verify(cheapSharkApiService).getDealsByStoreId(
                "PromoGamer/1.0 (teste@teste.com)",
                1L,
                0L
        );
    }

    @Test
    void shouldReturnEmptyListWhenApiReturnsEmptyList(){

        // Arrange

        when(cheapSharkApiService.getDealsByStoreId(
                "PromoGamer/1.0 (teste@teste.com)",
                1L,
                0L
        )).thenReturn(Collections.emptyList());

        // Act

        List<CheapSharkDealDTO> result = cheapSharkDealService.getDealsByStore(
                1L,
                0L
        );

        // Assert

        assertTrue(result.isEmpty());

        verify(cheapSharkApiService)
                .getDealsByStoreId(
                        "PromoGamer/1.0 (teste@teste.com)",
                        1L,
                        0L
                );
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenStoreIdIsNull(){

        assertThatThrownBy(() -> cheapSharkDealService.getDealsByStore(null, 0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Store id cannot it null");
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenPageNumberIsNull(){

        assertThatThrownBy(() -> cheapSharkDealService.getDealsByStore(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Page number cannot it null");
    }
}
