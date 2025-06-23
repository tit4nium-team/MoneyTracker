import SwiftUI
import Firebase // Add this line

@main
struct iOSApp: App {

    init(){
        FirebaseApp.configure()
        // Initialize Gemini Service for iOS
        FirebaseGeminiServiceImplKt.initializeIOSGeminiService()
      }
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}