import SwiftUI

struct RoomView: View {
    @Environment(\.presentationMode)
    var presentationMode: Binding<PresentationMode>

    @ObservedObject
    var client: Client

    @State
    var timeRemaining: TimeInterval? = Client.maxDuration

    private let timer = Timer.publish(
        every: 1.0,
        on: .main,
        in: .common
    ).autoconnect()

    private var speakers: [Participant] {
        self.client.speakers
    }

    private var listeners: [Participant] {
        self.client.listeners
    }

    private var isModerator: Bool {
        guard let local = self.client.local else {
            return false
        }

        return local.isModerator
    }

    private var headerView: some View {
        HeaderView()
    }

    private var participantsGridView: some View {
        ParticipantsGridView(
            speakers: self.speakers,
            listeners: self.listeners,
            localIsModerator: self.isModerator,
            timeRemaining: self.timeRemaining
        ) { sessionID, action in
            guard let participant = self.client.participants[sessionID.stringValue] else {
                return
            }

            logger.debug("Action: \(participant.username) -> \(action)")

            switch action {
            case .muteParticipant:
                self.client.muteParticipant(sessionID: sessionID)
            case .makeListener:
                self.client.makeListener(sessionID: sessionID)
            case .makeSpeaker:
                self.client.makeSpeaker(sessionID: sessionID)
            case .makeModerator:
                self.client.makeModerator(sessionID: sessionID)
            case .ejectParticipant:
                self.client.ejectParticipant(sessionID: sessionID)
            }
        }
    }

    private var invitationView: some View {
        Group {
            if let room = self.client.room {
                InvitationView(roomName: room.name)
            } else {
                EmptyView()
            }
        }
    }

    private var learnMoreView: some View {
        LearnMoreView()
    }

    private var isMicrophoneEnabled: Bool {
        self.client.local?.isMicrophoneEnabled ?? false
    }

    private func microphoneButton(local: Participant) -> some View {
        let isEnabled = self.isMicrophoneEnabled
        let imageName = isEnabled ? "mic.fill" : "mic.slash.fill"
        let title = isEnabled ? "Mute" : "Unmute"
        let foregroundColor = isEnabled ? Color.primary : Color.red

        return Button(action: self.toggleMic) {
            HStack {
                Image(systemName: imageName)
                Text(title)
                    .bold()
            }
            .foregroundColor(foregroundColor)
        }
    }

    private func handButton(local: Participant) -> some View {
        let isEnabled = local.isHandRaised
        let imageName = isEnabled ? "hand.raised.fill" : "hand.raised.slash.fill"
        let title = isEnabled ? "Lower" : "Raise"
        let foregroundColor = isEnabled ? Color.primary : Color.red

        return Button(action: self.toggleHand) {
            HStack {
                Image(systemName: imageName)
                Text(title)
                    .bold()
            }
            .foregroundColor(foregroundColor)
        }
    }

    private func leaveButton(isOwner: Bool) -> some View {
        let title = isOwner ? "End call" : "Leave room"

        return Button(action: self.leave) {
            Text(title)
                .bold()
        }
    }

    private func footerView(local: Participant?) -> some View {
        HStack {
            if let local = local {
                switch local.role {
                case .moderator, .speaker:
                    self.microphoneButton(local: local)
                case .listener:
                    self.handButton(local: local)
                }
            }
            Spacer()
            self.leaveButton(isOwner: local?.isOwner ?? false)
        }
    }

    private var waitingView: some View {
        WaitingView()
    }

    var body: some View {
        VStack {
            self.headerView
            ScrollView(/*@START_MENU_TOKEN@*/.vertical/*@END_MENU_TOKEN@*/, showsIndicators: false) {
                VStack(spacing: 20.0) {
                    if self.client.participants.isEmpty {
                        self.waitingView
                    } else {
                        self.participantsGridView
                    }
                    self.invitationView
                    self.learnMoreView
                }
            }
            .padding(.vertical, 20.0)
            self.footerView(local: self.client.local)
        }
        .padding(30.0)
        .background(Color.Daily.backgroundColor)
        .onReceive(self.timer) { _ in
            self.timeRemaining = self.timeRemaining(
                until: self.client.expirationDate
            )
            if self.timeRemaining == nil {
                self.leave()
            }
        }
        .alert(isPresented: self.$client.errorIsPresented) {
            let error = self.client.error ?? .unknown

            return Alert(
                title: Text(error.title),
                message: Text(error.message),
                dismissButton: .default(Text("OK")) {
                    self.leave()
                }
            )
        }
    }

    private func toggleMic() {
        guard let local = self.client.local else {
            return
        }

        self.client.set(microphoneEnabled: !local.isMicrophoneEnabled)
    }

    private func toggleHand() {
        guard let local = self.client.local else {
            return
        }

        self.client.set(handRaised: !local.isHandRaised)
    }

    private func leave() {
        self.client.leaveRoom()
        self.presentationMode.wrappedValue.dismiss()
    }

    private func timeRemaining(until date: Date) -> TimeInterval? {
        let timeInterval = date.timeIntervalSinceNow

        guard timeInterval > 0.0 else {
            return nil
        }

        return timeInterval
    }
}

struct RoomView_Previews: PreviewProvider {
    static var client: Client {
        .init(
            serverURL: URL(string: "https://example.com")!
        )
    }

    static var previews: some View {
        RoomView(client: self.client)
            .background(Color.Daily.backgroundColor)
    }
}
