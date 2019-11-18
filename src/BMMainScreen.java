import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.util.Callback;
import org.jbibtex.*;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class BMMainScreen implements Initializable, BMFilter {
    @FXML private Button createButton;
    @FXML private Button deleteButton;
    @FXML private MenuItem addNewEntryMenuItem;
    @FXML private MenuItem openLibraryMenuItem;
    @FXML private MenuItem createLibraryMenuItem;
    @FXML private TableView<Map> tableView;
    @FXML private TableColumn<Map, Integer> numberColumn;
    @FXML private TableColumn<Map, String> entryTypeColumn;
    @FXML private TableColumn<Map, String> authorEditorColumn;
    @FXML private TableColumn<Map, String> titleColumn;
    @FXML private TableColumn<Map, Object> yearColumn;
    @FXML private TableColumn<Map, String> journalBookTitleColumn;
    @FXML private TableColumn<Map, String> bibTexKeyColumn;
    @FXML private TextField searchBar;
    private BMParser parser;
    private BMFormatter formatter;
    private BibTeXDatabase database;
    private boolean aRowIsSelected = false;
    private int currentRowIndex = -1;
    private boolean matchFound = false;
    private Collection<BibTeXEntry> entries;



    public void createNewLibrary() {

    }

    public void addNewEntryMenuItemAction() {

    }

    public void deleteEntry() {

    }

    public void openLibraryMenuItemAction() throws ParseException {
        titleColumn.setCellFactory(TooltippedTableCell.forTableColumn());
        authorEditorColumn.setCellFactory(TooltippedTableCell.forTableColumn());
        journalBookTitleColumn.setCellFactory(TooltippedTableCell.forTableColumn());

        parser = new BMParser();
        entries = parser.readBibTexLibrary();
        database = parser.getBibTeXDatabase();

        if (entries != null) {
            ObservableList<Map> entriesForColumns = FXCollections.observableArrayList();

            Key numberKey = new Key("No");

            numberColumn.setCellValueFactory(new MapValueFactory<>(numberKey));
            titleColumn.setCellValueFactory(new MapValueFactory<>(BibTeXEntry.KEY_TITLE));

            entryTypeColumn.setCellValueFactory(new MapValueFactory<>(BibTeXEntry.KEY_TYPE));
            authorEditorColumn.setCellValueFactory(new MapValueFactory<>(BibTeXEntry.KEY_AUTHOR));

//        authorEditorColumn.setCellValueFactory(new MapValueFactory<>(BibTeXEntry.KEY_EDITOR));
            yearColumn.setCellValueFactory(new MapValueFactory<>(BibTeXEntry.KEY_YEAR));
            journalBookTitleColumn.setCellValueFactory(new MapValueFactory<>(BibTeXEntry.KEY_JOURNAL));

            bibTexKeyColumn.setCellValueFactory(new MapValueFactory<>(BibTeXEntry.KEY_KEY));

            int rowNumber = 1;

            for (BibTeXEntry entry: entries) {
                Map<Key, Object> tempMap = new HashMap<>();
                tempMap.put(numberKey, rowNumber);
                tempMap.put(BibTeXEntry.KEY_TYPE, entry.getType().toString());

                // @@@ IMPORTANT PART @@@
                // Every field of each entry is mapped as a key, value pair
                Map<Key, Value> allFields = entry.getFields();
                allFields.forEach((key, value) -> addEntryFieldsIntoMap(key, value, tempMap));

                tempMap.put(BibTeXEntry.KEY_KEY, entry.getKey().toString());
                entriesForColumns.add(tempMap);

                rowNumber++;
            }

            tableView.setItems(entriesForColumns);
        }
    }

//    private Collection<BibTeXEntry> addEntryMapToTableView()

    private void addEntryFieldsIntoMap(Key key, Value value, Map map) {
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

    private void addEntryFieldsIntoMap(Key key, Value value, Map map, String filter) {
        if (filter == null) {
            if (tableView == null) {

            }
        } else {
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
    }

    public void rowSelected() {
        Map<Key, Value> currentRow;

        if (!aRowIsSelected) {
            currentRow = tableView.getSelectionModel().getSelectedItem();
            currentRowIndex = tableView.getSelectionModel().getFocusedIndex();
            aRowIsSelected = true;
        } else {
            if (tableView.getSelectionModel().isSelected(currentRowIndex)) {
                tableView.getSelectionModel().clearSelection();
                aRowIsSelected = false;
            } else {
                currentRow = tableView.getSelectionModel().getSelectedItem();
                currentRowIndex = tableView.getSelectionModel().getFocusedIndex();
            }
        }
    }

    // REFACTOR THIS PLEASE @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    public void searchInsideMap() {
        if (!searchBar.getText().isEmpty()) {
            String filter = searchBar.getText();

            ObservableList<Map> entriesForColumns = FXCollections.observableArrayList();

            Key numberKey = new Key("No");

            int rowNumber = 1;

            if (entries != null) {
                for (BibTeXEntry entry: entries) {
                    matchFound = false;
                    Map<Key, Object> tempMap = new HashMap<>();


                    // @@@ IMPORTANT PART @@@
                    // Every field of each entry is mapped as a key, value pair
                    Map<Key, Value> allFields = entry.getFields();
                    allFields.forEach((key, value) -> {addEntryFieldsIntoMap(key, value, tempMap, filter);});

                    tempMap.put(numberKey, rowNumber);
                    tempMap.put(BibTeXEntry.KEY_TYPE, entry.getType().toString());
                    tempMap.put(BibTeXEntry.KEY_KEY, entry.getKey().toString());

                    if (entry.getType().toString().toLowerCase().contains(filter.toLowerCase())) {
                        matchFound = true;
                    }

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
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        BMConfig config = new BMConfig();
        BMFormatter bmFormatter = new BMFormatter();
//        bmFormatter.addEntryToEntriesMap();
    }
}
