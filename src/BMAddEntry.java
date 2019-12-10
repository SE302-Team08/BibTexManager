import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import org.jbibtex.Key;
import org.jbibtex.KeyMap;

import java.util.List;
import java.util.Map;

public class BMAddEntry {
    private GridPane editField;
    private ChoiceBox entryTypeChoiceBox;
    private Map<Key, Object> newEntry;

    public BMAddEntry(GridPane editField, ChoiceBox entryTypeChoiceBox) {
        this.editField = editField;
        this.entryTypeChoiceBox = entryTypeChoiceBox;
    }

    public void addEntry(List<Map<Key, Object>> entries) {
        String key;
        String value;

        newEntry = new KeyMap<>();

        String selectedType = entryTypeChoiceBox.getSelectionModel().getSelectedItem().toString().toLowerCase();
        newEntry.put(new Key("type"), selectedType);

        for (int i = 0; i < 26; ) {
            TextArea textArea = (TextArea) editField.getChildren().get(i++);
            Label label = (Label) editField.getChildren().get(i++);

            key = label.getText().toLowerCase();
            value = textArea.getText();

            if (value != null && !value.equals("")) {
                value = value.replace("\n", " ");

                newEntry.put(new Key(key), value);
            }
        }

        entries.add(newEntry);
    }

    public void arrangeEntryEditField() {
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

    public void resetEntryEditField() {
        for (int i = 0; i < 26; i+=2) {
            TextArea textArea = (TextArea) editField.getChildren().get(i);
            textArea.setText("");
            System.out.println("here");
        }
    }
}
