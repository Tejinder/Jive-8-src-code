package com.grail.synchro.scheduling.quartz;

import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.*;
import com.grail.synchro.exceptions.ProjectCostCaptureUpdateProcessException;
import com.grail.synchro.manager.*;
import com.grail.synchro.util.QuarterRangeUtil;
import com.grail.synchro.util.SynchroUtils;
import com.jivesoftware.base.UserManager;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.community.user.profile.ProfileFieldValue;
import com.jivesoftware.community.user.profile.ProfileManager;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 5/14/14
 * Time: 11:57 AM
 * To change this template use File | Settings | File Templates.
 */
public class ProjectCostsCaptureTask {

    private static Logger LOG = Logger.getLogger(ProjectCostsCaptureTask.class);

    private static ProjectCostsCaptureManager projectCostsCaptureManager;
    private static ProjectManager  synchroProjectManager;
    private static PIBManager pibManager;
    private static ProposalManager proposalManager;
    private static ProjectSpecsManager projectSpecsManager;
    private static UserManager userManager;
    private static ProfileManager profileManager;
    private static boolean updateInProgress = false;

    public void captureCosts() throws ProjectCostCaptureUpdateProcessException {
        if(!updateInProgress) {
            try {
                System.out.println("------Quartz Job Started ----");
            	LOG.debug("Quartz Job started running for Project Cost Fields Capture task on " + new Date());
                updateInProgress = true;
                Quarter quarter = QuarterRangeUtil.getCurrentQuarter();
                List<Project> projects = getProjectCostsCaptureManager().getProjects(quarter);


                for(Project project : projects) {
//                    if(project.getProjectID().intValue() != 1418113) {
//                        continue;
//                    }
                    List<ProjectCostsBean> projectCostsBeans = new ArrayList<ProjectCostsBean>();
                    List<CostInvestment> costInvestments = new ArrayList<CostInvestment>();

                    // Multimarket endmarketids
                    if(project.getMultiMarket()) {
                        List<FundingInvestment> fundingInvestments = getSynchroProjectManager().getProjectInvestments(project.getProjectID());
                        if(fundingInvestments != null && fundingInvestments.size() > 0) {
                            boolean containsAboveMarketDetails = false;
                            for(FundingInvestment fundingInvestment : fundingInvestments) {
                                CostInvestment bean = new CostInvestment();
                                bean.setAboveMarket(fundingInvestment.getAboveMarket());
                                bean.setInvestmentType(fundingInvestment.getInvestmentType().intValue());
                                if(fundingInvestment.getAboveMarket()) {
                                    bean.setSpiContact(project.getProjectOwner());
                                } else {
                                    bean.setSpiContact(fundingInvestment.getSpiContact());
                                }
                                bean.setProjectOwner(fundingInvestment.getProjectContact());
                                bean.setEstimatedCost(fundingInvestment.getEstimatedCost());
                                bean.setEstimatedCostCurrency(fundingInvestment.getEstimatedCostCurrency());
                                if(fundingInvestment.getAboveMarket()) {
                                    if(fundingInvestment.getInvestmentType().equals(SynchroGlobal.InvestmentType.GlOBAL.getId())
                                            || fundingInvestment.getInvestmentTypeID() == -1L) {
                                        bean.setEndmarketId(-100L);
                                    } else {
                                        bean.setEndmarketId(fundingInvestment.getInvestmentTypeID());
                                    }
                                    containsAboveMarketDetails = true;
                                } else {
                                    bean.setEndmarketId(fundingInvestment.getFieldworkMarketID());
                                }
                                costInvestments.add(bean);
                            }
                            if(!containsAboveMarketDetails) {
                                CostInvestment bean = new CostInvestment();
                                bean.setAboveMarket(true);
                                bean.setInvestmentType(SynchroGlobal.InvestmentType.GlOBAL.getId());
                                bean.setEstimatedCost(new BigDecimal(0));
                                bean.setEstimatedCostCurrency(-1L);
                                bean.setEndmarketId(-100L);
                                bean.setSpiContact(project.getProjectOwner());
                                bean.setProjectOwner(project.getProjectOwner());
                                costInvestments.add(bean);
                            }

                        }
                    } else { // Single market
                        List<EndMarketInvestmentDetail> endMarketInvestmentDetails = getSynchroProjectManager().getEndMarketDetails(project.getProjectID());
                        if(endMarketInvestmentDetails != null && endMarketInvestmentDetails.size() > 0) {
                            for(EndMarketInvestmentDetail detail:endMarketInvestmentDetails) {
                                CostInvestment bean = new CostInvestment();
                                bean.setAboveMarket(false);
                                bean.setInvestmentType(SynchroGlobal.InvestmentType.COUNTRY.getId());
                                bean.setEndmarketId(detail.getEndMarketID());
                                bean.setSpiContact(detail.getSpiContact());
                                bean.setProjectOwner(project.getProjectOwner());
                                bean.setEstimatedCost(detail.getInitialCost());
                                bean.setEstimatedCostCurrency(detail.getInitialCostCurrency());
                                costInvestments.add(bean);

                            }

                        }
                    }

                    for(CostInvestment costInvestment : costInvestments) {
                        BigDecimal totalProjectCost = new BigDecimal(0);
                        Long totalProjectCostCurrency = -1L;
                        BigDecimal latestProjectCost = new BigDecimal(0);
                        Long latestProjectCostCurrency = -1L;
                        Date projectStartDate = null;
                        Date projectEndDate = null;

                        ProjectCostsBean costsBean = new ProjectCostsBean();
                        costsBean.setProjectId(project.getProjectID());
                        costsBean.setEndmarketId(costInvestment.getEndmarketId());
                        costsBean.setMultiMarket(project.getMultiMarket());
                        costsBean.setAboveMarket(costInvestment.getAboveMarket());
                        costsBean.setInvestmentType(costInvestment.getInvestmentType().longValue());
                        costsBean.setSpiContact(costInvestment.getSpiContact());
                        costsBean.setProjectOwner(costInvestment.getProjectOwner());
                        projectStartDate = project.getStartDate();
                        projectEndDate = project.getEndDate();


                        //PIT
                        if(project.getMultiMarket()) {
                            totalProjectCost = project.getTotalCost();
                            totalProjectCostCurrency = project.getTotalCostCurrency() == null?-1L:project.getTotalCostCurrency();
                            latestProjectCost = costInvestment.getEstimatedCost();
                            latestProjectCostCurrency = costInvestment.getEstimatedCostCurrency() == null?-1L:costInvestment.getEstimatedCostCurrency();
                            costsBean.setEstimatedCost(totalProjectCost);
                            costsBean.setEstimatedCostCurrency(totalProjectCostCurrency);
                        } else {
                            totalProjectCost = latestProjectCost = costInvestment.getEstimatedCost();
                            totalProjectCostCurrency = latestProjectCostCurrency = costInvestment.getEstimatedCostCurrency() == null?-1L:costInvestment.getEstimatedCostCurrency();
                            costsBean.setEstimatedCost(latestProjectCost);
                            costsBean.setEstimatedCostCurrency(latestProjectCostCurrency);
                        }

                        //PIB
                        List<ProjectInitiation> projectInitiations =  null;
                        if(costInvestment.getAboveMarket()) {
                            projectInitiations = getPibManager().getPIBDetails(project.getProjectID(), -100L);
                        } else {
                            projectInitiations = getPibManager().getPIBDetails(project.getProjectID(), costInvestment.getEndmarketId());
                        }
                        if(projectInitiations != null && projectInitiations.size() > 0) {
                            ProjectInitiation projectInitiation = projectInitiations.get(0);

                            if(costInvestment.getInvestmentType().equals(SynchroGlobal.InvestmentType.GlOBAL.getId())
                                    || costInvestment.getInvestmentType().equals(SynchroGlobal.InvestmentType.COUNTRY.getId())) {
                                costsBean.setTenderingCost(projectInitiation.getFieldworkCost());
                                costsBean.setTenderingCostCurrency(projectInitiation.getFieldworkCostCurrency() == null?-1L:projectInitiation.getFieldworkCostCurrency());
                            }

                            if(projectInitiation.getLatestEstimate() != null) {
                                costsBean.setLatestEstimatedCost(projectInitiation.getLatestEstimate());
                                costsBean.setLatestEstimatedCostCurrency(projectInitiation.getLatestEstimateType() == null?-1L:new Long(projectInitiation.getLatestEstimateType()));

//                        if(projectInitiation.getStatus() == SynchroGlobal.StageStatus.PIB_COMPLETED.ordinal()) {

                                if(projectInitiation.getLatestEstimate().doubleValue() > 0) {
                                    if(project.getMultiMarket()) {
                                        totalProjectCost = costsBean.getLatestEstimatedCost();
                                        totalProjectCostCurrency = costsBean.getLatestEstimatedCostCurrency() == null?-1L:costsBean.getLatestEstimatedCostCurrency();
                                        latestProjectCost = costInvestment.getEstimatedCost();
                                        latestProjectCostCurrency = costInvestment.getEstimatedCostCurrency() == null?-1L:costInvestment.getEstimatedCostCurrency();
                                    } else {
                                        totalProjectCost = latestProjectCost = costsBean.getLatestEstimatedCost();
                                        totalProjectCostCurrency = latestProjectCostCurrency = costsBean.getLatestEstimatedCostCurrency() == null?-1L:costsBean.getLatestEstimatedCostCurrency();
                                    }
                                }
//                        }
                            }
                        }

                        PIBStakeholderList pibStakeholderList = getPibManager().getPIBStakeholderList(project.getProjectID(), costInvestment.getEndmarketId());

                        Map<Long, ProfileFieldValue> profileFieldMap = null;
                        if(pibStakeholderList != null) {
                            // Update Agency 1 cost details
                            if(pibStakeholderList.getAgencyContact1() != null && pibStakeholderList.getAgencyContact1() > 0) {
                                costsBean.setAgency1(pibStakeholderList.getAgencyContact1());
                                try {
                                    profileFieldMap = getProfileManager().getProfile(getUserManager().getUser(pibStakeholderList.getAgencyContact1()));
                                } catch (UserNotFoundException e) {
                                    LOG.error("Agency contact 1 ::" + pibStakeholderList.getAgencyContact1() + " not found.");
                                }

                                if(profileFieldMap != null && profileFieldMap.get(2L) != null) {
                                    costsBean.setAgency1Department(SynchroGlobal.getDepartmentNameById(profileFieldMap.get(2L).getValue()));
                                }
                            }
                            if(pibStakeholderList.getAgencyContact1Optional() != null && pibStakeholderList.getAgencyContact1Optional() > 0) {
                                costsBean.setAgency1optional(pibStakeholderList.getAgencyContact1Optional());
                                if(costsBean.getAgency1Department() == null) {
                                    try {
                                        profileFieldMap = getProfileManager().getProfile(getUserManager().getUser(pibStakeholderList.getAgencyContact1Optional()));
                                    } catch (UserNotFoundException e) {
                                        LOG.error("Agency contact 1 optional ::" + pibStakeholderList.getAgencyContact1Optional() + " not found.");
                                    }
                                    if(profileFieldMap != null && profileFieldMap.get(2L) != null) {
                                        costsBean.setAgency1Department(SynchroGlobal.getDepartmentNameById(profileFieldMap.get(2L).getValue()));
                                    }
                                }
                            }

                            // Update Agency 2 cost details
                            if(pibStakeholderList.getAgencyContact2() != null && pibStakeholderList.getAgencyContact2() > 0) {
                                costsBean.setAgency2(pibStakeholderList.getAgencyContact2());
                                try {
                                    profileFieldMap = getProfileManager().getProfile(getUserManager().getUser(pibStakeholderList.getAgencyContact2()));
                                } catch (UserNotFoundException e) {
                                    LOG.error("Agency contact 2 ::" + pibStakeholderList.getAgencyContact2() + " not found.");
                                }

                                if(profileFieldMap != null && profileFieldMap.get(2L) != null) {
                                    costsBean.setAgency2Department(SynchroGlobal.getDepartmentNameById(profileFieldMap.get(2L).getValue()));
                                }
                            }
                            if(pibStakeholderList.getAgencyContact2Optional() != null && pibStakeholderList.getAgencyContact2Optional() > 0) {
                                costsBean.setAgency2optional(pibStakeholderList.getAgencyContact2Optional());
                                if(costsBean.getAgency2Department() == null) {
                                    try {
                                        profileFieldMap = getProfileManager().getProfile(getUserManager().getUser(pibStakeholderList.getAgencyContact2Optional()));
                                    } catch (UserNotFoundException e) {
                                        LOG.error("Agency contact 2 ::" + pibStakeholderList.getAgencyContact2Optional() + " not found.");
                                    }

                                    if(profileFieldMap != null && profileFieldMap.get(2L) != null) {
                                        costsBean.setAgency2Department(SynchroGlobal.getDepartmentNameById(profileFieldMap.get(2L).getValue()));
                                    }
                                }
                            }

                            // Update Agency 3 cost details
                            if(pibStakeholderList.getAgencyContact3() != null && pibStakeholderList.getAgencyContact3() > 0) {
                                costsBean.setAgency3(pibStakeholderList.getAgencyContact3());
                                try {
                                    profileFieldMap = getProfileManager().getProfile(getUserManager().getUser(pibStakeholderList.getAgencyContact3()));
                                } catch (UserNotFoundException e) {
                                    LOG.error("Agency contact 1 optional ::" + pibStakeholderList.getAgencyContact3() + " not found.");
                                }
                                if(profileFieldMap != null && profileFieldMap.get(2L) != null) {
                                    costsBean.setAgency3Department(SynchroGlobal.getDepartmentNameById(profileFieldMap.get(2L).getValue()));
                                }
                            }
                            if(pibStakeholderList.getAgencyContact3Optional() != null && pibStakeholderList.getAgencyContact3Optional() > 0) {
                                costsBean.setAgency3optional(pibStakeholderList.getAgencyContact3Optional());
                                if(costsBean.getAgency3Department() == null) {
                                    try {
                                        profileFieldMap = getProfileManager().getProfile(getUserManager().getUser(pibStakeholderList.getAgencyContact3Optional()));
                                    } catch (UserNotFoundException e) {
                                        LOG.error("Agency contact 1 optional ::" + pibStakeholderList.getAgencyContact3Optional() + " not found.");
                                    }
                                    if(profileFieldMap != null && profileFieldMap.get(2L) != null) {
                                        costsBean.setAgency3Department(SynchroGlobal.getDepartmentNameById(profileFieldMap.get(2L).getValue()));
                                    }
                                }
                            }
                        }

                        // Proposal
                        List<ProposalInitiation> proposalInitiations = getProposalManager().getProposalDetails(project.getProjectID());
                        if(proposalInitiations != null && proposalInitiations.size() > 0) {
                            for(ProposalInitiation proposalInitiation: proposalInitiations) {
                                if(proposalInitiation.getIsAwarded()) {
                                    projectStartDate = proposalInitiation.getStartDate();
                                    projectEndDate = proposalInitiation.getEndDate();
                                    costsBean.setAwardedAgency(proposalInitiation.getAgencyID());

                                    if(project.getMultiMarket()) {
                                        if(costInvestment.getEstimatedCost() != null && costInvestment.getEstimatedCost().doubleValue() > 0) {
                                            latestProjectCost = costInvestment.getEstimatedCost();
                                            latestProjectCostCurrency = costInvestment.getEstimatedCostCurrency() == null?-1L:costInvestment.getEstimatedCostCurrency();
                                        }
                                    }

                                    ProposalEndMarketDetails endMarketDetails = getProposalManager().getProposalEMDetails(project.getProjectID(),costInvestment.getEndmarketId(), proposalInitiation.getAgencyID());
                                    if(endMarketDetails != null) {
                                        costsBean.setDatacollection(StringUtils.join(endMarketDetails.getDataCollectionMethod().toArray(),","));
                                        if(endMarketDetails.getTotalCost() != null) {
                                            BigDecimal propTotalCost = endMarketDetails.getTotalCost();
                                            Long popTotalCostCurrency = endMarketDetails.getTotalCostType() == null?-1L:new Long(endMarketDetails.getTotalCostType());
                                            costsBean.setProposalTotalCost(propTotalCost);
                                            costsBean.setProposalTotalCostCurrency(popTotalCostCurrency);
                                            if(propTotalCost.doubleValue() > 0) {
                                                totalProjectCost = propTotalCost;
                                                totalProjectCostCurrency = popTotalCostCurrency;
                                                if(!project.getMultiMarket()) {
                                                    latestProjectCost = propTotalCost;
                                                    latestProjectCostCurrency = popTotalCostCurrency;
                                                }
                                            }

                                        }
                                        if(endMarketDetails.getIntMgmtCost() != null) {
                                            costsBean.setProposalInitialMgmtCost(endMarketDetails.getIntMgmtCost());
                                            costsBean.setProposalInitialMgmtCostCurrency(endMarketDetails.getIntMgmtCostType() == null?-1L:new Long(endMarketDetails.getIntMgmtCostType()));
                                        }
                                        if(endMarketDetails.getLocalMgmtCost() != null) {
                                            costsBean.setProposalLocalMgmtCost(endMarketDetails.getLocalMgmtCost());
                                            costsBean.setProposalLocalMgmtCostCurrency(endMarketDetails.getLocalMgmtCostType() == null?-1L:new Long(endMarketDetails.getLocalMgmtCostType()));
                                        }
                                        if(endMarketDetails.getFieldworkCost() != null) {
                                            costsBean.setProposalFieldworkCost(endMarketDetails.getFieldworkCost());
                                            costsBean.setProposalFieldworkCostCurrency(endMarketDetails.getFieldworkCostType() == null?-1L:new Long(endMarketDetails.getFieldworkCostType()));
                                        }
                                        if(endMarketDetails.getOperationalHubCost() != null) {
                                            costsBean.setProposalOperationHubCost(endMarketDetails.getOperationalHubCost());
                                            costsBean.setProposalOperationHubCostCurrency(endMarketDetails.getOperationalHubCostType()== null?-1L:new Long(endMarketDetails.getOperationalHubCostType()));
                                        }
                                        if(endMarketDetails.getOtherCost() != null) {
                                            costsBean.setProposalOtherCost(endMarketDetails.getOtherCost());
                                            costsBean.setProposalOtherCostCurrency(endMarketDetails.getOtherCostType() == null?-1L:new Long(endMarketDetails.getOtherCostType()));
                                        }
                                    }

                                }
                            }
                        }

                        // Project Specs
                        List<ProjectSpecsInitiation> projectSpecsInitiations = getProjectSpecsManager().getProjectSpecsInitiation(project.getProjectID());
                        if(projectSpecsInitiations != null && projectSpecsInitiations.size() > 0) {
                            if(!costInvestment.getAboveMarket() && costInvestment.getEndmarketId() > 0) {
                                ProjectSpecsEndMarketDetails projectSpecsEMDetail = getProjectSpecsManager().getProjectSpecsEMDetails(project.getProjectID(), costInvestment.getEndmarketId());
                                if(projectSpecsEMDetail != null) {
                                    if(projectSpecsEMDetail.getOriginalFinalCost() != null) {
                                        costsBean.setPsOriginalCost(projectSpecsEMDetail.getOriginalFinalCost());
                                        costsBean.setPsOriginalCostCurrency(projectSpecsEMDetail.getOriginalFinalCostType() == null?-1L:new Long(projectSpecsEMDetail.getOriginalFinalCostType()));
                                    }

                                    if(projectSpecsEMDetail.getFinalCost() != null) {
                                        costsBean.setPsFinalCost(projectSpecsEMDetail.getFinalCost());
                                        costsBean.setPsFinalCostCurrency(projectSpecsEMDetail.getFinalCostType() == null?-1L:new Long(projectSpecsEMDetail.getFinalCostType()));

                                    }

                                    if(project.getMultiMarket()) {
                                        if(projectSpecsEMDetail.getTotalCost() != null) {
                                            totalProjectCost = latestProjectCost = projectSpecsEMDetail.getTotalCost();
                                            totalProjectCostCurrency  = latestProjectCostCurrency = projectSpecsEMDetail.getTotalCostType() == null?-1L:new Long(projectSpecsEMDetail.getTotalCostType());
                                        }
                                    } else {
                                        if(projectSpecsEMDetail.getFinalCost() != null && projectSpecsEMDetail.getFinalCost().intValue() > 0) {
                                            totalProjectCost = latestProjectCost = projectSpecsEMDetail.getFinalCost();
                                            totalProjectCostCurrency  = latestProjectCostCurrency = projectSpecsEMDetail.getFinalCostType() == null?-1L:new Long(projectSpecsEMDetail.getFinalCostType());
                                        } else {
                                            if(projectInitiations != null && projectInitiations.size() > 0) {
                                                ProjectInitiation pi = projectInitiations.get(0);
                                                totalProjectCost = latestProjectCost = pi.getLatestEstimate();
                                                totalProjectCostCurrency  = latestProjectCostCurrency = pi.getLatestEstimateType() == null?-1L:new Long(pi.getLatestEstimateType());
                                            } else {
                                                totalProjectCost = latestProjectCost = null;
                                                totalProjectCostCurrency = latestProjectCostCurrency = -1L;
                                            }

                                        }
                                    }
                                }
                            } else {
                                if(costInvestment.getAboveMarket() && costInvestment.getInvestmentType().equals(SynchroGlobal.InvestmentType.GlOBAL.getId())) {
                                    int zeroCostEMCount = 0;
                                    int totalEMCount = 0;
                                    for(ProjectSpecsInitiation projectSpecsInitiation : projectSpecsInitiations) {
                                        if(projectSpecsInitiation.getEndMarketID() > 0) {
                                            ProjectSpecsEndMarketDetails projectSpecsEMDetail = getProjectSpecsManager().getProjectSpecsEMDetails(projectSpecsInitiation.getProjectID(), projectSpecsInitiation.getEndMarketID());
                                            totalEMCount++;
                                            if(projectSpecsEMDetail.getTotalCost() == null
                                                    || projectSpecsEMDetail.getTotalCost().doubleValue() == 0) {
                                                zeroCostEMCount++;
                                            }
                                        }
                                    }
                                    if(zeroCostEMCount > 0 && totalEMCount == zeroCostEMCount) {
                                        List<ProjectSpecsInitiation> psAboveMarketDetailsList = getProjectSpecsManager().getProjectSpecsInitiation(project.getProjectID(), -100L);
                                        if(psAboveMarketDetailsList != null && psAboveMarketDetailsList.size() > 0) {
                                            ProjectSpecsInitiation projectSpecsInitiation = psAboveMarketDetailsList.get(0);
                                            if(projectSpecsInitiation.getAboveMarketFinalCost() != null && projectSpecsInitiation.getAboveMarketFinalCost().doubleValue() > 0) {
                                                totalProjectCost = latestProjectCost = projectSpecsInitiation.getAboveMarketFinalCost();
                                                totalProjectCostCurrency = latestProjectCostCurrency = projectSpecsInitiation.getAboveMarketFinalCostType() == null?-1L:new Long(projectSpecsInitiation.getAboveMarketFinalCostType());
                                            } else {
                                                //List<ProjectInitiation> pis = getPibManager().getPIBDetails(project.getProjectID(), -100L);
                                                if(projectInitiations != null && projectInitiations.size() > 0) {
                                                    ProjectInitiation pi = projectInitiations.get(0);
                                                    totalProjectCost = latestProjectCost = pi.getLatestEstimate();
                                                    totalProjectCostCurrency = latestProjectCostCurrency = pi.getLatestEstimateType() == null?-1L:new Long(pi.getLatestEstimateType());
                                                } else {
                                                    totalProjectCost = latestProjectCost = null;
                                                    totalProjectCostCurrency = latestProjectCostCurrency = -1L;
                                                }
                                            }
                                        } else {
                                            totalProjectCost = latestProjectCost = null;
                                            totalProjectCostCurrency = latestProjectCostCurrency = -1L;
                                        }
                                    } else {
                                        totalProjectCost = latestProjectCost = null;
                                        totalProjectCostCurrency = latestProjectCostCurrency = -1L;
                                    }
                                }
                            }
//                            if(costInvestment.getAboveMarket()) {
//                                List<ProjectSpecsInitiation> globalPSList = getProjectSpecsManager().getProjectSpecsInitiation(project.getProjectID(), -100L);
//                                if(globalPSList != null && globalPSList.size() > 0) {
//                                    ProjectSpecsInitiation globalPS = globalPSList.get(0);
//                                    if(globalPS.getAboveMarketFinalCost() != null && globalPS.getAboveMarketFinalCost().doubleValue() > 0) {
//                                        totalProjectCost = latestProjectCost = globalPS.getAboveMarketFinalCost();
//                                        totalProjectCostCurrency  = latestProjectCostCurrency = globalPS.getAboveMarketFinalCostType() == null?-1L:new Long(globalPS.getAboveMarketFinalCostType());
//                                    }
//                                }
//                            } else {
//                                if(costInvestment.getEndmarketId() > 0) {
//                                    ProjectSpecsEndMarketDetails projectSpecsEMDetail = getProjectSpecsManager().getProjectSpecsEMDetails(project.getProjectID(), costInvestment.getEndmarketId());
//                                    if(projectSpecsEMDetail != null) {
//                                        if(projectSpecsEMDetail.getOriginalFinalCost() != null) {
//                                            costsBean.setPsOriginalCost(projectSpecsEMDetail.getOriginalFinalCost());
//                                            costsBean.setPsOriginalCostCurrency(projectSpecsEMDetail.getOriginalFinalCostType() == null?-1L:new Long(projectSpecsEMDetail.getOriginalFinalCostType()));
//                                        }
//                                        if(projectSpecsEMDetail.getFinalCost() != null) {
//                                            costsBean.setPsFinalCost(projectSpecsEMDetail.getFinalCost());
//                                            costsBean.setPsFinalCostCurrency(projectSpecsEMDetail.getFinalCostType() == null?-1L:new Long(projectSpecsEMDetail.getFinalCostType()));
//                                            if(projectSpecsEMDetail.getFinalCost().doubleValue() > 0) {
//                                                totalProjectCost = latestProjectCost = projectSpecsEMDetail.getFinalCost();
//                                                totalProjectCostCurrency  = latestProjectCostCurrency = projectSpecsEMDetail.getFinalCostType() == null?-1L:new Long(projectSpecsEMDetail.getFinalCostType());
//                                            }
//                                        }
//                                    }
//                                }
//                            }
                        }

                        /** Need to uncomment once latest code approved **/
//                        List<ProjectSpecsInitiation> projectSpecsInitiations = getProjectSpecsManager().getProjectSpecsInitiation(project.getProjectID());
//                        if(projectSpecsInitiations != null && projectSpecsInitiations.size() > 0) {
//                            if((!costInvestment.getAboveMarket()) && costInvestment.getEndmarketId() > 0) {
//                                ProjectSpecsEndMarketDetails projectSpecsEMDetail = getProjectSpecsManager().getProjectSpecsEMDetails(project.getProjectID(), costInvestment.getEndmarketId());
//                                if(projectSpecsEMDetail != null) {
//                                    if(projectSpecsEMDetail.getOriginalFinalCost() != null) {
//                                        costsBean.setPsOriginalCost(projectSpecsEMDetail.getOriginalFinalCost());
//                                        costsBean.setPsOriginalCostCurrency(projectSpecsEMDetail.getOriginalFinalCostType() == null?-1L:new Long(projectSpecsEMDetail.getOriginalFinalCostType()));
//                                    }
//                                    if(projectSpecsEMDetail.getFinalCost() != null) {
//                                        costsBean.setPsFinalCost(projectSpecsEMDetail.getFinalCost());
//                                        costsBean.setPsFinalCostCurrency(projectSpecsEMDetail.getFinalCostType() == null?-1L:new Long(projectSpecsEMDetail.getFinalCostType()));
//                                        if(!project.getMultiMarket() && projectSpecsEMDetail.getFinalCost().doubleValue() > 0) {
//                                            totalProjectCost = latestProjectCost = projectSpecsEMDetail.getFinalCost();
//                                            totalProjectCostCurrency  = latestProjectCostCurrency = projectSpecsEMDetail.getFinalCostType() == null?-1L:new Long(projectSpecsEMDetail.getFinalCostType());
//                                        }
//                                    }
//
//                                    if(project.getMultiMarket()) {
//                                        if(projectSpecsEMDetail.getTotalCost() != null && projectSpecsEMDetail.getTotalCost().doubleValue() > 0) {
//                                            totalProjectCost = latestProjectCost = projectSpecsEMDetail.getTotalCost();
//                                            totalProjectCostCurrency  = latestProjectCostCurrency = projectSpecsEMDetail.getTotalCostType() == null?-1L:new Long(projectSpecsEMDetail.getTotalCostType());
//                                        }
//                                    }
//                                }
//
//                            } else {
//                                latestProjectCost = null;
//                                latestProjectCostCurrency = -1L;
//                            }
//                        }
                        /** ============================================ **/
                        costsBean.setProjectStartDate(projectStartDate);
                        costsBean.setProjectEndDate(projectEndDate);
                        costsBean.setTotalProjectCost(totalProjectCost);
                        costsBean.setTotalProjectCostCurrency(totalProjectCostCurrency);
                        costsBean.setLatestProjectCost(latestProjectCost);
                        costsBean.setLatestProjectCostCurrency(latestProjectCostCurrency);
                        costsBean.setProjectStatus(project.getStatus());
                        projectCostsBeans.add(costsBean);
                        // getProjectCostsCaptureManager().save(costsBean, quarter);
                    }

                    List<ProjectCostsBean> existingProjectCostBeans = getProjectCostsCaptureManager().get(project.getProjectID(), quarter);
                    if(existingProjectCostBeans != null && existingProjectCostBeans.size() > 0) {
                        for(ProjectCostsBean existingBean: existingProjectCostBeans) {
                            if(existingBean != null && !isCostBeanExits(existingBean, projectCostsBeans)) {
                                getProjectCostsCaptureManager().delete(existingBean.getProjectId(), existingBean.getEndmarketId(),existingBean.getInvestmentType().intValue(), quarter);
                            }
                        }
                    }

                    for(ProjectCostsBean bean:projectCostsBeans) {
                        getProjectCostsCaptureManager().save(bean, quarter);
                    }


                    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a");
                    JiveGlobals.setJiveProperty("synchro.raw.extract.lastupdate",  dateFormat.format(new Date()));
                    updateInProgress = false;
                }
            } catch (Exception e) {
                updateInProgress = false;
                throw new ProjectCostCaptureUpdateProcessException(e.getMessage(), e);
            }
        } else {
            throw new ProjectCostCaptureUpdateProcessException("Another update process already running.");
        }
    }

