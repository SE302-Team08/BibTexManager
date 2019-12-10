import javafx.beans.value.ObservableValue;
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
    @FXML private ChoiceBox entryTypeChoiceBox;
    @FXML private Button confirmButton;
    //    private BMFormatter formatter;
    private BibTeXDatabase database;
    private BMEditEntry bmEditEntry;
    private boolean aRowIsSelected = false;
    private int currentRowIndex = -1;
    private List<Map<Key, Object>> entries;
    private String searchKeyword = "";
    private Map currentRow;
    private boolean keepLastDeletedEntryFields = false;
    public static CheckBox optionalFields;

//    public void createLibrary() {
//
//    }
//
    public void addEntry() {
        confirmButton.setText("Add");
        entryTypeChoiceBox.getSelectionModel().select(0);

        BMAddEntry bmAddEntry = new BMAddEntry(entryEditField, entryTypeChoiceBox);

        confirmButton.setOnAction(event -> {
            bmAddEntry.addEntry(entries);
            displayEntries("");
        });

        if (bmEditEntry == null) {
            bmEditEntry = new BMEditEntry(entryEditField, entryTypeChoiceBox);
        }
        bmEditEntry.setSelectedRowToNull();

        if (!keepLastDeletedEntryFields) {
            bmAddEntry.resetEntryEditField();
            bmEditEntry.typeChanged();
        }

        mainBorderPane.setBottom(entryEditField);
    }

    public void deleteEntry() {
        if (aRowIsSelected) {
            keepLastDeletedEntryFields = true;

            entries.remove(currentRowIndex);
            resetRowNumbers();
            displayEntries("");
            System.out.println(currentRowIndex);
        }
    }

    public void openLibrary() {
        BMParser parser = new BMParser();
        entries = parser.readBibTexLibrary(null);
        database = parser.getBibTeXDatabase();

        displayEntries("");
    }

    public void searchInsideMap() {
        if (searchBar.getText() == null)
            searchKeyword = "";
        else
            searchKeyword = searchBar.getText();

        displayEntries(searchKeyword);
    }

    private void displayEntries(String searchKeyword) {
        ObservableList<Map> entriesObservableList = FXCollections.observableArrayList();

        if (entries != null) {
            if(searchKeyword.length() > 0) {
                for (Map<Key, Object> entryMap: entries) {
                    for (Key key: entryMap.keySet()) {
                        if (entryMap.get(key).toString().toLowerCase().contains(searchKeyword.toLowerCase())) {
                            entriesObservableList.add(entryMap);
                            break;
                        }
                    }

                }
            } else {
                entriesObservableList.addAll(entries);
            }

            tableView.getItems().clear();
            if (!entriesObservableList.isEmpty()) {
                tableView.setItems(entriesObservableList);
            }
        }
    }

    public void rowSelected() {
        currentRow = null;
        confirmButton.setText("Change");

        if (bmEditEntry != null) {
            confirmButton.setOnAction(event -> confirmChanges());
        }

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

                if (mainBorderPane.getBottom() == null) {
                    mainBorderPane.setBottom(entryEditField);
                }

                if (currentRow != null)
                    fillEntryEditField(currentRow.entrySet());

            }
        }
    }

    private void fillEntryEditField(Set currentRowSet) {
        keepLastDeletedEntryFields = false;

        Object[] currentRowArray = currentRowSet.toArray();
        int entryIndex = 0;

        for (int i = 0; i < currentRowArray.length; i++) {
            String currentElement = currentRowArray[i].toString().toLowerCase();
            if (currentElement.contains("rownumber=")) {
                currentElement = currentElement.replace("rownumber=", "");
                entryIndex = Integer.parseInt(currentElement) - 1;
                break;
            }
        }

        bmEditEntry = new BMEditEntry(entryIndex, entries, entryEditField, entryTypeChoiceBox);

        bmEditEntry.fillEntryEditFields();
    }

    public void confirmChanges() {
        bmEditEntry.changeEntry(entries);
        displayEntries(searchKeyword);
        if (currentRow != null) {
            tableView.getSelectionModel().select(currentRow);
        }
    }

    private void typeChanged() {
        if (bmEditEntry != null)
            bmEditEntry.typeChanged();
    }

    public void optionalFieldsSelected() {
        optionalFields.setSelected(!optionalFields.isSelected());
        if (currentRow != null) {
            tableView.getSelectionModel().select(currentRow);
        }

        typeChanged();
    }

    private void resetRowNumbers() {
        if (currentRowIndex != entries.size()) {
            int rowNumber = 1;
            for (Map<Key, Object> entry: entries) {
                entry.put(new Key("rownumber"), rowNumber++);
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mainBorderPane.getChildren().remove(mainBorderPane.getBottom());

        titleColumn.setCellFactory(TooltippedTableCell.forTableColumn());
        authorEditorColumn.setCellFactory(TooltippedTableCell.forTableColumn());
        journalBookTitleColumn.setCellFactory(TooltippedTableCell.forTableColumn());

        Key numberKey = new Key("rownumber");
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
            displayEntries("");
        }

        optionalFields = new CheckBox();
        entryTypeChoiceBox.getItems().addAll(FXCollections.observableArrayList(BMEntry.TYPES));
        entryTypeChoiceBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((ObservableValue observable, Object oldValue, Object newValue) -> typeChanged());

//        BMFormatter bmFormatter = new BMFormatter();
//        bmFormatter.addEntryToEntriesMap();
    }
}
