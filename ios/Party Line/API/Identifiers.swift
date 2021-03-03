import Foundation

struct SessionID: Equatable, Hashable {
    let uuid: UUID

    var stringValue: String {
        self.uuid.uuidString.lowercased()
    }

    init(uuid: UUID = .init()) {
        self.uuid = uuid
    }
}

extension SessionID: Decodable {
    init(from decoder: Decoder) throws {
        let container = try decoder.singleValueContainer()
        self.uuid = try container.decode(UUID.self)
    }
}

extension SessionID: CustomStringConvertible {
    var description: String {
        self.uuid.description.lowercased()
    }
}

struct UserID: Equatable, Hashable {
    let uuid: UUID

    var stringValue: String {
        self.uuid.uuidString.lowercased()
    }

    init(uuid: UUID = .init()) {
        self.uuid = uuid
    }
}

extension UserID: Decodable {
    init(from decoder: Decoder) throws {
        let container = try decoder.singleValueContainer()
        self.uuid = try container.decode(UUID.self)
    }
}

extension UserID: CustomStringConvertible {
    var description: String {
        self.uuid.description.lowercased()
    }
}

struct RoomID: Equatable, Hashable {
    let uuid: UUID

    var stringValue: String {
        self.uuid.uuidString.lowercased()
    }

    init(uuid: UUID = .init()) {
        self.uuid = uuid
    }
}

extension RoomID: Decodable {
    init(from decoder: Decoder) throws {
        let container = try decoder.singleValueContainer()
        self.uuid = try container.decode(UUID.self)
    }
}

extension RoomID: CustomStringConvertible {
    var description: String {
        self.uuid.description.lowercased()
    }
}
