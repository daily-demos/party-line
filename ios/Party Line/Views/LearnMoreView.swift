import SwiftUI

struct LearnMoreView: View {
    private var url: URL {
        // FIXME: Update URL
        URL(string: "https://daily.co")!
    }

    var body: some View {
        HStack {
            Spacer()
            Link("Learn more about this demo", destination: self.url)
                .font(.footnote)
            Spacer()
        }
    }
}

struct LearnMoreView_Previews: PreviewProvider {
    static var previews: some View {
        LearnMoreView()
    }
}
