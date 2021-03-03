import SwiftUI
import MobileCoreServices

struct InvitationView: View {
    let roomName: String

    @State private var isShareSheetPresented: Bool = false

    private var shareButtonView: some View {
        return Button(action: self.shareRoomURL) {
            Text("Share join code")
                .bold()
                .foregroundColor(Color.primary)
                .padding(.horizontal, 15)
                .padding(.vertical, 10)
                .frame(maxWidth: .infinity)
                .background(Color.Daily.accentColor)
                .cornerRadius(10.0)
                .fixedSize()
        }
    }

    var body: some View {
        VStack(alignment: .center, spacing: 10.0) {
            Text("Invite others")
                .font(.title3)
                .bold()
            Text("Share join code with others to invite them")
                .font(.footnote)
            self.shareButtonView
        }
        .padding()
        .overlay(
            RoundedRectangle(cornerRadius: 10)
                .stroke(Color.secondary.opacity(0.25))
        )
        .sheet(isPresented: self.$isShareSheetPresented) {
            ActivityViewController(activityItems: [self.roomName])
        }
    }

    func shareRoomURL() {
        self.isShareSheetPresented = true
    }
}

struct InvitationView_Previews: PreviewProvider {
    static var previews: some View {
        InvitationView(roomName: "lorem-ipsum")
    }
}
