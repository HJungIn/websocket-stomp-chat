<!doctype html>
<html lang="en">
  <head>
    <title>Websocket ChatRoom</title>
    <!-- Required meta tags -->
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">

    <!-- Bootstrap CSS -->
    <link rel="stylesheet" href="/webjars/bootstrap/4.3.1/dist/css/bootstrap.min.css">
    <style>
      [v-cloak] {
          display: none;
      }
    </style>
  </head>
  <body>
    <div class="container" id="app" v-cloak>
        <div>
            <h2>{{room.name}}</h2>
        </div>
        <div class="input-group">
            <div class="input-group-prepend">
                <label class="input-group-text">내용</label>
            </div>
            <input type="text" class="form-control" v-model="message" @keyup.enter="sendMessage">
            <div class="input-group-append">
                <button class="btn btn-primary" type="button" @click="sendMessage">보내기</button>
            </div>
        </div>
        <ul class="list-group">
            <li class="list-group-item" v-for="message in messages">
                {{message.sender}} - {{message.message}}</a>
            </li>
        </ul>
        <div></div>
        <button class="btn btn-warning" type="button" @click="disconnect">go out</button>
    </div>
    <!-- JavaScript -->
    <script src="/webjars/vue/2.5.16/dist/vue.min.js"></script>
    <script src="/webjars/axios/0.17.1/dist/axios.min.js"></script>
    <script src="/webjars/bootstrap/4.3.1/dist/js/bootstrap.min.js"></script>
    <script src="/webjars/sockjs-client/1.1.2/sockjs.min.js"></script>
    <script src="/webjars/stomp-websocket/2.3.3-1/stomp.min.js"></script>
    <script>
        // websocket & stomp initialize
        var sock = new SockJS("http://v4.test.mand.co.kr:3189/wss-dm"
            // ,
            // undefined, {
            // cors: {
            //     origin: '*',
            //     withCredentials: true,
            //     path: 'http://localhost:8080'
            // }
        // }
        );
        // var sock = new SockJS("/ws-stomp");
        var ws = Stomp.over(sock);
        // ws.withCredentials = true;
        // ws.defaults.headers['Access-Control-Allow-Origin'] = '*';
        // ws.defaults.withCredentials = true;
        // ws.heartbeat.

        // vue.js
        var vm = new Vue({
            el: '#app',
            data: {
                roomId: '',
                room: {},
                sender: '',
                message: '',
                messages: []
            },
            created() {
                this.roomId = localStorage.getItem('wschat.roomId');
                // this.roomId = 'aa';
                this.sender = localStorage.getItem('wschat.sender');
                this.findRoom();
            },
            methods: {
                findRoom: function() {
                    axios.get('/chat/room/'+this.roomId).then(response => { this.room = response.data; });
                },
                sendMessage: function() {
                    ws.send("/pub/chat/message", {}, JSON.stringify({type:'TALK', roomId:this.roomId, sender:this.sender, message:this.message}));
                    this.message = '';
                },
                recvMessage: function(recv) {
                    this.messages.unshift({"type":recv.type,"sender":recv.type=='ENTER'?'[알림]':recv.sender,"message":recv.message})
                },
                disconnect: function() {
                    console.log('Disconnected');
                    ws.send("/pub/chat/message", {}, JSON.stringify({type:'LEAVE', roomId:vm.$data.roomId, sender:vm.$data.sender, message:'back!'}));
                    ws.disconnect();
                }
            }
        });
        // pub/sub event
        ws.connect({}, function(frame) {
            console.log(111111111111);
            ws.subscribe("/sub/chat/room/"+vm.$data.roomId, function(message) {
                var recv = JSON.parse(message.body);
                vm.recvMessage(recv);
            });
            console.log(2222222222222222222);
            ws.send("/pub/chat/message", {}, JSON.stringify({type:'ENTER', roomId:vm.$data.roomId, sender:vm.$data.sender}));
            // ws.disconnect();

        }, function(error) {
            alert("error "+error);
        });
        console.log(3333333333333333333333);
        ws.disconnect(function() {
            console.log('Disconnected');
            ws.send("/pub/chat/message", {}, JSON.stringify({type:'LEAVE', roomId:vm.$data.roomId, sender:vm.$data.sender, message:'back!'}));
            // ws.close();
        });

        ws.onclose = function(event) {
            console.log('WebSocket connection closed');
        };

        window.onbeforeunload = function() {
            console.log('@@@@@@@@@@@@@@');
            ws.send("/pub/chat/message", {}, JSON.stringify({type:'LEAVE', roomId:vm.$data.roomId, sender:vm.$data.sender, message:'back!'}));
        };

        window.onclose = function() {
            console.log('##########');
            ws.send("/pub/chat/message", {}, JSON.stringify({type:'LEAVE', roomId:vm.$data.roomId, sender:vm.$data.sender, message:'back!'}));
            ws.disconnect();
        };

    </script>
  </body>
</html>