package com.grail.widget;

import com.jivesoftware.community.annotations.PropertyNames;
import com.jivesoftware.community.widget.*;

import java.util.Map;

@WidgetTypeMarker({WidgetType.COMMUNITY,
        WidgetType.HOMEPAGE})
@WidgetCategoryMarker({WidgetCategory.OTHER})
public class QuickSearchDocumentsWidget extends BaseWidget {

    private static final String FREEMARKER_FILE = "/template/widget/quick-search-documents.ftl";

    @Override
    public String getTitle(WidgetContext widgetContext) {
        return "Quick Search";
    }

    @Override
    public String getDescription(WidgetContext widgetContext) {
        return "This used search documents based on document extended properties.";
    }

    @Override
    public String render(WidgetContext widgetContext, ContainerSize size) {
        return applyFreemarkerTemplate(widgetContext, size, FREEMARKER_FILE);
    }

    @Override
    protected Map<String, Object> loadProperties(WidgetContext widgetContext, ContainerSize size) {
        Map<String, Object> properties = super.loadProperties(widgetContext, size);

        properties.put("sizeId", size.getID());
        properties.put("widgetFrameId", getWidgetFrameID());
        properties.put("widgetType", widgetContext.getWidgetType().getKey());
        properties.put("containerId", widgetContext.getContainer().getID());
        properties.put("containerType", widgetContext.getContainer().getObjectType());

        return properties;
    }
}
