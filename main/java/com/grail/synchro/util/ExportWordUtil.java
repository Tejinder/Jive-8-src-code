package com.grail.synchro.util;


import org.docx4j.XmlUtils;
import org.docx4j.convert.in.xhtml.XHTMLImporter;
import org.docx4j.convert.in.xhtml.XHTMLImporterImpl;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.AltChunkType;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.*;



/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 5/14/14
 * Time: 7:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExportWordUtil {

	public static void createPageHeader(WordprocessingMLPackage wordMLPackage, String content) {
        ObjectFactory factory = Context.getWmlObjectFactory();

        P header = factory.createP();

        BooleanDefaultTrue booleanDefaultTrue = new BooleanDefaultTrue();
        booleanDefaultTrue.setVal(true);

        PPr pPr = factory.createPPr();


        Text text = factory.createText();
        text.setValue(content);

        CTBorder border = new CTBorder();
        border.setVal(STBorder.NONE);

        PPrBase.PBdr pBdr = factory.createPPrBasePBdr();
        pBdr.setTop(border);
        pBdr.setRight(border);
        pBdr.setBottom(border);
        pBdr.setLeft(border);
        pPr.setPBdr(pBdr);

        Jc justification = factory.createJc();
        justification.setVal(JcEnumeration.CENTER);
        pPr.setJc(justification);

        header.setPPr(pPr);

        R run = factory.createR();
        run.getRunContent().add(text);

        RPr rPr = factory.createRPr();

        // Set font size
        HpsMeasure fontSize = new HpsMeasure();
        fontSize.setVal((new java.math.BigInteger("30")));
        rPr.setSz(fontSize);

        // Set Bold
        rPr.setB(booleanDefaultTrue);

        run.setRPr(rPr);

        header.getParagraphContent().add(run);
        wordMLPackage.getMainDocumentPart().addObject(header);
    }


    public static void createContentHeader(WordprocessingMLPackage wordMLPackage, String content) {
        ObjectFactory factory = Context.getWmlObjectFactory();

        BooleanDefaultTrue booleanDefaultTrue = new BooleanDefaultTrue();
        booleanDefaultTrue.setVal(true);

        CTBorder border = factory.createCTBorder();
        border.setVal(org.docx4j.wml.STBorder.SINGLE);
        border.setSz(new java.math.BigInteger("6"));
        border.setSpace(new java.math.BigInteger("1"));
        border.setColor("blue");

        PPrBase.PBdr pBdr = factory.createPPrBasePBdr();
        pBdr.setTop(border);
        pBdr.setRight(border);
        pBdr.setBottom(border);
        pBdr.setLeft(border);

        PPr pPr = factory.createPPr();
        pPr.setPBdr(pBdr);
        CTShd shd = factory.createCTShd();
        shd.setFill("e9e9e9");
        pPr.setShd(shd);

        P paragraph = factory.createP();
        paragraph.setPPr(pPr);


        Text text = factory.createText();
        text.setValue(content);

        R run = factory.createR();
        run.getRunContent().add(text);

        RPr rPr = factory.createRPr();
        // Set Color
        Color color = new Color();
        color.setVal("DF0101");
        rPr.setColor(color);

        // Set font size
        HpsMeasure fontSize = new HpsMeasure();
        fontSize.setVal((new java.math.BigInteger("23")));
        rPr.setSz(fontSize);

        // Set Bold
        rPr.setB(booleanDefaultTrue);


        run.setRPr(rPr);


        paragraph.getParagraphContent().add(run);
        wordMLPackage.getMainDocumentPart().getContent().add(paragraph);
    }

    public static void createContent(WordprocessingMLPackage wordMLPackage, String content) throws Docx4JException {

        ObjectFactory factory = Context.getWmlObjectFactory();
        P paragraph = factory.createP();

        R run = factory.createR();
        XHTMLImporterImpl xhtmlImporter = new XHTMLImporterImpl(wordMLPackage);
       // run.getRunContent().addAll(xhtmlImporter.convert("<body>"+ content + "</body>", null));

       // paragraph.getParagraphContent().add(run);
     //   wordMLPackage.getMainDocumentPart().getContent().add(paragraph);
        wordMLPackage.getMainDocumentPart().getContent().addAll( 
        		xhtmlImporter.convert("<body>"+ content + "</body>", null));
    }
}
