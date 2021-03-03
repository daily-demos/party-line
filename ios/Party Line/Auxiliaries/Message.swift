import Foundation

struct EventMessage: Decodable {
    enum CodingKeys: String, CodingKey {
        case action, content
    }

    let action: String
    let event: Event?
    let content: JSONValue?

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)

        let action = try container.decode(String.self, forKey: .action)

        let event: Event?
        var content: JSONValue? = nil

        switch action {
        case "demo-created-room":
            event = .demoCreatedRoom(try .init(from: decoder))
        case "demo-created-token":
            event = .demoCreatedToken(try .init(from: decoder))
        case "demo-joined-room":
            event = .demoJoinedRoom(try .init(from: decoder))
        case "app-message":
            event = .appMessage(try .init(from: decoder))
        case "error":
            event = .error(try .init(from: decoder))
        case "joined-meeting":
            event = .joinedMeeting(try .init(from: decoder))
        case "participant-joined":
            event = .participantJoined(try .init(from: decoder))
        case "participant-left":
            event = .participantLeft(try .init(from: decoder))
        case "participant-updated":
            event = .participantUpdated(try .init(from: decoder))
        case _:
            event = nil
            content = try .init(from: decoder)
        }

        self.action = action
        self.event = event
        self.content = content
    }
}

struct ConsoleMessage: Decodable {
    enum Level: String, Decodable {
        case log
        case info
        case warn
        case error
    }

    let level: Level
    let content: JSONValue
}

struct ErrorMessage: Decodable {
    let message: String
    let url: URL
    let line: Int
    let column: Int
    let content: JSONValue
}
