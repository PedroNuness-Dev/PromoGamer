package com.PedroNunesDev.PromoGamer.model;

import com.PedroNunesDev.PromoGamer.enums.DealEnumStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Persistable;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Deal implements Persistable<String>,Serializable {

    @Id
    @Column(name = "deal_id", unique = true)
    private String dealId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, name = "steam_app_id")
    private String steamAppId;

    @OneToOne(mappedBy = "deal", cascade = CascadeType.ALL)
    private Message message;

    @Column(name = "steam_rating_percent")
    private String steamRatingPercent;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private DealEnumStatus dealEnumStatus;

    @CreationTimestamp
    @Column(name = "creation_date", nullable = false)
    private LocalDateTime creationDate;

    @UpdateTimestamp
    @Column(name = "update_date", nullable = false)
    private LocalDateTime updateDate;

    @Transient
    @Builder.Default
    private boolean isNew = true;

    @Override
    public @Nullable String getId() {
        return this.dealId;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    public void updateStatus(DealEnumStatus dealEnumStatus){
        this.dealEnumStatus = dealEnumStatus;
    }
}