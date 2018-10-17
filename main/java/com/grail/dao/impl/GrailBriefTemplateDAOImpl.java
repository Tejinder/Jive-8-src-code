package com.grail.dao.impl;

import com.grail.GrailGlobals;
import com.grail.beans.GrailBriefTemplate;
import com.grail.beans.GrailBriefTemplateFilter;
import com.grail.dao.GrailBriefTemplateDAO;
import com.grail.object.GrailAttachment;
import com.grail.synchro.dao.util.SynchroDAOUtil;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroUserPropertiesUtil;
import com.jivesoftware.base.User;
import com.jivesoftware.base.database.dao.DAOException;
import com.jivesoftware.base.database.dao.JiveJdbcDaoSupport;
import com.jivesoftware.community.impl.dao.AttachmentBean;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 8/1/14
 * Time: 11:53 AM
 * To change this template use File | Settings | File Templates.
 */
public class GrailBriefTemplateDAOImpl extends JiveJdbcDaoSupport implements GrailBriefTemplateDAO {

    private final static Logger LOG = Logger.getLogger(GrailBriefTemplateDAOImpl.class);

    private SynchroDAOUtil synchroDAOUtil;

    private final static String TABLE_NAME = "grailbrieftemplate";

//    private final static String FIELDS = "researchneedspriorities, hypothesesbusinessneeds, markets, " +
//            "products, brands, categories, deliverydatetime, outputformat, captureddatetime, recipientemail, sender, isdraft";

    private final static String FIELDS = "researchneedspriorities, hypothesesbusinessneeds, markets, products, brands, " +
            "categories, deliverydatetime, outputformat, captureddatetime, recipientemail, sender, isdraft, methodologytype, " +
            "finalcost, finalcostcurrency, datasource, comments, batcontact, status, createdby, creationdate, modifiedby, modificationdate";

    private final static String INSERT_BRIEF_TEMPLATE = "INSERT INTO " + TABLE_NAME + " (id, " + FIELDS + ") " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    private final static String UPDATE_BRIEF_TEMPLATE = "UPDATE " + TABLE_NAME + " SET " + FIELDS.replaceAll(",", "=?,") + "=?" + " WHERE id=?";

    private final static String GET_ALL = "SELECT id," + FIELDS + " FROM " + TABLE_NAME + " ORDER BY id";
    private final static String GET_ALL_BY_SENDER = "SELECT id," + FIELDS + " FROM " + TABLE_NAME + " WHERE sender=? ORDER BY id";
    private final static String GET_BY_ID = "SELECT id," + FIELDS + " FROM " + TABLE_NAME + " WHERE id=?";
    private final static String GET_BY_ID_SENDER = "SELECT id," + FIELDS + " FROM " + TABLE_NAME + " WHERE id=? AND sender=?";

    private final static String GET_DRAFT_TEMPLATE = "SELECT id," + FIELDS + " FROM " + TABLE_NAME + " WHERE sender=? AND isdraft = 1";

    private final static String DELETE_DRAFT_TEMPLATE = "DELETE FROM "+ TABLE_NAME + " WHERE sender=? AND isdraft = 1";

    private static final String GET_ATTACHMENT_BY_OBJECT = "SELECT * from jiveattachment where objecttype=? AND objectid=?";

    private static final String INSERT_ATTACHMENT_USER = "INSERT INTO grailattachmentuser(attachmentid, userid) VALUES (?, ?)";
    private static final String INSERT_ATTACHMENT_USER_BY_ATTACHMENTS = "INSERT INTO grailattachmentuser(attachmentid, userid) VALUES ";

    private static final String DELETE_ATTACHMENT_USER = "DELETE from grailattachmentuser where attachmentid=? ";
    private static final String DELETE_ATTACHMENT_USER_ATTACHMENTS = "DELETE from grailattachmentuser where attachmentid in ";

    private static final String GET_ATTACHMENT_USER = "select userid from grailattachmentuser where attachmentid=? ";

