/** The STOMP client that connects to the server for sending and receiving notifications */
let stompClient = null


/**
 * Creates a websocket connection anytime the document is ready.
 */
$(document).ready(() => {
    connect(); // Start the websocket connection
})


/**
 * Called by STOMPJS whenever the websocket needs to reconnect
 * @returns A new instance of SockJS.
 */
function mySocketFactory() {
    return new SockJS(window.location.pathname + "/../websocket")
}


/**
 * Connects via websockets to the server, listening to all messages from /notifications/sending/occasions
 * and designates handleNotification to run whenever we get a message
 */
function connect() {

    stompClient = new StompJs.Client();
    stompClient.configure({
        webSocketFactory: mySocketFactory,
        reconnectDelay: 1000,
        debug: function (str) {
            // Add debug logging here
        },
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        onStompError: function (frame) {
            // Add error log here
        }
    });

    stompClient.onConnect = (frame) => {
        stompClient.subscribe('notifications/sending/occasions', handleNotification);
    }

    stompClient.activate();
}


/**
 * Whenever we receive a message from the /notifications/sending/occasions, this function will run.
 * This takes the notification, checks what type it is, then calls the relevant helper function
 * to handle that notification.
 *
 * USE: the functions called from here are to be implemented in the javascript file they related to.
 *      For example, The project page handles delete events differently to the calendar, hence the
 *      events.js and calendar.js files will have different handler functions with the same name.
 *
 * @param notification The notification to handle. (modeled by OutgoingNotification)
 */
function handleNotification(notification) {
    const content = JSON.parse(notification.body);
    for (let message of content) {
        const action = message.action;
        try {
            switch (action) {
                case 'create' :
                    handleCreateEvent(message);
                    break;
                case 'update' :
                    handleUpdateEvent(message);
                    break;
                case 'delete' :
                    handleDeleteEvent(message);
                    break;
                case 'edit' :
                    handleNotifyEvent(message);
                    break;
                case 'stop' :
                    handleStopEvent(message);
                    break;
                default :
                    // Do nothing, unknown message format
                    break;
            }
        } catch (error) {
            // Silence Reference errors when functions aren't defined
            // console log this error when debugging
        }

    }
}


/**
 * Sends a message to the server.
 * We don't need to add our ID as the server can get it from the websocket authentication
 *
 * @param occasionType The type of the object being edited (milestone, deadline, event)
 * @param occasionId The ID of the object being edited
 * @param action What action the user has performed to create this message
 */
function sendNotification(occasionType, occasionId, action) {
    stompClient.publish({
        destination: "notifications/message",
        body: JSON.stringify({
            'occasionType': occasionType,
            'occasionId': occasionId,
            'action': action
        })
    });
}