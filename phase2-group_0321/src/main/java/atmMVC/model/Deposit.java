package atmMVC.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Deposits cash or cheque into the ATM.
 */

public class Deposit extends Transaction {

    private String currency;
    private BigDecimal amountForeign;

    /**
     * Constructs a new deposit into the ATM
     * @param a the account that the deposit comes from
     * @param amount the amount that is being deposited
     * @param trxDateTime the time the deposit is made
     */
    public Deposit(Account a, BigDecimal amount, LocalDateTime trxDateTime) {
        super(a, amount, trxDateTime);
        this.trxType = "Deposit";
        this.undoable = true;
    }

    /**
     * Constructs a new deposit of foreign currency into the ATM
     * @param a the account that the deposit comes from
     * @param amountForeign the amount of foreign currency being deposited
     * @param amountCAD the amount of CAD currency that will be deposited into account a
     * @param trxDateTime the time the deposit is made
     * @param foreignCurrency the 3 character String representing the foreign currency (ie. USD)
     */
    public Deposit(Account a, BigDecimal amountForeign, BigDecimal amountCAD, LocalDateTime trxDateTime,
                   String foreignCurrency) {
        super(a, amountCAD, trxDateTime);
        this.trxType = "Deposit";
        this.amountForeign = amountForeign;
        this.currency = foreignCurrency;
        this.undoable = false;
    }

    /**
     * Constructs a new deposit into the ATM. This constructor is used specifically
     * when files are being read.
     * @param trxID the identification number of this particular deposit
     * @param c the customer that makes the deposit
     * @param a the account that the deposit is coming from
     * @param amount the amount that is being deposited
     * @param trxDateTime the time the deposit is made
     */
    public Deposit(int trxID, Customer c, Account a, BigDecimal amount, LocalDateTime trxDateTime) {
        super(trxID, a, amount, trxDateTime);
        this.trxType = "Deposit";
        this.undoable = true;
    }

    /**
     * Carries out the deposit
     * @return whether the deposit is successfully carried out
     */
    public boolean doTransaction() {
        this.account.addAmount(this.trxAmount);

        // add trx to file
        addTrxToFile(this);
        // update the account balance in the file
        this.account.recentTrxID = this.trxID;
        updateAccountFile(this.account);
        mostRecentTrx = this;
        // Bank reloads
        Bank.reloadBank();
        return true;
    }

    private void updateATMMoney() {
        BigDecimal[] divideBy50 = this.trxAmount.divideAndRemainder(BigDecimal.valueOf(50));
        if (divideBy50[0].compareTo(BigDecimal.valueOf(ATM.getBillOf50())) > 0) {
            divideBy50[0] = BigDecimal.valueOf(ATM.getBillOf50());
            divideBy50[1] = this.trxAmount.subtract(BigDecimal.valueOf(ATM.getBillOf50() * 50));
        }

        BigDecimal[] divideBy20 = divideBy50[1].divideAndRemainder(BigDecimal.valueOf(20));
        if (divideBy20[0].compareTo(BigDecimal.valueOf(ATM.getBillOf20())) > 0) {
            divideBy20[0] = BigDecimal.valueOf(ATM.getBillOf20());
            divideBy20[1] = divideBy50[1].subtract(BigDecimal.valueOf(ATM.getBillOf20() * 20));
        }

        BigDecimal[] divideBy10 = divideBy20[1].divideAndRemainder(BigDecimal.valueOf(10));
        if (divideBy10[0].compareTo(BigDecimal.valueOf(ATM.getBillOf10())) > 0) {
            divideBy10[0] = BigDecimal.valueOf(ATM.getBillOf10());
            divideBy10[1] = divideBy20[1].subtract(BigDecimal.valueOf(ATM.getBillOf10() * 10));
        }
        // should be dividable by 5, anyway
        BigDecimal[] divideBy5 = divideBy10[1].divideAndRemainder(BigDecimal.valueOf(5));

        ATM.billOf50 += divideBy50[0].intValue();
        ATM.billOf20 += divideBy20[0].intValue();
        ATM.billOf10 += divideBy10[0].intValue();
        ATM.billOf5 += divideBy5[0].intValue();
    }

    private void updateATMMoneyUSD() {
        BigDecimal[] divideBy50 = this.amountForeign.divideAndRemainder(BigDecimal.valueOf(50));
        if (divideBy50[0].compareTo(BigDecimal.valueOf(ATM.getBillOf50USD())) > 0) {
            divideBy50[0] = BigDecimal.valueOf(ATM.getBillOf50USD());
            divideBy50[1] = this.amountForeign.subtract(BigDecimal.valueOf(ATM.getBillOf50USD() * 50));
        }

        BigDecimal[] divideBy20 = divideBy50[1].divideAndRemainder(BigDecimal.valueOf(20));
        if (divideBy20[0].compareTo(BigDecimal.valueOf(ATM.getBillOf20USD())) > 0) {
            divideBy20[0] = BigDecimal.valueOf(ATM.getBillOf20USD());
            divideBy20[1] = divideBy50[1].subtract(BigDecimal.valueOf(ATM.getBillOf20USD() * 20));
        }

        BigDecimal[] divideBy10 = divideBy20[1].divideAndRemainder(BigDecimal.valueOf(10));
        if (divideBy10[0].compareTo(BigDecimal.valueOf(ATM.getBillOf10USD())) > 0) {
            divideBy10[0] = BigDecimal.valueOf(ATM.getBillOf10USD());
            divideBy10[1] = divideBy20[1].subtract(BigDecimal.valueOf(ATM.getBillOf10USD() * 10));
        }
        // should be dividable by 5, anyway
        BigDecimal[] divideBy5 = divideBy10[1].divideAndRemainder(BigDecimal.valueOf(5));

        ATM.billOf50USD += divideBy50[0].intValue();
        ATM.billOf20USD += divideBy20[0].intValue();
        ATM.billOf10USD += divideBy10[0].intValue();
        ATM.billOf5USD += divideBy5[0].intValue();
    }
}
