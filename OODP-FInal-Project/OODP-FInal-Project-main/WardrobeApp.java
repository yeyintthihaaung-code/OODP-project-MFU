import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * WardrobeApp - Main application for managing your wardrobe
 * Features: Add items, search, filter, mark worn, create outfits, generate random outfits
 */
public class WardrobeApp {
    private static final String DATA_FILE = "wardrobe.txt";

    private Scanner scanner;
    private Wardrobe wardrobe;
    private List<Outfit> outfits;
    private DateTimeFormatter dateFormat;

    // Color codes for terminal output
    private static final String RESET = "\033[0m";
    private static final String BOLD = "\033[1m";
    private static final String GREEN = "\033[92m";
    private static final String BLUE = "\033[94m";
    private static final String YELLOW = "\033[93m";
    private static final String RED = "\033[91m";
    private static final String CYAN = "\033[96m";

    public WardrobeApp() {
        this.scanner = new Scanner(System.in);
        this.wardrobe = new Wardrobe();
        this.outfits = new ArrayList<>();
        this.dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    }

    public void run() {
        showWelcome();
        loadWardrobe();
        boolean running = true;
        while (running) {
            displayMenu();
            int choice = getIntInput();
            switch (choice) {
                case 1: addClothingItem(); break;
                case 2: wardrobe.displayAllItems(); pause(); break;
                case 3: searchItem(); break;
                case 4: filterByCategory(); break;
                case 5: markItemAsWorn(); break;
                case 6: wardrobe.displayLaundryList(); pause(); break;
                case 7: editItem(); break;
                case 8: createOutfit(); break;
                case 9: viewOutfits(); break;
                case 10: randomOutfit(); break;
                case 11:
                    running = false;
                    saveWardrobe();
                    System.out.println("\n" + CYAN + "👋 Goodbye!\n" + RESET);
                    break;
                default: System.out.println(RED + "❌ Invalid choice.\n" + RESET);
            }
        }
    }

    private void loadWardrobe() {
        try {
            wardrobe.loadFromFile(DATA_FILE);
            if (!wardrobe.getItems().isEmpty()) {
                System.out.println(GREEN + "📂 Loaded " + wardrobe.getItems().size()
                        + " item(s) from " + DATA_FILE + RESET);
            }
        } catch (IOException e) {
            System.out.println(RED + "❌ Could not read " + DATA_FILE + ": " + e.getMessage() + RESET);
        } catch (WardrobeException e) {
            System.out.println(RED + "❌ Data file is corrupt: " + e.getMessage() + RESET);
        }
    }

    private void saveWardrobe() {
        try {
            wardrobe.saveToFile(DATA_FILE);
            System.out.println(GREEN + "💾 Saved " + wardrobe.getItems().size()
                    + " item(s) to " + DATA_FILE + RESET);
        } catch (IOException e) {
            System.out.println(RED + "❌ Failed to save: " + e.getMessage() + RESET);
        }
    }

    private void showWelcome() {
        System.out.println("\n" + CYAN + "========================================" + RESET);
        System.out.println(BOLD + BLUE + "  Welcome to Wardrobe Manager!" + RESET);
        System.out.println(CYAN + "========================================" + RESET);
        System.out.println("\n📍 Available Categories:");
        for (Category cat : Category.values()) {
            System.out.println("   " + YELLOW + cat.getMenuNumber() + ". " + cat.getDisplayName() + RESET);
        }
        System.out.println("\n💡 Tip: Set wear threshold (e.g., 5x) before laundry.\n");
    }

    private void displayMenu() {
        System.out.println("\n" + CYAN + "--- Wardrobe Manager ---" + RESET);
        System.out.println("1. Add Clothing Item");
        System.out.println("2. View All Items");
        System.out.println("3. Search Item");
        System.out.println("4. Filter by Category");
        System.out.println("5. Mark Item as Worn");
        System.out.println("6. View Laundry List");
        System.out.println("7. Edit Item");
        System.out.println("8. Create Outfit");
        System.out.println("9. View Outfits");
        System.out.println("10. Random Outfit");
        System.out.println("11. Exit");
        System.out.print("\nChoose (1-11): ");
    }

    private void addClothingItem() {
        System.out.print("\n📝 Item name (or 'back'): ");
        String name = scanner.nextLine().trim();
        if (name.equalsIgnoreCase("back")) return;

        Category category = selectCategory("Add");
        if (category == null) return;

        System.out.print("Color: ");
        String color = scanner.nextLine().trim();
        System.out.print("Size: ");
        String size = scanner.nextLine().trim();

        LocalDate purchaseDate = getDateInput("Purchase Date (DD-MM-YYYY, or 'back'): ");
        if (purchaseDate == null) return;

        LocalDate lastWornDate = getDateInput("Last Worn Date (DD-MM-YYYY, or 'back'): ");
        if (lastWornDate == null) return;

        double price = getPositiveDoubleInput("Purchase Price ($): ");
        int threshold = getPositiveIntInput("Times to wear before laundry: ");

        try {
            wardrobe.addItem(new ClothingItem(name, category, color, size, purchaseDate, lastWornDate, price, threshold));
            System.out.println(GREEN + "\n✅ Item added!" + RESET);
        } catch (WardrobeException e) {
            System.out.println(RED + "\n❌ " + e.getMessage() + RESET);
        }
    }

