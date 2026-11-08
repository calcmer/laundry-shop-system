    package LaundryTransactionSystem;

public class Customer extends PersonRecord {
    private String name;
    private String contactNumber;
    private boolean active = true;

    Customer(String id, String name, String contactNumber) {
        super(id);
        this.name = name;
        this.contactNumber = contactNumber;
    }
    
    //overriding the abstract class
    @Override public String getDisplaySummary() { return name + " (" + getId() + ")"; }
    public String getName() { return name; }
    public String getContactNumber() { return contactNumber; }
    public boolean isActive() { return active; }

    void update(String name, String contactNumber) {
        this.name = name;
        this.contactNumber = contactNumber;
    }
    void setActive(boolean active) { this.active = active; }
}
