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

        newEntry.put(new Key("rownumber"), entries.size() + 1);

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

    public void resetEntryEditField() {
        for (int i = 0; i < 26; i+=2) {
            TextArea textArea = (TextArea) editField.getChildren().get(i);
            textArea.setText("");
        }
    }
}
