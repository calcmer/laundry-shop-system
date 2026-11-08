package LaundryTransactionSystem;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

public class OrderManager {
    private static OrderManager instance;
    // arraylist declaration connected to order class
    private final ArrayList<Order> orders = new ArrayList<>();
    private final Path file;
    private int nextOrderNumber = 7323;

    public static OrderManager getInstance() {
        if (instance == null) instance = new OrderManager(Paths.get("data", "jocasha_orders.txt"));
        return instance;
    }

    public OrderManager(Path file) {
        this.file = file;
        load();
    }

    public static double serviceRate(String service) {
        if ("Wash, Dry & Fold".equals(service)) return 60.0;
        if ("Dry Cleaning".equals(service)) return 145.0;
        if ("Express Laundry".equals(service)) return 90.0;
        throw new IllegalArgumentException("Select a valid laundry service.");
    }

    public Order createOrder(Customer customer, String service, double weight,
                             LocalDate expectedPickup, double payment, String employee,
                             String specialInstructions) {
        String instructions = validateInstructions(specialInstructions);
        if (customer == null || !customer.isActive())
            throw new IllegalArgumentException("Select an active customer before confirming the order.");
        if (!Double.isFinite(weight) || weight <= 0 || weight > 100)
            throw new IllegalArgumentException("Weight must be greater than zero (maximum 100 kg).");
        if (expectedPickup == null || expectedPickup.isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Pickup date cannot be earlier than today.");
        double rate = serviceRate(service);
        double total = Math.round(weight * rate * 100.0) / 100.0;
        if (!Double.isFinite(total) || !Double.isFinite(payment) || payment < total)
            throw new IllegalArgumentException("Amount tendered must cover " + pesos(total) + ".");
        if (employee == null || employee.trim().isEmpty())
            throw new IllegalArgumentException("Employee information is required.");
        String id = "ORD-" + nextOrderNumber;
        while (findById(id) != null) id = "ORD-" + (++nextOrderNumber);
        LocalDateTime now = LocalDateTime.now();
        Order order = new Order(id, customer.getName(), now.toLocalDate().toString(),
                "Pending", service, weight, rate, payment,
                customer.getContactNumber(), expectedPickup.toString(), employee.trim(),
                now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), customer.getId());
        order.setSpecialInstructions(instructions);
        // adding in the list
        orders.add(order);
        ++nextOrderNumber;
        save();
        return order;
    }

    public boolean hasOrdersForCustomer(String customerId) {
        if (customerId == null || customerId.isEmpty()) return false;
        for (Order order : orders) if (customerId.equals(order.getCustomerId())) return true;
        return false;
    }

    public void syncCustomerDetails(Customer customer) {
        boolean changed = false;
        for (Order order : orders) {
            if (order.getCustomerId().equals(customer.getId())) {
                order.setCustomer(customer.getName());
                order.setContactNumber(customer.getContactNumber());
                changed = true;
            }
        }
        if (changed) save();
    }

    public int countAll() { return orders.size(); }
    public int countStatus(String status) {
        int count = 0;
        for (Order order : orders) if (order.getStatus().equals(status)) ++count;
        return count;
    }

    
    // search by id - linear search
    public Order findById(String id) {
        for (Order order : orders) if (order.getId().equals(id)) return order;
        return null;
    }

    
    // searching by users for multiple results - linear search
    public List<Order> findMatching(String status, String search, boolean newestFirst) {
        String keyword = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        ArrayList<Order> matches = new ArrayList<>();
        for (Order order : orders) {
            boolean statusOK = "All Orders".equals(status) || order.getStatus().equals(status);
            boolean searchOK = order.getId().toLowerCase(Locale.ROOT).contains(keyword)
                    || order.getCustomer().toLowerCase(Locale.ROOT).contains(keyword);
            if (statusOK && searchOK) matches.add(order);
        }
        
        // insertion sort for newest and oldest orders
        for (int i = 1; i < matches.size(); i++) {
            Order current = matches.get(i);
            int j = i - 1;
            while (j >= 0 && compareOrders(matches.get(j), current, newestFirst) > 0) {
                matches.set(j + 1, matches.get(j));
                j--;
            }
            matches.set(j + 1, current);
        }
        return matches;
    }

