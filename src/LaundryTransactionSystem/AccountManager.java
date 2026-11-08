package LaundryTransactionSystem;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;

public final class AccountManager {
    private static final String SIMPLE_HASH_PREFIX = "SHA256:";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static AccountManager instance;

    // arraylist connected to account class
    private final ArrayList<Account> users = new ArrayList<>();
    private final Path file;
    private int nextId = 1;

    public static AccountManager getInstance() {
        if (instance == null) instance = new AccountManager(Paths.get("data", "jocasha_accounts.txt"));
        return instance;
    }

    public AccountManager(Path file) {
        this.file = file;
        load();
    }

    public static final class LoginResult {
        public final Account account;
        public final String message;
        public final boolean newlyBlocked;
        LoginResult(Account account, String message, boolean newlyBlocked) {
            this.account = account;
            this.message = message;
            this.newlyBlocked = newlyBlocked;
        }
        public boolean succeeded() { 
            return account != null; 
        }
    }

    public boolean needsFirstAdmin() { 
        return users.isEmpty(); 
    }
    public boolean isActiveAccount(Account account) {
        return account != null && users.contains(account) && !account.isBlocked();
    }
    public boolean isAdmin(Account account) {
        return isActiveAccount(account) && account.isAdmin();
    }

    private void requireAdmin(Account actor) {
        if (!isAdmin(actor)) throw new SecurityException("Only an active administrator can manage accounts.");
    }
    private void requireKnown(Account account) {
        if (!isActiveAccount(account)) throw new SecurityException("Please sign in with an active account.");
    }
    private void requireTarget(Account target) {
        if (target == null || !users.contains(target)) throw new IllegalArgumentException("Account no longer exists.");
    }
    private String name(String text) {
        String value = text == null ? "" : text.trim().replaceAll("\\s+", " ");
        if (value.length() < 2 || value.length() > 80 || !value.matches("[\\p{L}][\\p{L} .'-]*"))
            throw new IllegalArgumentException("Enter a valid full name (2-80 characters).");
        return value;
    }
    private String username(String text) {
        String value = text == null ? "" : text.trim();
        if (!value.matches("[A-Za-z][A-Za-z0-9_.]{3,29}"))
            throw new IllegalArgumentException("Username must be 4-30 characters and start with a letter. Use letters, numbers, . or _.");
        if (value.matches("(?i)U[0-9]+"))
            throw new IllegalArgumentException("This username is reserved for employee IDs (like U001). Please choose another username.");
        return value;
    }
    private String role(String text) {
        if (!"Admin".equals(text) && !"Employee".equals(text))
            throw new IllegalArgumentException("Select Admin or Employee.");
        return text;
    }
    private Account byUsername(String value) {
        for (Account u : users) if (u.getUsername().equalsIgnoreCase(value)) return u;
        return null;
    }
    private void checkNewPassword(char[] password, char[] confirmation) {
        if (password == null || password.length < 6 || password.length > 128)
            throw new IllegalArgumentException("Password must be 6-128 characters long.");
        if (!Arrays.equals(password, confirmation))
            throw new IllegalArgumentException("Passwords do not match.");
    }

    private static byte[] simpleHash(char[] password, byte[] salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            return digest.digest(new String(password).getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalStateException("Password hashing is unavailable.", ex);
        }
    }

