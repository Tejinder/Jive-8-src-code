package com.grail.synchro.dao;

import java.util.List;

import com.grail.synchro.beans.EndMarketInvestmentDetail;

/**
 * @author Kanwar Grewal
 * @version 4.0, Date 12/3/2013
 */

public interface EndMarketDetailDAO {

	EndMarketInvestmentDetail save(final EndMarketInvestmentDetail endMarketInvestmentDetail);

	EndMarketInvestmentDetail update(final EndMarketInvestmentDetail endMarketInvestmentDetail);

	EndMarketInvestmentDetail get(final Long projectID, final Long endMarketID);

    List<EndMarketInvestmentDetail> getProjectEndMarkets(final Long projectID);

    List<Long> getProjectEndMarketIds(final Long projectID);

    void delete(final Long projectID, final Long endMarketID);
    
    void delete(final Long projectID);
    void updateSingleEndMarketId(final Long projectID, final Long endMarketID);
    void updateInitialCost(final EndMarketInvestmentDetail endMarketInvestmentDetail);
    void updateSPIContact(final Long projectID,final Long endMarketID, final Long spiContact);
    
    Integer getEndMarketStatus(final Long projectID, final Long endmarketID);
    void deleteEndMarketStatus(final Long projectID, final List<Long> endmarketIDs);
    void deleteEndMarketStatus(final Long projectID, final Long endmarketID);
    void setEndMarketStatus(final Long projectID, final List<Long> endmarketIDs, final Integer status);
    void setEndMarketStatus(final Long projectID, final Long endmarketID, final Integer status);
    void updateInitialCostSM(final EndMarketInvestmentDetail endMarketInvestmentDetail);
   // void setAllEndMarketStatus(final Long projectID, final Long status);
    
    void updateReferenceEndMarkets(final EndMarketInvestmentDetail endMarketInvestmentDetail);
    void updateFundingEndMarkets(final EndMarketInvestmentDetail endMarketInvestmentDetail);

}