    private int compareOrders(Order a, Order b, boolean newestFirst) {
        int result = a.getDate().compareTo(b.getDate());
        if (result == 0) result = a.getId().compareTo(b.getId());
        return newestFirst ? -result : result;
    }

    private String validateInstructions(String value) {
        String instructions = value == null ? "" : value.trim();
        if (instructions.length() > 250)
            throw new IllegalArgumentException("Special Instructions cannot exceed 250 characters.");
        return instructions;
    }

    public void editOrder(Order order, String customer, String service,
                          double weight, double pricePerKg, String specialInstructions) {
        requireOrder(order);
        if (!order.getCustomer().equals(customer == null ? "" : customer.trim()))
            throw new IllegalArgumentException("Change customer details from the Customers page, not an order.");
        String instructions = validateInstructions(specialInstructions);
        if (!order.getStatus().equals("Pending") && !order.getStatus().equals("In Progress"))
            throw new IllegalArgumentException("This order can no longer be edited.");
        if (!Double.isFinite(weight) || !Double.isFinite(pricePerKg)
                || weight <= 0 || weight > 100 || pricePerKg <= 0)
            throw new IllegalArgumentException("Weight must be greater than 0 and no more than 100 kg; rate must be positive.");
        boolean canChangeCharge = order.getStatus().equals("Pending") && order.getPayment() == 0;
        if (canChangeCharge) {
            serviceRate(service);
            order.setService(service);
            order.setWeight(weight);
            order.setPricePerKg(pricePerKg);
        } else if (!order.getService().equals(service)
                || weight != order.getWeight() || pricePerKg != order.getPricePerKg()) {
            throw new IllegalArgumentException("Weight, service and price cannot be changed after payment. "
                    + "To correct them, cancel this order and create a new one.");
        }
        order.setSpecialInstructions(instructions);
        save();
    }
        // change status based on the status
    public void advanceStatus(Order order, String next, double payment) {
        requireOrder(order);
        String current = order.getStatus();
        boolean valid = current.equals("Pending") && next.equals("In Progress")
                || current.equals("In Progress") && next.equals("Ready for Pickup")
                || current.equals("Ready for Pickup") && next.equals("Completed");
        if (!valid) throw new IllegalArgumentException("Invalid status change.");
        if (next.equals("Completed") && order.getPayment() < order.getTotal()) {
            if (!Double.isFinite(payment) || payment < order.getTotal())
                throw new IllegalArgumentException("Payment must cover the total.");
            order.setPayment(payment);
        }
        order.setStatus(next);
        save();
    }
        
    // cancelling order
    public void cancelOrder(Order order, String reason, AccountManager accounts,
                            Account admin, char[] adminPassword, boolean refundGiven) {
        requireOrder(order);
        if (!(order.getStatus().equals("Pending") || order.getStatus().equals("In Progress")
                || order.getStatus().equals("Ready for Pickup")))
            throw new IllegalArgumentException("This order cannot be canceled.");
        if (reason == null || reason.trim().isEmpty())
            throw new IllegalArgumentException("A cancellation reason is required.");
        if (order.getPayment() > 0 && !refundGiven)
            throw new IllegalArgumentException("Refund must be given and confirmed before cancellation.");
        accounts.requireAdminPassword(admin, adminPassword);
        if (order.getPayment() > 0) {
            order.setRefund(Math.min(order.getPayment(), order.getTotal()),
                    LocalDate.now().toString(), admin.getId());
        }
        order.setCancellation(reason.trim(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), admin.getId());
        order.setStatus("Canceled");
        save();
    }

    private void requireOrder(Order order) {
        if (order == null || !orders.contains(order))
            throw new IllegalArgumentException("Order not found in the current transaction list.");
    }

