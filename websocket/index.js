//import http module,express framework, server class from socket.io, cors.
const http = require("http");
const express = require("express");
const { Server } = require("socket.io");
const cors = require("cors");
//create express app
const app  = express();

//tells express to use cors so react on port 5173 can talk to this serrver
app.use(cors());

//wrap express app inside http server
const server = http.createServer(app);

//create scoket.io server on top of http server
const io = new Server(server, {
    cors: {
        //only allow GET & POST request type from React app
        origin: "http://localhost:5173",
        methods: ["GET", "POST"]
    }
});

//create quiz state enums 
const QUIZ_STATE = {
    NOT_STARTED: "NOT_STARTED",
    AWAITING_PLAYERS: "AWAITING_PLAYERS",
    RUNNING: "RUNNING",
    ENDED: "ENDED"
}

//rooms object stores all active quiz sessions in memory
// structure: { "1234(or room pin)": { quizId, hostId, players, currentQuestion, answers, quizstate } }
const rooms = {};

//generates a random 4 digit pin like "1234" etc
function generatePIN(){
    return Math.floor(1000 + Math.random() * 9000).toString();

}

//runs every time new user(host or player) connects to server
io.on("connection", (socket) => {
    // socket = unique connection for this user
    // socket.id = unique ID assigned to each connected user
    console.log(`User connected: ${socket.id}`);

    //1) host creates a room- triggered when host select quiz & clicks start
    socket.on("host:create-room",(quizId) => {
        const pin = generatePIN(); //generate unique pin for this quiz session
        
        //create new room & store all its data
        rooms[pin] = {
            quizId: quizId,
            hostId: socket.id,
            players: [],
            currentQuestion: 0,
            answers: {},
            quizState: QUIZ_STATE.AWAITING_PLAYERS

        };

        //host joins socket room with this PIN- now host will recieve all msgs sent to this PIN room.
        socket.join(pin);
        socket.data.pin = pin;
        socket.data.role = "host";

        //send PIN back to host so they can display it on screen
        socket.emit("room:created",{ pin });

        console.log(`Room ${pin} created and state: AWAITING_PLAYERS`);
    });
    //2)Player joins a room- triggered when player enters PIN & name & clicks join
    socket.on("player:join-room", ({pin,playerName}) => {
        if(!rooms[pin]){
            socket.emit("room:error","Room not found! Check your PIN");
            return;
        }
        //block joining if quiz already started or running
        if(rooms[pin].quizState === QUIZ_STATE.RUNNING){
            socket.emit("room:error","Quiz already started, You cannot join now!");
            return;

        }
        if(rooms[pin].quizState === QUIZ_STATE.ENDED){
            socket.emit("room:error", "Quiz has ended! You cannot join.");
            return;
        }
        //save pin on socket
        socket.data.pin = pin;
        socket.data.role = "player"
        //add player to room's players list
        rooms[pin].players.push({id: socket.id, name: playerName});
        //player joins socket room so they recieve future broadcasts
        socket.join(pin);
        socket.emit("room:joined", {pin,playerName});

        //tell host that new player joined, send updated player list
        const hostId = rooms[pin].hostId;
        io.to(hostId).emit("room:player-joined" , {
            players: rooms[pin].players
        });
        console.log(`${playerName} joined room ${pin}`)
    })

    //2)host starts a question-triggerd when host click Start ques buttom
    socket.on("host:start-question", ({ pin, question }) => {

        //stop if room dont exist
        if (!rooms[pin]) return;

        //state check
        if(rooms[pin].quizState === QUIZ_STATE.RUNNING){
            socket.emit("room:error", "A question is already running");
            return;
        }
        if(rooms[pin].quizState === QUIZ_STATE.ENDED){
            socket.emit("room:error", "Quiz has ended. Cannot start a question.");
            return;
        }

        //clear prev answers so we start fresh for new question
        rooms[pin].quizState = QUIZ_STATE.RUNNING
        rooms[pin].answers = {};

        //send ques to Everyone in the room (host & all players) 
        //room name is pin
        io.to(pin).emit("room:question-start", { question });

        console.log(`Question started in room ${pin}`);
    });

    //3)Player submits answer-its triggerd when player clicks on 1 or 4 colors buttons
    socket.on("player:submit-answer", ({ pin, optionId }) => {
        //stop if room dont exist
        if(!rooms[pin]) return;

        //State check:
        if (rooms[pin].quizState !== QUIZ_STATE.RUNNING){
            socket.emit("room:error", "Cannot submit answer right now.");
            return;
        }
        //if user has prev answered the ques, prevent double answering
        if (rooms[pin].answers[socket.id]){
            socket.emit("room:error", "You already submitted an answer!");
            return;
        }
        //store this player's ans using socket.id as key
        //{ "socket123": "option456" }
        rooms[pin].answers[socket.id] = optionId;

        //count total players in room
        const totalPlayers = rooms[pin].players.length;

        //count how many players answered so far
        //Object.keys gives arr of keys & .length gives count
        const totalAnswered = Object.keys(rooms[pin].answers).length;

        //get host's socket id to send them update
        const hostId = rooms[pin].hostId;

        //tell host how many players has answered(like: 3/10)
        io.to(hostId).emit("room:answer-received", {
            answered: totalAnswered, //how many answered so far
            total: totalPlayers //total players in room
        });
        console.log(`Answer received in room ${pin}: ${totalAnswered}/${totalPlayers}`);
    });

    //4)Host ends a ques-triggered when host click end ques button
    socket.on("host:end-question", ({ pin, correctOptionId }) => {
        //stop if room dont exist
        if(!rooms[pin]) return;
        // STATE CHECK
        if (rooms[pin].quizState !== QUIZ_STATE.RUNNING) {
            socket.emit("room:error", "No question is currently running.");
            return;
        }

        // transition back to AWAITING_PLAYERS
        // host can now start next question or end the quiz
        rooms[pin].quizState = QUIZ_STATE.AWAITING_PLAYERS;
        //get all ans submitted by players
        const answers = rooms[pin].answers;

        //send results to everyone in room including host
        //each player will use correctOptionId to check if they were right
        io.to(pin).emit("room:question-end", {
            correctOptionId, //the correct ans id
            answers //all player ans {socketId: optionId}
        });
        console.log(`Question ended in room ${pin}`);
    });
    //host ends entire quiz
    socket.on("host:end-quiz", ({pin}) => {
        if(!rooms[pin]) return;

        //STATE CHECK - must end current question before ending quiz
        if (rooms[pin].quizState === QUIZ_STATE.RUNNING) {
            socket.emit("room:error", "End the current question before ending the quiz.");
            return;
        }

        //transition to ENDED
        rooms[pin].quizState = QUIZ_STATE.ENDED;

        //tell all players quiz is over
        io.to(pin).emit("room:quiz-ended");

        // delete room after delay so quiz-ended event reaches all clients
        setTimeout(() => {
            delete rooms[pin];
            console.log(`Room ${pin} deleted from memory`);
        }, 5000);

        console.log(`Quiz ended in room ${pin} and state:Ended`);
    })

    //5)User Disconnects triggered when browser closes or connection drops
    socket.on("disconnect", () => {
        //get which room this socket was in 
        const pin = socket.data.pin;
        const role = socket.data.role;

        //only run if this socket was in room
        if (pin && rooms[pin]){
            if(role === "host"){
                // host left means end quiz for everyone
                io.to(pin).emit("room:error", "Host disconnected. Quiz ended.");
                rooms[pin].quizState = QUIZ_STATE.ENDED;
                setTimeout(() => delete rooms[pin], 5000);

            }else if (role === "player") {
                // player left - remove from list, notify host
                rooms[pin].players = rooms[pin].players.filter(p => p.id !== socket.id);
                io.to(rooms[pin].hostId).emit("room:player-joined", { players: rooms[pin].players });
            }

            // //get host socket Id
            // const hostId = rooms[pin].hostId;

            // //tell host that player left,send updated player list
            // io.to(hostId).emit("room:player-joined", {
            //     players: rooms[pin].players //updated list without disconnected player
            // });
        }
        console.log(`User disconnected: ${socket.id}`);
    });

});
//start server on port 3001.
server.listen(3001, () => {
    console.log("WebSocket server running on port 3001");
});


