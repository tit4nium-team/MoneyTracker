import SwiftUI
import Firebase // Add this line

@main
struct iOSApp: App {

    init(){
        FirebaseApp.configure()
        // Initialize Gemini Service for iOS using the actual fun from KMP
        // The generated name will be based on the Kotlin file name where `initializeGeminiService` actual is.
        // Assuming it's still in FirebaseGeminiServiceImpl.kt or a similarly named file for the actual.
        FirebaseGeminiServiceImplKt.initializeGeminiService()
      }
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}