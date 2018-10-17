package com.grail.synchro.util;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;

public class SynchroSheetStylingUtil {
	

	public static HSSFSheet mergeCells (HSSFSheet sheet, int row_start, int row_end, int col_start, int col_end)
	{
		sheet.addMergedRegion(new CellRangeAddress(
				row_start, //first row (0-based)
				row_end, //last row  (0-based)
				col_start, //first column (0-based)
				col_end  //last column  (0-based)
        ));
		return sheet;
	}
	
	public static HSSFCellStyle getHeadingStyle(HSSFWorkbook hwb, String border)
	{
		HSSFCellStyle style = hwb.createCellStyle();      
    	style.setAlignment(CellStyle.ALIGN_CENTER);

    	/*Cell Font Setting*/
        HSSFFont font = hwb.createFont();
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short) 10);
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);      
        style.setFont(font);

        /*Border*/
        if(border.length()==4)
        {
        	if(border.charAt(0) == '1')
	        style.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
        	
        	if(border.charAt(1) == '1')
	        style.setBorderRight(HSSFCellStyle.BORDER_MEDIUM );
        	
        	if(border.charAt(2) == '1')
	        style.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM );
        	
        	if(border.charAt(3) == '1')
	        style.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM );
        }
        return style;
		
	}
	
	public static HSSFCellStyle getOrientedHeadingStyle(HSSFWorkbook hwb, String border)
	{
		 HSSFCellStyle style = getHeadingStyle(hwb, border);
		 style.setRotation((short)45);
        return style;
		
	}
	
	public static HSSFCellStyle getCellStyle(HSSFWorkbook hwb, String border)
	{
		HSSFCellStyle style = hwb.createCellStyle();      
    	style.setAlignment(CellStyle.ALIGN_LEFT);

    	/*Cell Font Setting*/
        HSSFFont font = hwb.createFont();
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        
        /*Text Wrap*/
        style.setWrapText(true);
        
        /*Border*/
        if(border.length()==4)
        {
        	if(border.charAt(0) == '1')
	        style.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
        	
        	if(border.charAt(1) == '1')
	        style.setBorderRight(HSSFCellStyle.BORDER_MEDIUM );
        	
        	if(border.charAt(2) == '1')
	        style.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM );
        	
        	if(border.charAt(3) == '1')
	        style.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM );
        }
        
        return style;
	} 
	
	public static HSSFCellStyle getCellStyleItalic(HSSFWorkbook hwb, String border)
	{
		HSSFCellStyle style =getCellStyle (hwb, border);
		style.getFont(hwb).setItalic(true);
		return style;
	} 
	
}
