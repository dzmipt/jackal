let stompClient = null;

function connect(topic, obj) {
    var socket = new SockJS("/Jackal");
    stompClient = Stomp.over(socket);
    stompClient.reconnect_delay = 5000;
    stompClient.connect({}, function (frame) {
        console.log("Connected: " + frame);
       // stompClient.subscribe('/topic/jackal', actionResponse );
        //stompClient.subscribe('/jackal/init', onInit );
        //stompClient.subscribe('/jackal/view', onView );
        subscribe("init",onInit);
        subscribe("view",setView);
        sendMsg(topic, obj);
     }/*, function(error) {
        console.log('Error: '+error.headers.message);
     }, function() {
        console.log('Disconnected');
        stompClient = null;
     }*/
    );
}

function subscribe(topic,callback) {
    stompClient.subscribe("/jackal/"+topic, response => {
       // console.log(response.body);
        callback(JSON.parse(response.body));
     });
}

function sendMsg(topic, obj) {
    stompClient.send("/app/"+topic, {}, JSON.stringify(obj));
}

function send(topic, obj) {
    if (stompClient == null) connect(topic,obj);
    else sendMsg(topic,obj);
}

/*function sendTest() {
    send("test", {'name':'test'});
}*/

function actionResponse(response) {
   // console.log(response);
}

function onInit(res) {
//    console.log("Init: " + resMsg);
//    let res = JSON.parse(resMsg.body);
    init(res.icons);
    setView(res.view);
}
