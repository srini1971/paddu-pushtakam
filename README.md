# Paddu Pushtakam 📓

Paddu Pushtakam is a modern, lightweight, and robust Cashbook and Expense Tracker built natively for Android using **Kotlin** and **Jetpack Compose**. It allows users to track their daily cash flow (CASH IN / CASH OUT), organize expenses by categories, generate dynamic reports, and securely export data.

## 🚀 Key Features

*   **Real-time Dashboard:** Instantly view your total balance and today's cash flow (In/Out) dynamically calculated from your transaction history.
*   **Intuitive Logging:** Quickly log 'CASH IN' or 'CASH OUT' transactions. Each transaction supports an amount, description, dynamic date, and payment mode (Cash, UPI, Bank Transfer, Card).
*   **Categorization:** Categorize your entries (e.g., Sales, Salary, Groceries, Rent) for better financial tracking.
*   **Dynamic Reporting:** 
    *   Filter transactions by custom date ranges using dual calendar pickers.
    *   Quick-select durations like *This Month*, *Last Week*, *Single Day*, etc.
*   **Export & Share Engine:**
    *   **PDF Reports:** Generate neatly formatted, tabular A4 PDF documents of your transaction history.
    *   **CSV (Excel) Exports:** Generate comma-separated value files that natively open in Microsoft Excel or Google Sheets.
    *   **Native Sharing:** Automatically integrates with Android's system share sheet to send reports directly via WhatsApp, Gmail, or Google Drive securely via a local `FileProvider`.
*   **Smart "Soft Delete" System:** 
    *   Deleted transactions are immediately hidden from the UI and balance calculations, but are safely retained in the background database for 7 days.
    *   Automated background maintenance purges soft-deleted records older than a week upon app launch.
    *   Option to instantly "Hard Delete" records permanently.

## 🛠️ Architecture & Tech Stack

*   **Language:** Kotlin
*   **UI Toolkit:** Jetpack Compose (Material 3 Design)
*   **Architecture Pattern:** MVVM (Model-View-ViewModel)
*   **Local Database:** Room Database (SQLite wrapper)
    *   Features automated migrations and reactive data streams using Kotlin `StateFlow`.
*   **Concurrency:** Kotlin Coroutines & Flows
*   **Navigation:** Compose Navigation (`androidx.navigation.compose`)
*   **File Management:** Native Android `java.io.File`, `PdfDocument`, and `FileProvider` for secure cross-app data sharing.

## 📂 Project Structure

*   **`MainActivity.kt`**: The entry point. Handles setup, navigation state (`Screen` enum), and theme injection.
*   **`CashbookScreen.kt`**: The primary dashboard view. Displays the summary cards, the main transaction list, and the bottom "IN/OUT" floating buttons. Handles the Long-press delete dialogs.
*   **`CashbookReportScreen.kt`**: The advanced reporting interface. Manages the date-picker states, dynamic filtering logic, and the UI for the Export/Share action buttons.
*   **`ReportGenerator.kt`**: A standalone utility object containing the logic for writing `.csv` text and drawing/painting native `.pdf` tables using Android's graphic canvas.
*   **`TransactionViewModel.kt`**: The central brain connecting the UI to the database. Exposes `allTransactions` as a reactive `StateFlow` and handles background operations like calculating balances and auto-purging old soft-deleted data.
*   **`data/` (Database Layer)**:
    *   `TransactionEntity.kt`: The Room Entity data model representing a single cashbook row.
    *   `TransactionDao.kt`: The Data Access Object containing all SQL queries. Note: Queries actively filter out `deletedAt IS NOT NULL`.
    *   `AppDatabase.kt`: The Room Database configuration and active migration logic (currently Version 4).

## 🔒 Permissions & Security

The app does **not** require internet access or dangerous runtime permissions to operate core features.
*   **Sharing:** To enable sharing reports to other apps (like WhatsApp), the app utilizes an Android `FileProvider` (`res/xml/file_paths.xml`). This safely grants temporary read access to external apps for files located strictly within the app's internal cache directory.

## 🏃 Getting Started (For Developers)

1.  Open the project in **Android Studio (Koala or newer)**.
2.  Ensure you have JDK 17+ configured.
3.  Sync Gradle.
4.  Run `installDebug` via Gradle or click the **Run** button to deploy to an emulator or physical device.

---
*Built with ❤️ using Android Jetpack Compose*