    private void changeHash(Account account, char[] newPassword) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        String saltText = Base64.getEncoder().encodeToString(salt);
        String hashText = SIMPLE_HASH_PREFIX
                + Base64.getEncoder().encodeToString(simpleHash(newPassword, salt));
        account.setPassword(saltText, hashText);
    }

    private boolean matches(Account account, char[] password) {
        if (password == null) return false;
        byte[] salt = Base64.getDecoder().decode(account.getSalt());
        String savedText = account.getPasswordHash();
        if (savedText.startsWith(SIMPLE_HASH_PREFIX)) {
            byte[] saved = Base64.getDecoder().decode(savedText.substring(SIMPLE_HASH_PREFIX.length()));
            return MessageDigest.isEqual(simpleHash(password, salt), saved);
        }
        return false;
    }

    public Account createFirstAdmin(String fullName, String username, char[] password, char[] confirmation) {
        if (!users.isEmpty()) throw new SecurityException("First administrator is already registered.");
        return add(fullName, username, "Admin", password, confirmation);
    }
    public Account createAccount(Account actor, String fullName, String username, String role,
                                              char[] password, char[] confirmation) {
        requireAdmin(actor);
        return add(fullName, username, role, password, confirmation);
    }
    private Account add(String fullName, String login, String access,
                        char[] password, char[] confirmation) {
        String cleanName = name(fullName), cleanUser = username(login), cleanRole = role(access);
        checkNewPassword(password, confirmation);
        if (byUsername(cleanUser) != null) throw new IllegalArgumentException("This username already exists.");
        Account account = new Account(String.format(Locale.US, "U%03d", nextId), cleanName,
                                     cleanUser, cleanRole, false, 0, "", "");
        changeHash(account, password);
        users.add(account);
        nextId++;
        save();
        return account;
    }

    public LoginResult authenticate(String login, char[] password) {
        String identifier = login == null ? "" : login.trim();
        Account account = byUsername(identifier);
        if (account == null) {
            for (Account user : users) {
                if (user.getId().equalsIgnoreCase(identifier)) { account = user; break; }
            }
        }
        if (account == null) {
            return new LoginResult(null, "Invalid username or password.", false);
        }
        if (account.isBlocked()) return new LoginResult(null, "This account is blocked. Contact an administrator.", false);
        if (matches(account, password)) {
            account.setFailedAttempts(0);
            save();
            return new LoginResult(account, "Welcome!", false);
        }
        account.setFailedAttempts(account.getFailedAttempts() + 1);
        if (account.getFailedAttempts() >= 3) {
            account.setBlocked(true);
            save();
            return new LoginResult(null, "Three incorrect passwords. Account blocked. Ask an admin to unblock it.", true);
        }
        save();
        return new LoginResult(null, "Invalid username or password. "
                + (3 - account.getFailedAttempts()) + " attempt(s) remaining.", false);
    }

    private int admins() {
        int count = 0;
        for (Account u : users) if (u.isAdmin()) count++;
        return count;
    }
    public void editAccount(Account actor, Account target, String newName,
                                         String newUsername, String newRole) {
        requireAdmin(actor);
        requireTarget(target);
        String cleanName = name(newName), cleanUser = username(newUsername), cleanRole = role(newRole);
        Account other = byUsername(cleanUser);
        if (other != null && other != target) throw new IllegalArgumentException("This username already exists.");
        if (target == actor && !"Admin".equals(cleanRole))
            throw new SecurityException("You cannot remove your own administrator access.");
        if (target.isAdmin() && !"Admin".equals(cleanRole) && admins() <= 1)
            throw new SecurityException("At least one administrator must remain.");
        target.setFullName(cleanName);
        target.setUsername(cleanUser);
        target.setRole(cleanRole);
        save();
    }
    public void requireAdminPassword(Account actor, char[] password) {
        requireAdmin(actor);
        if (!matches(actor, password))
            throw new SecurityException("Administrator password is incorrect.");
    }

    public void deleteAccount(Account actor, Account target, char[] adminPassword) {
        requireAdmin(actor);
        requireTarget(target);
        if (actor == target) throw new SecurityException("You cannot delete your own account.");
        if (target.isAdmin() && admins() <= 1)
            throw new SecurityException("You cannot delete the last administrator.");
        requireAdminPassword(actor, adminPassword);
        users.remove(target);
        save();
    }
    public void unblockAccount(Account actor, Account target) {
        requireAdmin(actor);
        requireTarget(target);
        if (!target.isBlocked()) return;
        target.setBlocked(false);
        target.setFailedAttempts(0);
        save();
    }
    public void changePassword(Account actor, char[] oldPassword,
                                            char[] newPassword, char[] confirm) {
        requireKnown(actor);
        if (!matches(actor, oldPassword)) throw new SecurityException("Current password is incorrect.");
        checkNewPassword(newPassword, confirm);
        if (Arrays.equals(oldPassword, newPassword)) throw new IllegalArgumentException("Choose a different password.");
        changeHash(actor, newPassword);
        save();
    }
    
    // search for list of accounts
    public List<Account> search(String text) {
        String search = text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
        ArrayList<Account> results = new ArrayList<>();
        for (Account user : users) {
            if (user.getId().toLowerCase(Locale.ROOT).contains(search)
                    || user.getFullName().toLowerCase(Locale.ROOT).contains(search)
                    || user.getUsername().toLowerCase(Locale.ROOT).contains(search)) results.add(user);
        }
        return results;
    }
    public int count() { return users.size(); }

    private void load() {
        if (!Files.exists(file)) return;
        Properties p = new Properties();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            p.load(reader);
            nextId = Integer.parseInt(p.getProperty("nextId", "1"));
            int count = Integer.parseInt(p.getProperty("count", "0"));
            if (count <= 0 || nextId < 1)
                throw new IllegalStateException("Saved account file has no valid users.");
            ArrayList<Account> loaded = new ArrayList<>();
            int highest = 0;
            for (int i = 0; i < count; i++) {
                String k = "user." + i + ".";
                String id = required(p, k + "id");
                String role = required(p, k + "role");
                if (!"Admin".equals(role) && !"Employee".equals(role))
                    throw new IllegalArgumentException("Invalid role for " + id);
                Account u = new Account(id, required(p, k + "name"),
                        required(p, k + "username"), role,
                        Boolean.parseBoolean(p.getProperty(k + "blocked", "false")),
                        Integer.parseInt(p.getProperty(k + "attempts", "0")),
                        required(p, k + "salt"), required(p, k + "hash"));
                if (id.matches("U[0-9]+")) highest = Math.max(highest, Integer.parseInt(id.substring(1)));
                loaded.add(u);
            }
            users.addAll(loaded);
            nextId = Math.max(nextId, highest + 1);
            if (users.size() > 0 && admins() < 1)
                throw new IllegalStateException("Account data has no administrator. Restore from backup.");
        } catch (Exception e) {
            throw new IllegalStateException("Cannot load account data from " + file + ": " + e.getMessage(), e);
        }
    }
    private static String required(Properties p, String key) {
        String value = p.getProperty(key);
        if (value == null || value.trim().isEmpty())
            throw new IllegalArgumentException("Missing or empty field: " + key);
        return value;
    }

    private void save() {
        Properties p = new Properties();
        p.setProperty("nextId", String.valueOf(nextId));
        p.setProperty("count", String.valueOf(users.size()));
        for (int i = 0; i < users.size(); i++) {
            Account a = users.get(i);
            String k = "user." + i + ".";
            p.setProperty(k + "id", a.getId());
            p.setProperty(k + "name", a.getFullName());
            p.setProperty(k + "username", a.getUsername());
            p.setProperty(k + "role", a.getRole());
            p.setProperty(k + "blocked", String.valueOf(a.isBlocked()));
            p.setProperty(k + "attempts", String.valueOf(a.getFailedAttempts()));
            p.setProperty(k + "salt", a.getSalt());
            p.setProperty(k + "hash", a.getPasswordHash());
        }
        try {
            Path directory = file.toAbsolutePath().getParent();
            Files.createDirectories(directory);
            Path temp = file.resolveSibling(file.getFileName() + ".tmp");
            try (Writer writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
                p.store(writer, "Jocasha account records - password hashes only");
            }
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot save accounts: " + e.getMessage(), e);
        }
    }
}
