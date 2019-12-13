import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import org.reactfx.Change;

import java.util.Objects;

public class EntryEditFieldChange extends ChangeManager<String> {
    private GridPane entryEditField;
    private TextArea firstTextArea;

    public EntryEditFieldChange(GridPane entryEditField) {
        super("", "");
        this.entryEditField = entryEditField;
        firstTextArea = (TextArea) entryEditField.getChildren().get(0);
    }

    public EntryEditFieldChange(String oldValue, String newValue) {
        super(oldValue, newValue);
    }
    public EntryEditFieldChange(Change<String> c) {
        this(c.getOldValue(), c.getNewValue());
    }
    @Override void redo() {

    }
    @Override
    EntryEditFieldChange invert() { return new EntryEditFieldChange(newValue, oldValue); }

    @Override
    public boolean equals(Object other) {
        if(other instanceof EntryEditFieldChange) {
            EntryEditFieldChange that = (EntryEditFieldChange) other;
            return Objects.equals(this.oldValue, that.oldValue)
                    && Objects.equals(this.newValue, that.newValue);
        } else {
            return false;
        }
    }
}