    public static String pesos(double amount) {
        return String.format(Locale.US, "\u20b1%,.2f", amount);
    }

    private void load() {
        if (!Files.exists(file)) return;
        Properties data = new Properties();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            data.load(reader);
            int next = Integer.parseInt(data.getProperty("nextId", "7323"));
            int count = Integer.parseInt(data.getProperty("count", "0"));
            if (next < 1 || count < 0) throw new IllegalArgumentException("Invalid order counts.");
            ArrayList<Order> loaded = new ArrayList<>();
            int highest = 0;
            for (int i = 0; i < count; i++) {
                String p = "order." + i + ".";
                String id = required(data, p + "id");
                Order o = new Order(id, required(data, p + "customer"), required(data, p + "date"),
                        required(data, p + "status"), required(data, p + "service"),
                        Double.parseDouble(required(data, p + "weight")),
                        Double.parseDouble(required(data, p + "rate")),
                        Double.parseDouble(required(data, p + "payment")),
                        data.getProperty(p + "contact", ""), data.getProperty(p + "pickup", ""),
                        required(data, p + "createdBy"), required(data, p + "createdAt"),
                        data.getProperty(p + "customerId", ""));
                o.setPaymentDate(data.getProperty(p + "paymentDate", ""));
                o.setSpecialInstructions(data.getProperty(p + "instructions", ""));
                o.setCancellation(data.getProperty(p + "cancelReason", ""),
                        data.getProperty(p + "canceledAt", ""), data.getProperty(p + "canceledBy", ""));
                o.setRefund(Double.parseDouble(data.getProperty(p + "refundAmount", "0")),
                        data.getProperty(p + "refundDate", ""), data.getProperty(p + "refundedBy", ""));
                loaded.add(o);
                if (id.matches("ORD-[0-9]+")) highest = Math.max(highest, Integer.parseInt(id.substring(4)));
            }
            orders.addAll(loaded);
            nextOrderNumber = Math.max(next, highest + 1);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot load orders from " + file
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
        data.setProperty("nextId", String.valueOf(nextOrderNumber));
        data.setProperty("count", String.valueOf(orders.size()));
        for (int i = 0; i < orders.size(); i++) {
            Order o = orders.get(i);
            String p = "order." + i + ".";
            data.setProperty(p + "id", o.getId());
            data.setProperty(p + "customerId", o.getCustomerId());
            data.setProperty(p + "customer", o.getCustomer());
            data.setProperty(p + "date", o.getDate());
            data.setProperty(p + "status", o.getStatus());
            data.setProperty(p + "service", o.getService());
            data.setProperty(p + "weight", String.valueOf(o.getWeight()));
            data.setProperty(p + "rate", String.valueOf(o.getPricePerKg()));
            data.setProperty(p + "payment", String.valueOf(o.getPayment()));
            data.setProperty(p + "paymentDate", o.getPaymentDate());
            data.setProperty(p + "contact", o.getContactNumber());
            data.setProperty(p + "pickup", o.getExpectedPickupDate());
            data.setProperty(p + "createdBy", o.getCreatedBy());
            data.setProperty(p + "createdAt", o.getCreatedAt());
            data.setProperty(p + "instructions", o.getSpecialInstructions());
            data.setProperty(p + "cancelReason", o.getCancelReason());
            data.setProperty(p + "canceledAt", o.getCanceledAt());
            data.setProperty(p + "canceledBy", o.getCanceledBy());
            data.setProperty(p + "refundAmount", String.valueOf(o.getRefundAmount()));
            data.setProperty(p + "refundDate", o.getRefundDate());
            data.setProperty(p + "refundedBy", o.getRefundedBy());
        }
        try {
            Files.createDirectories(file.toAbsolutePath().getParent());
            Path temp = file.resolveSibling(file.getFileName() + ".tmp");
            try (Writer writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
                data.store(writer, "Jocasha order records");
            }
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot save orders: " + ex.getMessage(), ex);
        }
    }
}
