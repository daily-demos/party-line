import SwiftUI

struct ParticipantGridItemView: View {
    let participant: Participant
    let localIsModerator: Bool
    let onAction: ((Action) -> ())?

    internal init(
        participant: Participant,
        localIsModerator: Bool,
        onAction: ((Action) -> ())? = nil
    ) {
        self.participant = participant
        self.localIsModerator = localIsModerator
        self.onAction = onAction
    }

    private var username: String {
        self.participant.username
    }

    private var initialsView: some View {
        let username = self.username

        let components = username.split(separator: " ")
        let initials = components.compactMap { $0.first }.filter { $0.isLetter }

        let initialsString = String(initials).uppercased()

        let string: String
        if self.participant.isModerator {
            string = "⭐️ \(initialsString)"
        } else if self.participant.isHandRaised {
            string = "✋ \(initialsString)"
        } else {
            string = initialsString
        }

        return Text(string)
            .font(.title2)
            .bold()
    }

    private var avatarBackgroundView: some View {
        let isMicrophoneEnabled = self.participant.isMicrophoneEnabled

        typealias Colors = Color.Daily
        let fillColor = isMicrophoneEnabled ? Colors.accentColor : Colors.secondaryAccentColor

        return RoundedRectangle(
            cornerRadius: 40.0,
            style: .continuous
        )
        .fill(fillColor)
            .aspectRatio(1.0, contentMode: .fit)
    }

    private var microphoneBadgeIcon: some View {
        let isMicrophoneEnabled = self.participant.isMicrophoneEnabled

        let imageName = isMicrophoneEnabled ? "mic.fill" : "mic.slash.fill"
        let foregroundColor: Color = isMicrophoneEnabled ? .green : .red

        return Image(systemName: imageName)
            .resizable()
            .aspectRatio(contentMode: .fit)
            .foregroundColor(foregroundColor)
    }

    private var optionsIcon: some View {
        let imageName = "ellipsis"
        let foregroundColor: Color = .blue

        return Image(systemName: imageName)
            .resizable()
            .aspectRatio(contentMode: .fit)
            .foregroundColor(foregroundColor)
    }

    private var microphoneBadgeView: some View {
        BadgeView(self.microphoneBadgeIcon)
    }

    private var menuActions: [Action]? {
        guard self.localIsModerator else {
            return nil
        }

        var actions: [Action] = []

        switch self.participant.role {
        case .moderator:
            return nil
        case .speaker:
            actions.append(.makeListener)
            actions.append(.makeModerator)
            if self.participant.isMicrophoneEnabled {
                actions.append(.muteParticipant)
            }
        case .listener:
            actions.append(.makeSpeaker)
            actions.append(.makeModerator)
        }

        if !self.participant.isLocal {
            actions.append(.ejectParticipant)
        }

        return actions
    }

    private func optionsBadgeView(actions: [Action]) -> some View {
        Menu(
            content: {
                ForEach(actions, id: \.self) { action in
                    Button(action.description) {
                        self.onAction?(action)
                    }
                }
            },
            label: {
                BadgeView(self.optionsIcon)
            }
        )
    }

    private var avatarView: some View {
        ZStack {
            self.avatarBackgroundView
            self.initialsView
        }
    }

    private var badgesView: some View {
        HStack {
            if !self.participant.isListener {
                self.microphoneBadgeView
            }
            Spacer()
            if let actions = self.menuActions {
                self.optionsBadgeView(actions: actions)
            }
        }
    }

    private var nameView: some View {
        Text(self.participant.username)
            .font(.body)
            .bold()
            .lineLimit(2)
            .multilineTextAlignment(.center)
            .padding(5)
            .background(Color.Daily.backgroundColor)
            .contentShape(RoundedRectangle(
                cornerRadius: 10.0,
                style: .continuous
            ))
    }

    var body: some View {
        VStack {
            ZStack(alignment: .bottom) {
                self.avatarView
                self.badgesView
            }
            self.nameView
            Spacer()
        }
    }
}

struct ParticipantGridItemView_Previews: PreviewProvider {
    static var previews: some View {
        ParticipantGridItemView(
            participant: .init(
                sessionID: .init(),
                userID: .init(),
                username: "Jane Doe",
                role: .listener,
                isLocal: true,
                isOwner: false,
                isMicrophoneEnabled: true,
                isHandRaised: false
            ),
            localIsModerator: true
        )
        .background(Color.Daily.backgroundColor)
        .previewLayout(.fixed(width: 120.0, height: 150.0))
    }
}
