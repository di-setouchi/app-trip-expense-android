# ✈️ TripExpense (Travel Expense Tracker)

![Kotlin](https://img.shields.io/badge/Kotlin-1.9-blue.svg?logo=kotlin)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Modern%20UI-4285F4?logo=android)
![Coroutines](https://img.shields.io/badge/Coroutines-Flow-green.svg)
![Architecture](https://img.shields.io/badge/Architecture-MVVM%20%2B%20Clean-orange.svg)

**TripExpense** is an Android application designed to help travelers effortlessly track and manage their expenses across multiple currencies.

It focuses on providing a responsive user experience by persisting data locally, while integrating with the Frankfurter API to fetch the latest available exchange rates. The codebase is structured with a focus on data integrity, predictable UI states, and a clear separation of concerns.

<p align="center">
  <img width="250" alt="HomeScreen" src="https://github.com/user-attachments/assets/d8809a3f-f4ad-445e-a8f8-611124786c80" />
  &nbsp;&nbsp;&nbsp;
  <img width="250" alt="TripDetail" src="https://github.com/user-attachments/assets/90e06103-108a-4b4b-b475-37725030fde1" />
  &nbsp;&nbsp;&nbsp;
  <img width="250" alt="EditExpense" src="https://github.com/user-attachments/assets/cdbfa8d4-5638-4ea9-b3cb-b25a1c8c361a" />
</p>

## Key Features
* **Multi-Currency Support**: Add expenses in local currencies. The app automatically calculates the base currency amount using exchange rates.
* **Offline-First**: All trips and expenses are stored locally using Room.
* **Exchange Rate Integration**: Fetches the latest available rates via the [Frankfurter API](https://www.frankfurter.app/) to support currency conversion.
* **Precise Currency Calculation**: Implements Minor Units (Long) and BigDecimal to ensure accuracy and avoid floating-point errors.
## Tech Stack
* **UI**: Jetpack Compose, Material Design 3
* **Architecture**: MVVM, Unidirectional Data Flow (UDF)
* **Asynchronous / Reactive**: Kotlin Coroutines, Flow
* **Local Storage**: Room Database, DataStore (Preferences)
* **Networking**: Retrofit, Moshi
* **Navigation**: Jetpack Navigation Compose

## Architecture & Design Decisions

I built this app focusing on Separation of Concerns and Maintainability.

### 1. Robust Financial Modeling (`AmountUtil`)
Handling money with `Double` or `Float` leads to precision errors. In this app:
* All amounts are stored and calculated in Minor Units (Long) (e.g., $12.50 is stored as `1250`).
* BigDecimal with explicit rounding modes (RoundingMode.HALF_UP) for currency conversions, ensuring consistency across the application.

### 2. Layered Architecture and Domain Mapping
Data models are mapped between layers to keep the UI decoupled from the database structure:
* **Data Layer (`Entity`)**: Stores primitive types (Epoch time, JSON strings) in Room.
* **Domain Layer (`Model`)**: Contains pure Kotlin models (`LocalDate`, `Instant`) representing business rules.
* **UI Layer (`UiModel`)**: ViewModels map Domain models into UI-ready models (e.g., `ExpenseUiModel`), allowing the UI layer (Jetpack Compose) to focus solely on rendering.

### 3. Reactive State Management
ViewModels expose a single `uiState: StateFlow<UiState>` created via `combine()` from Room's Flow. 
For one-off events (like showing a Snackbar after deleting a trip), `SharedFlow` is used to prevent re-triggering events on configuration changes.

## Roadmap (Currently Working On)
I am continuously improving this project to align with production-level standards. My current active tasks are:

- [ ] **Unit Testing**: Adding JUnit4 / MockK tests, focusing heavily on `AmountUtil` boundary values and ViewModel state transitions via Turbine.
- [ ] **Dependency Injection**: Migrating from manual `ViewModelFactory` to **Dagger Hilt** for better scalability.
- [ ] **CI/CD**: Setting up GitHub Actions to run automated tests and lint checks on every Pull Request.

## Contact
**[Shinya Kito]**
- [LinkedIn](https://www.linkedin.com/in/shinya-kito/)
- [GitHub](https://github.com/di-setouchi)
