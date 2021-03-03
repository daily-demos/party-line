import SwiftUI

struct TimeRemainingView: View {
    var timeRemaining: TimeInterval?

    static let formatter: DateComponentsFormatter = {
        let formatter = DateComponentsFormatter()
        formatter.allowedUnits = [.minute, .second]
        formatter.zeroFormattingBehavior = .pad
        return formatter
    }()

    var timeRemainingString: String {
        guard let timeRemaining = self.timeRemaining else {
            return "Demo ended"
        }

        let timeRemainingString = Self.formatter.string(
            from: timeRemaining
        )!

        return "Demo ends in \(timeRemainingString)"
    }

    var timeRemainingColor: Color {
        guard let timeRemaining = self.timeRemaining else {
            return .red
        }

        return timeRemaining > 10.0 ? .secondary : .red
    }

    var body: some View {
        Text(self.timeRemainingString)
            .foregroundColor(self.timeRemainingColor)
            .font(.footnote)
    }
}

struct TimeRemainingView_Previews: PreviewProvider {
    static var previews: some View {
        TimeRemainingView(timeRemaining: 42.0)
    }
}
