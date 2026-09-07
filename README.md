# Clothes Management System (Wardrobe Manager)

A Java console application designed to help users catalog, organize, and manage their wardrobe efficiently. The system tracks clothing details, monitors wear cycles to notify users when items need washing, and offers automated or custom outfit generation.

---

## Key Features

* **Inventory Tracking:** Log details including category, color, size, purchase date, purchase price, and wear frequency.
* **Maintenance & Laundry Tracker:** Set custom wear thresholds per item to trigger laundry alerts automatically when items reach their wear limit.
* **Outfit Generation:** Create custom outfits or automatically build randomized outfits based on single-item-per-category rules.
* **Filter & Search:** Search items by name or filter inventory by specific categories (Top Wear, Lower Wear, Footwear, etc.).
* **Data Persistence:** Load and save wardrobe data automatically to `wardrobe.txt`.

---

## Class Architecture

| Class / Interface | Type | Role & Responsibilities |
| :--- | :--- | :--- |
| Displayable | Interface | Defines the standard contract (display()) for isual console presentation. |
| ClothingItem | Class | Implements Displayable. Models individual clothing items with wear tracking, metadata, and file serialization logic. |
| Outfit | Class | Implements Displayable. Aggregates multiple unique ClothingItem objects into a styled set (max 1 per category). |
| Wardrobe | Class | Manages the collection of ClothingItem objects. Handles searching, filtering, laundry logic, file I/O, and random outfit generation. |
| WardrobeApp | Class | Main entry point containing the terminal menu interface, user input handling, and app flow. |
| Category | Enum | Defines clothing categories (UPPER, TOP_WEAR, LOWER, FULL_BODY, UNDERWEAR, FOOTWEAR). |
| WardrobeException | Exception | Custom checked exception for handling system, storage, and validation errors cleanly. |

---

## Getting Started

### Prerequisites
* Java Development Kit (JDK): Version 8 or higher.

### Compilation & Execution

1. Clone or download all Java source files into a single directory.
2. Open a terminal in the directory containing the source files.
3. Compile all Java components:
   javac *.java

4. Run the application:
   java WardrobeApp

---

## Data Storage

The application automatically reads from and writes to wardrobe.txt in the root execution directory. If the file exists upon launch, stored items are loaded into the wardrobe automatically. All changes are saved upon exiting through menu option 11.
