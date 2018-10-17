package com.grail.synchro.manager;

import java.math.BigDecimal;
import java.util.List;

import com.grail.synchro.beans.*;
import com.grail.synchro.search.filter.StandardReportFilter;
import com.jivesoftware.base.User;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;


/**
 * @author: Tejinder
 * @since: 1.0
 */

public interface SynchroReportManagerNew {

    List<Long> getAgencyEvaluationReport(final EvaluationAgencyReportFiltersNew evaluationAgencyReportFilters);

}
