# AI Superstore Manager (سپر اسٹور مینیجر) - UI Layout & Screenshots Guide

Here are visual text wireframes illustrating the user interface design across the main screens of the application.

---

## 1. Home Dashboard (`HomeScreen.kt`)

```
+-------------------------------------------------------------+
| AI Superstore Manager                         [en | اردو]   |
+-------------------------------------------------------------+
| [Card: Dashboard Overview]                                  |
|   Dashboard / ڈیش بورڈ                                      |
|   Backup & Sync Status: Offline Ready                       |
+-------------------------------------------------------------+
| Quick Actions / فوری اقدامات                                 |
| +-------------------------+   +---------------------------+ |
| | [AddShoppingCart]       |   | [ReceiptLong]             | |
| | New Sale                |   | Import Bill               | |
| | (نئی سیل کریں)          |   | (بل امپورٹ کریں)          | |
| +-------------------------+   +---------------------------+ |
| +-------------------------+   +---------------------------+ |
| | [Add]                   |   | [MoveToInbox]             | |
| | Add Product             |   | Receive Stock             | |
| | (پروڈکٹ شامل کریں)      |   | (نیا اسٹاک حاصل کریں)     | |
| +-------------------------+   +---------------------------+ |
+-------------------------------------------------------------+
| Store Summary / صورتحال                                     |
| +-------------------------+   +---------------------------+ |
| | [AttachMoney]           |   | [TrendingUp]              | |
| | Today's Sales           |   | Est. Gross Profit         | |
| | Rs. 14,500.00           |   | Rs. 2,175.00              | |
| | 18 Sales Today          |   | Est. Margin               | |
| +-------------------------+   +---------------------------+ |
| +-------------------------+   +---------------------------+ |
| | [Category]              |   | [PendingActions]          | |
| | Active Products         |   | Pending Bills             | |
| | 124 in Catalogue        |   | 2 Awaiting Review         | |
| +-------------------------+   +---------------------------+ |
| +-------------------------+   +---------------------------+ |
| | [Warning] (Orange)      |   | [Error] (Red)             | |
| | Low Stock Alerts        |   | Out of Stock              | |
| | 4 Items Below Min       |   | 1 Item Empty              | |
| +-------------------------+   +---------------------------+ |
+-------------------------------------------------------------+
| [Home] [Products]  [Sales]  [Bill Import]  [Reports] [Set]  |
+-------------------------------------------------------------+
```

---

## 2. Product Catalogue (`ProductCatalogueScreen.kt`)

```
+-------------------------------------------------------------+
| Product Catalogue / مصنوعات کی فہرست                        |
+-------------------------------------------------------------+
| [Search: Search by English / Urdu name, barcode...]    [X]  |
+-------------------------------------------------------------+
| [Item Card 1]                                               |
| +-------+  Nestle Milkpak 1L           نسلے ملَک پیک        |
| | [IMG] |  Nestle • Dairy                                   |
| |       |  Price: Rs. 290.00 / l        Stock: 45 l [Green] |
| +-------+  Valuation: Cost Rs. 11,700 | Potential Rs. 13,050 |
|                                                    [Edit]   |
+-------------------------------------------------------------+
| [Item Card 2]                                               |
| +-------+  Sugar (چینی)                  چینی               |
| | [BAG] |  Refined • Grocery                                |
| |       |  Price: Rs. 160.00 / kg      Stock: 2 kg [Orange]|
| +-------+  Valuation: Cost Rs. 290  | Potential Rs. 320     |
|                                                    [Edit]   |
+-------------------------------------------------------------+
|                                                      [ + ]  |
+-------------------------------------------------------------+
```

---

## 3. Manual Sales & Cart (`ManualSalesScreen.kt`)

```
+-------------------------------------------------------------+
| Manual Sales / مینول فروخت                                  |
+-------------------------------------------------------------+
| [Search: Product name in English or Urdu...]               |
+-------------------------------------------------------------+
| Sugar (چینی)                                                |
| Rs. 160.00 / kg | Stock: 25.5 kg               [ 2.5 ] [+]  |
+-------------------------------------------------------------+
| Cooking Oil 1L (کوکنگ آئل)                                 |
| Rs. 550.00 / l  | Stock: 10 l                  [ 1   ] [+]  |
+-------------------------------------------------------------+
|                                                             |
|=============================================================|
| Cart (2 items)                          Total: Rs. 950.00   |
|                                       [ View Cart / Checkout ]|
+-------------------------------------------------------------+
```

---

## 4. Handwritten Bill Review (`BillReviewScreen.kt`)

```
+-------------------------------------------------------------+
| <-- Bill Review & Confirm / بل کا جائزہ                      |
+-------------------------------------------------------------+
| Bill #104 - Status: PENDING                                 |
| Review extracted handwritten lines before confirming.       |
+-------------------------------------------------------------+
| [Extracted Line 1]                                          |
| Original Handwritten: "چینی 2 کلو"                          |
| Extracted Qty: 2 kg                                         |
| Matched: Sugar (چینی)                       [Dropdown  v]  |
+-------------------------------------------------------------+
| [Extracted Line 2]                                          |
| Original Handwritten: "2 pck milkpak"                       |
| Extracted Qty: 2 pcs                                        |
| Matched: Nestle Milkpak 1L                  [Dropdown  v]  |
+-------------------------------------------------------------+
| [ Confirm & Deduct Stock ] (Atomic inventory deduction)     |
+-------------------------------------------------------------+
```

---

## 5. Sales & Financial Reports (`ReportsScreen.kt`)

```
+-------------------------------------------------------------+
| Reports / رپورٹس                              [ Export CSV ]|
+-------------------------------------------------------------+
| [Card: Sales & Profit Summary / منافع اور آمدن]              |
|   Sales Revenue             Est. Gross Profit               |
|   Rs. 45,200.00             Rs. 6,780.00 (Green)            |
|   Cost of Goods Sold (COGS): Rs. 38,420.00                  |
|   Total Recorded Transactions: 34                           |
+-------------------------------------------------------------+
| [Card: Stock Valuation / اسٹاک کی مالیت]                     |
|   Total Cost Valuation      Potential Sales Valuation       |
|   Rs. 245,000.00            Rs. 280,000.00                  |
|   Active Products: 124      Low Stock Items: 4 (Red)        |
+-------------------------------------------------------------+
```
