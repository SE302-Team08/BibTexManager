import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.Key;

import java.util.List;
import java.util.Map;

public class BMEditEntry {
    private int selectedIndex;
    private GridPane editField;
    private ChoiceBox entryType;
    private Map<Key, Object> selectedRow;
    private String[] previousEntryFields;

    public BMEditEntry(int selectedIndex, List<Map<Key, Object>> entries, GridPane editField, ChoiceBox entryType) {
        this.selectedIndex = selectedIndex;
        this.editField = editField;
        this.entryType = entryType;
        this.selectedRow = entries.get(selectedIndex);
        this.previousEntryFields = new String[15];
    }

    public void fillEntryEditFields() {
        String selectedRowType = selectedRow.get(BibTeXEntry.KEY_TYPE).toString().toLowerCase();
        previousEntryFields = BMEntry.entryTypesMap.get(selectedRowType);

        switch (selectedRowType) {
            case "article":
                entryType.getSelectionModel().select("Article");
                typeChanged();
                break;

            case "book":
                entryType.getSelectionModel().select("Book");
                typeChanged();
                break;

            case "booklet":
                entryType.getSelectionModel().select("Booklet");
                typeChanged();
                break;

            case "conference":
                entryType.getSelectionModel().select("Conference");
                typeChanged();
                break;

            case "inbook":
                entryType.getSelectionModel().select("InBook");
                typeChanged();
                break;

            case "incollection":
                entryType.getSelectionModel().select("InCollection");
                typeChanged();
                break;

            case "inproceedings":
                entryType.getSelectionModel().select("InProceedings");
                typeChanged();
                break;

            case "manual":
                entryType.getSelectionModel().select("Manual");
                typeChanged();
                break;

            case "mastersthesis":
                entryType.getSelectionModel().select("MastersThesis");
                typeChanged();
                break;

            case "misc":
                entryType.getSelectionModel().select("Misc");
                typeChanged();
                break;

            case "phdthesis":
                entryType.getSelectionModel().select("PhDThesis");
                typeChanged();
                break;

            case "proceedings":
                entryType.getSelectionModel().select("Proceedings");
                typeChanged();
                break;

            case "techreport":
                entryType.getSelectionModel().select("TechReport");
                typeChanged();
                break;

            case "unpublished":
                entryType.getSelectionModel().select("Unpublished");
                typeChanged();
                break;
        }
    }

    public void changeEntry(List<Map<Key, Object>> entries) {
        String key;
        String value;

        String selectedType = entryType.getSelectionModel().getSelectedItem().toString().toLowerCase();
        String[] neededEntryFields = BMEntry.entryTypesMap.get(selectedType);
        removeUnnecessaryFields(previousEntryFields, neededEntryFields);

        selectedRow.put(new Key("type"), selectedType);
        for (int i = 0; i < 26; ) {
            TextArea textArea = (TextArea) editField.getChildren().get(i++);
            Label label = (Label) editField.getChildren().get(i++);

            key = label.getText().toLowerCase();
            value = textArea.getText();

            if (value != null && !value.equals("")) {
                value = value.replace("\n", " ");

                selectedRow.put(new Key(key), value);
            }
        }

        entries.set(selectedIndex, selectedRow);
        previousEntryFields = BMEntry.entryTypesMap.get(selectedType);
    }

    private void removeUnnecessaryFields(String[] previousEntryFields, String[] neededEntryFields) {
        boolean fieldNotNeeded = true;

        if (previousEntryFields == null)
            previousEntryFields = BMEntry.entryTypesMap.get(entryType.getSelectionModel().getSelectedItem().toString().toLowerCase());

        for (String previousField: previousEntryFields) {
            for (String currentField: neededEntryFields) {
                if (currentField.toLowerCase().equals(previousField)) {
                    fieldNotNeeded = false;
                    break;
                }
            }
            if (fieldNotNeeded)
                selectedRow.remove(new Key(previousField));

            fieldNotNeeded = true;
        }
    }

    public void typeChanged() {
        String selectedType = entryType.getSelectionModel().getSelectedItem().toString().toLowerCase();
        String[] neededEntryFields = BMEntry.entryTypesMap.get(selectedType);

        int showUntil;
        if (!BMMainScreen.optionalFields.isSelected()) {
            showUntil = Integer.parseInt(neededEntryFields[0]) * 2;
        } else {
            showUntil = (Integer.parseInt(neededEntryFields[0]) + Integer.parseInt(neededEntryFields[1])) * 2;
        }

        int editFieldIndex = 0;
        for (int i = 2; i < neededEntryFields.length; i++) {
            if (selectedRow.get(new Key(neededEntryFields[i].toLowerCase())) != null) {
                TextArea textArea = (TextArea) editField.getChildren().get(editFieldIndex++);
                textArea.setText(selectedRow.get(new Key(neededEntryFields[i].toLowerCase())).toString());
            } else {
                TextArea textArea = (TextArea) editField.getChildren().get(editFieldIndex++);
                textArea.setText("");
            }

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
}
