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
        if (library == null || library.getName().length() < 1) {
            saveLibraryAs(entries);
            return;
        }
        addEntriesToDatabaseAndSave(library, entries);
        Toast.showToast("Library Saved");
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
            BMParser.library = library;
            new BMConfig().setProps(library);
            if (entries.size() > 0) {
                Toast.showToast("Library Saved As");
            }
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
                BMMainScreen.aChangeIsMade = false;
            } catch (IOException e) {
                Toast.showToast("Could Not Save Library");
            } catch (NullPointerException e) {
                BMMainScreen.aChangeIsMade = false;
                Toast.showToast("Library Saved Without Entries");
            }
        }
    }
}
