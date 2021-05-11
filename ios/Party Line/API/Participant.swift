import Foundation

struct Participant: Decodable {
    enum Role: String, RawRepresentable, Equatable {
        case moderator = "MOD"
        case speaker = "SPK"
        case listener = "LST"
    }

    let sessionID: SessionID
    let userID: UserID
    let username: String
    let role: Role

    let isLocal: Bool
    let isOwner: Bool
    let isMicrophoneEnabled: Bool
    let isHandRaised: Bool

    var isModerator: Bool {
        self.role == .moderator
    }

    var isSpeaker: Bool {
        self.role == .speaker
    }

    var isListener: Bool {
        self.role == .listener
    }

    var key: String {
        self.isLocal ? Self.localKey : self.id.stringValue
    }

    static let localKey: String = "local"

    enum CodingKeys: String, CodingKey {
        case sessionID = "session_id"
        case userID = "user_id"
        case audio = "audio"
        case local = "local"
        case owner = "owner"

        case username = "user_name"
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)

        let sessionID = try container.decode(SessionID.self, forKey: .sessionID)
        let userID = try container.decode(UserID.self, forKey: .userID)
        let isLocal = try container.decode(Bool.self, forKey: .local)
        let isOwner = try container.decode(Bool.self, forKey: .owner)
        let isMicrophoneEnabled = try container.decode(Bool.self, forKey: .audio)

        let rawUsername = try container.decode(String.self, forKey: .username)

        let username: String
        let role: Role
        let isHandRaised: Bool

        if let parsedUsername = ParsedUsername(rawValue: rawUsername) {
            username = parsedUsername.username
            role = parsedUsername.role
            isHandRaised = parsedUsername.isHandRaised
        } else {
            username = rawUsername
            role = .listener
            isHandRaised = false
        }

        self.init(
            sessionID: sessionID,
            userID: userID,
            username: username,
            role: role,
            isLocal: isLocal,
            isOwner: isOwner,
            isMicrophoneEnabled: isMicrophoneEnabled,
            isHandRaised: isHandRaised
        )
    }

    init(
        sessionID: SessionID,
        userID: UserID,
        username: String,
        role: Role,
        isLocal: Bool,
        isOwner: Bool,
        isMicrophoneEnabled: Bool,
        isHandRaised: Bool
    ) {
        self.sessionID = sessionID
        self.userID = userID
        self.username = username
        self.role = role
        self.isLocal = isLocal
        self.isOwner = isOwner
        self.isMicrophoneEnabled = isMicrophoneEnabled
        self.isHandRaised = isHandRaised
    }
}

extension Participant: Identifiable {
    var id: SessionID {
        self.sessionID
    }
}

extension Participant: CustomStringConvertible {
    var description: String {
        let properties = [
            "sessionID": String(describing: self.sessionID),
            "userID": String(describing: self.userID),
            "username": String(describing: self.username),
            "role": String(describing: self.role),
            "isLocal": String(describing: self.isLocal),
            "isOwner": String(describing: self.isOwner),
            "isMicrophoneEnabled": String(describing: self.isMicrophoneEnabled),
            "isHandRaised": String(describing: self.isHandRaised),
        ]

        let mappedProperties = properties.map {
            "\($0.key): \($0.value)"
        }
        let propertiesString = mappedProperties.joined(separator: ", ")

        return "<Participant \(propertiesString)>"
    }
}

/// Since this is merely a quick 'n dirty client-side technical demo
/// we didn't go the extra mile of setting up a custom server, too.
/// As such we encode some demo-specific properties of a participant
/// into their usernames.
///
/// - Important:
/// This is clearly not how you would do things on anything beyond a demo/prototype!
/// A participant's extra info should of course be handled by a dedicated server, instead.
private struct ParsedUsername: RawRepresentable {
    let username: String
    let role: Participant.Role
    let isHandRaised: Bool

    var rawValue: String {
        var rawValue = ""

        if self.isHandRaised {
            rawValue.append("✋ ")
        }

        rawValue.append(self.username)
        rawValue.append("_\(self.role.rawValue)")

        return rawValue
    }

    init?(rawValue: String) {
        let pattern = "^(?:(✋) )?(.+)(?:_(MOD|SPK|LST))$"

        let regex = try! NSRegularExpression(
          pattern: pattern,
          options: .caseInsensitive
        )

        let range = NSRange(location: 0, length: rawValue.utf16.count)
        guard let match = regex.firstMatch(in: rawValue, options: [], range: range) else {
            return nil
        }

        let prefixRange = Range(match.range(at: 1), in: rawValue)

        guard let infixRange = Range(match.range(at: 2), in: rawValue) else {
            return nil
        }

        guard let suffixRange = Range(match.range(at: 3), in: rawValue) else {
            return nil
        }

        self.isHandRaised = prefixRange != nil
        self.username = String(rawValue[infixRange])
        self.role = Participant.Role(rawValue: String(rawValue[suffixRange]))!
    }
}
