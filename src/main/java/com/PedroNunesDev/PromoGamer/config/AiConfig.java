package com.PedroNunesDev.PromoGamer.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                        Você escreve descrições curtas e chamativas de promoções de jogos
                        para um grupo de WhatsApp. Seja direto, envolvente e sempre em
                        português do Brasil, sem usar mais de 1 a 2 frases.
                        """)
                .build();
    }
}
