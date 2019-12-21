import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import org.jbibtex.*;
import org.w3c.dom.Document;

import java.awt.*;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;
import java.util.List;


public class BMMainScreen {
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
    @FXML private Label libraryName;

    private BMEditEntry bmEditEntry;
    private BMConfig config;
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
    private final ButtonType buttonTypeYes = new ButtonType("Yes");
    private final ButtonType buttonTypeNo = new ButtonType("No");
    public static boolean aChangeIsMade = false;
    public static CheckBox optionalFields;

    private KeyCombination deleteKC = new KeyCodeCombination(KeyCode.DELETE);
    private Runnable deleteRN = this::deleteEntry;

    private KeyCombination undoKC = new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN);
    private Runnable undoRN = this::undo;

    private KeyCombination redoKC = new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
    private Runnable redoRN = this::redo;

    @FXML
    private void createLibrary() {
        if (aChangeIsMade) {
            Optional<ButtonType> result = showConfirmation();
            if (result.isPresent()) {
                if (result.get() == buttonTypeNo) {
                    aChangeIsMade = false;
                    BMParser.library = null;
                    libraryName.setText("Library name: Not Named Yet");

                    clearUndoRedoStacks();
                    mainBorderPane.getChildren().remove(mainBorderPane.getBottom());

                    entries = new ArrayList<>();
                    tableView.getItems().clear();
                    Toast.showToast("New Library Created");
                }
            }
        } else {
            BMParser.library = null;
            libraryName.setText("Library name: Not Named Yet");

            clearUndoRedoStacks();
            mainBorderPane.getChildren().remove(mainBorderPane.getBottom());

            entries = new ArrayList<>();
            tableView.getItems().clear();
            Toast.showToast("New Library Created");
        }
    }

    @FXML
    private void saveLibrary() {
        BMFormatter.saveLibrary(entries);
        if (BMParser.library != null && BMParser.library.getName().length() > 0) {
            libraryName.setText("Library name: " + BMParser.library.getName());
        }
    }

    @FXML
    private void saveLibraryAs() {
        BMFormatter.saveLibraryAs(entries);
        aChangeIsMade = false;
        if (BMParser.library != null && BMParser.library.getName().length() > 0) {
            libraryName.setText("Library name: " + BMParser.library.getName());
        }
    }

    @FXML
    private void addEntry() {
        confirmButton.setText("Add");
        entryTypeChoiceBox.getSelectionModel().select(0);
        Toast.showToast("Add Entry From Below");

        BMAddEntry bmAddEntry = new BMAddEntry(entryEditField, entryTypeChoiceBox);

        confirmButton.setOnAction(event -> {
            bmAddEntry.addEntry(currentRowIndex, entries);
            aChangeIsMade = true;
            resetRowNumbers();
            displayEntries("");
            Toast.showToast("Entry Added");
            Map<Key, Object> addedEntry;
            if (currentRowIndex < 0) {
                tableView.scrollTo(entries.size());
                addedEntry = entries.get(entries.size() - 1);
            } else {
                addedEntry = entries.get(currentRowIndex);
            }
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

    @FXML
    private void deleteEntry() {
        if (aRowIsSelected && currentRow != null) {
            keepLastDeletedEntryFields = true;

            entries.remove(currentRowIndex);
            aChangeIsMade = true;
            resetRowNumbers();
            displayEntries("");
            Toast.showToast("Entry Deleted");
            deleteDuplicates(deletedEntriesUndo, undoEventStack, currentRow);
            deletedEntriesUndo.add(currentRow);
            undoEventStack.add(new HashMap<Map<Key, Object>, String>() {{put(currentRow, DELETE_EVENT);}});
            currentRow = null;
            currentRowIndex = -1;
            aRowIsSelected = false;
        } else {
            Toast.showToast("Nothing selected");
            BMMain.stage.requestFocus();
        }
    }

    @FXML
    private void openLibrary() {
        if (aChangeIsMade) {
            Optional<ButtonType> result = showConfirmation();
            if (result.isPresent()) {
                if (result.get() == buttonTypeNo) {
                    BMParser parser = new BMParser();
                    ArrayList<Map<Key, Object>> tmpEntries = null;
                    if (entries != null) {
                        tmpEntries = (ArrayList<Map<Key, Object>>) entries.clone();
                    }
                    entries = parser.readBibTexLibrary(null);

                    if (entries == null && tmpEntries != null) {
                        entries = tmpEntries;
                        clearUndoRedoStacks();
                    } else if (entries == null) {
                        Toast.showToast("File is corrupted");
                    }

                    if (BMParser.library != null) {
                        libraryName.setText("Library name: " + BMParser.library.getName());
                    }
                    aRowIsSelected = false;
                    displayEntries("");
                }
            }

        } else {
            BMParser parser = new BMParser();
            ArrayList<Map<Key, Object>> tmpEntries = null;
            if (entries != null) {
                tmpEntries = (ArrayList<Map<Key, Object>>) entries.clone();
            }
            entries = parser.readBibTexLibrary(null);

            if (entries == null && tmpEntries != null) {
                entries = tmpEntries;
                clearUndoRedoStacks();
            } else if (entries == null) {
                Toast.showToast("File is corrupted");
            }

            if (BMParser.library != null) {
                libraryName.setText("Library name: " + BMParser.library.getName());
            }
            aRowIsSelected = false;
            displayEntries("");
        }
    }

    @FXML
    private void openLibraryDirectory() {
        if (BMParser.library != null && BMParser.library.getName().length() > 0) {
            try {
                Runtime.getRuntime().exec("explorer.exe /select," + BMParser.library.getPath());
            } catch (IOException e) {
                Toast.showToast("Cannot Open Directory");
            }
        } else {
            Toast.showToast("No Library");
        }
    }

    @FXML
    private void searchInsideMap() {
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

    @FXML
    private void rowSelected() {
        BMMain.scene.getAccelerators().put(deleteKC, deleteRN);
        BMMain.scene.getAccelerators().put(undoKC, undoRN);
        BMMain.scene.getAccelerators().put(redoKC, redoRN);
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
                currentRowIndex = -1;
                currentRow = null;
            } else {
                currentRow = tableView.getSelectionModel().getSelectedItem();
                currentRowIndex = tableView.getSelectionModel().getFocusedIndex();

                if (mainBorderPane.getBottom() == null && currentRow != null) {
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

    @FXML
    private void confirmChanges() {
        if (currentRow != null) {
            Map<Key, Object> editedEntry = (HashMap)((HashMap) currentRow).clone();
            deleteDuplicates(editedEntriesUndo, undoEventStack, editedEntry);
            editedEntriesUndo.add(editedEntry);
            undoEventStack.add(new HashMap<Map<Key, Object>, String>() {{put(currentRow, EDIT_EVENT);}});
            bmEditEntry.changeEntry(entries);
            aChangeIsMade = true;
            displayEntries(searchKeyword);
            Toast.showToast("Entry Changed");
            if (currentRow != null) {
                tableView.getSelectionModel().select(currentRow);
            }
        }
    }

    private void typeChanged() {
        if (bmEditEntry != null)
            bmEditEntry.typeChanged();
    }

    @FXML
    private void optionalFieldsSelected() {
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

    @FXML
    private void undo() {
        if (!undoEventStack.empty()) {
            switch ((String) undoEventStack.pop().values().toArray()[0]) {
                case ADD_EVENT:
                    if (!addedEntriesUndo.empty()) {
                        Toast.showToast("Undo");
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
                        Toast.showToast("Undo");
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
                        Toast.showToast("Undo");
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

                default:
                    Toast.showToast("No Undo");
            }
        } else Toast.showToast("No Undo");
        BMMain.stage.requestFocus(); // This is because after each CTRL + Z key combination the stage loses focus to
                                     // toast message stage.
    }

    @FXML
    private void redo() {
        if (!redoEventStack.empty()) {
            switch ((String) redoEventStack.pop().values().toArray()[0]) {
                case ADD_EVENT:
                    if (!addedEntriesRedo.empty()) {
                        Toast.showToast("Redo");
                        Map<Key, Object> entryToBeAdded = addedEntriesRedo.pop();
                        bibTexKeyCheck(entries, entryToBeAdded);
                        entries.add((int) entryToBeAdded.get(new Key("rownumber")) - 1, entryToBeAdded);
                        deleteDuplicates(addedEntriesUndo, undoEventStack, entryToBeAdded);
                        addedEntriesUndo.add(entryToBeAdded);
                        undoEventStack.add(new HashMap<Map<Key, Object>, String>() {{put(entryToBeAdded, ADD_EVENT);}});
                        resetRowNumbers();
                        displayEntries("");
                    }
                    break;

                case DELETE_EVENT:
                    if (!deletedEntriesRedo.empty()) {
                        Toast.showToast("Redo");
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
                        Toast.showToast("Redo");
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

                default:
                    Toast.showToast("No Redo");
            }
        } else Toast.showToast("No Redo");

        BMMain.stage.requestFocus();// This is because after each CTRL + SHIFT + Z key combination the stage loses focus
                                    // to toast message stage.
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

    private void clearUndoRedoStacks() {
        deletedEntriesUndo.clear();
        deletedEntriesRedo.clear();
        editedEntriesUndo.clear();
        editedEntriesRedo.clear();
        addedEntriesUndo.clear();
        addedEntriesRedo.clear();
        undoEventStack.clear();
        redoEventStack.clear();
    }

    private Optional<ButtonType> showConfirmation() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Changed Library");
        alert.setHeaderText("Currently open library is not saved. Do you want to save?");
        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        return alert.showAndWait();
    }

    @FXML
    private void forgetLastOpenedLibrary() {
        config = new BMConfig();
        config.setProps(null);
    }

    @FXML
    private void scrollTop() {
        if (tableView != null) {
            tableView.scrollTo(0);
        }
    }

    @FXML
    private void scrollBottom() {
        if (tableView != null && entries != null) {
            tableView.scrollTo(entries.size());
        }
    }

    @FXML
    private void closeCurrentLibrary() {
        if (aChangeIsMade) {
            Optional<ButtonType> result = showConfirmation();
            if (result.isPresent()) {
                if (result.get() == buttonTypeNo) {
                    tableView.getSelectionModel().clearSelection();
                    tableView.getItems().clear();

                    BMParser.library = null;
                    libraryName.setText("Library name: No Library");

                    mainBorderPane.getChildren().remove(mainBorderPane.getBottom());

                    clearUndoRedoStacks();
                    aChangeIsMade = false;
                    entries = null;
                    currentRow = null;
                    currentRowIndex = -1;
                    aRowIsSelected = false;
                }
            }
        } else {
            tableView.getSelectionModel().clearSelection();
            tableView.getItems().clear();

            BMParser.library = null;
            libraryName.setText("Library name: No Library");

            mainBorderPane.getChildren().remove(mainBorderPane.getBottom());

            clearUndoRedoStacks();
            entries = null;
            currentRow = null;
            currentRowIndex = -1;
            aRowIsSelected = false;
        }
    }

    @FXML
    public void initialize() {
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
        if (propsDocument != null && propsDocument.getElementsByTagName("entry").item(0) != null) {
            entries = new BMParser().readBibTexLibrary(propsDocument.getElementsByTagName("entry").item(0).getTextContent());
            displayEntries("");
        }

        optionalFields = new CheckBox();
        entryTypeChoiceBox.getItems().addAll(FXCollections.observableArrayList(BMEntry.TYPES));
        entryTypeChoiceBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((ObservableValue observable, Object oldValue, Object newValue) -> typeChanged());

        if (BMParser.library != null && BMParser.library.getName().length() > 0) {
            libraryName.setText("Library name: " + BMParser.library.getName());
        } else {
            libraryName.setText("Library name: No Library");
        }

        Toolkit kit = Toolkit.getDefaultToolkit();
        Dimension dimensions = kit.getScreenSize();
        double screenHeight = dimensions.getHeight();
        double screenWidth = dimensions.getWidth();
        mainBorderPane.setPrefWidth(screenWidth/1.5);
        mainBorderPane.setPrefHeight(screenHeight/2);
    }
}
