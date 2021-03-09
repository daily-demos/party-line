import SwiftUI

struct WaitingView: View {
    var body: some View {
        VStack(alignment: .center, spacing: 10.0) {
            ProgressView()
            Text("Waiting for Room …")
                .bold()
        }
        .padding()
        .overlay(
            RoundedRectangle(cornerRadius: 10)
                .stroke(Color.secondary.opacity(0.25))
        )
    }
}

struct WaitingView_Previews: PreviewProvider {
    static var previews: some View {
        WaitingView()
    }
}
