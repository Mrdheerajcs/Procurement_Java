package com.procurement.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "publish_tender_documents")
@Data
public class TenderDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long documentId;
    private Long tenderId;
    private String fileName;
    private String filePath;
    private String fileType;
    private String docCategory;
}