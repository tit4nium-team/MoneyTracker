package com.example.moneytracker

// This imports the Swift class. The package name `iosApp` comes from the
// name of the Xcode project's target. If your Xcode target is named differently,
// adjust this import.
// Note: Sometimes the direct import might not be immediately resolvable by the IDE
// until the project is built. The KMP tooling handles the bridging.
import iosApp.FirebaseAiLogicHelper as SwiftFirebaseAiLogicHelper

actual class FirebaseAiLogicHelper {
    private val swiftHelper: SwiftFirebaseAiLogicHelper = SwiftFirebaseAiLogicHelper()

    actual fun performAiMagic(input: String): String {
        return swiftHelper.performAiMagic(input)
    }
}
