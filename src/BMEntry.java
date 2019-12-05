import java.util.HashMap;
import java.util.Map;

public final class BMEntry {
    public static final String[] TYPES = {"Article", "Book", "Booklet", "Conference", "InBook", "InCollection", "InProceedings", "Manual", "MastersThesis", "Misc", "PhDThesis", "Proceedings", "TechReport", "Unpublished"};
    public static final String[] ARTICLE = {"4", "7", "Author", "Title", "Journal", "Year", "Volume", "Number", "Pages", "Month", "Note", "Key", "Comment"};
    public static final String[] BOOK = {"4", "8", "Author", "Title", "Publisher", "Year", "Volume", "Series", "Address", "Edition", "Month", "Note", "Key", "Comment"};
    public static final String[] BOOKLET = {"1", "8", "Title", "Author", "HowPublished", "Address", "Month", "Year", "Note", "Key", "Comment"};
    public static final String[] CONFERENCE = {"4", "9", "Author", "Title", "Book Title", "Year", "Editor", "Pages", "Organization", "Publisher", "Address", "Month", "Note", "Key", "Comment"};
    public static final String[] INBOOK = {"5", "8", "Author", "Title", "Pages", "Publisher", "Year", "Volume", "Series", "Address", "Edition", "Month", "Note", "Key", "Comment"};
    public static final String[] INCOLLECTION = {"4", "9", "Author", "Title", "Book Title", "Year", "Editor", "Pages", "Organization", "Publisher", "Address", "Month", "Note", "Key", "Comment"};
    public static final String[] INPROCEEDINGS = {"4", "9", "Author", "Title", "Book Title", "Year", "Editor", "Pages", "Organization", "Publisher", "Address", "Month", "Note", "Key", "Comment"};
    public static final String[] MANUAL = {"1", "9", "Title", "Author", "Organization", "Address", "Edition", "Month", "Year", "Note", "Key", "Comment"};
    public static final String[] MASTERSTHESIS = {"4", "5", "Author", "Title", "School", "Year", "Address", "Month", "Note", "Key", "Comment"};
    public static final String[] MISC = {"0", "8", "Author", "Title", "HowPublished", "Month", "Year", "Note", "Key", "Comment"};
    public static final String[] PHDTHESIS = {"4", "5", "Author", "Title", "School", "Year", "Address", "Month", "Note", "Key", "Comment"};
    public static final String[] PROCEEDINGS = {"2", "8", "Title", "Year", "Editor", "Publisher", "Organization", "Address", "Month", "Note", "Key", "Comment"};
    public static final String[] TECHREPORT = {"4", "7", "Author", "Title", "Institution", "Year", "Type", "Number", "Address", "Month", "Note", "Key", "Comment"};
    public static final String[] UNPUBLISHED = {"3", "4", "Author", "Title", "Note", "Month", "Year", "Key", "Comment"};
    public static final Map<String, String[]> entryTypesMap = new HashMap<String, String[]>() {
        {
            put("article", ARTICLE);
            put("book", BOOK);
            put("booklet", BOOKLET);
            put("conference", CONFERENCE);
            put("inbook", INBOOK);
            put("incollection", INCOLLECTION);
            put("inproceedings", INPROCEEDINGS);
            put("manual", MANUAL);
            put("mastersthesis", MASTERSTHESIS);
            put("misc", MISC);
            put("phdthesis", PHDTHESIS);
            put("proceedings", PROCEEDINGS);
            put("techreport", TECHREPORT);
            put("unpublished", UNPUBLISHED);
        }
    };
}
