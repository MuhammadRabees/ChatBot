 # 🤖 AI Chatbot (Android)
 This is a modern, native Android chatbot application built with Kotlin. It is inspired by the design of ChatGPT and utilizes the gpt-4o model via OpenRouter to understand both text and image-based messages.

# 📸 Screenshots
<img width="259" height="556" alt="image" src="https://github.com/user-attachments/assets/c3727892-080a-47a4-9ed4-acab796103ef" /> 
<img width="262" height="546" alt="image" src="https://github.com/user-attachments/assets/a492b460-f1c8-4a03-a612-e5b1cb9d3e64" />
<img width="260" height="561" alt="image" src="https://github.com/user-attachments/assets/fbadabd9-37f6-46a5-8024-d42d36404ca6" />

# ✨ FeaturesMultimodal Chat:
Ask questions not just with text, but also about images using gpt-4o vision capabilities.

Camera & Gallery Integration: Attach and send a live photo from the camera or an existing image from the gallery.

Local Chat History: Every chat session, along with its messages, is saved locally on the user's phone using a Room Database.
Session Management: A ChatGPT-like side panel (Navigation Drawer) where all your past chat history is accessible.
"New Chat" Functionality: The app always starts with a fresh, blank "New Chat" screen.
"Thinking..." Indicator: Displays a "Thinking..." or "Analyzing image..." message while waiting for the bot's response.
Modern Android UI: Built with DrawerLayout, RecyclerView, and ViewBinding.

# 🛠️ Tech Stack & Libraries
Language:Kotlin
Architecture: MVVM (Model-View-ViewModel)
Asynchronous: Kotlin Coroutines
Database: Room DB (For local session and message history)
Networking: Retrofit 2 & OkHttp (For API calls)
Image Loading: Coil (To load images in chat bubbles and previews)
UI: ViewBinding, Material Design (DrawerLayout), RecyclerView
Image Picking: ActivityResultContracts (The modern way to handle Camera and Gallery)

# 🚀 Getting Started
You only need 3 steps to run this project:
# 1. Clone the repository
git clone [https://github.com/your-username/your-repo-name.git](https://github.com/your-username/your-repo-name.git)
# 2. Get API Key
   This app uses OpenRouter.ai for its API calls.
   Create an account on OpenRouter.Copy your API Key.
# 3. Create local.properties file
In your project's root folder (where gradle.properties is located), create a new file named local.properties.Add your API key to this file as follows:
OPENAI_API_KEY="sk-or-your-openrouter-api-key-here"
Your build.gradle file will automatically pick up this key.
# 4. Build & Run
That's it! You can now build and run the project in Android Studio.
  
# 🏛️ Architecture
This project follows a clean MVVM architecture:
View (Activity/Fragment): MainActivity.kt which only handles UI events (clicks) and observes data from the ViewModel.
ViewModel (ChatViewModel.kt): Acts as a bridge between the UI and the Repository. It handles all business logic (like managing sessions).
Repository (ChatRepository.kt): The single source of truth for data. It decides whether to fetch data from the Room Database (local cache) or Retrofit (remote network).
Model (Message.kt, ChatSession.kt): Database tables (@Entity) and data classes.

# 📄 LicenseMIT License
Copyright (c) 2025 [Your Name]

Permission is hereby granted, free of charge, to any person obtaining a copy
... (MIT License text) ...

