package com.grail.synchro.dao.impl;

import com.grail.synchro.beans.*;
import com.grail.synchro.dao.SynchroReminderDAO;
import com.grail.synchro.dao.util.SynchroDAOUtil;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroReminderUtils;
import com.jivesoftware.base.database.dao.DAOException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 3/2/15
 * Time: 3:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class SynchroReminderDAOImpl extends SynchroAbstractDAO implements SynchroReminderDAO {

    private static Logger LOG = Logger.getLogger(SynchroReminderDAOImpl.class);

    private SynchroDAOUtil synchroDAOUtil;

    private static String GENERAL_REMINDER_FIELDS = "remindto, body, datetime, isactive, createdby, createddate";
    private static String GENERAL_REMINDER_TABLE = "grailgeneralreminders";
    private static String INSERT_GENERAL_REMINDER = "INSERT INTO " + GENERAL_REMINDER_TABLE + " (id, "+GENERAL_REMINDER_FIELDS+") VALUES (?,?,?,?,?,?,?)";
    private static String UPDATE_GENERAL_REMINDER = "UPDATE " + GENERAL_REMINDER_TABLE + " SET "+ GENERAL_REMINDER_FIELDS.replaceAll(",","=?,")+"=? WHERE id = ?";
    private static String GET_ALL_GENERAL_REMINDERS = "SELECT id,"+GENERAL_REMINDER_FIELDS+" FROM "+GENERAL_REMINDER_TABLE;
    private static String GET_GENERAL_REMINDER_BY_ID = "SELECT id,"+GENERAL_REMINDER_FIELDS+" FROM "+GENERAL_REMINDER_TABLE+ " WHERE id =?";

    private static String PROJECT_REMINDER_FIELDS = "categorytypes,freqtype,reminddraftprojbefore," +
            "recurrencerangestart,recurrencerangeend,recurrencerangeendafter," +
            "lastremindersenton,nextReminderOn,totalremindercount,isactive,createdby,createddate,rangeEndType,remindToType";
    private static String PROJECT_REMINDER_TABLE = "grailprojectreminders";
    private static String INSERT_PROJECT_REMINDER = "INSERT INTO " + PROJECT_REMINDER_TABLE + " (id, "+PROJECT_REMINDER_FIELDS+") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private static String UPDATE_PROJECT_REMINDER = "UPDATE " + PROJECT_REMINDER_TABLE + " SET "+ PROJECT_REMINDER_FIELDS.replaceAll(",","=?,")+"=? WHERE id = ?";
    private static String GET_ALL_PROJECT_REMINDERS = "SELECT id,"+PROJECT_REMINDER_FIELDS+" FROM "+PROJECT_REMINDER_TABLE;
    private static String GET_PROJECT_REMINDER_BY_ID = "SELECT id,"+PROJECT_REMINDER_FIELDS+" FROM "+PROJECT_REMINDER_TABLE+ " WHERE id =?";


    private static String INSERT_PROJECT_REMINDER_USER_MAPPING = "INSERT INTO grailProjReminderUserMapping (reminderId, userId) VALUES (?,?)";
    private static String DELETE_PROJECT_REMINDER_USER_MAPPING = "DELETE FROM grailProjReminderUserMapping WHERE reminderId=?";

    private static String INSERT_PROJECT_REMINDER_DAILY_FREQ = "INSERT INTO grailProjReminderDailyFreq (reminderId, dayFrequency, dailyFrequencyType) VALUES (?,?,?)";
    private static String DELETE_PROJECT_REMINDER_DAILY_FREQ = "DELETE FROM grailProjReminderDailyFreq WHERE reminderId=?";

    private static String INSERT_PROJECT_REMINDER_WEEKLY_FREQ = "INSERT INTO grailProjReminderWeeklyFreq (reminderId, weekFrequency, weekDays) VALUES (?,?,?)";
    private static String DELETE_PROJECT_REMINDER_WEEKLY_FREQ = "DELETE FROM grailProjReminderWeeklyFreq WHERE reminderId=?";

    private static String INSERT_PROJECT_REMINDER_MONTHLY_FREQ = "INSERT INTO grailProjReminderMonthlyFreq (reminderId, dayOfMonth, weekNumOfMonth, dayOfWeek, monthFrequency, monthFrequencyType) VALUES (?,?,?,?,?,?)";
    private static String DELETE_PROJECT_REMINDER_MONTHLY_FREQ = "DELETE FROM grailProjReminderMonthlyFreq WHERE reminderId=?";

    private static String INSERT_PROJECT_REMINDER_YEARLY_FREQ = "INSERT INTO grailProjReminderYearlyFreq (reminderId, dayOfMonth, weekNumOfMonth, dayOfWeek, monthOfYear,yearFrequency,yearFrequencyType) VALUES (?,?,?,?,?,?,?)";
    private static String DELETE_PROJECT_REMINDER_YEARLY_FREQ = "DELETE FROM grailProjReminderYearlyFreq WHERE reminderId=?";





    @Override
    public Long saveGeneralReminder(final GeneralReminderBean bean) {
        Long id = synchroDAOUtil.nextSequenceID("id", GENERAL_REMINDER_TABLE);
        try{
            getSimpleJdbcTemplate().update(INSERT_GENERAL_REMINDER,
                    id,
                    bean.getRemindTo(),
                    bean.getReminderBody(),
                    bean.getReminderDate().getTime(),
                    1,
                    bean.getCreatedBy(),
                    bean.getCreatedDate().getTime()
            );
            return id;
        } catch (DAOException e) {
            LOG.error(e.getMessage());
        }
        return null;
    }

    @Override
    public Long updateGeneralReminder(GeneralReminderBean bean) {
        try{
            getSimpleJdbcTemplate().update(UPDATE_GENERAL_REMINDER,
                    bean.getRemindTo(),
                    bean.getReminderBody(),
                    bean.getReminderDate().getTime(),
                    bean.isActive()?1:0,
                    bean.getCreatedBy(),
                    bean.getCreatedDate().getTime(),
                    bean.getId()
            );
            return bean.getId();
        } catch (DAOException e) {
            LOG.error(e.getMessage());
        }
        return null;
    }

    @Override
    public void overrideExistingCategoryTypes(final ProjectReminderBean bean) {
        try {
            if(bean.getCategoryTypes() != null && bean.getCategoryTypes().size() > 0) {
                for(Long ct: bean.getCategoryTypes()) {
                    String selectQuery = "SELECT id,"+ PROJECT_REMINDER_FIELDS;
                    selectQuery += ",(SELECT df.dayFrequency FROM grailProjReminderDailyFreq df WHERE df.reminderId = id) as dailyFrequency";
                    selectQuery += ",(SELECT df.dailyFrequencyType FROM grailProjReminderDailyFreq df WHERE df.reminderId = id) as dailyFrequencyType";

                    selectQuery += ",(SELECT df.weekFrequency FROM grailProjReminderWeeklyFreq df WHERE df.reminderId = id) as weeklyFrequency";
                    selectQuery += ",(SELECT df.weekDays FROM grailProjReminderWeeklyFreq df WHERE df.reminderId = id) as weekdays";

                    selectQuery += ",(SELECT df.dayOfMonth FROM grailProjReminderMonthlyFreq df WHERE df.reminderId = id) as monthlyDayOfMonth";
                    selectQuery += ",(SELECT df.weekNumOfMonth FROM grailProjReminderMonthlyFreq df WHERE df.reminderId = id) as monthlyWeekNumOfMonth";
                    selectQuery += ",(SELECT df.dayOfWeek FROM grailProjReminderMonthlyFreq df WHERE df.reminderId = id) as monthlyDayOfWeek";
                    selectQuery += ",(SELECT df.monthFrequency FROM grailProjReminderMonthlyFreq df WHERE df.reminderId = id) as monthlyFrequency";
                    selectQuery += ",(SELECT df.monthFrequencyType FROM grailProjReminderMonthlyFreq df WHERE df.reminderId = id) as monthlyFrequencyType";

                    selectQuery += ",(SELECT df.dayOfMonth FROM grailProjReminderYearlyFreq df WHERE df.reminderId = id) as yearlyDayOfMonth";
                    selectQuery += ",(SELECT df.weekNumOfMonth FROM grailProjReminderYearlyFreq df WHERE df.reminderId = id) as yearlyWeekNumOfMonth";
                    selectQuery += ",(SELECT df.dayOfWeek FROM grailProjReminderYearlyFreq df WHERE df.reminderId = id) as yearlyDayOfWeek";
                    selectQuery += ",(SELECT df.monthOfYear FROM grailProjReminderYearlyFreq df WHERE df.reminderId = id) as yearlyMonthOfYear";
                    selectQuery += ",(SELECT df.yearFrequency FROM grailProjReminderYearlyFreq df WHERE df.reminderId = id) as yearlyFrequency";
                    selectQuery += ",(SELECT df.yearFrequencyType FROM grailProjReminderYearlyFreq df WHERE df.reminderId = id) as yearlyFrequencyType";
                    selectQuery += ",(array_to_string(array(select prum.userid from grailprojreminderusermapping prum where prum.reminderId = id),',')) as remindTo";

                    selectQuery +=  " FROM " + PROJECT_REMINDER_TABLE;
                    selectQuery += " WHERE ((SELECT count(*) FROM grailprojreminderusermapping um WHERE um.reminderid = id AND um.userid in ("+StringUtils.join(bean.getRemindTo(), ",")+")) > 0)";
                    selectQuery += " AND ("+ct+" = ANY(('{' || (categorytypes) || '}')::int[]))";
                    if(bean.getId() != null && bean.getId() > 0) {
                        selectQuery += " AND id != "+bean.getId();
                    }
                    try {
                        List<ProjectReminderBean> projectReminders  = getSimpleJdbcTemplate().query(selectQuery, projectReminderBeanParameterizedRowMapper);
                        if(projectReminders != null && projectReminders.size() > 0) {
                            for(ProjectReminderBean pr : projectReminders) {
                                List<Long> validCategoryTypes = new ArrayList<Long>();
                                for(Long categoryType : pr.getCategoryTypes()) {
                                    if(!bean.getCategoryTypes().contains(categoryType)) {
                                        validCategoryTypes.add(categoryType);
                                    }
                                }
                                if(validCategoryTypes.size() > 0) {
                                    String updateQuery = "UPDATE "+ PROJECT_REMINDER_TABLE + " SET categorytypes = '"+(StringUtils.join(validCategoryTypes, ","))+"' WHERE id="+pr.getId();
                                    try {
                                        getSimpleJdbcTemplate().getJdbcOperations().update(updateQuery);
                                    } catch (DAOException e) {
                                        e.printStackTrace();
                                        LOG.error(e.getMessage());
                                    }
                                } else {
//                                    String updateQuery = "UPDATE "+ PROJECT_REMINDER_TABLE + " SET categorytypes = '', isactive=0 WHERE id="+pr.getId();
//                                    try {
//                                        getSimpleJdbcTemplate().getJdbcOperations().update(updateQuery);
//                                    } catch (DAOException e) {
//                                        e.printStackTrace();
//                                        LOG.error(e.getMessage());
//                                    }
                                    String userMappingDeleteQuery = "DELETE FROM grailProjReminderUserMapping WHERE reminderId = "+ pr.getId();
                                    try {
                                        getSimpleJdbcTemplate().getJdbcOperations().update(userMappingDeleteQuery);
                                    } catch (DAOException e) {
                                        e.printStackTrace();
                                        LOG.error(e.getMessage());
                                    }

                                    String dailyReminderDeleteQuery = "DELETE FROM grailProjReminderDailyFreq WHERE reminderId = "+ pr.getId();
                                    try {
                                        getSimpleJdbcTemplate().getJdbcOperations().update(dailyReminderDeleteQuery);
                                    } catch (DAOException e) {
                                        e.printStackTrace();
                                        LOG.error(e.getMessage());
                                    }

                                    String weeklyReminderDeleteQuery = "DELETE FROM grailProjReminderWeeklyFreq WHERE reminderId = "+ pr.getId();
                                    try {
                                        getSimpleJdbcTemplate().getJdbcOperations().update(weeklyReminderDeleteQuery);
                                    } catch (DAOException e) {
                                        e.printStackTrace();
                                        LOG.error(e.getMessage());
                                    }

                                    String monthlyReminderDeleteQuery = "DELETE FROM grailProjReminderMonthlyFreq WHERE reminderId = "+ pr.getId();
                                    try {
                                        getSimpleJdbcTemplate().getJdbcOperations().update(monthlyReminderDeleteQuery);
                                    } catch (DAOException e) {
                                        e.printStackTrace();
                                        LOG.error(e.getMessage());
                                    }

                                    String yearlyReminderDeleteQuery = "DELETE FROM grailProjReminderYearlyFreq WHERE reminderId = "+ pr.getId();
                                    try {
                                        getSimpleJdbcTemplate().getJdbcOperations().update(yearlyReminderDeleteQuery);
                                    } catch (DAOException e) {
                                        e.printStackTrace();
                                        LOG.error(e.getMessage());
                                    }

                                    String deleteQuery = "DELETE FROM "+ PROJECT_REMINDER_TABLE + " WHERE id="+pr.getId();
                                    try {
                                        getSimpleJdbcTemplate().getJdbcOperations().update(deleteQuery);
                                    } catch (DAOException e) {
                                        e.printStackTrace();
                                        LOG.error(e.getMessage());
                                    }
                                }
                            }
                        }
                    } catch (DAOException e) {
                        e.printStackTrace();
                        LOG.error(e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Long saveProjectReminder(final ProjectReminderBean bean) {
        Long id = synchroDAOUtil.nextSequenceID("id", PROJECT_REMINDER_TABLE);
        try{
            getSimpleJdbcTemplate().update(INSERT_PROJECT_REMINDER,
                    id,
                    (bean.getCategoryTypes() != null && bean.getCategoryTypes().size() > 0)?StringUtils.join(bean.getCategoryTypes(), ","):null,
                    bean.getFrequencyType(),
                    bean.getDraftProjectRemindBefore(),
                    bean.getRangeStartDate() != null?bean.getRangeStartDate().getTime():null,
                    bean.getRangeEndDate() != null?bean.getRangeEndDate().getTime():null,
                    bean.getRangeEndAfter(),
                    bean.getLastReminderSentOn() != null?bean.getLastReminderSentOn().getTime():null,
                    bean.getNextReminderOn() != null?bean.getNextReminderOn().getTime():null,
                    bean.getTotalReminderCount(),
                    1,
                    bean.getCreatedBy(),
                    bean.getCreatedDate().getTime(),
                    bean.getRangeEndType(),
                    bean.getProjectReminderType()
            );
            return id;
        } catch (DAOException e) {
            LOG.error(e.getMessage());
        }
        return null;
    }

    @Override
    public Long updateProjectReminder(ProjectReminderBean bean) {
        try{
            getSimpleJdbcTemplate().update(UPDATE_PROJECT_REMINDER,
                    (bean.getCategoryTypes() != null && bean.getCategoryTypes().size() > 0)?StringUtils.join(bean.getCategoryTypes(),","):null,
                    bean.getFrequencyType(),
                    bean.getDraftProjectRemindBefore(),
                    bean.getRangeStartDate() != null?bean.getRangeStartDate().getTime():null,
                    bean.getRangeEndDate() != null?bean.getRangeEndDate().getTime():null,
                    bean.getRangeEndAfter(),
                    bean.getLastReminderSentOn() != null?bean.getLastReminderSentOn().getTime():null,
                    bean.getNextReminderOn() != null?bean.getNextReminderOn().getTime():null,
                    bean.getTotalReminderCount(),
                    bean.isActive()?1:0,
                    bean.getCreatedBy(),
                    bean.getCreatedDate().getTime(),
                    bean.getRangeEndType(),
                    bean.getProjectReminderType(),
                    bean.getId()
            );
            return bean.getId();
        } catch (DAOException e) {
            LOG.error(e.getMessage());
        }
        return null;
    }

    @Override
    public void updateProjectReminderScheduleDates(final Date lastReminderOn, final Date nextReminderOn, final Long reminderId) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("UPDATE ").append(PROJECT_REMINDER_TABLE).append(" SET lastremindersenton=?,nextReminderOn=? WHERE id=?");
        try {
            getSimpleJdbcTemplate().update(stringBuilder.toString(),
                    lastReminderOn != null?lastReminderOn.getTime():null,
                    nextReminderOn != null?nextReminderOn.getTime():null,
                    reminderId
            );
        } catch (DAOException e) {
            LOG.error(e.getMessage());
        }
    }

    @Override
    public void saveProjectReminderUserMapping(final Long reminderId, final Long userId) {
        try{
            getSimpleJdbcTemplate().update(INSERT_PROJECT_REMINDER_USER_MAPPING, reminderId, userId);
        } catch (DAOException e) {
            LOG.error(e.getMessage());
        }
    }

    @Override
    public void deleteProjectReminderUserMapping(final Long reminderId) {
        try{
            getSimpleJdbcTemplate().update(DELETE_PROJECT_REMINDER_USER_MAPPING, reminderId);
        } catch (DAOException e) {
            LOG.error(e.getMessage());
        }
    }

    @Override
    public void saveProjectReminderDailyFreq(final ProjectReminderBean bean) {
        try{
            getSimpleJdbcTemplate().update(INSERT_PROJECT_REMINDER_DAILY_FREQ,
                    bean.getId(),
                    bean.getDailyFrequency(),
                    bean.getDailyFrequencyType()
            );
        } catch (DAOException e) {
            LOG.error(e.getMessage());
        }
    }

    @Override
    public void deleteProjectReminderDailyFreq(final Long reminderId) {
        try{
            getSimpleJdbcTemplate().update(DELETE_PROJECT_REMINDER_DAILY_FREQ,reminderId);
        } catch (DAOException e) {
            LOG.error(e.getMessage());
        }
    }

    @Override
    public void saveProjectReminderWeeklyFreq(final ProjectReminderBean bean) {
        try{
            getSimpleJdbcTemplate().update(INSERT_PROJECT_REMINDER_WEEKLY_FREQ,
                    bean.getId(),
                    bean.getWeeklyFrequency(),
                    (bean.getWeekdayTypes() != null && bean.getWeekdayTypes().size() > 0)?StringUtils.join(bean.getWeekdayTypes(),","):null
            );
        } catch (DAOException e) {
            LOG.error(e.getMessage());
        }
    }

    @Override
    public void deleteProjectReminderWeeklyFreq(final Long reminderId) {
        try{
            getSimpleJdbcTemplate().update(DELETE_PROJECT_REMINDER_WEEKLY_FREQ,reminderId);
        } catch (DAOException e) {
            LOG.error(e.getMessage());
        }
    }

    @Override
    public void saveProjectReminderMonthlyFreq(ProjectReminderBean bean) {
        // reminderId, dayOfMonth, weekNumOfMonth, dayOfWeek, monthFrequency,monthFrequencyType
        try{
            getSimpleJdbcTemplate().update(INSERT_PROJECT_REMINDER_MONTHLY_FREQ,
                    bean.getId(),
                    bean.getMonthlyDayOfMonth(),
                    bean.getMonthlyWeekOfMonth(),
                    bean.getMonthlyDayOfWeek(),
                    bean.getMonthlyFrequency(),
                    bean.getMonthlyFrequencyType()
            );
        } catch (DAOException e) {
            LOG.error(e.getMessage());
        }
    }

    @Override
    public void deleteProjectReminderMonthlyFreq(final Long reminderId) {
        try{
            getSimpleJdbcTemplate().update(DELETE_PROJECT_REMINDER_MONTHLY_FREQ,reminderId);
        } catch (DAOException e) {
            LOG.error(e.getMessage());
        }

    }

    @Override
    public void saveProjectReminderYearlyFreq(final ProjectReminderBean bean) {
        try{
            getSimpleJdbcTemplate().update(INSERT_PROJECT_REMINDER_YEARLY_FREQ,
                    bean.getId(),
                    bean.getYearlyDayOfMonth(),
                    bean.getYearlyWeekOfMonth(),
                    bean.getYearlyDayOfWeek(),
                    bean.getYearlyMonthOfYear(),
                    bean.getYearlyFrequency(),
                    bean.getYearlyFrequencyType()
            );
        } catch (DAOException e) {
            LOG.error(e.getMessage());
        }
    }

    @Override
    public void deleteProjectReminderYearlyFreq(final Long reminderId) {
        try{
            getSimpleJdbcTemplate().update(DELETE_PROJECT_REMINDER_YEARLY_FREQ,reminderId);
        } catch (DAOException e) {
            LOG.error(e.getMessage());
        }
    }

    @Override
    public List<ProjectReminderBean> getProjectReminders(final ProjectReminderResultFilter filter) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT id,").append(PROJECT_REMINDER_FIELDS);
        sqlBuilder.append(",(SELECT df.dayFrequency FROM grailProjReminderDailyFreq df WHERE df.reminderId = id) as dailyFrequency");
        sqlBuilder.append(",(SELECT df.dailyFrequencyType FROM grailProjReminderDailyFreq df WHERE df.reminderId = id) as dailyFrequencyType");

        sqlBuilder.append(",(SELECT df.weekFrequency FROM grailProjReminderWeeklyFreq df WHERE df.reminderId = id) as weeklyFrequency");
        sqlBuilder.append(",(SELECT df.weekDays FROM grailProjReminderWeeklyFreq df WHERE df.reminderId = id) as weekdays");

        sqlBuilder.append(",(SELECT df.dayOfMonth FROM grailProjReminderMonthlyFreq df WHERE df.reminderId = id) as monthlyDayOfMonth");
        sqlBuilder.append(",(SELECT df.weekNumOfMonth FROM grailProjReminderMonthlyFreq df WHERE df.reminderId = id) as monthlyWeekNumOfMonth");
        sqlBuilder.append(",(SELECT df.dayOfWeek FROM grailProjReminderMonthlyFreq df WHERE df.reminderId = id) as monthlyDayOfWeek");
        sqlBuilder.append(",(SELECT df.monthFrequency FROM grailProjReminderMonthlyFreq df WHERE df.reminderId = id) as monthlyFrequency");
        sqlBuilder.append(",(SELECT df.monthFrequencyType FROM grailProjReminderMonthlyFreq df WHERE df.reminderId = id) as monthlyFrequencyType");

        sqlBuilder.append(",(SELECT df.dayOfMonth FROM grailProjReminderYearlyFreq df WHERE df.reminderId = id) as yearlyDayOfMonth");
        sqlBuilder.append(",(SELECT df.weekNumOfMonth FROM grailProjReminderYearlyFreq df WHERE df.reminderId = id) as yearlyWeekNumOfMonth");
        sqlBuilder.append(",(SELECT df.dayOfWeek FROM grailProjReminderYearlyFreq df WHERE df.reminderId = id) as yearlyDayOfWeek");
        sqlBuilder.append(",(SELECT df.monthOfYear FROM grailProjReminderYearlyFreq df WHERE df.reminderId = id) as yearlyMonthOfYear");
        sqlBuilder.append(",(SELECT df.yearFrequency FROM grailProjReminderYearlyFreq df WHERE df.reminderId = id) as yearlyFrequency");
        sqlBuilder.append(",(SELECT df.yearFrequencyType FROM grailProjReminderYearlyFreq df WHERE df.reminderId = id) as yearlyFrequencyType");
        sqlBuilder.append(",(array_to_string(array(select prum.userid from grailprojreminderusermapping prum where prum.reminderId = id),',')) as remindTo");

        sqlBuilder.append(" FROM ").append(PROJECT_REMINDER_TABLE);

        sqlBuilder.append(applyProjectReminderFilter(filter));

        if(filter != null) {
            if(filter.getStart() != null) {
                sqlBuilder.append(" OFFSET ").append(filter.getStart());
            }
            if(filter.getLimit() != null && filter.getLimit() > 0) {
                sqlBuilder.append(" LIMIT ").append(filter.getLimit());
            }
        }

        List<ProjectReminderBean> projectReminderBeans = Collections.emptyList();
        try{
            projectReminderBeans = getSimpleJdbcTemplate().query(sqlBuilder.toString(), projectReminderBeanParameterizedRowMapper);
        } catch (DAOException e) {
            LOG.error(e.getMessage());
        }
        return projectReminderBeans;
    }

    private String applyProjectReminderFilter(final ProjectReminderResultFilter filter) {
        List<String> conditions = new ArrayList<String>();
        if(filter.isShowOnlyActiveReminders()) {
            conditions.add("isactive = 1");
        }
        if(filter.getDate() != null) {
            StringBuilder dateFilterBuilder = new StringBuilder();
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeZone(SynchroReminderUtils.getRemindersTimeZone());
            calendar.setTime(filter.getDate());
            dateFilterBuilder.append("(nextReminderOn is null OR ");
            dateFilterBuilder.append("(").append("to_char(to_timestamp(nextReminderOn/1000),'YYYY')::int =").append(calendar.get(Calendar.YEAR)).append(")");
            dateFilterBuilder.append(" AND (").append("to_char(to_timestamp(nextReminderOn/1000),'mm')::int =").append(calendar.get(Calendar.MONTH)).append(")");
            dateFilterBuilder.append(" AND (").append("to_char(to_timestamp(nextReminderOn/1000),'dd')::int =").append(calendar.get(Calendar.DATE)).append(")");
            dateFilterBuilder.append(")");
            conditions.add(dateFilterBuilder.toString());
        }

        if(filter.getOwners() != null && filter.getOwners().size() > 0) {
            StringBuilder ownerFilterBuilder = new StringBuilder();
            ownerFilterBuilder.append("(");
            ownerFilterBuilder.append("(").append("(SELECT count(*) FROM grailprojreminderusermapping mp WHERE mp.reminderId = id AND mp.userId in ("+StringUtils.join(filter.getOwners(), ",")+")) > 0").append(")");
            ownerFilterBuilder.append(" OR ");
            ownerFilterBuilder.append("(createdby in ("+StringUtils.join(filter.getOwners(), ",")+"))");
            ownerFilterBuilder.append(")");
            conditions.add(ownerFilterBuilder.toString());
        }

        StringBuilder filterBuilder = new StringBuilder();
        if(conditions.size() > 0) {
            filterBuilder.append(" WHERE ").append(StringUtils.join(conditions, " AND "));
        }
        return filterBuilder.toString();
    }

    @Override
    public Integer getProjectRemindersTotalCount(final ProjectReminderResultFilter filter) {
        Integer count = 0;
        try {
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT count(*) FROM ").append(PROJECT_REMINDER_TABLE);
            sqlBuilder.append(applyProjectReminderFilter(filter));
            count = getSimpleJdbcTemplate().queryForInt(sqlBuilder.toString());
        } catch (DAOException e) {
            LOG.error(e.getMessage());
        }
        return count;
    }

    @Override
    public ProjectReminderBean getProjectReminder(final Long reminderId) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT id,").append(PROJECT_REMINDER_FIELDS);
        sqlBuilder.append(",(SELECT df.dayFrequency FROM grailProjReminderDailyFreq df WHERE df.reminderId = id) as dailyFrequency");
        sqlBuilder.append(",(SELECT df.dailyFrequencyType FROM grailProjReminderDailyFreq df WHERE df.reminderId = id) as dailyFrequencyType");

        sqlBuilder.append(",(SELECT df.weekFrequency FROM grailProjReminderWeeklyFreq df WHERE df.reminderId = id) as weeklyFrequency");
        sqlBuilder.append(",(SELECT df.weekDays FROM grailProjReminderWeeklyFreq df WHERE df.reminderId = id) as weekdays");

        sqlBuilder.append(",(SELECT df.dayOfMonth FROM grailProjReminderMonthlyFreq df WHERE df.reminderId = id) as monthlyDayOfMonth");
        sqlBuilder.append(",(SELECT df.weekNumOfMonth FROM grailProjReminderMonthlyFreq df WHERE df.reminderId = id) as monthlyWeekNumOfMonth");
        sqlBuilder.append(",(SELECT df.dayOfWeek FROM grailProjReminderMonthlyFreq df WHERE df.reminderId = id) as monthlyDayOfWeek");
        sqlBuilder.append(",(SELECT df.monthFrequency FROM grailProjReminderMonthlyFreq df WHERE df.reminderId = id) as monthlyFrequency");
        sqlBuilder.append(",(SELECT df.monthFrequencyType FROM grailProjReminderMonthlyFreq df WHERE df.reminderId = id) as monthlyFrequencyType");

        sqlBuilder.append(",(SELECT df.dayOfMonth FROM grailProjReminderYearlyFreq df WHERE df.reminderId = id) as yearlyDayOfMonth");
        sqlBuilder.append(",(SELECT df.weekNumOfMonth FROM grailProjReminderYearlyFreq df WHERE df.reminderId = id) as yearlyWeekNumOfMonth");
        sqlBuilder.append(",(SELECT df.dayOfWeek FROM grailProjReminderYearlyFreq df WHERE df.reminderId = id) as yearlyDayOfWeek");
        sqlBuilder.append(",(SELECT df.monthOfYear FROM grailProjReminderYearlyFreq df WHERE df.reminderId = id) as yearlyMonthOfYear");
        sqlBuilder.append(",(SELECT df.yearFrequency FROM grailProjReminderYearlyFreq df WHERE df.reminderId = id) as yearlyFrequency");
        sqlBuilder.append(",(SELECT df.yearFrequencyType FROM grailProjReminderYearlyFreq df WHERE df.reminderId = id) as yearlyFrequencyType");
        sqlBuilder.append(",(array_to_string(array(select prum.userid from grailprojreminderusermapping prum where prum.reminderId = id),',')) as remindTo");

        sqlBuilder.append(" FROM ").append(PROJECT_REMINDER_TABLE);
        sqlBuilder.append(" WHERE id =").append(reminderId);


        ProjectReminderBean projectReminder = null;
        try{
            projectReminder = getSimpleJdbcTemplate().queryForObject(sqlBuilder.toString(), projectReminderBeanParameterizedRowMapper);
        } catch (DAOException e) {
            LOG.error(e.getMessage());
        }
        return projectReminder;
    }

    private final ParameterizedRowMapper<ProjectReminderBean> projectReminderBeanParameterizedRowMapper = new ParameterizedRowMapper<ProjectReminderBean>() {
        public ProjectReminderBean mapRow(ResultSet rs, int row) throws SQLException {
            ProjectReminderBean projectReminderBean = new ProjectReminderBean();
            projectReminderBean.setId(rs.getLong("id"));
            projectReminderBean.setFrequencyType(rs.getInt("freqtype"));
            projectReminderBean.setDraftProjectRemindBefore(rs.getInt("reminddraftprojbefore"));
            projectReminderBean.setRangeStartDate(new Date(rs.getLong("recurrencerangestart")));
            if(rs.getLong("recurrencerangeend") > 0) {
                projectReminderBean.setRangeEndDate(new Date(rs.getLong("recurrencerangeend")));
            }
            if(rs.getLong("recurrencerangeendafter") > 0) {
                projectReminderBean.setRangeEndAfter(rs.getInt("recurrencerangeendafter"));
            }

            if(rs.getLong("lastremindersenton") > 0) {
                projectReminderBean.setLastReminderSentOn(new Date(rs.getLong("lastremindersenton")));
            }

            if(rs.getLong("nextReminderOn") > 0) {
                projectReminderBean.setNextReminderOn(new Date(rs.getLong("nextReminderOn")));
            }
            projectReminderBean.setTotalReminderCount(rs.getInt("totalremindercount"));
            projectReminderBean.setActive(rs.getBoolean("isactive"));
            projectReminderBean.setCreatedBy(rs.getLong("createdby"));
            projectReminderBean.setCreatedDate(new Date(rs.getLong("createddate")));

            String categoryTypesStr =  rs.getString("categorytypes");
            if(categoryTypesStr != null && !categoryTypesStr.equals("")) {
                List<Long> categoryTypes = new ArrayList<Long>();
                for(String cT : categoryTypesStr.split(",")) {
                    categoryTypes.add(Long.parseLong(cT));
                }
                projectReminderBean.setCategoryTypes(categoryTypes);
            }

            String remindToStr =  rs.getString("remindTo");
            if(remindToStr != null && !remindToStr.equals("")) {
                List<Long> remindTo = new ArrayList<Long>();
                for(String rT : remindToStr.split(",")) {
                    remindTo.add(Long.parseLong(rT));
                }
                projectReminderBean.setRemindTo(remindTo);
            }

            projectReminderBean.setDailyFrequency(rs.getInt("dailyFrequency"));
            projectReminderBean.setDailyFrequencyType(rs.getInt("dailyFrequencyType"));

            projectReminderBean.setWeeklyFrequency(rs.getInt("weeklyFrequency"));
            String weekdaysStr =  rs.getString("weekdays");
            if(weekdaysStr != null && !weekdaysStr.equals("")) {
                List<Integer> weekdays = new ArrayList<Integer>();
                for(String rT : weekdaysStr.split(",")) {
                    if(!weekdays.contains(Integer.parseInt(rT))) {
                        weekdays.add(Integer.parseInt(rT));
                    }
                }
                Collections.sort(weekdays);
                projectReminderBean.setWeekdayTypes(weekdays);
            }
            projectReminderBean.setMonthlyDayOfMonth(rs.getInt("monthlyDayOfMonth"));
            projectReminderBean.setMonthlyWeekOfMonth(rs.getInt("monthlyWeekNumOfMonth"));
            projectReminderBean.setMonthlyDayOfWeek(rs.getInt("monthlyDayOfWeek"));
            projectReminderBean.setMonthlyFrequency(rs.getInt("monthlyFrequency"));
            projectReminderBean.setMonthlyFrequencyType(rs.getInt("monthlyFrequencyType"));

            projectReminderBean.setYearlyDayOfMonth(rs.getInt("yearlyDayOfMonth"));
            projectReminderBean.setYearlyWeekOfMonth(rs.getInt("yearlyWeekNumOfMonth"));
            projectReminderBean.setYearlyDayOfWeek(rs.getInt("yearlyDayOfWeek"));
            projectReminderBean.setYearlyMonthOfYear(rs.getInt("yearlyMonthOfYear"));
            projectReminderBean.setYearlyFrequency(rs.getInt("yearlyFrequency"));
            projectReminderBean.setYearlyFrequencyType(rs.getInt("yearlyFrequencyType"));
            List<ProjectReminderViewsBean> projectReminderViewsBeans = getProjectReminderViews(projectReminderBean.getId(), SynchroPermHelper.getEffectiveUser().getID(), null);
            if(projectReminderViewsBeans != null && projectReminderViewsBeans.size() > 0) {
                projectReminderBean.setViewed(true);
            } else {
                projectReminderBean.setViewed(false);
            }

            projectReminderBean.setRangeEndType(rs.getInt("rangeEndType"));
            projectReminderBean.setProjectReminderType(rs.getInt("remindToType"));

            return projectReminderBean;
        }
    };


    @Override
    public List<GeneralReminderBean> getGeneralReminders(final GeneralReminderResultFilter filter) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT id").append(",").append(GENERAL_REMINDER_FIELDS).append(" FROM ").append(GENERAL_REMINDER_TABLE);
        sqlBuilder.append(applyGeneralReminderFilter(filter));

        if(filter != null) {
            if(filter.getStart() != null) {
                sqlBuilder.append(" OFFSET ").append(filter.getStart());
            }
            if(filter.getLimit() != null && filter.getLimit() > 0) {
                sqlBuilder.append(" LIMIT ").append(filter.getLimit());
            }
        }
        List<GeneralReminderBean> generalReminderBeans = Collections.emptyList();
        try{
            generalReminderBeans = getSimpleJdbcTemplate().query(sqlBuilder.toString(), generalReminderBeanParameterizedRowMapper);
        } catch (DAOException e) {
            LOG.error(e.getMessage());
        }
        return generalReminderBeans;
    }

    private String applyGeneralReminderFilter(final GeneralReminderResultFilter filter) {
        List<String> conditions = new ArrayList<String>();

        if(filter.isShowOnlyActiveReminders()) {
            conditions.add("isactive = 1");
        }

        if(filter.getDate() != null) {
            StringBuilder dateFilterBuilder = new StringBuilder();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(filter.getDate());
            dateFilterBuilder.append("(");
            dateFilterBuilder.append(" (").append("to_char(to_timestamp(datetime/1000),'YYYY')::int =").append(calendar.get(Calendar.YEAR)).append(")");
            dateFilterBuilder.append(" AND (").append("to_char(to_timestamp(datetime/1000),'mm')::int =").append(calendar.get(Calendar.MONTH)).append(")");
            dateFilterBuilder.append(" AND (").append("to_char(to_timestamp(datetime/1000),'dd')::int =").append(calendar.get(Calendar.DATE)).append(")");
            dateFilterBuilder.append(")");
            conditions.add(dateFilterBuilder.toString());
        }
        if(filter.getOwner() != null) {
            StringBuilder ownerFilterBuilder = new StringBuilder();
            ownerFilterBuilder.append("(").append("createdby=").append(filter.getOwner()).append(")");
            conditions.add(ownerFilterBuilder.toString());
        }

        StringBuilder filterBuilder = new StringBuilder();
        if(conditions.size() > 0) {
            filterBuilder.append(" WHERE ").append(StringUtils.join(conditions, " AND "));
        }
        return filterBuilder.toString();
    }

    @Override
    public Integer getGeneralRemindersTotalCount(GeneralReminderResultFilter filter) {
        Integer count = 0;
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT count(*) FROM ").append(GENERAL_REMINDER_TABLE);
        sqlBuilder.append(applyGeneralReminderFilter(filter));
        try{
            count = getSimpleJdbcTemplate().queryForInt(sqlBuilder.toString());
        } catch (DAOException e) {
            LOG.error(e.getMessage());
        }
        return count;
    }

    @Override
    public GeneralReminderBean getGeneralReminder(final Long reminderId) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT id").append(",").append(GENERAL_REMINDER_FIELDS).append(" FROM ").append(GENERAL_REMINDER_TABLE);
        sqlBuilder.append(" WHERE id=").append(reminderId);

        GeneralReminderBean generalReminder = null;
        try{
            generalReminder = getSimpleJdbcTemplate().queryForObject(sqlBuilder.toString(), generalReminderBeanParameterizedRowMapper);
        } catch (DAOException e) {
            LOG.error(e.getMessage());
        }
        return generalReminder;
    }

    private final ParameterizedRowMapper<GeneralReminderBean> generalReminderBeanParameterizedRowMapper = new ParameterizedRowMapper<GeneralReminderBean>() {
        public GeneralReminderBean mapRow(ResultSet rs, int row) throws SQLException {
            GeneralReminderBean generalReminderBean = new GeneralReminderBean();
            generalReminderBean.setId(rs.getLong("id"));
            generalReminderBean.setReminderBody(rs.getString("body"));
            generalReminderBean.setRemindTo(rs.getLong("remindto"));
            generalReminderBean.setReminderDate(new Date(rs.getLong("datetime")));
            generalReminderBean.setActive(rs.getBoolean("isactive"));
            generalReminderBean.setCreatedBy(rs.getLong("createdby"));
            generalReminderBean.setCreatedDate(new Date(rs.getLong("createddate")));
            List<GeneralReminderViewsBean> generalReminderViewsBeans = getGeneralReminderViews(generalReminderBean.getId(), SynchroPermHelper.getEffectiveUser().getID(), null);
            if(generalReminderViewsBeans != null && generalReminderViewsBeans.size() > 0) {
                generalReminderBean.setViewed(true);
            } else {
                generalReminderBean.setViewed(false);
            }
            return generalReminderBean;
        }
    };

    @Override
    public List<GeneralReminderViewsBean> getGeneralReminderViews(final Long reminderId, final Long userId, final Date date) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT reminderId, vieweddate, viewedby FROM grailgeneralreminderviews WHERE viewedby = "+userId);
        if(date != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            sqlBuilder.append(" AND (");
            sqlBuilder.append("(").append("to_char(to_timestamp(vieweddate/1000),'YYYY')::int =").append(calendar.get(Calendar.YEAR)).append(")");
            sqlBuilder.append(" AND (").append("to_char(to_timestamp(vieweddate/1000),'mm')::int =").append(calendar.get(Calendar.MONTH)).append(")");
            sqlBuilder.append(" AND (").append("to_char(to_timestamp(vieweddate/1000),'dd')::int =").append(calendar.get(Calendar.DATE)).append(")");
            sqlBuilder.append(")");
        }
        sqlBuilder.append(" AND reminderId="+reminderId);

        List<GeneralReminderViewsBean> generalReminderViewsBeans = null;
        try{
            generalReminderViewsBeans = getSimpleJdbcTemplate().query(sqlBuilder.toString(), generalReminderViewsBeanParameterizedRowMapper);
        } catch (DAOException e) {
            LOG.error(e.getMessage());
        }
        return generalReminderViewsBeans;
    }

    private final ParameterizedRowMapper<GeneralReminderViewsBean> generalReminderViewsBeanParameterizedRowMapper = new ParameterizedRowMapper<GeneralReminderViewsBean>() {
        public GeneralReminderViewsBean mapRow(ResultSet rs, int row) throws SQLException {
            GeneralReminderViewsBean generalReminderViewsBean = new GeneralReminderViewsBean();
            generalReminderViewsBean.setReminderId(rs.getLong("reminderId"));
            generalReminderViewsBean.setViewedDate(new Date(rs.getLong("vieweddate")));
            generalReminderViewsBean.setViewedBy(rs.getLong("viewedby"));
            return generalReminderViewsBean;
        }
    };

    @Override
    public List<ProjectReminderViewsBean> getProjectReminderViews(final Long reminderId, final Long userId, final Date date) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT reminderId, vieweddate, viewedby FROM grailprojectreminderviews WHERE viewedby = "+userId);
        if(date != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            sqlBuilder.append(" AND (");
            sqlBuilder.append("(").append("to_char(to_timestamp(vieweddate/1000),'YYYY')::int =").append(calendar.get(Calendar.YEAR)).append(")");
            sqlBuilder.append(" AND (").append("to_char(to_timestamp(vieweddate/1000),'mm')::int =").append(calendar.get(Calendar.MONTH)).append(")");
            sqlBuilder.append(" AND (").append("to_char(to_timestamp(vieweddate/1000),'dd')::int =").append(calendar.get(Calendar.DATE)).append(")");
            sqlBuilder.append(")");
        }
        sqlBuilder.append(" AND reminderId="+reminderId);

        List<ProjectReminderViewsBean> projectReminderViewsBeans = null;
        try{
            projectReminderViewsBeans = getSimpleJdbcTemplate().query(sqlBuilder.toString(), projectReminderViewsBeanParameterizedRowMapper);
        } catch (DAOException e) {
            LOG.error(e.getMessage());
        }
        return projectReminderViewsBeans;
    }

    private final ParameterizedRowMapper<ProjectReminderViewsBean> projectReminderViewsBeanParameterizedRowMapper = new ParameterizedRowMapper<ProjectReminderViewsBean>() {
        public ProjectReminderViewsBean mapRow(ResultSet rs, int row) throws SQLException {
            ProjectReminderViewsBean projectReminderViewsBean = new ProjectReminderViewsBean();
            projectReminderViewsBean.setReminderId(rs.getLong("reminderId"));
            projectReminderViewsBean.setViewedDate(new Date(rs.getLong("vieweddate")));
            projectReminderViewsBean.setViewedBy(rs.getLong("viewedby"));
            return projectReminderViewsBean;
        }
    };


    @Override
    public Integer getProjectReminderUnViewedCount(final Long userId, final Date date) {
        Integer count = 0;
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT count(*) FROM grailprojectreminders pr");
        sqlBuilder.append(" WHERE ");

        StringBuilder subQueryBuilder = new StringBuilder();
        subQueryBuilder.append("((SELECT count(*) FROM grailprojectreminderviews prv WHERE pr.id = prv.reminderId AND prv.viewedBy = "+userId);
        if(date != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            subQueryBuilder.append(" AND (");
            subQueryBuilder.append("(").append("to_char(to_timestamp(prv.vieweddate/1000),'YYYY')::int =").append(calendar.get(Calendar.YEAR)).append(")");
            subQueryBuilder.append(" AND (").append("to_char(to_timestamp(prv.vieweddate/1000),'mm')::int =").append(calendar.get(Calendar.MONTH)).append(")");
            subQueryBuilder.append(" AND (").append("to_char(to_timestamp(prv.vieweddate/1000),'dd')::int =").append(calendar.get(Calendar.DATE)).append(")");
            subQueryBuilder.append(")");
        }
        subQueryBuilder.append(") <= 0)");
        sqlBuilder.append(subQueryBuilder.toString());
        sqlBuilder.append(" AND pr.isactive=1 AND ((SELECT count(*) FROM grailprojreminderusermapping prum WHERE prum.reminderId = pr.id AND prum.userId = "+userId+") > 0)");

        try{
            count = getSimpleJdbcTemplate().queryForInt(sqlBuilder.toString());
        } catch (DAOException e) {
            LOG.error(e.getMessage());
        }
        return count;
    }

    @Override
    public Integer getGeneralReminderUnViewedCount(final Long userId, final Date date) {
        Integer count = 0;
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT count(*) FROM grailgeneralreminders gr");
        sqlBuilder.append(" WHERE ");

        StringBuilder subQueryBuilder = new StringBuilder();
        subQueryBuilder.append("((SELECT count(*) FROM grailgeneralreminderviews grv WHERE gr.id = grv.reminderId AND grv.viewedBy = "+userId);
        if(date != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            subQueryBuilder.append(" AND (");
            subQueryBuilder.append("(").append("to_char(to_timestamp(grv.vieweddate/1000),'YYYY')::int =").append(calendar.get(Calendar.YEAR)).append(")");
            subQueryBuilder.append(" AND (").append("to_char(to_timestamp(grv.vieweddate/1000),'mm')::int =").append(calendar.get(Calendar.MONTH)).append(")");
            subQueryBuilder.append(" AND (").append("to_char(to_timestamp(grv.vieweddate/1000),'dd')::int =").append(calendar.get(Calendar.DATE)).append(")");
            subQueryBuilder.append(")");
        }
        subQueryBuilder.append(") <= 0)");

        sqlBuilder.append(subQueryBuilder.toString());
        sqlBuilder.append(" AND gr.isactive=1 AND gr.remindTo="+userId);
        try{
            count = getSimpleJdbcTemplate().queryForInt(sqlBuilder.toString());
        } catch (DAOException e) {
            LOG.error(e.getMessage());
        }
        return count;
    }

    @Override
    public void updateProjectReminderViews(final Set<Long> reminderIds, final Long userId) {
        try {
            getSimpleJdbcTemplate().update("DELETE FROM grailprojectreminderviews WHERE viewedby= "+userId+ "AND reminderId in ("+(StringUtils.join(reminderIds, ","))+")");
            StringBuilder insertSqlBuilder = new StringBuilder();
            insertSqlBuilder.append("INSERT INTO grailprojectreminderviews (reminderId,viewedby,vieweddate) VALUES ");
            int iteration = 0;
            for(Long reminderId: reminderIds) {
                if(iteration > 0) {
                    insertSqlBuilder.append(",");
                }
                insertSqlBuilder.append("("+reminderId+","+userId+","+Calendar.getInstance().getTime().getTime()+")");
                iteration++;
            }
            getSimpleJdbcTemplate().update(insertSqlBuilder.toString());
        } catch (DAOException e) {
            LOG.error(e.getMessage());
        }
    }

    @Override
    public void updateGeneralReminderViews(final Set<Long> reminderIds, final Long userId) {
        try {
            getSimpleJdbcTemplate().update("DELETE FROM grailgeneralreminderviews WHERE viewedby= "+userId+ "AND reminderId in ("+(StringUtils.join(reminderIds, ","))+")");
            StringBuilder insertSqlBuilder = new StringBuilder();
            insertSqlBuilder.append("INSERT INTO grailgeneralreminderviews (reminderId,viewedby,vieweddate) VALUES ");
            int iteration = 0;
            for(Long reminderId: reminderIds) {
                if(iteration > 0) {
                    insertSqlBuilder.append(",");
                }
                insertSqlBuilder.append("("+reminderId+","+userId+","+Calendar.getInstance().getTime().getTime()+")");
                iteration++;
            }
            getSimpleJdbcTemplate().update(insertSqlBuilder.toString());
        } catch (DAOException e) {
            LOG.error(e.getMessage());
        }
    }

    @Override
    public void updateProjectReminderStatus(final Long reminderId, final boolean active) {
        try {
            String sql = "UPDATE "+PROJECT_REMINDER_TABLE + " SET isactive = 0 WHERE id =" +reminderId;
            getSimpleJdbcTemplate().update(sql);
        } catch (DAOException e) {
            LOG.error(e.getMessage());
        }
    }

    @Override
    public void updateGeneralReminderStatus(final Long reminderId, final boolean active) {
        try {
            String sql = "UPDATE "+GENERAL_REMINDER_TABLE + " SET isactive = 0 WHERE id =" +reminderId;
            getSimpleJdbcTemplate().update(sql);
        } catch (DAOException e) {
            LOG.error(e.getMessage());
        }
    }

    @Override
    public List<Long> checkForAvailableCategoryTypes(List<Long> categoryTypes, Long userId, Long reminderId) {
        List<Long> availableCategoryTypes = new ArrayList<Long>();
        try {
            if(categoryTypes != null && categoryTypes.size() > 0) {
                StringBuilder sqlBuilder = new StringBuilder();
                sqlBuilder.append("select count(*) from grailprojectreminders where ");
                List<String> categoryTypesFilter = new ArrayList<String>();
                for(Long categoryType :categoryTypes) {
                    categoryTypesFilter.add("("+categoryType+") = ANY(('{' || (categorytypes) || '}')::int[])");
                    sqlBuilder.append("("+StringUtils.join(categoryTypesFilter, " OR ") + ")  AND isactive=1");
                    if(reminderId != null) {
                        sqlBuilder.append(" AND id="+reminderId);
                    }
                    if(userId != null) {
                        sqlBuilder.append(" AND ((SELECT count(*) FROM grailprojreminderusermapping um where um.userId = "+userId+" and um.reminderid = id) > 0)");
                    }
                    Integer count = getSimpleJdbcTemplate().getJdbcOperations().queryForInt(sqlBuilder.toString());
                    if(count > 0) {
                        availableCategoryTypes.add(categoryType);
                    }
                }


            }
        } catch (DAOException e) {
            e.printStackTrace();
            LOG.error(e.getMessage());
        }
        return availableCategoryTypes;
    }

    @Override
    public List<Long> checkForAvailableCategoryTypes(List<Long> categoryTypes, List<Long> userIds, Long reminderId) {
        List<Long> availableCategoryTypes = new ArrayList<Long>();
        try {
            if(categoryTypes != null && categoryTypes.size() > 0) {
                for(Long categoryType :categoryTypes) {
                    StringBuilder sqlBuilder = new StringBuilder();
                    sqlBuilder.append("select count(*) from grailprojectreminders where ");
                    sqlBuilder.append("("+categoryType+") = ANY(('{' || (categorytypes) || '}')::int[]) AND isactive=1");
                    if(reminderId != null) {
                        sqlBuilder.append(" AND id != "+reminderId);
                    }
                    if(userIds != null && userIds.size() > 0) {
                        sqlBuilder.append(" AND ((SELECT count(*) FROM grailprojreminderusermapping um where um.userId in ("+StringUtils.join(userIds,",")+") and um.reminderid = id) > 0)");
                    }
                    Integer count = getSimpleJdbcTemplate().getJdbcOperations().queryForInt(sqlBuilder.toString());
                    if(count > 0) {
                        availableCategoryTypes.add(categoryType);
                    }
                }


            }
        } catch (DAOException e) {
            e.printStackTrace();
            LOG.error(e.getMessage());
        }
        return availableCategoryTypes;
    }

    public SynchroDAOUtil getSynchroDAOUtil() {
        return synchroDAOUtil;
    }

    public void setSynchroDAOUtil(SynchroDAOUtil synchroDAOUtil) {
        this.synchroDAOUtil = synchroDAOUtil;
    }
}
