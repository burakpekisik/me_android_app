# ME Android App

This project is a simple Android application that uses **WebView** to display a website in a mobile-friendly format. It doesn't include any local database; instead, it focuses on converting a web-based interface into a mobile application for easy access via Android devices.

## Table of Contents

- [Features](#features)
- [Technologies Used](#technologies-used)
- [Installation](#installation)
- [Usage](#usage)
- [License](#license)

## Features

- **WebView Integration**: Renders a website directly within the mobile application.
- **Mobile-friendly**: Converts a web interface into a mobile experience.
- **Basic Navigation**: Supports in-app navigation and handling of basic browser functions (back, forward, refresh).

## Technologies Used

- **Java/Kotlin**: The primary languages used for Android development.
- **WebView**: An Android widget that allows for rendering web content inside the app.

## Installation

To run the project on your local machine:

1. **Clone the repository:**
    ```bash
    git clone https://github.com/burakpekisik/me_android_app.git
    ```

2. **Open the project in Android Studio.**

3. **Build the project:**
    Android Studio will automatically resolve the dependencies and build the project.

4. **Run the application:**
    You can run the application on an Android emulator or a connected physical device.

## Usage

The application loads the specified website inside the app using WebView. Users can interact with the web content as if it were a native mobile app, including navigation between pages, form submissions, and other web-based features.

To change the website being displayed, modify the URL in the WebView setup code within the app.

### Example WebView Setup (in MainActivity.java or MainActivity.kt):

```java
WebView webView = findViewById(R.id.webview);
webView.getSettings().setJavaScriptEnabled(true);
webView.loadUrl("https://yourwebsite.com");
```

## License

This project is licensed under the MIT License. See the [LICENSE](./LICENSE) file for more details.
