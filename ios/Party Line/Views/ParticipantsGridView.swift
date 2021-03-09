import SwiftUI

struct ParticipantsGridView: View {
    let speakers: [Participant]
    var listeners: [Participant]

    let localIsModerator: Bool
    let timeRemaining: TimeInterval?
    let onAction: ((SessionID, Action) -> ())?

    init(
        speakers: [Participant],
        listeners: [Participant],
        localIsModerator: Bool,
        timeRemaining: TimeInterval?,
        onAction: ((SessionID, Action) -> ())? = nil
    ) {
        self.speakers = speakers
        self.listeners = listeners
        self.localIsModerator = localIsModerator
        self.timeRemaining = timeRemaining
        self.onAction = onAction
    }

    private let columns: [GridItem] = [
        GridItem(.flexible(), spacing: 30.0),
        GridItem(.flexible(), spacing: 30.0),
        GridItem(.flexible(), spacing: 30.0),
    ]

    private var speakersAndModeratorsHeaderView: some View {
        HStack {
            Text("Speakers")
                .bold()
                .foregroundColor(.secondary)
            Spacer()
            TimeRemainingView(
                timeRemaining: self.timeRemaining
            )
        }
    }

    private var speakersAndModeratorsGridView: some View {
        LazyVGrid(columns: self.columns) {
            ForEach(self.speakers) { participant in
                ParticipantGridItemView(
                    participant: participant,
                    localIsModerator: self.localIsModerator
                )
            }
        }
    }

    private var speakersAndModeratorsView: some View {
        VStack(alignment: .leading, spacing: 20.0) {
            self.speakersAndModeratorsHeaderView
            self.speakersAndModeratorsGridView
        }
    }

    private var listenersHeaderView: some View {
        Text("Listeners")
            .bold()
            .foregroundColor(.secondary)
    }

    private var listenersGridView: some View {
        LazyVGrid(columns: self.columns) {
            ForEach(self.listeners) { participant in
                ParticipantGridItemView(
                    participant: participant,
                    localIsModerator: self.localIsModerator
                ) { action in
                    self.onAction?(participant.sessionID, action)
                }
            }
        }
    }

    private var listenersView: some View {
        VStack(alignment: .leading, spacing: 20.0) {
            self.listenersHeaderView
            self.listenersGridView
        }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 30.0) {
            self.speakersAndModeratorsView
            Divider()
            self.listenersView
        }
    }
}

struct ParticipantsGridView_Previews: PreviewProvider {
    static var speakers: [Participant] {
        return (0..<3).map { i in
            let role: Participant.Role = (i == 0) ? .moderator : .speaker
            return Participant(
                sessionID: .init(),
                userID: .init(),
                username: "Jane Doe_LST",
                role: role,
                isLocal: false,
                isOwner: false,
                isMicrophoneEnabled: false,
                isHandRaised: false
            )
        }
    }

    static var listeners: [Participant] {
        return (0..<3).map { i in
            return Participant(
                sessionID: .init(),
                userID: .init(),
                username: "John Doe_LST",
                role: .listener,
                isLocal: false,
                isOwner: false,
                isMicrophoneEnabled: false,
                isHandRaised: true
            )
        }
    }

    static var previews: some View {
        ParticipantsGridView(
            speakers: self.speakers,
            listeners: self.listeners,
            localIsModerator: true,
            timeRemaining: 42.0
        )
            .background(Color.Daily.backgroundColor)
            .previewLayout(.sizeThatFits)
    }
}
