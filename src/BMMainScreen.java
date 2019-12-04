import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import org.jbibtex.*;
import org.w3c.dom.Document;

import java.net.URL;
import java.util.*;

public class BMMainScreen implements Initializable, BMFilter {
//    @FXML private Button createButton;
//    @FXML private Button deleteButton;
//    @FXML private MenuItem addNewEntryMenuItem;
//    @FXML private MenuItem openLibraryMenuItem;
//    @FXML private MenuItem createLibraryMenuItem;
    @FXML private TableView<Map> tableView;
    @FXML private TableColumn<Map, Integer> numberColumn;
    @FXML private TableColumn<Map, String> entryTypeColumn;
    @FXML private TableColumn<Map, String> authorEditorColumn;
    @FXML private TableColumn<Map, String> titleColumn;
    @FXML private TableColumn<Map, Object> yearColumn;
    @FXML private TableColumn<Map, String> journalBookTitleColumn;
    @FXML private TableColumn<Map, String> bibTexKeyColumn;
    @FXML private TextField searchBar;
    @FXML private BorderPane mainBorderPane;
    @FXML private GridPane entryEditField;
    @FXML private ChoiceBox entryTypeChoice;
    @FXML private Button confirmButton;
    private BMParser parser;
//    private BMFormatter formatter;
    private BibTeXDatabase database;
    private BMEditEntry bmEditEntry;
    private boolean aRowIsSelected = false;
    private int currentRowIndex = -1;
    private boolean matchFound = false;
    private Collection<BibTeXEntry> entries;
    private ObservableList<Map> entriesForColumns;

    public static CheckBox optionalFields;

//    public void createLibrary() {
//
//    }
//
//    public void addEntry() {
//
//    }
//
//    public void deleteEntry() {
//
//    }

    public void openLibrary() {
        parser = new BMParser();
        entries = parser.readBibTexLibrary(null);
        database = parser.getBibTeXDatabase();

        getEntries("");
    }

    private void addEntryFieldsIntoMap(Key key, Value value, Map<Key, Object> map, String filter) {

        if (!key.getValue().equals("year")) {
            map.put(key, value.toUserString());
            if (value.toUserString().toLowerCase().contains(filter.toLowerCase())) {
                matchFound = true;
            }
        } else {
            try {
                map.put(key, Integer.parseInt(value.toUserString()));
                if (value.toUserString().toLowerCase().contains(filter.toLowerCase())) {
                    matchFound = true;
                }
            } catch (NumberFormatException e) {
                map.put(key, value.toUserString());
            }
        }

    }

    public void searchInsideMap() {
        String searchKeyword;

        if (searchBar.getText() == null)
            searchKeyword = "";
        else
            searchKeyword = searchBar.getText();

        getEntries(searchKeyword);
    }

    private void getEntries(String searchKeyword) {
        entriesForColumns = FXCollections.observableArrayList();

        Key numberKey = new Key("No");

        int rowNumber = 1;

        if (entries != null) {
            for (BibTeXEntry entry: entries) {
                matchFound = false;
                Map<Key, Object> tempMap = new HashMap<>();

                // @@@ IMPORTANT PART @@@
                // Every field of each entry is mapped as a key, value pair
                Map<Key, Value> allFields = entry.getFields();
                allFields.forEach((key, value) -> addEntryFieldsIntoMap(key, value, tempMap, searchKeyword));

                tempMap.put(numberKey, rowNumber);
                tempMap.put(BibTeXEntry.KEY_TYPE, entry.getType().toString());
                tempMap.put(BibTeXEntry.KEY_KEY, entry.getKey().toString());

                if (entry.getType().toString().toLowerCase().contains(searchKeyword.toLowerCase()))
                    matchFound = true;

                if (entry.getKey().toString().toLowerCase().contains(searchKeyword.toLowerCase()))
                    matchFound = true;

                if (matchFound) {
                    entriesForColumns.add(tempMap);
                }

                rowNumber++;
            }
        }
        tableView.getItems().clear();

        if (!entriesForColumns.isEmpty()) {
            tableView.setItems(entriesForColumns);
        }

    }

