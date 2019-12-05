import javafx.collections.ObservableList;
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
    private List<Map<Key, Object>> entries;
    private GridPane editField;
    private ChoiceBox entryType;
    private Map<Key, Object> selectedRow;

    public BMEditEntry(int selectedIndex, List<Map<Key, Object>> entries, GridPane editField, ChoiceBox entryType) {
        this.selectedIndex = selectedIndex;
        this.editField = editField;
        this.entryType = entryType;
        this.entries = entries;
        this.selectedRow = entries.get(selectedIndex);
    }

    public void fillEntryEditFields() {
        String selectedRowType = selectedRow.get(BibTeXEntry.KEY_TYPE).toString().toLowerCase();

        switch (selectedRowType) {
            case "article":
                entryType.getSelectionModel().select("Article");
                fillEntryEditFields(BMEntry.ARTICLE);
                break;

            case "book":
                entryType.getSelectionModel().select("Book");
                fillEntryEditFields(BMEntry.BOOK);
                break;

            case "booklet":
                entryType.getSelectionModel().select("Booklet");
                fillEntryEditFields(BMEntry.BOOKLET);
                break;

            case "conference":
                entryType.getSelectionModel().select("Conference");
                fillEntryEditFields(BMEntry.CONFERENCE);
                break;

            case "inbook":
                entryType.getSelectionModel().select("InBook");
                fillEntryEditFields(BMEntry.INBOOK);
                break;

            case "incollection":
                entryType.getSelectionModel().select("InCollection");
                fillEntryEditFields(BMEntry.INCOLLECTION);
                break;

            case "inproceedings":
                entryType.getSelectionModel().select("InProceedings");
                fillEntryEditFields(BMEntry.INPROCEEDINGS);
                break;

            case "manual":
                entryType.getSelectionModel().select("Manual");
                fillEntryEditFields(BMEntry.MANUAL);
                break;

            case "mastersthesis":
                entryType.getSelectionModel().select("MastersThesis");
                fillEntryEditFields(BMEntry.MASTERSTHESIS);
                break;

            case "misc":
                entryType.getSelectionModel().select("Misc");
                fillEntryEditFields(BMEntry.MISC);
                break;

            case "phdthesis":
                entryType.getSelectionModel().select("PhDThesis");
                fillEntryEditFields(BMEntry.PHDTHESIS);
                break;

            case "proceedings":
                entryType.getSelectionModel().select("Proceedings");
                fillEntryEditFields(BMEntry.PROCEEDINGS);
                break;

            case "techreport":
                entryType.getSelectionModel().select("TechReport");
                fillEntryEditFields(BMEntry.TECHREPORT);
                break;

            case "unpublished":
                entryType.getSelectionModel().select("Unpublished");
                fillEntryEditFields(BMEntry.UNPUBLISHED);
                break;
        }
    }

    private void fillEntryEditFields(String[] typeFields) {
        int j = 0;
        for (int i = 2; i < typeFields.length; i++) {
            if (selectedRow.get(new Key(typeFields[i].toLowerCase())) != null) {
                TextArea textArea = (TextArea) editField.getChildren().get(j++);
                textArea.setText(selectedRow.get(new Key(typeFields[i].toLowerCase())).toString());
            } else {
                TextArea textArea = (TextArea) editField.getChildren().get(j++);
                textArea.setText("");
            }

            Label label = (Label) editField.getChildren().get(j++);
            label.setText(typeFields[i]);
        }

        int showUntil;
        if (!BMMainScreen.optionalFields.isSelected()) {
            showUntil = Integer.parseInt(typeFields[0]) * 2;
            j = showUntil;
        } else {
            showUntil = (Integer.parseInt(typeFields[0]) + Integer.parseInt(typeFields[1])) * 2;
            j = showUntil;
            for (int i = 0; i < showUntil; ) {
                TextArea textArea = (TextArea) editField.getChildren().get(i++);
                Label label = (Label) editField.getChildren().get(i++);

                textArea.setVisible(true);
                label.setVisible(true);
            }
        }

        for (int i = j; i < 26; ) {
            TextArea textArea = (TextArea) editField.getChildren().get(i++);
            Label label = (Label) editField.getChildren().get(i++);

            textArea.setVisible(false);
            label.setVisible(false);
        }
    }

    public void changeEntryFields(ObservableList<Map> entriesObservableList) {
        String key;
        String value;
        String[] neededEntryFields = BMEntry.entryTypesMap.get(entryType.getSelectionModel().getSelectedItem().toString().toLowerCase());

        boolean fieldNeeded;

        selectedRow.put(new Key("type"), entryType.getSelectionModel().getSelectedItem().toString().toLowerCase());
        for (int i = 0; i < 26; ) {
            TextArea textArea = (TextArea) editField.getChildren().get(i++);
            Label label = (Label) editField.getChildren().get(i++);

            key = label.getText().toLowerCase();
            value = textArea.getText();
            
            fieldNeeded = doesEntryNeedThisField(key, neededEntryFields);

            if (value != null && !value.equals("")) {
                value = value.replace("\n", " ");
                
                if (fieldNeeded)
                    selectedRow.put(new Key(key), value);

                else
                    selectedRow.remove(new Key(key));
            }
        }

        entriesObservableList.set(selectedIndex, selectedRow);
    }

    private boolean doesEntryNeedThisField(String fieldName, String[] neededEntryFields) {
        for (String field: neededEntryFields) {
            if (field.toLowerCase().equals(fieldName))
                return true;
        }
        return false;
    }

    public void typeChanged() {

    }
}
