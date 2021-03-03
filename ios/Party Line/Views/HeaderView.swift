import SwiftUI

struct HeaderView: View {
    var body: some View {
        VStack(alignment: .leading) {
            HStack(alignment: .center) {
                Text("Party line")
                    .font(.largeTitle)
                    .bold()
                Spacer()
                Image("logo")
            }
            Text("An audio API demo from Daily")
                .font(.footnote)
                .foregroundColor(.secondary)
        }
    }
}

struct HeaderView_Previews: PreviewProvider {
    static var previews: some View {
        HeaderView()
            .background(Color.Daily.backgroundColor)
            .previewLayout(.sizeThatFits)
    }
}
