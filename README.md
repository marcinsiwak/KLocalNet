# KLocalNet

[![Maven Central](https://img.shields.io/maven-central/v/io.github.marcinsiwak/klocalnet.svg?label=Maven%20Central)](https://search.maven.org/artifact/io.github.marcinsiwak/klocalnet)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

A Kotlin Multiplatform library for local network communication with automatic server election and WebSocket-based messaging. Perfect for building peer-to-peer applications that work across Android and iOS.

## Installation

### Gradle (Kotlin DSL)

Add the dependency to your `build.gradle.kts`:

```kotlin
commonMain.dependencies {
    implementation("io.github.marcinsiwak:klocalnet:1.0.6")
}
```

## Setup

### Android

Initialize KLocalNet in your `Application` class:

```kotlin

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        KLocalNet.init(this)
    }
}
```

Don't forget to add the required permissions in your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_MULTICAST_STATE" />
```

### iOS

Initialize KLocalNet before using it. Create a function in iosMain and call it from your iOS app's entry point (e.g., in `AppDelegate`):

```kotlin

fun initializeApp() {
    KLocalNet.init()
}
```

## Usage

### Basic Setup

```kotlin

// Get the manager instance
val manager = KLocalNet.getKLocalNetManager()

// Observe WiFi connection state
launch {
    manager.isWifiConnected.collect { isConnected ->
        println("WiFi connected: $isConnected")
    }
}

// Observe loading state
launch {
    manager.isLoading.collect { isLoading ->
        println("Loading: $isLoading")
    }
}

// Connect to the network
launch {
    manager.connect()
}
```

### Defining Custom Events

Create custom event classes by extending `WebSocketEvent`:

```kotlin

@Serializable
data class ChatMessage(
    val userId: String,
    val message: String,
    val timestamp: Long
) : WebSocketEvent()

@Serializable
data class GameStateUpdate(
    val gameId: String,
    val state: String
) : WebSocketEvent()
```

### Registering Custom Events

Before using custom events, register them with the manager:

```kotlin

manager.setCustomEvents(
    listOf(
        ChatMessage::class to serializer<ChatMessage>(),
        GameStateUpdate::class to serializer<GameStateUpdate>()
    )
)
```

### Receiving Messages

#### Server-side (Host Device)

```kotlin

launch {
    manager.serverMessages.collect { event ->
        when (event) {
            is ClientActions.UserConnected -> {
                println("User connected: ${event.id}, isHost: ${event.isHost}")
            }
            is ClientActions.UserDisconnected -> {
                println("User disconnected: ${event.id}")
            }
            is ChatMessage -> {
                println("Received chat: ${event.message}")
                // Process the message
            }
            // Handle other custom events
        }
    }
}
```

#### Client-side

```kotlin

launch {
    manager.clientMessages.collect { event ->
        when (event) {
            is ServerActions.ServerStarted -> {
                println("Server has started")
            }
            is ClientActions.ServerDownDetected -> {
                println("Server is down, re-election in progress")
            }
            is ChatMessage -> {
                println("Received chat: ${event.message}")
            }
            // Handle other custom events
        }
    }
}
```

### Sending Messages

#### From Server to Specific Client

```kotlin
val userId = "192.168.1.5"
val message = ChatMessage(
    userId = manager.getDeviceId(),
    message = "Hello from server!",
    timestamp = System.currentTimeMillis()
)

launch {
    manager.send(id = userId, webSocketEvent = message)
}
```

#### From Server to All Clients

```kotlin
val message = GameStateUpdate(
    gameId = "game123",
    state = "started"
)

launch {
    manager.sendToAll(webSocketEvent = message)
}
```

#### From Client to Server

```kotlin
val message = ChatMessage(
    userId = manager.getDeviceId(),
    message = "Hello from client!",
    timestamp = System.currentTimeMillis()
)

launch {
    manager.sendFromClient(webSocketEvent = message)
}
```

### Advanced Features

#### Check if Current Device is Server

```kotlin
val isServer = manager.isServerRunning()
if (isServer) {
    println("This device is the host")
}
```

#### Get Device ID

```kotlin
val deviceId = manager.getDeviceId()
println("My device ID: $deviceId") // e.g., "192.168.1.5"
```

#### Session Management

To influence server election based on existing session state:

```kotlin
// Device with an active session gets priority in server election
manager.setHasSession(
    hasSession = true,
    lastUpdate = System.currentTimeMillis()
)
```

#### Disconnect All Users

If you're the server and want to disconnect all clients:

```kotlin
launch {
    manager.disconnectUsers()
}
```

## How It Works

### Server Election Process

1. **Discovery Phase**: All devices on the network broadcast their presence via UDP
2. **Election Algorithm**: Devices use a distributed election algorithm considering:
   - Active session status (devices with sessions get priority)
   - Last update timestamp (more recent updates get priority)
   - IP address (as a tiebreaker)
3. **Server Election**: One device is elected as the host/server
4. **Automatic Reconnection**: If the server goes down, a new election occurs automatically

### Communication Flow

1. **Server Device**: 
   - Runs a Ktor WebSocket server
   - Accepts connections from clients
   - Broadcasts messages to all or specific clients

2. **Client Devices**:
   - Connect to the elected server via WebSocket
   - Send messages to the server
   - Receive messages from the server

## Built-in Events

### ClientActions

- `UserConnected(id: String, isHost: Boolean)` - A user has connected to the server
- `UserDisconnected(id: String)` - A user has disconnected
- `ServerDownDetected` - The server has gone offline (triggers re-election)

### ServerActions

- `ServerStarted` - The server has successfully started

## Example: Simple Chat Application

```kotlin

@Serializable
data class ChatMessage(
    val userId: String,
    val message: String
) : WebSocketEvent()

class ChatManager {
    private val manager = KLocalNet.getKLocalNetManager()
    
    init {
        // Register custom events
        manager.setCustomEvents(
            listOf(ChatMessage::class to serializer<ChatMessage>())
        )
        
        // Start observing messages
        observeMessages()
    }
    
    private fun observeMessages() {
        // Listen for messages as server
        launch {
            manager.serverMessages.collect { event ->
                when (event) {
                    is ChatMessage -> {
                        println("${event.userId}: ${event.message}")
                        // Broadcast to all clients
                        manager.sendToAll(event)
                    }
                    is ClientActions.UserConnected -> {
                        println("${event.id} joined the chat")
                    }
                }
            }
        }
        
        // Listen for messages as client
        launch {
            manager.clientMessages.collect { event ->
                if (event is ChatMessage) {
                    println("${event.userId}: ${event.message}")
                }
            }
        }
    }
    
    suspend fun sendMessage(text: String) {
        val message = ChatMessage(
            userId = manager.getDeviceId(),
            message = text
        )
        
        if (manager.isServerRunning()) {
            // If we're the server, broadcast to everyone
            manager.sendToAll(message)
        } else {
            // If we're a client, send to server
            manager.sendFromClient(message)
        }
    }
}
```

## Requirements

- **Android**: minSdk 24, compileSdk 36
- **iOS**: iOS 13+
- **JVM**: Java 17+

## Dependencies

KLocalNet uses the following libraries:
- Ktor (client and server)
- Kotlinx Serialization
- Kotlinx Coroutines
- Koin (dependency injection)

## License

```
Copyright 2026 Marcin Siwak

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Author

Marcin Siwak
- Email: marcinsiwak15@gmail.com
- GitHub: [@marcinsiwak](https://github.com/marcinsiwak)

## Links

- [GitHub Repository](https://github.com/marcinsiwak/KLocalNet/)
- [Maven Central](https://search.maven.org/artifact/io.github.marcinsiwak/klocalnet)