    private boolean isCostBeanExits(ProjectCostsBean existingBean, List<ProjectCostsBean> projectCostsBeans) {
        boolean isExists = false;
        if(projectCostsBeans != null && projectCostsBeans.size() > 0) {
            for(ProjectCostsBean bean: projectCostsBeans) {
                if(bean.getProjectId().equals(existingBean.getProjectId())
                        && bean.getEndmarketId().equals(existingBean.getEndmarketId()) && bean.getInvestmentType().equals(existingBean.getInvestmentType())) {
                    return true;
                }
            }
        }
        return isExists;
    }

    private class CostInvestment {
        private Long endmarketId;
        private Boolean isAboveMarket = false;
        private Integer investmentType;
        private Long spiContact;
        private Long projectOwner;
        private BigDecimal estimatedCost;
        private Long estimatedCostCurrency;


        public Long getEndmarketId() {
            return endmarketId;
        }

        public void setEndmarketId(Long endmarketId) {
            this.endmarketId = endmarketId;
        }

        public Boolean getAboveMarket() {
            return isAboveMarket;
        }

        public void setAboveMarket(Boolean aboveMarket) {
            isAboveMarket = aboveMarket;
        }

        public Integer getInvestmentType() {
            return investmentType;
        }

        public void setInvestmentType(Integer investmentType) {
            this.investmentType = investmentType;
        }

