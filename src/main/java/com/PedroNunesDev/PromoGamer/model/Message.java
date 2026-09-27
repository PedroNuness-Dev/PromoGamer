package com.PedroNunesDev.PromoGamer.model;

import com.PedroNunesDev.PromoGamer.enums.DealSourceType;
import com.PedroNunesDev.PromoGamer.enums.MessageStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long messageId;

    @OneToOne
    @JoinColumn(name = "deal_id")
    private Deal deal;

    @Column(name = "send_date")
    private LocalDateTime sendAt;

    @CreationTimestamp
    private LocalDateTime creationAt;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private MessageStatus messageStatus = MessageStatus.PENDENTE;

    @Enumerated(EnumType.STRING)
    private DealSourceType sourceType;

    private String number;
    private String mediatype;
    private String mimetype;
    private String media;
    private String caption;

    public void markAsSent(){
        messageStatus = MessageStatus.ENVIADA;
        sendAt = LocalDateTime.now();
    }

    public void markAsFailed() {
        this.messageStatus = MessageStatus.FALHA;
    }
}
