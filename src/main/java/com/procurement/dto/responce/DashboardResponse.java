package com.procurement.dto.responce;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class DashboardResponse {

    // Admin Dashboard Data
    private AdminStats adminStats;
    private List<PipelineData> pipeline;
    private List<DepartmentData> departmentWise;
    private List<TenderData> recentTenders;
    private List<DeadlineData> upcomingDeadlines;
    private List<ActivityData> recentActivities;

    // Vendor Dashboard Data
    private VendorStats vendorStats;
    private List<QuickAction> quickActions;

    @Data
    public static class AdminStats {
        private Long totalTenders;
        private Long publishedThisMonth;  // ✅ NEW
        private Long activeMprs;
        private Long totalBids;
        private Long purchaseOrders;
        private Long qualifiedVendors;    // ✅ REPLACED inventoryItems
        private Long activeContracts;
        private Long pendingApprovals;
        private Long pendingEvaluations;
        private BigDecimal totalPurchaseValue;
        private Long contractsEndingSoon;
    }

    @Data
    public static class PipelineData {
        private String label;
        private Long count;
        private String color;
    }

    @Data
    public static class DepartmentData {
        private String dept;
        private Double percentage;
        private Long tenders;
        private String color;
    }

    @Data
    public static class TenderData {
        private Long tenderId;
        private String tenderNo;
        private String title;
        private String publishedDate;
        private String deadline;
        private String status;
        private String statusClass;
        private Long bids;
    }

    @Data
    public static class DeadlineData {
        private String id;
        private String event;
        private String date;
        private Integer left;
        private String urgency;
    }

    @Data
    public static class ActivityData {
        private String icon;
        private String color;
        private String text;
        private String time;
    }

    @Data
    public static class VendorStats {
        private Long openTenders;
        private Long myBids;
        private Long pendingEvaluations;  // ✅ NEW
        private Long wonContracts;
        private BigDecimal totalContractValue;
        private Long pendingClarifications;
    }

    @Data
    public static class QuickAction {
        private String icon;
        private String title;
        private String description;
        private String link;
        private String buttonText;
    }
}