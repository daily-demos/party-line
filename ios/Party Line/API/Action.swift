import Foundation

enum Action: CustomStringConvertible {
    case muteParticipant
    case makeListener
    case makeSpeaker
    case makeModerator
    case ejectParticipant

    var description: String {
        switch self {
        case .muteParticipant:
            return "Mute"
        case .makeListener:
            return "Demote to Listener"
        case .makeSpeaker:
            return "Promote to Speaker"
        case .makeModerator:
            return "Promote to Moderator"
        case .ejectParticipant:
            return "Eject"
        }
    }
}
