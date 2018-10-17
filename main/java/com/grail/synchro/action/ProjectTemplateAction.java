package com.grail.synchro.action;

import org.apache.log4j.Logger;
import org.springframework.web.bind.ServletRequestDataBinder;

import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectTemplate;
import com.grail.synchro.manager.ProjectManager;
import com.jivesoftware.community.action.JiveActionSupport;
import com.opensymphony.xwork2.Preparable;

/**
 * @author: Kanwar Grewal
 * @version 4.0
 */
public class ProjectTemplateAction extends JiveActionSupport implements Preparable {

    private static final Logger LOGGER = Logger.getLogger(ProjectTemplateAction.class);
    private Project project;
    private ProjectManager synchroProjectManager;
    private String name;
    private Long id;

	public void setName(String name) {
		this.name = name;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void prepare() throws Exception {
    	 this.project = new Project();
            // Apply request binding ONLY if the request is of type POST
            if(getRequest().getMethod().equalsIgnoreCase("POST")){
                ServletRequestDataBinder binder = new ServletRequestDataBinder(this.project);
                binder.bind(getRequest());
                if(binder.getBindingResult().hasErrors()){
                	LOGGER.debug("Error occurred while binding the request object with the Project bean in Create Project Action");
                    input();
                }
            }
        }
    
    
    @Override
    public String input() {
    	//TODO Process INPUT Parameters
    	return INPUT;
    }
    


    public String execute() {
    	 ProjectTemplate template = createTemplate();
    	 if(id!=null && id>0)
    	 {
    		 template.setTemplateID(id);
    	 }
    	 synchroProjectManager.saveTemplate(template);
    	return SUCCESS;
    }
    
    
    private ProjectTemplate createTemplate()
    {
    	ProjectTemplate template = new ProjectTemplate();
    	/*template.setTemplateName(name);    	
    	template.setName(project.getName());
    	template.setDescription(project.getDescription());
    	template.setCategoryType(project.getCategoryType());
    	template.setBrand(project.getBrand());
    	template.setMethodology(project.getMethodology());
    	template.setMethodologyGroup(project.getMethodologyGroup());
    	template.setProposedMethodology(project.getProposedMethodology());
    	template.setEndMarkets(project.getEndMarkets());
    	template.setStartDate(project.getStartDate());
    	template.setEndDate(project.getEndDate());
    	template.setOwnerID(project.getOwnerID());
    	template.setSpi(project.getSpi());*/
    	return template;
    }
    
    
    public void setSynchroProjectManager(ProjectManager synchroProjectManager) {
		this.synchroProjectManager = synchroProjectManager;
	}
    
}
