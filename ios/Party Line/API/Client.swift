import SwiftUI
import Combine
import WebKit
import AVFoundation
import os.log

/// <#Description#>
final class Client: NSObject, ObservableObject {
    /// The username to use when joining a room.
    let userName: String

    /// The name of the room to connect to.
    var roomName: String?

    /// The Daily account server to connect to.
    let serverURL: URL

    /// The room currently joined by `self`.
    @Published
    var room: Room?

    /// The room's participants with speaking permissions.
    @Published
    private(set) var speakers: [Participant] = []

    /// The room's participants with listening permissions.
    @Published
    private(set) var listeners: [Participant] = []

    /// The room's participants.
    @Published
    private(set) var participants: [String: Participant] = [:] {
        didSet {
            var speakers: [Participant] = []
            var listeners: [Participant] = []

            for participant in self.participants.values {
                switch participant.role {
                case .moderator, .speaker:
                    speakers.append(participant)
                case .listener:
                    listeners.append(participant)
                }
            }

            let userNameComparator: (Participant, Participant) -> Bool = {
                $0.sortKey < $1.sortKey
            }

            speakers.sort(by: userNameComparator)
            listeners.sort(by: userNameComparator)

            self.speakers = speakers
            self.listeners = listeners
        }
    }

    /// The expected expiration date
    var expirationDate: Date {
        guard let room = self.room else {
            // If we haven't yet retrieved a room object
            // return the upper bound instead:
            return Date().addingTimeInterval(Client.maxDuration * 10.0)
        }

        return room.config.expirationDate
    }

    /// Convenience property for accessing the local participant.
    var local: Participant? {
        self.participants[Participant.localKey]
    }

    /// Convenience property for check if the participant is the owner of a room.
    var isOwner: Bool {
        self.local?.isOwner ?? false
    }

    /// A headless webview for working with `Daily.js`.
    private lazy var webView: WKWebView = Self.makeWebView(messageHandler: self)

    /// The Daily.co account's server URL.
    static var demoServerURL: URL {
        return URL(string: "https://devrel.daily.co")!
    }

    /// For the demo rooms expire after at most 10 minutes.
    static var maxDuration: TimeInterval {
        10.0 * 60.0
    }

    /// The URL of the page that.
    private static var pageURL: URL {
        URL(string: "https://audio-only-server.netlify.app/static/bridge.html")!
    }

    private static let eventsMessageHandlerName: String = "events"
    private static let consoleMessageHandlerName: String = "console"
    private static let errorsMessageHandlerName: String = "errors"

    // MARK: - Initialization

