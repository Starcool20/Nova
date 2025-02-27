# Nova: Your AI Companion for Android

Nova is an Android app that leverages a voice assistant to provide meaningful conversations, engage users in real-time, and offer companionship at any time. Built to meet real-world needs, Nova enhances accessibility and interactivity, functioning seamlessly in the background to bring a Siri- or Alexa-like experience to Android users.

## Features
- **Natural Conversations:** Powered by cutting-edge AI, Nova can hold interactive conversations, tell jokes, or simply keep you company.
- **Voice Recognition:** Uses Android's `SpeechRecognizer` API to understand spoken words accurately.
- **Background Mode:** Always available in the background with an overlay interface, accessible with a single tap for instant engagement.
- **Seamless Integration:** Delivers responses via server-side GPT model processing, enabling accurate, context-aware replies.
  
## Why Nova?
Nova is designed for users who:
- Desire a personal assistant for Android without relying on existing systems.
- Appreciate the ability to have a quick conversation or just need a listening ear.
- Want an AI assistant that’s engaging, humorous, and always ready to talk.

Whether you’re looking for a brief chat or a quick laugh, Nova is here for you.

## How It Works
1. **Voice Recognition:** Nova listens for your voice using Android’s `SpeechRecognizer` API and captures audio through `AudioRecorder`.
2. **AI Processing:** Audio data is sent securely to a server where GPT-powered processing provides a customized response.
3. **Instant Interaction:** Nova’s overlay button appears on your screen, allowing you to instantly summon and interact with the assistant.

> **Server Repository:** The server code for processing requests can be found [here](https://github.com/Starcool20/Nova---AI-Server-).

## Android Version Support
- The built APK supports Android 7.1 (API level 25) to Android 14 (API level 34).
- To support Android 15, you can adjust the configuration in the `build.gradle` file:
  1. Open the `build.gradle` file in your project.
  2. Update the `compileSdk` and `targetSdk` versions to 35.
  3. Sync the project to apply changes.

## Benefits
- **Accessibility:** Designed for users with limited access to high-end virtual assistants.
- **Entertainment:** Offers a fun, interactive experience for those moments when you need a companion.
- **Simplicity & Speed:** Get quick responses through Nova’s smooth and simple UI, without needing additional setup.

## Getting Started
1. **Clone the Repository:** 
   ```bash
   git clone https://github.com/yourusername/nova.git
