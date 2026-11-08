package LaundryTransactionSystem;

public class Order {
    private final String id;
    private final String customerId;
    private final String date;
    private String customer;
    private String status;
    private String service;
    private double weight;
    private double pricePerKg;
    private double payment;
    private String paymentDate = "";
    private String contactNumber;
    private final String expectedPickupDate;
    private final String createdBy;
    private final String createdAt;
    private String specialInstructions = "";
    private String cancelReason = "";
    private String canceledAt = "";
    private String canceledBy = "";
    private double refundAmount;
    private String refundDate = "";
    private String refundedBy = "";

    
    // constructor overloading
    public Order(String id, String customer, String date, String status,
                 String service, double weight, double pricePerKg, double payment) {
        this(id, customer, date, status, service, weight, pricePerKg, payment,
                "", "", "Sample Data", date);
    }

    public Order(String id, String customer, String date, String status,
                 String service, double weight, double pricePerKg, double payment,
                 String contactNumber, String expectedPickupDate,
                 String createdBy, String createdAt) {
        this(id, customer, date, status, service, weight, pricePerKg, payment,
                contactNumber, expectedPickupDate, createdBy, createdAt, "");
    }

    public Order(String id, String customer, String date, String status,
                 String service, double weight, double pricePerKg, double payment,
                 String contactNumber, String expectedPickupDate,
                 String createdBy, String createdAt, String customerId) {
        this.id = id;
        this.customerId = customerId;
        this.customer = customer;
        this.date = date;
        this.status = status;
        this.service = service;
        this.weight = weight;
        this.pricePerKg = pricePerKg;
        this.payment = payment;
        if (payment > 0) this.paymentDate = date;
        this.contactNumber = contactNumber;
        this.expectedPickupDate = expectedPickupDate;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getCustomerId() { return customerId; }
    public String getCustomer() { return customer; }
    public String getDate() { return date; }
    public String getStatus() { return status; }
    public String getService() { return service; }
    public double getWeight() { return weight; }
    public double getPricePerKg() { return pricePerKg; }
    public double getPayment() { return payment; }
    public String getPaymentDate() { return paymentDate; }
    public String getContactNumber() { return contactNumber; }
    public String getExpectedPickupDate() { return expectedPickupDate; }
    public String getCreatedBy() { return createdBy; }
    public String getCreatedAt() { return createdAt; }
    public String getSpecialInstructions() { return specialInstructions; }
    public String getCancelReason() { return cancelReason; }
    public String getCanceledAt() { return canceledAt; }
    public String getCanceledBy() { return canceledBy; }
    public double getRefundAmount() { return refundAmount; }
    public String getRefundDate() { return refundDate; }
    public String getRefundedBy() { return refundedBy; }

    public double getTotal() {
        return Math.round(weight * pricePerKg * 100.0) / 100.0;
    }
    public double getChange() {
        return Math.round(Math.max(0, payment - getTotal()) * 100.0) / 100.0;
    }

    void setSpecialInstructions(String instructions) {
        this.specialInstructions = instructions == null ? "" : instructions.trim();
    }
    void setCustomer(String customer) { this.customer = customer; }
    void setContactNumber(String contact) { this.contactNumber = contact; }
    void setPaymentDate(String date) { this.paymentDate = date; }
    void setService(String service) { this.service = service; }
    void setWeight(double weight) { this.weight = weight; }
    void setPricePerKg(double pricePerKg) { this.pricePerKg = pricePerKg; }
    void setPayment(double payment) {
        this.payment = payment;
        this.paymentDate = payment > 0 ? java.time.LocalDate.now().toString() : "";
    }
    void setStatus(String status) { this.status = status; }
    void setRefund(double amount, String date, String employee) {
        refundAmount = amount;
        refundDate = date;
        refundedBy = employee;
    }
    void setCancellation(String reason, String dateTime, String employee) {
        cancelReason = reason;
        canceledAt = dateTime;
        canceledBy = employee;
    }
}
