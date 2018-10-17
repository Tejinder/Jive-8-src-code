package com.grail.kantar.util;

import javax.servlet.ServletContext;
import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 10/21/14
 * Time: 6:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class KantarMakeRequestUtils {

    public static void processClicks(ServletContext context, Integer submitClicksCounter, Integer makeRequestClicksCounter, Integer kantarPortalClicksCounter) {
        String root = context.getRealPath("/");
        File path = new File(root + "/kantarbrieftemplate");

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
                    } else if(line.contains("kantarportalclicks")) {
                        String countStr = line.replace("kantarportalclicks:","").trim();
                        if(countStr != null && !countStr.equals("")) {
                            kantarPortalClicksCounter = kantarPortalClicksCounter + Integer.parseInt(countStr);
                        }
                    }
                }
            }

            //writing
            try {

                FileWriter fw = new FileWriter(file);
                BufferedWriter bw = new BufferedWriter (fw);
                PrintWriter fileOut = new PrintWriter (bw);
                fileOut.println ("submitbuttonclicks:"+submitClicksCounter + "\r\n" + "makerequestbuttonclicks:" + makeRequestClicksCounter + "\r\n" + "kantarportalclicks:" + kantarPortalClicksCounter);
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
