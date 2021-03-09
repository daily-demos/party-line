import SwiftUI

struct ContentView: View {
    @ObservedObject
    private var client: Client = .init(
        serverURL: Client.demoServerURL
    )

    @State
    private var firstName: String = ""

    @State
    private var lastName: String = ""

    @State
    private var roomName: String = ""

    private var headerView: some View {
        HeaderView()
    }

    private var firstNameView: some View {
        VStack(alignment: .leading, spacing: 15.0) {
            Text("First name")
                .bold()
            TextField("Enter first name", text: self.$firstName)
                .autocapitalization(.words)
        }
    }

    private var lastNameView: some View {
        VStack(alignment: .leading, spacing: 15.0) {
            Text("Last name")
                .bold()
            TextField("Enter last name", text: self.$lastName)
                .autocapitalization(.words)
        }
    }

    private var roomNameView: some View {
        return VStack(alignment: .leading, spacing: 15.0) {
            Text("Join Code")
                .bold()
            TextField("Enter join code", text: self.$roomName)
                .autocapitalization(/*@START_MENU_TOKEN@*/.none/*@END_MENU_TOKEN@*/)
            Text("Enter a join code to join an existing room or leave empty to create and join a new room.")
                .font(.footnote)
                .lineLimit(2)
                .fixedSize(
                    horizontal: false,
                    vertical: true
                )
        }
    }

    private var buttonTitle: String {
        if self.roomName.isEmpty {
            return "Create and join room"
        } else {
            return "Join room"
        }
    }

    private var isSigninButtonDisabled: Bool {
        guard self.firstName.isEmpty else {
            return false
        }

        guard !self.client.isReady else {
            return false
        }

        return true
    }

    private var signinButton: some View {
        return Button(action: self.signIn) {
            Text(self.buttonTitle)
                .bold()
                .foregroundColor(self.firstName.isEmpty ? .secondary : .primary)
                .padding()
                .frame(maxWidth: .infinity)
                .background(Color.Daily.accentColor)
                .cornerRadius(10.0)
        }
        .disabled(self.isSigninButtonDisabled)
    }

    private var learnMoreView: some View {
        LearnMoreView()
    }

    private var formView: some View {
        VStack(alignment: .leading, spacing: 30.0) {
            Text("Get started")
                .font(.title2)
                .bold()
            self.firstNameView
            self.lastNameView
            self.roomNameView
            self.signinButton
            self.learnMoreView
        }
        .textFieldStyle(RoundedBorderTextFieldStyle())
        .disableAutocorrection(true)
    }

    var body: some View {
        ZStack(alignment: Alignment(horizontal: .center, vertical: .top)) {
            Color.Daily.backgroundColor
                .ignoresSafeArea()
            VStack(alignment: .leading, spacing: 50.0) {
                self.headerView
                self.formView
            }
            .padding(30)
        }
        .onAppear {
            self.client.prepareIfNeeded()
        }
        .fullScreenCover(
            isPresented: self.$client.roomIsPresented,
            onDismiss: self.signOut
        ) {
            if let client = self.client {
                RoomView(client: client)
            } else {
                Text("No session")
            }
        }
    }

    func signIn() {
        guard !self.firstName.isEmpty else {
            return
        }

        let firstName = self.firstName.trimmingCharacters(in: .whitespacesAndNewlines)
        let lastName = self.lastName.trimmingCharacters(in: .whitespacesAndNewlines)
        let roomName = self.roomName.trimmingCharacters(in: .whitespacesAndNewlines)

        let role: Participant.Role = roomName.isEmpty ? .moderator : .listener

        let userName: String

        switch (firstName, lastName) {
        case (firstName, ""):
            userName = "\(firstName)_\(role.rawValue)"
        case (firstName, lastName):
            userName = "\(firstName) \(lastName)_\(role.rawValue)"
        case _:
            fatalError("Unreachable.")
        }

        if roomName.isEmpty {
            self.client.createAndJoinRoom(
                userName: userName
            )
        } else {
            self.client.joinRoom(
                userName: userName,
                roomName: roomName
            )
        }
    }

    func signOut() {
        self.client.leaveRoom()
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
            .background(Color.Daily.backgroundColor)
            .preferredColorScheme(.light)
    }
}
