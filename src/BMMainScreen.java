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
import java.security.SecureRandom;
import java.util.*;


public class BMMainScreen implements Initializable {
//    @FXML private Button createButton;
//    @FXML private Button deleteButton;
//    @FXML private MenuItem addNewEntryMenuItem;
//    @FXML private MenuItem openLibraryMenuItem;
//    @FXML private MenuItem createLibraryMenuItem;
    @FXML private TableView<Map> tableView;
    @FXML private TableColumn<Map, Integer> numberColumn;
    @FXML private TableColumn<Map, String> entryTypeColumn;
    @FXML private TableColumn<Map, String> authorColumn;
    @FXML private TableColumn<Map, String> titleColumn;
    @FXML private TableColumn<Map, Object> yearColumn;
    @FXML private TableColumn<Map, String> journalColumn;
    @FXML private TableColumn<Map, String> bibTexKeyColumn;
    @FXML private TextField searchBar;
    @FXML private BorderPane mainBorderPane;
    @FXML private GridPane entryEditField;
    @FXML private ChoiceBox entryTypeChoiceBox;
    @FXML private Button confirmButton;

    @FXML private Button undoBtn;
    @FXML private Button redoBtn;
//    private EventStream<ChangeManager<?>> changes;
//    private UndoManager<ChangeManager<?>> undoManager;


