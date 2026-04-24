package com.procurement.service.impl;

import com.procurement.dto.responce.ApiResponse;
import com.procurement.dto.responce.LandingPageResponse;
import com.procurement.entity.*;
import com.procurement.repository.*;
import com.procurement.service.LandingPageService;
import com.procurement.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LandingPageServiceImpl implements LandingPageService {

    private final TenderHeaderRepository tenderRepository;
    private final VendorRepository vendorRepository;
    private final ContractRepository contractRepository;
    private final BidTechnicalRepository bidTechnicalRepository;

    @Override
    public ResponseEntity<ApiResponse<LandingPageResponse>> getLandingPageData() {
        log.info("Fetching landing page data (public)");

        LandingPageResponse response = new LandingPageResponse();

        // 1. Stats
        response.setStats(getStats());

        // 2. Latest Tenders (PUBLISHED, limit 20)
        response.setLatestTenders(getLatestTenders());

        // 3. Submitted Bids Statistics (last 12 months)
        response.setSubmittedBids(getSubmittedBidsStats());

        // 4. Tenders Awarded (last 20)
        response.setTendersAwarded(getTendersAwarded());

        // 5. News & Events (dynamic based on recent activities)
        response.setNewsEvents(getNewsEvents());

        // 6. Projects Overview
        response.setProjectsOverview(getProjectsOverview());

        return ResponseUtil.success(response, "Landing page data retrieved");
    }

    private LandingPageResponse.Stats getStats() {
        LandingPageResponse.Stats stats = new LandingPageResponse.Stats();

        // Total Tenders (all)
        stats.setTotalTenders(tenderRepository.count());

        // Open Tenders (PUBLISHED with bid end date >= today)
        long openTenders = tenderRepository.findAll().stream()
                .filter(t -> "PUBLISHED".equals(t.getTenderStatus()) &&
                        t.getBidEndDate() != null &&
                        !t.getBidEndDate().isBefore(LocalDate.now()))
                .count();
        stats.setOpenTenders(openTenders);

        // Registered Vendors
        stats.setRegisteredVendors(vendorRepository.count());

        // Contracts Awarded
        long awardedContracts = contractRepository.findAll().stream()
                .filter(c -> "AWARDED".equals(c.getStatus()))
                .count();
        stats.setContractsAwarded(awardedContracts);

        // Total Procurement Value
        BigDecimal totalValue = contractRepository.findAll().stream()
                .map(Contract::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setTotalProcurementValue(totalValue);

        return stats;
    }

    private List<LandingPageResponse.TenderData> getLatestTenders() {
        List<LandingPageResponse.TenderData> tenders = new ArrayList<>();

        List<TenderHeader> latestTenders = tenderRepository.findAll().stream()
                .filter(t -> "PUBLISHED".equals(t.getTenderStatus()))
                .sorted(Comparator.comparing(TenderHeader::getPublishDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(20)
                .collect(Collectors.toList());

        for (TenderHeader tender : latestTenders) {
            LandingPageResponse.TenderData data = new LandingPageResponse.TenderData();
            data.setTenderId(tender.getTenderNo());
            data.setTitle(tender.getTenderTitle());
            data.setDepartment(tender.getDepartment() != null ? tender.getDepartment() : "General");
            data.setDeadline(tender.getBidEndDate() != null ? tender.getBidEndDate().toString() : "-");

            // Determine status
            if (tender.getBidEndDate() != null) {
                if (tender.getBidEndDate().isBefore(LocalDate.now())) {
                    data.setStatus("Closed");
                } else {
                    data.setStatus("Open");
                }
            } else {
                data.setStatus("Open");
            }

            data.setLocation("Pan India");
            tenders.add(data);
        }

        // If not enough real data, add sample
        if (tenders.isEmpty()) {
            tenders.addAll(getSampleTenders());
        }

        return tenders;
    }

    private List<LandingPageResponse.TenderData> getSampleTenders() {
        List<LandingPageResponse.TenderData> samples = new ArrayList<>();
        String[] titles = {"Solar Panel Installation", "Biometric Attendance System", "ERP Implementation Services",
                "Warehouse Management Setup", "Diesel Generator Supply", "Mobile App Development",
                "Access Control System", "Annual Housekeeping Contract", "E-Waste Disposal Services",
                "Vehicle Fleet Management System", "AI-Based Analytics Platform", "Water Purification Systems"};
        String[] depts = {"Energy", "HR", "IT", "Logistics", "Facilities", "IT", "Security", "Admin", "Environment", "Transport", "IT", "Facilities"};

        for (int i = 0; i < Math.min(titles.length, 12); i++) {
            LandingPageResponse.TenderData data = new LandingPageResponse.TenderData();
            data.setTenderId("EP/2026/" + (111 + i));
            data.setTitle(titles[i]);
            data.setDepartment(depts[i]);
            data.setDeadline(LocalDate.now().plusDays(15 + i).toString());
            data.setStatus(i % 3 == 0 ? "Closing" : "Open");
            data.setLocation(getLocationByIndex(i));
            samples.add(data);
        }
        return samples;
    }

    private String getLocationByIndex(int i) {
        String[] locations = {"Delhi NCR", "Mumbai", "Bangalore", "Chennai", "Kolkata", "Hyderabad", "Pune", "Ahmedabad"};
        return locations[i % locations.length];
    }

    private List<LandingPageResponse.BidStats> getSubmittedBidsStats() {
        List<LandingPageResponse.BidStats> stats = new ArrayList<>();

        // Get bids grouped by month
        Map<String, List<BidTechnical>> bidsByMonth = bidTechnicalRepository.findAll().stream()
                .filter(b -> b.getSubmittedAt() != null && "SUBMITTED".equals(b.getSubmissionStatus()))
                .collect(Collectors.groupingBy(b ->
                        b.getSubmittedAt().format(DateTimeFormatter.ofPattern("MMM yyyy"))));

        // Get approvals by month (using evaluatedAt for QUALIFIED)
        Map<String, Long> approvedByMonth = bidTechnicalRepository.findAll().stream()
                .filter(b -> b.getEvaluatedAt() != null && "QUALIFIED".equals(b.getEvaluationStatus()))
                .collect(Collectors.groupingBy(b ->
                                b.getEvaluatedAt().format(DateTimeFormatter.ofPattern("MMM yyyy")),
                        Collectors.counting()));

        // Get rejections by month
        Map<String, Long> rejectedByMonth = bidTechnicalRepository.findAll().stream()
                .filter(b -> b.getEvaluatedAt() != null && "DISQUALIFIED".equals(b.getEvaluationStatus()))
                .collect(Collectors.groupingBy(b ->
                                b.getEvaluatedAt().format(DateTimeFormatter.ofPattern("MMM yyyy")),
                        Collectors.counting()));

        // Get last 12 months
        LocalDate now = LocalDate.now();
        for (int i = 11; i >= 0; i--) {
            LocalDate monthDate = now.minusMonths(i);
            String monthKey = monthDate.format(DateTimeFormatter.ofPattern("MMM yyyy"));

            LandingPageResponse.BidStats stat = new LandingPageResponse.BidStats();
            stat.setMonth(monthKey);
            stat.setSubmitted((long) bidsByMonth.getOrDefault(monthKey, new ArrayList<>()).size());
            stat.setApproved(approvedByMonth.getOrDefault(monthKey, 0L));
            stat.setRejected(rejectedByMonth.getOrDefault(monthKey, 0L));
            stats.add(stat);
        }

        // If no data, return sample
        if (stats.stream().allMatch(s -> s.getSubmitted() == 0)) {
            return getSampleBidStats();
        }

        return stats;
    }

    private List<LandingPageResponse.BidStats> getSampleBidStats() {
        List<LandingPageResponse.BidStats> samples = new ArrayList<>();
        String[] months = {"Jan 2026", "Feb 2026", "Mar 2026", "Apr 2026", "May 2026", "Jun 2026",
                "Jul 2026", "Aug 2026", "Sep 2026", "Oct 2026", "Nov 2026", "Dec 2026"};
        int[] submitted = {20, 22, 25, 10, 14, 19, 23, 26, 28, 30, 32, 35};
        int[] approved = {15, 16, 18, 7, 9, 13, 17, 19, 21, 23, 25, 28};
        int[] rejected = {5, 6, 7, 3, 5, 6, 6, 7, 7, 7, 7, 7};

        for (int i = 0; i < months.length; i++) {
            LandingPageResponse.BidStats stat = new LandingPageResponse.BidStats();
            stat.setMonth(months[i]);
            stat.setSubmitted((long) submitted[i]);
            stat.setApproved((long) approved[i]);
            stat.setRejected((long) rejected[i]);
            samples.add(stat);
        }
        return samples;
    }

    private List<String> getTendersAwarded() {
        List<String> awardedTenders = new ArrayList<>();

        List<Contract> contracts = contractRepository.findAll().stream()
                .filter(c -> "AWARDED".equals(c.getStatus()))
                .sorted(Comparator.comparing(Contract::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(20)
                .collect(Collectors.toList());

        for (Contract contract : contracts) {
            awardedTenders.add(contract.getTenderNo() + " - " + contract.getTenderTitle());
        }

        // If not enough, add samples
        if (awardedTenders.size() < 10) {
            String[] samples = {
                    "EP/2025/089 - HVAC Maintenance Contract",
                    "EP/2025/090 - Electrical Panel Upgrade",
                    "EP/2025/091 - Office Interior Renovation",
                    "EP/2025/092 - Data Backup Solution",
                    "EP/2025/093 - Firewall Implementation",
                    "EP/2025/094 - Video Conferencing Setup",
                    "EP/2025/095 - Printer & Copier Supply"
            };
            for (String sample : samples) {
                if (!awardedTenders.contains(sample)) {
                    awardedTenders.add(sample);
                }
            }
        }

        return awardedTenders;
    }

    private List<String> getNewsEvents() {
        List<String> news = new ArrayList<>();

        // Dynamic news based on recent activities
        long todayTenders = tenderRepository.findAll().stream()
                .filter(t -> t.getPublishDate() != null && t.getPublishDate().equals(LocalDate.now()))
                .count();

        if (todayTenders > 0) {
            news.add(todayTenders + " new tender(s) published today");
        }

        long expiringTenders = tenderRepository.findAll().stream()
                .filter(t -> t.getBidEndDate() != null &&
                        t.getBidEndDate().equals(LocalDate.now().plusDays(3)))
                .count();

        if (expiringTenders > 0) {
            news.add(expiringTenders + " tender(s) closing in 3 days");
        }

        // Add standard news
        news.add("Portal is fully operational");
        news.add("New KYC guidelines released");
        news.add("Vendor registration fee waived for MSME");

        return news;
    }

    private List<String> getProjectsOverview() {
        List<String> projects = new ArrayList<>();

        // Get active contracts as projects
        List<Contract> activeContracts = contractRepository.findAll().stream()
                .filter(c -> "AWARDED".equals(c.getStatus()) &&
                        c.getEndDate() != null &&
                        !c.getEndDate().isBefore(LocalDate.now()))
                .limit(5)
                .collect(Collectors.toList());

        for (Contract contract : activeContracts) {
            projects.add(contract.getTenderTitle() + " - Active");
        }

        // Add completed projects
        List<Contract> completedContracts = contractRepository.findAll().stream()
                .filter(c -> "COMPLETED".equals(c.getStatus()) ||
                        (c.getEndDate() != null && c.getEndDate().isBefore(LocalDate.now())))
                .limit(3)
                .collect(Collectors.toList());

        for (Contract contract : completedContracts) {
            projects.add(contract.getTenderTitle() + " - Completed");
        }

        // If no real data, add samples
        if (projects.isEmpty()) {
            String[] samples = {
                    "ERP Implementation - Active",
                    "Data Center Setup - Completed",
                    "Network Security Upgrade - Active",
                    "Office Renovation - Completed",
                    "Cloud Migration - Active"
            };
            projects.addAll(Arrays.asList(samples));
        }

        return projects;
    }
}