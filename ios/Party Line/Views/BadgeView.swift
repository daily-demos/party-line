import SwiftUI

struct BadgeView<Content>: View
where
    Content: View
{
    let content: Content

    init(_ content: Content) {
        self.content = content
    }

    var body: some View {
        self.content
            .padding(5.0)
            .frame(width: 32.0, height: 32.0)
            .background(
                Circle()
                    .foregroundColor(Color.white)
            )
    }
}

struct BadgeView_Previews: PreviewProvider {
    static var previews: some View {
        BadgeView(Image(systemName: "mic"))
    }
}
