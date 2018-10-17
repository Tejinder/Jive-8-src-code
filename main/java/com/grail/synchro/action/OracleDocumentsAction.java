package com.grail.synchro.action;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.json.simple.JSONObject;

import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.community.palette.SkinThemeUtils;
import com.jivesoftware.community.theme.ThemeManager;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 3/16/15
 * Time: 12:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class OracleDocumentsAction extends JiveActionSupport {
    private String page;
    private String pageContent = "";
    private Integer documentType = 1;
    private String redirectURL;

    private SkinThemeUtils skinThemeUtils;
    private ThemeManager themeManager;
    private boolean showOwnedByPanel = false;
    private String ownerPanelContent = "";

    private File image;
    private String imageContentType;
    private String imageFileName;

    private InputStream uploadImageStatus;
    private static final String UPLOAD_IMAGE_RESPONSE = "uploadImageResponse";

    private static final Logger logger = Logger.getLogger(OracleDocumentsAction.class);

    @Override
    public String input() {
        BufferedReader br;
        String sCurrentLine;
        try {
            br = new BufferedReader(new FileReader(getFilePath()));
            while ((sCurrentLine = br.readLine()) != null) {
                pageContent += sCurrentLine;
            }

            br = new BufferedReader(new FileReader(getOwnerPanelFilePath()));
            while ((sCurrentLine = br.readLine()) != null) {
                ownerPanelContent += sCurrentLine;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(page.equals("oraclemanuals-ladingpage")) {
            showOwnedByPanel = false;
        } else {
            showOwnedByPanel = true;
        }
        return INPUT;
    }

    private String getOwnerPanelFilePath() {
        String fileName = "";

        if(page.equals("oraclegovernance-ladingpage")) {
            fileName = "oraclegovernance/landing-page-owner-panel-content.html";
        } else if(page.equals("oraclegovernance-legal-disclaimer")) {
            fileName = "oraclegovernance/oraclegovernance-legal-disclaimer-owner-panel-content.html";
        } else if(page.equals("oraclemanuals-4screen")) {
            fileName = "oraclemanuals/oracle-manuals-4screen-owner-panel-content.html";
        }  else if(page.equals("oraclemanuals-4tune")) {
            fileName = "oraclemanuals/oracle-manuals-4tune-owner-panel-content.html";
        } else if(page.equals("oraclemanuals-4cast")) {
            fileName = "oraclemanuals/oracle-manuals-4cast-owner-panel-content.html";
        } else if(page.equals("oraclemanuals-4sight")) {
            fileName = "oraclemanuals/oracle-manuals-4sight-owner-panel-content.html";
        } else if(page.equals("oraclemanuals-4real")) {
            fileName = "oraclemanuals/oracle-manuals-4real-owner-panel-content.html";
        } else if(page.equals("oraclemanuals-consumer-segmentation")) {
            fileName = "oraclemanuals/oracle-manuals-consumer-segmentation-owner-panel-content.html";
        } else if(page.equals("oraclemanuals-market-track")) {
            fileName = "oraclemanuals/oracle-manuals-market-track-owner-panel-content.html";
        } else if(page.equals("oraclemanuals-general-consumer-survey")) {
            fileName = "oraclemanuals/oracle-manuals-general-consumer-survey-owner-panel-content.html";
        } else if(page.equals("oraclemanuals-trade-marketing")) {
            fileName = "oraclemanuals/oracle-manuals-trade-marketing-owner-panel-content.html";
        } else if(page.equals("oraclemanuals-choice-based-conjoint")) {
            fileName = "oraclemanuals/oracle-manuals-choice-based-conjoint-owner-panel-content.html";
        } else if(page.equals("oraclemanuals-communications-toolkit")) {
            fileName = "oraclemanuals/oracle-manuals-communications-toolkit-owner-panel-content.html";
        } else if(page.equals("oraclemanuals-customer-voice")) {
            fileName = "oraclemanuals/oracle-manuals-customer-voice-owner-panel-content.html";
        } else if(page.equals("oraclemanuals-unbranded-product-testing")) {
            fileName = "oraclemanuals/oracle-manuals-unbranded-product-testing-owner-panel-content.html";
        } else if(page.equals("oraclemanuals-branded-product-testing")) {
            fileName = "oraclemanuals/oracle-manuals-branded-product-testing-owner-panel-content.html";
        } else if(page.equals("oraclemanuals-tobacco-flavour-vector")) {
            fileName = "oraclemanuals/oracle-manuals-tobacco-flavour-vector-owner-panel-content.html";
        } else if(page.equals("oraclemanuals-product-dialogue-encounter-workshop")) {
            fileName = "oraclemanuals/oracle-manuals-product-dialogue-encounter-workshop-owner-panel-content.html";
        } else if(page.equals("oraclemanuals-consumer-physical-quality-evaluation")) {
            fileName = "oraclemanuals/oracle-manuals-consumer-physical-quality-evaluation-owner-panel-content.html";
        }  else if(page.equals("oraclemanuals-psychophysical-analysis")) {
            fileName = "oraclemanuals/oracle-manuals-psychophysical-analysis-owner-panel-content.html";
        }

//        if(page.contains("oraclegovernance")) {
//            fileName = "oraclegovernance/owner-panel-content.html";
//        } else if(page.contains("oraclemanuals")) {
//            fileName = "oraclemanuals/owner-panel-content.html";
//        }

        return ServletActionContext.getServletContext().getRealPath("/") + "/oracledocuments/"+fileName;
    }

    private String getFilePath() {
        String fileName = "";


        if(page.equals("oraclegovernance-ladingpage")) {
            fileName = "oraclegovernance/landing-page.html";
        } else if(page.equals("oraclegovernance-legal-disclaimer")) {
            fileName = "oraclegovernance/oraclegovernance-legal-disclaimer.html";
        } else if(page.equals("oraclemanuals-ladingpage")) {
            fileName = "oraclemanuals/landing-page.html";
        } else if(page.equals("oraclemanuals-4screen")) {
            fileName = "oraclemanuals/oracle-manuals-4screen.html";
        }  else if(page.equals("oraclemanuals-4tune")) {
            fileName = "oraclemanuals/oracle-manuals-4tune.html";
        } else if(page.equals("oraclemanuals-4cast")) {
            fileName = "oraclemanuals/oracle-manuals-4cast.html";
        } else if(page.equals("oraclemanuals-4sight")) {
            fileName = "oraclemanuals/oracle-manuals-4sight.html";
        } else if(page.equals("oraclemanuals-4real")) {
            fileName = "oraclemanuals/oracle-manuals-4real.html";
        } else if(page.equals("oraclemanuals-consumer-segmentation")) {
            fileName = "oraclemanuals/oracle-manuals-consumer-segmentation.html";
        } else if(page.equals("oraclemanuals-market-track")) {
            fileName = "oraclemanuals/oracle-manuals-market-track.html";
        } else if(page.equals("oraclemanuals-general-consumer-survey")) {
            fileName = "oraclemanuals/oracle-manuals-general-consumer-survey.html";
        } else if(page.equals("oraclemanuals-trade-marketing")) {
            fileName = "oraclemanuals/oracle-manuals-trade-marketing.html";
        } else if(page.equals("oraclemanuals-choice-based-conjoint")) {
            fileName = "oraclemanuals/oracle-manuals-choice-based-conjoint.html";
        } else if(page.equals("oraclemanuals-communications-toolkit")) {
            fileName = "oraclemanuals/oracle-manuals-communications-toolkit.html";
        } else if(page.equals("oraclemanuals-customer-voice")) {
            fileName = "oraclemanuals/oracle-manuals-customer-voice.html";
        } else if(page.equals("oraclemanuals-unbranded-product-testing")) {
            fileName = "oraclemanuals/oracle-manuals-unbranded-product-testing.html";
        } else if(page.equals("oraclemanuals-branded-product-testing")) {
            fileName = "oraclemanuals/oracle-manuals-branded-product-testing.html";
        } else if(page.equals("oraclemanuals-tobacco-flavour-vector")) {
            fileName = "oraclemanuals/oracle-manuals-tobacco-flavour-vector.html";
        } else if(page.equals("oraclemanuals-product-dialogue-encounter-workshop")) {
            fileName = "oraclemanuals/oracle-manuals-product-dialogue-encounter-workshop.html";
        } else if(page.equals("oraclemanuals-consumer-physical-quality-evaluation")) {
            fileName = "oraclemanuals/oracle-manuals-consumer-physical-quality-evaluation.html";
        }  else if(page.equals("oraclemanuals-psychophysical-analysis")) {
            fileName = "oraclemanuals/oracle-manuals-psychophysical-analysis.html";
        }

        return ServletActionContext.getServletContext().getRealPath("/") + "/oracledocuments/"+fileName;
    }

    @Override
    public String execute() {

        return SUCCESS;
    }

    public String saveOracleDocument() {
        try {
            File file = new File(getFilePath());
            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(pageContent);
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return SUCCESS;
    }

    public String saveOwnerContent() {
        try {
            File file = new File(getOwnerPanelFilePath());
            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(ownerPanelContent);
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return SUCCESS;
    }

    public String updateOwnerImage() {

        Map<String, Object> result = new HashMap<String, Object>();
        JSONObject out = new JSONObject();


        String rootPath =  ServletActionContext.getServletContext().getRealPath("/").concat("oracledocuments/");

        File imagesFolder = new File(rootPath + "/ownerimages");
        if(!imagesFolder.exists()) {
            imagesFolder.mkdir();
        }

        File fileToCreate = new File(imagesFolder, this.imageFileName);
        try {
            FileUtils.copyFile(this.image, fileToCreate);
            result.put("success", true);
            result.put("imagePath", "/oracledocuments/ownerimages/" +this.imageFileName);
            //ServletActionContext.getResponse().getOutputStream().print("<script>top.$('.mce-btn.mce-open').parent().find('.mce-textbox').val('"+fileToCreate.getAbsolutePath()+"').closest('.mce-window').find('.mce-primary').click();</script>");
        } catch (IOException e) {
            result.put("success", false);
        }




        try {
            out.put("data", result);
            uploadImageStatus = new ByteArrayInputStream(out.toString().getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
        	logger.error(e.getMessage());
        }
        return UPLOAD_IMAGE_RESPONSE;
    }


    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getPageContent() {
        return pageContent;
    }

    public void setPageContent(String pageContent) {
        this.pageContent = pageContent;
    }


    public Integer getDocumentType() {
        return documentType;
    }

    public void setDocumentType(Integer documentType) {
        this.documentType = documentType;
    }

    public String getRedirectURL() {
        return redirectURL;
    }

    public void setRedirectURL(String redirectURL) {
        this.redirectURL = redirectURL;
    }

    public boolean isShowOwnedByPanel() {
        return showOwnedByPanel;
    }

    public void setShowOwnedByPanel(boolean showOwnedByPanel) {
        this.showOwnedByPanel = showOwnedByPanel;
    }

    public SkinThemeUtils getSkinThemeUtils() {
        if(skinThemeUtils == null) {
            skinThemeUtils = JiveApplication.getContext().getSpringBean("skinThemeUtils");
        }
        return skinThemeUtils;
    }

    public ThemeManager getThemeManager() {
        if(themeManager == null) {
            themeManager = JiveApplication.getContext().getSpringBean("themeManager");
        }
        return themeManager;
    }

    public void setThemeManager(ThemeManager themeManager) {
        this.themeManager = themeManager;
    }

    public void setSkinThemeUtils(SkinThemeUtils skinThemeUtils) {
        this.skinThemeUtils = skinThemeUtils;
    }

    public String getOwnerPanelContent() {
        return ownerPanelContent;
    }

    public void setOwnerPanelContent(String ownerPanelContent) {
        this.ownerPanelContent = ownerPanelContent;
    }

    public File getImage() {
        return image;
    }

    public void setImage(File image) {
        this.image = image;
    }

    public String getImageContentType() {
        return imageContentType;
    }

    public void setImageContentType(String imageContentType) {
        this.imageContentType = imageContentType;
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
    }

    public InputStream getUploadImageStatus() {
        return uploadImageStatus;
    }

    public void setUploadImageStatus(InputStream uploadImageStatus) {
        this.uploadImageStatus = uploadImageStatus;
    }
}
