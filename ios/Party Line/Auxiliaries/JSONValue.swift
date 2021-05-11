import Foundation

enum JSONValue: Decodable {
    case null
    case bool(Bool)
    case int(Int)
    case double(Double)
    case string(String)
    case object([String: JSONValue])
    case array([JSONValue])

    init(from decoder: Decoder) throws {
        let container = try decoder.singleValueContainer()
        if let value = try? container.decode(Bool.self) {
            self = .bool(value)
        } else if let value = try? container.decode(Int.self) {
            self = .int(value)
        } else if let value = try? container.decode(Double.self) {
            self = .double(value)
        } else if let value = try? container.decode(String.self) {
            self = .string(value)
        } else if let value = try? container.decode([String: JSONValue].self) {
            self = .object(value)
        } else if let value = try? container.decode([JSONValue].self) {
            self = .array(value)
        } else if container.decodeNil() {
            self = .null
        } else {
            throw DecodingError.typeMismatch(
                JSONValue.self,
                DecodingError.Context(
                    codingPath: container.codingPath,
                    debugDescription: "Not a valid JSON value"
                )
            )
        }
    }
}

extension JSONValue: CustomStringConvertible {
    var description: String {
        switch self {
        case .null:
            return "null"
        case .bool(let value):
            return value.description
        case .int(let value):
            return value.description
        case .double(let value):
            return value.description
        case .string(let value):
            return "\"\(value.description)\""
        case .object(let value):
            return value.description
        case .array(let value):
            return value.description
        }
    }
}
