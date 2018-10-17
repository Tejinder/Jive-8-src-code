package com.jivesoftware.community.browse.sort;

import com.jivesoftware.community.browse.QueryFilterDef;

public class MostRatedDocumentsSort extends AbstractBrowseSort {
    public MostRatedDocumentsSort() {
        super("browse.sorts.by_rating.desc", SortOrder.DESCENDING);
    }

    public MostRatedDocumentsSort(String label, SortOrder defaultOrder) {
        super("ratedDocs", defaultOrder);
        setLabel(label);
    }

    @Override
    public String getColumnName(QueryFilterDef.Archetype archetype) {
        StringBuilder builder = new StringBuilder();
        return builder.append("rating").toString();
    }

}