    @Override
    public Long save(final GrailBriefTemplate briefTemplate) {
        try {
            Long id = synchroDAOUtil.nextSequenceID("id", TABLE_NAME);
            briefTemplate.setId(id);
            getSimpleJdbcTemplate().getJdbcOperations().update(INSERT_BRIEF_TEMPLATE,
                    briefTemplate.getId(),
                    briefTemplate.getResearchNeedsPriorities() != null?briefTemplate.getResearchNeedsPriorities().trim():null,
                    briefTemplate.getHypothesisBusinessNeed() != null?briefTemplate.getHypothesisBusinessNeed().trim():null,
                    briefTemplate.getMarkets() != null?briefTemplate.getMarkets():-1,
                    briefTemplate.getProducts() != null?briefTemplate.getProducts().trim():null,
                    briefTemplate.getBrands() != null?briefTemplate.getBrands().trim():null,
                    briefTemplate.getCategories() != null?briefTemplate.getCategories().trim():null,
                    briefTemplate.getDeliveryDate() != null?briefTemplate.getDeliveryDate().getTime():null,
                    (briefTemplate.getOutputFormat() == null || briefTemplate.getOutputFormat().equals(-1))?null:briefTemplate.getOutputFormat(),
                    briefTemplate.getCapturedDate() != null?briefTemplate.getCapturedDate().getTime():((new Date()).getTime()),
                    briefTemplate.getRecipientEmail(),
                    briefTemplate.getSender(),
                    briefTemplate.getDraft()?1:0,
                    briefTemplate.getMethodologyType(),
                    briefTemplate.getFinalCost(),
                    briefTemplate.getFinalCostCurrency(),
                    briefTemplate.getDataSource(),
                    briefTemplate.getComments(),
                    briefTemplate.getBatContact(),
                    briefTemplate.getStatus(),
                    briefTemplate.getCreatedBy(),
                    briefTemplate.getCreationDate() != null?briefTemplate.getCreationDate().getTime():(new Date()).getTime(),
                    briefTemplate.getModifiedBy(),
                    briefTemplate.getModificationDate() != null?briefTemplate.getModificationDate().getTime():(new Date()).getTime()
            );
            return id;
        } catch (DAOException e) {
            LOG.error(e.getMessage());
        }
        return null;
    }

    @Override
    public Long update(final GrailBriefTemplate briefTemplate) {
        try {
            getSimpleJdbcTemplate().getJdbcOperations().update(UPDATE_BRIEF_TEMPLATE,
                    briefTemplate.getResearchNeedsPriorities() != null?briefTemplate.getResearchNeedsPriorities().trim():null,
                    briefTemplate.getHypothesisBusinessNeed() != null?briefTemplate.getHypothesisBusinessNeed().trim():null,
                    briefTemplate.getMarkets() != null?briefTemplate.getMarkets():-1,
                    briefTemplate.getProducts() != null?briefTemplate.getProducts().trim():null,
                    briefTemplate.getBrands() != null?briefTemplate.getBrands().trim():null,
                    briefTemplate.getCategories() != null?briefTemplate.getCategories().trim():null,
                    briefTemplate.getDeliveryDate() != null?briefTemplate.getDeliveryDate().getTime():null,
                    (briefTemplate.getOutputFormat() == null || briefTemplate.getOutputFormat().equals(-1))?null:briefTemplate.getOutputFormat(),
                    briefTemplate.getCapturedDate() != null?briefTemplate.getCapturedDate().getTime():((new Date()).getTime()),
                    briefTemplate.getRecipientEmail(),
                    briefTemplate.getSender(),
                    briefTemplate.getDraft()?1:0,
                    briefTemplate.getMethodologyType(),
                    briefTemplate.getFinalCost(),
                    briefTemplate.getFinalCostCurrency(),
                    briefTemplate.getDataSource(),
                    briefTemplate.getComments(),
                    briefTemplate.getBatContact(),
                    briefTemplate.getStatus(),
                    briefTemplate.getCreatedBy(),
                    briefTemplate.getCreationDate() != null?briefTemplate.getCreationDate().getTime():(new Date()).getTime(),
                    briefTemplate.getModifiedBy(),
                    briefTemplate.getModificationDate() != null?briefTemplate.getModificationDate().getTime():(new Date()).getTime(),
                    briefTemplate.getId()

            );

        } catch (DataAccessException e) {
            LOG.error(e.getMessage());
        }
        return briefTemplate.getId();
    }

