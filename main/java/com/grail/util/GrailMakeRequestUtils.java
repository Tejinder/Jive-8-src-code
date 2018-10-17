package com.grail.util;

import javax.servlet.ServletContext;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 7/31/14
 * Time: 4:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class GrailMakeRequestUtils {


    public static void processClicks(ServletContext context, Integer submitClicksCounter, Integer makeRequestClicksCounter, Integer grailPortalClicksCounter) {
        String root = context.getRealPath("/");
        File path = new File(root + "/grailbrieftemplate");

        if (!path.exists()) {
            path.mkdirs();
        }
        try {
            File file = new File(path + File.separator + "clicks.txt");
            String line;
            if(file.canRead()) {
                BufferedReader reader = new BufferedReader(new FileReader(file.getAbsolutePath()));
                while ((line=reader.readLine())!=null){
                    if(line.contains("submitbuttonclicks:")) {
                        String countStr = line.replace("submitbuttonclicks:","").trim();
                        if(countStr != null && !countStr.equals("")) {
                            submitClicksCounter = submitClicksCounter + Integer.parseInt(countStr);
                        }
                    } else if(line.contains("makerequestbuttonclicks")) {
                        String countStr = line.replace("makerequestbuttonclicks:","").trim();
                        if(countStr != null && !countStr.equals("")) {
                            makeRequestClicksCounter = makeRequestClicksCounter + Integer.parseInt(countStr);
                        }
                    } else if(line.contains("grailportalclicks")) {
                        String countStr = line.replace("grailportalclicks:","").trim();
                        if(countStr != null && !countStr.equals("")) {
                            grailPortalClicksCounter = grailPortalClicksCounter + Integer.parseInt(countStr);
                        }
                    }
                }
            }

            //writing
            try {

                FileWriter fw = new FileWriter(file);
                BufferedWriter bw = new BufferedWriter (fw);
                PrintWriter fileOut = new PrintWriter (bw);
                fileOut.println ("submitbuttonclicks:"+submitClicksCounter + "\r\n" + "makerequestbuttonclicks:" + makeRequestClicksCounter + "\r\n" + "grailportalclicks:" + grailPortalClicksCounter);
                fileOut.close();
                System.out.println("the file " + file + " is created!");
            }
            catch (Exception e){
                System.out.println(e.toString());
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