        public Long getSpiContact() {
            return spiContact;
        }

        public void setSpiContact(Long spiContact) {
            this.spiContact = spiContact;
        }

        public Long getProjectOwner() {
            return projectOwner;
        }

        public void setProjectOwner(Long projectOwner) {
            this.projectOwner = projectOwner;
        }

        public BigDecimal getEstimatedCost() {
            return estimatedCost;
        }

        public void setEstimatedCost(BigDecimal estimatedCost) {
            this.estimatedCost = estimatedCost;
        }

        public Long getEstimatedCostCurrency() {
            return estimatedCostCurrency;
        }

        public void setEstimatedCostCurrency(Long estimatedCostCurrency) {
            this.estimatedCostCurrency = estimatedCostCurrency;
        }
    }

    public static ProjectCostsCaptureManager getProjectCostsCaptureManager() {
        if(projectCostsCaptureManager == null) {
            return JiveApplication.getContext().getSpringBean("projectCostsCaptureManager");
        }
        return projectCostsCaptureManager;

    }


    public static ProjectManager getSynchroProjectManager() {
        if(synchroProjectManager == null) {
            return JiveApplication.getContext().getSpringBean("synchroProjectManager");
        }
        return synchroProjectManager;

    }

    public static PIBManager getPibManager() {
        if(pibManager == null) {
            return JiveApplication.getContext().getSpringBean("pibManager");
        }
        return pibManager;

    }

    public static ProposalManager getProposalManager() {
        if(proposalManager == null) {
            return JiveApplication.getContext().getSpringBean("proposalManager");
        }
        return proposalManager;
    }

    public static ProjectSpecsManager getProjectSpecsManager() {
        if(projectSpecsManager == null) {
            return JiveApplication.getContext().getSpringBean("projectSpecsManager");
        }
        return projectSpecsManager;

    }


    public static UserManager getUserManager() {
        if(userManager == null) {
            return JiveApplication.getContext().getSpringBean("userManager");
        }
        return userManager;

    }

    public static ProfileManager getProfileManager() {
        if(profileManager == null) {
            return JiveApplication.getContext().getSpringBean("profileManager");
        }
        return profileManager;

    }


}
