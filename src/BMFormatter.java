import javafx.stage.FileChooser;
import org.jbibtex.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class BMFormatter {
    public static void saveLibrary(ArrayList<Map<Key, Object>> entries) {
        File library = BMParser.library;
        addEntriesToDatabaseAndSave(library, entries);
    }

    public static void saveLibraryAs(ArrayList<Map<Key, Object>> entries) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilterBIB = new FileChooser.ExtensionFilter("bib files (*.bib)", "*.bib");
        FileChooser.ExtensionFilter extFilterTXT = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilterBIB);
        fileChooser.getExtensionFilters().add(extFilterTXT);
        fileChooser.setTitle("Save BibTex Library");

        File library = fileChooser.showSaveDialog(BMMain.stage);

        if (library != null) {
            addEntriesToDatabaseAndSave(library, entries);
        } else {
            Toast.showToast("No File Selected");
        }
    }

    private static void addEntriesToDatabaseAndSave(File library, ArrayList<Map<Key, Object>> entries) {
        if (library != null) {
            try {
                BufferedWriter writer = new BufferedWriter(
                        new FileWriter(library, false)
                );

                BibTeXDatabase database = new BibTeXDatabase();
                BibTeXFormatter formatter = new BibTeXFormatter();
                for (Map<Key, Object> entry: entries) {
                    Key type = new Key((String) entry.get(BibTeXEntry.KEY_TYPE));
                    Key entryKey = new Key((String) entry.get(BibTeXEntry.KEY_KEY));

                    BibTeXEntry newEntry = new BibTeXEntry(type, entryKey);

                    for (Key key: entry.keySet()) {
                        if (!key.toString().equals("key") && !key.toString().equals("type")) {
                            newEntry.addField(key, new StringValue(entry.get(key).toString(), StringValue.Style.QUOTED));
                        }
                    }
                    database.addObject(newEntry);
                }
                formatter.format(database, writer);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
