import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jbibtex.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public class BMParser {
    private FileChooser fileChooser;
    private File file;
    private BufferedReader reader;
    private BibTeXParser bibTeXParser;
    private BibTeXDatabase bibTeXDatabase;
    private Collection<BibTeXEntry> entries;


    public Collection readBibTexLibrary(String filePath) {

        try {
            // A file with the exact location of the bib file is created. The location is stored in bibFilePath
            if (filePath == null) {
                fileChooser = new FileChooser();
                fileChooser.setTitle("Open BibTex Library");

                file = fileChooser.showOpenDialog(BMMain.stage);
            } else {
                file = new File(filePath);
            }

            if (file != null) {
                // A reader created to read the contents of the bib file.
                reader = new BufferedReader(
                        // Just passing the file into the reader makes the reader have all the contents of the file
                        new FileReader(file)
                );

                // BibTex Parser created here.
                bibTeXParser = new org.jbibtex.BibTeXParser(){

                    @Override
                    public void checkStringResolution(org.jbibtex.Key key, org.jbibtex.BibTeXString string){

                        if(string == null){
                            System.err.println("Unresolved string: \"" + key.getValue() + "\"");
                        }
                    }

                    @Override
                    public void checkCrossReferenceResolution(org.jbibtex.Key key, org.jbibtex.BibTeXEntry entry){

                        if(entry == null){
                            System.err.println("Unresolved cross-reference: \"" + key.getValue() + "\"");
                        }
                    }
                };
//                LaTeXParser laTeXParser = new LaTeXParser();
//                laTeXParser.parse(filterReader);

                //Database initialized to the parsed version of the reader/file by using the bibTexParser created above.
                bibTeXDatabase = bibTeXParser.parseFully(reader);

                // Each BibTex Entry can be mapped to their key with the code below.
                // Map<Key, BibTeXEntry> myMap = bibTeXDatabase.getEntries();

                // A BibTex Entry collection is created and it includes all the entries within that specific file
                entries = bibTeXDatabase.getEntries().values();

                // This for loop loops through each entry in the bib file
//            for (BibTeXEntry entry: entries) {
//
//                // @@@ IMPORTANT PART @@@
//                // Every field of each entry is mapped as a key, value pair
//                Map<Key, Value> allFields = entry.getFields();
//                // Type of the entry is printed before each entry
//                System.out.println("Entry Type: " + entry.getType());
//                // For each field in an entry we loop through them and get them as key, value pairs and print them.
//                // author (key) = J. R. R. Tolkien (value)
//                allFields.forEach((key, value) -> System.out.println("\t" + key + " = " + value.toUserString()));
//
//                System.out.println();
//            }
                reader.close();
                new BMConfig().setProps();

                return entries;
            }

        } catch (org.jbibtex.ParseException e) {
            System.out.println(bibTeXParser.getExceptions());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("There is an error related to the bib file. Please check the location of the bib file");
        }

        return null;
    }

    public Collection<BibTeXEntry> getEntriesMap() {
        return entries;
    }

    public BibTeXDatabase getBibTeXDatabase() {
        return bibTeXDatabase;
    }

    public File getFile() {
        return file;
    }
}
