//decides the screen player sees in their mobile after join page
//view of selecting options
import { useState, useEffect } from "react";
import socket from "../socket";

//fixed array of colors & symbols for 4 option buttons
//index 0 = first option, index 1 = second option
const OPTION_STYLES = [
  { color: "#e21b3c", symbol: "▲" }, //red triangle for option 1
  { color: "#1368ce", symbol: "●" }, // option 2 = blue circle
  { color: "#ffa602", symbol: "■" }, // option 3 = yellow square
  { color: "#26890c", symbol: "◆" }, // option 4 = green diamond
];

//PlayerView component receives pin & playerName from App.jsx
function PlayerView({ pin, playerName }) {
  //stores current ques obj received from host
  const [question, setQuestion] = useState(null);

  //stores option id player clicked
  //null = player hasn't clicked anything yet
  const [selectedOption, setSelectedOption] = useState(null);

  //tracks if player has already submitted ans
  //if false- then can still click buttons if true buttons disabled, ans submitted
  const [isAnswered, setIsAnswered] = useState(false);

  //stores correct option id after host end question
  //null= ques still running, if "optId123" means host ended ques this is correct ans
  const [result, setResult] = useState(null);

  //tracks if player is waiting for host to start ques
  //if true show waiting screen else show ques with 4 buttons
  const [waiting, setWaiting] = useState(true);

  const [error, setError] = useState(null);
  //useEffect runs once when component first loads
  useEffect(() => {
    //listen for "room:question-start" event from Node.js server
    //this fires when host clicks "start ques" button
    socket.on("room:question-start", ({ question }) => {
      //save ques data to display on screen
      setQuestion(question);

      //reset selected option from prev ques
      setSelectedOption(null);

      //re enable buttons for new ques
      setIsAnswered(false);

      //clear result from prev ques
      setResult(null);
      //hide waiting screen & show ques+buttons
      setWaiting(false);
    });

    //listen for "room:question-end" event from Node.js server
    //fires when host clicks "end ques" button
    socket.on("room:question-end", ({ correctOptionId }) => {
      //save correct option id to compare with player's ans
      setResult(correctOptionId);
    });

    socket.on("room:quiz-ended", () => {
      setResult("QUIZ_OVER");
    });
    //listen for any error from server
    socket.on("room:error", (msg) => {
      setError(msg);
    });

    // cleanup function runs when player leaves this page
    return () => {
      //stop listening to ques start and end
      socket.off("room:question-start");
      socket.off("room:question-end");
      socket.off("room:quiz-ended");
      socket.off("room:error");
    };
  }, []); //empty arr

  //runs when player clicks any option button
  function handleSelectOption(optionId) {
    //if player already answered then do nothing
    if (isAnswered) return; //prevents player from changing ans after submit

    //save which option player clicked
    setSelectedOption(optionId);

    //mark as answered, this disables all 4 buttons
    setIsAnswered(true);

    //send player's ans to Node.js server. server will store ans and tell host
    // x/y players answered
    socket.emit("player:submit-answer", { pin, optionId });
  }
  //show error on screen if server sends one
  if (error) {
    return (
      <div className="error-screen">
        <h2>{error}</h2>
      </div>
    );
  }

  //Screen1: Waiting screen
  if (waiting) {
    return (
      <div className="waiting">
        <h2>Welcome {playerName}!</h2>
        <p>Waiting for host to start the quiz..!</p>
      </div>
    );
  }

  //Screen2: Result Screen- shows when result is set
  if (result) {
    if (result === "QUIZ_OVER") {
      return (
        <div className="result">
          <h2>Quiz Finished!</h2>
          <p>Thanks for playing!</p>
        </div>
      );
    }
    //compare player's selected option with correct option
    const isCorrect = selectedOption === result;

    return (
      <div className="result">
        <h2>{isCorrect ? "Correct Answer!" : "Wrong Answer!"}</h2>
        <p>Waiting for next question...</p>
      </div>
    );
  }

  //Screen3: Question Screen- shows when ques received & not yet ended
  //also display ques text & 4 colored option buttons
  return (
    <div className="player-view">
      <h3>{question.text}</h3>
      <div className="options-grid">
        {question.optionList.map((opt, index) => (
          <button
            key={opt.id}
            className="option-btn"
            style={{
              // background color from OPTION_STYLES array
              // index % 4 = always stays between 0-3
              // if player answered → add "88" to color hex
              //   "88" makes color semi-transparent (faded look)
              // if not answered → full bright color
              backgroundColor: isAnswered
                ? OPTION_STYLES[index % 4].color + "88"
                : OPTION_STYLES[index % 4].color,

              //if answered and this button is NOT selected one fade it to 50% opactiy
              opacity: isAnswered && selectedOption !== opt.id ? 0.5 : 1,

              //show "not-allowed" cursor after answering
              //show "pointer" cursor before answering
              cursor: isAnswered ? "not-allowed" : "pointer",
            }}
            //when clicked , run below
            onClick={() => handleSelectOption(opt.id)}
            //disable button after player answers
            disabled={isAnswered}
          >
            <span className="symbol">{OPTION_STYLES[index % 4].symbol}</span>
            <span className="opt-text">{opt.text}</span>
          </button>
        ))}
      </div>
      {isAnswered && <p className="answered-msg">Answer submitted!</p>}
    </div>
  );
}
//export so App.jsx can import and use this component
export default PlayerView;
