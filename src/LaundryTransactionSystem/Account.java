package LaundryTransactionSystem;

public final class Account extends PersonRecord {
    private String fullName;
    private String username;
    private String role;
    private boolean blocked;
    private int failedAttempts;
    private String salt;
    private String passwordHash;

    Account(String id, String fullName, String username, String role,
            boolean blocked, int failedAttempts, String salt, String passwordHash) {
        super(id);
        this.fullName = fullName;
        this.username = username;
        this.role = role;
        this.blocked = blocked;
        this.failedAttempts = failedAttempts;
        this.salt = salt;
        this.passwordHash = passwordHash;
    }

    // overriding the abstract method
    @Override public String getDisplaySummary() { return fullName + " (" + getId() + ") - " + role; }
    public String getFullName() { return fullName; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public boolean isAdmin() { return "Admin".equals(role); }
    public boolean isBlocked() { return blocked; }
    public int getFailedAttempts() { return failedAttempts; }
    public String getStatus() { return blocked ? "Blocked" : "Active"; }

    void setFullName(String value) { fullName = value; }
    void setUsername(String value) { username = value; }
    void setRole(String value) { role = value; }
    void setBlocked(boolean value) { blocked = value; }
    void setFailedAttempts(int value) { failedAttempts = value; }
    String getSalt() { return salt; }
    String getPasswordHash() { return passwordHash; }
    void setPassword(String newSalt, String newHash) { salt = newSalt; passwordHash = newHash; }
}
