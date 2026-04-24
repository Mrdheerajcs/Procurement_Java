package com.procurement.service.impl;

import com.procurement.dto.responce.ApiResponse;
import com.procurement.dto.responce.DashboardResponse;
import com.procurement.entity.*;
import com.procurement.helper.CurrentUser;
import com.procurement.repository.*;
import com.procurement.service.DashboardService;
import com.procurement.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final TenderHeaderRepository tenderRepository;
    private final MprRepository mprRepository;
    private final BidTechnicalRepository bidTechnicalRepository;
    private final ContractRepository contractRepository;
    private final VendorRepository vendorRepository;
    private final BidFinancialRepository bidFinancialRepository;

    @Override
    public ResponseEntity<ApiResponse<DashboardResponse>> getAdminDashboard() {
        log.info("Fetching admin dashboard data");
        DashboardResponse response = new DashboardResponse();
        response.setAdminStats(getAdminStats());
        response.setPipeline(getPipelineData());
        response.setDepartmentWise(getDepartmentWiseData());
        response.setRecentTenders(getRecentTenders());
        response.setUpcomingDeadlines(getUpcomingDeadlines());
        response.setRecentActivities(getRecentActivities());
        return ResponseUtil.success(response, "Dashboard data retrieved");
    }

    @Override
    public ResponseEntity<ApiResponse<DashboardResponse>> getVendorDashboard() {
        log.info("Fetching vendor dashboard data");
        Vendor vendor = getCurrentVendor();
        DashboardResponse response = new DashboardResponse();
        response.setVendorStats(getVendorStats(vendor));
        response.setQuickActions(getQuickActions());
        response.setRecentActivities(getVendorRecentActivities(vendor));
        return ResponseUtil.success(response, "Vendor dashboard data retrieved");
    }

    // ========== PRIVATE METHODS ==========

    private DashboardResponse.AdminStats getAdminStats() {
        DashboardResponse.AdminStats stats = new DashboardResponse.AdminStats();

        LocalDate now = LocalDate.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();

        // Total Tenders
        stats.setTotalTenders(tenderRepository.count());

        // Tenders published this month
        long publishedThisMonth = tenderRepository.countTendersPublishedInMonth(currentMonth, currentYear);
        stats.setPublishedThisMonth(publishedThisMonth);

        // Active MPRs (status 'n' = pending approval)
        long activeMprs = mprRepository.findAll().stream()
                .filter(m -> "n".equals(m.getStatus()) || "PENDING".equals(m.getApprovalStatus()))
                .count();
        stats.setActiveMprs(activeMprs);
        stats.setPendingApprovals(activeMprs);

        // Total Bids (SUBMITTED)
        long totalBids = bidTechnicalRepository.findAll().stream()
                .filter(b -> "SUBMITTED".equals(b.getSubmissionStatus()))
                .count();
        stats.setTotalBids(totalBids);

        // Pending Evaluations (PENDING + CLARIFICATION_NEEDED)
        long pendingEval = bidTechnicalRepository.findAll().stream()
                .filter(b -> "SUBMITTED".equals(b.getSubmissionStatus()) &&
                        ("PENDING".equals(b.getEvaluationStatus()) ||
                                "CLARIFICATION_NEEDED".equals(b.getEvaluationStatus())))
                .count();
        stats.setPendingEvaluations(pendingEval);

        // Purchase Orders (Contracts awarded)
        long purchaseOrders = contractRepository.countAwardedContracts();
        BigDecimal totalPurchaseValue = contractRepository.sumAwardedAmount();
        stats.setPurchaseOrders(purchaseOrders);
        stats.setTotalPurchaseValue(totalPurchaseValue);

        // ✅ REMOVED Inventory Items - Replaced with Qualified Vendors
        long qualifiedVendors = bidTechnicalRepository.findAll().stream()
                .filter(b -> "QUALIFIED".equals(b.getEvaluationStatus()))
                .map(BidTechnical::getVendor)
                .distinct()
                .count();
        stats.setQualifiedVendors(qualifiedVendors);

        // Active Contracts
        long activeContracts = contractRepository.findAll().stream()
                .filter(c -> "AWARDED".equals(c.getStatus()) && c.getEndDate() != null &&
                        !c.getEndDate().isBefore(LocalDate.now()))
                .count();
        stats.setActiveContracts(activeContracts);

        // Contracts ending this quarter
        long contractsEndingSoon = contractRepository.countContractsEndingInNext90Days();
        stats.setContractsEndingSoon(contractsEndingSoon);

        return stats;
    }

    private List<DashboardResponse.PipelineData> getPipelineData() {
        List<DashboardResponse.PipelineData> pipeline = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // Draft (PENDING_APPROVAL)
        long draft = tenderRepository.findByTenderStatus("PENDING_APPROVAL").size();

        // Published
        long published = tenderRepository.findByTenderStatus("PUBLISHED").size();

        // Bid Open = PUBLISHED with bid end date > today
        long bidOpen = tenderRepository.findAll().stream()
                .filter(t -> "PUBLISHED".equals(t.getTenderStatus()) &&
                        t.getBidEndDate() != null &&
                        !t.getBidEndDate().isBefore(today))
                .count();

        // Evaluation = tenders with bids submitted
        long evaluation = tenderRepository.findAll().stream()
                .filter(t -> bidTechnicalRepository.findByTender(t).stream()
                        .anyMatch(b -> "SUBMITTED".equals(b.getSubmissionStatus())))
                .count();

        // Awarded
        long awarded = tenderRepository.findByTenderStatus("AWARDED").size();

        // ✅ Closed = tenders where bidEndDate < today
        long closed = tenderRepository.findAll().stream()
                .filter(t -> t.getBidEndDate() != null && t.getBidEndDate().isBefore(today))
                .count();

        pipeline.add(createPipeline("Draft", draft, "#94a3b8"));
        pipeline.add(createPipeline("Published", published, "#3b82f6"));
        pipeline.add(createPipeline("Bid Open", bidOpen, "#f59e0b"));
        pipeline.add(createPipeline("Evaluation", evaluation, "#8b5cf6"));
        pipeline.add(createPipeline("Awarded", awarded, "#10b981"));
        pipeline.add(createPipeline("Closed", closed, "#6b7280"));

        return pipeline;
    }

    private DashboardResponse.PipelineData createPipeline(String label, long count, String color) {
        DashboardResponse.PipelineData data = new DashboardResponse.PipelineData();
        data.setLabel(label);
        data.setCount(count);
        data.setColor(color);
        return data;
    }

    private List<DashboardResponse.DepartmentData> getDepartmentWiseData() {
        List<DashboardResponse.DepartmentData> deptData = new ArrayList<>();

        Map<String, Long> deptCount = tenderRepository.findAll().stream()
                .filter(t -> t.getDepartment() != null && t.getDepartment().trim().length() > 0)
                .collect(Collectors.groupingBy(
                        TenderHeader::getDepartment,
                        Collectors.counting()
                ));

        long total = deptCount.values().stream().mapToLong(Long::longValue).sum();

        if (total == 0) {
            deptData.add(createDepartment("PWD", 35.0, 4L, "#3b82f6"));
            deptData.add(createDepartment("Health", 25.0, 3L, "#10b981"));
            deptData.add(createDepartment("IT", 20.0, 2L, "#06b6d4"));
            deptData.add(createDepartment("Admin", 12.0, 2L, "#f59e0b"));
            deptData.add(createDepartment("Finance", 8.0, 1L, "#8b5cf6"));
        } else {
            for (Map.Entry<String, Long> entry : deptCount.entrySet()) {
                double percentage = (entry.getValue() * 100.0) / total;
                deptData.add(createDepartment(
                        entry.getKey(),
                        percentage,
                        entry.getValue(),
                        getColorForDept(entry.getKey())
                ));
            }
        }
        return deptData;
    }

    private DashboardResponse.DepartmentData createDepartment(String dept, double pct, long count, String color) {
        DashboardResponse.DepartmentData data = new DashboardResponse.DepartmentData();
        data.setDept(dept);
        data.setPercentage(pct);
        data.setTenders(count);
        data.setColor(color);
        return data;
    }

    private String getColorForDept(String dept) {
        switch (dept.toLowerCase()) {
            case "it department": return "#3b82f6";
            case "hr department": return "#10b981";
            case "finance": return "#f59e0b";
            case "pwd": return "#8b5cf6";
            default: return "#06b6d4";
        }
    }

    private List<DashboardResponse.TenderData> getRecentTenders() {
        List<DashboardResponse.TenderData> recentTenders = new ArrayList<>();

        List<TenderHeader> tenders = tenderRepository.findAll().stream()
                .sorted(Comparator.comparing(TenderHeader::getLastUpdatedDt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(8)
                .collect(Collectors.toList());

        for (TenderHeader tender : tenders) {
            DashboardResponse.TenderData data = new DashboardResponse.TenderData();
            data.setTenderId(tender.getTenderId());
            data.setTenderNo(tender.getTenderNo());
            data.setTitle(tender.getTenderTitle());
            data.setPublishedDate(tender.getPublishDate() != null ? tender.getPublishDate().toString() : "-");
            data.setDeadline(tender.getBidEndDate() != null ? tender.getBidEndDate().toString() : "-");
            data.setStatus(tender.getTenderStatus());
            data.setStatusClass(getStatusClass(tender.getTenderStatus()));

            long bidCount = bidTechnicalRepository.findByTender(tender).stream()
                    .filter(b -> "SUBMITTED".equals(b.getSubmissionStatus()))
                    .count();
            data.setBids(bidCount);

            recentTenders.add(data);
        }
        return recentTenders;
    }

    private String getStatusClass(String status) {
        switch (status) {
            case "PUBLISHED": return "ds-badge-success";
            case "EVALUATION": return "ds-badge-info";
            case "PENDING_APPROVAL": return "ds-badge-warning";
            case "APPROVED": return "ds-badge-success";
            case "AWARDED": return "ds-badge-purple";
            case "CLOSED": return "ds-badge-muted";
            default: return "ds-badge-secondary";
        }
    }

    private List<DashboardResponse.DeadlineData> getUpcomingDeadlines() {
        List<DashboardResponse.DeadlineData> deadlines = new ArrayList<>();
        LocalDate today = LocalDate.now();

        List<TenderHeader> tenders = tenderRepository.findAll().stream()
                .filter(t -> t.getBidEndDate() != null && !t.getBidEndDate().isBefore(today))
                .sorted(Comparator.comparing(TenderHeader::getBidEndDate))
                .limit(5)
                .collect(Collectors.toList());

        for (TenderHeader tender : tenders) {
            DashboardResponse.DeadlineData data = new DashboardResponse.DeadlineData();
            data.setId(tender.getTenderNo());
            data.setEvent("Bid Deadline");
            data.setDate(tender.getBidEndDate().toString());

            long daysLeft = ChronoUnit.DAYS.between(today, tender.getBidEndDate());
            data.setLeft((int) daysLeft);
            data.setUrgency(daysLeft <= 3 ? "high" : daysLeft <= 7 ? "medium" : "low");

            deadlines.add(data);
        }
        return deadlines;
    }

    private List<DashboardResponse.ActivityData> getRecentActivities() {
        List<DashboardResponse.ActivityData> activities = new ArrayList<>();

        // Recent approved tenders
        List<TenderHeader> recentTenders = tenderRepository.findAll().stream()
                .filter(t -> t.getApprovedAt() != null)
                .sorted(Comparator.comparing(TenderHeader::getApprovedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(3)
                .collect(Collectors.toList());

        for (TenderHeader tender : recentTenders) {
            DashboardResponse.ActivityData data = new DashboardResponse.ActivityData();
            data.setIcon("bi-check2-circle");
            data.setColor("#10b981");
            data.setText("Tender approved: " + tender.getTenderNo());
            data.setTime(getTimeAgo(tender.getApprovedAt()));
            activities.add(data);
        }

        // Recent bid submissions
        List<BidTechnical> recentBids = bidTechnicalRepository.findAll().stream()
                .filter(b -> b.getSubmittedAt() != null)
                .sorted(Comparator.comparing(BidTechnical::getSubmittedAt, Comparator.reverseOrder()))
                .limit(3)
                .collect(Collectors.toList());

        for (BidTechnical bid : recentBids) {
            DashboardResponse.ActivityData data = new DashboardResponse.ActivityData();
            data.setIcon("bi-send");
            data.setColor("#06b6d4");
            data.setText("Bid submitted for " + bid.getTender().getTenderNo() + " by " + bid.getVendor().getVendorName());
            data.setTime(getTimeAgo(bid.getSubmittedAt()));
            activities.add(data);
        }

        return activities;
    }

    private List<DashboardResponse.ActivityData> getVendorRecentActivities(Vendor vendor) {
        List<DashboardResponse.ActivityData> activities = new ArrayList<>();

        List<BidTechnical> bids = bidTechnicalRepository.findByVendor(vendor).stream()
                .sorted(Comparator.comparing(BidTechnical::getSubmittedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(3)
                .collect(Collectors.toList());

        for (BidTechnical bid : bids) {
            DashboardResponse.ActivityData data = new DashboardResponse.ActivityData();
            data.setIcon("bi-send");
            data.setColor("#06b6d4");
            data.setText("Bid submitted for " + bid.getTender().getTenderNo());
            data.setTime(getTimeAgo(bid.getSubmittedAt()));
            activities.add(data);
        }

        List<Contract> contracts = contractRepository.findByVendorId(vendor.getVendorId()).stream()
                .sorted(Comparator.comparing(Contract::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(2)
                .collect(Collectors.toList());

        for (Contract contract : contracts) {
            DashboardResponse.ActivityData data = new DashboardResponse.ActivityData();
            data.setIcon("bi-trophy");
            data.setColor("#10b981");
            data.setText("Contract awarded for " + contract.getTenderNo());
            data.setTime(getTimeAgo(contract.getCreatedAt()));
            activities.add(data);
        }

        return activities;
    }

    private DashboardResponse.ActivityData createActivity(String icon, String color, String text, String time) {
        DashboardResponse.ActivityData data = new DashboardResponse.ActivityData();
        data.setIcon(icon);
        data.setColor(color);
        data.setText(text);
        data.setTime(time);
        return data;
    }

    private String getTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) return "Recently";
        long hours = ChronoUnit.HOURS.between(dateTime, LocalDateTime.now());
        if (hours < 1) return "Just now";
        if (hours < 24) return hours + " hours ago";
        long days = ChronoUnit.DAYS.between(dateTime, LocalDateTime.now());
        if (days < 7) return days + " days ago";
        if (days < 30) return (days / 7) + " weeks ago";
        return dateTime.toLocalDate().toString();
    }

    private DashboardResponse.VendorStats getVendorStats(Vendor vendor) {
        DashboardResponse.VendorStats stats = new DashboardResponse.VendorStats();

        long openTenders = tenderRepository.findAll().stream()
                .filter(t -> "PUBLISHED".equals(t.getTenderStatus()) &&
                        t.getBidEndDate() != null &&
                        !t.getBidEndDate().isBefore(LocalDate.now()))
                .count();
        stats.setOpenTenders(openTenders);

        long myBids = bidTechnicalRepository.findByVendor(vendor).stream()
                .filter(b -> "SUBMITTED".equals(b.getSubmissionStatus()))
                .count();
        stats.setMyBids(myBids);

        long pendingEval = bidTechnicalRepository.findByVendor(vendor).stream()
                .filter(b -> "SUBMITTED".equals(b.getSubmissionStatus()) &&
                        ("PENDING".equals(b.getEvaluationStatus()) || "CLARIFICATION_NEEDED".equals(b.getEvaluationStatus())))
                .count();
        stats.setPendingEvaluations(pendingEval);

        List<Contract> contracts = contractRepository.findByVendorId(vendor.getVendorId()).stream()
                .filter(c -> "AWARDED".equals(c.getStatus()))
                .collect(Collectors.toList());
        stats.setWonContracts((long) contracts.size());

        BigDecimal totalValue = contracts.stream()
                .map(Contract::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setTotalContractValue(totalValue);

        long pendingClarifications = bidTechnicalRepository.findByVendorAndEvaluationStatus(vendor, "CLARIFICATION_NEEDED").size();
        stats.setPendingClarifications(pendingClarifications);

        return stats;
    }

    private List<DashboardResponse.QuickAction> getQuickActions() {
        List<DashboardResponse.QuickAction> actions = new ArrayList<>();
        actions.add(createQuickAction("bi-search", "Browse Tenders", "Find and participate in open tenders", "/searchtender", "Search Tenders →"));
        actions.add(createQuickAction("bi-send", "Submit Bids", "Submit technical and financial bids", "/bid-submission", "Submit Bid →"));
        actions.add(createQuickAction("bi-file-contract", "My Contracts", "View awarded contracts", "/vendor-contracts", "View Contracts →"));
        return actions;
    }

    private DashboardResponse.QuickAction createQuickAction(String icon, String title, String desc, String link, String btnText) {
        DashboardResponse.QuickAction action = new DashboardResponse.QuickAction();
        action.setIcon(icon);
        action.setTitle(title);
        action.setDescription(desc);
        action.setLink(link);
        action.setButtonText(btnText);
        return action;
    }

    private Vendor getCurrentVendor() {
        String username = CurrentUser.getCurrentUserOrThrow().getUsername();
        return vendorRepository.findByEmailId(username)
                .orElseThrow(() -> new RuntimeException("Vendor not found for user: " + username));
    }
}