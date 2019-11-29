import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

public class BMConfig {
    private Properties props;
    private File propsFile;
    private OutputStream os;
    private DocumentBuilderFactory documentBuilderFactory;
    private DocumentBuilder documentBuilder;
    private Document propsDocument;
    private NodeList nodeList;

    public BMConfig() {
        propsFile = new File("C:\\Users\\oguzs\\IdeaProjects\\PracticeRange\\src\\props.xml");
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void setProps(File lastFile) {
        props = new Properties();

        if (lastFile != null) {
            props.setProperty("lastOpenedFile", lastFile.getAbsolutePath());
            try {
                os = new FileOutputStream(propsFile);
                props.storeToXML(os, "User Configuration");

                propsDocument = documentBuilder.parse(propsFile);

                nodeList = propsDocument.getElementsByTagName("entry");
//                System.out.println(nodeList.item(0).getTextContent());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            }
        }
    }

    public Document getProps() {
        try {
            propsDocument = documentBuilder.parse(propsFile);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return propsDocument;
    }
}
