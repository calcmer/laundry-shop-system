package LaundryTransactionSystem;

public abstract class PersonRecord {
    private final String id;

    protected PersonRecord(String id) {
        this.id = id;
    }

    public final String getId() { 
        return id; 
    }
    // abstract method
    public abstract String getDisplaySummary();
}
