# News App

Welcome to the News Aggregator app! This Android application provides a convenient way to stay updated with the latest news headlines. The app fetches the top headlines from various countries, allows users to swipe to delete or pin articles, and introduces a dynamic update feature to display new headlines regularly.

## Essential Features

1. **Splash Screen and Initial View:**
   - Displays a splash logo on app load.
   - After the splash screen, shows a list view with the first 10 headlines.

2. **Dynamic List Update:**
   - Sets up a timer that introduces a new batch of up to 5 random headlines to the top of the list every 10 seconds.
   - Allows users to manually trigger fetching the next batch from local storage and resetting the drip timer.

3. **Fetching and Storing Headlines:**
   - Implements a background task to fetch the top 100 news headlines from the [NewsAPI](https://newsapi.org).
   - Stores headlines in local storage for offline access.

4. **User Interaction:**
   - Allows users to swipe a headline to delete it or pin it to the top of the view.
   - Pinned headlines stay in view when the list updates, whether manually or automatically.
   - Deleting a headline removes it from view, with the next headline appearing at the top of the list.

5. **Multiple Country Support:**
   - Fetches headlines from multiple countries to ensure a total of 100 headlines in a single request.

## Technologies Used

- **Language:** Kotlin
- **Network Requests:** Retrofit
- **Database:** Room
- **Image Loading:** Coil
- **Swipe Actions:** [Swipe](https://github.com/saket/Swipe)
- **Web View:** Android WebView
- **Threading:** Kotlin Coroutines

## 3rd Party Dependencies

- [Retrofit](https://square.github.io/retrofit/): For making network requests.
- [Coil](https://coil-kt.github.io/coil/): For loading and caching images.
- [Swipe](https://github.com/saket/Swipe): For implementing swipe actions in the UI.

## Architecture

The app follows the MVVM (Model-View-ViewModel) architecture pattern:

- **Model:** Represents the data and business logic, including the repository and data access objects.
- **View:** Displays the UI and interacts with the user.
- **ViewModel:** Acts as a bridge between the Model and the View, handling UI-related logic.

## API Endpoints

### NewsAPI Endpoints

#### 1. Get Top 100 Headlines
- **Endpoint:** `/v2/top-headlines`
- **Method:** `GET`
- **Parameters:**
  - `country`: Country code (e.g., `us`, `in`).
  - `apiKey`: API key for authentication.
  - `pageSize`: Number of headlines per page (default is 100).

### Room Database Queries

#### 1. Get All Articles
- **Query:** `SELECT * FROM NewsArticle`
- **Description:** Retrieves all stored news articles from the local database.

#### 2. Insert Articles
- **Query:** `INSERT INTO NewsArticle VALUES (...)`
- **Description:** Inserts a list of news articles into the local database.

#### 3. Delete All Articles
- **Query:** `DELETE FROM NewsArticle`
- **Description:** Deletes all stored news articles from the local database.

## Installation

To install and run the app, follow these steps:

1. Clone the repository:

   ```bash
   