    @Override
    public GrailBriefTemplate get(Long id) {
        GrailBriefTemplate bean = null;
        try{
            bean = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(GET_BY_ID,
                    grailBriefTemplateParameterizedRowMapper, id);
        } catch (DataAccessException e) {
            LOG.error(e.getMessage());
        }
        return bean;
    }

    @Override
    public GrailBriefTemplate get(Long id, Long userId) {
        GrailBriefTemplate bean = null;
        try{
            bean = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(GET_BY_ID_SENDER,
                    grailBriefTemplateParameterizedRowMapper, id,userId);
        } catch (DataAccessException e) {
            LOG.error(e.getMessage());
        }
        return bean;
    }

    @Override
    public List<GrailBriefTemplate> getAll(Long userId) {
        List<GrailBriefTemplate> grailBriefTemplates = Collections.emptyList();
        try {
            grailBriefTemplates = getSimpleJdbcTemplate().getJdbcOperations().query(GET_ALL_BY_SENDER,
                    grailBriefTemplateParameterizedRowMapper,userId);
        } catch (DataAccessException e) {
            LOG.error(e.getMessage());
        }
        return grailBriefTemplates;
    }

    @Override
    public List<GrailBriefTemplate> getAll() {
        List<GrailBriefTemplate> grailBriefTemplates = Collections.emptyList();
        try {
            grailBriefTemplates = getSimpleJdbcTemplate().getJdbcOperations().query(GET_ALL,
                    grailBriefTemplateParameterizedRowMapper);
        } catch (DataAccessException e) {
            LOG.error(e.getMessage());
        }
        return grailBriefTemplates;
    }

    @Override
    public List<GrailBriefTemplate> getAll(GrailBriefTemplateFilter grailBriefTemplateFilter) {
        List<GrailBriefTemplate> grailBriefTemplates = Collections.emptyList();
        String fields =  "id," + FIELDS;
        fields += ", (select (ju.firstname || ' ' || ju.lastname) from jiveuser ju where ju.userid = createdby) as ownerName";
        fields += ", (select em.name from grailendmarketfields em where em.id = markets) as marketName";
        fields += ", (SELECT m.name FROM grailbuttonmethodologytype m WHERE m.id = methodologytype) as methodologyTypeName";
//        fields += ", (CASE" +
//                " WHEN methodologytype = "+ GrailGlobals.GrailMethodologyType.QUALITATIVE.getId()+" THEN '" + GrailGlobals.GrailMethodologyType.QUALITATIVE.getName() + "'" +
//                " WHEN methodologytype = "+ GrailGlobals.GrailMethodologyType.DESK_RESEARCH.getId()+" THEN '" +
//                "" + GrailGlobals.GrailMethodologyType.DESK_RESEARCH.getName() + "'" +
//                " WHEN methodologytype = "+ GrailGlobals.GrailMethodologyType.OTHERS.getId()+" THEN '" + GrailGlobals.GrailMethodologyType.OTHERS.getName() + "'" +
//                " ELSE ''"+
//                " END) as methodologyTypeName";

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT " + fields + " FROM " + TABLE_NAME );
        sqlBuilder.append(applyFilter(grailBriefTemplateFilter));
        if(grailBriefTemplateFilter != null && grailBriefTemplateFilter.getSortField() != null) {
            sqlBuilder.append(getOrderByField(grailBriefTemplateFilter.getSortField(), grailBriefTemplateFilter.getAscendingOrder()));
        } else  {
            sqlBuilder.append(" ORDER BY id");
        }
        if(grailBriefTemplateFilter != null) {
            if(grailBriefTemplateFilter.getStart() != null) {
                sqlBuilder.append(" OFFSET ").append(grailBriefTemplateFilter.getStart());
            }
            if(grailBriefTemplateFilter.getLimit() != null && grailBriefTemplateFilter.getLimit() > 0) {
                sqlBuilder.append(" LIMIT ").append(grailBriefTemplateFilter.getLimit());
            }
        }
        try {
            grailBriefTemplates = getSimpleJdbcTemplate().getJdbcOperations().query(sqlBuilder.toString(),
                    grailBriefTemplateParameterizedRowMapper);
        } catch (DataAccessException e) {
            LOG.error(e.getMessage());
        }
        return grailBriefTemplates;
    }

