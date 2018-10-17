package com.grail.synchro.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.FieldAttachmentBean;
import com.grail.synchro.beans.PIBMethodologyWaiver;
import com.grail.synchro.beans.PIBReporting;
import com.grail.synchro.beans.PIBStakeholderList;
import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectInitiation;
import com.grail.synchro.beans.ProjectWaiver;
import com.grail.synchro.dao.PIBDAONew;
import com.grail.synchro.dao.ProjectWaiverDAO;
import com.grail.synchro.dao.util.SynchroDAOUtil;
import com.grail.synchro.object.SynchroAttachment;
import com.jivesoftware.base.database.dao.DAOException;
import com.jivesoftware.base.database.dao.JiveJdbcDaoSupport;
import com.jivesoftware.community.impl.dao.AttachmentBean;
import com.grail.synchro.search.filter.ProjectResultFilter;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroUtils;

/**
 * @author: vivek
 * @since: 1.0
 */
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public class PIBDAOImplNew extends JiveJdbcDaoSupport implements PIBDAONew {

    private static final Logger LOG = Logger.getLogger(PIBDAOImplNew.class.getName());
    private SynchroDAOUtil synchroDAOUtil;
    private ProjectWaiverDAO projectWaiverDAO;

    
    
    private static final String INSERT_PIB_SQL = "INSERT INTO grailpib( " +
            " projectid, endmarketid, bizquestion, " +
            " researchobjective, actionstandard, researchdesign, " +
            " sampleprofile, stimulusmaterial,others, stimulidate, " +
            " latestEstimate,latestestimatetype, npiReferenceNo, " +
            " deviationfromsm, " +
            " creationby, modificationby, creationdate, modificationdate,status,legalapprovalrcvd,legalapprovalnotreq,legalapprover,hasTenderingProcess,fieldworkCost,fieldworkCostCurrency, bizquestiontext, researchobjectivetext, actionstandardtext, researchdesigntext, sampleprofiletext, stimulusmaterialtext, otherstext, nonkantar, pibsavedate, piblegalapprovaldate, pibcompletiondate," +
            " brief,  brieftext, brieflegalapprover, islegalsignoffreq, briefapprovaldate, brieflegalapprovalstatus, brieflegalapproveroffline ) " +
            " VALUES (?, ?, ?, " +
            " ?, ?, ?, " +
            " ?, ?, ?, ?, " +
            " ?, ?, ?, " +
            " ?, " +
            " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";
    
    private static final String INSERT_NEW_PIB_SQL = "INSERT INTO grailpibnew( " +
            " projectid, brief, " +
            " brieftext, legalapprover, status, " +
            " islegalsignoffreq,  " +
            " creationby, modificationby, creationdate, modificationdate) " +
            " VALUES (?, ?, ?, " +
            " ?, ?, ?, " +
            " ?, ?, ?, ?) ";
    
    private static final String INSERT_PIB_OTHER_EM_SQL = "INSERT INTO grailpib( " +
            " projectid, endmarketid, bizquestion, " +
            " researchobjective, actionstandard, researchdesign, " +
            " sampleprofile, stimulusmaterial,others, stimulidate, deviationfromsm, " +
            " npiReferenceNo, " +
            " creationby, modificationby, creationdate, modificationdate,status,hasTenderingProcess,fieldworkCost,fieldworkCostCurrency,bizquestiontext, researchobjectivetext, actionstandardtext, researchdesigntext, sampleprofiletext, stimulusmaterialtext, otherstext) " +
            " VALUES (?, ?, ?, " +
            " ?, ?, ?, ?, ?, " +
            " ?, " +
            " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";


    private static final String LOAD_PIB = " SELECT projectid, endmarketid, bizquestion, " +
            "       researchobjective, actionstandard, researchdesign, " +
            "       sampleprofile, " +
            "       stimulusmaterial, others, stimulidate,latestestimate,latestestimatetype, npireferenceno, deviationfromsm,  " +
            "       creationby, " +
            "       modificationby, creationdate, modificationdate, status, legalapprovalrcvd,legalapprovalnotreq,legalapprover, isendmarketchanged," +
            "       notifyabovemarketcontacts, approvechanges, notifyspi, notifypo, hasTenderingProcess, fieldworkCost, fieldworkCostCurrency, bizquestiontext, researchobjectivetext, actionstandardtext, researchdesigntext, sampleprofiletext, stimulusmaterialtext, otherstext, nonkantar, pibsavedate, piblegalapprovaldate, pibcompletiondate, pibnotifyamcontactsdate, brieflegalapproveroffline " +
            "  FROM grailpib ";
    
    private static final String LOAD_PIB_NEW = " SELECT projectid, brief, brieftext, " +
            "       brieflegalapprover, status, islegalsignoffreq, " +
            "       creationby, " +
            "       modificationby, creationdate, modificationdate, briefsendforapproval, briefneedsdiscussion, briefapprovaldate, brieflegalapprovalstatus, sendreminderdate, sendforapprovaldate, brieflegalapproveroffline " +
            "  FROM grailpib ";
    private static final String GET_PIB_BY_PROJECT_ID = LOAD_PIB + " where projectid = ? ";
    
    private static final String GET_PIB_NEW_BY_PROJECT_ID = LOAD_PIB_NEW + " where projectid = ? ";
    
    private static final String GET_PIB_BY_PROJECT_ID_END_MARKET_ID = LOAD_PIB + " where projectid = ? and endmarketid=? ";

    private static final String UPDATE_PIB_BY_PROJECT_ID = "UPDATE grailpib " +
            "   SET endmarketid=?, bizquestion=?, researchobjective=?, actionstandard=?, " +
            "   researchdesign=?, sampleprofile=?, stimulusmaterial=?, others=?, stimulidate=?, " +
            "   latestestimate=?, latestestimatetype=?, npireferenceno=?, deviationfromsm=?, " +
            "   modificationby=?, modificationdate=?, status=?, legalapprovalrcvd=?, legalapprovalnotreq=?, legalapprover=?, hasTenderingProcess=?, fieldworkCost=?, fieldworkCostCurrency=?, " +
            " bizquestiontext=?, researchobjectivetext=?, actionstandardtext=?, researchdesigntext=?, sampleprofiletext=?, stimulusmaterialtext=?, otherstext=?, nonkantar=? ,pibsavedate=?, piblegalapprovaldate=?, pibcompletiondate=? "+
            "  WHERE projectid = ? ";
    
    private static final String UPDATE_PIB_NEW_BY_PROJECT_ID = "UPDATE grailpib " +
            "   SET brief=?, brieftext=?, brieflegalapprover=?, " +
            "   modificationby=?, modificationdate=?, status=?, briefapprovaldate=?, brieflegalapprovalstatus=?, islegalsignoffreq=?, brieflegalapproveroffline=?  "+
            "  WHERE projectid = ? ";
    
    private static final String UPDATE_PIB_SEND_FOR_APPROVAL = "UPDATE grailpib " +
            "   SET briefsendforapproval=?, brieflegalapprover=?, briefneedsdiscussion=?, " +
            "   modificationby=?, modificationdate=?, sendforapprovaldate=? "+
            "  WHERE projectid = ? ";
    
    private static final String RESET_PIB = "UPDATE grailpib " +
            "   SET brief=?, brieftext=?, brieflegalapprover=?, isbriefapproved=?, briefapprovaldate=?, islegalsignoffreq=?,  " +
            "   briefsendforapproval=?, briefneedsdiscussion=?, brieflegalapprovalstatus=?, sendreminderdate=? "+
            "  WHERE projectid = ? ";
    
    private static final String UPDATE_SEND_REMINDER = "UPDATE grailpib " +
            "   SET sendreminderdate=? "+
            "  WHERE projectid = ? ";
    
    private static final String UPDATE_PIB_NEEDS_DISCUSSION = "UPDATE grailpib " +
            "   SET briefneedsdiscussion=?, briefsendforapproval=?, " +
            "   modificationby=?, modificationdate=? "+
            "  WHERE projectid = ? ";
    
    private static final String CONFIRM_LEGAL_APPROVAL_SUBMISSION = "UPDATE grailpib " +
            "   SET briefneedsdiscussion=?, briefsendforapproval=?, briefapprovaldate=?, brieflegalapprovalstatus=?,   " +
            "   modificationby=?, modificationdate=? "+
            "  WHERE projectid = ? ";
    
    private static final String UPDATE_PIB_BY_PROJECT_ID_END_MARKET_ID = "UPDATE grailpib " +
            "   SET endmarketid=?, bizquestion=?, researchobjective=?, actionstandard=?, " +
            "   researchdesign=?, sampleprofile=?, stimulusmaterial=?, others=?, stimulidate=?, " +
            "   latestestimate=?, latestestimatetype=?, npireferenceno=?, deviationfromsm=?, " +
            "   modificationby=?, modificationdate=?, status=?, legalapprovalrcvd=?, legalapprovalnotreq=?, legalapprover=?, isendmarketchanged=?, hasTenderingProcess=?, fieldworkCost=?, fieldworkCostCurrency=?,  " +
            " bizquestiontext=?, researchobjectivetext=?, actionstandardtext=?, researchdesigntext=?, sampleprofiletext=?, stimulusmaterialtext=?, otherstext=?, nonkantar=?, pibsavedate=?, piblegalapprovaldate=? "+
            "  WHERE projectid = ? and endmarketid=? ";
    
    private static final String UPDATE_PIB_BY_PROJECT_ID_END_MARKET_ID_OTHER = "UPDATE grailpib " +
            "   SET endmarketid=?, bizquestion=?, researchobjective=?, actionstandard=?, " +
            "   researchdesign=?, sampleprofile=?, stimulusmaterial=?, others=?, stimulidate=?, " +
            "   npireferenceno=?, " +
            "   modificationby=?, modificationdate=?, " +
            "   bizquestiontext=?, researchobjectivetext=?, actionstandardtext=?, researchdesigntext=?, sampleprofiletext=?, stimulusmaterialtext=?, otherstext=?, nonkantar=? "+
            "  WHERE projectid = ? and endmarketid=? ";

    private static final String GET_PIB_REPORTING_BY_PROJECT_ID = "SELECT projectid, endmarketid, " +
            "       topLinePresentation, presentation, fullreport, globalsummary, otherReportingRequirements, otherReportingRequirementstext " +
            "  FROM grailpibreporting WHERE projectid = ? ";
    
    private static final String GET_PIB_REPORTING_BY_PROJECT_ID_END_MARKET_ID = "SELECT projectid, endmarketid, " +
            "       topLinePresentation, presentation, fullreport, globalsummary, otherReportingRequirements, otherReportingRequirementstext " +
            "  FROM grailpibreporting WHERE projectid = ? and endmarketid=? ";

    private static final String GET_PIB_STAKEHOLDER_BY_PROJECT_ENDMARKET_ID = "SELECT projectid, endmarketid, " +
            "       agencycontact1, agencycontact2, agencycontact3," +
            "		agencycontact1optional, agencycontact2optional, agencycontact3optional, " +
            "       globallegalcontact, globalprocurementcontact, globalcommunicationagency, productcontact " +
            "  FROM grailpibstakeholderlist WHERE projectid = ? and endmarketid=?";
    
    private static final String GET_PIB_STAKEHOLDER_BY_PROJECT_ID = "SELECT projectid, endmarketid, " +
            "       agencycontact1, agencycontact2, agencycontact3, " +
            "		agencycontact1optional, agencycontact2optional, agencycontact3optional," +
            "       globallegalcontact, globalprocurementcontact, globalcommunicationagency, productcontact " +
            "  FROM grailpibstakeholderlist WHERE projectid = ? ";

    private static final String INSERT_PIB_REPORTING = "INSERT INTO grailpibreporting(" +
            "            projectid, endmarketid, " +
            "            topLinePresentation, presentation, fullreport, globalsummary, otherReportingRequirements, otherReportingRequirementstext)" +
            "            VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String INSERT_PIB_STAKEHOLDER_LIST = "INSERT INTO grailpibstakeholderlist(" +
            "            projectid, endmarketid, " +
            "            agencycontact1, agencycontact2, agencycontact3," +
            "			 agencycontact1optional, agencycontact2optional, agencycontact3optional,	" +
            "            globallegalcontact, globalprocurementcontact, globalcommunicationagency, productcontact)" +
            "            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String INSERT_PIB_LEGAL_CONTACT = "INSERT INTO grailpibstakeholderlist(" +
            "            projectid, endmarketid, " +
            "            globallegalcontact)" +
            "            VALUES (?, ?, ?)";

    private static final String UPDATE_PIB_REPORTING_BY_PROJECT_ID = "UPDATE grailpibreporting " +
            "   SET endmarketid=?, topLinePresentation=?, presentation=?, fullreport=?, globalsummary=?,  " +
            "   otherReportingRequirements=?, otherReportingRequirementstext=? " +
            " WHERE projectid = ? ";
    
    private static final String UPDATE_PIB_REPORTING_BY_PROJECT_ID_END_MARKET_ID = "UPDATE grailpibreporting " +
            "   SET endmarketid=?, topLinePresentation=?, presentation=?, fullreport=?, globalsummary=?, " +
            "   otherReportingRequirements=?, otherReportingRequirementstext=? " +
            " WHERE projectid = ? and endmarketid=? ";

    private static final String UPDATE_PIB_STAKEHOLDER_LIST_BY_PROJECT_ID = "UPDATE grailpibstakeholderlist " +
            "   SET endmarketid=?, agencycontact1=?, agencycontact2=?, agencycontact3=?, " +
            "	agencycontact1optional=?, agencycontact2optional=?, agencycontact3optional=?," +
            "   globallegalcontact=?, globalprocurementcontact=?, globalcommunicationagency=?, productcontact=? " +
            " WHERE projectid = ? ";
    
    private static final String UPDATE_PIB_STAKEHOLDER_NON_AGENCYLIST_BY_PROJECT_ID = "UPDATE grailpibstakeholderlist " +
            "   SET endmarketid=?, " +
            "   globallegalcontact=?, globalprocurementcontact=?, globalcommunicationagency=?, productcontact=? " +
            " WHERE projectid = ? ";
    
    private static final String UPDATE_PIB_STAKEHOLDER_LIST_BY_PROJECT_ID_END_MARKET_ID = "UPDATE grailpibstakeholderlist " +
            "   SET endmarketid=?, agencycontact1=?, agencycontact2=?, agencycontact3=?," +
            "	agencycontact1optional=?, agencycontact2optional=?, agencycontact3optional=?, " +
            "   globallegalcontact=?, globalprocurementcontact=?, globalcommunicationagency=?, productcontact=? " +
            " WHERE projectid = ? and endmarketid=? ";
    
    private static final String UPDATE_PIB_STAKEHOLDER_LEGAL_CONTACT = "UPDATE grailpibstakeholderlist " +
            "   SET globallegalcontact=?" +
            " WHERE projectid = ? and endmarketid=? ";

    private static final String INSERT_ATTACHMENT = "INSERT INTO grailfieldattachment(projectid, endmarketid, " +
            "	fieldcategoryid, attachmentid, userid)" +
            "  VALUES (?, ?, ?, ?, ?)";
    
    private static final String REMOVE_ATTACHMENT = "DELETE FROM grailfieldattachment where attachmentid=?";
    
    private static final String GET_ATTACHMENT = "SELECT projectid, endmarketid, fieldcategoryid, attachmentid, userid FROM grailfieldattachment where projectid=? and endmarketid=?";
    
    private static final String INSERT_PIB_METHODOLOGY_WAIVER = "INSERT INTO grailpibmethodologywaiver( " +
            " projectid, endmarketid, methodologydeviationrationale, methodologyapprover, creationby, " +
            "       modificationby, creationdate, modificationdate, status) " +
            " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ";
    
    private static final String INSERT_PIB_KANTAR_METHODOLOGY_WAIVER = "INSERT INTO grailpibkantarmw( " +
            " projectid, endmarketid, methodologydeviationrationale, methodologyapprover, creationby, " +
            "       modificationby, creationdate, modificationdate, status, waiverid) " +
            " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";
    
    private static final String UPDATE_PIB_METHODOLOGY_WAIVER = "UPDATE grailpibmethodologywaiver " +
            "  SET methodologydeviationrationale=?, methodologyapprover=?, isapproved=?, modificationby=?, modificationdate=?, status=? " +
            "  WHERE projectid = ? and endmarketid=? ";
    
    private static final String UPDATE_PIB_KANTAR_METHODOLOGY_WAIVER = "UPDATE grailpibkantarmw " +
            "  SET methodologydeviationrationale=?, methodologyapprover=?, isapproved=?, modificationby=?, modificationdate=?, status=? " +
            "  WHERE projectid = ? and endmarketid=? ";

    private static final String APPROVE_PIB_METHODOLOGY_WAIVER = "UPDATE grailpibmethodologywaiver " +
            "  SET methodologyapprovercomment=?, isapproved=? " +
            "  WHERE projectid = ? and endmarketid=? ";
    
    private static final String APPROVE_PIB_KANTAR_METHODOLOGY_WAIVER = "UPDATE grailpibkantarmw " +
            "  SET methodologyapprovercomment=?, isapproved=? " +
            "  WHERE projectid = ? and endmarketid=? ";
    
    private static final String REJECT_PIB_METHODOLOGY_WAIVER = "UPDATE grailpibmethodologywaiver " +
            "  SET methodologyapprovercomment=?, isapproved=? " +
            "  WHERE projectid = ? and endmarketid=? ";
    
    private static final String REJECT_PIB_KANTAR_METHODOLOGY_WAIVER = "UPDATE grailpibkantarmw " +
            "  SET methodologyapprovercomment=?, isapproved=? " +
            "  WHERE projectid = ? and endmarketid=? ";
    
    private static final String REQ_FOR_INFO_PIB_METHODOLOGY_WAIVER = "UPDATE grailpibmethodologywaiver " +
            "  SET methodologyapprovercomment=?,status=?" +
            "  WHERE projectid = ? and endmarketid=? ";
    
    private static final String REQ_FOR_INFO_PIB_KANTAR_METHODOLOGY_WAIVER = "UPDATE grailpibkantarmw " +
            "  SET methodologyapprovercomment=?,status=?" +
            "  WHERE projectid = ? and endmarketid=? ";

    private static final String GET_PIB_METHODOLOGY_WAIVER = "SELECT projectid, endmarketid, " +
            "       methodologydeviationrationale, methodologyapprover, methodologyapprovercomment, isapproved, status, " +
            "		creationby, modificationby, creationdate, modificationdate "+
            "  FROM grailpibmethodologywaiver WHERE projectid = ? and endmarketid=?";
    
    private static final String GET_PIB_KANTAR_METHODOLOGY_WAIVER = "SELECT projectid, endmarketid, " +
            "       methodologydeviationrationale, methodologyapprover, methodologyapprovercomment, isapproved, status, " +
            "		creationby, modificationby, creationdate, modificationdate, waiverid "+
            "  FROM grailpibkantarmw WHERE projectid = ? and endmarketid=?";

    private static final String UPDATE_PIB_METH_WAIVER_SINGLE_ENDMARKET_ID = "UPDATE grailpibmethodologywaiver" +
            " SET endmarketid=?  WHERE projectid = ?";

    private static final String UPDATE_PIB_REPORTING_SINGLE_ENDMARKET_ID = "UPDATE grailpibreporting" +
            " SET endmarketid=? WHERE projectid = ?";

    private static final String UPDATE_PIB_STAKEHOLDER_SINGLE_ENDMARKET_ID = "UPDATE grailpibstakeholderlist" +
            " SET endmarketid=? WHERE projectid = ?";

    private static final String GET_ATTACHMENT_BY_OBJECT = "SELECT * from jiveattachment where objecttype=? AND objectid=?";
    private static final String UPDATE_PIB_STATUS = "UPDATE grailpib SET status=? WHERE projectid = ?";
    
    private static final String UPDATE_PIB_COMPLETION_DATE = "UPDATE grailpib SET pibcompletiondate=? WHERE projectid = ?";
    
    private static final String INSERT_ATTACHMENT_USER = "INSERT INTO grailattachmentuser(attachmentid, userid) VALUES (?, ?)";
    private static final String DELETE_ATTACHMENT_USER = "DELETE from grailattachmentuser where attachmentid=? ";
    private static final String GET_ATTACHMENT_USER = "Select userid from grailattachmentuser where attachmentid=? ";
    private static final String UPDATE_PIB_DEVIATION_BY_PROJECT_ID = "UPDATE grailpib " +
            "   SET deviationfromsm=?, modificationby=?, modificationdate=? " +
            "  WHERE projectid = ? ";
    private static final String UPDATE_ATTACHMENT_OBJECT_ID = "UPDATE jiveattachment " +
            "   SET objectid=? WHERE attachmentid = ? ";
    
    private static final String UPDATE_PIB_ACTION_STANDARD = "UPDATE grailpib SET actionstandard=?, actionstandardtext=? WHERE projectid = ? and endmarketid=?";
    private static final String UPDATE_PIB_RESEARCH_DESIGN = "UPDATE grailpib SET researchdesign=?, researchdesigntext=? WHERE projectid = ? and endmarketid=?";
    private static final String UPDATE_PIB_SAMPLE_PROFILE = "UPDATE grailpib SET sampleprofile=?, sampleprofiletext=? WHERE projectid = ? and endmarketid=?";
    private static final String UPDATE_PIB_STIMULUS_MATERIAL = "UPDATE grailpib SET stimulusmaterial=?, stimulusmaterialtext=? WHERE projectid = ? and endmarketid=?";
    private static final String UPDATE_PIB_BUSINESS_QUESTION = "UPDATE grailpib SET bizquestion=?, bizquestiontext=? WHERE projectid = ? and endmarketid=?";
    private static final String UPDATE_PIB_RESEARCH_OBJECTIVE = "UPDATE grailpib SET researchobjective=?, researchobjectivetext=? WHERE projectid = ? and endmarketid=?";
    
    private static final String DELETE_ENDMARKET_PIB = "DELETE FROM grailpib WHERE projectid = ? AND endmarketid NOT IN ("+SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID+")";
    private static final String DELETE_ENDMARKET_PIB_MW = "DELETE FROM grailpibmethodologywaiver WHERE projectid = ? AND endmarketid NOT IN ("+SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID+")";
    private static final String DELETE_ENDMARKET_PIB_REPORTING = "DELETE FROM grailpibreporting WHERE projectid = ? AND endmarketid NOT IN ("+SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID+")";
    private static final String DELETE_ENDMARKET_PIB_STAKEHOLDER = "DELETE FROM grailpibstakeholderlist WHERE projectid = ? AND endmarketid NOT IN ("+SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID+")";
  
    private static final String DELETE_ENDMARKET_PIB_ENDMARKET_ID = "DELETE FROM grailpib WHERE projectid = ? AND endmarketid = ? ";
    private static final String DELETE_ENDMARKET_PIB_MW_ENDMARKET_ID = "DELETE FROM grailpibmethodologywaiver WHERE projectid = ? AND endmarketid = ?";
    private static final String DELETE_ENDMARKET_PIB_REPORTING_ENDMARKET_ID = "DELETE FROM grailpibreporting WHERE projectid = ? AND endmarketid = ?";
    private static final String DELETE_ENDMARKET_PIB_STAKEHOLDER_ENDMARKET_ID = "DELETE FROM grailpibstakeholderlist WHERE projectid = ? AND endmarketid = ?";
  
    
    private static final String UPDATE_PIB_NOTIFY_ABOVE_MARKET_CONTACTS = "UPDATE grailpib SET notifyabovemarketcontacts=?, pibnotifyamcontactsdate=? WHERE projectid = ? and endmarketid=?";
    private static final String UPDATE_PIB_APPROVE_CHANGES = "UPDATE grailpib SET approvechanges=? WHERE projectid = ? and endmarketid=?";
    private static final String UPDATE_PIB_NOTIFY_SPI = "UPDATE grailpib SET notifyspi=? WHERE projectid = ? ";
    private static final String UPDATE_PIB_NOTIFY_PO = "UPDATE grailpib SET notifypo=? WHERE projectid = ? ";
    
    private static final String UPDATE_PIB_LATEST_ESTIMATE = "UPDATE grailpib " +
            "   SET latestestimate=?, latestestimatetype=? " +
            "  WHERE projectid = ?";
    
    private static final String GET_PROJECT_ID_FROM_WAIVER_ID = "Select projectid from grailpibkantarmw where waiverid=? ";
    
    private static final String UPDATE_OTHER_SPI_CONTACT = "UPDATE grailpibstakeholderlist " +
            "   SET otherspicontact=? WHERE projectid = ? and endmarketid=?";
    private static final String INSERT_OTHER_SPI_CONTACT = "INSERT INTO grailpibstakeholderlist(" +
            "            projectid, endmarketid, otherspicontact) VALUES (?, ?, ?)";
    
    private static final String UPDATE_OTHER_LEGAL_CONTACT = "UPDATE grailpibstakeholderlist " +
            "   SET otherlegalcontact=? WHERE projectid = ? and endmarketid=?";
    private static final String INSERT_OTHER_LEGAL_CONTACT = "INSERT INTO grailpibstakeholderlist(" +
            "            projectid, endmarketid, otherlegalcontact) VALUES (?, ?, ?)";
    
    private static final String UPDATE_OTHER_AGENCY_CONTACT = "UPDATE grailpibstakeholderlist " +
            "   SET otheragencycontact=? WHERE projectid = ? and endmarketid=?";
    private static final String INSERT_OTHER_AGENCY_CONTACT = "INSERT INTO grailpibstakeholderlist(" +
            "            projectid, endmarketid, otheragencycontact) VALUES (?, ?, ?)";
    
    private static final String UPDATE_OTHER_PRODUCT_CONTACT = "UPDATE grailpibstakeholderlist " +
            "   SET otherproductcontact=? WHERE projectid = ? and endmarketid=?";
    private static final String INSERT_OTHER_PRODUCT_CONTACT = "INSERT INTO grailpibstakeholderlist(" +
            "            projectid, endmarketid, otherproductcontact) VALUES (?, ?, ?)";
    
    private static final String UPDATE_AGENCY_CONTACT = "UPDATE grailpibstakeholderlist " +
            "   SET agencycontact1=? WHERE projectid = ? and endmarketid=?";
    
    private static final String INSERT_AGENCY_CONTACT = "INSERT INTO grailpibstakeholderlist(" +
            "            projectid, endmarketid, agencycontact1) VALUES (?, ?, ?)";
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public ProjectInitiation savePIBReporting(final ProjectInitiation projectInitiation) {
        try {
        	final int count = getSimpleJdbcTemplate().queryForInt("SELECT count(endmarketid) from grailpibreporting " +
                    "WHERE projectid = ? AND endmarketid = ?",
                    projectInitiation.getProjectID(), projectInitiation.getEndMarketID());
            if(count==0)
            {
             
	            getSimpleJdbcTemplate().update(INSERT_PIB_REPORTING,  projectInitiation.getProjectID(),projectInitiation.getEndMarketID(),
	                    BooleanUtils.toIntegerObject(projectInitiation.getTopLinePresentation()),
	                    BooleanUtils.toIntegerObject(projectInitiation.getPresentation()),
	                    BooleanUtils.toIntegerObject(projectInitiation.getFullreport()),
	                    BooleanUtils.toIntegerObject(projectInitiation.getGlobalSummary()),
	                    projectInitiation.getOtherReportingRequirements(),
	                    projectInitiation.getOtherReportingRequirementsText());
            }
            return projectInitiation;
        } catch (DataAccessException daEx) {
            final String message = "Failed to SAVE Reporting Info for RIB - " + projectInitiation.getProjectID();
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }

    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public ProjectInitiation savePIBStakeholderList(final ProjectInitiation projectInitiation) {
        try {
        	final int count = getSimpleJdbcTemplate().queryForInt("SELECT count(endmarketid) from grailpibstakeholderlist " +
                    "WHERE projectid = ? AND endmarketid = ?",
                    projectInitiation.getProjectID(), projectInitiation.getEndMarketID());
            if(count==0)
            {
            
	        	getSimpleJdbcTemplate().update(INSERT_PIB_STAKEHOLDER_LIST,  projectInitiation.getProjectID(),projectInitiation.getEndMarketID(),
	                    projectInitiation.getAgencyContact1(),projectInitiation.getAgencyContact2(),projectInitiation.getAgencyContact3(),
	                    projectInitiation.getAgencyContact1Optional(),projectInitiation.getAgencyContact2Optional(),projectInitiation.getAgencyContact3Optional(),
	                    projectInitiation.getGlobalLegalContact(),projectInitiation.getGlobalProcurementContact(),projectInitiation.getGlobalCommunicationAgency(),
						projectInitiation.getProductContact());
            }
            //This else block is added while implementing the other stakeholders for SPI, Legal and Product Contact
            else
            {
            	updatePIBStakeholderList(projectInitiation);
            }
            return projectInitiation;
        } catch (DataAccessException daEx) {
            final String message = "Failed to SAVE Stakeholder List for PIB - " + projectInitiation.getProjectID();
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }

    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public ProjectInitiation savePIBLegalContact(final ProjectInitiation projectInitiation) {
        try {
            getSimpleJdbcTemplate().update(INSERT_PIB_LEGAL_CONTACT,  projectInitiation.getProjectID(),projectInitiation.getEndMarketID(),
                    projectInitiation.getGlobalLegalContact());
            return projectInitiation;
        } catch (DataAccessException daEx) {
            final String message = "Failed to SAVE Legal Contact for Projecr - " + projectInitiation.getProjectID();
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }

    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public ProjectInitiation updatePIBReporting(final ProjectInitiation projectInitiation) {
        try {
            getSimpleJdbcTemplate().update(UPDATE_PIB_REPORTING_BY_PROJECT_ID,
                    projectInitiation.getEndMarketID(),
                    BooleanUtils.toIntegerObject(projectInitiation.getTopLinePresentation()),
                    BooleanUtils.toIntegerObject(projectInitiation.getPresentation()),
                    BooleanUtils.toIntegerObject(projectInitiation.getFullreport()),
                    BooleanUtils.toIntegerObject(projectInitiation.getGlobalSummary()),
                    projectInitiation.getOtherReportingRequirements(),
                    projectInitiation.getOtherReportingRequirementsText(),
                    projectInitiation.getProjectID());
            return projectInitiation;
        } catch (DataAccessException daEx) {
            final String message = "Failed to UPDATE Reporting Info for PIB - " + projectInitiation.getProjectID() + " and DocID " ;
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updatePIBMultiMarketReporting(final ProjectInitiation projectInitiation) {
        try {
            getSimpleJdbcTemplate().update(UPDATE_PIB_REPORTING_BY_PROJECT_ID_END_MARKET_ID,
                    projectInitiation.getEndMarketID(),
                    BooleanUtils.toIntegerObject(projectInitiation.getTopLinePresentation()),
                    BooleanUtils.toIntegerObject(projectInitiation.getPresentation()),
                    BooleanUtils.toIntegerObject(projectInitiation.getFullreport()),
                    BooleanUtils.toIntegerObject(projectInitiation.getGlobalSummary()),
                    projectInitiation.getOtherReportingRequirements(),
                    projectInitiation.getOtherReportingRequirementsText(),
                    projectInitiation.getProjectID(), projectInitiation.getEndMarketID());
         //   return projectInitiation;
        } catch (DataAccessException daEx) {
            final String message = "Failed to UPDATE Reporting Info for PIB - " + projectInitiation.getProjectID() + " and End Market Id -- "+ projectInitiation.getEndMarketID() ;
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public ProjectInitiation updatePIBStakeholderList(final ProjectInitiation projectInitiation) {
        try {
            getSimpleJdbcTemplate().update(UPDATE_PIB_STAKEHOLDER_LIST_BY_PROJECT_ID,
                    projectInitiation.getEndMarketID(),
                    projectInitiation.getAgencyContact1(),
                    projectInitiation.getAgencyContact2(),
                    projectInitiation.getAgencyContact3(),
                    projectInitiation.getAgencyContact1Optional(),
                    projectInitiation.getAgencyContact2Optional(),
                    projectInitiation.getAgencyContact3Optional(),
                    projectInitiation.getGlobalLegalContact(),
                    projectInitiation.getGlobalProcurementContact(),
                    projectInitiation.getGlobalCommunicationAgency(),
                    projectInitiation.getProductContact(),
                    projectInitiation.getProjectID());
            return projectInitiation;
        } catch (DataAccessException daEx) {
            final String message = "Failed to UPDATE Stakeholder List for Project- " + projectInitiation.getProjectID()  ;
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updatePIBNonAgencyStakeholderList(final ProjectInitiation projectInitiation) {
        try {
            getSimpleJdbcTemplate().update(UPDATE_PIB_STAKEHOLDER_NON_AGENCYLIST_BY_PROJECT_ID,
                    projectInitiation.getEndMarketID(),
                    projectInitiation.getGlobalLegalContact(),
                    projectInitiation.getGlobalProcurementContact(),
                    projectInitiation.getGlobalCommunicationAgency(),
					projectInitiation.getProductContact(),
                    projectInitiation.getProjectID());
        
        } catch (DataAccessException daEx) {
            final String message = "Failed to UPDATE Non Agency Stakeholder List for Project - " + projectInitiation.getProjectID()  ;
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public ProjectInitiation updatePIBMultiMarketStakeholderList(final ProjectInitiation projectInitiation)
    {
    	 try {
             getSimpleJdbcTemplate().update(UPDATE_PIB_STAKEHOLDER_LIST_BY_PROJECT_ID_END_MARKET_ID,
                     projectInitiation.getEndMarketID(),
                     projectInitiation.getAgencyContact1(),
                     projectInitiation.getAgencyContact2(),
                     projectInitiation.getAgencyContact3(),
                     projectInitiation.getAgencyContact1Optional(),
                     projectInitiation.getAgencyContact2Optional(),
                     projectInitiation.getAgencyContact3Optional(),
                     projectInitiation.getGlobalLegalContact(),
                     projectInitiation.getGlobalProcurementContact(),
                     projectInitiation.getGlobalCommunicationAgency(),
					 projectInitiation.getProductContact(),
                     projectInitiation.getProjectID(),projectInitiation.getEndMarketID());
             return projectInitiation;
         } catch (DataAccessException daEx) {
             final String message = "Failed to UPDATE Reporting Info for RIB - " + projectInitiation.getProjectID() + " and End Market Id -- "+ projectInitiation.getEndMarketID() ;
             LOG.log(Level.SEVERE, message, daEx);
             throw new DAOException(message, daEx);
         }
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updatePIBMultiMarketLegalContact(final ProjectInitiation projectInitiation)
    {
    	
    	final int count = getSimpleJdbcTemplate().queryForInt("SELECT count(endmarketid) from grailpibstakeholderlist " +
                "WHERE projectid = ? AND endmarketid = ?",
                projectInitiation.getProjectID(), projectInitiation.getEndMarketID());
        if(count==0)
        {
        	savePIBLegalContact(projectInitiation);
           
        }
        else
        {
	    	try {
	             getSimpleJdbcTemplate().update(UPDATE_PIB_STAKEHOLDER_LEGAL_CONTACT,
	                     projectInitiation.getGlobalLegalContact(),
	                     projectInitiation.getProjectID(),projectInitiation.getEndMarketID());
	             
	         } catch (DataAccessException daEx) {
	             final String message = "Failed to UPDATE PIB Legal Contact Info for Project - " + projectInitiation.getProjectID() + " and End Market Id -- "+ projectInitiation.getEndMarketID() ;
	             LOG.log(Level.SEVERE, message, daEx);
	             throw new DAOException(message, daEx);
	         }
        }
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateOtherSPIContact(final String otherSPIContact, final Long projectID, final Long endMarketID) {
       
    	final int count = getSimpleJdbcTemplate().queryForInt("SELECT count(endmarketid) from grailpibstakeholderlist " +
                "WHERE projectid = ? AND endmarketid = ?",
                projectID, endMarketID);
        if(count==0)
        {
        	saveOtherSPIContact(otherSPIContact, projectID, endMarketID);
           
        }
        else
        {
	    	try {
	            getSimpleJdbcTemplate().update(UPDATE_OTHER_SPI_CONTACT,
	            		otherSPIContact,
	            		projectID, endMarketID);
	            
	        } catch (DataAccessException daEx) {
	            final String message = "Failed to UPDATE Other SPI Contact for Project- " + projectID  ;
	            LOG.log(Level.SEVERE, message, daEx);
	            throw new DAOException(message, daEx);
	        }
        }
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void saveOtherSPIContact(final String otherSPIContact, final Long projectID, final Long endMarketID) {
        try {
            getSimpleJdbcTemplate().update(INSERT_OTHER_SPI_CONTACT,  projectID,endMarketID,
            		otherSPIContact);
             } catch (DataAccessException daEx) {
            final String message = "Failed to SAVE Other SPI Contact Contact for Projecr - " + projectID;
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }

    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateOtherLegalContact(final String otherLegalContact, final Long projectID, final Long endMarketID) {
       
    	final int count = getSimpleJdbcTemplate().queryForInt("SELECT count(endmarketid) from grailpibstakeholderlist " +
                "WHERE projectid = ? AND endmarketid = ?",
                projectID, endMarketID);
        if(count==0)
        {
        	saveOtherLegalContact(otherLegalContact,projectID,endMarketID);
           
        }
        else
        {
	    	try {
	            getSimpleJdbcTemplate().update(UPDATE_OTHER_LEGAL_CONTACT,
	            		otherLegalContact,
	            		projectID, endMarketID);
	            
	        } catch (DataAccessException daEx) {
	            final String message = "Failed to UPDATE Other LEGAL Contact for Project- " + projectID  ;
	            LOG.log(Level.SEVERE, message, daEx);
	            throw new DAOException(message, daEx);
	        }
        }
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void saveOtherLegalContact(final String otherLegalContact, final Long projectID, final Long endMarketID) {
        try {
            getSimpleJdbcTemplate().update(INSERT_OTHER_LEGAL_CONTACT,  projectID,endMarketID,
            		otherLegalContact);
             } catch (DataAccessException daEx) {
            final String message = "Failed to SAVE Other LEGAL Contact Contact for Projecr - " + projectID;
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }

    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateOtherProductContact(final String otherProductContact, final Long projectID, final Long endMarketID) {
       
    	final int count = getSimpleJdbcTemplate().queryForInt("SELECT count(endmarketid) from grailpibstakeholderlist " +
                "WHERE projectid = ? AND endmarketid = ?",
                projectID, endMarketID);
        if(count==0)
        {
        	saveOtherProductContact(otherProductContact,projectID,endMarketID);
           
        }
        else
        {
	    	try {
	            getSimpleJdbcTemplate().update(UPDATE_OTHER_PRODUCT_CONTACT,
	            		otherProductContact,
	            		projectID, endMarketID);
	            
	        } catch (DataAccessException daEx) {
	            final String message = "Failed to UPDATE Other PRODUCT Contact for Project- " + projectID  ;
	            LOG.log(Level.SEVERE, message, daEx);
	            throw new DAOException(message, daEx);
	        }
        }
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void saveOtherProductContact(final String otherProductContact, final Long projectID, final Long endMarketID) {
        try {
            getSimpleJdbcTemplate().update(INSERT_OTHER_PRODUCT_CONTACT,  projectID,endMarketID,
            		otherProductContact);
             } catch (DataAccessException daEx) {
            final String message = "Failed to SAVE Other PRODUCT Contact Contact for Projecr - " + projectID;
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }

    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateOtherAgencyContact(final String otherAgencyContact, final Long projectID, final Long endMarketID) {
       
    	final int count = getSimpleJdbcTemplate().queryForInt("SELECT count(endmarketid) from grailpibstakeholderlist " +
                "WHERE projectid = ? AND endmarketid = ?",
                projectID, endMarketID);
        if(count==0)
        {
        	saveOtherAgencyContact(otherAgencyContact,projectID,endMarketID);
           
        }
        else
        {
	    	try {
	            getSimpleJdbcTemplate().update(UPDATE_OTHER_AGENCY_CONTACT,
	            		otherAgencyContact,
	            		projectID, endMarketID);
	            
	        } catch (DataAccessException daEx) {
	            final String message = "Failed to UPDATE Other Agency Contact for Project- " + projectID  ;
	            LOG.log(Level.SEVERE, message, daEx);
	            throw new DAOException(message, daEx);
	        }
        }
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void saveOtherAgencyContact(final String otherAgencyContact, final Long projectID, final Long endMarketID) {
        try {
            getSimpleJdbcTemplate().update(INSERT_OTHER_AGENCY_CONTACT,  projectID,endMarketID,
            		otherAgencyContact);
             } catch (DataAccessException daEx) {
            final String message = "Failed to SAVE Other Agency Contact Contact for Projecr - " + projectID;
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }

    }
    @Override
    public String getOtherSPIContact(final Long projectID, final Long endMarketId) {
    		
    	
    	String SELECT_OTHER_SPI_CONTACT = "SELECT otherspicontact from grailpibstakeholderlist " +
                " where projectID = ? and endmarketid = ?";
        String otherSpiContact="";
        List<String> otherSpiContactList = new ArrayList<String>();
        try {
            int count = getSimpleJdbcTemplate().queryForInt("SELECT count(*) from grailpibstakeholderlist where projectID = ?", projectID);
            if(count>0)
            {
            	otherSpiContactList = getSimpleJdbcTemplate().getJdbcOperations().queryForList(SELECT_OTHER_SPI_CONTACT,String.class, projectID, endMarketId);
            }
            
        } catch (DataAccessException e) {
            final String message = "Failed to get other spi contact  for projectID - " + projectID;
            LOG.log(Level.SEVERE, message, e);
            throw new DAOException(message, e);
        }
        if(otherSpiContactList.size()>0)
        {
        	otherSpiContact = otherSpiContactList.get(0);
        }
        return otherSpiContact;
    	
    }
    
    @Override
    public String getOtherLegalContact(final Long projectID, final Long endMarketId) {
    		
    	
    	String SELECT_OTHER_LEGAL_CONTACT = "SELECT otherlegalcontact from grailpibstakeholderlist " +
                " where projectID = ? and endmarketid = ?";
        String otherLegalContact="";
        List<String> otherLegalContactList = new ArrayList<String>();
        try {
            int count = getSimpleJdbcTemplate().queryForInt("SELECT count(*) from grailpibstakeholderlist where projectID = ?", projectID);
            if(count>0)
            {
            	otherLegalContactList = getSimpleJdbcTemplate().getJdbcOperations().queryForList(SELECT_OTHER_LEGAL_CONTACT,String.class, projectID, endMarketId);
            }
            
        } catch (DataAccessException e) {
            final String message = "Failed to get other Legal contact  for projectID - " + projectID;
            LOG.log(Level.SEVERE, message, e);
            throw new DAOException(message, e);
        }
        if(otherLegalContactList.size()>0)
        {
        	otherLegalContact = otherLegalContactList.get(0);
        }
        return otherLegalContact;
    	
    }
    
    @Override
    public String getOtherProductContact(final Long projectID, final Long endMarketId) {
    		
    	
    	String SELECT_OTHER_PRODUCT_CONTACT = "SELECT otherproductcontact from grailpibstakeholderlist " +
                " where projectID = ? and endmarketid = ?";
        String otherProductContact="";
        List<String> otherProductContactList = new ArrayList<String>();
        try {
            int count = getSimpleJdbcTemplate().queryForInt("SELECT count(*) from grailpibstakeholderlist where projectID = ?", projectID);
            if(count>0)
            {
            	otherProductContactList = getSimpleJdbcTemplate().getJdbcOperations().queryForList(SELECT_OTHER_PRODUCT_CONTACT,String.class, projectID, endMarketId);
            }
            
        } catch (DataAccessException e) {
            final String message = "Failed to get other Product contact  for projectID - " + projectID;
            LOG.log(Level.SEVERE, message, e);
            throw new DAOException(message, e);
        }
        if(otherProductContactList.size()>0)
        {
        	otherProductContact = otherProductContactList.get(0);
        }
        return otherProductContact;
    	
    }

    @Override
    public String getOtherAgencyContact(final Long projectID, final Long endMarketId) {
    		
    	
    	String SELECT_OTHER_AGENCY_CONTACT = "SELECT otheragencycontact from grailpibstakeholderlist " +
                " where projectID = ? and endmarketid = ?";
        String otherAgencyContact="";
        List<String> otherAgencyContactList = new ArrayList<String>();
        try {
            int count = getSimpleJdbcTemplate().queryForInt("SELECT count(*) from grailpibstakeholderlist where projectID = ?", projectID);
            if(count>0)
            {
            	otherAgencyContactList = getSimpleJdbcTemplate().getJdbcOperations().queryForList(SELECT_OTHER_AGENCY_CONTACT,String.class, projectID, endMarketId);
            }
            
        } catch (DataAccessException e) {
            final String message = "Failed to get other Agency contact  for projectID - " + projectID;
            LOG.log(Level.SEVERE, message, e);
            throw new DAOException(message, e);
        }
        if(otherAgencyContactList.size()>0)
        {
        	otherAgencyContact = otherAgencyContactList.get(0);
        }
        return otherAgencyContact;
    	
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateAgencyContact(final Long projectID, final Long endMarketId, final Long updatedAgencyContact) {
    	
       
        try {
            int count = getSimpleJdbcTemplate().queryForInt("SELECT count(*) from grailpibstakeholderlist where projectID = ? and endmarketid = ?", projectID, endMarketId);
            if(count>0)
            {
            	try {
                    getSimpleJdbcTemplate().update(UPDATE_AGENCY_CONTACT,
                    		updatedAgencyContact,
                    		projectID,
                    		endMarketId);
                
                } catch (DataAccessException daEx) {
                    final String message = "Failed to UPDATE Agency Stakeholder List for Project - " + projectID  ;
                    LOG.log(Level.SEVERE, message, daEx);
                    throw new DAOException(message, daEx);
                }
            }
            else
            {
            	 try {
                     getSimpleJdbcTemplate().update(INSERT_AGENCY_CONTACT,  projectID,endMarketId,
                    		 updatedAgencyContact);
                      } catch (DataAccessException daEx) {
                     final String message = "Failed to SAVE Agency Contact Contact for Project - " + projectID;
                     LOG.log(Level.SEVERE, message, daEx);
                     throw new DAOException(message, daEx);
                 }
            }
            
        } catch (DataAccessException e) {
            final String message = "Failed to get other Agency contact  for projectID - " + projectID;
            LOG.log(Level.SEVERE, message, e);
            throw new DAOException(message, e);
        }
       
    	
    	
    }
    @Override
    public List<PIBReporting> getPIBReporting(final Long projectID) {
        return getSimpleJdbcTemplate().query(GET_PIB_REPORTING_BY_PROJECT_ID, pibReportingRowMapper, projectID);
    }
    @Override
    public List<PIBReporting> getPIBReporting(final Long projectID, final Long endMarketId) {
        return getSimpleJdbcTemplate().query(GET_PIB_REPORTING_BY_PROJECT_ID_END_MARKET_ID, pibReportingRowMapper, projectID,endMarketId);
    }
    @Override
    public List<PIBStakeholderList> getPIBStakeholderList(final Long projectID, final Long endMakerketId) {
        return getSimpleJdbcTemplate().query(GET_PIB_STAKEHOLDER_BY_PROJECT_ENDMARKET_ID, pibStakeholderListRowMapper, projectID, endMakerketId);
    }
    
    @Override
    public List<PIBStakeholderList> getPIBStakeholderList(final Long projectID) {
        return getSimpleJdbcTemplate().query(GET_PIB_STAKEHOLDER_BY_PROJECT_ID, pibStakeholderListRowMapper, projectID);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public ProjectInitiation save(final ProjectInitiation projectInitiation) {
        try {
            
        	final int count = getSimpleJdbcTemplate().queryForInt("SELECT count(projectid) from grailpib " +
                    "WHERE projectid = ?",
                    projectInitiation.getProjectID());
            if(count==0)
            {
            
	        	/*getSimpleJdbcTemplate().update(INSERT_NEW_PIB_SQL, projectInitiation.getProjectID(), projectInitiation.getBrief(),
	                    projectInitiation.getBriefText(),
	                    projectInitiation.getBriefLegalApprover(), 1, 1,
	                    projectInitiation.getCreationBy(), projectInitiation.getModifiedBy(), projectInitiation.getCreationDate(), projectInitiation.getModifiedDate());
	        	*/
	        	getSimpleJdbcTemplate().update(INSERT_PIB_SQL, projectInitiation.getProjectID(), projectInitiation.getEndMarketID(),
	                    projectInitiation.getBizQuestion(),
	                    projectInitiation.getResearchObjective(), projectInitiation.getActionStandard(),projectInitiation.getResearchDesign(),
	                    projectInitiation.getSampleProfile(), projectInitiation.getStimulusMaterial(),projectInitiation.getOthers(),
	                    ((projectInitiation.getStimuliDate() != null) ? projectInitiation.getStimuliDate().getTime():null),
	                    projectInitiation.getLatestEstimate(),projectInitiation.getLatestEstimateType(), projectInitiation.getNpiReferenceNo(),
	                    projectInitiation.getDeviationFromSM()!=null?projectInitiation.getDeviationFromSM():0,
	                    projectInitiation.getCreationBy(), projectInitiation.getModifiedBy(), projectInitiation.getCreationDate(), projectInitiation.getModifiedDate(), projectInitiation.getStatus(),
	                    BooleanUtils.toIntegerObject(projectInitiation.getLegalApprovalRcvd()),BooleanUtils.toIntegerObject(projectInitiation.getLegalApprovalNotReq()),projectInitiation.getLegalApprover(),
						projectInitiation.getHasTenderingProcess(),
	                    projectInitiation.getFieldworkCost(),
	                    projectInitiation.getFieldworkCostCurrency(),
	                    projectInitiation.getBizQuestionText(),
	                    projectInitiation.getResearchObjectiveText(),
	                    projectInitiation.getActionStandardText(),
	                    projectInitiation.getResearchDesignText(),
	                    projectInitiation.getSampleProfileText(),
	                    projectInitiation.getStimulusMaterialText(),
	                    projectInitiation.getOthersText(),
	                    projectInitiation.getNonKantar(),
	                    projectInitiation.getPibSaveDate()!=null?projectInitiation.getPibSaveDate().getTime():0,
	                    projectInitiation.getPibLegalApprovalDate()!=null?projectInitiation.getPibLegalApprovalDate().getTime():0,
	                    projectInitiation.getPibCompletionDate()!=null?projectInitiation.getPibCompletionDate().getTime():0,
	                    projectInitiation.getBrief(), projectInitiation.getBriefText(), projectInitiation.getBriefLegalApprover(), projectInitiation.getLegalSignOffRequired(),
	                    projectInitiation.getLegalApprovalDate()!=null?projectInitiation.getLegalApprovalDate().getTime():0,
	                    projectInitiation.getLegalApprovalStatus(),
	                    projectInitiation.getBriefLegalApproverOffline());
	                   
            }
            return projectInitiation;
        } catch (DataAccessException daEx) {
            final String message = "Failed to SAVE PIB for projectID" + projectInitiation.getProjectID();
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public ProjectInitiation saveOtherEM(final ProjectInitiation projectInitiation) {
        try {
            getSimpleJdbcTemplate().update(INSERT_PIB_OTHER_EM_SQL, projectInitiation.getProjectID(), projectInitiation.getEndMarketID(),
                    projectInitiation.getBizQuestion(),
                    projectInitiation.getResearchObjective(), projectInitiation.getActionStandard(),projectInitiation.getResearchDesign(),
                    projectInitiation.getSampleProfile(), projectInitiation.getStimulusMaterial(),projectInitiation.getOthers(),
                    ((projectInitiation.getStimuliDate() != null) ? projectInitiation.getStimuliDate().getTime():null),
                    projectInitiation.getDeviationFromSM()!=null?projectInitiation.getDeviationFromSM():0,
                    projectInitiation.getNpiReferenceNo(),
                    projectInitiation.getCreationBy(), projectInitiation.getModifiedBy(), projectInitiation.getCreationDate(), projectInitiation.getModifiedDate(), projectInitiation.getStatus(),
					projectInitiation.getHasTenderingProcess(),
                    projectInitiation.getFieldworkCost(),
                    projectInitiation.getFieldworkCostCurrency(),
                    projectInitiation.getBizQuestionText(),
                    projectInitiation.getResearchObjectiveText(),
                    projectInitiation.getActionStandardText(),
                    projectInitiation.getResearchDesignText(),
                    projectInitiation.getSampleProfileText(),
                    projectInitiation.getStimulusMaterialText(),
                    projectInitiation.getOthersText());
            return projectInitiation;
        } catch (DataAccessException daEx) {
            final String message = "Failed to SAVE PIB for projectID" + projectInitiation.getProjectID();
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
  

    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public ProjectInitiation update(final ProjectInitiation projectInitiation) {
        try{
            getSimpleJdbcTemplate().update( UPDATE_PIB_BY_PROJECT_ID,
                    //projectInitiation.getVersionNumber(),
                    projectInitiation.getEndMarketID(),
                    projectInitiation.getBizQuestion(),
                    projectInitiation.getResearchObjective(),
                    projectInitiation.getActionStandard(),
                    projectInitiation.getResearchDesign(),
                    projectInitiation.getSampleProfile(),
                    projectInitiation.getStimulusMaterial(),
                    projectInitiation.getOthers(),
                    ((projectInitiation.getStimuliDate() != null)?projectInitiation.getStimuliDate().getTime():null),
                    projectInitiation.getLatestEstimate(),
                    projectInitiation.getLatestEstimateType(),
                    projectInitiation.getNpiReferenceNo(),
                    projectInitiation.getDeviationFromSM(),
                    projectInitiation.getModifiedBy(),
                    projectInitiation.getModifiedDate(),
                    projectInitiation.getStatus(),
                    BooleanUtils.toIntegerObject(projectInitiation.getLegalApprovalRcvd()),
                    BooleanUtils.toIntegerObject(projectInitiation.getLegalApprovalNotReq()),
                    projectInitiation.getLegalApprover(),
                    projectInitiation.getHasTenderingProcess(),
                    projectInitiation.getFieldworkCost(),
                    projectInitiation.getFieldworkCostCurrency(),
                    projectInitiation.getBizQuestionText(),
                    projectInitiation.getResearchObjectiveText(),
                    projectInitiation.getActionStandardText(),
                    projectInitiation.getResearchDesignText(),
                    projectInitiation.getSampleProfileText(),
                    projectInitiation.getStimulusMaterialText(),
                    projectInitiation.getOthersText(),
                    //BooleanUtils.toIntegerObject(projectInitiation.getNonKantar()),
                    projectInitiation.getNonKantar(),
                    projectInitiation.getModifiedDate(),
                    projectInitiation.getModifiedDate(),
                    projectInitiation.getModifiedDate(),
                    //where clause
                    projectInitiation.getProjectID());
            return projectInitiation;
        } catch (DataAccessException daEx) {
            final String message = "Failed to UPDATE Research Initiation Brief for projectID  " + projectInitiation.getProjectID() ;
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public ProjectInitiation updateNew(final ProjectInitiation projectInitiation) {
        try{
            getSimpleJdbcTemplate().update( UPDATE_PIB_NEW_BY_PROJECT_ID,
                    projectInitiation.getBrief(),
                    projectInitiation.getBriefText(),
                    projectInitiation.getBriefLegalApprover(),
                    projectInitiation.getModifiedBy(),
                    projectInitiation.getModifiedDate(),
                    projectInitiation.getStatus(),
                    projectInitiation.getLegalApprovalDate()!=null?projectInitiation.getLegalApprovalDate().getTime():0,
                    projectInitiation.getLegalApprovalStatus(),
                    projectInitiation.getLegalSignOffRequired(),
                    projectInitiation.getBriefLegalApproverOffline(),
                    
                    //where clause
                    projectInitiation.getProjectID());
            return projectInitiation;
        } catch (DataAccessException daEx) {
            final String message = "Failed to UPDATE Research Initiation Brief for projectID  " + projectInitiation.getProjectID() ;
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updatePIBDeviation(final ProjectInitiation projectInitiation) {
        try{
            getSimpleJdbcTemplate().update( UPDATE_PIB_DEVIATION_BY_PROJECT_ID,
                    projectInitiation.getDeviationFromSM(),
                    projectInitiation.getModifiedBy(),
                    projectInitiation.getModifiedDate(),
                    
                    //where clause
                    projectInitiation.getProjectID());
          
        } catch (DataAccessException daEx) {
            final String message = "Failed to UPDATE PIB Deviation for projectID  " + projectInitiation.getProjectID() ;
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
        
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public ProjectInitiation updateMultiMarket(final ProjectInitiation projectInitiation) {
        try{
            getSimpleJdbcTemplate().update( UPDATE_PIB_BY_PROJECT_ID_END_MARKET_ID,
                    //projectInitiation.getVersionNumber(),
                    projectInitiation.getEndMarketID(),
                    projectInitiation.getBizQuestion(),
                    projectInitiation.getResearchObjective(),
                    projectInitiation.getActionStandard(),
                    projectInitiation.getResearchDesign(),
                    projectInitiation.getSampleProfile(),
                    projectInitiation.getStimulusMaterial(),
                    projectInitiation.getOthers(),
                    ((projectInitiation.getStimuliDate() != null)?projectInitiation.getStimuliDate().getTime():null),
                    projectInitiation.getLatestEstimate(),
                    projectInitiation.getLatestEstimateType(),
                    projectInitiation.getNpiReferenceNo(),
                    projectInitiation.getDeviationFromSM(),
                    projectInitiation.getModifiedBy(),
                    projectInitiation.getModifiedDate(),
                    projectInitiation.getStatus(),
                    BooleanUtils.toIntegerObject(projectInitiation.getLegalApprovalRcvd()),
                    BooleanUtils.toIntegerObject(projectInitiation.getLegalApprovalNotReq()),
                    projectInitiation.getLegalApprover(),
                    BooleanUtils.toIntegerObject(projectInitiation.getIsEndMarketChanged()),
					projectInitiation.getHasTenderingProcess(),
                    projectInitiation.getFieldworkCost(),
                    projectInitiation.getFieldworkCostCurrency(),
                    projectInitiation.getBizQuestionText(),
                    projectInitiation.getResearchObjectiveText(),
                    projectInitiation.getActionStandardText(),
                    projectInitiation.getResearchDesignText(),
                    projectInitiation.getSampleProfileText(),
                    projectInitiation.getStimulusMaterialText(),
                    projectInitiation.getOthersText(),
                    //BooleanUtils.toIntegerObject(projectInitiation.getNonKantar()),
                    projectInitiation.getNonKantar(),
                    projectInitiation.getModifiedDate(),
                    projectInitiation.getModifiedDate(),
                    
                    //where clause
                    projectInitiation.getProjectID(),
                    projectInitiation.getEndMarketID());
            return projectInitiation;
        } catch (DataAccessException daEx) {
            final String message = "Failed to UPDATE Research Initiation Brief for projectID  " + projectInitiation.getProjectID() ;
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateMultiMarketOtherEM(final ProjectInitiation projectInitiation) {
       
    	final int count = getSimpleJdbcTemplate().queryForInt("SELECT count(endmarketid) from grailpib " +
                "WHERE projectid = ? AND endmarketid = ?",
                projectInitiation.getProjectID(), projectInitiation.getEndMarketID());
        if(count==0)
        {
           saveOtherEM(projectInitiation);
        }
        else
        {
        	try{
                getSimpleJdbcTemplate().update( UPDATE_PIB_BY_PROJECT_ID_END_MARKET_ID_OTHER,
                        projectInitiation.getEndMarketID(),
                        projectInitiation.getBizQuestion(),
                        projectInitiation.getResearchObjective(),
                        projectInitiation.getActionStandard(),
                        projectInitiation.getResearchDesign(),
                        projectInitiation.getSampleProfile(),
                        projectInitiation.getStimulusMaterial(),
                        projectInitiation.getOthers(),
                        ((projectInitiation.getStimuliDate() != null)?projectInitiation.getStimuliDate().getTime():null),
                        projectInitiation.getNpiReferenceNo(),
                        
                        projectInitiation.getModifiedBy(),
                        projectInitiation.getModifiedDate(),
                       // projectInitiation.getStatus(),
                        projectInitiation.getBizQuestionText(),
                        projectInitiation.getResearchObjectiveText(),
                        projectInitiation.getActionStandardText(),
                        projectInitiation.getResearchDesignText(),
                        projectInitiation.getSampleProfileText(),
                        projectInitiation.getStimulusMaterialText(),
                        projectInitiation.getOthersText(),
                        //BooleanUtils.toIntegerObject(projectInitiation.getNonKantar()),
                        projectInitiation.getNonKantar(),
                        
                        //where clause
                        projectInitiation.getProjectID(),
                        projectInitiation.getEndMarketID());
                
            } catch (DataAccessException daEx) {
                final String message = "Failed to UPDATE Research Initiation Brief for projectID  " + projectInitiation.getProjectID() ;
                LOG.log(Level.SEVERE, message, daEx);
                throw new DAOException(message, daEx);
            }
        } 
    	
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateLatestEstimate(final ProjectInitiation projectInitiation) {
        try{
            getSimpleJdbcTemplate().update( UPDATE_PIB_LATEST_ESTIMATE,
                    projectInitiation.getLatestEstimate(),
                    projectInitiation.getLatestEstimateType(),
                    projectInitiation.getProjectID());
          
        } catch (DataAccessException daEx) {
            final String message = "Failed to UPDATE Latest Estimate for projectID  " + projectInitiation.getProjectID() ;
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
        
    }
    
    @Override
    public List<ProjectInitiation> getProjectInitiation(final Long projectID) {
        return getSimpleJdbcTemplate().query(GET_PIB_BY_PROJECT_ID, projectInitiationRowMapper, projectID);
    }
    @Override
    public List<ProjectInitiation> getProjectInitiationNew(final Long projectID) {
        return getSimpleJdbcTemplate().query(GET_PIB_NEW_BY_PROJECT_ID, newProjectInitiationRowMapper, projectID);
    }
    
    @Override
    public List<ProjectInitiation> getProjectInitiationPendingActivities(ProjectResultFilter projectFilter) {
    	StringBuilder pibDetailsSQL = new StringBuilder();
    	pibDetailsSQL.append("select * from grailpib where legalapprover = ? and sendforapproval = 1 ");
        return getSimpleJdbcTemplate().query(pibDetailsSQL.toString(), newProjectInitiationRowMapper, 2102);
    }
    
    @Override
    public List<ProjectInitiation> getProjectInitiation(final Long projectID, final Long endMarketId) {
        return getSimpleJdbcTemplate().query(GET_PIB_BY_PROJECT_ID_END_MARKET_ID, projectInitiationRowMapper, projectID,endMarketId);
    }
    @Override
    public int addDocumentAttachment(final Long projectId, final Long endMarketId, final Long fieldCategoryId,
                                     final Long attachmentId, final Long userId) {

        try {

            getSimpleJdbcTemplate().getJdbcOperations().update(
                    INSERT_ATTACHMENT, projectId, endMarketId, fieldCategoryId,
                    attachmentId, userId);
            return attachmentId.intValue();
        } catch (DataAccessException daEx) {
            final String message = "Failed to insert attachment details for project  - "
                    + projectId + " and attachmentId --" + attachmentId;
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    @Override
    public boolean removeDocumentAttachment(final Long attachmentId) {
        boolean success = false;
        try {
            getSimpleJdbcTemplate().getJdbcOperations().update(REMOVE_ATTACHMENT, attachmentId);
            success = true;
        } catch (DataAccessException daEx) {
            final String message = "Failed to remove document Attachment -- " + attachmentId;
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);

        }
        return success;
    }
    @Override
    public void updateDocumentAttachment(Long attachmentId, long objectId)
    {
    	try{
            getSimpleJdbcTemplate().update( UPDATE_ATTACHMENT_OBJECT_ID, objectId,attachmentId);
          
        } catch (DataAccessException daEx) {
            final String message = "Failed to UPDATE Object Id for Attachment Id --  " + attachmentId ;
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    @Override
    public void saveAttachmentUser(final Long attachmentId, Long userId) {
        try {
            getSimpleJdbcTemplate().update(INSERT_ATTACHMENT_USER,  attachmentId,userId);
            
        } catch (DataAccessException daEx) {
            final String message = "Failed to SAVE Attachment User for Attachment Id - " + attachmentId;
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }

    }
    @Override
    public void deleteAttachmentUser(final Long attachmentId) {
    	try {
            getSimpleJdbcTemplate().update(DELETE_ATTACHMENT_USER,  attachmentId);
            
        } catch (DataAccessException daEx) {
            final String message = "Failed to Delete Attachment User for Attachment Id - " + attachmentId;
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }

    }
    @Override
    public Long getAttachmentUser(final Long attachmentId) {
    	Long userId=null;
    	try {
    			userId = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(GET_ATTACHMENT_USER, attachmentId);
            
            
        } catch (DataAccessException daEx) {
           // final String message = "Failed to Get Attachment User for Attachment Id - " + attachmentId;
           // LOG.log(Level.SEVERE, message, daEx);
           // throw new DAOException(message, daEx);
        }
    	return userId;
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void savePIBMethodologyWaiver(final PIBMethodologyWaiver pibWaiver) {
        try {
            final int count = getSimpleJdbcTemplate().queryForInt("SELECT count(endmarketid) from grailpibmethodologywaiver " +
                    "WHERE projectid = ? AND endmarketid = ?",
                    pibWaiver.getProjectID(), pibWaiver.getEndMarketID());
            if(count==0)
            {
                getSimpleJdbcTemplate().update(INSERT_PIB_METHODOLOGY_WAIVER, pibWaiver.getProjectID(), pibWaiver.getEndMarketID(),
                        pibWaiver.getMethodologyDeviationRationale(), pibWaiver.getMethodologyApprover(),
                        pibWaiver.getCreationBy(), pibWaiver.getModifiedBy(), pibWaiver.getCreationDate(), pibWaiver.getModifiedDate(), pibWaiver.getStatus());
            }
            else
            {
                updatePIBMethodologyWaiver(pibWaiver);
            }

        } catch (DataAccessException daEx) {
            final String message = "Failed to SAVE PIB Methodology Waiver for projectID" + pibWaiver.getProjectID() + " and EndMarket --"+ pibWaiver.getEndMarketID();
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void savePIBKantarMethodologyWaiver(final PIBMethodologyWaiver pibWaiver) {
        try {
            final int count = getSimpleJdbcTemplate().queryForInt("SELECT count(endmarketid) from grailpibkantarmw " +
                    "WHERE projectid = ? AND endmarketid = ?",
                    pibWaiver.getProjectID(), pibWaiver.getEndMarketID());
            if(count==0)
            {
            	ProjectWaiver projectWaiver = new ProjectWaiver();
            	projectWaiver.setCreationBy(pibWaiver.getCreationBy());
            	projectWaiver.setModifiedBy(pibWaiver.getModifiedBy());
            	projectWaiver.setCreationDate(pibWaiver.getCreationDate());
            	projectWaiver.setModifiedDate(pibWaiver.getModifiedDate());
            	projectWaiver.setIsKantar(true);
            	projectWaiver.setStatus(SynchroGlobal.ProjectWaiverStatus.PENDING_APPROVAL.value());
            	ProjectWaiver pw = projectWaiverDAO.create(projectWaiver);
                projectWaiver.setWaiverID(pw.getWaiverID());
                projectWaiver.setApproverID(pibWaiver.getMethodologyApprover());
                
                projectWaiverDAO.saveWaiverApprover(projectWaiver);
            	
            	getSimpleJdbcTemplate().update(INSERT_PIB_KANTAR_METHODOLOGY_WAIVER, pibWaiver.getProjectID(), pibWaiver.getEndMarketID(),
                        pibWaiver.getMethodologyDeviationRationale(), pibWaiver.getMethodologyApprover()!=null?pibWaiver.getMethodologyApprover():-1,
                        pibWaiver.getCreationBy(), pibWaiver.getModifiedBy(), pibWaiver.getCreationDate(), pibWaiver.getModifiedDate(), pibWaiver.getStatus(), pw.getWaiverID());
            }
            else
            {
                List<PIBMethodologyWaiver> pwList = getPIBKantarMethodologyWaiver(pibWaiver.getProjectID(), pibWaiver.getEndMarketID());
                if(pwList!=null && pwList.size()>0)
                {
                	ProjectWaiver projectWaiver = new ProjectWaiver();
                	projectWaiver.setCreationBy(pibWaiver.getCreationBy());
                	projectWaiver.setModifiedBy(pibWaiver.getModifiedBy());
                	projectWaiver.setCreationDate(pibWaiver.getCreationDate());
                	projectWaiver.setModifiedDate(pibWaiver.getModifiedDate());
                	
                    projectWaiver.setWaiverID(pwList.get(0).getWaiverID());
                    projectWaiver.setApproverID(pibWaiver.getMethodologyApprover()!=null?pibWaiver.getMethodologyApprover():-1);
                    
                    projectWaiverDAO.saveWaiverApprover(projectWaiver);
                    
                   	projectWaiver.setStatus(SynchroGlobal.ProjectWaiverStatus.PENDING_APPROVAL.value());
                                       
                    projectWaiverDAO.update(projectWaiver);
                   
                }
            	updatePIBKantarMethodologyWaiver(pibWaiver);
            }

        } catch (DataAccessException daEx) {
            final String message = "Failed to SAVE PIB Kantar Methodology Waiver for projectID" + pibWaiver.getProjectID() + " and EndMarket --"+ pibWaiver.getEndMarketID();
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updatePIBMethodologyWaiver(final PIBMethodologyWaiver pibWaiver) {
        try {

            getSimpleJdbcTemplate().update(UPDATE_PIB_METHODOLOGY_WAIVER,
                    pibWaiver.getMethodologyDeviationRationale(), pibWaiver.getMethodologyApprover()!=null?pibWaiver.getMethodologyApprover():-1,pibWaiver.getIsApproved(),
                    pibWaiver.getModifiedBy(), pibWaiver.getModifiedDate(), pibWaiver.getStatus(), pibWaiver.getProjectID(), pibWaiver.getEndMarketID());


        } catch (DataAccessException daEx) {
            final String message = "Failed to Update PIB Methodology Waiver for projectID" + pibWaiver.getProjectID() + " and EndMarket --"+ pibWaiver.getEndMarketID();
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updatePIBKantarMethodologyWaiver(final PIBMethodologyWaiver pibWaiver) {
        try {

            getSimpleJdbcTemplate().update(UPDATE_PIB_KANTAR_METHODOLOGY_WAIVER,
                    pibWaiver.getMethodologyDeviationRationale(), pibWaiver.getMethodologyApprover()!=null?pibWaiver.getMethodologyApprover():-1,pibWaiver.getIsApproved(),
                    pibWaiver.getModifiedBy(), pibWaiver.getModifiedDate(), pibWaiver.getStatus(), pibWaiver.getProjectID(), pibWaiver.getEndMarketID());


        } catch (DataAccessException daEx) {
            final String message = "Failed to Update PIB Kantar Methodology Waiver for projectID" + pibWaiver.getProjectID() + " and EndMarket --"+ pibWaiver.getEndMarketID();
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    @Override
    public Long getProjectId(final Long waiverId) {
    	Long projectId=null;
    	try {
    		projectId = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(GET_PROJECT_ID_FROM_WAIVER_ID, waiverId);
            
            
        } catch (DataAccessException daEx) {
           // final String message = "Failed to Get Attachment User for Attachment Id - " + attachmentId;
           // LOG.log(Level.SEVERE, message, daEx);
           // throw new DAOException(message, daEx);
        }
    	return projectId;
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updatePIBMethWaiverSingleEndMarketId(final Long projectID,
                                                     final Long endMarketID) {
        try {
            getSimpleJdbcTemplate().getJdbcOperations().update(
                    UPDATE_PIB_METH_WAIVER_SINGLE_ENDMARKET_ID, endMarketID, projectID);

        } catch (DataAccessException daEx) {
            final String message = "Failed to update the Single endmarket Id for Project "
                    + projectID;
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updatePIBReportingSingleEndMarketId(final Long projectID,
                                                    final Long endMarketID) {
        try {
            getSimpleJdbcTemplate().getJdbcOperations().update(
                    UPDATE_PIB_REPORTING_SINGLE_ENDMARKET_ID, endMarketID, projectID);

        } catch (DataAccessException daEx) {
            final String message = "Failed to update the Single endmarket Id for Project "
                    + projectID;
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updatePIBStakeholderListSingleEndMarketId(final Long projectID,
                                                          final Long endMarketID) {
        try {
            getSimpleJdbcTemplate().getJdbcOperations().update(
                    UPDATE_PIB_STAKEHOLDER_SINGLE_ENDMARKET_ID, endMarketID, projectID);

        } catch (DataAccessException daEx) {
            final String message = "Failed to update the Single endmarket Id for Project "
                    + projectID;
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
 
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void approvePIBMethodologyWaiver(final PIBMethodologyWaiver pibWaiver) {
        try {

            getSimpleJdbcTemplate().update(APPROVE_PIB_METHODOLOGY_WAIVER,
                    pibWaiver.getMethodologyApproverComment(), 1,
                    pibWaiver.getProjectID(), pibWaiver.getEndMarketID());


        } catch (DataAccessException daEx) {
            final String message = "Failed to Approve PIB Methodology Waiver for projectID" + pibWaiver.getProjectID() + " and EndMarket --"+ pibWaiver.getEndMarketID();
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void approvePIBKantarMethodologyWaiver(final PIBMethodologyWaiver pibWaiver) {
        try {

            getSimpleJdbcTemplate().update(APPROVE_PIB_KANTAR_METHODOLOGY_WAIVER,
                    pibWaiver.getMethodologyApproverComment(), 1,
                    pibWaiver.getProjectID(), pibWaiver.getEndMarketID());
            
            List<PIBMethodologyWaiver> pwList = getPIBKantarMethodologyWaiver(pibWaiver.getProjectID(), pibWaiver.getEndMarketID());
            if(pwList!=null && pwList.size()>0)
            {
            	ProjectWaiver projectWaiver = new ProjectWaiver();
            	projectWaiver.setCreationBy(pibWaiver.getCreationBy());
            	projectWaiver.setModifiedBy(pibWaiver.getModifiedBy());
            	projectWaiver.setCreationDate(pibWaiver.getCreationDate());
            	projectWaiver.setModifiedDate(pibWaiver.getModifiedDate());
            	projectWaiver.setWaiverID(pwList.get(0).getWaiverID());
            	projectWaiver.setStatus(SynchroGlobal.ProjectWaiverStatus.APPROVED.value());
                               
                projectWaiverDAO.update(projectWaiver);
            }


        } catch (DataAccessException daEx) {
            final String message = "Failed to Approve PIB KANTAR Methodology Waiver for projectID" + pibWaiver.getProjectID() + " and EndMarket --"+ pibWaiver.getEndMarketID();
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void rejectPIBMethodologyWaiver(final PIBMethodologyWaiver pibWaiver) {
        try {

            getSimpleJdbcTemplate().update(REJECT_PIB_METHODOLOGY_WAIVER,
            		pibWaiver.getMethodologyApproverComment(), 2,
                    pibWaiver.getProjectID(), pibWaiver.getEndMarketID());
            
          


        } catch (DataAccessException daEx) {
            final String message = "Failed to Reject PIB Methodology Waiver for projectID" + pibWaiver.getProjectID() + " and EndMarket --"+ pibWaiver.getEndMarketID();
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void rejectPIBKantarMethodologyWaiver(final PIBMethodologyWaiver pibWaiver) {
        try {

            getSimpleJdbcTemplate().update(REJECT_PIB_KANTAR_METHODOLOGY_WAIVER,
            		pibWaiver.getMethodologyApproverComment(), 2,
                    pibWaiver.getProjectID(), pibWaiver.getEndMarketID());
            
            List<PIBMethodologyWaiver> pwList = getPIBKantarMethodologyWaiver(pibWaiver.getProjectID(), pibWaiver.getEndMarketID());
            if(pwList!=null && pwList.size()>0)
            {
            	ProjectWaiver projectWaiver = new ProjectWaiver();
            	projectWaiver.setCreationBy(pibWaiver.getCreationBy());
            	projectWaiver.setModifiedBy(pibWaiver.getModifiedBy());
            	projectWaiver.setCreationDate(pibWaiver.getCreationDate());
            	projectWaiver.setModifiedDate(pibWaiver.getModifiedDate());
            	projectWaiver.setWaiverID(pwList.get(0).getWaiverID());
            	projectWaiver.setStatus(SynchroGlobal.ProjectWaiverStatus.REJECTED.value());
                               
                projectWaiverDAO.update(projectWaiver);
            }


        } catch (DataAccessException daEx) {
            final String message = "Failed to Reject PIB Kantar Methodology Waiver for projectID" + pibWaiver.getProjectID() + " and EndMarket --"+ pibWaiver.getEndMarketID();
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void reqForInfoPIBMethodologyWaiver(final PIBMethodologyWaiver pibWaiver) {
        try {

            getSimpleJdbcTemplate().update(REQ_FOR_INFO_PIB_METHODOLOGY_WAIVER,
            		pibWaiver.getMethodologyApproverComment(), SynchroGlobal.MethodologyWaiverStatus.PIB_METH_WAIV_MORE_INFO_REQ.ordinal(),
                    pibWaiver.getProjectID(), pibWaiver.getEndMarketID());


        } catch (DataAccessException daEx) {
            final String message = "Failed to Updtade Req For Methodology PIB Methodology Waiver for projectID" + pibWaiver.getProjectID() + " and EndMarket --"+ pibWaiver.getEndMarketID();
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void reqForInfoPIBKantarMethodologyWaiver(final PIBMethodologyWaiver pibWaiver) {
        try {

            getSimpleJdbcTemplate().update(REQ_FOR_INFO_PIB_KANTAR_METHODOLOGY_WAIVER,
            		pibWaiver.getMethodologyApproverComment(), SynchroGlobal.MethodologyWaiverStatus.PIB_METH_WAIV_MORE_INFO_REQ.ordinal(),
                    pibWaiver.getProjectID(), pibWaiver.getEndMarketID());


        } catch (DataAccessException daEx) {
            final String message = "Failed to Updtade Req For Methodology PIB Kantar Methodology Waiver for projectID" + pibWaiver.getProjectID() + " and EndMarket --"+ pibWaiver.getEndMarketID();
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }

    @Override
    public List<PIBMethodologyWaiver> getPIBMethodologyWaiver(final Long projectID,final Long endMakerketId)
    {
        return getSimpleJdbcTemplate().query(GET_PIB_METHODOLOGY_WAIVER, pibMethodologyWaiverRowMapper, projectID, endMakerketId);
    }
    
    @Override
    public List<PIBMethodologyWaiver> getPIBKantarMethodologyWaiver(final Long projectID,final Long endMakerketId)
    {
        return getSimpleJdbcTemplate().query(GET_PIB_KANTAR_METHODOLOGY_WAIVER, pibKantarMethodologyWaiverRowMapper, projectID, endMakerketId);
    }
    
    @Override
    public List<PIBMethodologyWaiver> getPIBMethodologyWaivers(final ProjectResultFilter projectResultFilter)
    {
    	List<PIBMethodologyWaiver> methodologyWaivers = Collections.emptyList();
    	/*StringBuilder sql = new StringBuilder();
    	
    	String waiverFields = "select project.projectid, project.name,project.budgetyear, waiver.methodologyapprover, waiver.isapproved, (select (u.firstname || ' ' || u.lastname) FROM jiveUser u where u.userid = waiver.methodologyapprover) as methodologyapproverName from grailpibmethodologywaiver waiver, grailproject project where waiver.projectid = project.projectid";
        sql = new StringBuilder(waiverFields);
        
        
     // Keyword filter
        if(projectResultFilter.getKeyword() != null && !projectResultFilter.getKeyword().equals("")) {
            StringBuilder keywordCondition = new StringBuilder();
            keywordCondition.append(" AND (lower(project.name) like '%").append(projectResultFilter.getKeyword().toLowerCase()).append("%'")
                    .append(" OR ").append("(''|| project.projectId ||'') like ").append("'%").append(synchroDAOUtil.getFormattedProjectCode(projectResultFilter.getKeyword())).append("%'")
                   // .append(" OR ").append("lower(methodologyapproverName) like ").append("'%").append(projectResultFilter.getKeyword().toLowerCase()).append("%'")
                    .append(" OR ").append("project.budgetyear = ").append(projectResultFilter.getKeyword().toLowerCase()).append(")");
            
            
            sql.append(keywordCondition.toString());
        }
        */
    	StringBuilder sql = new StringBuilder(getMethWaiverDashboardQuery(projectResultFilter,false));
        
        
        if(projectResultFilter.getSortField() != null && !projectResultFilter.getSortField().equals("")) {
            sql.append(" order by ").append(getOrderByField(projectResultFilter.getSortField())).append(" ").append(SynchroDAOUtil.getSortType(projectResultFilter.getAscendingOrder())).append(",").append(getOrderByField("id")).append(" ").append(SynchroDAOUtil.getSortType(0));
        } else {
            sql.append(" order by ").append(getOrderByField("id")).append(" ").append(SynchroDAOUtil.getSortType(0));
        }
        try {
            if(projectResultFilter.getStart() != null) {
                sql.append(" OFFSET ").append(projectResultFilter.getStart());
            }
            if(projectResultFilter.getLimit() != null && projectResultFilter.getLimit() > 0) {
                sql.append(" LIMIT ").append(projectResultFilter.getLimit());
            }
            methodologyWaivers = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), methodologyDashboardRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load methodology Waivers by filter";
        //    LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return methodologyWaivers;
    }
    
    
    private String getMethWaiverDashboardQuery(final ProjectResultFilter projectResultFilter, boolean fetchCount)
    {
    	StringBuilder methodologyWaiverSQL = new StringBuilder();
    	
    	String waiverFields = "";
    	if(fetchCount)
    	{
    		waiverFields = "select count(*) from grailpibmethodologywaiver waiver, grailproject project ";
    	}
    	else
    	{
    			
    		waiverFields = "select project.projectid, project.name,project.budgetyear, project.methodologydetails, waiver.creationby, waiver.modificationby, waiver.creationdate, waiver.modificationdate, waiver.methodologyapprover, waiver.isapproved, waiver.methodologydeviationrationale, waiver.methodologyapprovercomment, (select (u.firstname || ' ' || u.lastname) FROM jiveUser u where u.userid = waiver.methodologyapprover) as methodologyapproverName from grailpibmethodologywaiver waiver, grailproject project ";
    	}
    	methodologyWaiverSQL.append(waiverFields);
    
    	methodologyWaiverSQL.append(" where waiver.projectid = project.projectid");
    	
    	if(projectResultFilter.isFetchCancelProjects())
        {
    		methodologyWaiverSQL.append(" and project.iscancel IN (0,1) ");
        }
        else
        {
        	methodologyWaiverSQL.append(" and project.iscancel IN (0) ");
        }
    	
    	// Fetch the MethodologyWaivers for Projects in New Synchro only http://redmine.nvish.com/redmine/issues/516
    	methodologyWaiverSQL.append(" and project.newsynchro = 1 ");
    	
    	//http://redmine.nvish.com/redmine/issues/352
    	if(SynchroPermHelper.isGlobalUserType())
    	{
    		// Global User will be able to fetch only the Waivers for Projects with Global budget location
    		methodologyWaiverSQL.append(" and project.budgetlocation = -1 ");
    	}
    	// This will fetch all the End Market projects for the particular regions for the which the user the associated with
    	else if(SynchroPermHelper.isRegionalUserType())
    	{
    		
    		
    		methodologyWaiverSQL.append(" AND project.budgetlocation in (");
    		
    		//List<Long> regionBudgetLocations = SynchroUtils.getBudgetLocationRegions(SynchroPermHelper.getEffectiveUser());
    		
    		//http://redmine.nvish.com/redmine/issues/448
          	List<Long> regionBudgetLocations = SynchroUtils.getBudgetLocationRegions(SynchroPermHelper.getEffectiveUser());
          	List<Long> userRegionsEM = SynchroUtils.getBudgetLocationRegionsEndMarkets(SynchroPermHelper.getEffectiveUser());
      		List<Long> userEM = SynchroUtils.getBudgetLocationEndMarkets(SynchroPermHelper.getEffectiveUser());
      		List<Long> allEM = new ArrayList<Long>();
      		
      		allEM.addAll(regionBudgetLocations);
      		
      	// Regional User will be able to fetch only the Waivers for Projects with their Regions as  budget location
      		//allEM.addAll(userRegionsEM);
      		//allEM.addAll(userEM);
      		
    		methodologyWaiverSQL.append(StringUtils.join(allEM, ","));
    		methodologyWaiverSQL.append(")");
    		methodologyWaiverSQL.append(" and project.projecttype in (2,3) ");
    		
    	}
    	// This will fetch all the End Market projects for the particular end markets for the which the user the associated with
    	// It should be fetched on the basis of Budget Location and not End Market. //http://redmine.nvish.com/redmine/issues/338
    	else if(SynchroPermHelper.isEndMarketUserType())
    	{
    		
    		// http://redmine.nvish.com/redmine/issues/444
        	if(SynchroPermHelper.isLegaUserType())
        	{
        		List<Long> emBudgetLocations = SynchroUtils.getBudgetLocationEndMarkets(SynchroPermHelper.getEffectiveUser());
        		if(emBudgetLocations!=null && emBudgetLocations.size()>0)
        		{
        			/*methodologyWaiverSQL.append(" AND project.projectid in ( select endmarket.projectid from grailendmarketinvestment endmarket where endmarket.endmarketid in (");
        			methodologyWaiverSQL.append(StringUtils.join(emBudgetLocations, ","));
        			methodologyWaiverSQL.append("))");*/
        			
        			methodologyWaiverSQL.append(" AND project.budgetlocation in (");
    	    	
    	    		methodologyWaiverSQL.append(StringUtils.join(emBudgetLocations, ","));
    	    		methodologyWaiverSQL.append(") ");
        		}
        		
        	}
        	else
        	{
	    		methodologyWaiverSQL.append(" AND project.budgetlocation in (");
	    		List<Long> emBudgetLocations = SynchroUtils.getBudgetLocationEndMarkets(SynchroPermHelper.getEffectiveUser());
	    		methodologyWaiverSQL.append(StringUtils.join(emBudgetLocations, ","));
	    		methodologyWaiverSQL.append(")");
	    		methodologyWaiverSQL.append(" and project.projecttype = 3 ");
        	}
    	}
    	
    	
    	methodologyWaiverSQL.append(applyWaiverFilterNew(projectResultFilter));
    	return methodologyWaiverSQL.toString();
    	
     
    }
    
    private String getPendingMethWaiverDashboardQuery(final ProjectResultFilter projectResultFilter, boolean fetchCount)
    {
    	StringBuilder methodologyWaiverSQL = new StringBuilder();
    	
    	String waiverFields = "";
    	if(fetchCount)
    	{
    		waiverFields = "select count(*) from grailpibmethodologywaiver waiver, grailproject project where waiver.projectid = project.projectid and (waiver.isapproved is null OR waiver.isapproved < 1) ";
    	}
    	else
    	{
    			
    		waiverFields = "select project.projectid, project.name,project.budgetyear,project.methodologydetails, waiver.methodologyapprover, waiver.creationby, waiver.modificationby, waiver.creationdate, waiver.modificationdate, waiver.isapproved, waiver.isapproved, waiver.methodologydeviationrationale, waiver.methodologyapprovercomment, (select (u.firstname || ' ' || u.lastname) FROM jiveUser u where u.userid = waiver.methodologyapprover) as methodologyapproverName from grailpibmethodologywaiver waiver, grailproject project where waiver.projectid = project.projectid and (waiver.isapproved is null OR waiver.isapproved < 1) ";
    	}
        
    	methodologyWaiverSQL.append(waiverFields);
    	
    	if(projectResultFilter.isFetchCancelProjects())
        {
    		methodologyWaiverSQL.append(" and project.iscancel IN (0,1) ");
        }
        else
        {
        	methodologyWaiverSQL.append(" and project.iscancel IN (0) ");
        }
	 
    	// Fetch the Pending Meth Waivers for Projects in New Synchro only http://redmine.nvish.com/redmine/issues/516
    	methodologyWaiverSQL.append(" and project.newsynchro = 1 ");
    	
    	methodologyWaiverSQL.append(applyWaiverFilterNew(projectResultFilter));
    	return methodologyWaiverSQL.toString();
    	
     
    }
    
    private String getAgencyWaiverDashboardQuery(final ProjectResultFilter projectResultFilter, boolean fetchCount)
    {
    	StringBuilder methodologyWaiverSQL = new StringBuilder();
    	
    	String waiverFields = "";
    	if(fetchCount)
    	{
    		waiverFields = "select count(*) from grailpibkantarmw waiver, grailproject project ";
    	}
    	else
    	{
    			
    		waiverFields = "select project.projectid, project.name,project.budgetyear, project.methodologydetails, waiver.creationby, waiver.modificationby, waiver.creationdate, waiver.modificationdate, waiver.methodologyapprover, waiver.isapproved, waiver.methodologydeviationrationale, waiver.methodologyapprovercomment, (select (u.firstname || ' ' || u.lastname) FROM jiveUser u where u.userid = waiver.methodologyapprover) as methodologyapproverName from grailpibkantarmw waiver, grailproject project ";
    	}
        
    	methodologyWaiverSQL.append(waiverFields);
    	methodologyWaiverSQL.append(" where waiver.projectid = project.projectid");
    	
    	if(projectResultFilter.isFetchCancelProjects())
        {
    		methodologyWaiverSQL.append(" and project.iscancel IN (0,1) ");
        }
        else
        {
        	methodologyWaiverSQL.append(" and project.iscancel IN (0) ");
        }
    	
    	// Fetch the AgencyWaivers for Projects in New Synchro only http://redmine.nvish.com/redmine/issues/516
    	methodologyWaiverSQL.append(" and project.newsynchro = 1 ");
    	
    	//http://redmine.nvish.com/redmine/issues/352
    	if(SynchroPermHelper.isGlobalUserType())
    	{
    		// Global User will be able to fetch only the Waivers for Projects with Global budget location
    		methodologyWaiverSQL.append(" and project.budgetlocation = -1 ");
    	}
    	// This will fetch all the End Market projects for the particular regions for the which the user the associated with
    	else if(SynchroPermHelper.isRegionalUserType())
    	{
    		
    		
    		methodologyWaiverSQL.append(" AND project.budgetlocation in (");
    		//List<Long> regionBudgetLocations = SynchroUtils.getBudgetLocationRegions(SynchroPermHelper.getEffectiveUser());
    		//methodologyWaiverSQL.append(StringUtils.join(regionBudgetLocations, ","));
    		
    		
    		//http://redmine.nvish.com/redmine/issues/448
          	List<Long> regionBudgetLocations = SynchroUtils.getBudgetLocationRegions(SynchroPermHelper.getEffectiveUser());
          	List<Long> userRegionsEM = SynchroUtils.getBudgetLocationRegionsEndMarkets(SynchroPermHelper.getEffectiveUser());
      		List<Long> userEM = SynchroUtils.getBudgetLocationEndMarkets(SynchroPermHelper.getEffectiveUser());
      		List<Long> allEM = new ArrayList<Long>();
      		
      		allEM.addAll(regionBudgetLocations);
      		
    		
          	// Regional User will be able to fetch only the Waivers for Projects with their Regions as  budget location
          		//allEM.addAll(userRegionsEM);
          		//allEM.addAll(userEM);
    		methodologyWaiverSQL.append(StringUtils.join(allEM, ","));
    		methodologyWaiverSQL.append(")");
    		methodologyWaiverSQL.append(" and project.projecttype in (2,3) ");
    		
    		
    	}
    	// This will fetch all the End Market projects for the particular end markets for the which the user the associated with
    	// It should be fetched on the basis of Budget Location and not End Market. //http://redmine.nvish.com/redmine/issues/338
    	else if(SynchroPermHelper.isEndMarketUserType())
    	{
    		
    		// http://redmine.nvish.com/redmine/issues/444
        	if(SynchroPermHelper.isLegaUserType())
        	{
        		List<Long> emBudgetLocations = SynchroUtils.getBudgetLocationEndMarkets(SynchroPermHelper.getEffectiveUser());
        		if(emBudgetLocations!=null && emBudgetLocations.size()>0)
        		{
        			/*methodologyWaiverSQL.append(" AND project.projectid in ( select endmarket.projectid from grailendmarketinvestment endmarket where endmarket.endmarketid in (");
        			methodologyWaiverSQL.append(StringUtils.join(emBudgetLocations, ","));
        			methodologyWaiverSQL.append("))");*/
        			
        			methodologyWaiverSQL.append(" AND project.budgetlocation in (");
    				
    				methodologyWaiverSQL.append(StringUtils.join(emBudgetLocations, ","));
    				methodologyWaiverSQL.append(") ");
        		}
        		
        	}
        	else
        	{
    		
				methodologyWaiverSQL.append(" AND project.budgetlocation in (");
				List<Long> emBudgetLocations = SynchroUtils.getBudgetLocationEndMarkets(SynchroPermHelper.getEffectiveUser());
				methodologyWaiverSQL.append(StringUtils.join(emBudgetLocations, ","));
				methodologyWaiverSQL.append(")");
				methodologyWaiverSQL.append(" and project.projecttype = 3 ");
        	}
    	}
    	
    	methodologyWaiverSQL.append(applyWaiverFilterNew(projectResultFilter));
    	return methodologyWaiverSQL.toString();
    	
     
    }
    private String getPendingAgencyWaiverDashboardQuery(final ProjectResultFilter projectResultFilter, boolean fetchCount)
    {
    	StringBuilder methodologyWaiverSQL = new StringBuilder();
    	
    	String waiverFields = "";
    	if(fetchCount)
    	{
    		waiverFields = "select count(*) from grailpibkantarmw waiver, grailproject project where waiver.projectid = project.projectid and (waiver.isapproved is null OR waiver.isapproved < 1) ";
    	}
    	else
    	{
    			
    		waiverFields = "select project.projectid, project.name,project.budgetyear, project.methodologydetails, waiver.creationby, waiver.modificationby, waiver.creationdate, waiver.modificationdate, waiver.methodologyapprover, waiver.isapproved, waiver.methodologydeviationrationale, waiver.methodologyapprovercomment,  (select (u.firstname || ' ' || u.lastname) FROM jiveUser u where u.userid = waiver.methodologyapprover) as methodologyapproverName from grailpibkantarmw waiver, grailproject project where waiver.projectid = project.projectid and (waiver.isapproved is null OR waiver.isapproved < 1)";
    	}
        
    	methodologyWaiverSQL.append(waiverFields);
    	
    	if(projectResultFilter.isFetchCancelProjects())
        {
    		methodologyWaiverSQL.append(" and project.iscancel IN (0,1) ");
        }
        else
        {
        	methodologyWaiverSQL.append(" and project.iscancel IN (0) ");
        }
    	//methodologyWaiverSQL.append(" where waiver.projectid = project.projectid");
    	
    	// Fetch the Pending Agency Waivers for Projects in New Synchro only http://redmine.nvish.com/redmine/issues/516
    	methodologyWaiverSQL.append(" and project.newsynchro = 1 ");
    	
    	methodologyWaiverSQL.append(applyWaiverFilterNew(projectResultFilter));
    	return methodologyWaiverSQL.toString();
    	
     
    }
    
    private String applyWaiverFilterNew(final ProjectResultFilter projectResultFilter)
    {
    	StringBuilder waiverFilterSQL = new StringBuilder();
    	
    	// Keyword filter
        if(projectResultFilter.getKeyword() != null && !projectResultFilter.getKeyword().equals("")) {
            StringBuilder keywordCondition = new StringBuilder();
            keywordCondition.append(" AND (lower(project.name) like '%").append(projectResultFilter.getKeyword().toLowerCase()).append("%'")
                    .append(" OR ").append("(''|| project.projectId ||'') like ").append("'%").append(synchroDAOUtil.getFormattedProjectCode(projectResultFilter.getKeyword())).append("%'")
                   // .append(" OR ").append("lower(methodologyapproverName) like ").append("'%").append(projectResultFilter.getKeyword().toLowerCase()).append("%'")
                    .append(" OR project.projectid in (select e.projectid from grailendmarketinvestment e, grailendmarketfields ef where lower(ef.name) like '%").append(projectResultFilter.getKeyword().toLowerCase()).append("%' and e.endmarketid = ef.id )")
                    .append(" OR waiver.methodologyapprover in (select u.userid from jiveuser u where (u.firstname || ' ' || u.lastname) like '%"+projectResultFilter.getKeyword()+"%')")
                    .append(" OR ").append("(''|| project.budgetyear ||'') like '%").append(projectResultFilter.getKeyword()).append("%'");
            
            if("pending".contains(projectResultFilter.getKeyword().toLowerCase()))
            {
            	keywordCondition.append(" OR ").append("waiver.isapproved is NULL ");
            }
            if("approved".contains(projectResultFilter.getKeyword().toLowerCase()))
            {
            	keywordCondition.append(" OR ").append(" waiver.isapproved = 1");
            }
            if("rejected".contains(projectResultFilter.getKeyword().toLowerCase()))
            {
            	keywordCondition.append(" OR ").append(" waiver.isapproved = 2");
            }
            
         // Methodologies Keyword
            Map<Integer, String> allMethodologies = SynchroGlobal.getMethodologies();
            List<Integer> methodologies = new ArrayList<Integer>();
            if(allMethodologies!=null)
            {
         	   for(Integer methId : allMethodologies.keySet())
         	   {
         		   if(allMethodologies.get(methId).toLowerCase().contains(projectResultFilter.getKeyword().toLowerCase()))
         		   {
         			   methodologies.add(methId);
         		   }
         	   }
         	   if(methodologies!=null && methodologies.size()>0)
         	   {
         		   keywordCondition.append(" OR (project.methodologydetails in ('").append(methodologies.get(0)+"").append("')");
		               if(methodologies.size()>0)
		               {
		            	   keywordCondition.append(" or project.methodologydetails like ('").append(methodologies.get(0)+"").append(",%')");
		            	   keywordCondition.append(" or project.methodologydetails like ('%,").append(methodologies.get(0)+"").append("')");
		            	   keywordCondition.append(" or project.methodologydetails like ('%,") .append(methodologies.get(0)+"").append(",%')");
			               	for(int i=1;i<methodologies.size();i++)
			               	{
			               		keywordCondition.append(" or project.methodologydetails in ('").append(methodologies.get(i)+"").append("')");
			               		keywordCondition.append(" or project.methodologydetails like ('").append(methodologies.get(i)+"").append(",%')");
			               		keywordCondition.append(" or project.methodologydetails like ('%,").append(methodologies.get(i)+"").append("')");
			               		keywordCondition.append(" or project.methodologydetails like ('%,").append(methodologies.get(i)+"").append(",%')");
			               	}
		               }
		               keywordCondition.append(")");
         	   }
         	   
            }
            
            
            
            keywordCondition.append(")");		
            
            waiverFilterSQL.append(keywordCondition.toString());
        }
        
     // Initiator filter
        if(projectResultFilter.getWaiverInitiator() != null && !projectResultFilter.getWaiverInitiator().equals(""))
        {
            StringBuilder waiverInitiatorCondition = new StringBuilder();
            waiverInitiatorCondition.append(" AND waiver.creationby in (select u.userid from jiveuser u where (u.firstname || ' ' || u.lastname) like '%"+projectResultFilter.getWaiverInitiator()+"%')");
            waiverFilterSQL.append(waiverInitiatorCondition.toString());
        }
        
        
     // Start Date Filter
        
        if(projectResultFilter.getStartDateBegin() != null && projectResultFilter.getStartDateComplete()!=null) {
            StringBuilder startDateCondition = new StringBuilder();
            String startDateBegin = SynchroUtils.getDateString(projectResultFilter.getStartDateBegin().getTime());
            String startDateComplete = SynchroUtils.getDateString(projectResultFilter.getStartDateComplete().getTime());
            startDateCondition.append(" AND to_char(to_timestamp(waiver.creationdate/1000),'YYYY-MM-DD') >= '"+startDateBegin+"'  AND to_char(to_timestamp(waiver.creationdate/1000),'YYYY-MM-DD') <= '"+startDateComplete+"'");
            
            waiverFilterSQL.append(startDateCondition.toString());
            
        }
        else if(projectResultFilter.getStartDateBegin() != null)
        {
     	   StringBuilder startDateCondition = new StringBuilder();
            String startDateBegin = SynchroUtils.getDateString(projectResultFilter.getStartDateBegin().getTime());
            startDateCondition.append(" AND to_char(to_timestamp(waiver.creationdate/1000),'YYYY-MM-DD') >= '"+startDateBegin+"'");
            
            waiverFilterSQL.append(startDateCondition.toString());
        }
        else if(projectResultFilter.getStartDateComplete()!=null)
        {
     	   StringBuilder startDateCondition = new StringBuilder();
            String startDateComplete = SynchroUtils.getDateString(projectResultFilter.getStartDateComplete().getTime());
            startDateCondition.append(" AND to_char(to_timestamp(waiver.creationdate/1000),'YYYY-MM-DD') <= '"+startDateComplete+"'");
            
            waiverFilterSQL.append(startDateCondition.toString());
        }
       
        // End Date Filter
        if(projectResultFilter.getEndDateBegin() != null && projectResultFilter.getEndDateComplete()!=null) {
            StringBuilder endDateCondition = new StringBuilder();
            String endDateBegin = SynchroUtils.getDateString(projectResultFilter.getEndDateBegin().getTime());
            String endDateComplete = SynchroUtils.getDateString(projectResultFilter.getEndDateComplete().getTime());
            endDateCondition.append(" AND to_char(to_timestamp(waiver.modificationdate/1000),'YYYY-MM-DD') >= '"+endDateBegin+"'  AND to_char(to_timestamp(waiver.modificationdate/1000),'YYYY-MM-DD') <= '"+endDateComplete+"'");
            
            waiverFilterSQL.append(endDateCondition.toString());
            
        }
        else if (projectResultFilter.getEndDateBegin() != null)
        {
     	   StringBuilder endDateCondition = new StringBuilder();
            String endDateBegin = SynchroUtils.getDateString(projectResultFilter.getEndDateBegin().getTime());
            endDateCondition.append(" AND to_char(to_timestamp(waiver.modificationdate/1000),'YYYY-MM-DD') >= '"+endDateBegin+"'");
            waiverFilterSQL.append(endDateCondition.toString());
        }
        else if (projectResultFilter.getEndDateComplete()!=null)
        {
     	   StringBuilder endDateCondition = new StringBuilder();
            String endDateComplete = SynchroUtils.getDateString(projectResultFilter.getEndDateComplete().getTime());
            endDateCondition.append(" AND to_char(to_timestamp(waiver.modificationdate/1000),'YYYY-MM-DD') <= '"+endDateComplete+"'");
            
            waiverFilterSQL.append(endDateCondition.toString());
        }
        
      //Research Agency Filter
        if(projectResultFilter.getResearchAgencies() != null && projectResultFilter.getResearchAgencies().size()>0 && !isListNull(projectResultFilter.getResearchAgencies())) {
            StringBuilder researchAgenciesCondition = new StringBuilder();
            researchAgenciesCondition.append(" AND project.projectid in (select pcd.projectid from grailprojectcostdetails pcd where pcd.agencyid in (");
            researchAgenciesCondition.append(StringUtils.join(projectResultFilter.getResearchAgencies(),","));
            researchAgenciesCondition.append("))");
            waiverFilterSQL.append(researchAgenciesCondition.toString());
            
        }
        
      //Cost Component Filter
        if(projectResultFilter.getCostComponents() != null && projectResultFilter.getCostComponents().size()>0 && !isListNull(projectResultFilter.getCostComponents())) {
            StringBuilder costComponentsCondition = new StringBuilder();
            costComponentsCondition.append(" AND project.projectid in (select pcd.projectid from grailprojectcostdetails pcd where pcd.costcomponent in (");
            costComponentsCondition.append(StringUtils.join(projectResultFilter.getCostComponents(),","));
            costComponentsCondition.append("))");
            waiverFilterSQL.append(costComponentsCondition.toString());
            
        }
        //Waiver Status Filter
        if(projectResultFilter.getWaiverStatus() != null && projectResultFilter.getWaiverStatus().size()>0 && !isListNull(projectResultFilter.getWaiverStatus())) {
            StringBuilder waiverStatusCondition = new StringBuilder();
            
            boolean isPendingStatus = false; 
            for(Long wStatus : projectResultFilter.getWaiverStatus())
            {
            	if(wStatus==0)
            	{
            		isPendingStatus = true;
            	}
            }
            if(isPendingStatus)
            {
            	 waiverStatusCondition.append(" AND (waiver.isapproved IS NULL OR waiver.isapproved in (");
                 waiverStatusCondition.append(StringUtils.join(projectResultFilter.getWaiverStatus(),","));
                 
                 waiverStatusCondition.append("))");
                 waiverFilterSQL.append(waiverStatusCondition.toString());
            }
            else
            {
            	 waiverStatusCondition.append(" AND waiver.isapproved in (");
                 waiverStatusCondition.append(StringUtils.join(projectResultFilter.getWaiverStatus(),","));
                 
                 waiverStatusCondition.append(")");
                 waiverFilterSQL.append(waiverStatusCondition.toString());
            }
            
        }
        
        //Research End Market Filter
        if(projectResultFilter.getResearchEndMarkets() != null && projectResultFilter.getResearchEndMarkets().size()>0 && !isListNull(projectResultFilter.getResearchEndMarkets())) {
            StringBuilder researchEndMarketsCondition = new StringBuilder();
            researchEndMarketsCondition.append(" AND project.projectid in ( select endmarket.projectid from grailendmarketinvestment endmarket where endmarket.endmarketid in (");
            researchEndMarketsCondition.append(StringUtils.join(projectResultFilter.getResearchEndMarkets(),","));
            researchEndMarketsCondition.append("))");
            waiverFilterSQL.append(researchEndMarketsCondition.toString());
            
        }
        
        //Methodology Details Filter
        if(projectResultFilter.getMethDetails() != null && projectResultFilter.getMethDetails().size()>0 && !isListNull(projectResultFilter.getMethDetails())) {
            StringBuilder methodologyDetailsCondition = new StringBuilder();
           
            
            methodologyDetailsCondition.append(" AND (project.methodologydetails in ('")
                    .append(projectResultFilter.getMethDetails().get(0)+"").append("')");
            if(projectResultFilter.getMethDetails().size()>0)
            {
         	   methodologyDetailsCondition.append(" or project.methodologydetails like ('").append(projectResultFilter.getMethDetails().get(0)+"").append(",%')");
         	   methodologyDetailsCondition.append(" or project.methodologydetails like ('%,").append(projectResultFilter.getMethDetails().get(0)+"").append("')");
         	   methodologyDetailsCondition.append(" or project.methodologydetails like ('%,") .append(projectResultFilter.getMethDetails().get(0)+"").append(",%')");
	               	for(int i=1;i<projectResultFilter.getMethDetails().size();i++)
	               	{
	               		methodologyDetailsCondition.append(" or project.methodologydetails in ('").append(projectResultFilter.getMethDetails().get(i)+"").append("')");
	               		methodologyDetailsCondition.append(" or project.methodologydetails like ('").append(projectResultFilter.getMethDetails().get(i)+"").append(",%')");
	               		methodologyDetailsCondition.append(" or project.methodologydetails like ('%,").append(projectResultFilter.getMethDetails().get(i)+"").append("')");
	               		methodologyDetailsCondition.append(" or project.methodologydetails like ('%,").append(projectResultFilter.getMethDetails().get(i)+"").append(",%')");
	               	}
            }
            methodologyDetailsCondition.append(")");
            LOG.info("Methodology Details Filter Query - " + methodologyDetailsCondition.toString());
            waiverFilterSQL.append(methodologyDetailsCondition.toString());
            
        }
        
        //BudgetYear Filter
        if(projectResultFilter.getBudgetYears() != null && projectResultFilter.getBudgetYears().size()>0 && !isListNull(projectResultFilter.getBudgetYears())) {
            StringBuilder budgetYearsCondition = new StringBuilder();
            budgetYearsCondition.append(" AND project.budgetyear in (");
            budgetYearsCondition.append(StringUtils.join(projectResultFilter.getBudgetYears(),","));
            budgetYearsCondition.append(")");
            waiverFilterSQL.append(budgetYearsCondition.toString());
            
        }
        
        return waiverFilterSQL.toString();
    }
    
    private boolean isListNull(List<Long> projectFilterList)
    {
    	if(projectFilterList!=null && projectFilterList.size()==1)
    	{
    		if(projectFilterList.get(0)==null)
    		{
    			return true;
    		}
    	}
    	return false;
    }

    @Override
    public List<PIBMethodologyWaiver> getPIBAgencyWaivers(final ProjectResultFilter projectResultFilter)
    {
    	List<PIBMethodologyWaiver> methodologyWaivers = Collections.emptyList();
    	StringBuilder sql = new StringBuilder();
    	
    	/*String waiverFields = "select project.projectid, project.name,project.budgetyear, waiver.methodologyapprover, waiver.isapproved, (select (u.firstname || ' ' || u.lastname) FROM jiveUser u where u.userid = waiver.methodologyapprover) as methodologyapproverName from grailpibkantarmw waiver, grailproject project where waiver.projectid = project.projectid";
        sql = new StringBuilder(waiverFields);
        
        
     // Keyword filter
        if(projectResultFilter.getKeyword() != null && !projectResultFilter.getKeyword().equals("")) {
            StringBuilder keywordCondition = new StringBuilder();
            keywordCondition.append(" AND (lower(project.name) like '%").append(projectResultFilter.getKeyword().toLowerCase()).append("%'")
                    .append(" OR ").append("(''|| project.projectId ||'') like ").append("'%").append(synchroDAOUtil.getFormattedProjectCode(projectResultFilter.getKeyword())).append("%'")
                   // .append(" OR ").append("lower(methodologyapproverName) like ").append("'%").append(projectResultFilter.getKeyword().toLowerCase()).append("%'")
                    .append(" OR ").append("project.budgetyear = ").append(projectResultFilter.getKeyword().toLowerCase()).append(")");
            
            
            sql.append(keywordCondition.toString());
        }
        */
    	sql.append(getAgencyWaiverDashboardQuery(projectResultFilter,false));
        
        
        if(projectResultFilter.getSortField() != null && !projectResultFilter.getSortField().equals("")) {
            sql.append(" order by ").append(getOrderByField(projectResultFilter.getSortField())).append(" ").append(SynchroDAOUtil.getSortType(projectResultFilter.getAscendingOrder())).append(",").append(getOrderByField("id")).append(" ").append(SynchroDAOUtil.getSortType(0));
        } else {
            sql.append(" order by ").append(getOrderByField("id")).append(" ").append(SynchroDAOUtil.getSortType(0));
        }
        try {
            if(projectResultFilter.getStart() != null) {
                sql.append(" OFFSET ").append(projectResultFilter.getStart());
            }
            if(projectResultFilter.getLimit() != null && projectResultFilter.getLimit() > 0) {
                sql.append(" LIMIT ").append(projectResultFilter.getLimit());
            }
            methodologyWaivers = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), methodologyDashboardRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load methodology Waivers by filter";
        //    LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return methodologyWaivers;
    }
    
    @Override
    public List<PIBMethodologyWaiver> getPIBPendingMethodologyWaivers(final ProjectResultFilter projectResultFilter)
    {
    	List<PIBMethodologyWaiver> methodologyWaivers = Collections.emptyList();
    	/*StringBuilder sql = new StringBuilder();
    	
    	String waiverFields = "select project.projectid, project.name,project.budgetyear, waiver.methodologyapprover, waiver.isapproved, (select (u.firstname || ' ' || u.lastname) FROM jiveUser u where u.userid = waiver.methodologyapprover) as methodologyapproverName from grailpibmethodologywaiver waiver, grailproject project where waiver.projectid = project.projectid and (waiver.isapproved is null OR waiver.isapproved < 1)";
        sql = new StringBuilder(waiverFields);
        
        
     // Keyword filter
        if(projectResultFilter.getKeyword() != null && !projectResultFilter.getKeyword().equals("")) {
            StringBuilder keywordCondition = new StringBuilder();
            keywordCondition.append(" AND (lower(project.name) like '%").append(projectResultFilter.getKeyword().toLowerCase()).append("%'")
                    .append(" OR ").append("(''|| project.projectId ||'') like ").append("'%").append(synchroDAOUtil.getFormattedProjectCode(projectResultFilter.getKeyword())).append("%'")
                   // .append(" OR ").append("lower(methodologyapproverName) like ").append("'%").append(projectResultFilter.getKeyword().toLowerCase()).append("%'")
                    .append(" OR ").append("project.budgetyear = ").append(projectResultFilter.getKeyword().toLowerCase()).append(")");
            
            
            sql.append(keywordCondition.toString());
        }
        */
    	StringBuilder sql = new StringBuilder(getPendingMethWaiverDashboardQuery(projectResultFilter,false));
        
        
        if(projectResultFilter.getSortField() != null && !projectResultFilter.getSortField().equals("")) {
            sql.append(" order by ").append(getOrderByField(projectResultFilter.getSortField())).append(" ").append(SynchroDAOUtil.getSortType(projectResultFilter.getAscendingOrder())).append(",").append(getOrderByField("id")).append(" ").append(SynchroDAOUtil.getSortType(0));
        } else {
          //  sql.append(" order by ").append(getOrderByField("id")).append(" ").append(SynchroDAOUtil.getSortType(0));
            sql.append(" order by ").append(getOrderByField("modificationDate")).append(" ").append(SynchroDAOUtil.getSortType(1));
        }
        try {
            if(projectResultFilter.getStart() != null) {
                sql.append(" OFFSET ").append(projectResultFilter.getStart());
            }
            if(projectResultFilter.getLimit() != null && projectResultFilter.getLimit() > 0) {
                sql.append(" LIMIT ").append(projectResultFilter.getLimit());
            }
            methodologyWaivers = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), methodologyDashboardRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load methodology Waivers by filter";
        //    LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return methodologyWaivers;
    }
    
    @Override
    public List<PIBMethodologyWaiver> getPIBPendingAgencyWaivers(final ProjectResultFilter projectResultFilter)
    {
    	List<PIBMethodologyWaiver> methodologyWaivers = Collections.emptyList();
    	/*StringBuilder sql = new StringBuilder();
    	
    	String waiverFields = "select project.projectid, project.name,project.budgetyear, waiver.methodologyapprover, waiver.isapproved, (select (u.firstname || ' ' || u.lastname) FROM jiveUser u where u.userid = waiver.methodologyapprover) as methodologyapproverName from grailpibkantarmw waiver, grailproject project where waiver.projectid = project.projectid and (waiver.isapproved is null OR waiver.isapproved < 1)";
        sql = new StringBuilder(waiverFields);
        
        
     // Keyword filter
        if(projectResultFilter.getKeyword() != null && !projectResultFilter.getKeyword().equals("")) {
            StringBuilder keywordCondition = new StringBuilder();
            keywordCondition.append(" AND (lower(project.name) like '%").append(projectResultFilter.getKeyword().toLowerCase()).append("%'")
                    .append(" OR ").append("(''|| project.projectId ||'') like ").append("'%").append(synchroDAOUtil.getFormattedProjectCode(projectResultFilter.getKeyword())).append("%'")
                   // .append(" OR ").append("lower(methodologyapproverName) like ").append("'%").append(projectResultFilter.getKeyword().toLowerCase()).append("%'")
                    .append(" OR ").append("project.budgetyear = ").append(projectResultFilter.getKeyword().toLowerCase()).append(")");
            
            
            sql.append(keywordCondition.toString());
        }
        
        */
    	StringBuilder sql = new StringBuilder(getPendingAgencyWaiverDashboardQuery(projectResultFilter,false));
        
        if(projectResultFilter.getSortField() != null && !projectResultFilter.getSortField().equals("")) {
            sql.append(" order by ").append(getOrderByField(projectResultFilter.getSortField())).append(" ").append(SynchroDAOUtil.getSortType(projectResultFilter.getAscendingOrder())).append(",").append(getOrderByField("id")).append(" ").append(SynchroDAOUtil.getSortType(0));
        } else {
            //sql.append(" order by ").append(getOrderByField("id")).append(" ").append(SynchroDAOUtil.getSortType(0));
        	//http://redmine.nvish.com/redmine/issues/402
        	sql.append(" order by ").append(getOrderByField("modificationDate")).append(" ").append(SynchroDAOUtil.getSortType(1));
        }
        try {
            if(projectResultFilter.getStart() != null) {
                sql.append(" OFFSET ").append(projectResultFilter.getStart());
            }
            if(projectResultFilter.getLimit() != null && projectResultFilter.getLimit() > 0) {
                sql.append(" LIMIT ").append(projectResultFilter.getLimit());
            }
            methodologyWaivers = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), methodologyDashboardRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load methodology Waivers by filter";
        //    LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return methodologyWaivers;
    }
    
    private String getOrderByField(final String sortField) {
        if(StringUtils.isNotBlank(sortField)) {
            String field = null;
            if(sortField.equals("id")) {
                field = "project.projectID";
            } else if(sortField.equals("name")) {
                field = "project.name";
            } else if(sortField.equals("budgetyear")) {
                field = "project.budgetyear";
            } else if(sortField.equals("methodologyApprover")) {
                field = "methodologyapprovername";
            } else if(sortField.equals("status")) {
                field = "waiver.isapproved ";
            }  else if(sortField.equals("modificationDate")) {
                field = "waiver.modificationdate ";
            } 
            else {
                field = sortField;
            }
            return field;
        }
        return null;
    }
    @Override
    public Long getPIBMethodologyWaiversTotalCount(final ProjectResultFilter filter) {
        Long count = 0L;
       
        StringBuilder sql = new StringBuilder(getMethWaiverDashboardQuery(filter,true));
    	
    	/*String waiverFields = "select count(*) from grailpibmethodologywaiver waiver, grailproject project where waiver.projectid = project.projectid ";
        sql = new StringBuilder(waiverFields);
        */
        
       
        try {
            count = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(sql.toString());
        	//String sql  = "select count(*) from grailproject p";
        	//count = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(sql);
        }
        catch (DataAccessException e) {
            throw new DAOException(e.getMessage(), e);
        }
        return count;
    }
    
    @Override
    public Long getPIBAgencyWaiversTotalCount(final ProjectResultFilter filter) {
        Long count = 0L;
        StringBuilder sql = new StringBuilder(getAgencyWaiverDashboardQuery(filter,true));
        
    	
    	/*String waiverFields = "select count(*) from grailpibkantarmw waiver, grailproject project where waiver.projectid = project.projectid ";
        sql = new StringBuilder(waiverFields);
        */
        try {
            count = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(sql.toString());
        	//String sql  = "select count(*) from grailproject p";
        	//count = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(sql);
        }
        catch (DataAccessException e) {
            throw new DAOException(e.getMessage(), e);
        }
        return count;
    }
    
    @Override
    public Long getPIBPendingMethodologyWaiversTotalCount(final ProjectResultFilter projectResultFilter) {
        Long count = 0L;
      /*  StringBuilder sql = new StringBuilder();
    	
    	String waiverFields = "select count(*) from grailpibmethodologywaiver waiver, grailproject project where waiver.projectid = project.projectid and (waiver.isapproved is null OR waiver.isapproved < 1)";
        sql = new StringBuilder(waiverFields);
        */
        StringBuilder sql = new StringBuilder(getPendingMethWaiverDashboardQuery(projectResultFilter,true));
        try {
            count = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(sql.toString());
        	//String sql  = "select count(*) from grailproject p";
        	//count = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(sql);
        }
        catch (DataAccessException e) {
            throw new DAOException(e.getMessage(), e);
        }
        return count;
    }
    
    @Override
    public Long getPIBPendingAgencyWaiversTotalCount(final ProjectResultFilter projectResultFilter) {
        Long count = 0L;
      /*  StringBuilder sql = new StringBuilder();
    	
    	String waiverFields = "select count(*) from grailpibkantarmw waiver, grailproject project where waiver.projectid = project.projectid and (waiver.isapproved is null OR waiver.isapproved < 1)";
        sql = new StringBuilder(waiverFields);
        */
        
        StringBuilder sql = new StringBuilder(getPendingAgencyWaiverDashboardQuery(projectResultFilter,true));
        try {
            count = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(sql.toString());
        	//String sql  = "select count(*) from grailproject p";
        	//count = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(sql);
        }
        catch (DataAccessException e) {
            throw new DAOException(e.getMessage(), e);
        }
        return count;
    }
    @Override
    public List<FieldAttachmentBean> getDocumentAttachment(final Long projectId, final Long endMakerketId) {
        return getSimpleJdbcTemplate().query(GET_ATTACHMENT, fieldAttachmentRowMapper, projectId,endMakerketId);

    }

    @Override
    public AttachmentBean getDocumentAttachment(final SynchroAttachment attachment) {
        AttachmentBean bean = null;
        try{
            bean = getSimpleJdbcTemplate().queryForObject(GET_ATTACHMENT_BY_OBJECT, attachmentRowMapper, attachment.getObjectType(), attachment.getID());
        } catch (DAOException e) {
             e.printStackTrace();
        }
        return bean;
    }

    @Override
    public List<AttachmentBean> getFieldAttachments(SynchroAttachment attachment) {
        List<AttachmentBean> bean = new ArrayList<AttachmentBean>();
        try{
            bean = getSimpleJdbcTemplate().query(GET_ATTACHMENT_BY_OBJECT, attachmentRowMapper, attachment.getObjectType(), attachment.getID());
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return bean;
    }
  
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updatePIBStatus(final Long projectID,final Integer status) {
        try {
            getSimpleJdbcTemplate().getJdbcOperations().update(
            		UPDATE_PIB_STATUS, status, projectID);

        } catch (DataAccessException daEx) {
            final String message = "Failed to update the PIB Status for Project "
                    + projectID;
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updatePIBCompletionDate(final Long projectID,final Date pibCompletionDate) {
        try {
            getSimpleJdbcTemplate().getJdbcOperations().update(
            		UPDATE_PIB_COMPLETION_DATE, pibCompletionDate.getTime(), projectID);

        } catch (DataAccessException daEx) {
            final String message = "Failed to update the PIB Completion Date for Project "
                    + projectID;
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updatePIBActionStandard(final ProjectInitiation projectInitiation)
    {
    	final int count = getSimpleJdbcTemplate().queryForInt("SELECT count(endmarketid) from grailpib " +
                "WHERE projectid = ? AND endmarketid = ?",
                projectInitiation.getProjectID(), projectInitiation.getEndMarketID());
        if(count==0)
        {
           save(projectInitiation);
        }
        else
        {
        	try {
                getSimpleJdbcTemplate().getJdbcOperations().update(
               		 UPDATE_PIB_ACTION_STANDARD, projectInitiation.getActionStandard(), projectInitiation.getActionStandardText(), projectInitiation.getProjectID(), projectInitiation.getEndMarketID());

            } catch (DataAccessException daEx) {
                final String message = "Failed to update the Action Standard for Project " + projectInitiation.getProjectID() +" and EndMarket id --"+ projectInitiation.getEndMarketID();
                LOG.log(Level.SEVERE, message, daEx);
                throw new DAOException(message, daEx);
            }
        } 
    	
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updatePIBResearchDesign(final ProjectInitiation projectInitiation)
    {
    	 
    	final int count = getSimpleJdbcTemplate().queryForInt("SELECT count(endmarketid) from grailpib " +
                "WHERE projectid = ? AND endmarketid = ?",
                projectInitiation.getProjectID(), projectInitiation.getEndMarketID());
        if(count==0)
        {
           save(projectInitiation);
        }
        else
        {
        	try {
                getSimpleJdbcTemplate().getJdbcOperations().update(
               		 UPDATE_PIB_RESEARCH_DESIGN, projectInitiation.getResearchDesign(), projectInitiation.getResearchDesignText(), projectInitiation.getProjectID(), projectInitiation.getEndMarketID());

            } catch (DataAccessException daEx) {
                final String message = "Failed to update the Research Design for Project " + projectInitiation.getProjectID() +" and EndMarket id --"+ projectInitiation.getEndMarketID();
                LOG.log(Level.SEVERE, message, daEx);
                throw new DAOException(message, daEx);
            }

        } 
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updatePIBSampleProfile(final ProjectInitiation projectInitiation)
    {
    	final int count = getSimpleJdbcTemplate().queryForInt("SELECT count(endmarketid) from grailpib " +
                "WHERE projectid = ? AND endmarketid = ?",
                projectInitiation.getProjectID(), projectInitiation.getEndMarketID());
        if(count==0)
        {
           save(projectInitiation);
        }
        else
        {

        	try {
                 getSimpleJdbcTemplate().getJdbcOperations().update(
                		 UPDATE_PIB_SAMPLE_PROFILE, projectInitiation.getSampleProfile(), projectInitiation.getSampleProfileText(), projectInitiation.getProjectID(), projectInitiation.getEndMarketID());

             } catch (DataAccessException daEx) {
                 final String message = "Failed to update the Sample Profile for Project " + projectInitiation.getProjectID() +" and EndMarket id --"+ projectInitiation.getEndMarketID();
                 LOG.log(Level.SEVERE, message, daEx);
                 throw new DAOException(message, daEx);
             }

        }  
    	
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updatePIBStimulusMaterial(final ProjectInitiation projectInitiation)
    {
    	final int count = getSimpleJdbcTemplate().queryForInt("SELECT count(endmarketid) from grailpib " +
                "WHERE projectid = ? AND endmarketid = ?",
                projectInitiation.getProjectID(), projectInitiation.getEndMarketID());
        if(count==0)
        {
           save(projectInitiation);
        }
        else
        {

        	try {
                getSimpleJdbcTemplate().getJdbcOperations().update(
               		 UPDATE_PIB_STIMULUS_MATERIAL, projectInitiation.getStimulusMaterial(), projectInitiation.getStimulusMaterialText(), projectInitiation.getProjectID(), projectInitiation.getEndMarketID());

            } catch (DataAccessException daEx) {
                final String message = "Failed to update the Stimulus Material for Project " + projectInitiation.getProjectID() +" and EndMarket id --"+ projectInitiation.getEndMarketID();
                LOG.log(Level.SEVERE, message, daEx);
                throw new DAOException(message, daEx);
            }
        }  
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updatePIBBusinessQuestion(final ProjectInitiation projectInitiation)
    {
    	final int count = getSimpleJdbcTemplate().queryForInt("SELECT count(endmarketid) from grailpib " +
                "WHERE projectid = ? AND endmarketid = ?",
                projectInitiation.getProjectID(), projectInitiation.getEndMarketID());
        if(count==0)
        {
           save(projectInitiation);
        }
        else
        {

        	try {
                getSimpleJdbcTemplate().getJdbcOperations().update(
                		UPDATE_PIB_BUSINESS_QUESTION, projectInitiation.getBizQuestion(),  projectInitiation.getBizQuestionText(), projectInitiation.getProjectID(), projectInitiation.getEndMarketID());

            } catch (DataAccessException daEx) {
                final String message = "Failed to update the Business Question for Project " + projectInitiation.getProjectID() +" and EndMarket id --"+ projectInitiation.getEndMarketID();
                LOG.log(Level.SEVERE, message, daEx);
                throw new DAOException(message, daEx);
            }
        }  
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updatePIBResearchObjective(final ProjectInitiation projectInitiation)
    {
    	final int count = getSimpleJdbcTemplate().queryForInt("SELECT count(endmarketid) from grailpib " +
                "WHERE projectid = ? AND endmarketid = ?",
                projectInitiation.getProjectID(), projectInitiation.getEndMarketID());
        if(count==0)
        {
           save(projectInitiation);
        }
        else
        {

        	try {
                getSimpleJdbcTemplate().getJdbcOperations().update(
                		UPDATE_PIB_RESEARCH_OBJECTIVE, projectInitiation.getResearchObjective(), projectInitiation.getResearchObjectiveText(), projectInitiation.getProjectID(), projectInitiation.getEndMarketID());

            } catch (DataAccessException daEx) {
                final String message = "Failed to update the Research Objective for Project " + projectInitiation.getProjectID() +" and EndMarket id --"+ projectInitiation.getEndMarketID();
                LOG.log(Level.SEVERE, message, daEx);
                throw new DAOException(message, daEx);
            }
        }  
    }
    
  
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void deletePIBEndMarket(final Long projectID) {
        try {
            getSimpleJdbcTemplate().getJdbcOperations().update(DELETE_ENDMARKET_PIB, projectID);
        }
        catch (DataAccessException daEx) {
            final String message = "Failed to Delete PIB Endmarket for project " + projectID;
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void deletePIBMWEndMarket(final Long projectID) {
        try {
            getSimpleJdbcTemplate().getJdbcOperations().update(DELETE_ENDMARKET_PIB_MW, projectID);
        }
        catch (DataAccessException daEx) {
            final String message = "Failed to Delete PIB MW Endmarket for project " + projectID;
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void deletePIBReportingEndMarket(final Long projectID) {
        try {
            getSimpleJdbcTemplate().getJdbcOperations().update(DELETE_ENDMARKET_PIB_REPORTING, projectID);
        }
        catch (DataAccessException daEx) {
            final String message = "Failed to Delete PIB Reporting Endmarket for project " + projectID;
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void deletePIBStakeholderEndMarket(final Long projectID) {
        try {
            getSimpleJdbcTemplate().getJdbcOperations().update(DELETE_ENDMARKET_PIB_STAKEHOLDER, projectID);
        }
        catch (DataAccessException daEx) {
            final String message = "Failed to Delete PIB Stakeholder Endmarket for project " + projectID;
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void deletePIBEndMarket(final Long projectID,final Long endMarketID) {
        try {
            getSimpleJdbcTemplate().getJdbcOperations().update(DELETE_ENDMARKET_PIB_ENDMARKET_ID, projectID, endMarketID);
        }
        catch (DataAccessException daEx) {
            final String message = "Failed to Delete PIB Endmarket for project " + projectID;
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void deletePIBMWEndMarket(final Long projectID, final Long endMarketID) {
        try {
            getSimpleJdbcTemplate().getJdbcOperations().update(DELETE_ENDMARKET_PIB_MW_ENDMARKET_ID, projectID, endMarketID);
        }
        catch (DataAccessException daEx) {
            final String message = "Failed to Delete PIB MW Endmarket for project " + projectID;
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void deletePIBReportingEndMarket(final Long projectID, final Long endMarketID) {
        try {
            getSimpleJdbcTemplate().getJdbcOperations().update(DELETE_ENDMARKET_PIB_REPORTING_ENDMARKET_ID, projectID, endMarketID);
        }
        catch (DataAccessException daEx) {
            final String message = "Failed to Delete PIB Reporting Endmarket for project " + projectID;
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void deletePIBStakeholderEndMarket(final Long projectID, final Long endMarketID) {
        try {
            getSimpleJdbcTemplate().getJdbcOperations().update(DELETE_ENDMARKET_PIB_STAKEHOLDER_ENDMARKET_ID, projectID, endMarketID);
        }
        catch (DataAccessException daEx) {
            final String message = "Failed to Delete PIB Stakeholder Endmarket for project " + projectID;
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updatePIBNotifyAboveMarketContact(final Long projectId, final Long endMarketId, final Integer notifyAboveMarketContact)
    {
    	try {
            getSimpleJdbcTemplate().getJdbcOperations().update(
            		UPDATE_PIB_NOTIFY_ABOVE_MARKET_CONTACTS, notifyAboveMarketContact, System.currentTimeMillis(), projectId, endMarketId);

        } catch (DataAccessException daEx) {
            final String message = "Failed to update the PIB Notify Above Market Contact for Project " + projectId +" and EndMarket id --"+ endMarketId;
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
         
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updatePIBApproveChanges(final Long projectId, final Long endMarketId, final Integer approveChanges)
    {
    	try {
            getSimpleJdbcTemplate().getJdbcOperations().update(
            		UPDATE_PIB_APPROVE_CHANGES, approveChanges, projectId, endMarketId);

        } catch (DataAccessException daEx) {
            final String message = "Failed to update the PIB Approve Changes for Project " + projectId +" and EndMarket id --"+ endMarketId;
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
         
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateNotifySPI(final Long projectId, final Integer notifySPI)
    {
    	try {
            getSimpleJdbcTemplate().getJdbcOperations().update(
            		UPDATE_PIB_NOTIFY_SPI, notifySPI, projectId);

        } catch (DataAccessException daEx) {
            final String message = "Failed to update the PIB Notify SPI for Project " + projectId ;
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateNotifyPO(final Long projectId, final Integer notifyPO)
    {
    	try {
            getSimpleJdbcTemplate().getJdbcOperations().update(
            		UPDATE_PIB_NOTIFY_PO, notifyPO, projectId);

        } catch (DataAccessException daEx) {
            final String message = "Failed to update the PIB Notify PO for Project " + projectId ;
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    
    private final RowMapper<AttachmentBean> attachmentRowMapper =  new RowMapper<AttachmentBean>() {
        public AttachmentBean mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            AttachmentBean bean = new AttachmentBean();
            bean.setID(rs.getLong("attachmentid"));
            bean.setObjectID(rs.getLong("objectid"));
            bean.setObjectType(rs.getInt("objecttype"));
            bean.setName(rs.getString("filename"));
            bean.setSize(rs.getInt("filesize"));
            bean.setContentType(rs.getString("contenttype"));
            bean.setCreationDate(rs.getLong("creationdate"));
            bean.setModificationDate(rs.getLong("modificationdate"));
            return bean;
        }
    };

    private final RowMapper<PIBReporting> pibReportingRowMapper = new RowMapper<PIBReporting>() {

        public PIBReporting mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            PIBReporting reporting = new PIBReporting();
            reporting.setEndMarketID(rs.getLong("endMarketID"));
            reporting.setProjectID(rs.getLong("projectid"));
            reporting.setTopLinePresentation(rs.getBoolean("toplinepresentation"));
            reporting.setPresentation(rs.getBoolean("presentation"));
            reporting.setFullreport(rs.getBoolean("fullreport"));
            reporting.setGlobalSummary(rs.getBoolean("globalsummary"));
            reporting.setOtherReportingRequirements(rs.getString("otherreportingrequirements"));
            reporting.setOtherReportingRequirementsText(rs.getString("otherreportingrequirementstext"));
            return reporting;
        }
    };

    private final RowMapper<PIBStakeholderList> pibStakeholderListRowMapper = new RowMapper<PIBStakeholderList>() {

        public PIBStakeholderList mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            PIBStakeholderList stakeholderList = new PIBStakeholderList();
            stakeholderList.setEndMarketID(rs.getLong("endMarketID"));
            stakeholderList.setProjectID(rs.getLong("projectid"));
            stakeholderList.setAgencyContact1(rs.getLong("agencycontact1"));
            stakeholderList.setAgencyContact2(rs.getLong("agencycontact2"));
            stakeholderList.setAgencyContact3(rs.getLong("agencycontact3"));
            
            stakeholderList.setAgencyContact1Optional(rs.getLong("agencycontact1optional"));
            stakeholderList.setAgencyContact2Optional(rs.getLong("agencycontact2optional"));
            stakeholderList.setAgencyContact3Optional(rs.getLong("agencycontact3optional"));
            
            stakeholderList.setGlobalLegalContact(rs.getLong("globallegalcontact"));
            stakeholderList.setGlobalProcurementContact(rs.getLong("globalprocurementcontact"));
            stakeholderList.setGlobalCommunicationAgency(rs.getLong("globalcommunicationagency"));
			stakeholderList.setProductContact(rs.getLong("productcontact"));
            return stakeholderList;
        }
    };
    private final RowMapper<PIBMethodologyWaiver> pibMethodologyWaiverRowMapper = new RowMapper<PIBMethodologyWaiver>() {

        public PIBMethodologyWaiver mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            PIBMethodologyWaiver methodologyWaiver = new PIBMethodologyWaiver();
            methodologyWaiver.setEndMarketID(rs.getLong("endmarketid"));
            methodologyWaiver.setProjectID(rs.getLong("projectid"));
            methodologyWaiver.setMethodologyDeviationRationale(rs.getString("methodologydeviationrationale"));
            methodologyWaiver.setMethodologyApprover(rs.getLong("methodologyapprover"));
            methodologyWaiver.setMethodologyApproverComment(rs.getString("methodologyapprovercomment"));
            methodologyWaiver.setIsApproved(rs.getInt("isapproved"));
            methodologyWaiver.setStatus(rs.getInt("status"));
            
            methodologyWaiver.setCreationBy(rs.getLong("creationby"));
            methodologyWaiver.setModifiedBy(rs.getLong("modificationby"));
            methodologyWaiver.setCreationDate(rs.getLong("creationdate"));
            methodologyWaiver.setModifiedDate(rs.getLong("modificationdate"));

            return methodologyWaiver;
        }
    };
    
    private final RowMapper<PIBMethodologyWaiver> pibKantarMethodologyWaiverRowMapper = new RowMapper<PIBMethodologyWaiver>() {

        public PIBMethodologyWaiver mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            PIBMethodologyWaiver methodologyWaiver = new PIBMethodologyWaiver();
            methodologyWaiver.setEndMarketID(rs.getLong("endmarketid"));
            methodologyWaiver.setProjectID(rs.getLong("projectid"));
            methodologyWaiver.setMethodologyDeviationRationale(rs.getString("methodologydeviationrationale"));
            methodologyWaiver.setMethodologyApprover(rs.getLong("methodologyapprover"));
            methodologyWaiver.setMethodologyApproverComment(rs.getString("methodologyapprovercomment"));
            methodologyWaiver.setIsApproved(rs.getInt("isapproved"));
            methodologyWaiver.setStatus(rs.getInt("status"));
            
            methodologyWaiver.setCreationBy(rs.getLong("creationby"));
            methodologyWaiver.setModifiedBy(rs.getLong("modificationby"));
            methodologyWaiver.setCreationDate(rs.getLong("creationdate"));
            methodologyWaiver.setModifiedDate(rs.getLong("modificationdate"));
            methodologyWaiver.setWaiverID(rs.getLong("waiverid"));

            return methodologyWaiver;
        }
    };

    private final ParameterizedRowMapper<PIBMethodologyWaiver> methodologyDashboardRowMapper = new ParameterizedRowMapper<PIBMethodologyWaiver>() {

        public PIBMethodologyWaiver mapRow(ResultSet rs, int row) throws SQLException {
        	PIBMethodologyWaiver methodologyWaiver = new PIBMethodologyWaiver();
        	methodologyWaiver.setProjectID(rs.getLong("projectID"));
        	methodologyWaiver.setProjectName(rs.getString("name"));
        	methodologyWaiver.setEndMarketName("End Market");
        	methodologyWaiver.setBudgetYear(rs.getInt("budgetyear"));
        	methodologyWaiver.setMethodologyApprover(rs.getLong("methodologyapprover"));
        	methodologyWaiver.setApproverName(rs.getString("methodologyapproverName"));
        	methodologyWaiver.setIsApproved(rs.getInt("isapproved"));
        	
        	methodologyWaiver.setMethodologyDeviationRationale(rs.getString("methodologydeviationrationale"));
        	methodologyWaiver.setMethodologyApproverComment(rs.getString("methodologyapprovercomment"));
        	methodologyWaiver.setMethodologyDetails(synchroDAOUtil.getIDs(rs.getString("methodologyDetails")));
        	
        	methodologyWaiver.setCreationBy(rs.getLong("creationby"));
            methodologyWaiver.setModifiedBy(rs.getLong("modificationby"));
            methodologyWaiver.setCreationDate(rs.getLong("creationdate"));
            methodologyWaiver.setModifiedDate(rs.getLong("modificationdate"));
            
        	return methodologyWaiver;
        }
    };
    /**
     * Reusable row mapper for mapping a result set to ProjectInitiation
     */
    private final RowMapper<ProjectInitiation> projectInitiationRowMapper = new RowMapper<ProjectInitiation>() {
        public ProjectInitiation mapRow(ResultSet rs, int row) throws SQLException {
            ProjectInitiation initiationBean = new ProjectInitiation();
            initiationBean.setProjectID(rs.getLong("projectid"));
            initiationBean.setEndMarketID(rs.getLong("endmarketid"));
            initiationBean.setBizQuestion(rs.getString("bizquestion"));
            initiationBean.setResearchObjective(rs.getString("researchobjective"));
            initiationBean.setActionStandard(rs.getString("actionstandard"));
            initiationBean.setResearchDesign(rs.getString("researchdesign"));
            initiationBean.setStimulusMaterial(rs.getString("stimulusmaterial"));
            initiationBean.setOthers(rs.getString("others"));

            if(rs.getLong("stimuliDate") > 0)
            {
                initiationBean.setStimuliDate(new Date(rs.getLong("stimuliDate")));
            }

            initiationBean.setSampleProfile(rs.getString("sampleprofile"));
            initiationBean.setLatestEstimate(rs.getBigDecimal("latestestimate"));
            initiationBean.setLatestEstimateType(rs.getInt("latestestimatetype"));
            initiationBean.setNpiReferenceNo(rs.getString("npireferenceno"));
            initiationBean.setDeviationFromSM(rs.getInt("deviationfromsm"));
            initiationBean.setStatus(rs.getInt("status"));
            initiationBean.setLegalApprovalRcvd(rs.getBoolean("legalapprovalrcvd"));
            initiationBean.setLegalApprovalNotReq(rs.getBoolean("legalapprovalnotreq"));
           // initiationBean.setLegalApprover(rs.getLong("legalapprover"));
            initiationBean.setLegalApprover(rs.getString("legalapprover"));
            initiationBean.setIsEndMarketChanged(rs.getBoolean("isendmarketchanged"));
            initiationBean.setNotifyAboveMarketContacts(rs.getBoolean("notifyabovemarketcontacts"));
            initiationBean.setApproveChanges(rs.getBoolean("approvechanges"));
            initiationBean.setNotifySPI(rs.getBoolean("notifyspi"));
            initiationBean.setNotifyPO(rs.getBoolean("notifypo"));
			initiationBean.setHasTenderingProcess(rs.getLong("hasTenderingProcess"));
            initiationBean.setFieldworkCost(rs.getBigDecimal("fieldworkCost"));
            if(rs.getObject("fieldworkCostCurrency")!=null)
            {
            	initiationBean.setFieldworkCostCurrency(rs.getLong("fieldworkCostCurrency"));	
            }
            
            initiationBean.setBizQuestionText(rs.getString("bizquestiontext"));
            initiationBean.setResearchObjectiveText(rs.getString("researchobjectivetext"));
            initiationBean.setActionStandardText(rs.getString("actionstandardtext"));
            initiationBean.setResearchDesignText(rs.getString("researchdesigntext"));
            initiationBean.setSampleProfileText(rs.getString("sampleprofiletext"));
            initiationBean.setStimulusMaterialText(rs.getString("stimulusmaterialtext"));
            initiationBean.setOthersText(rs.getString("otherstext"));
            //initiationBean.setNonKantar(rs.getBoolean("nonkantar"));
            initiationBean.setNonKantar(rs.getInt("nonkantar"));
            
            if(rs.getLong("pibsavedate") > 0) {
            	initiationBean.setPibSaveDate(new Date(rs.getLong("pibsavedate")));
            }
            if(rs.getLong("piblegalapprovaldate") > 0) {
            	initiationBean.setPibLegalApprovalDate(new Date(rs.getLong("piblegalapprovaldate")));
            }
            if(rs.getLong("pibcompletiondate") > 0) {
            	initiationBean.setPibCompletionDate(new Date(rs.getLong("pibcompletiondate")));
            }
            if(rs.getLong("pibnotifyamcontactsdate") > 0) {
            	initiationBean.setPibNotifyAMContactsDate(new Date(rs.getLong("pibnotifyamcontactsdate")));
            }
            
            initiationBean.setBriefLegalApproverOffline(rs.getString("brieflegalapproveroffline"));
            
            initiationBean.setCreationBy(rs.getLong("creationby"));
            initiationBean.setModifiedBy(rs.getLong("modificationby"));
            initiationBean.setCreationDate(rs.getLong("creationdate"));
            initiationBean.setModifiedDate(rs.getLong("modificationdate"));
            
            return initiationBean;
        }
    };
    private final RowMapper<ProjectInitiation> newProjectInitiationRowMapper = new RowMapper<ProjectInitiation>() {
        public ProjectInitiation mapRow(ResultSet rs, int row) throws SQLException {
            ProjectInitiation initiationBean = new ProjectInitiation();
            initiationBean.setProjectID(rs.getLong("projectid"));
            
            initiationBean.setBrief(rs.getString("brief"));
            initiationBean.setBriefText(rs.getString("brieftext"));
            initiationBean.setBriefLegalApprover(rs.getLong("brieflegalapprover"));
            initiationBean.setStatus(rs.getInt("status"));
            initiationBean.setSendForApproval(rs.getInt("briefsendforapproval"));
            initiationBean.setNeedsDiscussion(rs.getInt("briefneedsdiscussion"));
            initiationBean.setLegalApprovalStatus(rs.getInt("brieflegalapprovalstatus"));
            if(rs.getLong("briefapprovaldate") >0)
            {
            	initiationBean.setLegalApprovalDate(new Date(rs.getLong("briefapprovaldate")));
            }
            
            initiationBean.setLegalSignOffRequired(rs.getInt("islegalsignoffreq"));
            
            if(rs.getLong("sendreminderdate") >0)
            {
            	initiationBean.setSendReminderDate(new Date(rs.getLong("sendreminderdate")));
            }
            
            if(rs.getLong("sendforapprovaldate") >0)
            {
            	initiationBean.setSendForApprovalDate(new Date(rs.getLong("sendforapprovaldate")));
            }
            
            initiationBean.setBriefLegalApproverOffline(rs.getString("brieflegalapproveroffline"));
            return initiationBean;
        }
    };
  
    private final RowMapper<FieldAttachmentBean> fieldAttachmentRowMapper = new RowMapper<FieldAttachmentBean>() {

        public FieldAttachmentBean mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            FieldAttachmentBean fieldAttachment = new FieldAttachmentBean();
            fieldAttachment.setProjectId(rs.getLong("projectid"));
            fieldAttachment.setEndMarketId(rs.getLong("endmarketid"));
            fieldAttachment.setFieldCategoryId(rs.getLong("fieldcategoryid"));
            fieldAttachment.setAttachmentId(rs.getLong("attachmentid"));
            fieldAttachment.setUserId(rs.getLong("userid"));

            return fieldAttachment;
        }
    };

    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updatePIBSendForApproval(final ProjectInitiation projectInitiation) {
        try{
            getSimpleJdbcTemplate().update( UPDATE_PIB_SEND_FOR_APPROVAL,
                    projectInitiation.getSendForApproval(),
                    projectInitiation.getBriefLegalApprover(),
                    projectInitiation.getNeedsDiscussion(),
                    projectInitiation.getModifiedBy(),
                    projectInitiation.getModifiedDate(),
                    
                    projectInitiation.getSendForApprovalDate()!=null?projectInitiation.getSendForApprovalDate().getTime():null,
                    //where clause
                    projectInitiation.getProjectID());
          
        } catch (DataAccessException daEx) {
            final String message = "Failed to UPDATE Send for Approval for projectID  " + projectInitiation.getProjectID() ;
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
        
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void resetPIB(final ProjectInitiation projectInitiation) {
        try{
            getSimpleJdbcTemplate().update( RESET_PIB,
            		null,null,null,null,null,null,null,null,null,null,
                    
                    //where clause
                    projectInitiation.getProjectID());
            
            
          
        } catch (DataAccessException daEx) {
            final String message = "Failed to RESET PIB For Project Id  " + projectInitiation.getProjectID() ;
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
        
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updatePIBSendReminder(final ProjectInitiation projectInitiation) {
        try{
            getSimpleJdbcTemplate().update( UPDATE_SEND_REMINDER,
            		projectInitiation.getSendReminderDate().getTime(),
                    
                    //where clause
                    projectInitiation.getProjectID());
          
        } catch (DataAccessException daEx) {
            final String message = "Failed to UPDATE Send for Reminder for projectID  " + projectInitiation.getProjectID() ;
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
        
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updatePIBNeedsDiscussion(final ProjectInitiation projectInitiation) {
        try{
            getSimpleJdbcTemplate().update( UPDATE_PIB_NEEDS_DISCUSSION,
            		projectInitiation.getNeedsDiscussion(),
            		projectInitiation.getSendForApproval(),
                    projectInitiation.getModifiedBy(),
                    projectInitiation.getModifiedDate(),
                    
                    //where clause
                    projectInitiation.getProjectID());
          
        } catch (DataAccessException daEx) {
            final String message = "Failed to UPDATE Needs Discussion for projectID  " + projectInitiation.getProjectID() ;
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
        
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void confirmLegalApprovalSubmission(final ProjectInitiation projectInitiation) {
        try{
            getSimpleJdbcTemplate().update( CONFIRM_LEGAL_APPROVAL_SUBMISSION,
            		projectInitiation.getNeedsDiscussion(),
            		projectInitiation.getSendForApproval(),
            		projectInitiation.getLegalApprovalDate().getTime(),
            		projectInitiation.getLegalApprovalStatus(),
            		
                    projectInitiation.getModifiedBy(),
                    projectInitiation.getModifiedDate(),
                    
                    //where clause
                    projectInitiation.getProjectID());
          
        } catch (DataAccessException daEx) {
            final String message = "Failed to UPDATE Legal Approval Submission for projectID  " + projectInitiation.getProjectID() ;
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
        
    }
    public SynchroDAOUtil getSynchroDAOUtil() {
        return synchroDAOUtil;
    }

    public void setSynchroDAOUtil(SynchroDAOUtil synchroDAOUtil) {
        this.synchroDAOUtil = synchroDAOUtil;
    }

	public ProjectWaiverDAO getProjectWaiverDAO() {
		return projectWaiverDAO;
	}

	public void setProjectWaiverDAO(ProjectWaiverDAO projectWaiverDAO) {
		this.projectWaiverDAO = projectWaiverDAO;
	}

}
