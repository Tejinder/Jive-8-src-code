package com.grail.kantar.dao.impl;

import com.grail.kantar.beans.KantarBriefTemplate;
import com.grail.kantar.beans.KantarBriefTemplateFilter;
import com.grail.kantar.dao.KantarBriefTemplateDAO;
import com.grail.kantar.object.KantarAttachment;
import com.grail.kantar.util.KantarGlobals;
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
 * Date: 10/16/14
 * Time: 1:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class KantarBriefTemplateDAOImpl extends JiveJdbcDaoSupport implements KantarBriefTemplateDAO {

    private final static Logger LOG = Logger.getLogger(KantarBriefTemplateDAOImpl.class);

    private SynchroDAOUtil synchroDAOUtil;

    private final static String TABLE_NAME = "grailkantarbrieftemplate";

    private final static String FIELDS = "researchneedspriorities, hypothesesbusinessneeds, markets, products, brands, " +
            "categories, deliverydatetime, outputformat, captureddatetime, recipientemail, sender, isdraft, methodologytype, " +
            "finalcost, finalcostcurrency, datasource, comments, batcontact, status, createdby, creationdate, modifiedby, modificationdate";

//    private final static String FIELDS = "researchneedspriorities, hypothesesbusinessneeds, markets, " +
//            "products, brands, categories, deliverydatetime, outputformat, captureddatetime, recipientemail, sender, isdraft, batcontact";

    private final static String INSERT_BRIEF_TEMPLATE = "INSERT INTO " + TABLE_NAME + " (id, " + FIELDS + ") " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    private final static String UPDATE_BRIEF_TEMPLATE = "UPDATE " + TABLE_NAME + " SET " + FIELDS.replaceAll(",", "=?,") + "=?" + " WHERE id=?";

    private final static String GET_ALL = "SELECT id," + FIELDS + " FROM " + TABLE_NAME;
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
    public Long save(final KantarBriefTemplate briefTemplate) {
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
    public Long update(final KantarBriefTemplate briefTemplate) {
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
    public KantarBriefTemplate get(final Long id) {
        KantarBriefTemplate bean = null;
        try{
            bean = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(GET_BY_ID,
                    kantarBriefTemplateParameterizedRowMapper, id);
        } catch (DataAccessException e) {
            LOG.error(e.getMessage());
        }
        return bean;
    }

    @Override
    public KantarBriefTemplate get(final Long id, final Long userId) {
        KantarBriefTemplate bean = null;
        try{
            bean = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(GET_BY_ID_SENDER,
                    kantarBriefTemplateParameterizedRowMapper, id, userId);
        } catch (DataAccessException e) {
            LOG.error(e.getMessage());
        }
        return bean;
    }

    @Override
    public List<KantarBriefTemplate> getAll(final Long userId) {
        List<KantarBriefTemplate> kantarBriefTemplates = Collections.emptyList();
        try {
            kantarBriefTemplates = getSimpleJdbcTemplate().getJdbcOperations().query(GET_ALL_BY_SENDER,
                    kantarBriefTemplateParameterizedRowMapper,userId);
        } catch (DataAccessException e) {
            LOG.error(e.getMessage());
        }
        return kantarBriefTemplates;
    }

    @Override
    public List<KantarBriefTemplate> getAll() {
        List<KantarBriefTemplate> kantarBriefTemplates = Collections.emptyList();
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append(GET_ALL);
        sqlBuilder.append(" ORDER BY id");
        try {
            kantarBriefTemplates = getSimpleJdbcTemplate().getJdbcOperations().query(sqlBuilder.toString(),
                    kantarBriefTemplateParameterizedRowMapper);
        } catch (DataAccessException e) {
            LOG.error(e.getMessage());
        }
        return kantarBriefTemplates;
    }

    @Override
    public List<KantarBriefTemplate> getAll(final KantarBriefTemplateFilter kantarBriefTemplateFilter) {
        List<KantarBriefTemplate> kantarBriefTemplates = Collections.emptyList();
        String fields =  "id," + FIELDS;
        fields += ", (select (ju.firstname || ' ' || ju.lastname) from jiveuser ju where ju.userid = createdby) as ownerName";
        fields += ", (select em.name from grailendmarketfields em where em.id = markets) as marketName";
        fields += ", (SELECT m.name FROM grailkantarbtnmethodologytype m WHERE m.id = methodologytype) as methodologyTypeName";
//        fields += ", (CASE" +
//                " WHEN methodologytype = "+ KantarGlobals.KantarMethodologyType.QUALITATIVE.getId()+" THEN '" + KantarGlobals.KantarMethodologyType.QUALITATIVE.getName() + "'" +
//                " WHEN methodologytype = "+ KantarGlobals.KantarMethodologyType.DESK_RESEARCH.getId()+" THEN '" +
//                "" + KantarGlobals.KantarMethodologyType.DESK_RESEARCH.getName() + "'" +
//                " WHEN methodologytype = "+ KantarGlobals.KantarMethodologyType.OTHERS.getId()+" THEN '" + KantarGlobals.KantarMethodologyType.OTHERS.getName() + "'" +
//                " ELSE ''"+
//                " END) as methodologyTypeName";

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT " + fields + " FROM " + TABLE_NAME );
        sqlBuilder.append(applyFilter(kantarBriefTemplateFilter));
        if(kantarBriefTemplateFilter != null && kantarBriefTemplateFilter.getSortField() != null) {
            sqlBuilder.append(getOrderByField(kantarBriefTemplateFilter.getSortField(), kantarBriefTemplateFilter.getAscendingOrder()));
        } else  {
            sqlBuilder.append(" ORDER BY id");
        }
        if(kantarBriefTemplateFilter != null) {
            if(kantarBriefTemplateFilter.getStart() != null) {
                sqlBuilder.append(" OFFSET ").append(kantarBriefTemplateFilter.getStart());
            }
            if(kantarBriefTemplateFilter.getLimit() != null && kantarBriefTemplateFilter.getLimit() > 0) {
                sqlBuilder.append(" LIMIT ").append(kantarBriefTemplateFilter.getLimit());
            }
        }
        try {
            kantarBriefTemplates = getSimpleJdbcTemplate().getJdbcOperations().query(sqlBuilder.toString(),
                    kantarBriefTemplateParameterizedRowMapper);
        } catch (DataAccessException e) {
            LOG.error(e.getMessage());
        }
        return kantarBriefTemplates;
    }

    @Override
    public Integer getTotalCount(final KantarBriefTemplateFilter kantarBriefTemplateFilter) {
        Integer count = 0;
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT count(*) FROM ").append(TABLE_NAME);
        sqlBuilder.append(applyFilter(kantarBriefTemplateFilter));
        try {
            count = getSimpleJdbcTemplate().getJdbcOperations().queryForInt(sqlBuilder.toString());
        } catch (DataAccessException e) {
            LOG.error(e.getMessage());
        }
        return count;
    }

    private String applyFilter(final KantarBriefTemplateFilter kantarBriefTemplateFilter) {
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

        if(kantarBriefTemplateFilter != null) {
            // Keyword filter
            if(kantarBriefTemplateFilter.getKeyword() != null && !kantarBriefTemplateFilter.getKeyword().equals("")) {
                StringBuilder keyWordFilter = new StringBuilder();
                // Code filter
                Pattern pattern = Pattern.compile("^K0{0,4}([1-9][0-9]*)*$", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(kantarBriefTemplateFilter.getKeyword());
                if(matcher != null && matcher.matches()) {
                    keyWordFilter.append("(lower(CASE WHEN char_length('' || id || '') < 5 THEN ('K' || (CASE WHEN char_length('' || id || '') = 4 THEN '0' WHEN char_length('' || id || '') = 3 THEN '00' WHEN char_length('' || id || '') = 2 THEN '000' WHEN char_length('' || id || '') = 1 THEN '0000' END)  || id) ELSE ('K' || id || '') END) like '%"+kantarBriefTemplateFilter.getKeyword().toLowerCase()+"%')");
                } else {
                    keyWordFilter.append("((''|| id ||'') like '%"+kantarBriefTemplateFilter.getKeyword()+"%')");
                }
                // Owner filter
                keyWordFilter.append(" OR ").append("((SELECT count(*) FROM jiveuser ju WHERE ju.userid = createdby AND lower(ju.firstname || ' ' || ju.lastname) like '%"+kantarBriefTemplateFilter.getKeyword().toLowerCase()+"%') > 0)");
                // markets filter
                keyWordFilter.append(" OR ").append("((SELECT lower(em.name) FROM grailendmarketfields em WHERE em.id = markets) like '%"+kantarBriefTemplateFilter.getKeyword().toLowerCase()+"%')");
                // Methodology type filter
                keyWordFilter.append(" OR ").append("((SELECT count(*) FROM grailkantarbtnmethodologytype mt WHERE mt.id = methodologytype AND lower(mt.name) like '%"+kantarBriefTemplateFilter.getKeyword().toLowerCase()+"%') > 0)");
                //keyWordFilter.append(" OR ").append("(methodologytype = -100 AND lower('Others') like '%"+kantarBriefTemplateFilter.getKeyword().toLowerCase()+"%')");
                // Raised date filter
                keyWordFilter.append(" OR ").append("(((to_char(to_timestamp(creationDate/1000),'dd') || '/' || to_char(to_timestamp(creationDate/1000),'mm') || '/' || to_char(to_timestamp(creationDate/1000),'YYYY'))) like '%"+kantarBriefTemplateFilter.getKeyword()+"%')");
                // delivery date filter
                keyWordFilter.append(" OR ").append("(((to_char(to_timestamp(deliverydatetime/1000),'dd') || '/' || to_char(to_timestamp(deliverydatetime/1000),'mm') || '/' || to_char(to_timestamp(deliverydatetime/1000),'YYYY'))) like '%"+kantarBriefTemplateFilter.getKeyword()+"%')");
                // Status
                keyWordFilter.append(" OR ").append("(status = "+ KantarGlobals.KantarBriefTemplateStatusType.IN_PROGRESS.getId()+" AND lower('"+ KantarGlobals.KantarBriefTemplateStatusType.IN_PROGRESS.getName()+"') like '%"+kantarBriefTemplateFilter.getKeyword().toLowerCase()+"%')");
                keyWordFilter.append(" OR ").append("(status = "+ KantarGlobals.KantarBriefTemplateStatusType.COMPLETED.getId()+" AND lower('"+ KantarGlobals.KantarBriefTemplateStatusType.COMPLETED.getName()+"') like '%"+kantarBriefTemplateFilter.getKeyword().toLowerCase()+"%')");
                keyWordFilter.append(" OR ").append("(status = "+ KantarGlobals.KantarBriefTemplateStatusType.CANCELLED.getId()+" AND lower('"+ KantarGlobals.KantarBriefTemplateStatusType.CANCELLED.getName()+"') like '%"+kantarBriefTemplateFilter.getKeyword().toLowerCase()+"%')");
                conditions.add(keyWordFilter.toString());
            }

            // Owner filter
            if(kantarBriefTemplateFilter.getInitiators() != null && kantarBriefTemplateFilter.getInitiators().size() > 0) {
                conditions.add("(createdby in ("+StringUtils.join(kantarBriefTemplateFilter.getInitiators(),",")+"))");
            }

            if(kantarBriefTemplateFilter.getBatContacts() != null && kantarBriefTemplateFilter.getBatContacts().size() > 0) {
                conditions.add("(batcontact in ("+StringUtils.join(kantarBriefTemplateFilter.getBatContacts(),",")+"))");
            }

            if(kantarBriefTemplateFilter.getEndMarkets() != null && kantarBriefTemplateFilter.getEndMarkets().size() > 0) {
                conditions.add("(markets in ("+StringUtils.join(kantarBriefTemplateFilter.getEndMarkets(),",")+"))");
            }

            if(kantarBriefTemplateFilter.getMethodologies() != null && kantarBriefTemplateFilter.getMethodologies().size() > 0) {
                conditions.add("(methodologytype in ("+StringUtils.join(kantarBriefTemplateFilter.getMethodologies(),",")+"))");
            }

            if(kantarBriefTemplateFilter.getDeliveryDateFrom() != null || kantarBriefTemplateFilter.getDeliveryDateTo() != null) {
                if(kantarBriefTemplateFilter.getDeliveryDateFrom() != null && kantarBriefTemplateFilter.getDeliveryDateTo() != null) {
                    if(kantarBriefTemplateFilter.getDeliveryDateFrom().getTime() <= kantarBriefTemplateFilter.getDeliveryDateTo().getTime()) {
                        conditions.add("(deliverydatetime between " + kantarBriefTemplateFilter.getDeliveryDateFrom().getTime() + " AND " + kantarBriefTemplateFilter.getDeliveryDateTo().getTime()+")");
                    }
                } else if(kantarBriefTemplateFilter.getDeliveryDateFrom() != null) {
                    conditions.add("(deliverydatetime between " + kantarBriefTemplateFilter.getDeliveryDateFrom().getTime() + " AND (select max(deliverydatetime) from grailkantarbrieftemplate))");
                } else if(kantarBriefTemplateFilter.getDeliveryDateTo() != null) {
                    conditions.add("(deliverydatetime between (select min(deliverydatetime) from grailkantarbrieftemplate) AND " + kantarBriefTemplateFilter.getDeliveryDateTo().getTime()+")");
                }
            }
        }

        if(conditions.size() > 0) {
            filterBuilder.append(" WHERE (").append(StringUtils.join(conditions," AND ")).append(")");
        }
        return filterBuilder.toString();
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

    private static String toProjectId(String keyword) {
        String code = "";
        String id = keyword.replaceAll("^K0*","");
        if(!id.equals("")) {
            String pattern = "^(K0{4})([1-9][0-9]*)$";
            if(keyword.matches(pattern)) {
                code = keyword.replaceAll(pattern, "$2");
            } else {
                code = keyword;
            }
        } else {
            code = keyword;
        }
        return code;
    }

    @Override
    public KantarBriefTemplate getDraftTemplate(final Long userId) {
        KantarBriefTemplate bean = null;
        try{
            bean = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(GET_DRAFT_TEMPLATE,
                    kantarBriefTemplateParameterizedRowMapper, userId);
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

    private final ParameterizedRowMapper<KantarBriefTemplate> kantarBriefTemplateParameterizedRowMapper
            = new ParameterizedRowMapper<KantarBriefTemplate>() {
        public KantarBriefTemplate mapRow(final ResultSet rs, final int row) throws SQLException {
            KantarBriefTemplate kantarBriefTemplate = new KantarBriefTemplate();
            kantarBriefTemplate.setId(rs.getLong("id"));
            kantarBriefTemplate.setResearchNeedsPriorities(rs.getString("researchneedspriorities"));
            kantarBriefTemplate.setHypothesisBusinessNeed(rs.getString("hypothesesbusinessneeds"));
            kantarBriefTemplate.setMarkets(rs.getLong("markets"));
            kantarBriefTemplate.setProducts(rs.getString("products"));
            kantarBriefTemplate.setBrands(rs.getString("brands"));
            kantarBriefTemplate.setCategories(rs.getString("categories"));
            if(rs.getLong("deliverydatetime") > 0) {
                kantarBriefTemplate.setDeliveryDate(new Date(rs.getLong("deliverydatetime")));
            }
            if(rs.getInt("outputformat") > 0) {
                kantarBriefTemplate.setOutputFormat(rs.getInt("outputformat"));
            }
            kantarBriefTemplate.setCapturedDate(new Date(rs.getLong("captureddatetime")));
            kantarBriefTemplate.setRecipientEmail(rs.getString("recipientemail"));
            kantarBriefTemplate.setSender(rs.getLong("sender"));
            kantarBriefTemplate.setDraft(rs.getBoolean("isdraft"));
            kantarBriefTemplate.setMethodologyType(rs.getLong("methodologytype"));
            kantarBriefTemplate.setFinalCost(rs.getBigDecimal("finalcost"));
            kantarBriefTemplate.setFinalCostCurrency(rs.getLong("finalcostcurrency"));
            kantarBriefTemplate.setDataSource(rs.getString("datasource"));
            kantarBriefTemplate.setComments(rs.getString("comments"));
            kantarBriefTemplate.setBatContact(rs.getLong("batcontact"));
            kantarBriefTemplate.setStatus(rs.getInt("status"));
            kantarBriefTemplate.setCreatedBy(rs.getLong("createdby"));
            kantarBriefTemplate.setCreationDate(new Date(rs.getLong("creationdate")));
            kantarBriefTemplate.setModifiedBy(rs.getLong("modifiedby"));
            kantarBriefTemplate.setModificationDate(new Date(rs.getLong("modificationdate")));
            return kantarBriefTemplate;
        }
    };


    @Override
    public List<AttachmentBean> getKantarAttachments(final long objectId, final int objectType) {
        List<AttachmentBean> attachments = new ArrayList<AttachmentBean>();
        try{
            attachments = getSimpleJdbcTemplate().query(GET_ATTACHMENT_BY_OBJECT, attachmentRowMapper, objectType, objectId);
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return attachments;
    }


    @Override
    public List<AttachmentBean> getKantarAttachments(final KantarAttachment kantarAttachment) {
        List<AttachmentBean> attachments = new ArrayList<AttachmentBean>();
        try{
            attachments = getSimpleJdbcTemplate().query(GET_ATTACHMENT_BY_OBJECT, attachmentRowMapper, kantarAttachment.getObjectType(), kantarAttachment.getID());
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
    public List<KantarBriefTemplate> getPendingActivities(Long userId) {
        List<KantarBriefTemplate> kantarBriefTemplates = Collections.emptyList();
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT id,"+FIELDS+" FROM "+TABLE_NAME);
        sqlBuilder.append(" WHERE ").append("(batcontact = "+userId+" OR createdby = "+userId+") AND finalcost is null  AND status="+KantarGlobals.KantarBriefTemplateStatusType.IN_PROGRESS.getId()+"");
        try {
            kantarBriefTemplates = getSimpleJdbcTemplate().getJdbcOperations().query(sqlBuilder.toString(),
                    kantarBriefTemplateParameterizedRowMapper);
        } catch (DataAccessException e) {
            LOG.error(e.getMessage());
        }
        return kantarBriefTemplates;
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