    private String getOrderByField(final String sortField, final Integer order) {
        if(StringUtils.isNotBlank(sortField)) {
            StringBuilder orderBy = new StringBuilder();
            String field = null;
            if(sortField.equals("owner")) {
                field = "ownerName";
            } else if(sortField.equals("market")) {
                field = "marketName";
            } else if(sortField.equals("requestRaised")) {
                field = "creationdate";
            } else if(sortField.equals("deliveryDate")) {
                field = "deliverydatetime";
            } else if(sortField.equals("methodologyType")) {
                field = "methodologyTypeName";
            } else {
                field = sortField;
            }
            if(StringUtils.isNotBlank(field)) {
                orderBy.append(" ORDER BY ");
                orderBy.append(field).append(" ").append(SynchroDAOUtil.getSortType(order));
                if(!field.equals("id")) {
                    orderBy.append(",id").append(" ").append(SynchroDAOUtil.getSortType(order));
                }
            }
            return orderBy.toString();
        } else {
            return " ORDER BY id";
        }
    }

    private String applyFilter(final GrailBriefTemplateFilter grailBriefTemplateFilter) {
        User user = SynchroPermHelper.getEffectiveUser();
        StringBuilder filterBuilder = new StringBuilder();
        List<String> conditions = new ArrayList<String>();
        if(!(SynchroPermHelper.isSynchroAdmin(user) || SynchroPermHelper.isSynchroMiniAdmin() || SynchroPermHelper.isSynchroGlobalSuperUser(user))) {
            List<String> accessConditions = new ArrayList<String>();
            accessConditions.add("createdby = "+user.getID());
            accessConditions.add("batcontact = "+user.getID());

            Map<String, String> userProperties = user.getProperties();
            if(userProperties != null && !userProperties.isEmpty()) {
                if(userProperties.containsKey(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_LIST)
                        && userProperties.get(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_LIST) != null
                        && !userProperties.get(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_LIST).equals("")) {
                    accessConditions.add("markets in ("+userProperties.get(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_LIST)+")");
                }
            }

            if(accessConditions.size() > 0) {
                conditions.add("(" + StringUtils.join(accessConditions, " OR ") + ")");
            }
        }

        if(grailBriefTemplateFilter != null) {
            // Keyword filter
            if(grailBriefTemplateFilter.getKeyword() != null && !grailBriefTemplateFilter.getKeyword().equals("")) {
                StringBuilder keyWordFilter = new StringBuilder();
                // Code filter
                Pattern pattern = Pattern.compile("^G0{0,4}([1-9][0-9]*)*$", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(grailBriefTemplateFilter.getKeyword());
                if(matcher != null && matcher.matches()) {
                    keyWordFilter.append("(lower(CASE WHEN char_length('' || id || '') < 5 THEN ('G' || (CASE WHEN char_length('' || id || '') = 4 THEN '0' WHEN char_length('' || id || '') = 3 THEN '00' WHEN char_length('' || id || '') = 2 THEN '000' WHEN char_length('' || id || '') = 1 THEN '0000' END)  || id) ELSE ('G' || id || '') END) like '%"+grailBriefTemplateFilter.getKeyword().toLowerCase()+"%')");
                } else {
                    keyWordFilter.append("((''|| id ||'') like '%"+grailBriefTemplateFilter.getKeyword()+"%')");
                }
                // Owner filter
                keyWordFilter.append(" OR ").append("((SELECT count(*) FROM jiveuser ju WHERE ju.userid = createdby AND lower(ju.firstname || ' ' || ju.lastname) like '%"+grailBriefTemplateFilter.getKeyword().toLowerCase()+"%') > 0)");
                // markets filter
                keyWordFilter.append(" OR ").append("((SELECT lower(em.name) FROM grailendmarketfields em WHERE em.id = markets) like '%"+grailBriefTemplateFilter.getKeyword().toLowerCase()+"%')");
                // Methodology type filter
                keyWordFilter.append(" OR ").append("((SELECT count(*) FROM grailbuttonmethodologytype mt WHERE mt.id = methodologytype AND lower(mt.name) like '%"+grailBriefTemplateFilter.getKeyword().toLowerCase()+"%') > 0)");
                //keyWordFilter.append(" OR ").append("(methodologytype = -100 AND lower('Others') like '%"+grailBriefTemplateFilter.getKeyword().toLowerCase()+"%')");
                // Raised date filter
                keyWordFilter.append(" OR ").append("(((to_char(to_timestamp(creationDate/1000),'dd') || '/' || to_char(to_timestamp(creationDate/1000),'mm') || '/' || to_char(to_timestamp(creationDate/1000),'YYYY'))) like '%"+grailBriefTemplateFilter.getKeyword()+"%')");
                // delivery date filter
                keyWordFilter.append(" OR ").append("(((to_char(to_timestamp(deliverydatetime/1000),'dd') || '/' || to_char(to_timestamp(deliverydatetime/1000),'mm') || '/' || to_char(to_timestamp(deliverydatetime/1000),'YYYY'))) like '%"+grailBriefTemplateFilter.getKeyword()+"%')");
                // Status
                keyWordFilter.append(" OR ").append("(status = "+ GrailGlobals.GrailBriefTemplateStatusType.IN_PROGRESS.getId()+" AND lower('"+ GrailGlobals.GrailBriefTemplateStatusType.IN_PROGRESS.getName()+"') like '%"+grailBriefTemplateFilter.getKeyword().toLowerCase()+"%')");
                keyWordFilter.append(" OR ").append("(status = "+ GrailGlobals.GrailBriefTemplateStatusType.COMPLETED.getId()+" AND lower('"+ GrailGlobals.GrailBriefTemplateStatusType.COMPLETED.getName()+"') like '%"+grailBriefTemplateFilter.getKeyword().toLowerCase()+"%')");
                keyWordFilter.append(" OR ").append("(status = "+ GrailGlobals.GrailBriefTemplateStatusType.CANCELLED.getId()+" AND lower('"+ GrailGlobals.GrailBriefTemplateStatusType.CANCELLED.getName()+"') like '%"+grailBriefTemplateFilter.getKeyword().toLowerCase()+"%')");
                conditions.add(keyWordFilter.toString());
            }

            // Owner filter
            if(grailBriefTemplateFilter.getInitiators() != null && grailBriefTemplateFilter.getInitiators().size() > 0) {
                conditions.add("(createdby in ("+StringUtils.join(grailBriefTemplateFilter.getInitiators(),",")+"))");
            }

            if(grailBriefTemplateFilter.getBatContacts() != null && grailBriefTemplateFilter.getBatContacts().size() > 0) {
                conditions.add("(batcontact in ("+StringUtils.join(grailBriefTemplateFilter.getBatContacts(),",")+"))");
            }

            if(grailBriefTemplateFilter.getEndMarkets() != null && grailBriefTemplateFilter.getEndMarkets().size() > 0) {
                conditions.add("(markets in ("+StringUtils.join(grailBriefTemplateFilter.getEndMarkets(),",")+"))");
            }

            if(grailBriefTemplateFilter.getMethodologies() != null && grailBriefTemplateFilter.getMethodologies().size() > 0) {
                conditions.add("(methodologytype in ("+StringUtils.join(grailBriefTemplateFilter.getMethodologies(),",")+"))");
            }

            if(grailBriefTemplateFilter.getDeliveryDateFrom() != null || grailBriefTemplateFilter.getDeliveryDateTo() != null) {
                if(grailBriefTemplateFilter.getDeliveryDateFrom() != null && grailBriefTemplateFilter.getDeliveryDateTo() != null) {
                    if(grailBriefTemplateFilter.getDeliveryDateFrom().getTime() <= grailBriefTemplateFilter.getDeliveryDateTo().getTime()) {
                        conditions.add("(deliverydatetime between " + grailBriefTemplateFilter.getDeliveryDateFrom().getTime() + " AND " + grailBriefTemplateFilter.getDeliveryDateTo().getTime()+")");
                    }
                } else if(grailBriefTemplateFilter.getDeliveryDateFrom() != null) {
                    conditions.add("(deliverydatetime between " + grailBriefTemplateFilter.getDeliveryDateFrom().getTime() + " AND (select max(deliverydatetime) from grailbrieftemplate))");
                } else if(grailBriefTemplateFilter.getDeliveryDateTo() != null) {
                    conditions.add("(deliverydatetime between (select min(deliverydatetime) from grailbrieftemplate) AND " + grailBriefTemplateFilter.getDeliveryDateTo().getTime()+")");
                }
            }
        }

        if(conditions.size() > 0) {
            filterBuilder.append(" WHERE (").append(StringUtils.join(conditions," AND ")).append(")");
        }
        return filterBuilder.toString();
    }

    @Override
    public Integer getTotalCount(GrailBriefTemplateFilter grailBriefTemplateFilter) {
        Integer count = 0;
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT count(*) FROM ").append(TABLE_NAME);
        sqlBuilder.append(applyFilter(grailBriefTemplateFilter));
        try {
            count = getSimpleJdbcTemplate().getJdbcOperations().queryForInt(sqlBuilder.toString());
        } catch (DataAccessException e) {
            LOG.error(e.getMessage());
        }
        return count;
    }

    @Override
    public GrailBriefTemplate getDraftTemplate(final Long userId) {
        GrailBriefTemplate bean = null;
        try{
            bean = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(GET_DRAFT_TEMPLATE,
                    grailBriefTemplateParameterizedRowMapper, userId);
        } catch (DataAccessException e) {
            LOG.error(e.getMessage());
        }
        return bean;
    }

    @Override
    public void deleteDraftTemplate(final Long userId) {
        try{
            getSimpleJdbcTemplate().getJdbcOperations().update(DELETE_DRAFT_TEMPLATE, userId);
        } catch (DataAccessException e) {
            LOG.error(e.getMessage());
        }
    }

    private final ParameterizedRowMapper<GrailBriefTemplate> grailBriefTemplateParameterizedRowMapper
            = new ParameterizedRowMapper<GrailBriefTemplate>() {
        public GrailBriefTemplate mapRow(final ResultSet rs, final int row) throws SQLException {
            GrailBriefTemplate grailBriefTemplate = new GrailBriefTemplate();
            grailBriefTemplate.setId(rs.getLong("id"));
            grailBriefTemplate.setResearchNeedsPriorities(rs.getString("researchneedspriorities"));
            grailBriefTemplate.setHypothesisBusinessNeed(rs.getString("hypothesesbusinessneeds"));
            grailBriefTemplate.setMarkets(rs.getLong("markets"));
            grailBriefTemplate.setProducts(rs.getString("products"));
            grailBriefTemplate.setBrands(rs.getString("brands"));
            grailBriefTemplate.setCategories(rs.getString("categories"));
            if(rs.getLong("deliverydatetime") > 0) {
                grailBriefTemplate.setDeliveryDate(new Date(rs.getLong("deliverydatetime")));
            }
            if(rs.getInt("outputformat") > 0) {
                grailBriefTemplate.setOutputFormat(rs.getInt("outputformat"));
            }
            grailBriefTemplate.setCapturedDate(new Date(rs.getLong("captureddatetime")));
            grailBriefTemplate.setRecipientEmail(rs.getString("recipientemail"));
            grailBriefTemplate.setSender(rs.getLong("sender"));
            grailBriefTemplate.setDraft(rs.getBoolean("isdraft"));
            grailBriefTemplate.setMethodologyType(rs.getLong("methodologytype"));
            grailBriefTemplate.setFinalCost(rs.getBigDecimal("finalcost"));
            grailBriefTemplate.setFinalCostCurrency(rs.getLong("finalcostcurrency"));
            grailBriefTemplate.setDataSource(rs.getString("datasource"));
            grailBriefTemplate.setComments(rs.getString("comments"));
            grailBriefTemplate.setBatContact(rs.getLong("batcontact"));
            grailBriefTemplate.setStatus(rs.getInt("status"));
            grailBriefTemplate.setCreatedBy(rs.getLong("createdby"));
            grailBriefTemplate.setCreationDate(new Date(rs.getLong("creationdate")));
            grailBriefTemplate.setModifiedBy(rs.getLong("modifiedby"));
            grailBriefTemplate.setModificationDate(new Date(rs.getLong("modificationdate")));
            return grailBriefTemplate;
        }
    };


    @Override
    public List<AttachmentBean> getGrailAttachments(long objectId, int objectType) {
        List<AttachmentBean> attachments = new ArrayList<AttachmentBean>();
        try{
            attachments = getSimpleJdbcTemplate().query(GET_ATTACHMENT_BY_OBJECT, attachmentRowMapper, objectType, objectId);
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return attachments;
    }

    @Override
    public List<AttachmentBean> getGrailAttachments(final GrailAttachment grailAttachment) {
        List<AttachmentBean> attachments = new ArrayList<AttachmentBean>();
        try{
            attachments = getSimpleJdbcTemplate().query(GET_ATTACHMENT_BY_OBJECT, attachmentRowMapper, grailAttachment.getObjectType(), grailAttachment.getID());
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return attachments;
    }

    @Override
    public void saveAttachmentUser(Long attachmentId, Long userId) {
        try {
            getSimpleJdbcTemplate().update(INSERT_ATTACHMENT_USER,  attachmentId, userId);

        } catch (DataAccessException daEx) {
            final String message = "Failed to SAVE Attachment User for Attachment Id - " + attachmentId;
            daEx.printStackTrace();
            throw new DAOException(message, daEx);
        }
    }

    @Override
    public void saveAttachmentUser(List<Long> attachmentIds, Long userId) {
        try {
            StringBuilder insertAttachmentUsers = new StringBuilder();
            insertAttachmentUsers.append(INSERT_ATTACHMENT_USER_BY_ATTACHMENTS);
            int it = 0;
            for(Long attachmentId: attachmentIds) {
                if(it > 0) {
                    insertAttachmentUsers.append(",");
                }
                insertAttachmentUsers.append("("+attachmentId+", "+userId+")");
                it++;
            }
            getSimpleJdbcTemplate().update(insertAttachmentUsers.toString());
        } catch (DataAccessException daEx) {
            final String message = "Failed to SAVE Attachment User for Attachment Ids - " + StringUtils.join(attachmentIds, ",");
            daEx.printStackTrace();
            throw new DAOException(message, daEx);
        }
    }

    @Override
    public void deleteAttachmentUser(Long attachmentId) {
        try {
            getSimpleJdbcTemplate().update(DELETE_ATTACHMENT_USER,  attachmentId);

        } catch (DataAccessException daEx) {
            final String message = "Failed to Delete Attachment User for Attachment Id - " + attachmentId;
            daEx.printStackTrace();
            throw new DAOException(message, daEx);
        }
    }

    @Override
    public void deleteAttachmentUser(List<Long> attachmentIds) {
        try {
            StringBuilder deleteAttachmentUsers = new StringBuilder();
            deleteAttachmentUsers.append(DELETE_ATTACHMENT_USER_ATTACHMENTS);
            deleteAttachmentUsers.append(" (").append(StringUtils.join(attachmentIds, ",")).append(")");

        } catch (DataAccessException daEx) {
            final String message = "Failed to Delete Attachment User for Attachment Ids - " + StringUtils.join(attachmentIds, ",");
            daEx.printStackTrace();
            throw new DAOException(message, daEx);
        }
    }

    @Override
    public Long getAttachmentUser(Long attachmentId) {
        Long userId = null;
        try {
            userId = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(GET_ATTACHMENT_USER, attachmentId);
        } catch (DataAccessException daEx) {
        }
        return userId;
    }

    @Override
    public List<GrailBriefTemplate> getPendingActivities(final Long userId) {
        List<GrailBriefTemplate> grailBriefTemplates = Collections.emptyList();
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT id,"+FIELDS+" FROM "+TABLE_NAME);
        sqlBuilder.append(" WHERE ").append("(batcontact = "+userId+" OR createdby = "+userId+") AND finalcost is null AND status="+GrailGlobals.GrailBriefTemplateStatusType.IN_PROGRESS.getId()+"");
        try {
            grailBriefTemplates = getSimpleJdbcTemplate().getJdbcOperations().query(sqlBuilder.toString(),
                    grailBriefTemplateParameterizedRowMapper);
        } catch (DataAccessException e) {
            LOG.error(e.getMessage());
        }
        return grailBriefTemplates;
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

    public SynchroDAOUtil getSynchroDAOUtil() {
        return synchroDAOUtil;
    }

    public void setSynchroDAOUtil(SynchroDAOUtil synchroDAOUtil) {
        this.synchroDAOUtil = synchroDAOUtil;
    }
}
