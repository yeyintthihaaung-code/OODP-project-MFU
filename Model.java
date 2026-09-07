import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Custom checked exception for wardrobe-related errors
 * (duplicates, missing items, corrupt file data, etc.).
 */
class WardrobeException extends Exception {
    public WardrobeException(String message) {
        super(message);
    }
}

/**
 * Displayable - interface implemented by every printable wardrobe entity.
 * Enables parametric polymorphism: a single generic method can display
 * any List<T extends Displayable> without caring about the concrete type.
 */
interface Displayable {
    void display();
}

/**
 * Category Enum - Defines all valid clothing categories
 */
enum Category {
    UPPER("Upper", 1),
    TOP_WEAR("TopWear", 2),
    LOWER("Lower", 3),
    FULL_BODY("FullBody", 4),
    UNDERWEAR("Underwear", 5),
    FOOTWEAR("Footwear", 6);

    private final String displayName;
    private final int menuNumber;

    Category(String displayName, int menuNumber) {
        this.displayName = displayName;
        this.menuNumber = menuNumber;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMenuNumber() {
        return menuNumber;
    }

    public static Category fromMenuNumber(int number) {
        for (Category cat : Category.values()) {
            if (cat.menuNumber == number) return cat;
        }
        return null;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

/**
 * ClothingItem - Represents a single clothing item
 */
class ClothingItem implements Displayable {
    private String name;
    private Category category;
    private String color;
    private String size;
    private LocalDate purchaseDate;
    private LocalDate lastWornDate;
    private double purchasePrice;
    private int timesWorn;
    private int wearThresholdForLaundry;

    public ClothingItem(String name, Category category, String color, String size,
                        LocalDate purchaseDate, LocalDate lastWornDate,
                        double purchasePrice, int wearThresholdForLaundry) {
        this.name = name;
        this.category = category;
        this.color = color;
        this.size = size;
        this.purchaseDate = purchaseDate;
        this.lastWornDate = lastWornDate;
        this.purchasePrice = purchasePrice;
        this.timesWorn = 0;
        this.wearThresholdForLaundry = wearThresholdForLaundry;
    }

    // Getters
    public String getName() { return name; }
    public Category getCategory() { return category; }
    public String getColor() { return color; }
    public String getSize() { return size; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public LocalDate getLastWornDate() { return lastWornDate; }
    public double getPurchasePrice() { return purchasePrice; }
    public int getTimesWorn() { return timesWorn; }
    public int getWearThresholdForLaundry() { return wearThresholdForLaundry; }

    // Setters
    public void setColor(String color) { this.color = color; }
    public void setSize(String size) { this.size = size; }
    public void setLastWornDate(LocalDate lastWornDate) { this.lastWornDate = lastWornDate; }
    public void setPurchasePrice(double purchasePrice) { this.purchasePrice = purchasePrice; }
    public void setTimesWorn(int timesWorn) { this.timesWorn = timesWorn; }
    public void setWearThresholdForLaundry(int wearThresholdForLaundry) {
        this.wearThresholdForLaundry = wearThresholdForLaundry;
    }

    public void incrementTimesWorn() { this.timesWorn++; }

    @Override
    public void display() {
        System.out.println(this);
    }

    /** Serialize to a pipe-delimited line for file persistence. */
    public String toFileString() {
        return String.join("|",
                name,
                category.name(),
                color,
                size,
                purchaseDate.toString(),
                lastWornDate == null ? "null" : lastWornDate.toString(),
                String.valueOf(purchasePrice),
                String.valueOf(timesWorn),
                String.valueOf(wearThresholdForLaundry));
    }

    /** Reconstruct a ClothingItem from a serialized file line. */
    public static ClothingItem fromFileString(String line) throws WardrobeException {
        String[] parts = line.split("\\|", -1);
        if (parts.length != 9) {
            throw new WardrobeException("Corrupt data line: " + line);
        }
        try {
            Category cat = Category.valueOf(parts[1]);
            LocalDate purchase = LocalDate.parse(parts[4]);
            LocalDate lastWorn = parts[5].equals("null") ? null : LocalDate.parse(parts[5]);
            double price = Double.parseDouble(parts[6]);
            int worn = Integer.parseInt(parts[7]);
            int threshold = Integer.parseInt(parts[8]);
            ClothingItem item = new ClothingItem(parts[0], cat, parts[2], parts[3],
                    purchase, lastWorn, price, threshold);
            item.setTimesWorn(worn);
            return item;
        } catch (Exception e) {
            throw new WardrobeException("Failed to parse item: " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return String.format("%-20s %-12s %-12s %-8s %-12s %-12s %-10s %-10s %-10s",
                name, category, color, size,
                purchaseDate, lastWornDate == null ? "Never" : lastWornDate,
                String.format("$%.2f", purchasePrice), timesWorn, wearThresholdForLaundry);
    }
}

/**
 * Outfit - Represents a collection of clothing items (max 1 per category)
 */
class Outfit implements Displayable {
    private String name;
    private List<ClothingItem> items;

    public Outfit(String name) {
        this.name = name;
        this.items = new ArrayList<>();
    }

    public boolean addItem(ClothingItem item) {
        for (ClothingItem existing : items) {
            if (existing.getCategory() == item.getCategory()) {
                return false;
            }
        }
        items.add(item);
        return true;
    }

    public boolean hasCategory(Category category) {
        for (ClothingItem item : items) {
            if (item.getCategory() == category) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void display() {
        System.out.println("\n✨ Outfit: " + name);
        for (ClothingItem item : items) {
            System.out.println("  - " + item.getName() + " (" + item.getCategory() + ")");
        }
    }

    public String getName() { return name; }
    public List<ClothingItem> getItems() { return items; }
}

/**
 * Wardrobe - Manages all clothing items
 */
class Wardrobe {
    private List<ClothingItem> items;

    public Wardrobe() {
        this.items = new ArrayList<>();
    }

    /**
     * Generic display method — parametric polymorphism.
     * Works on any List whose element type implements Displayable
     * (ClothingItem, Outfit, or any future Displayable class).
     */
    public static <T extends Displayable> void displayAll(List<T> list) {
        for (T item : list) {
            item.display();
        }
    }

    /** Throws a custom exception if an item with the same name already exists. */
    public void addItem(ClothingItem item) throws WardrobeException {
        for (ClothingItem existing : items) {
            if (existing.getName().equalsIgnoreCase(item.getName())) {
                throw new WardrobeException(
                        "An item named '" + item.getName() + "' already exists.");
            }
        }
        items.add(item);
    }

    public void displayAllItems() {
        if (items.isEmpty()) {
            System.out.println("\n❌ No items found.");
            return;
        }
        System.out.println("\n" + "=".repeat(145));
        System.out.printf("%-20s %-12s %-12s %-8s %-12s %-12s %-10s %-10s %-10s\n",
                "Name", "Category", "Color", "Size", "Purchase", "Last Worn", "Price", "Worn", "Threshold");
        System.out.println("=".repeat(145));
        displayAll(items);
    }

    /** Throws WardrobeException if no item is found. */
    public ClothingItem searchByName(String name) throws WardrobeException {
        for (ClothingItem item : items) {
            if (item.getName().equalsIgnoreCase(name)) {
                return item;
            }
        }
        throw new WardrobeException("Item '" + name + "' not found in wardrobe.");
    }

    public void displayByCategory(Category category) {
        System.out.println("\n" + "=".repeat(145));
        System.out.printf("%-20s %-12s %-12s %-8s %-12s %-12s %-10s %-10s %-10s\n",
                "Name", "Category", "Color", "Size", "Purchase", "Last Worn", "Price", "Worn", "Threshold");
        System.out.println("=".repeat(145));
        boolean found = false;
        for (ClothingItem item : items) {
            if (item.getCategory() == category) {
                System.out.println(item);
                found = true;
            }
        }
        if (!found) {
            System.out.println("❌ No items in this category.");
        }
    }

    public void displayItemsForSelection(String title) {
        if (items.isEmpty()) {
            System.out.println("\n❌ No items available.");
            return;
        }
        System.out.println("\n" + title);
        System.out.println("-".repeat(70));
        System.out.printf("%-4s %-20s %-15s %-20s\n",
                "No.", "Name", "Category", "Color");
        System.out.println("-".repeat(70));
        for (int i = 0; i < items.size(); i++) {
            ClothingItem item = items.get(i);
            System.out.printf("%-4d %-20s %-15s %-20s\n",
                    (i + 1), item.getName(), item.getCategory(), item.getColor());
        }
    }

    public void displayLaundryList() {
        List<ClothingItem> needsLaundry = new ArrayList<>();
        for (ClothingItem item : items) {
            if (item.getTimesWorn() >= item.getWearThresholdForLaundry()) {
                needsLaundry.add(item);
            }
        }

        if (needsLaundry.isEmpty()) {
            System.out.println("\n✅ No items need laundry.");
            return;
        }
        System.out.println("\n" + "=".repeat(90));
        System.out.printf("%-20s %-15s %-20s %-8s %-12s %-12s\n",
                "Name", "Category", "Color", "Size", "Times Worn", "Threshold");
        System.out.println("=".repeat(90));
        for (ClothingItem item : needsLaundry) {
            System.out.printf("%-20s %-15s %-20s %-8s %-12d %-12d\n",
                    item.getName(), item.getCategory(), item.getColor(), item.getSize(),
                    item.getTimesWorn(), item.getWearThresholdForLaundry());
        }
    }

    public Outfit generateRandomOutfit() {
        if (items.isEmpty()) return null;

        Outfit outfit = new Outfit("Random Outfit");
        Random random = new Random();
        List<Category> categories = new ArrayList<>();
        Collections.addAll(categories, Category.values());
        Collections.shuffle(categories, random);

        for (Category cat : categories) {
            List<ClothingItem> categoryItems = new ArrayList<>();
            for (ClothingItem item : items) {
                if (item.getCategory() == cat) {
                    categoryItems.add(item);
                }
            }
            if (!categoryItems.isEmpty()) {
                ClothingItem randomItem = categoryItems.get(random.nextInt(categoryItems.size()));
                outfit.addItem(randomItem);
            }
        }

        return outfit.getItems().isEmpty() ? null : outfit;
    }

    public List<ClothingItem> getItems() { return items; }

    // ---- File I/O ----

    /** Save the entire wardrobe to a plain-text file. */
    public void saveToFile(String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            for (ClothingItem item : items) {
                writer.println(item.toFileString());
            }
            writer.flush();
            if (writer.checkError()) {
                throw new IOException("Failed to write to file: " + filename);
            }
        }
    }

    /** Load wardrobe contents from a file; no-op if the file does not exist. */
    public void loadFromFile(String filename) throws IOException, WardrobeException {
        File file = new File(filename);
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                items.add(ClothingItem.fromFileString(line));
            }
        }
    }
}
