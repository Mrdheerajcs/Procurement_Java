package com.procurement.dto.responce;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class LandingPageResponse {

    private Stats stats;
    private List<TenderData> latestTenders;
    private List<BidStats> submittedBids;
    private List<String> tendersAwarded;
    private List<String> newsEvents;
    private List<String> projectsOverview;

    @Data
    public static class Stats {
        private Long totalTenders;
        private Long openTenders;
        private Long registeredVendors;
        private Long contractsAwarded;
        private BigDecimal totalProcurementValue;
    }

    @Data
    public static class TenderData {
        private String tenderId;
        private String title;
        private String department;
        private String deadline;
        private String status;
        private String location;
    }

    @Data
    public static class BidStats {
        private String month;
        private Long submitted;
        private Long approved;
        private Long rejected;
    }
}