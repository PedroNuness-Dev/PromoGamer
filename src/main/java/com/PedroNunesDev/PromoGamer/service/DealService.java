package com.PedroNunesDev.PromoGamer.service;

import com.PedroNunesDev.PromoGamer.dto.CheapSharkDealDTO;
import com.PedroNunesDev.PromoGamer.dto.DealDtoResponse;
import com.PedroNunesDev.PromoGamer.enums.DealEnumStatus;
import com.PedroNunesDev.PromoGamer.model.Deal;
import com.PedroNunesDev.PromoGamer.repository.DealRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DealService {

    private final CheapSharkDealService cheapSharkDealService;
    private final DealRepository dealRepository;

    public List<DealDtoResponse> registerDeals(Long storeId) {

        long initialPageNumber = 0;
        List<Deal> dealsForSaving = new ArrayList<>();

        log.info("Iniciando busca de novas deals para storeId={}", storeId);

        do {
            log.debug("Buscando deals na página {} para storeId={}", initialPageNumber, storeId);

            List<CheapSharkDealDTO> dealsSearched = cheapSharkDealService.getDealsByStore(storeId, initialPageNumber);
            log.debug("Página {}: {} deals retornadas pela API do CheapShark", initialPageNumber, dealsSearched.size());

            Set<String> dealsSearchedId = dealsSearched
                    .stream()
                    .map(CheapSharkDealDTO::dealID)
                    .collect(Collectors.toSet());

            Set<String> existingDealsId = dealRepository.getExistingDealsId(dealsSearchedId);
            log.debug("Página {}: {} deals já existentes no banco", initialPageNumber, existingDealsId.size());

            List<CheapSharkDealDTO> newDeals = dealsSearched.stream()
                    .filter(dto -> !existingDealsId.contains(dto.dealID()))
                    .toList();

            log.info("Página {}: {} novas deals encontradas", initialPageNumber, newDeals.size());

            dealsForSaving.addAll(newDeals.stream() // Analisar melhoria para não entrar na construção se vier vazia
                    .map(deal -> Deal.builder()
                            .dealId(deal.dealID())
                            .title(deal.title())
                            .dealEnumStatus(DealEnumStatus.PENDENTE)
                            .steamAppId(deal.steamAppId())
                            .steamRatingPercent(deal.steamRatingPercent())
                            .build())
                    .toList());

            initialPageNumber++;
        } while (dealsForSaving.isEmpty() && initialPageNumber < 5);

        if (dealsForSaving.isEmpty()) {
            log.warn("Nenhuma nova deal encontrada para storeId={} após percorrer até a página {}", storeId, initialPageNumber);
            return List.of();
        }

        List<Deal> dealsSaved = dealRepository.saveAll(dealsForSaving);
        log.info("{} novas deals salvas no banco para storeId={}", dealsSaved.size(), storeId);

        return dealsSaved
                .stream()
                .map(deal -> new DealDtoResponse(
                        deal.getDealId(),
                        deal.getTitle(),
                        deal.getSteamAppId()
                )).toList();
    }

    @Transactional(readOnly = true)
    public List<DealDtoResponse> getAllDealsByStatus(String dealsEnumStatusType){

        DealEnumStatus dealEnumStatus = DealEnumStatus.from(dealsEnumStatusType);

        List<Deal> deals = dealRepository.getAllDealsByDealEnumStatus(dealEnumStatus);

        if (deals.isEmpty()) return List.of();

        return deals
                .stream()
                .map(deal -> new DealDtoResponse(
                  deal.getDealId(),
                  deal.getTitle(),
                  deal.getSteamAppId()
                ))
                .toList();
    }
}