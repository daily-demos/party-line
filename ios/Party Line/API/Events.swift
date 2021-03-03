import Foundation
import WebKit

struct DemoCreatedRoomEvent: Decodable {
    let room: Room
}

struct DemoCreatedTokenEvent: Decodable {
    let token: String
}

struct DemoJoinedRoomEvent: Decodable {
    let room: Room
}

// https://docs.daily.co/reference#app-message
struct AppMessageEvent: Decodable {
    let callFrameId: String
    let data: JSONValue
    let fromId: UUID
}

// https://docs.daily.co/reference#error
struct ErrorEvent: Decodable {
    enum CodingKeys: String, CodingKey {
        case message = "errorMsg"
    }

    let message: String
}

// https://docs.daily.co/reference#joined-meeting
struct JoinedMeetingEvent: Decodable {
    let participants: [String: Participant]
}

// https://docs.daily.co/reference#error
struct ParticipantJoinedEvent: Decodable {
    let participant: Participant
}

// https://docs.daily.co/reference#participant-left
struct ParticipantLeftEvent: Decodable {
    let participant: Participant
}

// https://docs.daily.co/reference#participant-updated
struct ParticipantUpdatedEvent: Decodable {
    let participant: Participant
}

enum Event {
    case demoCreatedRoom(DemoCreatedRoomEvent)
    case demoCreatedToken(DemoCreatedTokenEvent)
    case demoJoinedRoom(DemoJoinedRoomEvent)
    case appMessage(AppMessageEvent)
    case error(ErrorEvent)
    case joinedMeeting(JoinedMeetingEvent)
    case participantJoined(ParticipantJoinedEvent)
    case participantLeft(ParticipantLeftEvent)
    case participantUpdated(ParticipantUpdatedEvent)
}

extension Event: CustomStringConvertible {
    var description: String {
        switch self {
        case .demoCreatedRoom(let event):
            return String(describing: event)
        case .demoCreatedToken(let event):
            return String(describing: event)
        case .demoJoinedRoom(let event):
            return String(describing: event)
        case .appMessage(let event):
            return String(describing: event)
        case .error(let event):
            return String(describing: event)
        case .joinedMeeting(let event):
            return String(describing: event)
        case .participantJoined(let event):
            return String(describing: event)
        case .participantLeft(let event):
            return String(describing: event)
        case .participantUpdated(let event):
            return String(describing: event)
        }
    }
}
