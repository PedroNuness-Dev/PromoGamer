package com.PedroNunesDev.PromoGamer.service;

import com.PedroNunesDev.PromoGamer.dto.MessageDtoResponse;
import com.PedroNunesDev.PromoGamer.dto.SteamAppDataDTO;
import com.PedroNunesDev.PromoGamer.dto.SteamPackageDataDTO;
import com.PedroNunesDev.PromoGamer.enums.DealEnumStatus;
import com.PedroNunesDev.PromoGamer.enums.DealSourceType;
import com.PedroNunesDev.PromoGamer.exception.ResourceNotFoundException;
import com.PedroNunesDev.PromoGamer.mapper.MessageMapper;
import com.PedroNunesDev.PromoGamer.model.Deal;
import com.PedroNunesDev.PromoGamer.model.Message;
import com.PedroNunesDev.PromoGamer.repository.DealRepository;
import com.PedroNunesDev.PromoGamer.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;
    private final DealRepository dealRepository;
    private final SteamService steamService;
    private final MessageMapper messageMapper;
    private final MessageTemplateBuilder templateBuilder;
    private final ChatClient chatClient;

    @Value("${promogamer.whatsapp.group-number}")
    private String groupNumber;


    @Transactional
    public MessageDtoResponse saveNewMessage(){

        log.info("Iniciando salvamento de nova mensagem para envio...");

        Deal deal = dealRepository.findFirstByDealEnumStatus(DealEnumStatus.PENDENTE)
                .orElseThrow(() -> new ResourceNotFoundException("Nenhum promoção para envio encontrada"));

        Message message = buildMessageWithDetails(deal);

        if (message == null){
            deal.updateStatus(DealEnumStatus.IGNORADO);
            dealRepository.save(deal);
            return null;
        }

        Message newMessage = messageRepository.save(message);

        deal.updateStatus(DealEnumStatus.PROCESSADO);
        dealRepository.save(deal);

        return messageMapper.toDTO(newMessage);
    }

    private Message buildMessageWithDetails(Deal deal){

        Message message = null;

        SteamAppDataDTO steamAppDataDTO = steamService.buildDetailsFromDealBySteamAPIForBaseGame(deal);

        if (steamAppDataDTO != null){
            message = buildMessageFromAppData(deal, steamAppDataDTO);
        }
        else{
            SteamPackageDataDTO steamPackageDataDTO = steamService.buildDetailsFromDealBySteamAPIForPacakge(deal);

            if (steamPackageDataDTO != null){
                message = buildMessageFromPackageData(deal,steamPackageDataDTO);
            }
        }

        return message;
    }

    private String generateDescriptionGameWithAI(String informationGame, DealSourceType dealSourceType) {
        String prompt = buildPromptForDescription(informationGame, dealSourceType);

        try {
            String description = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            log.info("Descrição gerada via IA com sucesso...");
            return description;

        }
        catch (WebClientResponseException.TooManyRequests e) {
            log.warn("Rate limit atingido ao gerar descrição para: [{}]", informationGame, e);
            return informationGame;

        } catch (ResourceAccessException e) {
            log.error("Falha de rede/timeout ao chamar a IA para: [{}]", informationGame, e);
            return informationGame;

        } catch (Exception e) {
            // catch-all de segurança para não quebrar o fluxo de envio da mensagem
            log.error("Falha inesperada ao gerar descrição via IA para: [{}]. Usando fallback.", informationGame, e);
            return informationGame;
        }
    }

    private String buildPromptForDescription(String informationGame, DealSourceType dealSourceType) {
        return switch (dealSourceType) {
            case BASE_GAME -> """
                    Reescreva a descrição a seguir de um jogo em promoção,  de forma
                    curta e chamativa (máximo 2 frases), destacando o que torna o jogo interessante:
                    
                    Descrição original: %s
                    """.formatted(informationGame);

            case PACKAGE -> """
                    Escreva uma descrição curta e chamativa (máximo 2 frases) para o
                    seguinte pacote de jogos em promoção:
                    
                    Nome do pacote: %s
                    """.formatted(informationGame);
        };
    }

    private Message buildMessageFromAppData(Deal deal, SteamAppDataDTO data) {

        String imageUrl = data.headerImage();
        String storeUrl = "https://store.steampowered.com/app/" + data.steamAppId();

        String descriptionGameWithAI = generateDescriptionGameWithAI(data.shortDescription(), DealSourceType.BASE_GAME);

        String caption = templateBuilder.buildCaptionForApp(data, storeUrl,descriptionGameWithAI);

        return Message.builder()
                .deal(deal)
                .sourceType(DealSourceType.BASE_GAME)
                .number(groupNumber)
                .mediatype("image")
                .mimetype("image/jpeg")
                .media(imageUrl)
                .caption(caption)
                .build();
    }

    private Message buildMessageFromPackageData(Deal deal, SteamPackageDataDTO data) {

        String storeUrl = "https://store.steampowered.com/sub/" + deal.getSteamAppId();

        String imageUrl = data.headerImage();

        String descriptionGameWithAI = generateDescriptionGameWithAI(data.name(), DealSourceType.PACKAGE);

        String caption = templateBuilder.buildCaptionForPackage(data, storeUrl, descriptionGameWithAI);

        return Message.builder()
                .deal(deal)
                .sourceType(DealSourceType.PACKAGE)
                .number(groupNumber)
                .mediatype("image")
                .mimetype("image/jpeg")
                .media(imageUrl)
                .caption(caption)
                .build();
    }
}
