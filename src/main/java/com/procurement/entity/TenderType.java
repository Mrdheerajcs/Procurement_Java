package com.procurement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "mas_tender_type")
@Getter
@Setter
public class TenderType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tenderTypeId;

    private String tenderCode;
    private String tenderName;
}