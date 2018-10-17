package com.grail.kantar.dao.impl;

import com.grail.kantar.beans.KantarReportBean;
import com.grail.kantar.beans.KantarReportResultFilter;
import com.grail.kantar.dao.KantarReportDAO;
import com.grail.kantar.object.KantarAttachment;
import com.grail.kantar.util.KantarGlobals;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.dao.util.SynchroDAOUtil;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroUserPropertiesUtil;
import com.grail.synchro.util.SynchroUtils;
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

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 12/30/14
 * Time: 12:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class KantarReportDAOImpl extends JiveJdbcDaoSupport implements KantarReportDAO {

    private final static Logger LOG = Logger.getLogger(KantarReportDAOImpl.class);

    private SynchroDAOUtil synchroDAOUtil;
    private static final String TABLE_NAME = "grailkantarreport";

    private static final String FIELDS = "reportname, market, reporttype, otherreporttype, " +
            "comments, creationby, modificationby, creationdate, modificationdate";

    private static final String INSERT_REPORT = "INSERT INTO " + TABLE_NAME + " (id, "+FIELDS+") VALUES (?,?,?,?,?,?,?,?,?,?)";

    private static final String UPDATE_REPORT = "UPDATE " + TABLE_NAME + " SET " + FIELDS.replaceAll(",", "=?,") + "=?" + " WHERE id = ?";

    private static final String GET_ALL  =  "SELECT id," + FIELDS + " FROM " + TABLE_NAME;

    private static final String GET_BY_ID =  GET_ALL + " WHERE id = ?";

    private static final String GET_BY_ID_USER = GET_ALL + " WHERE id = ? AND creationby = ?";

    private static final String GET_ALL_BY_USER = GET_ALL + " WHERE creationby = ?";

    private static final String GET_ATTACHMENT_BY_OBJECT = "SELECT * from jiveattachment where objecttype=? AND objectid=?";

    private static final String INSERT_ATTACHMENT_USER = "INSERT INTO grailattachmentuser(attachmentid, userid) VALUES (?, ?)";
    private static final String INSERT_ATTACHMENT_USER_BY_ATTACHMENTS = "INSERT INTO grailattachmentuser(attachmentid, userid) VALUES ";

    private static final String DELETE_ATTACHMENT_USER = "DELETE from grailattachmentuser where attachmentid=? ";
    private static final String DELETE_ATTACHMENT_USER_ATTACHMENTS = "DELETE from grailattachmentuser where attachmentid in ";

    private static final String GET_ATTACHMENT_USER = "select userid from grailattachmentuser where attachmentid=? ";


    @Override
    public Long save(final KantarReportBean kantarReportBean) {
        if(kantarReportBean != null) {
            try {
                Date dt = new Date();
                Long id = synchroDAOUtil.nextSequenceID("id", TABLE_NAME);
                kantarReportBean.setId(id);
                getSimpleJdbcTemplate().getJdbcOperations().update(INSERT_REPORT,
                        kantarReportBean.getId(),
                        kantarReportBean.getReportName(),
                        kantarReportBean.getCountry(),
                        kantarReportBean.getReportType(),
                        kantarReportBean.getOtherReportType(),
                        kantarReportBean.getComments(),
                        kantarReportBean.getCreatedBy() != null?kantarReportBean.getCreatedBy():SynchroPermHelper.getEffectiveUser().getID(),
                        kantarReportBean.getModifiedBy() != null?kantarReportBean.getModifiedBy():SynchroPermHelper.getEffectiveUser().getID(),
                        kantarReportBean.getCreationDate() != null?kantarReportBean.getCreationDate().getTime():dt.getTime(),
                        kantarReportBean.getModificationDate() != null?kantarReportBean.getModificationDate().getTime():dt.getTime()
                );
                return id;
            } catch (DataAccessException e) {
                LOG.error(e.getMessage());
            }
        }
        return null;
    }

    @Override
    public Long update(final KantarReportBean kantarReportBean) {
        if(kantarReportBean != null && kantarReportBean.getId() != null) {
            try {
                Date dt = new Date();
                getSimpleJdbcTemplate().getJdbcOperations().update(UPDATE_REPORT,
                        kantarReportBean.getReportName(),
                        kantarReportBean.getCountry(),
                        kantarReportBean.getReportType(),
                        kantarReportBean.getOtherReportType(),
                        kantarReportBean.getComments(),
                        kantarReportBean.getCreatedBy() != null?kantarReportBean.getCreatedBy():SynchroPermHelper.getEffectiveUser().getID(),
                        kantarReportBean.getModifiedBy() != null?kantarReportBean.getModifiedBy():SynchroPermHelper.getEffectiveUser().getID(),
                        kantarReportBean.getCreationDate() != null?kantarReportBean.getCreationDate().getTime():dt.getTime(),
                        kantarReportBean.getModificationDate() != null?kantarReportBean.getModificationDate().getTime():dt.getTime(),
                        kantarReportBean.getId()
                );
                return kantarReportBean.getId();
            } catch (DataAccessException e) {
                LOG.error(e.getMessage());
            }
        }
        return null;
    }

    @Override
    public KantarReportBean get(final Long id) {
        KantarReportBean bean = null;
        try {
            bean = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(GET_BY_ID,
                    kantarReportBeanParameterizedRowMapper, id);
        } catch (DataAccessException e) {
            LOG.error(e.getMessage());
        }
        return bean;
    }

    @Override
    public KantarReportBean get(final Long id, final Long userId) {
        KantarReportBean bean = null;
        try {
            bean = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(GET_BY_ID_USER,
                    kantarReportBeanParameterizedRowMapper, id, userId);
        } catch (DataAccessException e) {
            LOG.error(e.getMessage());
        }
        return bean;
    }

    @Override
    public List<KantarReportBean> getAll(final Long userId) {
        List<KantarReportBean> list = Collections.emptyList();
        try {
            list = getSimpleJdbcTemplate().getJdbcOperations().query(GET_ALL_BY_USER,
                    kantarReportBeanParameterizedRowMapper, userId);
        } catch (DataAccessException e) {
            LOG.error(e.getMessage());
        }
        return list;
    }

    @Override
    public List<KantarReportBean> getAll() {
        List<KantarReportBean> list = Collections.emptyList();
        try {
            list = getSimpleJdbcTemplate().getJdbcOperations().query(GET_ALL,
                    kantarReportBeanParameterizedRowMapper);
        } catch (DataAccessException e) {
            LOG.error(e.getMessage());
        }
        return list;
    }

    @Override
    public List<KantarReportBean> getAll(final KantarReportResultFilter kantarReportResultFilter) {
        return getAll(kantarReportResultFilter, null);
    }

    @Override
    public List<KantarReportBean> getAll(final KantarReportResultFilter kantarReportResultFilter, final User owner) {
        List<KantarReportBean> beans = Collections.emptyList();
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT ");
        sqlBuilder.append("id,").append(FIELDS);
        sqlBuilder.append(", (select (ju.firstname || ' ' || ju.lastname) from jiveuser ju where ju.userid = creationby) as authorName");
        sqlBuilder.append(", (select em.name from grailendmarketfields em where em.id = market) as marketName");
        sqlBuilder.append(", (CASE");
        for(KantarGlobals.KantarReportType reportType : KantarGlobals.KantarReportType.values()) {
            if(reportType.getId().equals(-100)) {
                sqlBuilder.append(" WHEN reporttype = " + reportType.getId() + " THEN otherreporttype");
            } else {
                sqlBuilder.append(" WHEN reporttype = " + reportType.getId() + " THEN '" + reportType.getName() + "'");
            }
        }
        sqlBuilder.append(" ELSE '' END) as reportTypeName");
        sqlBuilder.append(" FROM ").append(TABLE_NAME);
        sqlBuilder.append(applyFilter(kantarReportResultFilter, owner));
        if(kantarReportResultFilter != null && kantarReportResultFilter.getSortField() != null) {
            sqlBuilder.append(getOrderByField(kantarReportResultFilter.getSortField(), kantarReportResultFilter.getAscendingOrder()));
        } else  {
            sqlBuilder.append(" ORDER BY id");
        }
        if(kantarReportResultFilter != null) {
            if(kantarReportResultFilter.getStart() != null) {
                sqlBuilder.append(" OFFSET ").append(kantarReportResultFilter.getStart());
            }
            if(kantarReportResultFilter.getLimit() != null && kantarReportResultFilter.getLimit() > 0) {
                sqlBuilder.append(" LIMIT ").append(kantarReportResultFilter.getLimit());
            }
        }
        try {
            beans = getSimpleJdbcTemplate().getJdbcOperations().query(sqlBuilder.toString(), kantarReportBeanParameterizedRowMapper);
        } catch (DataAccessException e) {
            LOG.error(e.getMessage());
        }
        return beans;
    }

    private String getOrderByField(final String sortField, final Integer order) {
        if(StringUtils.isNotBlank(sortField)) {
            StringBuilder orderBy = new StringBuilder();
            String field = null;
            if(sortField.equals("name")) {
                field = "reportName";
            } else if(sortField.equals("author")) {
                field = "authorName";
            } else if(sortField.equals("country")) {
                field = "marketName";
            } else if(sortField.equals("reportType")) {
                field = "reportTypeName";
            } else if(sortField.equals("reportTypeUploadDate")) {
                field = "modificationdate";
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

    @Override
    public Integer getTotalCount(final KantarReportResultFilter kantarReportResultFilter) {
        return getTotalCount(kantarReportResultFilter, null);
    }

    @Override
    public Integer getTotalCount(final KantarReportResultFilter kantarReportResultFilter, final User owner) {
        Integer count = 0;
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT count(*) FROM ").append(TABLE_NAME);
        sqlBuilder.append(applyFilter(kantarReportResultFilter, owner));
        try {
            count = getSimpleJdbcTemplate().getJdbcOperations().queryForInt(sqlBuilder.toString());
        } catch (DataAccessException e) {
            LOG.error(e.getMessage());
        }
        return count;
    }

    private String applyFilter(final KantarReportResultFilter kantarReportResultFilter, final User owner) {
        User user = SynchroPermHelper.getEffectiveUser();
        StringBuilder filterBuilder = new StringBuilder();
        List<String> conditions = new ArrayList<String>();
        // Flag for 'Australia', 'Canada', 'New Zealand'
        boolean hasAccessToAustraliaEndmarket = false;
        boolean hasAccessToNewzealandEndmarket = false;
        boolean hasAccessToCanadaEndmarket = false;
        if(!(SynchroPermHelper.isSynchroAdmin(user) || SynchroPermHelper.isSynchroMiniAdmin()
                || SynchroPermHelper.isDocumentRepositoryBATUser(user) || SynchroPermHelper.isDocumentRepositoryAgencyUser(user))) {
            List<String> accessConditions = new ArrayList<String>();
            accessConditions.add("creationby = "+user.getID());
//            accessConditions.add("modificationby = "+user.getID());
            Map<String, String> userProperties = user.getProperties();
            Set<Integer> endmarkets = new HashSet<Integer>();
            String restrictedEndmarketsCondition = null;
            String endmarketAccessCountryName = null;
            if(userProperties != null && !userProperties.isEmpty()) {
                if(userProperties.containsKey(SynchroUserPropertiesUtil.COUNTRY)
                        && userProperties.get(SynchroUserPropertiesUtil.COUNTRY) != null
                        && !userProperties.get(SynchroUserPropertiesUtil.COUNTRY).equals("")) {
                    try {
                        Integer country = -1;
                        String countryName = userProperties.get(SynchroUserPropertiesUtil.COUNTRY);
                        Map<Integer, String> countries = SynchroGlobal.getEndMarkets();
                        String decodedCountryName = SynchroUtils.getDecodedString(countryName);
                        for(Integer id : countries.keySet()) {
                            if(countries.get(id).equalsIgnoreCase(decodedCountryName)) {
                                country = id;
                            }
                        }
                        endmarkets.add(country);
                        endmarketAccessCountryName = decodedCountryName;
                    } catch (Exception e) {
                        LOG.error("User country is non-numeric");
                    }
                }
                restrictedEndmarketsCondition = getRestrictedEndmarketsCondition(user, endmarketAccessCountryName);

                if(SynchroPermHelper.isSynchroGlobalSuperUser(user) || SynchroPermHelper.isSynchroRegionalSuperUser(user)) {
                    if(restrictedEndmarketsCondition != null && !restrictedEndmarketsCondition.equals("")) {
                        accessConditions.add(restrictedEndmarketsCondition);
                    }
                } else {
//                    if(SynchroPermHelper.isSynchroRegionalSuperUser(user)) {
//                        if(userProperties.containsKey(SynchroUserPropertiesUtil.GRAIL_REGIONAL_ACCESS_LIST)
//                                && userProperties.get(SynchroUserPropertiesUtil.GRAIL_REGIONAL_ACCESS_LIST) != null
//                                && !userProperties.get(SynchroUserPropertiesUtil.GRAIL_REGIONAL_ACCESS_LIST).equals("")) {
//
//                            String [] regions = userProperties.get(SynchroUserPropertiesUtil.GRAIL_REGIONAL_ACCESS_LIST).split(",");
//                            for(String region: regions) {
//                                Map<Integer, String> ems = SynchroGlobal.getEndMarketsByRegion(Long.parseLong(region));
//                                if(!ems.isEmpty()) {
//                                    endmarkets.addAll(ems.keySet());
//                                }
//                            }
//
//                        }
//                    } else
                    if(SynchroPermHelper.isSynchroAreaSuperUser(user)) {
                        if(userProperties.containsKey(SynchroUserPropertiesUtil.GRAIL_AREA_ACCESS_LIST)
                                && userProperties.get(SynchroUserPropertiesUtil.GRAIL_AREA_ACCESS_LIST) != null
                                && !userProperties.get(SynchroUserPropertiesUtil.GRAIL_AREA_ACCESS_LIST).equals("")) {
                            String [] areas = userProperties.get(SynchroUserPropertiesUtil.GRAIL_AREA_ACCESS_LIST).split(",");
                            for(String area: areas) {
                                Map<Integer, String> ems = SynchroGlobal.getEndMarketsByArea(Long.parseLong(area));
                                if(!ems.isEmpty()) {
                                    endmarkets.addAll(ems.keySet());
                                }
                            }
                        }
                    } else if(SynchroPermHelper.isSynchroEndmarketSuperUser(user)) {
                        if(userProperties.containsKey(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_LIST)
                                && userProperties.get(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_LIST) != null
                                && !userProperties.get(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_LIST).equals("")) {
                            String [] ems = userProperties.get(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_LIST).split(",");
                            for(String em: ems) {
                                endmarkets.add(Integer.parseInt(em));
                            }
                        }
                    }
                }

            }
            if(endmarkets.size() > 0) {
                String marketCondition = "(market in ("+StringUtils.join(endmarkets,",")+",-100)";
                if(!SynchroPermHelper.isSynchroEndmarketSuperUser(user)) {
                    if(restrictedEndmarketsCondition != null && !restrictedEndmarketsCondition.equals("")) {
                        marketCondition += " AND " + restrictedEndmarketsCondition;
                    }
                }
                marketCondition += ")";
                accessConditions.add(marketCondition);
            } else {
                accessConditions.add("(market in (-100))");
            }

            if(accessConditions.size() > 0) {
                conditions.add("(" + StringUtils.join(accessConditions, " OR ") + ")");
            }
        }

        if(kantarReportResultFilter != null) {
            // Keyword filter
            if(kantarReportResultFilter.getKeyword() != null && !kantarReportResultFilter.getKeyword().equals("")) {
                StringBuilder keyWordFilter = new StringBuilder();
                // report name filter
                keyWordFilter.append("(lower(reportname) like '%"+kantarReportResultFilter.getKeyword().toLowerCase()+"%')");
                // Author filter
                keyWordFilter.append(" OR ").append("((SELECT count(*) FROM jiveuser ju WHERE ju.userid = creationby AND lower(ju.firstname || ' ' || ju.lastname) like '%"+kantarReportResultFilter.getKeyword().toLowerCase()+"%') > 0)");
                // market filter
                keyWordFilter.append(" OR ").append("((SELECT lower(em.name) FROM grailendmarketfields em WHERE em.id = market) like '%"+kantarReportResultFilter.getKeyword().toLowerCase()+"%')");
                for(KantarGlobals.KantarReportType reportType : KantarGlobals.KantarReportType.values()) {
                    if(reportType.getId().equals(-100)) {
                        keyWordFilter.append(" OR ").append("(reporttype = "+ reportType.getId() +" AND lower(otherreporttype) like '%"+kantarReportResultFilter.getKeyword().toLowerCase()+"%')");
                    } else {
                        keyWordFilter.append(" OR ").append("(reporttype = "+ reportType.getId() +" AND lower('"+ reportType.getName() +"') like '%"+kantarReportResultFilter.getKeyword().toLowerCase()+"%')");
                    }
                }
                keyWordFilter.append(" OR ").append("((to_char(to_timestamp(modificationdate/1000),'dd')::int || '/' || to_char(to_timestamp(modificationdate/1000),'mm')::int || '/' || to_char(to_timestamp(modificationdate/1000),'YYYY')::int) like '%"+kantarReportResultFilter.getKeyword()+"%')");
                conditions.add(keyWordFilter.toString());
            }

            // Report name
            if(kantarReportResultFilter.getReportName() != null && !kantarReportResultFilter.getReportName().equals("")) {
                conditions.add("(lower(reportname) like '%"+kantarReportResultFilter.getReportName().toLowerCase()+"%')");
            }

            // Author filter
            if(kantarReportResultFilter.getAuthors() != null && kantarReportResultFilter.getAuthors().size() > 0) {
                conditions.add("(creationby in ("+StringUtils.join(kantarReportResultFilter.getAuthors(),",")+"))");
            }

            // Country filter
            if(kantarReportResultFilter.getEndMarkets() != null && kantarReportResultFilter.getEndMarkets().size() > 0) {
                conditions.add("(market in ("+StringUtils.join(kantarReportResultFilter.getEndMarkets(),",")+"))");
            }

            // Report type filter
            if(kantarReportResultFilter.getReportTypes() != null && kantarReportResultFilter.getReportTypes().size() > 0) {
                conditions.add("(reporttype in ("+StringUtils.join(kantarReportResultFilter.getReportTypes(),",")+"))");
            }

            if(kantarReportResultFilter.isOtherType()) {
                conditions.add("((SELECT count(*) FROM grailkantarreporttype WHERE othertype=1 and id=reporttype) > 0)");
            }
        }

        if(conditions.size() > 0) {
            filterBuilder.append(" WHERE (").append(StringUtils.join(conditions, " AND ")).append(")");
        }
        return filterBuilder.toString();
    }

    private String getRestrictedEndmarketsCondition(User user,String countryName) {
        String endmarketRestrictCondition = null;
        List<String> endmarketRestrictList = new ArrayList<String>();

        if(countryName == null || countryName.equals("") || !countryName.equalsIgnoreCase("australia")) {
            endmarketRestrictList.add("australia");
        }

        if(countryName == null || countryName.equals("") || !countryName.equalsIgnoreCase("new zealand")) {
            endmarketRestrictList.add("new zealand");
        }

        if(countryName == null || countryName.equals("") || !countryName.equalsIgnoreCase("canada")) {
            endmarketRestrictList.add("canada");
        }
        if(endmarketRestrictList.size() > 0) {
            endmarketRestrictCondition = "market not in (select id from grailendmarketfields where lower(name) in ('"+StringUtils.join(endmarketRestrictList,"','")+"'))";
        }
        return endmarketRestrictCondition;
    }



    private final ParameterizedRowMapper<KantarReportBean> kantarReportBeanParameterizedRowMapper
            = new ParameterizedRowMapper<KantarReportBean>() {
        public KantarReportBean mapRow(final ResultSet rs, final int row) throws SQLException {
            KantarReportBean kantarReportBean = new KantarReportBean();
            kantarReportBean.setId(rs.getLong("id"));
            kantarReportBean.setReportName(rs.getString("reportname"));
            kantarReportBean.setCountry(rs.getLong("market"));
            kantarReportBean.setReportType(rs.getInt("reporttype"));
            kantarReportBean.setOtherReportType(rs.getString("otherreporttype"));
            kantarReportBean.setComments(rs.getString("comments"));
            kantarReportBean.setCreatedBy(rs.getLong("creationby"));
            kantarReportBean.setModifiedBy(rs.getLong("modificationby"));
            kantarReportBean.setCreationDate(new Date(rs.getLong("creationdate")));
            kantarReportBean.setModificationDate(new Date(rs.getLong("modificationdate")));
            return kantarReportBean;
        }
    };

    @Override
    public List<AttachmentBean> getKantarReportAttachments(long objectId, int objectType) {
        List<AttachmentBean> attachments = new ArrayList<AttachmentBean>();
        try{
            attachments = getSimpleJdbcTemplate().query(GET_ATTACHMENT_BY_OBJECT, attachmentRowMapper, objectType, objectId);
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return attachments;
    }

    @Override
    public List<AttachmentBean> getKantarReportAttachments(KantarAttachment kantarAttachment) {
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

    @Override
    public List<Long> getAuthors() {
        List<Long> authors = new ArrayList<Long>();
        try {
            String sql = "SELECT creationby FROM " + TABLE_NAME + " group by creationby";
            authors = getSimpleJdbcTemplate().getJdbcOperations().queryForList(sql, Long.class);
        } catch (DataAccessException e) {

        }
        return authors;
    }

    public SynchroDAOUtil getSynchroDAOUtil() {
        return synchroDAOUtil;
    }

    public void setSynchroDAOUtil(SynchroDAOUtil synchroDAOUtil) {
        this.synchroDAOUtil = synchroDAOUtil;
    }
}
