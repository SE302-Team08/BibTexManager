import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import org.jbibtex.*;
import org.w3c.dom.Document;

import javax.security.auth.kerberos.DelegationPermission;
import java.awt.*;
import java.net.URL;
import java.security.SecureRandom;
import java.util.*;
import java.util.List;


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
    private Stack<Map<Map<Key, Object>, String>> undoEventStack = new Stack<>();
    private Stack<Map<Map<Key, Object>, String>> redoEventStack = new Stack<>();
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
            Map<Key, Object> addedEntry = entries.get(entries.size() - 1);
            deleteDuplicates(addedEntriesUndo, undoEventStack, addedEntry);
            addedEntriesUndo.add(addedEntry);
            undoEventStack.add(new HashMap<Map<Key, Object>, String>() {{put(addedEntry, ADD_EVENT);}});
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
            deleteDuplicates(deletedEntriesUndo, undoEventStack, currentRow);
            deletedEntriesUndo.add(currentRow);
            undoEventStack.add(new HashMap<Map<Key, Object>, String>() {{put(currentRow, DELETE_EVENT);}});
            currentRow = null;
            currentRowIndex = -1;
            aRowIsSelected = false;
        }
    }

    public void openLibrary() {
        BMParser parser = new BMParser();
        ArrayList<Map<Key, Object>> tmpEntries = null;
        if (entries != null) {
           tmpEntries = (ArrayList<Map<Key, Object>>) entries.clone();
        }
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
        Map<Key, Object> editedEntry = (HashMap)((HashMap) currentRow).clone();
        deleteDuplicates(editedEntriesUndo, undoEventStack, editedEntry);
        editedEntriesUndo.add(editedEntry);
        undoEventStack.add(new HashMap<Map<Key, Object>, String>() {{put(currentRow, EDIT_EVENT);}});
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
        if (!undoEventStack.empty()) {
            switch ((String) undoEventStack.pop().values().toArray()[0]) {
                case ADD_EVENT:
                    if (!addedEntriesUndo.empty()) {
                        Map<Key, Object> entryToBeRemoved = addedEntriesUndo.pop();
                        entries.remove(entryToBeRemoved);
                        deleteDuplicates(addedEntriesRedo, redoEventStack, entryToBeRemoved);
                        addedEntriesRedo.add(entryToBeRemoved);
                        redoEventStack.add(new HashMap<Map<Key, Object>, String>() {{
                            put(entryToBeRemoved, ADD_EVENT);
                        }});
                        resetRowNumbers();
                        displayEntries("");
                    }
                    break;

                case DELETE_EVENT:
                    if (!deletedEntriesUndo.empty()) {
                        Map<Key, Object> entryToBeAdded = deletedEntriesUndo.pop();
                        bibTexKeyCheck(entries, entryToBeAdded);
                        entries.add((int) entryToBeAdded.get(new Key("rownumber")) - 1, entryToBeAdded);
                        deleteDuplicates(deletedEntriesRedo, redoEventStack, entryToBeAdded);
                        deletedEntriesRedo.add(entryToBeAdded);
                        redoEventStack.add(new HashMap<Map<Key, Object>, String>() {{
                            put(entryToBeAdded, DELETE_EVENT);
                        }});
                        resetRowNumbers();
                        displayEntries("");
                    }
                    break;

                case EDIT_EVENT:
                    if (!editedEntriesUndo.empty()) {
                        Map<Key, Object> demodifiedEntry = editedEntriesUndo.pop();
                        bibTexKeyCheck(entries, demodifiedEntry);
                        int entryIndex = (int) demodifiedEntry.get(new Key("rownumber")) - 1;
                        Map<Key, Object> modifiedEntry = entries.get(entryIndex);
                        entries.set(entryIndex, demodifiedEntry);
                        deleteDuplicates(editedEntriesRedo, redoEventStack, demodifiedEntry);
                        editedEntriesRedo.add(modifiedEntry);
                        redoEventStack.add(new HashMap<Map<Key, Object>, String>() {{
                            put(demodifiedEntry, EDIT_EVENT);
                        }});
                        resetRowNumbers();
                        displayEntries("");
                    }
                    break;
            }
        }
    }

    private void redo() {
        if (!redoEventStack.empty()) {
            switch ((String) redoEventStack.pop().values().toArray()[0]) {
                case ADD_EVENT:
                    if (!addedEntriesRedo.empty()) {
                        Map<Key, Object> entryToBeAdded = addedEntriesRedo.pop();
                        bibTexKeyCheck(entries, entryToBeAdded);
                        entries.add(entryToBeAdded);
                        deleteDuplicates(addedEntriesUndo, undoEventStack, entryToBeAdded);
                        addedEntriesUndo.add(entryToBeAdded);
                        undoEventStack.add(new HashMap<Map<Key, Object>, String>() {{put(entryToBeAdded, ADD_EVENT);}});
                        resetRowNumbers();
                        displayEntries("");
                    }
                    break;

                case DELETE_EVENT:
                    if (!deletedEntriesRedo.empty()) {
                        Map<Key, Object> entryToBeRemoved = deletedEntriesRedo.pop();
                        entries.remove(entryToBeRemoved);
                        deleteDuplicates(deletedEntriesUndo, undoEventStack, entryToBeRemoved);
                        deletedEntriesUndo.add(entryToBeRemoved);
                        undoEventStack.add(new HashMap<Map<Key, Object>, String>() {{
                            put(entryToBeRemoved, DELETE_EVENT);
                        }});
                        resetRowNumbers();
                        displayEntries("");
                    }
                    break;

                case EDIT_EVENT:
                    if (!editedEntriesRedo.empty()) {
                        Map<Key, Object> modifiedEntry = editedEntriesRedo.pop();
                        bibTexKeyCheck(entries, modifiedEntry);
                        int entryIndex = (int) modifiedEntry.get(new Key("rownumber")) - 1;
                        Map<Key, Object> demodifiedEntry = entries.get(entryIndex);
                        entries.set(entryIndex, modifiedEntry);
                        deleteDuplicates(editedEntriesUndo, undoEventStack, demodifiedEntry);
                        editedEntriesUndo.add(demodifiedEntry);
                        undoEventStack.add(new HashMap<Map<Key, Object>, String>() {{
                            put(modifiedEntry, EDIT_EVENT);
                        }});
                        resetRowNumbers();
                        displayEntries("");
                    }
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

    private void deleteDuplicates(Stack<Map<Key, Object>> stack, Stack<Map<Map<Key, Object>, String>> eventStack, Map<Key, Object> entry) {
        if (!stack.empty() && !eventStack.empty()) {
            for (int i = 0; i < stack.size(); i++) {
                Map<Key, Object> currEntry = stack.get(i);
                if (currEntry == entry) {
                    stack.remove(currEntry);
                    eventStack.remove(currEntry);
                    deleteDuplicates(stack, eventStack, entry);
                }
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
        Toolkit kit = Toolkit.getDefaultToolkit();
        Dimension dimensions = kit.getScreenSize();
        double screenHeight = dimensions.getHeight();
        double screenWidth = dimensions.getWidth();
        mainBorderPane.setPrefWidth(screenWidth/1.5);
        mainBorderPane.setPrefHeight(screenHeight/2);
    }
}
