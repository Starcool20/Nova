# Nova: Your AI Companion for Android

Nova is an advanced AI assistant designed to provide a seamless and interactive experience on Android devices. It operates via a dedicated Vercel-hosted server and is accessible through the Nova Android app. With witty and engaging responses, Nova enhances user experience by facilitating intelligent conversations and executing various commands efficiently.

## Features

- **Conversational AI** – Nova engages in meaningful conversations, responding with humor and intelligence while remembering past interactions.
- **Voice Recognition** – Uses Android's `SpeechRecognizer` API for accurate speech processing.
- **Command Execution** – Supports various system-level tasks, including:
  - **Calling** – Example: `Call 00000000` (Requires call permission)
  - **Messaging** – Compatible with SMS, WhatsApp, and Telegram.
  - **Setting Alarms** – Example: `Set an alarm for 1 hour 50 minutes.`
  - **Playing Songs** – Example: `Play [SONG_NAME]`
  - **Device Checks** – Battery percentage, storage, RAM, location, WiFi status, etc.
  - **App Launching** – Opens installed apps using package names.
- **Background Accessibility** – Nova remains accessible via an overlay interface for quick interactions.
- **Multi-Platform Messaging** – Allows seamless message-sending across various communication platforms.
- **Context Retention** – Remembers previous interactions for a more natural conversational flow.

## Why Choose Nova?

Nova is designed for users who:
- Want a personal AI assistant that’s engaging and context-aware.
- Need a voice assistant for performing quick tasks effortlessly.
- Enjoy witty and interactive AI-driven conversations.

Whether for utility or entertainment, Nova adapts to your needs, making every interaction unique and enjoyable.

## Installation & Testing

- Nova AI has been tested successfully on **Android 8.1 (API 27)** and remains functional on newer versions.
- Further testing is encouraged on **Android 15 (API 36)** for enhanced compatibility.
- The APK is available in the Nova Android GitHub repository for direct installation and testing.

## Getting Started

1. **Clone the Repository:**  
   ```bash
   git clone https://github.com/Starcool20/Nova.git
   ```
2. **Run the App:** Install the APK from the GitHub repository and grant the necessary permissions.
3. **Interact with Nova:** Use voice or text commands for an engaging AI experience.

## Example Interactions

```bash
User: "Send HI to Jeri on WhatsApp."
Nova AI: Whatsapp "HI" to "Jeri", isCommand = true

User: "What is today’s date?"
Nova AI: Today's date is ${data_json.date}, isCommand = false
```

Nova AI is your reliable, intelligent, and engaging assistant, offering smooth and intuitive functionality across Android devices.

## Note
Nova is still on early stage so you might experince some issues

**Try Nova today!** 🚀
