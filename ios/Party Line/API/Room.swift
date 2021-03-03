import Foundation

struct Room: Decodable, Equatable {
    enum CodingKeys: String, CodingKey {
        case id
        case name
        case config
    }

    struct Config: Decodable, Equatable {
        enum CodingKeys: String, CodingKey {
            case exp
        }

        let expirationDate: Date

        init(from decoder: Decoder) throws {
            let container = try decoder.container(keyedBy: CodingKeys.self)
            self.expirationDate = Date(timeIntervalSince1970: try container.decode(
                TimeInterval.self,
                forKey: .exp
            ))
        }
    }

    let id: RoomID
    let name: String
    let config: Config
}
