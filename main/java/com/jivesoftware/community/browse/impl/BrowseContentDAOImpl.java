/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.browse.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jivesoftware.base.database.LongRowMapper;
import com.jivesoftware.base.database.dao.DAOException;
import com.jivesoftware.base.database.dao.JiveJdbcDaoSupport;
import com.jivesoftware.base.database.dao.JiveJdbcOperationsTemplate;
import com.jivesoftware.base.database.dao.SequenceDAO;
import com.jivesoftware.community.ContainerAwareEntityDescriptor;
import com.jivesoftware.community.EntityDescriptor;
import com.jivesoftware.community.JiveConstants;
import com.jivesoftware.community.browse.BrowseContentBean;
import com.jivesoftware.community.browse.BrowseContentDAO;
import com.jivesoftware.community.browse.BrowseManager;
import com.jivesoftware.community.browse.BrowseUserEntityRelBean;
import com.jivesoftware.community.browse.QueryFilterDef;
import com.jivesoftware.community.browse.TableJoin;
import com.jivesoftware.community.browse.filter.BrowseFilter;
import com.jivesoftware.community.browse.sort.BrowseSort;
import com.jivesoftware.community.browse.sort.BrowseSort.SortOrder;
import com.jivesoftware.community.browse.sort.ComputedResultSort;
import com.jivesoftware.community.browse.sort.MultipleColumnBrowseSort;
import com.jivesoftware.community.impl.CachedPreparedStatement;
import com.jivesoftware.community.user.relationships.UserRelationship;
import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.jivesoftware.community.browse.filter.MostRatedFilter;