    /// Creates a client configured with the provided arguments.
    /// - Parameters:
    ///   - userName: The username to join rooms with.
    ///   - roomName: The room's name to join.
    ///   - serverURL: The room's server URL
    init(
        userName: String,
        roomName: String?,
        serverURL: URL
    ) {
        logger.trace(#function)

        self.userName = userName
        self.roomName = roomName
        self.serverURL = serverURL
        self.room = nil

        super.init()

        self.webView.navigationDelegate = self

        self.attachHeadlessWebViewToViewHierarchy()

        self.loadWebView()
    }

    /// Makes sure we don't leak the webview.
    deinit {
        logger.trace(#function)

        self.detachHeadlessWebViewFromViewHierarchy()
    }

    /// Creates a pre-configured webview.
    /// - Parameter configure: additional configurations
    /// - Returns: <#description#>
    private static func makeWebView(
        messageHandler: WKScriptMessageHandler
//        configure: (WKWebViewConfiguration) -> ()
    ) -> WKWebView {
        let webpagePreferences = WKWebpagePreferences()
        webpagePreferences.allowsContentJavaScript = true
        webpagePreferences.preferredContentMode = .mobile

        let configuration = WKWebViewConfiguration()

        // Make sure the webview's `<audio>` elements are
        // allowed to auto-play inline:
        configuration.allowsInlineMediaPlayback = true
        // … and without user interaction:
        configuration.mediaTypesRequiringUserActionForPlayback = []

        // We need to register corresponding message handlers
        // in order to be able to receive messages from JS:
        configuration.userContentController.add(
            messageHandler,
            name: Self.eventsMessageHandlerName
        )

        // These message handlers are optional, but handy:
        configuration.userContentController.add(
            messageHandler,
            name: Self.consoleMessageHandlerName
        )
        configuration.userContentController.add(
            messageHandler,
            name: Self.errorsMessageHandlerName
        )

        configuration.defaultWebpagePreferences = webpagePreferences

        // We don't intend to ever show the webview, so we give it a zero-size:
        let webView = WKWebView(
            frame: CGRect.zero,
            configuration: configuration
        )

        // We pretend to be iOS Safari:
        webView.customUserAgent = """
            Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_6) \
            AppleWebKit/605.1.15 (KHTML, like Gecko) \
            Version/14.0.3 Safari/605.1.15
        """

        // Since the webview is off-screen anyway these are
        // not strictly necessary, but it doesn't harm either:
        webView.allowsBackForwardNavigationGestures = false
        webView.scrollView.isScrollEnabled = false

        return webView
    }

    /// Unfortunately the only way to ensure a webview actually
    /// fully works is to attach it to the app's view hierarchy.
    private func attachHeadlessWebViewToViewHierarchy() {
        guard let window = UIApplication.shared.windows.first else {
            logger.warning(
                "Could not attach Webview to view hierarchy: Expected window, found none."
            )
            return
        }

        // An alpha of `0.0` would cause the webview to not work,
        // so we use the smallest positive value greater than zero:
        self.webView.alpha = .leastNonzeroMagnitude
        // The view is of zero-size, so we might as well disable interactions:
        self.webView.isUserInteractionEnabled = false

        window.addSubview(self.webView)
    }

    /// Removes the headless webview from the view hierarchy to
    /// prevent leaking it on deinit.
    private func detachHeadlessWebViewFromViewHierarchy() {
        self.webView.removeFromSuperview()
    }

    /// Loads the server-side page used for bridging.
    private func loadWebView() {
        let pageURL = Self.pageURL

        logger.debug("LOADING: \(pageURL.absoluteString)")

        self.webView.load(URLRequest(url: pageURL))
    }

    /// Attempts to join a room (and create one if necessary).
    func start() {
        if let roomName = self.roomName {
            self.joinRoom(
                url: self.serverURL.appendingPathComponent(roomName)
            )
        } else {
            self.createAndJoinRoom()
        }
    }

    // MARK: - Room Initilization

    /// Creates a room and joins it as the owner.
    func createAndJoinRoom() {
        self.callJavascript(
            """
            createAndJoinRoomNonBlocking({
                userName: "\(self.userName)"
            });
            """
        )
    }

    /// Joines a room at `url` as a listener.
    func joinRoom(url: URL) {
        self.callJavascript(
            """
            joinRoomNonBlocking({
                userName: "\(self.userName)",
                roomUrl: "\(url.absoluteString)"
            });
            """
        )
    }

    // Leaves the currently joined room.
    func leaveRoom() {
        self.callJavascript(
            """
            leaveRoomNonBlocking({});
            """
        )
    }

    // MARK: - User Controls

    /// Sets the microphone's `enabled` state.
    /// - Parameter microphoneEnabled: `true` if enabled, otherwise `false`.
    func set(microphoneEnabled: Bool) {
        self.callJavascript(
            """
            setMicrophoneEnabled(\(microphoneEnabled));
            """
        )
    }

    /// Sets the hand's `raised` state.
    /// - Parameter handRaised: `true` if raised, otherwise `false`.
    func set(handRaised: Bool) {
        self.callJavascript(
            """
            setHandRaised(\(handRaised));
            """
        )
    }

    /// Sets the user's username.
    /// - Parameter username: the username to change to
    func set(username: String) {
        self.callJavascript(
            """
            setUserName("\(username)");
            """
        )
    }

    // MARK: - Moderator Controls

    /// Promotes a given participant to moderator.
    /// - Parameter sessionID: the participant's session id.
    func makeModerator(sessionID: SessionID) {
        self.callJavascript(
            """
            makeModerator("\(sessionID)");
            """
        )
    }

    /// Promotes a given participant to speaker.
    /// - Parameter sessionID: the participant's session id.
    func makeSpeaker(sessionID: SessionID) {
        self.callJavascript(
            """
            makeSpeaker("\(sessionID)");
            """
        )
    }

    /// Demotes a given participant to listener.
    /// - Parameter sessionID: the participant's session id.
    func makeListener(sessionID: SessionID) {
        self.callJavascript(
            """
            makeListener("\(sessionID)");
            """
        )
    }

    /// Mutes a given participant.
    /// - Parameter sessionID: the participant's session id.
    func muteParticipant(sessionID: SessionID) {
        self.callJavascript(
            """
            muteParticipant("\(sessionID)");
            """
        )
    }

    /// Ejects a given participant from the room.
    /// - Parameter sessionID: the participant's session id.
    func ejectParticipant(sessionID: SessionID) {
        self.callJavascript(
            """
            ejectParticipant("\(sessionID)");
            """
        )
    }

    // MARK: - Private Utility Methods

    /// Calls the provided javascript code, passing the result to `completionHandler`.
    /// - Parameters:
    ///   - javascript: javascript code to evaluate
    ///   - completionHandler: completion handler
    private func callJavascript(
        _ javascript: String,
        completionHandler: ((Result<Any, Error>) -> ())? = nil
    ) {
        logger.debug("Running Javascript: \(javascript)")

        self.webView.evaluateJavaScript(javascript) { (response, error) in
            let result: Result<Any, Error>

            if let error = error {
                logger.error("Error running Javascript: \(error.localizedDescription)")
                result = .failure(error)
            } else {
                result = .success(response as Any)
            }

            completionHandler?(result)
        }
    }
}

// MARK: - WKNavigationDelegate

extension Client: WKNavigationDelegate {
    /// Start the client once the page has finished loading.
    func webView(
        _ webView: WKWebView,
        didFinish navigation: WKNavigation!
    ) {
        logger.trace(#function)

        self.start()
    }

    /// Not stricly necessary, but rather useful when debugging.
    func webView(
        _ webView: WKWebView,
        didFail navigation: WKNavigation!,
        withError error: Error
    ) {
        logger.error("Error: \(error.localizedDescription)")
    }

    /// Not stricly necessary, but rather useful when debugging.
    func webView(
        _ webView: WKWebView,
        decidePolicyFor navigationResponse: WKNavigationResponse,
        decisionHandler: @escaping (WKNavigationResponsePolicy) -> Void
    ) {
        if let response = navigationResponse.response as? HTTPURLResponse {
            if response.statusCode >= 400 {
                logger.error("Error: \(navigationResponse)")
            }
        }

        decisionHandler(.allow)
    }
}

// MARK: - WKScriptMessageHandler

extension Client: WKScriptMessageHandler {
    func userContentController(
        _ userContentController: WKUserContentController,
        didReceive message: WKScriptMessage
    ) {
        logger.trace(#function)

        logger.debug("message.name: \(message.name)")

        switch message.name {
        case Self.eventsMessageHandlerName:
            self.handleEventMessage(body: message.body)
        case Self.consoleMessageHandlerName:
            self.handleConsoleMessage(body: message.body)
        case Self.errorsMessageHandlerName:
            self.handleErrorMessage(body: message.body)
        case let name:
            fatalError("Unrecognized message: '\(name)'")
        }
    }

    private func handleEventMessage(body: Any) {
        logger.trace(#function)

        guard let json = body as? String else {
            fatalError("Expected JSON string, found '\(type(of: body))'")
        }

        logger.debug("JSON: \(json)")

        guard let data = json.data(using: .utf8) else {
            return
        }

        do {
            let decoder = JSONDecoder.daily
            let message = try decoder.decode(EventMessage.self, from: data)

            print("Event:", message.action, "=>" , String(reflecting: message.event))

            if let event = message.event {
                self.handleEvent(event)
            } else {
                logger.warning("Unrecognized event: \(message.action)")
                logger.warning("Event content: \(String(describing: message.content as Any))")
            }
        } catch let error {
            logger.error("Error decoding event message: \(error.localizedDescription)")
            return
        }
    }

    // This is not strictly necessary, but helps while debugging:
    private func handleConsoleMessage(body: Any) {
        logger.trace(#function)

        guard let json = body as? String else {
            fatalError("Expected JSON string, found '\(type(of: body))'")
        }

        guard let data = json.data(using: .utf8) else {
            return
        }

        do {
            let message = try JSONDecoder().decode(ConsoleMessage.self, from: data)
            switch message.level {
            case .log:
                logger.log("WebView Console: \(message.content)")
            case .info:
                logger.info("WebView Console: \(message.content)")
            case .warn:
                logger.warning("WebView Console: \(message.content)")
            case .error:
                logger.error("WebView Console: \(message.content)")
            }
        } catch let error {
            logger.error("Error decoding console message: \(error.localizedDescription)")
            return
        }
    }

    // This is not strictly necessary, but helps while debugging:
    private func handleErrorMessage(body: Any) {
        logger.trace(#function)

        guard let json = body as? String else {
            fatalError("Expected JSON string, found '\(type(of: body))'")
        }

        guard let data = json.data(using: .utf8) else {
            return
        }

        do {
            let message = try JSONDecoder().decode(ErrorMessage.self, from: data)
            logger.error("WebView Error: \(message.content)")
        } catch let error {
            logger.error("Error decoding error message: \(error.localizedDescription)")
            return
        }
    }

    private func handleEvent(_ event: Event) {
        switch event {
        case .demoCreatedRoom(let event):
            self.handleDemoCreatedRoom(event)
        case .demoCreatedToken(let event):
            self.handleDemoCreatedToken(event)
        case .demoJoinedRoom(let event):
            self.handleDemoJoinedRoom(event)
        case .appMessage(let event):
            self.handleAppMessageEvent(event)
        case .error(let event):
            self.handleErrorEvent(event)
        case .joinedMeeting(let event):
            self.handleJoinedMeetingEvent(event)
        case .participantJoined(let event):
            self.handleParticipantJoinedEvent(event)
        case .participantLeft(let event):
            self.handleParticipantLeftEvent(event)
        case .participantUpdated(let event):
            self.handleParticipantUpdatedEvent(event)
        }
    }

    private func handleDemoCreatedRoom(
        _ event: DemoCreatedRoomEvent
    ) {
        logger.trace(#function)

        self.room = event.room
    }

    private func handleDemoCreatedToken(
        _ event: DemoCreatedTokenEvent
    ) {
        logger.trace(#function)
    }

    private func handleDemoJoinedRoom(
        _ event: DemoJoinedRoomEvent
    ) {
        logger.trace(#function)

        self.room = event.room
    }

    private func handleAppMessageEvent(
        _ event: AppMessageEvent
    ) {
        logger.trace(#function)
    }

    private func handleErrorEvent(
        _ event: ErrorEvent
    ) {
        logger.error("Daily Error: \(event.message)")

        self.leaveRoom()
    }

    private func handleJoinedMeetingEvent(
        _ event: JoinedMeetingEvent
    ) {
        logger.trace(#function)

        self.participants = event.participants
    }

    private func handleParticipantJoinedEvent(
        _ event: ParticipantJoinedEvent
    ) {
        logger.trace(#function)

        let participant = event.participant
        let key = participant.key

        self.participants[key] = participant
    }

    private func handleParticipantLeftEvent(
        _ event: ParticipantLeftEvent
    ) {
        logger.trace(#function)

        let participant = event.participant
        let key = participant.key

        self.participants[key] = nil
    }

    private func handleParticipantUpdatedEvent(
        _ event: ParticipantUpdatedEvent
    ) {
        logger.trace(#function)

        let participant = event.participant
        let key = participant.key

        self.participants.updateValue(participant, forKey: key)
    }
}