    private void searchItem() {
        System.out.print("\n🔍 Enter item name: ");
        String name = scanner.nextLine().trim();
        try {
            ClothingItem item = wardrobe.searchByName(name);
            System.out.println("\n" + "=".repeat(90));
            System.out.println(item);
        } catch (WardrobeException e) {
            System.out.println(RED + "\n❌ " + e.getMessage() + RESET);
        }
    }

    private void filterByCategory() {
        Category category = selectCategory("Filter");
        if (category != null) {
            wardrobe.displayByCategory(category);
        }
    }

    private void markItemAsWorn() {
        if (wardrobe.getItems().isEmpty()) {
            System.out.println(RED + "\n❌ No items available." + RESET);
            return;
        }
        wardrobe.displayItemsForSelection(BOLD + "--- Mark Item as Worn ---" + RESET);
        System.out.println("0. Cancel\n");
        System.out.print("Select item number: ");
        int choice = getIntInput();
        if (choice > 0 && choice <= wardrobe.getItems().size()) {
            ClothingItem item = wardrobe.getItems().get(choice - 1);
            item.incrementTimesWorn();
            System.out.println(GREEN + "\n✅ Marked '" + item.getName() + "' as worn! (Total: " + item.getTimesWorn() + "x)" + RESET);
            if (item.getTimesWorn() >= item.getWearThresholdForLaundry()) {
                System.out.println(YELLOW + "⚠️  This item needs laundry!" + RESET);
            }
        }
    }

    private void editItem() {
        if (wardrobe.getItems().isEmpty()) {
            System.out.println(RED + "\n❌ No items to edit." + RESET);
            return;
        }
        boolean editing = true;
        while (editing) {
            wardrobe.displayItemsForSelection(BOLD + "--- Select Item to Edit ---" + RESET);
            System.out.println("0. Exit\n");
            System.out.print("Select item number: ");
            int choice = getIntInput();
            if (choice == 0) {
                editing = false;
            } else if (choice > 0 && choice <= wardrobe.getItems().size()) {
                editItemFields(wardrobe.getItems().get(choice - 1));
            }
        }
    }

    private void editItemFields(ClothingItem item) {
        System.out.println("\n" + BOLD + "--- Edit: " + item.getName() + " ---" + RESET);
        System.out.println("1. Color (Current: " + item.getColor() + ")");
        System.out.println("2. Size (Current: " + item.getSize() + ")");
        System.out.println("3. Last Worn Date (Current: " + (item.getLastWornDate() == null ? "Never" : item.getLastWornDate()) + ")");
        System.out.println("4. Price (Current: $" + item.getPurchasePrice() + ")");
        System.out.println("5. Times Worn (Current: " + item.getTimesWorn() + ")");
        System.out.print("\nSelect field (1-5): ");
        int field = getIntInput();
        switch (field) {
            case 1:
                System.out.print("New color: ");
                item.setColor(scanner.nextLine().trim());
                System.out.println(GREEN + "✅ Updated!" + RESET);
                saveWardrobe();
                break;
            case 2:
                System.out.print("New size: ");
                item.setSize(scanner.nextLine().trim());
                System.out.println(GREEN + "✅ Updated!" + RESET);
                saveWardrobe();
                break;
            case 3:
                LocalDate newDate = getDateInput("New date (DD-MM-YYYY): ");
                if (newDate != null) {
                    item.setLastWornDate(newDate);
                    System.out.println(GREEN + "✅ Updated!" + RESET);
                    saveWardrobe();
                }
                break;
            case 4:
                double newPrice = getPositiveDoubleInput("New price ($): ");
                item.setPurchasePrice(newPrice);
                System.out.println(GREEN + "✅ Updated!" + RESET);
                saveWardrobe();
                break;
            case 5:
                int newTimesWorn = getPositiveIntInput("New times worn: ");
                item.setTimesWorn(newTimesWorn);
                System.out.println(GREEN + "✅ Updated!" + RESET);
                saveWardrobe();
                break;
        }
        displayUpdatedItem(item);
    }