@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public class BrowseContentDAOImpl extends JiveJdbcDaoSupport implements BrowseContentDAO {

    private SequenceDAO sequence;

    private static final long GRAPH_ID = 1001; // the graph id for friend/follow relationships

    public void setSequenceDAO(SequenceDAO sequence) {
        this.sequence = sequence;
    }

    private static final String CREATE_SQL = "INSERT INTO jiveBrowseCnt (browseID, objectType, objectID, authorID, subject, status, "
            + "containerType, containerID, creationDate, modificationDate, lastActivityDate) "
            + "VALUES (:browseID, :objectType, :ID, :authorID, :subject, :statusCode, :containerType, :containerID, "
            + ":creationDateMillis, :modificationDateMillis, :lastActivityDateMillis)";

    private static final String DELETE_SQL = "DELETE FROM jiveBrowseCnt WHERE browseID = :id";

    private static final String UPDATE_SQL = "UPDATE jiveBrowseCnt SET "
            + "authorID = :authorID, subject = :subject, status = :statusCode, "
            + "containerType = :containerType, containerID = :containerID, creationDate = :creationDateMillis, "
            + "modificationDate = :modificationDateMillis, lastActivityDate = :lastActivityDateMillis "
            + "WHERE browseID = :browseID";

    private static final String UPDATE_LAST_ACTIVITY_SQL = "UPDATE jiveBrowseCnt SET "
            + "lastActivityDate = :lastActivityDateMillis " + "WHERE browseID = :browseID";

    private static final String GET_ID_BY_OBJECT_TYPE_AND_ID_SQL = "SELECT browseID FROM jiveBrowseCnt WHERE objectType = :type AND objectID = :id";

    private static final String GET_OBJECT_TYPE_AND_ID_BY_ID_SQL = "SELECT objectType, objectID FROM jiveBrowseCnt WHERE browseID = :browseID";

    private static final String GET_CONTAINER_TYPE_AND_ID_BY_IDS_SQL = "SELECT browseID, objectType, objectID, containerType, containerID FROM jiveBrowseCntr WHERE browseID IN (:browseIds)";

    private static final String GET_BULK_CONTAINER_ID_BY_OBJECT_TYPE_AND_ID_SQL = "SELECT objectID, browseID FROM jiveBrowseCntr WHERE objectType = :type AND objectID IN (:ids)";

    private static final String GET_BULK_CONTENT_ID_BY_OBJECT_TYPE_AND_ID_SQL = "SELECT objectID, browseID FROM jiveBrowseCnt WHERE objectType = :type AND objectID IN (:ids)";

    private static final String GET_LAST_ACTIVITY_BY_OBJECT_TYPE_AND_ID_SQL = "SELECT lastActivityDate FROM jiveBrowseCnt WHERE objectType = :type AND objectID = :id";

    private static final String GET_BULK_LAST_ACTIVITY_BY_OBJECT_TYPE_AND_ID_SQL = "SELECT objectID, lastActivityDate FROM jiveBrowseCnt WHERE objectType = :type AND objectID IN(:ids)";

    private static final String SET_PARTICIPATED_SQL = "INSERT INTO jiveBrowsePrtd (browseID, userID) (SELECT :id,:userID FROM jiveIdentityTable WHERE NOT EXISTS "
            + "(SELECT browseID FROM jiveBrowsePrtd b WHERE b.browseID = :id AND b.userID = :userID) )";

    private static final String DELETE_ALL_PARTICIPATED_SQL = "DELETE FROM jiveBrowsePrtd WHERE browseID = :id";

    private static final String SET_FOLLOW_CONTENT_SQL = "INSERT INTO jiveBrowseFol (browseID, userID) (SELECT :id,:userID FROM jiveIdentityTable WHERE NOT EXISTS "
            + "(SELECT browseID FROM jiveBrowseFol b WHERE b.browseID = :id AND b.userID = :userID) )";

    private static final String DELETE_FOLLOW_CONTENT_SQL = "DELETE FROM jiveBrowseFol WHERE browseID = :id AND userID = :userID";

    private static final String DELETE_ALL_FOLLOW_CONTENT_SQL = "DELETE FROM jiveBrowseFol WHERE browseID = :id";

    private static final String GET_FOLLOWER_COUNT_SQL = "SELECT COUNT(f.userID) FROM jiveBrowseFol f JOIN jiveUser u " +
            "ON u.userID = f.userID WHERE f.browseID = :browseID AND u.userEnabled = 1";

    private static final String GET_TOP_FOLLOWED_CONTAINERS = "SELECT f.browseID FROM jiveBrowseFol f WHERE EXISTS "
            + "(SELECT 1 FROM jiveBrowseCntr browsecontainer WHERE browsecontainer.browseID = f.browseID) "
            + "GROUP BY f.browseID ORDER BY COUNT(f.browseID) DESC";

    private static final String GET_TOP_FOLLOWED_USERS = "SELECT rel.relatedUserID FROM jiveUserRel rel "
            + "WHERE rel.graphID=? AND rel.state=? AND rel.retirementDate=0 "
            + "GROUP BY rel.relatedUserID ORDER BY COUNT(rel.relatedUserID) DESC";

    private static final String GET_BULK_FOLLOWER_COUNT_SQL =
            "SELECT f.browseID, COUNT(f.userID) as countValue FROM jiveBrowseFol f JOIN jiveUser u ON u.userID = f.userID " +
                    "WHERE u.userEnabled = 1 AND f.browseID in (:browseIDs) GROUP BY f.browseID";

    private static final String GET_FOLLOWING_COUNT_USER_SQL = "SELECT COUNT DISTINCT(relatedUserID) FROM jiveUserRel rel " +
            "JOIN jiveUser u ON u.userID = rel.userID WHERE u.userEnabled = 1 AND rel.userID = :userID " +
            "AND rel.graphID=:graphID AND rel.state = :state AND rel.retirementDate = 0";

    private static final String GET_PARTICIPANTS_SQL = "SELECT userID FROM jiveBrowsePrtd WHERE browseID = :browseID";

    private static final String GET_SYNC_CONTENT_BY_OBJECT_TYPE_AND_ID_RANGE = "SELECT browseID, objectType, objectID, modificationDate "
            + "FROM jiveBrowseCnt WHERE objectType = :objectType AND objectID >= :startID AND objectID <= :endID";

    private static final String GET_SYNC_PARTICIPATED_BY_OBJECT_TYPE_AND_ID_RANGE = "SELECT c.objectType, c.objectID, p.userID "
            + "FROM jiveBrowseCnt c INNER JOIN jiveBrowsePrtd p ON c.browseID = p.browseID "
            + "WHERE c.objectType = :objectType AND c.objectID >= :startID AND c.objectID <= :endID";

    private static final String GET_SYNC_FOLLOWED_BY_OBJECT_TYPE_AND_ID_RANGE = "SELECT c.objectType, c.objectID, f.userID "
            + "FROM jiveBrowseCnt c INNER JOIN jiveBrowseFol f ON c.browseID = f.browseID "
            + "WHERE c.objectType = :objectType AND c.objectID >= :startID AND c.objectID <= :endID";

    private static final String GET_MIN_ID_BY_OBJECT_TYPE = "SELECT MIN(objectID) FROM jiveBrowseCnt WHERE objectType = :objectType GROUP BY objectType";

    private static final String GET_MAX_ID_BY_OBJECT_TYPE = "SELECT MAX(objectID) FROM jiveBrowseCnt WHERE objectType = :objectType GROUP BY objectType";

    private static final String DISTINCT_OBJECT_TYPES = "SELECT DISTINCT objectType FROM jiveBrowseCnt";

    // these queries are only used by tests

    private static final String GET_BEAN_BY_OBJECT_TYPE_AND_ID_SQL = "SELECT * FROM jiveBrowseCnt WHERE objectType = :type AND objectID = :id";

    private static final String GET_BEAN_BY_PARTICIPATED_USER_SQL = "SELECT * FROM jiveBrowseCnt b WHERE b.browseID IN "
            + "(SELECT browseID FROM jiveBrowsePrtd p WHERE p.userID = :userId)";

    private static final String GET_BEAN_BY_FOLLOWED_USER_SQL = "SELECT * FROM jiveBrowseCnt b WHERE b.browseID IN "
            + "(SELECT browseID FROM jiveBrowseFol f WHERE f.userID = :userId)";

    // ///////////////////////////////////////

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public BrowseContentBean create(BrowseContentBean bean) {
        long id = sequence.nextID(BrowseManager.OBJECTID);
        bean.setBrowseID(id);
        template().update(CREATE_SQL, source(bean));
        return bean;
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void update(BrowseContentBean bean) {
        if (template().update(UPDATE_SQL, source(bean)) != 1) {
            throw new DAOException("Update failed, given object does not exist in database");
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateLastActivityDate(BrowseContentBean bean) {
        if (template().update(UPDATE_LAST_ACTIVITY_SQL, source(bean)) != 1) {
            throw new DAOException("Update failed, given object does not exist in database");
        }
    }

    public Long getBrowseID(int objectType, long objectID) {
        try {
            return template().queryForLong(GET_ID_BY_OBJECT_TYPE_AND_ID_SQL,
                    new MapSqlParameterSource(ImmutableMap.of("type", objectType, "id", objectID)));
        }
        catch (EmptyResultDataAccessException erdae) {
            return null;
        }
    }

    @Override
    public Map<Long, Long> getBrowseIDs(boolean container, int objectType, Collection<Long> objectIds) {
        try {
            String sql = container ? GET_BULK_CONTAINER_ID_BY_OBJECT_TYPE_AND_ID_SQL
                    : GET_BULK_CONTENT_ID_BY_OBJECT_TYPE_AND_ID_SQL;
            List<BrowseTuple> tuples = template().query(sql, new BrowseTupleRowMapper(),
                    new MapSqlParameterSource(ImmutableMap.of("type", objectType, "ids", objectIds)));
            Map<Long, Long> results = Maps.newHashMap();
            for (BrowseTuple tuple : tuples) {
                results.put(tuple.objectID, tuple.browseID);
            }
            return results;
        }
        catch (EmptyResultDataAccessException erdae) {
            return Maps.newHashMap();
        }
    }

    public EntityDescriptor getBrowseObject(long browseID) {
        try {
            List<BrowseEntity> entities = template().query(GET_OBJECT_TYPE_AND_ID_BY_ID_SQL, new BrowseEntityRowMapper(),
                    new MapSqlParameterSource(ImmutableMap.of("browseID", browseID)));
            for (BrowseEntity entity : entities) {
                return new EntityDescriptor(entity.objectType, entity.objectID);
            }
            return null;
        }
        catch (EmptyResultDataAccessException erdae) {
            return null;
        }
    }

    public Map<Long, ContainerAwareEntityDescriptor> getBrowseContainerObjects(Collection<Long> browseIds) {
        try {
            List<BrowseIdContainerEntity> entities = template().query(GET_CONTAINER_TYPE_AND_ID_BY_IDS_SQL,
                    new BrowseIdContainerEntityRowMapper(), new MapSqlParameterSource(ImmutableMap.of("browseIds", browseIds)));

            Map<Long, ContainerAwareEntityDescriptor> browseObjects = Maps.newHashMapWithExpectedSize(browseIds.size());
            for (BrowseIdContainerEntity entity : entities) {
                browseObjects.put(entity.browseID, new ContainerAwareEntityDescriptor(entity.objectType, entity.objectID,
                        entity.containerID, entity.containerType));
            }
            return browseObjects;
        }
        catch (EmptyResultDataAccessException erdae) {
            return null;
        }
    }


    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void setParticipated(long browseID, long userID) {
        try {
            setParticipatedNested(browseID, userID);
        }
        catch (DuplicateKeyException e) {
            logger.info("Participated already set for user and browse ID. Ignoring duplicate participant.");

            if (logger.isDebugEnabled()) {
                logger.debug(e.getMessage());
            }
        }
    }

    /**
     * Do not expose this method! It exists only to provide a nested transaction to {@link #setParticipated(long, long)}.
     */
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    //public, otherwise @Transactional is ignored
    public void setParticipatedNested(long browseID, long userID) {
        template().update(SET_PARTICIPATED_SQL,
                new MapSqlParameterSource(ImmutableMap.of("id", browseID, "userID", userID)));
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public boolean setFollow(long browseID, long userID) {
        try {
            return setFollowNested(browseID, userID);
        }
        catch (DuplicateKeyException e) {
            logger.info("Follow already set for user and browse ID. Ignoring duplicate follow.");

            if (logger.isDebugEnabled()) {
                logger.debug(e.getMessage());
            }

            return false;
        }
    }

    /**
     * Do not expose this method! It exists only to provide a nested transaction to {@link #setFollow(long, long)}.
     */
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    //public, otherwise @Transactional is ignored
    public boolean setFollowNested(long browseID, long userID) {
        int updated = template().update(SET_FOLLOW_CONTENT_SQL,
                new MapSqlParameterSource(ImmutableMap.of("id", browseID, "userID", userID)));
        return updated > 0;
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void delete(long browseID) {
        template().update(DELETE_SQL, new MapSqlParameterSource(ImmutableMap.of("id", browseID)));
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void deleteAllParticipated(long browseID) {
        template().update(DELETE_ALL_PARTICIPATED_SQL, new MapSqlParameterSource(ImmutableMap.of("id", browseID)));
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public boolean deleteFollow(long browseID, long userID) {
        int updated = template().update(DELETE_FOLLOW_CONTENT_SQL,
                new MapSqlParameterSource(ImmutableMap.of("id", browseID, "userID", userID)));
        return updated > 0;
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void deleteAllFollow(long browseID) {
        template().update(DELETE_ALL_FOLLOW_CONTENT_SQL, new MapSqlParameterSource(ImmutableMap.of("id", browseID)));
    }

    public List<BrowseContentBean> getSyncContentBeans(int objectType, long startID, long endID) {
        try {
            return template().query(
                    GET_SYNC_CONTENT_BY_OBJECT_TYPE_AND_ID_RANGE,
                    new BrowseSyncContentBeanRowMapper(),
                    new MapSqlParameterSource(ImmutableMap.of("objectType", objectType, "startID", startID, "endID",
                            endID)));
        }
        catch (EmptyResultDataAccessException erdae) {
            return null;
        }
    }

    public List<BrowseUserEntityRelBean> getSyncParticipatedBeans(int objectType, long startID, long endID) {
        try {
            return template().query(
                    GET_SYNC_PARTICIPATED_BY_OBJECT_TYPE_AND_ID_RANGE,
                    new BrowseSyncUserEntityRelBeanRowMapper(),
                    new MapSqlParameterSource(ImmutableMap.of("objectType", objectType, "startID", startID, "endID",
                            endID)));
        }
        catch (EmptyResultDataAccessException erdae) {
            return null;
        }
    }

    public List<BrowseUserEntityRelBean> getSyncFollowedBeans(int objectType, long startID, long endID) {
        try {
            return template().query(
                    GET_SYNC_FOLLOWED_BY_OBJECT_TYPE_AND_ID_RANGE,
                    new BrowseSyncUserEntityRelBeanRowMapper(),
                    new MapSqlParameterSource(ImmutableMap.of("objectType", objectType, "startID", startID, "endID",
                            endID)));
        }
        catch (EmptyResultDataAccessException erdae) {
            return null;
        }
    }

    public long getMinObjectID(int objectType) {
        try {
            return template().queryForLong(GET_MIN_ID_BY_OBJECT_TYPE,
                    new MapSqlParameterSource(ImmutableMap.of("objectType", objectType)));
        }
        catch (EmptyResultDataAccessException erdae) {
            return 0;
        }
    }

    public long getMaxObjectID(int objectType) {
        try {
            return template().queryForLong(GET_MAX_ID_BY_OBJECT_TYPE,
                    new MapSqlParameterSource(ImmutableMap.of("objectType", objectType)));
        }
        catch (EmptyResultDataAccessException erdae) {
            return 0;
        }
    }

    @Override
    public List<Integer> getDistinctObjectTypes() {
        try {
            return template().query(DISTINCT_OBJECT_TYPES, new RowMapper<Integer>() {
                public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return rs.getInt("objectType");
                }
            });
        }
        catch (EmptyResultDataAccessException erdae) {
            return Lists.newLinkedList();
        }
    }

    @Override
    public Long getLastActivityDate(int objectType, long objectID) {
        try {
            return template().queryForLong(GET_LAST_ACTIVITY_BY_OBJECT_TYPE_AND_ID_SQL,
                    new MapSqlParameterSource(ImmutableMap.of("type", objectType, "id", objectID)));
        }
        catch (EmptyResultDataAccessException erdae) {
            return null;
        }
    }

    @Override
    public Map<Long, Long> getLatestActivityDates(boolean isContainer, int objectType, Collection<Long> ids) {
        if (ids.size() == 0) {
            return Maps.newHashMap();
        }
        Map<Long, Long> results = Maps.newHashMap();
        List<LatestActivity> activities;
        try {
            activities = template().query(GET_BULK_LAST_ACTIVITY_BY_OBJECT_TYPE_AND_ID_SQL, new LatestActivityRowMapper(),
                    new MapSqlParameterSource(ImmutableMap.of("type", objectType, "ids", ids)));
        } catch (EmptyResultDataAccessException erdae) {
            return results;
        }
        for (LatestActivity activity : activities) {
            results.put(activity.objectID, activity.activityTime);

        }
        return results;
    }

    // for testing purposes only
    public BrowseContentBean getBrowseContentBean(int objectType, long objectID) {
        try {
            return template().queryForObject(GET_BEAN_BY_OBJECT_TYPE_AND_ID_SQL, new BrowseContentBeanRowMapper(),
                    new MapSqlParameterSource(ImmutableMap.of("type", objectType, "id", objectID)));
        }
        catch (EmptyResultDataAccessException erdae) {
            return null;
        }
    }

    // for testing purposes only
    public List<BrowseContentBean> getBrowseContentBeansPartipatedByUser(long userID) {
        try {
            return template().query(GET_BEAN_BY_PARTICIPATED_USER_SQL, new BrowseContentBeanRowMapper(),
                    new MapSqlParameterSource(ImmutableMap.of("userId", userID)));
        }
        catch (EmptyResultDataAccessException erdae) {
            return null;
        }
    }

    // for testing purposes only
    public List<BrowseContentBean> getBrowseContentBeansFollowedByUser(long userID) {
        try {
            return template().query(GET_BEAN_BY_FOLLOWED_USER_SQL, new BrowseContentBeanRowMapper(),
                    new MapSqlParameterSource(ImmutableMap.of("userId", userID)));
        }
        catch (EmptyResultDataAccessException erdae) {
            return null;
        }
    }

    @Override
    public List<Long> getTopFollowedContainers(int start, int numResults) {
        try {
            JiveJdbcOperationsTemplate template = new JiveJdbcOperationsTemplate(getSimpleJdbcTemplate());
            return template.queryScrollable(GET_TOP_FOLLOWED_CONTAINERS, start, numResults, LongRowMapper.getLongRowMapper());
        }
        catch (EmptyResultDataAccessException erdae) {
            return null;
        }
    }

    @Override
    public List<Long> getTopFollowedUsers(int start, int numResults) {
        try {
            JiveJdbcOperationsTemplate template = new JiveJdbcOperationsTemplate(getSimpleJdbcTemplate());
            return template.queryScrollable(GET_TOP_FOLLOWED_USERS, start, numResults, LongRowMapper.getLongRowMapper(),
                    GRAPH_ID, UserRelationship.RelationshipState.APPROVED.getState());
        }
        catch (EmptyResultDataAccessException erdae) {
            return null;
        }
    }

    @Override
    public int getFollowerCountForUser(long userID) {
        return template().queryForInt(
                GET_BEAN_BY_FOLLOWED_USER_SQL,
                new MapSqlParameterSource(ImmutableMap.of("userID", userID, "graphID", GRAPH_ID, "state",
                        UserRelationship.RelationshipState.APPROVED.getState())));
    }

    @Override
    public int getFollowingCountForUser(long userID) {
        return template().queryForInt(
                GET_FOLLOWING_COUNT_USER_SQL,
                new MapSqlParameterSource(ImmutableMap.of("userID", userID, "graphID", GRAPH_ID, "state",
                        UserRelationship.RelationshipState.APPROVED.getState())));
    }

    @Override
    public int getFollowerCount(long browseID) {
        return template().queryForInt(GET_FOLLOWER_COUNT_SQL,
                new MapSqlParameterSource(ImmutableMap.of("browseID", browseID)));
    }

    @Override
    public Map<Long, Integer> getFollowerCount(Collection<Long> browseIDs) {
        if (browseIDs.size() == 0) {
            return Maps.newHashMap();
        }
        Map<Long, Integer> counts = Maps.newHashMap();
        List<FollowerCount> results;
        try {
            results = template().query(GET_BULK_FOLLOWER_COUNT_SQL, new FollowerCountRowMapper(),
                    new MapSqlParameterSource(ImmutableMap.of("browseIDs", browseIDs)));
        }
        catch (EmptyResultDataAccessException erdae) {
            results = Lists.newLinkedList();
        }
        for (FollowerCount result : results) {
            counts.put(result.browseID, result.count);
        }
        return counts;
    }

    @Override
    public List<Long> getParticipants(long browseID) {
        try {
            return template().query(GET_PARTICIPANTS_SQL, LongRowMapper.getLongRowMapper(),
                    new MapSqlParameterSource(ImmutableMap.of("browseID", browseID)));
        }
        catch (EmptyResultDataAccessException erdae) {
            return null;
        }
    }

    public CachedPreparedStatement getBrowseQuery(Set<BrowseFilter> browseFilters, BrowseSort sort,
            QueryFilterDef.Archetype archetype, boolean isCountQuery) {

        StringBuilder builder = new StringBuilder();
        CachedPreparedStatement pstmt = new CachedPreparedStatement();
        String alias = QueryFilterDef.getBrowseTableAlias(archetype);
        boolean hasMostRatedFilter = hasMostRatedFilter(browseFilters);

        // determine if SELECT DISTINCT is required
        boolean requiresDistinct = false;
        for (BrowseFilter filter : browseFilters) {
            QueryFilterDef def = filter.getQueryFilterDef(browseFilters, archetype);
            if (def != null) {
                if (def.isRequiresDistinct()) {
                    requiresDistinct = true;
                    break;
                }
            }
        }
        String distinct = "";
        String distinctClose = "";
        if (requiresDistinct) {
            StringBuilder distinctBuilder = new StringBuilder();
            // must use parentheses with DISTINCT when used as a COUNT expression
            if (isCountQuery) {
                distinctBuilder.append("DISTINCT(");
                distinctClose = ")";
            }
            else {
                distinctBuilder.append("DISTINCT ");
            }
            String selectFilterForSort = getSelectFilterForSort(sort, archetype);

            if (StringUtils.isNotBlank(selectFilterForSort)) {
                distinctBuilder.append(selectFilterForSort).append(",");
            }

            if (sort instanceof MultipleColumnBrowseSort && !((MultipleColumnBrowseSort)sort).isSelectAlreadyIncludedForExtraColumn()) {
                String selectFilterForMultipleColumnSort = getSelectFilterForSort(((MultipleColumnBrowseSort)sort).getExtraColumnSort(), archetype);

                if (StringUtils.isNotBlank(selectFilterForMultipleColumnSort)) {
                    distinctBuilder.append(selectFilterForMultipleColumnSort).append(",");
                }
            }

            distinct = distinctBuilder.toString();
        }

        // build column list
        StringBuilder columnBuilder = new StringBuilder();
        String fromTable = null;
        if (archetype == QueryFilterDef.Archetype.Content) {
            if (isCountQuery) {
                // multi-column distincts are not supported inside a count
                columnBuilder.append(distinct).append(alias).append(".browseID").append(distinctClose);
            }
            else {
                columnBuilder.append(distinct).append(alias).append(".objectType, ").append(alias).append(".objectID,")
                        .append(alias).append(".containerType,").append(alias).append(".containerID").append(distinctClose);
            }
            fromTable = "jiveBrowseCnt ";
            
            if(hasMostRatedFilter) {
                columnBuilder.append(",").append("COALESCE((sum(cast(acclaim.score as float)) / count(acclaim.objectID)) ,0) AS rating");
                columnBuilder.append(",").append("(SELECT vc.viewCount from jiveViewCount vc where vc.objectID = ")
                        //.append(alias).append(".objectID) AS viewCount");
                        .append(alias).append(".objectID")
                        .append(" AND vc.objectType = ").append(alias).append(".objectType")
                        .append(") AS viewCount");
            }
        }
        else if (archetype == QueryFilterDef.Archetype.Container) {
            if (isCountQuery) {
                // multi-column distincts are not supported inside a count
                columnBuilder.append(distinct).append(alias).append(".browseID").append(distinctClose);
            }
            else {
                columnBuilder.append(distinct).append(alias).append(".objectType, ").append(alias).append(".objectID,")
                        .append(alias).append(".containerType,").append(alias).append(".containerID").append(distinctClose);
            }
            fromTable = "jiveBrowseCntr ";
        }
        else {
            // you cannot use column aliases with COUNT
            if (isCountQuery) {
                // multi-column distincts are not supported inside a count
                columnBuilder.append(distinct).append(alias).append(".userID").append(distinctClose);
            }
            else {
                columnBuilder.append(distinct).append(JiveConstants.USER).append(" AS objectType, ").append(alias)
                        .append(".userID AS objectID,").append("0 AS containerType, 0 AS containerID")
                        .append(distinctClose);
            }
            fromTable = "jiveUser ";
        }

        // add SELECT ... FROM
        if (isCountQuery) {
            if (requiresDistinct) {
                builder.append("SELECT COUNT(").append(columnBuilder.toString()).append(")");
            }
            else {
                builder.append("SELECT COUNT(*)");
            }
        }
        else {
            builder.append("SELECT ").append(columnBuilder.toString());
        }
        builder.append(" FROM ").append(fromTable).append(alias);

        // add join tables using TableJoin objects
        List<TableJoin> tableJoins = Lists.newArrayList();
        for (BrowseFilter filter : browseFilters) {
            QueryFilterDef def = filter.getQueryFilterDef(browseFilters, archetype);
            if (def != null && def.getTableJoins() != null) {
                // add joins that don't already exist to ensure there are no redundant duplicates
                for (TableJoin join : def.getTableJoins()) {
                    if (!tableJoins.contains(join)) {
                        tableJoins.add(join);
                    }
                }
            }
        }
        // add joins from sort
        List<TableJoin> sortJoins = null;
        if (sort != null) {
            sortJoins = sort.getTableJoins(archetype);
            if (sortJoins != null) {
                for (TableJoin sortJoin : sortJoins) {
                    if (!tableJoins.contains(sortJoin)) {
                        tableJoins.add(sortJoin);
                    }
                }
            }
        }
        // apply parameters
        for (TableJoin join : tableJoins) {
            builder.append(join.getJoinType().sql + " JOIN ").append(join.getTable()).append(" ").append(join.getAlias()).append(" ON ").append(join.getCondition());
            addParameters(join.getTableJoinParameterTypes(), join.getTableJoinParameterValues(), pstmt);
        }

        // Join acclaim table
        if(hasMostRatedFilter) {
            builder.append(" LEFT JOIN jiveAcclaim acclaim on (")
                    .append(alias).append(".objectID")
                    .append(" = acclaim.objectID)");
        }

        // add predicates
        StringBuilder predicates = new StringBuilder();
        boolean first = true;
        for (BrowseFilter filter : browseFilters) {
            QueryFilterDef def = filter.getQueryFilterDef(browseFilters, archetype);
            if (def == null || StringUtils.isBlank(def.getPredicate())) {
                continue;
            }
            if (!first) {
                predicates.append(") AND (");
            }
            first = false;
            predicates.append(def.getPredicate());
            addParameters(def.getPredicateParameterTypes(), def.getPredicateParameterValues(), pstmt);
        }

        if (predicates.length() > 0) {
            builder.append(" WHERE (");
            builder.append(predicates.toString());
            builder.append(")");
            if(hasMostRatedFilter) {
                builder.append(" AND ").append(alias).append(".objectType=").append(JiveConstants.DOCUMENT);
            }
        } else if(hasMostRatedFilter) {
            builder.append(" WHERE ").append(alias).append(".objectType=").append(JiveConstants.DOCUMENT);
        }

        if(hasMostRatedFilter) {
            builder.append(" GROUP BY ");
            builder.append(alias).append(".objectID").append(",");
            builder.append(alias).append(".objectType").append(",");
            builder.append(alias).append(".containerID").append(",");
            builder.append(alias).append(".containerType");
        }

        String orderByModifierForSort = getOrderModifierForSort(sort, archetype);

        if (StringUtils.isNotBlank(orderByModifierForSort)) {
            builder.append(" ORDER BY ").append(orderByModifierForSort);
            if (sort != null && sort.getOrder() == SortOrder.DESCENDING) {
                builder.append(" DESC");
            }

            if (sort instanceof MultipleColumnBrowseSort) {
                BrowseSort extraColumn = ((MultipleColumnBrowseSort)sort).getExtraColumnSort();
                String orderByModifierForExtraColumn = getOrderModifierForSort(extraColumn, archetype);
                if (StringUtils.isNotBlank(orderByModifierForExtraColumn)) {
                    builder.append(", ").append(orderByModifierForExtraColumn);

                    if (extraColumn.getOrder() == SortOrder.DESCENDING) {
                        builder.append(" DESC");
                    }
                }
            }
        }
        
        if(hasMostRatedFilter) {
            builder.append(", viewCount DESC");
        }

        pstmt.setSQL(builder.toString());
        return pstmt;
    }

    private boolean hasMostRatedFilter(Set<BrowseFilter> browseFilters) {
        for (BrowseFilter filter : browseFilters) {
            if(filter instanceof MostRatedFilter) {
                return true;
            }
        }
        return false;
    }
    private String getOrderModifierForSort(BrowseSort sort, QueryFilterDef.Archetype archetype) {
        String orderByModifierForSort = "";
        if (sort instanceof ComputedResultSort) {
            orderByModifierForSort = ((ComputedResultSort)sort).getOrderBy();
        } else if (sort != null) {
            orderByModifierForSort = sort.getColumnName(archetype);
        }
        return orderByModifierForSort;
    }

    private String getSelectFilterForSort(BrowseSort sort, QueryFilterDef.Archetype archetype) {
        String selectFilterForSort = "";
        if (sort instanceof ComputedResultSort) {
            selectFilterForSort = ((ComputedResultSort)sort).getComputedSelect();
        } else if (sort != null) {
            selectFilterForSort = sort.getColumnName(archetype);
        }
        return selectFilterForSort;
    }

    private void addParameters(List<QueryFilterDef.ParamType> types, List<? extends Object> values,
            CachedPreparedStatement pstmt) {
        int index = 0;
        if (types == null) {
            return;
        }
        for (QueryFilterDef.ParamType type : types) {
            switch (type) {
            case BIGINT:
                pstmt.addLong((Long) values.get(index));
                break;
            case DOUBLE:
                pstmt.addDouble((Double) values.get(index));
                break;
            case BOOLEAN:
                pstmt.addBoolean((Boolean) values.get(index));
                break;
            case INTEGER:
                pstmt.addInt((Integer) values.get(index));
                break;
            case VARCHAR:
                pstmt.addString((String) values.get(index));
                break;
            }
            index++;
        }
    }

    private static class FollowerCount {
        public long browseID;
        public int count;
    }

    private static class BrowseTuple {
        public long objectID;
        public long browseID;
    }

    private static class BrowseEntity {
        public int objectType;
        public long objectID;
    }

    private static class BrowseIdContainerEntity {
        public long browseID;
        public int objectType;
        public long objectID;
        public int containerType;
        public long containerID;
    }

    private static class LatestActivity {
        public long objectID;
        public long activityTime;
    }

    private static class FollowerCountRowMapper implements RowMapper<FollowerCount> {
        public FollowerCount mapRow(ResultSet rs, int rowNum) throws SQLException {
            FollowerCount obj = new FollowerCount();
            obj.browseID = rs.getLong(1);
            obj.count = rs.getInt(2);
            return obj;
        }
    }

    private static class BrowseTupleRowMapper implements RowMapper<BrowseTuple> {
        public BrowseTuple mapRow(ResultSet rs, int rowNum) throws SQLException {
            BrowseTuple obj = new BrowseTuple();
            obj.objectID = rs.getLong(1);
            obj.browseID = rs.getLong(2);
            return obj;
        }
    }

    private static class BrowseEntityRowMapper implements RowMapper<BrowseEntity> {
        public BrowseEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
            BrowseEntity obj = new BrowseEntity();
            obj.objectType = rs.getInt(1);
            obj.objectID = rs.getLong(2);
            return obj;
        }
    }

    private static class BrowseIdContainerEntityRowMapper implements RowMapper<BrowseIdContainerEntity> {
        public BrowseIdContainerEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
            BrowseIdContainerEntity obj = new BrowseIdContainerEntity();
            obj.browseID = rs.getLong(1);
            obj.objectType = rs.getInt(2);
            obj.objectID = rs.getLong(3);
            obj.containerType = rs.getInt(4);
            obj.containerID = rs.getLong(5);
            return obj;
        }
    }

    private static class LatestActivityRowMapper implements RowMapper<LatestActivity> {
        public LatestActivity mapRow(ResultSet rs, int rowNum) throws SQLException {
            LatestActivity obj = new LatestActivity();
            obj.objectID = rs.getLong(1);
            obj.activityTime = rs.getLong(2);
            return obj;
        }
    }

    private static class BrowseContentBeanRowMapper implements RowMapper<BrowseContentBean> {
        public BrowseContentBean mapRow(ResultSet rs, int rowNum) throws SQLException {
            BrowseContentBean obj = new BrowseContentBean();
            obj.setBrowseID(rs.getLong("browseID"));
            obj.setContainerID(rs.getLong("containerID"));
            obj.setContainerType(rs.getInt("containerType"));
            obj.setObjectType(rs.getInt("objectType"));
            obj.setID(rs.getLong("objectID"));
            obj.setAuthorID(rs.getLong("authorID"));
            obj.setStatusCode(rs.getInt("status"));
            obj.setCreationDateMillis(rs.getLong("creationDate"));
            obj.setModificationDateMillis(rs.getLong("modificationDate"));
            obj.setSubject(rs.getString("subject"));
            obj.setLastActivityDateMillis(rs.getLong("lastActivityDate"));
            return obj;
        }
    }

    private static class BrowseSyncContentBeanRowMapper implements RowMapper<BrowseContentBean> {
        public BrowseContentBean mapRow(ResultSet rs, int rowNum) throws SQLException {
            BrowseContentBean obj = new BrowseContentBean();
            obj.setBrowseID(rs.getLong("browseID"));
            obj.setObjectType(rs.getInt("objectType"));
            obj.setID(rs.getLong("objectID"));
            obj.setModificationDateMillis(rs.getLong("modificationDate"));
            return obj;
        }
    }

    private static class BrowseSyncUserEntityRelBeanRowMapper implements RowMapper<BrowseUserEntityRelBean> {
        public BrowseUserEntityRelBean mapRow(ResultSet rs, int rowNum) throws SQLException {
            BrowseUserEntityRelBean obj = new BrowseUserEntityRelBean();
            obj.setObjectType(rs.getInt("objectType"));
            obj.setID(rs.getLong("objectID"));
            obj.setUserID(rs.getLong("userID"));
            return obj;
        }
    }

}
