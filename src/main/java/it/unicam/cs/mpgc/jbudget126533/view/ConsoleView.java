package it.unicam.cs.mpgc.jbudget126533.view;

import it.unicam.cs.mpgc.jbudget126533.model.*;
import it.unicam.cs.mpgc.jbudget126533.util.InvalidInputException;
import it.unicam.cs.mpgc.jbudget126533.controller.Ledger;
import it.unicam.cs.mpgc.jbudget126533.util.Pair;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Classe che si occupa di gestire la console con tutte le funzionalità complete.
 */
public class ConsoleView implements View {

    private final Scanner scanner = new Scanner(System.in);
    private boolean flag = true;
    private final Ledger ledger;

    public ConsoleView(Ledger ledger) {
        this.ledger = ledger;
    }

    /**
     * Metodo che stampa le scelte del menu completo.
     */
    public void menu() {
        System.out.println("===============================================");
        System.out.println("|                MENU PRINCIPALE              |");
        System.out.println("===============================================");
        System.out.println("| [0]  Exit.                                  |");
        System.out.println("| [1]  Add transaction [GUADAGNO/SPESA].      |");
        System.out.println("| [2]  Print transaction list.                |");
        System.out.println("| [3]  Return balance.                        |");
        System.out.println("| [4]  Transactions by date range.            |");
        System.out.println("| [5]  Transactions by tag.                   |");
        System.out.println("| [6]  Budget trend in time.                  |");
        System.out.println("| [7]  Transactions for each tag.             |");
        System.out.println("| [8]  Manage tags.                           |");
        System.out.println("| [9]  Manage budgets.                        |");
        System.out.println("| [10] Manage scheduled transactions.         |");
        System.out.println("| [11] Manage amortization plans.             |");
        System.out.println("| [12] View deadlines.                        |");
        System.out.println("| [13] View statistics.                       |");
        System.out.println("===============================================");
    }

    public void choice(String option) throws InvalidInputException {
        switch (option) {
            case "0": exit(); break;
            case "1": addTransaction(); break;
            case "2": printTransactionsList(); break;
            case "3": showBalance(); break;
            case "4": transactionByDate(); break;
            case "5": transactionByTag(); break;
            case "6": transactionTrend(); break;
            case "7": transactionForEachTag(); break;
            case "8": manageTags(); break;
            case "9": manageBudgets(); break;
            case "10": manageScheduledTransactions(); break;
            case "11": manageAmortizationPlans(); break;
            case "12": viewDeadlines(); break;
            case "13": viewStatistics(); break;
            default: throw new InvalidInputException("Option not found in the menu.");
        }
        System.out.println("\n-----------------------------------------------\n");
    }

    private void exit() {
        System.out.println("Goodbye!");
        close();
    }

    private void showBalance() {
        System.out.println("Balance: € " + String.format("%.2f", this.ledger.getBalance()));
    }

    // ==================== TRANSAZIONI ====================