    private void displayUpdatedItem(ClothingItem item) {
        System.out.println("\n" + CYAN + "--- Updated Item Details ---" + RESET);
        System.out.println(BOLD + "Name: " + RESET + item.getName());
        System.out.println(BOLD + "Category: " + RESET + item.getCategory());
        System.out.println(BOLD + "Color: " + RESET + item.getColor());
        System.out.println(BOLD + "Size: " + RESET + item.getSize());
        System.out.println(BOLD + "Purchase Date: " + RESET + item.getPurchaseDate());
        System.out.println(BOLD + "Last Worn Date: " + RESET + (item.getLastWornDate() == null ? "Never" : item.getLastWornDate()));
        System.out.println(BOLD + "Price: " + RESET + "$" + item.getPurchasePrice());
        System.out.println(BOLD + "Times Worn: " + RESET + item.getTimesWorn());
    }

    private void createOutfit() {
        if (wardrobe.getItems().isEmpty()) {
            System.out.println(RED + "\n❌ No items available." + RESET);
            return;
        }
        System.out.print("\n👔 Outfit name: ");
        String outfitName = scanner.nextLine().trim();
        if (outfitName.equalsIgnoreCase("back")) return;

        Outfit outfit = new Outfit(outfitName);
        boolean adding = true;
        while (adding) {
            wardrobe.displayItemsForSelection(BOLD + "--- Add Items to Outfit ---" + RESET);
            System.out.println("0. Done\n");
            System.out.print("Select item number: ");
            int choice = getIntInput();
            if (choice == 0) {
                adding = false;
            } else if (choice > 0 && choice <= wardrobe.getItems().size()) {
                ClothingItem item = wardrobe.getItems().get(choice - 1);
                if (outfit.hasCategory(item.getCategory())) {
                    System.out.println(YELLOW + "⚠️  Already has " + item.getCategory() + "!" + RESET);
                } else if (outfit.addItem(item)) {
                    System.out.println(GREEN + "✅ Added " + item.getName() + RESET);
                }
            }
        }
        if (!outfit.getItems().isEmpty()) {
            outfits.add(outfit);
            System.out.println(GREEN + "\n✅ Outfit created!" + RESET);
        }
    }

    private void viewOutfits() {
        if (outfits.isEmpty()) {
            System.out.println(YELLOW + "\n📭 No outfits created yet." + RESET);
        } else {
            Wardrobe.displayAll(outfits);
        }
    }

    private void randomOutfit() {
        Outfit outfit = wardrobe.generateRandomOutfit();
        if (outfit == null) {
            System.out.println(RED + "\n❌ Not enough items for random outfit." + RESET);
        } else {
            outfit.display();
            System.out.print("\n💾 Save this outfit? (yes/no): ");
            String response = scanner.nextLine().trim().toLowerCase();
            if (response.equals("yes") || response.equals("y")) {
                System.out.print("Outfit name: ");
                String name = scanner.nextLine().trim();
                Outfit saved = new Outfit(name);
                for (ClothingItem item : outfit.getItems()) {
                    saved.addItem(item);
                }
                outfits.add(saved);
                System.out.println(GREEN + "✅ Outfit saved!" + RESET);
            }
        }
    }

    private Category selectCategory(String context) {
        while (true) {
            System.out.println("\n" + BOLD + "Select Category:" + RESET);
            for (Category cat : Category.values()) {
                System.out.println(cat.getMenuNumber() + ". " + cat.getDisplayName());
            }
            System.out.print("Choose (1-6, or 'back'): ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("back")) return null;
            try {
                int choice = Integer.parseInt(input);
                Category cat = Category.fromMenuNumber(choice);
                if (cat != null) return cat;
            } catch (NumberFormatException e) {}
            System.out.println(RED + "❌ Invalid choice." + RESET);
        }
    }

    private LocalDate getDateInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("back")) return null;
            try {
                return LocalDate.parse(input, dateFormat);
            } catch (Exception e) {
                System.out.println(RED + "❌ Invalid format. Use DD-MM-YYYY." + RESET);
            }
        }
    }

    private double getPositiveDoubleInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                double value = scanner.nextDouble();
                scanner.nextLine();
                if (value >= 0) return value;
            } catch (Exception e) {
                scanner.nextLine();
            }
            System.out.println(RED + "❌ Please enter positive number." + RESET);
        }
    }

    private int getPositiveIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = scanner.nextInt();
                scanner.nextLine();
                if (value > 0) return value;
            } catch (Exception e) {
                scanner.nextLine();
            }
            System.out.println(RED + "❌ Please enter number > 0." + RESET);
        }
    }

    private int getIntInput() {
        try {
            int value = scanner.nextInt();
            scanner.nextLine();
            return value;
        } catch (Exception e) {
            scanner.nextLine();
            return -1;
        }
    }

    private void pause() {
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }

    public static void main(String[] args) {
        WardrobeApp app = new WardrobeApp();
        app.run();
    }
}
