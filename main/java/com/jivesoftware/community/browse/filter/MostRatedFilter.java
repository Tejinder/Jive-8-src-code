package com.jivesoftware.community.browse.filter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.jivesoftware.community.JiveConstants;
import com.jivesoftware.community.JiveContentObject;
import com.jivesoftware.community.browse.QueryFilterDef;
import com.jivesoftware.community.browse.TableJoin;
import com.jivesoftware.community.browse.provider.DocumentBrowseFilterProvider;
import com.jivesoftware.community.work.ContentStatusFilter;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MostRatedFilter extends ContentStatusFilter {

    public MostRatedFilter(String description) {
        super("mostrated", description ,JiveContentObject.Status.PUBLISHED);
    }

    @Override
    public boolean isSearchable() {
        return false;
    }

    @Override
    protected void appendCustomStatusPredicate() {
        predicate
                .append(" OR (")
                .append(alias)
                .append(".objectType=")
                .append(JiveConstants.DOCUMENT)
                .append(" AND ")
                .append(" EXISTS (SELECT v.internalDocID FROM jiveDocument d INNER JOIN jiveDocVersion v ON d.internalDocID = v.internalDocID ")
                .append("WHERE d.internalDocID = ")
                .append(alias)
                .append(".objectID ")
                .append("AND v.versionID IN (SELECT MAX(versionID) FROM jiveDocVersion v2 WHERE v2.internalDocID = v.internalDocID) ")
                .append("AND v.status IN ").append(toInClause(statuses));

        predicate.append(") )");
    }

    private String toInClause(Collection<Integer> statuses) {
        StringBuilder inclause = new StringBuilder();
        inclause.append("(");
        Iterator<Integer> iter = statuses.iterator();
        while (iter.hasNext()) {
            inclause.append(iter.next());
            if (iter.hasNext()) {
                inclause.append(",");
            }
        }
        inclause.append(")");
        return inclause.toString();
    }


    @Override
    public QueryFilterDef getQueryFilterDef(Set<BrowseFilter> browseFilters, QueryFilterDef.Archetype archetype) {

        if (archetype == QueryFilterDef.Archetype.Container) {
            throw new UnsupportedOperationException();
        }
        alias = QueryFilterDef.getBrowseTableAlias(archetype);
        predicate = new StringBuilder();
        paramTypes = Lists.newArrayList();
        paramValues = Lists.newArrayList();

        if (statuses != null) {
            predicate.append(" (");
            Iterator<Integer> statusIDIterator = statuses.iterator();
            while (statusIDIterator.hasNext()) {
                Integer statusID = statusIDIterator.next();
                predicate.append(alias).append(".status=?");
                paramTypes.add(QueryFilterDef.ParamType.INTEGER);
                paramValues.add(statusID);
                if (statusIDIterator.hasNext()) {
                    predicate.append(" OR ");
                }
            }
            appendCustomStatusPredicate();
            predicate.append(" ) ");
        }




       /* return new QueryFilterDef(null, null, null, predicate.toString(), ImmutableList.copyOf(paramTypes),
                ImmutableList.copyOf(paramValues));*/
        
        return new QueryFilterDef(null, predicate.toString(), ImmutableList.copyOf(paramTypes),
                ImmutableList.copyOf(paramValues));
     

    }
}
