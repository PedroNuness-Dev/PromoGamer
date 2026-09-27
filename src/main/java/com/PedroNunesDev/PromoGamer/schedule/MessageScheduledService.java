package com.PedroNunesDev.PromoGamer.schedule;

import com.PedroNunesDev.PromoGamer.dto.MessageDtoResponse;
import com.PedroNunesDev.PromoGamer.dto.WhatsappMessagePayloadDTO;
import com.PedroNunesDev.PromoGamer.exception.ResourceNotFoundException;
import com.PedroNunesDev.PromoGamer.model.Message;
import com.PedroNunesDev.PromoGamer.repository.MessageRepository;
import com.PedroNunesDev.PromoGamer.client.EvolutionApiService;
import com.PedroNunesDev.PromoGamer.service.MessageService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageScheduledService {

    private final MessageService messageService;
    private final MessageRepository messageRepository;
    private final EvolutionApiService evolutionApiService;

    @Value("${evolution.api.key}")
    private String apikey;

    @Scheduled(cron = "0 0/10 12-13 * * *", zone = "America/Recife")
    public void executarNaJanelaMeioDia(){
        executarEnvioDeMensagem();
    }

    //@Scheduled(cron = "0 0/10 18-20 * * *", zone = "America/Recife")
    @Scheduled(initialDelay = 1, fixedDelay = 1,timeUnit = TimeUnit.MINUTES)
    public void executarNaJanelaNoite(){
        executarEnvioDeMensagem();
    }

    @Transactional
    public void executarEnvioDeMensagem(){

        log.info("Iniciando tentativa de envio de mensagem via EvoulitonAPI");

        try{
            MessageDtoResponse response = messageService.saveNewMessage();

            if (response == null) return; // Caso não consiga montar a mensagem para envio

            log.info("Mensagem construida com sucesso, iniciando processo de envio...");

            evolutionApiService.sendWhatsappMessage(apikey,buildWhatsappMessage(response));

            log.info("Envio de mensagem concluido com sucesso!");

            updateMessage(response.id()); // marca como enviada
        }
        catch (ResourceNotFoundException e){
            log.warn(e.getMessage());
        }
        catch (FeignException.NotFound e) {
            log.warn("Ocorreu um erro na construção de uma nova mensagem para envio: {}", e.getMessage());
        }
        catch (FeignException.TooManyRequests e){
            log.error("Ocorreu um erro 429 na API do CheapShark: {}", e.getMessage());
        }
        catch (Exception e){
            log.error("Ocorreu um erro inesperado: {}", e.getMessage());
        }
    }

    @Transactional
    public void updateMessage(Long idMessage){

        Message message = messageRepository.findById(idMessage)
                .orElseThrow(() -> new ResourceNotFoundException("Mensagem com o id: ["+idMessage+"] não encontada"));

        message.markAsSent();
        messageRepository.save(message);
    }

    public WhatsappMessagePayloadDTO buildWhatsappMessage(MessageDtoResponse response){

        return new WhatsappMessagePayloadDTO(
                response.number(),
                response.mediatype(),
                response.mimetype(),
                response.media(),
                response.caption()
        );
    }
}
