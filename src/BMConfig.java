import org.w3c.dom.Document;
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
    private File propsFile;
    private DocumentBuilder documentBuilder;
    private Document propsDocument;

    public BMConfig() {
        final String dir = System.getProperty("user.dir");
        propsFile = new File(dir + "/props.xml");

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            propsFile.createNewFile();
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException | IOException e) {
            e.printStackTrace();
        }
    }

    public void setProps(File lastFile) {
        Properties props = new Properties();

        if (lastFile != null) {
            props.setProperty("lastOpenedFile", lastFile.getAbsolutePath());
            try {
                OutputStream os = new FileOutputStream(propsFile);
                props.storeToXML(os, "User Configuration");

                propsDocument = documentBuilder.parse(propsFile);
            } catch (IOException | SAXException e) {
                e.printStackTrace();
            }
        } else {
            props.setProperty("lastOpenedFile", "");
            try {
                OutputStream os = new FileOutputStream(propsFile);
                props.storeToXML(os, "User Configuration");

                propsDocument = documentBuilder.parse(propsFile);
            } catch (IOException | SAXException e) {
                e.printStackTrace();
            }
        }
    }

    public Document getProps() {
        try {
            propsDocument = documentBuilder.parse(propsFile);
        } catch (IOException | SAXException e) {
            e.printStackTrace();
        }
        return propsDocument;
    }
}