    public void rowSelected() {
        Map currentRow;

        if (!aRowIsSelected) {
            currentRow = tableView.getSelectionModel().getSelectedItem();
            currentRowIndex = tableView.getSelectionModel().getFocusedIndex();
            aRowIsSelected = true;

            if (currentRow != null) {
                fillEntryEditField(currentRow.entrySet());
                mainBorderPane.setBottom(entryEditField);
            }

        } else {
            if (tableView.getSelectionModel().isSelected(currentRowIndex)) {
                tableView.getSelectionModel().clearSelection();
                mainBorderPane.getChildren().remove(mainBorderPane.getBottom());
                aRowIsSelected = false;
            } else {
                currentRow = tableView.getSelectionModel().getSelectedItem();
                currentRowIndex = tableView.getSelectionModel().getFocusedIndex();

                if (currentRow != null)
                    fillEntryEditField(currentRow.entrySet());

            }
        }
    }

    private void fillEntryEditField(Set currentRowSet) {
        Object[] currentRowArray = currentRowSet.toArray();
        int entryIndex = 0;

        for (int i = 0; i < 5; i++) {
            String currentElement = currentRowArray[i].toString().toLowerCase();
            if (currentElement.contains("no=")) {
                currentElement = currentElement.replace("no=", "");
                entryIndex = Integer.parseInt(currentElement) - 1;
                break;
            }
        }

        bmEditEntry = new BMEditEntry(entryIndex, entriesForColumns.get(entryIndex), entryEditField, entryTypeChoice);
        bmEditEntry.fillEntryEditFields();
    }

    public void confirmChanges() {
        bmEditEntry.changeEntryFields(entriesForColumns);
//        tableView.getItems().clear();
        tableView.setItems(entriesForColumns);
    }

    public void optionalFieldsSelected() {
        optionalFields.setSelected(!optionalFields.isSelected());
        if (tableView.getSelectionModel().getSelectedItem() != null) {
            fillEntryEditField(tableView.getSelectionModel().getSelectedItem().entrySet());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mainBorderPane.getChildren().remove(mainBorderPane.getBottom());

        titleColumn.setCellFactory(TooltippedTableCell.forTableColumn());
        authorEditorColumn.setCellFactory(TooltippedTableCell.forTableColumn());
        journalBookTitleColumn.setCellFactory(TooltippedTableCell.forTableColumn());

        Key numberKey = new Key("No");
        numberColumn.setCellValueFactory(new MapValueFactory<>(numberKey));
        titleColumn.setCellValueFactory(new MapValueFactory<>(BibTeXEntry.KEY_TITLE));

        entryTypeColumn.setCellValueFactory(new MapValueFactory<>(BibTeXEntry.KEY_TYPE));
        authorEditorColumn.setCellValueFactory(new MapValueFactory<>(BibTeXEntry.KEY_AUTHOR));

//        authorEditorColumn.setCellValueFactory(new MapValueFactory<>(BibTeXEntry.KEY_EDITOR));
        yearColumn.setCellValueFactory(new MapValueFactory<>(BibTeXEntry.KEY_YEAR));
        journalBookTitleColumn.setCellValueFactory(new MapValueFactory<>(BibTeXEntry.KEY_JOURNAL));

        bibTexKeyColumn.setCellValueFactory(new MapValueFactory<>(BibTeXEntry.KEY_KEY));

        BMConfig config = new BMConfig();
        Document propsDocument = config.getProps();
        if (propsDocument != null) {
            entries = new BMParser().readBibTexLibrary(propsDocument.getElementsByTagName("entry").item(0).getTextContent());
            getEntries("");
        }

        optionalFields = new CheckBox();
        entryTypeChoice.getItems().addAll(FXCollections.observableArrayList(BMEntry.TYPES));

//        BMFormatter bmFormatter = new BMFormatter();
//        bmFormatter.addEntryToEntriesMap();
    }
}
