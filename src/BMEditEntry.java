import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.Key;

import java.util.Map;

public class BMEditEntry {
    public void fillEntryEditFields(Map selectedRow, GridPane editField) {
        String selectedRowType = selectedRow.get(BibTeXEntry.KEY_TYPE).toString().toLowerCase();

        switch (selectedRowType) {
            case "article":
                fillEntryEditFields(selectedRow, editField, BMEntry.ARTICLE);
                break;

            case "book":
                fillEntryEditFields(selectedRow, editField, BMEntry.BOOK);
                break;

            case "booklet":
                fillEntryEditFields(selectedRow, editField, BMEntry.BOOKLET);
                break;

            case "conference":
                fillEntryEditFields(selectedRow, editField, BMEntry.CONFERENCE);
                break;

            case "inbook":
                fillEntryEditFields(selectedRow, editField, BMEntry.INBOOK);
                break;

            case "incollection":
                fillEntryEditFields(selectedRow, editField, BMEntry.INCOLLECTION);
                break;

            case "inproceedings":
                fillEntryEditFields(selectedRow, editField, BMEntry.INPROCEEDINGS);
                break;

            case "manual":
                fillEntryEditFields(selectedRow, editField, BMEntry.MANUAL);
                break;

            case "mastersthesis":
                fillEntryEditFields(selectedRow, editField, BMEntry.MASTERSTHESIS);
                break;

            case "misc":
                fillEntryEditFields(selectedRow, editField, BMEntry.MISC);
                break;

            case "phdthesis":
                fillEntryEditFields(selectedRow, editField, BMEntry.PHDTHESIS);
                break;

            case "proceedings":
                fillEntryEditFields(selectedRow, editField, BMEntry.PROCEEDINGS);
                break;

            case "techreport":
                fillEntryEditFields(selectedRow, editField, BMEntry.TECHREPORT);
                break;

            case "unpublished":
                fillEntryEditFields(selectedRow, editField, BMEntry.UNPUBLISHED);
                break;
        }
    }

    private void fillEntryEditFields(Map selectedRow, GridPane editField, String[] typeFields) {
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

        for (int i = j; i < 26; ) {
            TextArea textArea = (TextArea) editField.getChildren().get(i++);
            Label label = (Label) editField.getChildren().get(i++);

            textArea.setVisible(false);
            label.setVisible(false);
        }
    }
}
