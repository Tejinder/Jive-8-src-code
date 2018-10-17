package com.grail.widget;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.google.common.collect.Lists;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.jivesoftware.community.Community;
import com.jivesoftware.community.ContainerAwareEntityDescriptor;
import com.jivesoftware.community.Document;
import com.jivesoftware.community.DocumentManager;
import com.jivesoftware.community.DocumentResultFilter;
import com.jivesoftware.community.JiveConstants;
import com.jivesoftware.community.JiveContainer;
import com.jivesoftware.community.JiveContainerManager;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.JiveIterator;
import com.jivesoftware.community.ResultFilter;
import com.jivesoftware.community.annotations.PropertyNames;
import com.jivesoftware.community.rating.RatingManager;
import com.jivesoftware.community.widget.BaseLocationFilterableWidget;
import com.jivesoftware.community.widget.WidgetCategory;
import com.jivesoftware.community.widget.WidgetCategoryMarker;
import com.jivesoftware.community.widget.WidgetContext;
import com.jivesoftware.community.widget.WidgetType;
import com.jivesoftware.community.widget.WidgetTypeMarker;
//import com.jivesoftware.community.impl.ListJiveIterator;

/**
 * User: Leo
 * Date: Jan 29, 2010
 * Time: 10:58:45 AM
 */

@WidgetTypeMarker({WidgetType.COMMUNITY,
WidgetType.HOMEPAGE})
@WidgetCategoryMarker({WidgetCategory.CONTENT})
@PropertyNames({"numResults"})
public class MostRatedDocumentsWidget extends BaseLocationFilterableWidget {

    private static final Logger LOG = LogManager.getLogger(MostRatedDocumentsWidget.class.getName());

    public static final int DEFAULT_MAX_RESULTS = 100;
    public static final String MAX_RESULT_OVERRIDE_PROPERTY = "jive.mostRatedDocumentsWidget.maxResults";
    // FreeMarker template for rendering preview and published widget.
    private static final String FREEMARKER_FILE = "/template/widget/most-rated-documents.ftl";

    protected long containerID = -1;
    protected int containerType = -1;

    private int numResults = 5;
   // private RatingManager ratingManager;
  //  private CommunityManager communityManager;
    private DocumentManager documentManager;
    protected JiveContainerManager containerManager;
    private RatingManager ratingManager;
    private boolean moreResultsAvailable;

    public void setRatingManager(RatingManager ratingManager) {
        this.ratingManager = ratingManager;
    }

    /*public void setCommunityManager(CommunityManager communityManager) {
        this.communityManager = communityManager;
    } */

    public void setContainerManager(JiveContainerManager containerManager) {
        this.containerManager = containerManager;
    }

    public void setDocumentManager(DocumentManager documentManager) {
        this.documentManager = documentManager;
    }

    public boolean isMoreResultsAvailable() {
        return moreResultsAvailable;
    }

    public void setMoreResultsAvailable(boolean moreResultsAvailable) {
        this.moreResultsAvailable = moreResultsAvailable;
    }

    /**
     * Called to get the description that should be displayed for
     * the widget when the user hovers over it in the "customize" mode list of
     * widgets.
     *
     * @param widgetContext Context in which the widget instance is executing.
     */
    public String getDescription(WidgetContext widgetContext) {
        return "Lists the most average rated documents in the community and it's sub community.";
    } 

    /**
     * Called to get the widget's default title. The user will be
     * able to change this. If they do, their new title will be set with a call
     * to the final method BaseWidget.setCustomTitle.
     *
     * @param widgetContext Context in which the widget instance is executing.
     */
    public String getTitle(WidgetContext widgetContext) {
        return "Best Rated Documents";
    }

    /**
     * Called to get the HTML used to display the widget when it's
     * previewed or published.
     *
     * @param widgetContext Context in which the widget instance is executing.
     * @param containerSize An enum constant representing the size of the widget
     *                      instance's current container: LARGE or SMALL.
     */
    public String render(WidgetContext widgetContext,
                         ContainerSize containerSize) {
        // Process the included FTL file to render the HTML for display.
        return applyFreemarkerTemplate(widgetContext, containerSize,
                FREEMARKER_FILE);
    }

