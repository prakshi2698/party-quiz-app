//HostView:control panel for host on laptop controls the quiz and sees live player responses
import { useState, useEffect } from "react";
import socket from "../socket";
//fetch quiz data from Spring Boot API
import axios from "axios";
//for QR code
import { QRCodeSVG } from "qrcode.react";

function HostView() {
  //stores generated PIN for this quiz session
  const [pin, setPin] = useState(null);

  //stores list of players who joined the room
  //updates live as players join/leave
  const [players, setPlayers] = useState([]);

  //stores full quiz object-fetched from Spring Boot when host selects a quiz
  const [quiz, setQuiz] = useState(null);

  //tracks which ques we are currently on - starts at 0
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);

  //tracks how many players have answered curr ques
  const [answered, setAnswered] = useState(0);

  //tracks if current ques is active/started
  const [questionStarted, setQuestionStarted] = useState(false);

  //stores list of all quizzed fetched from spring boot
  const [quizList, setQuizList] = useState([]);

  const [quizEnded, setQuizEnded] = useState(false);

  //making a join link inside QR code
  //when player scans QR, it opens link with pin- /join-quiz?pin=4521
  //JoinPage will read pin=4521 and autofill it
  const joinLink = `${window.location.origin}/join-quiz?pin=${pin}`;

  //Fetch quiz list from spring boot
  useEffect(() => {
    //call spring boot api to get all quizzes
    axios
      .get("http://localhost:9090/api/quiz")
      .then((res) => setQuizList(res.data));
  }, []);

  //Listen for socket events from node.js
  useEffect(() => {
    //listen for "room:player-joined" from node.js server
    //fires everytime new player joins room
    socket.on("room:player-joined", ({ players }) => {
      //update player list with new list from server
      setPlayers(players);
    });
    //listen for "room:answer-received" from Node.js server
    socket.on("room:answer-received", ({ answered, total }) => {
      //update how many players have answered
      setAnswered(answered);
    });
    //listen for PIN sent back from Node.js server
    socket.on("room:created", ({ pin }) => {
      //save pin to state
      setPin(pin);
    });
    //cleanup remove listener
    return () => {
      socket.off("room:player-joined");
      socket.off("room:answer-received");
      socket.off("room:created");
    };
  }, []);

  // host selects quiz and creates room
  async function handleStartQuiz(quizId) {
    //fetch full quiz details from Spring boot
    //includes all ques & options
    const res = await axios.get(`http://localhost:9090/api/quiz/${quizId}`);

    //save full quiz data to state
    setQuiz(res.data);

    //tell node.js server to create room for this quiz
    socket.emit("host:create-room", quizId);
  }
  //Host starts a ques
  function handleStartQuestion() {
    //get current ques object from quiz
    const question = quiz.questionList[currentQuestionIndex];
    setAnswered(0); //reset ans coun to 0 for new ques
    //mark ques as started
    setQuestionStarted(true);
    //send ques to all players in room via node.js
    socket.emit("host:start-question", { pin, question });
  }
  //Host Ends a ques
  function handleEndQuestion() {
    //get current ques object
    const question = quiz.questionList[currentQuestionIndex];
    //find correct option from question's option list
    const correctOption = question.optionList.find((o) => o.isCorrect);
    //send correct option id to node.js
    socket.emit("host:end-question", {
      pin,
      correctOptionId: correctOption.id,
    });
    //mark ques as not started
    setQuestionStarted(false);

    //check if this was last ques end the quiz
    if (currentQuestionIndex === quiz.questionList.length - 1) {
      setQuizEnded(true);
      socket.emit("host:end-quiz", { pin });
    }
  }
  //Host moves to next question
  function handleNextQuestion() {
    //increase ques index by 1
    setCurrentQuestionIndex((prev) => prev + 1);
    //reset ans count for new ques
    setAnswered(0);
  }
  //Screen1: Quiz selection screen- host see list of quiz to pick from:
  if (!pin) {
    return (
      <div className="host-view">
        <h2>Select Quiz to Start</h2>
        {quizList.map((q) => (
          <button key={q.id} onClick={() => handleStartQuiz(q.id)}>
            {q.name}
          </button>
        ))}
      </div>
    );
  }
  //show loading screen while quiz data is fetched
  if (!quiz) {
    return <p>Loading...</p>;
  }

  //get current ques object based on current index
  const currentQuestion = quiz.questionList[currentQuestionIndex];
  if (quizEnded) {
    return (
      <div className="host-view">
        <h2>Quiz Over!</h2>
        <p>Total Players: {players.length}</p>
      </div>
    );
  }

  //Screen2: Main host control screen: shows when room created &quiz loaded
  return (
    <div className="host-view">
      <div
        className="pin-display"
        style={{
          display: "flex",
          alignItems: "center",
          gap: "30px",
        }}
      >
        <div>
          <h2>
            Game PIN: <span>{pin}</span>
          </h2>
          <p>Players joined: {players.length}</p>
        </div>
        <QRCodeSVG value={joinLink} size={180} />
      </div>
      <div className="question-display">
        <h3>
          Question {currentQuestionIndex + 1}/{quiz.questionList.length}
        </h3>
        <p>{currentQuestion.text}</p>
      </div>
      {questionStarted && (
        <div className="answer-count">
          <h3>
            {answered}/{players.length} answered
          </h3>
        </div>
      )}
      <div className="host-controls">
        {!questionStarted ? (
          <button onClick={handleStartQuestion}>Start Question</button>
        ) : (
          <button onClick={handleEndQuestion}>End Question</button>
        )}
        {!questionStarted && currentQuestionIndex > 0 && (
          <button onClick={handleNextQuestion}>Next Question</button>
        )}
      </div>
    </div>
  );
}
export default HostView;
