class A {
    private v:string;
    constructor (val:string) {
        this.v = val;
    }
    ftake():string {
        return "hello " + this.v;
    }
}

let a = new A("something2");
console.log(a.ftake());

$(function(){
    console.log("JQuery started");
});


//var Stomp = require('stompjs');

//let socket = new SockJS("/Jackal");
//let stompClient = Stomp.over(socket);
