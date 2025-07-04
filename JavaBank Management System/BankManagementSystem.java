import java.io.*;
import java.util.*;

// =============== MODEL ===============
abstract class Account {
    protected String accountNumber;
    protected String accountHolder;
    protected double balance;

    public Account(String accountNumber, String accountHolder) {
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
        this.balance = 0.0;
    }

    public abstract String getAccountType();

    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            System.out.printf("Deposited ₹%.2f successfully.\n", amount);
        } else {
            System.out.println("Deposit amount must be positive.");
        }
    }

    public boolean withdraw(double amount) {
        if (amount <= 0) {
            System.out.println("Withdrawal amount must be positive.");
            return false;
        }
        if (amount > balance) {
            System.out.println("Insufficient balance.");
            return false;
        }
        balance -= amount;
        System.out.printf("Withdrawn ₹%.2f successfully.\n", amount);
        return true;
    }

    public void display() {
        System.out.printf("Account: %s | Holder: %s | Type: %s | Balance: ₹%.2f\n",
                accountNumber, accountHolder, getAccountType(), balance);
    }

    public String toFileString() {
        return String.format("%s,%s,%s,%.2f", accountNumber, accountHolder, getAccountType(), balance);
    }

    public static Account fromFileString(String line) {
        String[] parts = line.split(",");
        String accNo = parts[0];
        String name = parts[1];
        String type = parts[2];
        double balance = Double.parseDouble(parts[3]);

        Account account = type.equals("Savings") ? new SavingsAccount(accNo, name) : new CurrentAccount(accNo, name);
        account.balance = balance;
        return account;
    }

    public String getAccountNumber() {
        return accountNumber;
    }
}

class SavingsAccount extends Account {
    public SavingsAccount(String accNo, String name) {
        super(accNo, name);
    }

    public String getAccountType() {
        return "Savings";
    }
}

class CurrentAccount extends Account {
    public CurrentAccount(String accNo, String name) {
        super(accNo, name);
    }

    public String getAccountType() {
        return "Current";
    }
}

// =============== REPOSITORY ===============
class AccountRepository {
    private final String FILE_PATH = "accounts.txt";

    public Map<String, Account> loadAccounts() {
        Map<String, Account> accounts = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                Account acc = Account.fromFileString(line);
                accounts.put(acc.getAccountNumber(), acc);
            }
        } catch (IOException e) {
            System.out.println("No previous data found. Starting fresh.");
        }
        return accounts;
    }

    public void saveAccounts(Collection<Account> accounts) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_PATH))) {
            for (Account acc : accounts) {
                writer.println(acc.toFileString());
            }
        } catch (IOException e) {
            System.out.println("Failed to save account data.");
        }
    }
}

// =============== SERVICE ===============
class AccountService {
    private final AccountRepository repository;
    private final Map<String, Account> accounts;

    public AccountService() {
        repository = new AccountRepository();
        accounts = repository.loadAccounts();
    }

    public void createAccount(String accNo, String name, String type) {
        if (accounts.containsKey(accNo)) {
            System.out.println("Account with this number already exists.");
            return;
        }

        Account acc = type.equalsIgnoreCase("Savings") ? new SavingsAccount(accNo, name)
                : new CurrentAccount(accNo, name);

        accounts.put(accNo, acc);
        repository.saveAccounts(accounts.values());
        System.out.println("Account created successfully.");
    }

    public void deposit(String accNo, double amount) {
        Map<String, Account> fileAccounts = repository.loadAccounts();

        Account acc = fileAccounts.get(accNo);
        if (acc != null) {
            acc.deposit(amount);
            repository.saveAccounts(fileAccounts.values());
            System.out.printf("Deposited ₹%.2f successfully into account %s\n", amount, accNo);
        } else {
            System.out.println("Account not found.");
        }
    }

    public void withdraw(String accNo, double amount) {
        Account acc = accounts.get(accNo);
        if (acc != null) {
            if (acc.withdraw(amount)) {
                repository.saveAccounts(accounts.values());
            }
        } else {
            System.out.println("Account not found.");
        }
    }

    public void checkBalance(String accNo) {
        Account acc = accounts.get(accNo);
        if (acc != null) {
            acc.display();
        } else {
            System.out.println("Account not found.");
        }
    }

    public void listAllAccounts() {
        if (accounts.isEmpty()) {
            System.out.println("No accounts found.");
            return;
        }

        for (Account acc : accounts.values()) {
            acc.display();
        }
    }
}

// =============== UI ===============
public class BankManagementSystem {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        AccountService service = new AccountService();

        while (true) {
            System.out.println("\n==== BANK MANAGEMENT SYSTEM ====");
            System.out.println("1. Create Account");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. Check Balance");
            System.out.println("5. Show All Accounts");
            System.out.println("6. Exit");
            System.out.print("Choose an option: ");

            int choice = sc.nextInt();
            sc.nextLine(); // Clear buffer

            switch (choice) {
                case 1 -> {
                    System.out.print("Enter Account Number: ");
                    String accNo = sc.nextLine();
                    System.out.print("Enter Holder Name: ");
                    String name = sc.nextLine();
                    System.out.print("Account Type (Savings/Current): ");
                    String type = sc.nextLine();
                    service.createAccount(accNo, name, type);
                }
                case 2 -> {
                    System.out.print("Enter Account Number: ");
                    String accNo = sc.nextLine();
                    System.out.print("Enter Amount to Deposit: ");
                    double amt = sc.nextDouble();
                    service.deposit(accNo, amt);
                }
                case 3 -> {
                    System.out.print("Enter Account Number: ");
                    String accNo = sc.nextLine();
                    System.out.print("Enter Amount to Withdraw: ");
                    double amt = sc.nextDouble();
                    service.withdraw(accNo, amt);
                }
                case 4 -> {
                    System.out.print("Enter Account Number: ");
                    String accNo = sc.nextLine();
                    service.checkBalance(accNo);
                }
                case 5 -> service.listAllAccounts();
                case 6 -> {
                    System.out.println("Thank you for using the system.");
                    System.exit(0);
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }
}
