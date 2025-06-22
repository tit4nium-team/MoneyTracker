import SwiftUI
import Firebase // Add this line

@main
struct iOSApp: App {

    init(){
        FirebaseApp.configure()
      }
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}