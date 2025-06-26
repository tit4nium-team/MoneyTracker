import Foundation
import FirebaseCore
import FirebaseVertexAI // Certifique-se de que esta dependência foi adicionada via SPM no Xcode

// É crucial que FirebaseApp.configure() seja chamado antes de usar qualquer serviço Firebase.
// Geralmente, isso é feito no AppDelegate ou na struct App principal do SwiftUI.
// Se já estiver sendo chamado lá, esta chamada pode ser removida ou condicionada.
// Para garantir que funcione, vamos chamar aqui, mas com uma verificação para evitar múltiplas configurasções.
private func configureFirebaseAppIfNeeded() {
    if FirebaseApp.app() == nil {
        FirebaseApp.configure()
    }
}

@objcMembers
public class FirebaseAISwiftWrapper: NSObject {

    private var generativeModel: GenerativeModel?

    override public init() {
        super.init()
        configureFirebaseAppIfNeeded() // Garante que o Firebase está configurado

        // Initialize the Vertex AI service
        let vertex = VertexAI.vertexAI()

        // Initialize the generative model with a model that supports your use case
        // Defina o nome do modelo. Para texto, "gemini-1.5-flash" ou "gemini-1.0-pro" são comuns.
        // Verifique a documentação do Firebase para os modelos mais recentes e suas capacidades.
        self.generativeModel = vertex.generativeModel(modelName: "gemini-1.5-flash-latest")
        // Se precisar de configurações específicas (temperatura, etc.), você pode defini-las aqui:
        // let config = GenerationConfig(
        //     temperature: 0.7,
        //     topP: 1.0,
        //     topK: 40,
        //     maxOutputTokens: 2048
        // )
        // self.generativeModel = vertex.generativeModel(modelName: "gemini-1.0-pro", generationConfig: config)
    }

    public func generateInsights(prompt: String, completion: @escaping (String?, Error?) -> Void) {
        guard let model = self.generativeModel else {
            let error = NSError(domain: "FirebaseAISwiftWrapperError",
                                code: -1,
                                userInfo: [NSLocalizedDescriptionKey: "Generative model not initialized"])
            completion(nil, error)
            return
        }

        Task {
            do {
                let response = try await model.generateContent(prompt)
                // Assumindo que a resposta contém texto que pode ser usado diretamente.
                // Pode ser necessário processar `response.candidates` ou `response.text`
                // dependendo da estrutura da resposta do modelo Gemini.
                if let text = response.text {
                    completion(text, nil)
                } else {
                    let error = NSError(domain: "FirebaseAISwiftWrapperError",
                                        code: -2,
                                        userInfo: [NSLocalizedDescriptionKey: "No text content in response"])
                    completion(nil, error)
                }
            } catch {
                NSLog("FirebaseAISwiftWrapper Error: \(error.localizedDescription)")
                completion(nil, error)
            }
        }
    }
}