    //    private BMFormatter formatter;
    private BibTeXDatabase database;
    private BMEditEntry bmEditEntry;
    private boolean aRowIsSelected = false;
    private int currentRowIndex = -1;
    private ArrayList<Map<Key, Object>> entries;
    private String searchKeyword = "";
    private Map currentRow;
    private boolean keepLastDeletedEntryFields = false;
    private Stack<Map<Key, Object>> deletedEntriesUndo = new Stack<>();
    private Stack<Map<Key, Object>> deletedEntriesRedo = new Stack<>();
    private Stack<Map<Key, Object>> editedEntriesUndo = new Stack<>();
    private Stack<Map<Key, Object>> editedEntriesRedo = new Stack<>();
    private Stack<Map<Key, Object>> addedEntriesUndo = new Stack<>();
    private Stack<Map<Key, Object>> addedEntriesRedo = new Stack<>();
    private Stack<String> undoEventStack = new Stack<>();
    private Stack<String> redoEventStack = new Stack<>();
    private final String ADD_EVENT = "add_event";
    private final String DELETE_EVENT = "delete_event";
    private final String EDIT_EVENT = "edit_event";
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
            tableView.scrollTo(entries.size());
            addedEntriesUndo.add(entries.get(entries.size() - 1));
            undoEventStack.add(ADD_EVENT);
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
            deletedEntriesUndo.add(currentRow);
            undoEventStack.add(DELETE_EVENT);
        }
    }

    public void openLibrary() {
        BMParser parser = new BMParser();
        ArrayList<Map<Key, Object>> tmpEntries = (ArrayList<Map<Key, Object>>) entries.clone();
        entries = parser.readBibTexLibrary(null);

        if (entries == null && tmpEntries != null) {
            entries = tmpEntries;
        }

        database = parser.getBibTeXDatabase();
        aRowIsSelected = false;

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
        editedEntriesUndo.add((HashMap)((HashMap) currentRow).clone());
        undoEventStack.add(EDIT_EVENT);
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

    private void undo() {
//        System.out.println(undoEventStack);
        if (!undoEventStack.empty()) {
            switch (undoEventStack.pop()) {
                case ADD_EVENT:
                    Map<Key, Object> entryToBeRemoved = addedEntriesUndo.pop();
                    entries.remove(entryToBeRemoved);
                    addedEntriesRedo.add(entryToBeRemoved);
                    redoEventStack.add(ADD_EVENT);
                    resetRowNumbers();
                    displayEntries("");
                    break;

                case DELETE_EVENT:
                    Map<Key, Object> entryToBeAdded = deletedEntriesUndo.pop();
                    bibTexKeyCheck(entries, entryToBeAdded);
                    entries.add((int) entryToBeAdded.get(new Key("rownumber")) - 1, entryToBeAdded);
                    deletedEntriesRedo.add(entryToBeAdded);
                    redoEventStack.add(DELETE_EVENT);
                    resetRowNumbers();
                    displayEntries("");
                    break;

                case EDIT_EVENT:
                    Map<Key, Object> demodifiedEntry = editedEntriesUndo.pop();
//                    bibTexKeyCheck(entries, demodifiedEntry);
                    int entryIndex = (int) demodifiedEntry.get(new Key("rownumber")) - 1;
                    Map<Key, Object> modifiedEntry = entries.get(entryIndex);
                    entries.set(entryIndex, demodifiedEntry);
                    editedEntriesRedo.add(modifiedEntry);
                    redoEventStack.add(EDIT_EVENT);
                    resetRowNumbers();
                    displayEntries("");
                    break;
            }
        }
    }

    private void redo() {
//        System.out.println(redoEventStack);
        if (!redoEventStack.empty()) {
            switch (redoEventStack.pop()) {
                case ADD_EVENT:
                    Map<Key, Object> entryToBeAdded = addedEntriesRedo.pop();
                    bibTexKeyCheck(entries, entryToBeAdded);
                    entries.add(entryToBeAdded);
                    addedEntriesUndo.add(entryToBeAdded);
                    undoEventStack.add(ADD_EVENT);
                    resetRowNumbers();
                    displayEntries("");
                    break;

                case DELETE_EVENT:
                    Map<Key, Object> entryToBeRemoved = deletedEntriesRedo.pop();
                    entries.remove(entryToBeRemoved);
                    deletedEntriesUndo.add(entryToBeRemoved);
                    undoEventStack.add(DELETE_EVENT);
                    resetRowNumbers();
                    displayEntries("");
                    break;

                case EDIT_EVENT:
                    Map<Key, Object> modifiedEntry = editedEntriesRedo.pop();
                    bibTexKeyCheck(entries, modifiedEntry);

                    int entryIndex = (int) modifiedEntry.get(new Key("rownumber")) - 1;
                    Map<Key, Object> demodifiedEntry = entries.get(entryIndex);
                    entries.set(entryIndex, modifiedEntry);
                    editedEntriesUndo.add(demodifiedEntry);
                    undoEventStack.add(EDIT_EVENT);
                    resetRowNumbers();
                    displayEntries("");
                    break;
            }
        }
    }

    private void bibTexKeyCheck(List<Map<Key, Object>> entries, Map<Key, Object> entry) {
        SecureRandom random = new SecureRandom();
        String key = (String) entry.get(BibTeXEntry.KEY_KEY);
        for (Map<Key, Object> currEntry: entries) {
            if (currEntry.get(BibTeXEntry.KEY_KEY).equals(key) && currEntry != entry &&
                    currEntry.get(new Key("rownumber")) != entry.get(new Key("rownumber"))) {
                key = key + random.nextInt(1000);
                entry.put(BibTeXEntry.KEY_KEY, key);
                bibTexKeyCheck(entries, entry);
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mainBorderPane.getChildren().remove(mainBorderPane.getBottom());

        titleColumn.setCellFactory(TooltippedTableCell.forTableColumn());
        authorColumn.setCellFactory(TooltippedTableCell.forTableColumn());
        journalColumn.setCellFactory(TooltippedTableCell.forTableColumn());

        Key numberKey = new Key("rownumber");
        numberColumn.setCellValueFactory(new MapValueFactory<>(numberKey));
        titleColumn.setCellValueFactory(new MapValueFactory<>(BibTeXEntry.KEY_TITLE));

        entryTypeColumn.setCellValueFactory(new MapValueFactory<>(BibTeXEntry.KEY_TYPE));
        authorColumn.setCellValueFactory(new MapValueFactory<>(BibTeXEntry.KEY_AUTHOR));

        yearColumn.setCellValueFactory(new MapValueFactory<>(BibTeXEntry.KEY_YEAR));
        journalColumn.setCellValueFactory(new MapValueFactory<>(BibTeXEntry.KEY_JOURNAL));

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

//        redoBtn.disableProperty().bind(undoManager.redoAvailableProperty().map(x -> !x));
        undoBtn.setOnAction(evt -> undo());
        redoBtn.setOnAction(evt -> redo());
    }
}
