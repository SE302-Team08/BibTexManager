import javafx.stage.FileChooser;
import org.jbibtex.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class BMParser {
    static File library;
    private Collection<BibTeXEntry> entries;
    private ArrayList<Map<Key, Object>> entriesList;

    public ArrayList<Map<Key, Object>> readBibTexLibrary(String filePath) {
        entriesList = new ArrayList<>();

        try {
            // A file with the exact location of the bib file is created. The location is stored in bibFilePath
            if (filePath == null) {

                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Open BibTex Library");

                library = fileChooser.showOpenDialog(BMMain.stage);
            } else {
                library = new File(filePath);
            }

            if (library != null) {
                // A reader created to read the contents of the bib file.
                // Just passing the file into the reader makes the reader have all the contents of the file
                BufferedReader reader = new BufferedReader(
                        // Just passing the file into the reader makes the reader have all the contents of the file
                        new FileReader(library)
                );

                // BibTex Parser created here.
                BibTeXParser bibTeXParser = new BibTeXParser();

                //Database initialized to the parsed version of the reader/file by using the bibTexParser created above.
                BibTeXDatabase bibTeXDatabase = bibTeXParser.parseFully(reader);

                // Each BibTex Entry can be mapped to their key with the code below.
                // Map<Key, BibTeXEntry> myMap = bibTeXDatabase.getEntries();

                // A BibTex Entry collection is created and it includes all the entries within that specific file
                entries = bibTeXDatabase.getEntries().values();

//            }
                reader.close();
                new BMConfig().setProps(library);
                getEntries();

                return entriesList;
            }

        } catch (org.jbibtex.ParseException e) {
            Toast.showToast("File is corrupted");
        } catch (IOException e) {
            System.err.println("There is an error related to the bib file. Please check the location of the bib file");
        }

        return null;
    }

    private void addEntryFieldsIntoMap(Key key, Value value, Map<Key, Object> map) {
        if (!key.getValue().equals("year")) {
            map.put(key, value.toUserString());
        } else {
            try {
                map.put(key, Integer.parseInt(value.toUserString()));
            } catch (NumberFormatException e) {
                map.put(key, value.toUserString());
            }
        }
    }

    private void getEntries() {
        Key numberKey = new Key("rownumber");

        int rowNumber = 1;

        if (entries != null) {
            for (BibTeXEntry entry: entries) {
                Map<Key, Object> tempMap = new HashMap<>();
                tempMap.put(numberKey, rowNumber);

                Map<Key, Value> allFields = entry.getFields();
                allFields.forEach((key, value) -> addEntryFieldsIntoMap(key, value, tempMap));

                tempMap.put(BibTeXEntry.KEY_TYPE, entry.getType().toString());
                tempMap.put(BibTeXEntry.KEY_KEY, entry.getKey().toString());

                entriesList.add(tempMap);
                rowNumber++;
            }
        }
    }
}
