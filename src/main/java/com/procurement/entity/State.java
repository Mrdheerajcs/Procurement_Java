package com.procurement.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "mas_state")
@Getter
@Setter
public class State {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "state_id")
    private Long stateId;
    @Column(name = "state_name", nullable = false)
    private String stateName;
    @Column(name = "state_code")
    private String stateCode;
    // 🔗 Many States → One Country
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}