import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.Key;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

public class BMEditEntry {
    private int selectedIndex;
    private GridPane editField;
    private ChoiceBox entryTypeChoiceBox;
    private Map<Key, Object> selectedRow;
    private String[] previousEntryFields;
    private BMAddEntry bmAddEntry;

    public BMEditEntry(GridPane editField, ChoiceBox entryTypeChoiceBox) {
        this.editField = editField;
        this.entryTypeChoiceBox = entryTypeChoiceBox;
    }

    public BMEditEntry(int selectedIndex, List<Map<Key, Object>> entries, GridPane editField, ChoiceBox entryTypeChoiceBox) {
        this.selectedIndex = selectedIndex;
        this.editField = editField;
        this.entryTypeChoiceBox = entryTypeChoiceBox;
        this.selectedRow = entries.get(selectedIndex);
        this.previousEntryFields = new String[15];
        this.bmAddEntry = new BMAddEntry(editField, entryTypeChoiceBox);
    }

    public void fillEntryEditFields() {
        String selectedRowType = selectedRow.get(BibTeXEntry.KEY_TYPE).toString().toLowerCase();
        previousEntryFields = BMEntry.entryTypesMap.get(selectedRowType);

        switch (selectedRowType) {
            case "article":
                entryTypeChoiceBox.getSelectionModel().select("Article");
                typeChanged();
                break;

            case "book":
                entryTypeChoiceBox.getSelectionModel().select("Book");
                typeChanged();
                break;

            case "booklet":
                entryTypeChoiceBox.getSelectionModel().select("Booklet");
                typeChanged();
                break;

            case "conference":
                entryTypeChoiceBox.getSelectionModel().select("Conference");
                typeChanged();
                break;

            case "inbook":
                entryTypeChoiceBox.getSelectionModel().select("InBook");
                typeChanged();
                break;

            case "incollection":
                entryTypeChoiceBox.getSelectionModel().select("InCollection");
                typeChanged();
                break;

            case "inproceedings":
                entryTypeChoiceBox.getSelectionModel().select("InProceedings");
                typeChanged();
                break;

            case "manual":
                entryTypeChoiceBox.getSelectionModel().select("Manual");
                typeChanged();
                break;

            case "mastersthesis":
                entryTypeChoiceBox.getSelectionModel().select("MastersThesis");
                typeChanged();
                break;

            case "misc":
                entryTypeChoiceBox.getSelectionModel().select("Misc");
                typeChanged();
                break;

            case "phdthesis":
                entryTypeChoiceBox.getSelectionModel().select("PhDThesis");
                typeChanged();
                break;

            case "proceedings":
                entryTypeChoiceBox.getSelectionModel().select("Proceedings");
                typeChanged();
                break;

            case "techreport":
                entryTypeChoiceBox.getSelectionModel().select("TechReport");
                typeChanged();
                break;

            case "unpublished":
                entryTypeChoiceBox.getSelectionModel().select("Unpublished");
                typeChanged();
                break;
        }
    }

    public void changeEntry(List<Map<Key, Object>> entries) {
        String key;
        String value;

        String selectedType = entryTypeChoiceBox.getSelectionModel().getSelectedItem().toString().toLowerCase();
        String[] entryFieldOptions = BMEntry.entryTypesMap.get(selectedType);
        int numberOfFields = Integer.parseInt(entryFieldOptions[0]) + Integer.parseInt(entryFieldOptions[1]);

        if (bmAddEntry.checkRequiredFields()) {
            removeUnnecessaryFields(previousEntryFields, entryFieldOptions);

            selectedRow.put(new Key("type"), selectedType);
            for (int i = 0; i < numberOfFields * 2; ) {
                TextArea textArea = (TextArea) editField.getChildren().get(i++);
                Label label = (Label) editField.getChildren().get(i++);

                key = label.getText().toLowerCase();
                value = textArea.getText();

                if (!value.equals("")) {
                    value = value.replace("\n", " ");

                    selectedRow.put(new Key(key), value);
                }
            }

            String entryKey = bibTexKeyCheck(entries, (String) selectedRow.get(BibTeXEntry.KEY_KEY));
            selectedRow.put(BibTeXEntry.KEY_KEY, entryKey);

            entries.set(selectedIndex, selectedRow);
            Toast.showToast("Entry Changed");
            BMMainScreen.aChangeIsMade = true;
            previousEntryFields = BMEntry.entryTypesMap.get(selectedType);
        }
    }

    private void removeUnnecessaryFields(String[] previousEntryFields, String[] entryFieldOptions) {
        boolean fieldNotNeeded = true;

        if (previousEntryFields == null)
            previousEntryFields = BMEntry.entryTypesMap.get(entryTypeChoiceBox.getSelectionModel().getSelectedItem().toString().toLowerCase());

        for (String previousField: previousEntryFields) {
            for (String currentField: entryFieldOptions) {
                if (currentField.toLowerCase().equals(previousField)) {
                    fieldNotNeeded = false;
                    break;
                }
            }
            if (fieldNotNeeded && selectedRow != null)
                selectedRow.remove(new Key(previousField));

            fieldNotNeeded = true;
        }
    }

    private String bibTexKeyCheck(List<Map<Key, Object>> entries, String key) {
        SecureRandom random = new SecureRandom();
        for (Map<Key, Object> entry: entries) {
            if (entry.get(BibTeXEntry.KEY_KEY).equals(key) && entry != selectedRow) {
                key = key + random.nextInt(1000);
                bibTexKeyCheck(entries, key);
            }
        }
        return key;
    }

    public void typeChanged() {
        String selectedType = entryTypeChoiceBox.getSelectionModel().getSelectedItem().toString().toLowerCase();
        String[] neededEntryFields = BMEntry.entryTypesMap.get(selectedType);

        int showUntil;
        if (!BMMainScreen.optionalFields.isSelected()) {
            showUntil = Integer.parseInt(neededEntryFields[0]) * 2;
        } else {
            showUntil = (Integer.parseInt(neededEntryFields[0]) + Integer.parseInt(neededEntryFields[1])) * 2;
        }

        int editFieldIndex = 0;
        for (int i = 2; i < neededEntryFields.length; i++) {
            TextArea textArea = (TextArea) editField.getChildren().get(editFieldIndex++);
            if (selectedRow != null && selectedRow.get(new Key(neededEntryFields[i].toLowerCase())) != null) {
                textArea.setText(selectedRow.get(new Key(neededEntryFields[i].toLowerCase())).toString());
            } else if (selectedRow != null)
                textArea.setText("");

            Label label = (Label) editField.getChildren().get(editFieldIndex++);
            label.setText(neededEntryFields[i]);
        }

        for (int i = 0; i < showUntil; ) {
            TextArea textArea = (TextArea) editField.getChildren().get(i++);
            Label label = (Label) editField.getChildren().get(i++);

            textArea.setVisible(true);
            label.setVisible(true);
        }

        for (int i = showUntil; i < 26; ) {
            TextArea textArea = (TextArea) editField.getChildren().get(i++);
            Label label = (Label) editField.getChildren().get(i++);

            textArea.setVisible(false);
            label.setVisible(false);
        }
    }

    public void setSelectedRowToNull() {
        selectedRow = null;
    }
}
