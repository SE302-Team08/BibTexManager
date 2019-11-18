import javafx.stage.FileChooser;
import org.jbibtex.*;

import java.io.BufferedWriter;
import java.io.File;
import java.util.Map;

public class BMFormatter {
    private File file;
    private FileChooser fileChooser;
    private BufferedWriter writer;
    private BibTeXFormatter bibTeXFormatter;
    private BibTeXEntry entry;

    public void addEntryToDatabase(BibTeXDatabase database, Map<Key, Value> entryFields) {

    }

    public void deleteEntryFromDatabase(BibTeXDatabase database, Key entryKey) {

    }
}
