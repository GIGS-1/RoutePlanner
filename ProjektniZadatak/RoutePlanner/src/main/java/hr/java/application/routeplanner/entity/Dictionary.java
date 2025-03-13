package hr.java.application.routeplanner.entity;

import com.github.vincentrussell.ini.Ini;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Dictionary {
    public static String get(String id) throws ParserConfigurationException, IOException, SAXException {
        File fXmlFile = new File("dat/dictionary.xml");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        doc.getDocumentElement().normalize();

        FileInputStream ulazIni = new FileInputStream("dat/settings.ini");
        Ini ini = new Ini();
        ini.load(ulazIni);
        String lang = (String) ini.getValue("Language", "lang");

        Element e = (Element) doc.getElementsByTagName(id).item(0);
        return e.getElementsByTagName(lang).item(0).getTextContent();
    }
}