    private void addTransaction() {
        try {
            System.out.println("[1] GUADAGNO, [2] SPESA: ");
            MovementType type = scanner.nextLine().equals("1") ? MovementType.GUADAGNO : MovementType.SPESA;

            System.out.println("User: ");
            String userName = scanner.nextLine().toUpperCase();

            System.out.println("Amount: ");
            double amount = Double.parseDouble(scanner.nextLine());
            if (type == MovementType.SPESA && amount > 0) amount = -amount;

            System.out.println("Date [yyyy-MM-dd] (enter for today): ");
            String dateInput = scanner.nextLine();
            LocalDate date = dateInput.isEmpty() ? LocalDate.now() : LocalDate.parse(dateInput);

            // Crea una persona temporanea
            Person person = new Person(userName);

            // Selezione tag multipli
            List<ITag> selectedTags = selectMultipleTags();

            ITransaction transaction = new Transaction(type, person, amount, date, selectedTags);
            this.ledger.write(transaction);
            this.ledger.addTransaction(transaction);

            System.out.println("Transaction added successfully.");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private List<ITag> selectMultipleTags() {
        List<ITag> selectedTags = new ArrayList<>();
        System.out.println("Available tags:");
        List<ITag> availableTags = TagManager.getSelectedTagsList();

        for (int i = 0; i < availableTags.size(); i++) {
            System.out.println("[" + i + "] " + availableTags.get(i).getFullPath());
        }

        System.out.println("Select tags (comma separated indices, empty to finish): ");
        String input = scanner.nextLine();

        if (!input.isEmpty()) {
            String[] indices = input.split(",");
            for (String indexStr : indices) {
                try {
                    int index = Integer.parseInt(indexStr.trim());
                    if (index >= 0 && index < availableTags.size()) {
                        selectedTags.add(availableTags.get(index));
                    }
                } catch (NumberFormatException e) {
                    // Ignora input non valido
                }
            }
        }

        return selectedTags;
    }

    private void printTransactionsList() {
        System.out.println("Type            | User            | Amount          | Date            | Tags");
        System.out.println("------------------------------------------------------------------------------------");

        for (ITransaction transaction : this.ledger.getTransaction()) {
            String tags = transaction.getTags().stream()
                    .map(ITag::getName)
                    .collect(Collectors.joining(", "));

            System.out.printf("%-15s | %-15s | %-15.2f | %-15s | %s%n",
                    transaction.getType(),
                    transaction.getUser(),
                    transaction.getMoney(),
                    transaction.getDate(),
                    tags);
        }
    }

    // ==================== TAG MANAGEMENT ====================

    private void manageTags() {
        boolean inTagMenu = true;
        while (inTagMenu) {
            System.out.println("\n=== TAG MANAGEMENT ===");
            System.out.println("1. List all tags");
            System.out.println("2. Create new tag");
            System.out.println("3. Select tags for transactions");
            System.out.println("4. View tag hierarchy");
            System.out.println("5. Back to main menu");
            System.out.print("Choice: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1": listAllTags(); break;
                case "2": createNewTag(); break;
                case "3": selectTagsForTransactions(); break;
                case "4": viewTagHierarchy(); break;
                case "5": inTagMenu = false; break;
                default: System.out.println("Invalid choice");
            }
        }
    }

    private void listAllTags() {
        System.out.println("\n=== ALL TAGS ===");
        TagManager.getAllTags().values().forEach(tag ->
                System.out.println("- " + tag.getFullPath() +
                        (TagManager.isTagSelected(tag.getName()) ? " [SELECTED]" : "")));
    }

    private void createNewTag() {
        try {
            System.out.print("Tag name: ");
            String name = scanner.nextLine();

            System.out.print("Parent tag name (empty for root): ");
            String parentName = scanner.nextLine();

            ITag parent = parentName.isEmpty() ? null : TagManager.getTag(parentName);
            ITag newTag = TagManager.createTag(name, parent);

            System.out.println("Tag created: " + newTag.getFullPath());
        } catch (Exception e) {
            System.out.println("Error creating tag: " + e.getMessage());
        }
    }

    private void selectTagsForTransactions() {
        System.out.println("\n=== SELECT TAGS FOR TRANSACTIONS ===");
        List<ITag> allTags = new ArrayList<>(TagManager.getAllTags().values());

        for (int i = 0; i < allTags.size(); i++) {
            ITag tag = allTags.get(i);
            System.out.println("[" + i + "] " + tag.getFullPath() +
                    (TagManager.isTagSelected(tag.getName()) ? " ✓" : ""));
        }

        System.out.print("Select tags to toggle (comma separated indices): ");
        String input = scanner.nextLine();

        if (!input.isEmpty()) {
            String[] indices = input.split(",");
            for (String indexStr : indices) {
                try {
                    int index = Integer.parseInt(indexStr.trim());
                    if (index >= 0 && index < allTags.size()) {
                        ITag tag = allTags.get(index);
                        if (TagManager.isTagSelected(tag.getName())) {
                            TagManager.deselectTag(tag.getName());
                            System.out.println("Deselected: " + tag.getName());
                        } else {
                            TagManager.selectTag(tag.getName());
                            System.out.println("Selected: " + tag.getName());
                        }
                    }
                } catch (NumberFormatException e) {
                    // Ignora input non valido
                }
            }
        }
    }

    private void viewTagHierarchy() {
        System.out.println("\n=== TAG HIERARCHY ===");
        printTagTree(TagManager.getAllTags().values().stream()
                .filter(tag -> tag.getParent() == null)
                .collect(Collectors.toList()), 0);
    }

    private void printTagTree(List<ITag> tags, int depth) {
        for (ITag tag : tags) {
            String indent = "  ".repeat(depth);
            System.out.println(indent + "└─ " + tag.getName() +
                    (TagManager.isTagSelected(tag.getName()) ? " ✓" : ""));
            printTagTree(new ArrayList<>(tag.getChildren()), depth + 1);
        }
    }

    // ==================== BUDGET MANAGEMENT ====================

    private void manageBudgets() {
        boolean inBudgetMenu = true;
        while (inBudgetMenu) {
            System.out.println("\n=== BUDGET MANAGEMENT ===");
            System.out.println("1. List all budgets");
            System.out.println("2. Set new budget");
            System.out.println("3. Remove budget");
            System.out.println("4. Check exceeded budgets");
            System.out.println("5. Update all budgets");
            System.out.println("6. Back to main menu");
            System.out.print("Choice: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1": listAllBudgets(); break;
                case "2": setNewBudget(); break;
                case "3": removeBudget(); break;
                case "4": checkExceededBudgets(); break;
                case "5": updateAllBudgets(); break;
                case "6": inBudgetMenu = false; break;
                default: System.out.println("Invalid choice");
            }
        }
    }

    private void listAllBudgets() {
        System.out.println("\n=== ALL BUDGETS ===");
        Map<String, Budget> budgets = ledger.getAllBudgets();
        if (budgets.isEmpty()) {
            System.out.println("No budgets set.");
            return;
        }

        for (Budget budget : budgets.values()) {
            System.out.printf("- %s: %.2f/%.2f (%.1f%%) %s%n",
                    budget.getCategory(),
                    budget.getSpentAmount(),
                    budget.getAllocatedAmount(),
                    budget.getUsagePercentage(),
                    budget.isExceeded() ? "[EXCEEDED!]" : "");
        }
    }

    private void setNewBudget() {
        try {
            System.out.print("Category: ");
            String category = scanner.nextLine();

            System.out.print("Amount: ");
            double amount = Double.parseDouble(scanner.nextLine());

            System.out.print("Start date [yyyy-MM-dd]: ");
            LocalDate startDate = LocalDate.parse(scanner.nextLine());

            System.out.print("End date [yyyy-MM-dd]: ");
            LocalDate endDate = LocalDate.parse(scanner.nextLine());

            ledger.setBudget(category, amount, startDate, endDate);
            System.out.println("Budget set successfully.");

        } catch (Exception e) {
            System.out.println("Error setting budget: " + e.getMessage());
        }
    }

    private void removeBudget() {
        System.out.print("Category to remove: ");
        String category = scanner.nextLine();
        ledger.removeBudget(category);
        System.out.println("Budget removed.");
    }

    private void checkExceededBudgets() {
        List<Budget> exceeded = ledger.getExceededBudgets();
        if (exceeded.isEmpty()) {
            System.out.println("No exceeded budgets.");
        } else {
            System.out.println("=== EXCEEDED BUDGETS ===");
            for (Budget budget : exceeded) {
                System.out.printf("- %s: %.2f/%.2f (%.1f%%)%n",
                        budget.getCategory(),
                        budget.getSpentAmount(),
                        budget.getAllocatedAmount(),
                        budget.getUsagePercentage());
            }
        }
    }

    private void updateAllBudgets() {
        ledger.updateBudgets();
        System.out.println("All budgets updated.");
    }

    // ==================== SCHEDULED TRANSACTIONS ====================

    private void manageScheduledTransactions() {
        boolean inScheduledMenu = true;
        while (inScheduledMenu) {
            System.out.println("\n=== SCHEDULED TRANSACTIONS ===");
            System.out.println("1. List scheduled transactions");
            System.out.println("2. Add scheduled transaction");
            System.out.println("3. Remove scheduled transaction");
            System.out.println("4. Check and execute scheduled");
            System.out.println("5. Back to main menu");
            System.out.print("Choice: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1": listScheduledTransactions(); break;
                case "2": addScheduledTransaction(); break;
                case "3": removeScheduledTransaction(); break;
                case "4": checkScheduledTransactions(); break;
                case "5": inScheduledMenu = false; break;
                default: System.out.println("Invalid choice");
            }
        }
    }

    private void listScheduledTransactions() {
        List<ScheduledTransaction> scheduled = ledger.getScheduledTransactions();
        if (scheduled.isEmpty()) {
            System.out.println("No scheduled transactions.");
            return;
        }

        for (int i = 0; i < scheduled.size(); i++) {
            ScheduledTransaction st = scheduled.get(i);
            System.out.printf("[%d] %s: %.2f %s, %s, Next: %s%n",
                    i, st.getDescription(), st.getAmount(), st.getType(),
                    st.getRecurrence(), st.getNextExecutionDate());
        }
    }

    private void addScheduledTransaction() {
        try {
            System.out.print("Description: ");
            String description = scanner.nextLine();

            System.out.print("Amount: ");
            double amount = Double.parseDouble(scanner.nextLine());

            System.out.print("Type [1] GUADAGNO [2] SPESA: ");
            MovementType type = scanner.nextLine().equals("1") ? MovementType.GUADAGNO : MovementType.SPESA;

            System.out.print("Recurrence [1] GIORNALIERO [2] SETTIMANALE [3] MENSILE [4] ANNUALE: ");
            int recurChoice = Integer.parseInt(scanner.nextLine());
            RecurrenceType recurrence = RecurrenceType.values()[recurChoice - 1];

            System.out.print("Start date [yyyy-MM-dd]: ");
            LocalDate startDate = LocalDate.parse(scanner.nextLine());

            System.out.print("End date [yyyy-MM-dd] (optional, enter to skip): ");
            String endDateInput = scanner.nextLine();
            LocalDate endDate = endDateInput.isEmpty() ? null : LocalDate.parse(endDateInput);

            List<ITag> tags = selectMultipleTags();

            ScheduledTransaction st = new ScheduledTransaction(description, amount, type, tags, recurrence, startDate, endDate);
            ledger.addScheduledTransaction(st);
            System.out.println("Scheduled transaction added.");

        } catch (Exception e) {
            System.out.println("Error adding scheduled transaction: " + e.getMessage());
        }
    }

    private void removeScheduledTransaction() {
        listScheduledTransactions();
        System.out.print("Index to remove: ");
        try {
            int index = Integer.parseInt(scanner.nextLine());
            ledger.removeScheduledTransaction(index);
            System.out.println("Scheduled transaction removed.");
        } catch (Exception e) {
            System.out.println("Error removing: " + e.getMessage());
        }
    }

    private void checkScheduledTransactions() {
        ledger.checkScheduledTransactions();
        System.out.println("Scheduled transactions checked and executed.");
    }

    // ==================== AMORTIZATION PLANS ====================

    private void manageAmortizationPlans() {
        boolean inAmortMenu = true;
        while (inAmortMenu) {
            System.out.println("\n=== AMORTIZATION PLANS ===");
            System.out.println("1. List amortization plans");
            System.out.println("2. Create amortization plan");
            System.out.println("3. Delete amortization plan");
            System.out.println("4. Process due installments");
            System.out.println("5. Back to main menu");
            System.out.print("Choice: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1": listAmortizationPlans(); break;
                case "2": createAmortizationPlan(); break;
                case "3": deleteAmortizationPlan(); break;
                case "4": processDueInstallments(); break;
                case "5": inAmortMenu = false; break;
                default: System.out.println("Invalid choice");
            }
        }
    }

    private void listAmortizationPlans() {
        List<AmortizationPlan> plans = ledger.getAmortizationPlans();
        if (plans.isEmpty()) {
            System.out.println("No amortization plans.");
            return;
        }

        for (int i = 0; i < plans.size(); i++) {
            AmortizationPlan plan = plans.get(i);
            System.out.printf("[%d] %s: %.2f€, %.1f%%, %d installments%n",
                    i, plan.getDescription(), plan.getPrincipalAmount(),
                    plan.getAnnualInterestRate(), plan.getNumberOfInstallments());
        }
    }

    private void createAmortizationPlan() {
        try {
            System.out.print("Description: ");
            String description = scanner.nextLine();

            System.out.print("Principal amount: ");
            double principal = Double.parseDouble(scanner.nextLine());

            System.out.print("Annual interest rate (%): ");
            double interestRate = Double.parseDouble(scanner.nextLine());

            System.out.print("Number of installments: ");
            int installments = Integer.parseInt(scanner.nextLine());

            System.out.print("Start date [yyyy-MM-dd]: ");
            LocalDate startDate = LocalDate.parse(scanner.nextLine());

            List<ITag> tags = selectMultipleTags();

            AmortizationPlan plan = ledger.createAmortizationPlan(description, principal, interestRate, installments, startDate, tags);
            System.out.println("Amortization plan created with " + plan.getInstallments().size() + " installments.");

        } catch (Exception e) {
            System.out.println("Error creating amortization plan: " + e.getMessage());
        }
    }

    private void deleteAmortizationPlan() {
        listAmortizationPlans();
        System.out.print("Index to delete: ");
        try {
            int index = Integer.parseInt(scanner.nextLine());
            List<AmortizationPlan> plans = ledger.getAmortizationPlans();
            if (index >= 0 && index < plans.size()) {
                ledger.deleteAmortizationPlan(plans.get(index));
                System.out.println("Amortization plan deleted.");
            }
        } catch (Exception e) {
            System.out.println("Error deleting: " + e.getMessage());
        }
    }

    private void processDueInstallments() {
        ledger.processAmortizationDueDates();
        System.out.println("Due installments processed.");
    }

    // ==================== DEADLINES ====================

    private void viewDeadlines() {
        System.out.println("\n=== DEADLINES ===");
        List<Deadline> deadlines = ledger.getAllDeadlines();

        long total = deadlines.size();
        long overdue = ledger.getOverdueDeadlines().size();
        long dueToday = ledger.getDueTodayDeadlines().size();
        long future = ledger.getUpcomingDeadlines().size();

        System.out.printf("Total: %d | Overdue: %d | Due today: %d | Future: %d%n", total, overdue, dueToday, future);

        for (Deadline deadline : deadlines) {
            String status = deadline.isPaid() ? "✅" : deadline.isOverdue() ? "⚠️" : deadline.isDueToday() ? "📅" : "⏳";
            System.out.printf("%s %s: %.2f€ - %s%n", status, deadline.getDueDate(), deadline.getAmount(), deadline.getDescription());
        }
    }

    // ==================== STATISTICS ====================

    private void viewStatistics() {
        boolean inStatsMenu = true;
        while (inStatsMenu) {
            System.out.println("\n=== STATISTICS ===");
            System.out.println("1. Balance for date range");
            System.out.println("2. Balance trend");
            System.out.println("3. Transactions by tag");
            System.out.println("4. Back to main menu");
            System.out.print("Choice: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1": balanceForDateRange(); break;
                case "2": balanceTrend(); break;
                case "3": transactionsByTag(); break;
                case "4": inStatsMenu = false; break;
                default: System.out.println("Invalid choice");
            }
        }
    }

    private void balanceForDateRange() {
        try {
            System.out.print("Start date [yyyy-MM-dd]: ");
            LocalDate start = LocalDate.parse(scanner.nextLine());

            System.out.print("End date [yyyy-MM-dd] (enter for today): ");
            String endInput = scanner.nextLine();
            LocalDate end = endInput.isEmpty() ? LocalDate.now() : LocalDate.parse(endInput);

            System.out.print("Type [1] GUADAGNO [2] SPESA [3] ALL: ");
            String typeInput = scanner.nextLine();
            MovementType type = typeInput.equals("1") ? MovementType.GUADAGNO :
                    typeInput.equals("2") ? MovementType.SPESA : null;

            double balance = ledger.balanceForDates(type, start, end);
            System.out.printf("Balance for %s to %s: %.2f€%n", start, end, balance);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void balanceTrend() {
        try {
            System.out.print("Start date [yyyy-MM-dd]: ");
            LocalDate start = LocalDate.parse(scanner.nextLine());

            System.out.print("End date [yyyy-MM-dd] (enter for today): ");
            String endInput = scanner.nextLine();
            LocalDate end = endInput.isEmpty() ? LocalDate.now() : LocalDate.parse(endInput);

            Pair<Boolean, Double> trend = ledger.trendBalance(start, end).apply(ledger.getTransaction());
            String trendIcon = trend.getFirst() ? "📈" : "📉";
            System.out.printf("%s Trend: %.2f€ (%s)%n", trendIcon, trend.getSecond(),
                    trend.getFirst() ? "Positive" : "Negative");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void transactionsByTag() {
        System.out.print("Type [1] GUADAGNO [2] SPESA: ");
        MovementType type = scanner.nextLine().equals("1") ? MovementType.GUADAGNO : MovementType.SPESA;

        HashMap<String, Double> balances = ledger.balanceForEachTag(type);
        System.out.println("\n=== BALANCE BY TAG ===");

        balances.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(Math.abs(e2.getValue()), Math.abs(e1.getValue())))
                .forEach(entry -> System.out.printf("%-20s: %8.2f€%n", entry.getKey(), entry.getValue()));
    }

    // ==================== METODI ESISTENTI (aggiornati) ====================

    private void transactionByDate() {
        try {
            System.out.println("[1] GUADAGNO, [2] SPESA: ");
            MovementType type = scanner.nextLine().equals("1") ? MovementType.GUADAGNO : MovementType.SPESA;

            System.out.println("Start date [yyyy-MM-dd]: ");
            LocalDate start = LocalDate.parse(scanner.nextLine());

            System.out.println("End date [yyyy-MM-dd] (enter for today): ");
            String endInput = scanner.nextLine();
            LocalDate end = endInput.isEmpty() ? LocalDate.now() : LocalDate.parse(endInput);

            double balance = ledger.balanceForDates(type, start, end);
            System.out.printf("%s in range [%s, %s]: %.2f€%n", type, start, end, balance);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void transactionByTag() {
        try {
            System.out.println("[1] GUADAGNO, [2] SPESA: ");
            MovementType type = scanner.nextLine().equals("1") ? MovementType.GUADAGNO : MovementType.SPESA;

            System.out.println("Tag: ");
            String tag = scanner.nextLine();

            double balance = ledger.balanceForTag(type, tag);
            System.out.printf("Total %s for tag '%s': %.2f€%n", type, tag, balance);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void transactionTrend() {
        try {
            System.out.println("Start date [yyyy-MM-dd]: ");
            LocalDate start = LocalDate.parse(scanner.nextLine());

            System.out.println("End date [yyyy-MM-dd] (enter for today): ");
            String endInput = scanner.nextLine();
            LocalDate end = endInput.isEmpty() ? LocalDate.now() : LocalDate.parse(endInput);

            Pair<Boolean, Double> trend = ledger.trendBalance(start, end).apply(ledger.getTransaction());
            System.out.printf("Trend [%s to %s]: %s (%.2f€)%n",
                    start, end, trend.getFirst() ? "Positive" : "Negative", trend.getSecond());

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void transactionForEachTag() {
        System.out.println("[1] GUADAGNO, [2] SPESA: ");
        MovementType type = scanner.nextLine().equals("1") ? MovementType.GUADAGNO : MovementType.SPESA;

        HashMap<String, Double> balances = ledger.balanceForEachTag(type);
        System.out.println("Tag         | Amount");
        System.out.println("---------------------");

        balances.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(Math.abs(e2.getValue()), Math.abs(e1.getValue())))
                .forEach(entry -> System.out.printf("%-12s| %8.2f€%n", entry.getKey(), entry.getValue()));
    }

    // ==================== METODI INTERFACCIA ====================

    @Override
    public void open() {
        this.ledger.read();
        this.ledger.updateBudgets();

        while (flag) {
            menu();
            System.out.print("Option: ");
            String option = scanner.nextLine();

            try {
                choice(option);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    @Override
    public void close() {
        flag = false;
        System.out.println("Application closed.");
        System.exit(0);
    }
}