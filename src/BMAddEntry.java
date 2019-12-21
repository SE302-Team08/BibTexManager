import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.Key;
import org.jbibtex.KeyMap;

import java.util.List;
import java.util.Map;
import java.security.SecureRandom;

public class BMAddEntry {
    private GridPane editField;
    private ChoiceBox entryTypeChoiceBox;
    private Map<Key, Object> newEntry;
    private int numberOfEntries;

    public BMAddEntry(GridPane editField, ChoiceBox entryTypeChoiceBox) {
        this.editField = editField;
        this.entryTypeChoiceBox = entryTypeChoiceBox;
    }

    public void addEntry(int index, List<Map<Key, Object>> entries) {
        if (entries != null) {
            numberOfEntries = entries.size();
            String key;
            String value;

            if (checkRequiredFields()) {
                newEntry = new KeyMap<>();
                if (index > -1) {
                    newEntry.put(new Key("rownumber"), index);
                } else {
                    newEntry.put(new Key("rownumber"), entries.size() + 1);
                }
                String selectedType = entryTypeChoiceBox.getSelectionModel().getSelectedItem().toString().toLowerCase();
                newEntry.put(new Key("type"), selectedType);
                String[] entryFieldOptions = BMEntry.entryTypesMap.get(selectedType);
                int numberOfFields = Integer.parseInt(entryFieldOptions[0]) + Integer.parseInt(entryFieldOptions[1]);

                for (int i = 0; i < numberOfFields * 2; ) {
                    TextArea textArea = (TextArea) editField.getChildren().get(i++);
                    Label label = (Label) editField.getChildren().get(i++);

                    key = label.getText().toLowerCase();
                    value = textArea.getText();

                    if (key.equals("key")) {
                        value = bibTexKeyCheck(entries, value);
                    }

                    if (!value.equals("")) {
                        value = value.replace("\n", " ");

                        newEntry.put(new Key(key), value);
                    }
                }

                if (index > -1) {
                    entries.add(index, newEntry);
                    BMMainScreen.aChangeIsMade = true;
                    Toast.showToast("Entry Added");
                } else {
                    entries.add(newEntry);
                    BMMainScreen.aChangeIsMade = true;
                    Toast.showToast("Entry Added");
                }
            }
        } else {
            Toast.showToast("No Library");
            BMMain.stage.requestFocus();
        }
    }

    public boolean checkRequiredFields() {
        String selectedType = entryTypeChoiceBox.getSelectionModel().getSelectedItem().toString();
        String[] entryFieldOptions = BMEntry.entryTypesMap.get(selectedType.toLowerCase());
        int numberOfRequiredFields = Integer.parseInt(entryFieldOptions[0]);
        int keyFieldIndex = Integer.parseInt(entryFieldOptions[0]) + Integer.parseInt(entryFieldOptions[1]);
        String entryKey = "";

        keyFieldIndex = (keyFieldIndex - 2) * 2;
        TextArea keyTextArea = (TextArea) editField.getChildren().get(keyFieldIndex);

        for (int i = 0; i < numberOfRequiredFields * 2; ) {

            TextArea textArea = (TextArea) editField.getChildren().get(i++);
            Label label = (Label) editField.getChildren().get(i++);

            String fieldName = label.getText();
            String fieldContent = textArea.getText();

            if (fieldContent.equals("") || fieldContent.trim().length() < 1) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Required Field Error!");
                alert.setContentText(fieldName + " field cannot be empty for entry type " + selectedType + ".");

                alert.showAndWait();
                return false;
            }

            if (keyTextArea.getText().trim().equals("")) {
                int lastIndex = 4;
                while (true) {
                    try {
                        entryKey += textArea.getText().replace(" ", "").substring(0, lastIndex);
                    } catch (StringIndexOutOfBoundsException e) {
                        lastIndex--;
                        continue;
                    }
                    break;
                }
            }
        }

        if ((numberOfRequiredFields == 0 || entryKey.trim().length() < 1) && keyTextArea.getText().equals("")) {
            keyTextArea.setText(Integer.toString(numberOfEntries+1));
        } else if (keyTextArea.getText().equals("")) {
            keyTextArea.setText(entryKey);
        }

        return true;
    }

    private String bibTexKeyCheck(List<Map<Key, Object>> entries, String key) {
        SecureRandom random = new SecureRandom();
        for (Map<Key, Object> entry: entries) {
            if (entry.get(BibTeXEntry.KEY_KEY).equals(key)) {
                key = key + random.nextInt(1000);
                bibTexKeyCheck(entries, key);
            }
        }
        return key;
    }

    public void resetEntryEditField() {
        for (int i = 0; i < 26; i+=2) {
            TextArea textArea = (TextArea) editField.getChildren().get(i);
            textArea.setText("");
        }
    }
}
