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
    private File file;
    private OutputStream os;
    private DocumentBuilderFactory documentBuilderFactory;
    private DocumentBuilder documentBuilder;
    private Document propsDocument;
    private NodeList nodeList;

    public void setProps() {
        props = new Properties();
        file = new File("C:\\Users\\oguzs\\IdeaProjects\\PracticeRange\\src\\props.xml");

        File lastOpenedFile = new BMParser().getFile();
        System.out.println(lastOpenedFile);

        if (lastOpenedFile != null) {
            props.setProperty("lastOpenedFile", lastOpenedFile.getAbsolutePath());
            try {
                os = new FileOutputStream(file);
                props.storeToXML(os, "WHATATA");

                documentBuilderFactory = DocumentBuilderFactory.newInstance();
                documentBuilder = documentBuilderFactory.newDocumentBuilder();
                propsDocument = documentBuilder.parse(file);


                nodeList = propsDocument.getElementsByTagName("lastFile");
                System.out.println(nodeList.item(0).getTextContent());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
        }
    }

    public Document getProps() {
        return propsDocument;
    }
}
