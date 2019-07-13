package atmMVC.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * A bank manager.
 */
public class BankManager extends BankPerson {

    /**
     * Constructs a customer with a given username and password.
     * @param username the username for this customer
     * @param password the password for this customer
     * @param sinNumber the SIN number for this customer
     */
    public BankManager(String username, String password, int sinNumber)
    {
        super(username, password, sinNumber);
        this.role = "Manager";
    }

    /**
     * Constructs a customer with a given username and password. This constructor is used specifically when files
     * are being read.
     * @param  employeeNumber the customer identifier
     * @param username the username for this customer
     * @param password the password for this customer
     * @param sinNumber the SIN number for this customer
     */
    public BankManager(int employeeNumber, String username, String password, int sinNumber) {
        super(employeeNumber, username, password, sinNumber);
        this.role = "Manager";
    }

    /**
     * Processes and creates an account for each customer account request
     */
    public String createAccounts() {
        ArrayList<Account> newAccounts = new ArrayList<>();
        File arFile = new File(Config.accountRequestFile);
        if (arFile.exists() && arFile.isFile()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(arFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Account a = null;
                    String[] accountFieldsArray = line.split("\t");
                    Customer c = Bank.findCustomer(Integer.parseInt(accountFieldsArray[0]));
                    if (c != null) {
                        if (accountFieldsArray[1].equalsIgnoreCase("ChequingAccount")) {
                            a = new ChequingAccount(c.getCustomerNumber(), BigDecimal.valueOf(0),
                                    LocalDateTime.now(), false);
                            if(c.primaryChequingAccount == null) {
                                ((ChequingAccount) a).setPrimary(true);
                                c.primaryChequingAccount = (ChequingAccount) a;
                            }
                        }
                        else if (accountFieldsArray[1].equalsIgnoreCase("SavingAccount"))
                            a = new SavingAccount(c.getCustomerNumber(), BigDecimal.valueOf(0), LocalDateTime.now());
                        else if (accountFieldsArray[1].equalsIgnoreCase("PowerSavingAccount"))
                            a = new PowerSavingAccount(c.getCustomerNumber(),
                                    BigDecimal.valueOf(0), LocalDateTime.now());
                        else if (accountFieldsArray[1].equalsIgnoreCase("CreditCardAccount"))
                            a = new CreditCardAccount(c.getCustomerNumber(), BigDecimal.valueOf(0),
                                    LocalDateTime.now());
                        else if (accountFieldsArray[1].equalsIgnoreCase("LineOfCreditAccount"))
                            a = new LineOfCreditAccount(c.getCustomerNumber(), BigDecimal.valueOf(0),
                                    LocalDateTime.now());

                        if (a != null)
                            newAccounts.add(a);
                    }
                }
                String inputStr = "";
                reader.close();

                // wipe out account request file
                FileOutputStream fileOut = new FileOutputStream(arFile);
                fileOut.write(inputStr.getBytes());
                fileOut.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        // new account created
        if(newAccounts.size() > 0) {
            for(Account newAccount: newAccounts)
                addNewAccountToFile(newAccount);
            // reload bank
            Bank.reloadBank();
            return newAccounts.size() + " new accounts have been created.";
        }
        else
            return "There is no request to create account.";
    }

    /**
     * Creates a new account using the information from the accountRequestForFX parameter and
     * updates the accountFile accordingly.
     *
     * @param accountRequestForFX a NewAccountRequestForFX object
     */
    public boolean createAccount(NewAccountRequestForFX accountRequestForFX) {
        Account a = null;
        Customer c = Bank.findCustomer(accountRequestForFX.getCustomerNumber());
        if (c != null) {
            String accountType = accountRequestForFX.accountTypeProperty().getValueSafe();
            if (accountType.equalsIgnoreCase("ChequingAccount")) {
                a = new ChequingAccount(c.getCustomerNumber(), BigDecimal.valueOf(0),
                        LocalDateTime.now(), false);
                if(c.primaryChequingAccount == null) {
                    ((ChequingAccount) a).setPrimary(true);
                    c.primaryChequingAccount = (ChequingAccount) a;
                }
            }
            else if (accountType.equalsIgnoreCase("SavingAccount"))
                a = new SavingAccount(c.getCustomerNumber(), BigDecimal.valueOf(0), LocalDateTime.now());
            else if (accountType.equalsIgnoreCase("PowerSavingAccount"))
                a = new PowerSavingAccount(c.getCustomerNumber(), BigDecimal.valueOf(0), LocalDateTime.now());
            else if (accountType.equalsIgnoreCase("CreditCardAccount"))
                a = new CreditCardAccount(c.getCustomerNumber(), BigDecimal.valueOf(0),
                        LocalDateTime.now());
            else if (accountType.equalsIgnoreCase("LineOfCreditAccount"))
                a = new LineOfCreditAccount(c.getCustomerNumber(), BigDecimal.valueOf(0),
                        LocalDateTime.now());
        }

        // new account created
        if (a != null) {
            addNewAccountToFile(a);
            // reload bank
            Bank.reloadBank();
            return true;
        }
        else
            return false;
    }

    /**
     * Reads through the observable array of account requests and updates the accountRequestFile accordingly.
     *
     * @param requests an observable array of new account requests
     */
    public void updateAccountRequestFile(ObservableList<NewAccountRequestForFX> requests) {
        File arFile = new File(Config.accountRequestFile);
        boolean hasAccess = true;
        if (!arFile.exists()) {
            try {
                hasAccess = arFile.createNewFile();
            }
            catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
        if (arFile.exists() && arFile.isFile() && hasAccess) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(arFile, false))) {
                for(NewAccountRequestForFX requestForFX : requests) {
                    String line = requestForFX.getCustomerNumber() + "\t" +
                            requestForFX.accountTypeProperty().getValueSafe() + "\t" +
                            requestForFX.getRequestDateTime() + "\n";
                    bw.write(line);
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

    }

    /**
     * Returns an observable arrayList of NewAccountRequestForFX objects constructed using the accountRequestFile
     * (i.e. an arrayList of all accounts being requested at this bank)
     *
     */
    public ObservableList<NewAccountRequestForFX> getNewAccountRequestsFX() {
        ObservableList<NewAccountRequestForFX> newAccountRequests = FXCollections.observableArrayList();
        File arFile = new File(Config.accountRequestFile);
        if (arFile.exists() && arFile.isFile()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(arFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Account a = null;
                    String[] accountFieldsArray = line.split("\t");
                    newAccountRequests.add(new NewAccountRequestForFX(Integer.parseInt(accountFieldsArray[0]),
                            accountFieldsArray[1], LocalDateTime.parse(accountFieldsArray[2])));
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        return newAccountRequests;
    }

    /**
     * Undoes the most recent transaction made by a customer
     */
    public boolean undoMostRecentTransaction() {
        boolean result = false;
        if(Transaction.mostRecentTrx != null && Transaction.mostRecentTrx.undoable) {
            result = Transaction.unDoTransaction(Transaction.mostRecentTrx);
            Transaction.mostRecentTrx = null;
        }
        return result;
    }

    /**
     * Undoes a particular transaction a customer has made
     * @param trxID the transaction ID of the particular transaction
     */
    public boolean undoTransaction(int trxID) {
        Transaction transaction = findTransaction(trxID);
        if(transaction != null && transaction.undoable)
            return Transaction.unDoTransaction(transaction);
        return false;
    }

    /**
     * Returns an observable arrayList of TransactionForFX objects constructed using the trxFile
     * (i.e. an arrayList of all transactions that have occurred at this bank)
     *
     */
    public ObservableList<TransactionForFX> getAllTransactions() {
        ObservableList<TransactionForFX> transactions = FXCollections.observableArrayList();
        File tFile = new File(Config.trxFile);
        if (tFile.exists() && tFile.isFile()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(tFile))) {
                Scanner in = new Scanner(reader);
                // transaction file field: trxID, customer#, trxType, trxAmount, accountNumber, trxDateTime,
                // accountNumber2/CustomerNumber2/payee, undoable, isReversingEntry
                while (in.hasNext()) {
                    int trxID = in.nextInt();
                    int customerNumber = in.nextInt();
                    String trxType = in.next();
                    BigDecimal trxAmount = in.nextBigDecimal();
                    int accountNumber = in.nextInt();
                    LocalDateTime trxDateTime = LocalDateTime.parse(in.next());
                    int number2 = in.nextInt();
                    boolean undoable = in.nextBoolean();
                    boolean reversingEntry = in.nextBoolean();
                    transactions.add(new TransactionForFX(trxID, customerNumber, accountNumber,
                            trxAmount, trxType, trxDateTime, number2, undoable, reversingEntry));
                }
                in.close();
            } catch(Exception e){
                System.out.println(e.getMessage());
            }
        }
        return transactions;
    }
}