    /**
     * Called to get properties for use in your FTL file. These
     * will be added to the FreeMarker context.
     *
     * @param widgetContext Context in which the widget instance is executing.
     * @param size          An enum constant representing the size of the widget
     *                      instance's current container: LARGE or SMALL.
     * @return A map of the properties and their values.
     */
    protected Map<String, Object> loadProperties(WidgetContext widgetContext,
                                                 ContainerSize size) {
        // First load existing properties.
        Map<String, Object> properties = super.loadProperties(widgetContext, size);

        LOG.debug("WidgetType :::          "+widgetContext.getWidgetType());
        if (widgetContext.getWidgetType() == WidgetType.COMMUNITY ||
                widgetContext.getWidgetType() == WidgetType.HOMEPAGE 
               // || widgetContext.getWidgetType() == WidgetType.PERSONALIZEDHOMEPAGE
                ) {

            //get the community, project, or group if this is a container widget
            JiveContainer container = getContainer(widgetContext);




            //if containerType is set to project or group containerID will not be null because of validation
            if(container == null) {
                container = communityManager.getRootCommunity();
            }

            containerID = container.getID();
            containerType = container.getObjectType();

            List<Document> mostRatedDocuments = getSortedDocList((Community) container);


            List<Document> list = new LinkedList<Document>();
            int docsSize = 0;
	           for(int i=0;i<mostRatedDocuments.size();i++)
	           {
	        	   docsSize++;
	               if(docsSize <= getNumResults()) {
	                   list.add(mostRatedDocuments.get(i));
	               } else {
	                   break;
	               }
	           }
            
            /*  while(mostRatedDocuments.hasNext()) {
                docsSize++;
                if(docsSize <= getNumResults()) {
                    list.add(mostRatedDocuments.next());
                } else {
                    break;
                }
            }*/
            this.moreResultsAvailable = docsSize > getNumResults();
            // Get the name of the community this instance is in, then add it as a
            // property.
            String communityName = ((Community)container).getDisplayName();

            String userName = widgetContext.getUser().getName();
            properties.put("communityName", communityName);
            properties.put("userName", userName);
            properties.put("container", container);
            properties.put("numResults", numResults);
            properties.put("documents", list);
            properties.put("isMostRated", true );
            properties.put("moreResultsAvailable", this.moreResultsAvailable);
            //this.moreResultsAvailable = recentDocs.hasMoreThanNeeded();
            properties.put("isRoot", Boolean.valueOf(isRootCommunity(container)));

        }
        return properties;
    }

    private List<Document> getSortedDocList(Community community) {

       DocumentResultFilter resultFilter = DocumentResultFilter.createDefaultFilter();
       resultFilter.setRestrictToLatestVersion(true);    
       resultFilter.setFromMostRatedWidget(true);
       resultFilter.setSortField(JiveConstants.RATING);
       resultFilter.setSortOrder(ResultFilter.DESCENDING);
       resultFilter.setNumResults(getNumResults() + 25);

       // return documentManager.getDocuments(community,resultFilter);
       
       Iterable<ContainerAwareEntityDescriptor> documents = documentManager.getDocumentsAsContainerAwareEntityDescriptors(community, resultFilter);
       List<Long> docIdList = Lists.newLinkedList();
       for (ContainerAwareEntityDescriptor doc : documents) {
    	   docIdList.add(doc.getID());
       }
       List<Document> documentsList = documentManager.getDocuments(docIdList);
       return documentsList;
    }
    
  /*  public int getNumResults() {
        return numResults;
    }

    public void setNumResults(int numResults) {
        if (numResults > 0) {
            this.numResults = numResults;
        }
    }    */
      public String validateWidget() {
        String message ="";

           if ( this.numResults > JiveGlobals.getJiveIntProperty(MAX_RESULT_OVERRIDE_PROPERTY, DEFAULT_MAX_RESULTS) ) {
            try {

                message = "Too many results";
            } catch (Throwable e) {
                message = "Your number of results must not exceed " + JiveGlobals.getJiveIntProperty(MAX_RESULT_OVERRIDE_PROPERTY, DEFAULT_MAX_RESULTS) + ".";
            }
            return message;
        }

        if ( this.numResults < 1 ) {
            try {
                message = "Too few results";
                
            } catch (Throwable e) {
                message = "You must specify a non-zero number of results.";
            }
            return message;
        }

        return null;
    }


    private static class MostRatedLimitingIterator implements Iterable<Document>, Iterator<Document> {
        private final Iterable<Document> documents;
        private int limit;
        private int position;

        MostRatedLimitingIterator(Iterable<Document> documents, int limit) {
            this.documents = documents;
            this.limit = limit;
        }

        public boolean hasNext()
        {
           // return (this.position < this.limit) && (this.documents.hasNext());
        	return false;
        }

        public Document next() {
          //  Document next = (Document)this.documents.next();
           // this.position += 1;
           // return next;
        	return null;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        public Iterator<Document> iterator() {
            return this;
        }

        public boolean hasMoreThanNeeded()
        {
            if (this.position > 0) {
                throw new IllegalStateException();
            }

            boolean hasMore = false;
            long firstID = 0L;
            int docCount = 0;

            for (Document doc : this.documents) {
                docCount++;
                if (firstID == 0L) {
                    firstID = doc.getID();
                }

                if (docCount >= this.limit) {
                    hasMore = true;
                    break;
                }
            }

//            if (docCount > 0) {
//                this.documents.setIndex(firstID);
//            }

            return hasMore;
        }
    }
}