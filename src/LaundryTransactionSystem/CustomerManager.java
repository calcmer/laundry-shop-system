package LaundryTransactionSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.io.Reader;
import java.io.Writer;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.Paths;

public class CustomerManager {
    private static CustomerManager instance;
    // arraylist declaration connected to customer class
    private final ArrayList<Customer> customers = new ArrayList<>();
    private final Path file;
    private int nextCustomerNumber = 1;
    private OrderManager linkedOrders;

    public static CustomerManager getInstance() {
        if (instance == null) instance = new CustomerManager(Paths.get("data", "jocasha_customers.txt"));
        return instance;
    }

    public CustomerManager(Path file) {
        this.file = file;
        load();
    }

    public void setOrderManager(OrderManager manager) { linkedOrders = manager; }

    private String cleanName(String name) {
        String cleaned = name == null ? "" : name.trim().replaceAll("\\s+", " ");
        if (cleaned.length() < 2 || cleaned.length() > 80
                || !cleaned.matches("[\\p{L}][\\p{L} .'-]*")) {
            throw new IllegalArgumentException("Enter a valid customer name (2-80 letters/characters)." );
        }
        return cleaned;
    }

    private String cleanContact(String contact) {
        String cleaned = contact == null ? "" : contact.trim().replaceAll("[\\s-]", "");
        if (!cleaned.matches("\\+?[0-9]{7,15}")) {
            throw new IllegalArgumentException("Enter a valid contact number (7-15 digits, optional +)." );
        }
        return cleaned;
    }
    // search for specific contact number
    private Customer findByContact(String contact) {
        for (Customer customer : customers) {
            if (customer.getContactNumber().equals(contact)) return customer;
        }
        return null;
    }
    // search for specific id
    public Customer findById(String id) {
        for (Customer customer : customers) {
            if (customer.getId().equalsIgnoreCase(id)) return customer;
        }
        return null;
    }

    public Customer registerCustomer(String name, String contact) {
        String cleanedName = cleanName(name);
        String cleanedContact = cleanContact(contact);
        Customer existing = findByContact(cleanedContact);
        if (existing != null) {
            if (!existing.isActive() && existing.getName().equalsIgnoreCase(cleanedName)) {
                existing.setActive(true);
                save();
                return existing;
            }
            throw new IllegalArgumentException("This number belongs to " + existing.getName()
                    + " (" + existing.getId() + "). Search for that customer instead."
                    + (!existing.isActive() ? " An archived profile can be restored using the same name and number." : ""));
        }
        String id = String.format(Locale.US, "C%03d", nextCustomerNumber++);
        Customer customer = new Customer(id, cleanedName, cleanedContact);
        // adding the information to the arraylist
        customers.add(customer);
        save();
        return customer;
    }

    public void updateCustomer(Customer customer, String name, String contact) {
        if (customer == null || !customer.isActive() || !customers.contains(customer)) {
            throw new IllegalArgumentException("This customer is no longer active.");
        }
        String cleanedName = cleanName(name);
        String cleanedContact = cleanContact(contact);
        Customer duplicate = findByContact(cleanedContact);
        if (duplicate != null && duplicate != customer) {
            throw new IllegalArgumentException("Contact number already belongs to "
                    + duplicate.getName() + " (" + duplicate.getId() + ").");
        }
        customer.update(cleanedName, cleanedContact);
        save();
        if (linkedOrders != null) linkedOrders.syncCustomerDetails(customer);
    }

    public boolean deleteCustomer(Customer customer,
                                  AccountManager accounts, Account actor, char[] adminPassword) {
        accounts.requireAdminPassword(actor, adminPassword);
        if (customer == null || !customer.isActive() || !customers.contains(customer)) {
            throw new IllegalArgumentException("This customer is no longer active.");
        }
        boolean hasLinkedOrders = linkedOrders == null
                || linkedOrders.hasOrdersForCustomer(customer.getId());
        if (hasLinkedOrders) {
            customer.setActive(false);
            save();
            return true;
        }
        customers.remove(customer);
        save();
        return false;
    }
    // search for customers list
    public List<Customer> search(String keyword) {
        String query = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        ArrayList<Customer> results = new ArrayList<>();
        for (Customer customer : customers) {
            if (customer.isActive() && (customer.getId().toLowerCase(Locale.ROOT).contains(query)
                    || customer.getName().toLowerCase(Locale.ROOT).contains(query)
                    || customer.getContactNumber().contains(query))) {
                results.add(customer);
            }
        }
        return results;
    }

    public boolean hasAnotherActiveCustomerNamed(String name, Customer excluding) {
        String value = name == null ? "" : name.trim().replaceAll("\\s+", " ");
        for (Customer customer : customers) {
            if (customer != excluding && customer.isActive()
                    && customer.getName().equalsIgnoreCase(value)) return true;
        }
        return false;
    }

    public int count() { return search("").size(); }

    private void load() {
        if (!Files.exists(file)) return;
        Properties data = new Properties();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            data.load(reader);
            int next = Integer.parseInt(data.getProperty("nextId", "1"));
            int count = Integer.parseInt(data.getProperty("count", "0"));
            if (count < 0 || next < 1) throw new IllegalArgumentException("Invalid customer counts.");
            ArrayList<Customer> loaded = new ArrayList<>();
            int highest = 0;
            for (int i = 0; i < count; i++) {
                String prefix = "customer." + i + ".";
                String id = required(data, prefix + "id");
                Customer c = new Customer(id, required(data, prefix + "name"),
                        required(data, prefix + "contact"));
                c.setActive(Boolean.parseBoolean(required(data, prefix + "active")));
                loaded.add(c);
                if (id.matches("C[0-9]+")) highest = Math.max(highest, Integer.parseInt(id.substring(1)));
            }
            customers.addAll(loaded);
            nextCustomerNumber = Math.max(next, highest + 1);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot load customers from " + file
                    + ". Restore the saved file: " + ex.getMessage(), ex);
        }
    }

    private static String required(Properties data, String key) {
        String value = data.getProperty(key);
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException("Missing " + key);
        return value;
    }

    private void save() {
        Properties data = new Properties();
        data.setProperty("nextId", String.valueOf(nextCustomerNumber));
        data.setProperty("count", String.valueOf(customers.size()));
        for (int i = 0; i < customers.size(); i++) {
            Customer c = customers.get(i);
            String prefix = "customer." + i + ".";
            data.setProperty(prefix + "id", c.getId());
            data.setProperty(prefix + "name", c.getName());
            data.setProperty(prefix + "contact", c.getContactNumber());
            data.setProperty(prefix + "active", String.valueOf(c.isActive()));
        }
        try {
            Files.createDirectories(file.toAbsolutePath().getParent());
            Path temp = file.resolveSibling(file.getFileName() + ".tmp");
            try (Writer writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
                data.store(writer, "Jocasha customers");
            }
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot save customers: " + ex.getMessage(), ex);
        }
    }
}
