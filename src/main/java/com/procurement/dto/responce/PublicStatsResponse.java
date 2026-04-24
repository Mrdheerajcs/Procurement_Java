package com.procurement.dto.responce;

import lombok.Data;

@Data
public
class PublicStatsResponse {
    private Long totalTenders;
    private Long openTenders;
    private Long registeredVendors;
    private Long contractsAwarded;
    // getters and setters
    public Long getTotalTenders() { return totalTenders; }
    public void setTotalTenders(Long totalTenders) { this.totalTenders = totalTenders; }
    public Long getOpenTenders() { return openTenders; }
    public void setOpenTenders(Long openTenders) { this.openTenders = openTenders; }
    public Long getRegisteredVendors() { return registeredVendors; }
    public void setRegisteredVendors(Long registeredVendors) { this.registeredVendors = registeredVendors; }
    public Long getContractsAwarded() { return contractsAwarded; }
    public void setContractsAwarded(Long contractsAwarded) { this.contractsAwarded = contractsAwarded; }
}