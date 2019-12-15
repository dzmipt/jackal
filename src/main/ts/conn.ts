declare var Stomp:any;

let stompClient = null;
//import * as stomp from 'stompjs';
//import * from 'stompjs';

function connect(topic:string, obj:any) {
    let socket = new SockJS("/Jackal");
    stompClient = Stomp.over(socket);
    stompClient.reconnect_delay = 5000;
    stompClient.connect({}, function (frame) {
        console.log("Connected: " + frame);
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

function subscribe(topic:string, callback: (res:any)=>void) {
    stompClient.subscribe("/jackal/"+topic, response => {
       // console.log(response.body);
        callback(JSON.parse(response.body));
     });
}

function sendMsg(topic:string, obj:any) {
    stompClient.send("/app/"+topic, {}, JSON.stringify(obj));
}

function send(topic:string, obj:any) {
    if (stompClient == null) connect(topic,obj);
    else sendMsg(topic,obj);
}
