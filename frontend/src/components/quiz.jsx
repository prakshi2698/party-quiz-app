import { useEffect, useState } from "react";
import Results from "./results";
import axios from "axios";
function Quiz() {
  //Inside quiz we will have list of questions,there options and answers
  const [quizBank, setQuizBank] = useState(null);
  //define state->useState to make the site responsive
  //useState gives 2 paramaters->variable and set method.

  //set initial ans to userState
  const [userAnswers, setUserAnswers] = useState([]);

  //also set the current ques we will begin the ques no from 0
  const [currentQuestion, setCurrentQuestion] = useState(0);

  //track selected answer by user of current ques
  //if user has not selected any option its null else option
  const selectedAnswer = userAnswers[currentQuestion];

  //track if quiz is finished- needed for generating results
  const [isQuizFinished, setIsQuizFinished] = useState(false);
  useEffect(() => {
    axios
      .get("http://localhost:9090/api/quiz")
      .then((res) => {
        const data = res.data;
        if (data.length === 0) {
          console.log("No quizzes found!");
          return;
        }

        // Fetch questions for the first quiz
        const quizId = data[0].id;
        axios
          .get(`http://localhost:9090/api/quiz/${quizId}/question`)
          .then((qRes) => {
            data[0].questionList = qRes.data;
            setQuizBank(data);
            setUserAnswers(Array(data[0].questionList.length).fill(null));
          });
      })
      .catch((error) => {
        console.error("Error fetching quiz:", error);
      });
  }, []);
  if (!quizBank) return <p>Loading quiz!</p>;
  //first I need to fetch the questions from quesBank
  const questions = quizBank[0].questionList;

  //assume initial ans arr be null for all 3 ques
  const initialAnswers = Array(questions.length).fill(null);

  //while clicking on option how the button would react
  function handleSelectOption(option) {
    //copy userAnswers to newUserAnswers arr
    const newUserAnswers = [...userAnswers];
    //set and for current quest as option selected by user
    newUserAnswers[currentQuestion] = option;
    //update ans selected by user
    setUserAnswers(newUserAnswers);
  }

  //when u select prev and next navigation button->
  //next should increase ques no(go to next ques)
  //prev should decrease ques no(go to prev ques)->also shouldnt be negative
  function goToNextQuestion() {
    if (currentQuestion === questions.length - 1) {
      setIsQuizFinished(true);
    } else {
      setCurrentQuestion(currentQuestion + 1);
    }
  }
  function goToPreviousQuestion() {
    if (currentQuestion > 0) {
      setCurrentQuestion(currentQuestion - 1);
    }
  }

  //if we want to restart quiz do below-(basically reset all states in quiz back to initial value)
  //change the setCurrentQuestion back  to 0
  //also reset the setUserAnswers back to initial answers or (null...)

  function restartQuiz() {
    setUserAnswers(initialAnswers);
    setCurrentQuestion(0);
    setIsQuizFinished(false);
  }

  //check if quiz is finished then return from results component
  if (isQuizFinished) {
    //here we can pass the userAnswers from the quiz to results as props
    //use props since we are passing from parent to child component
    //if pass something in same component we use state but in diff component use prop
    return (
      <Results
        userAnswers={userAnswers}
        questionBank={quizBank}
        restartQuiz={restartQuiz}
      />
    );
  }
  //in nav bar button we set below prop of disabled->
  //disable prev button from clicked when u are on 1st ques
  //disable next button from clicked if u have not selected any option
  return (
    <>
      <h2>Question {currentQuestion + 1}</h2>
      <p className="question">{questions[currentQuestion].text}</p>

      {questions[currentQuestion].optionList.map((opt) => (
        <button
          key={opt.id}
          className={"option" + (selectedAnswer === opt.id ? " selected" : "")}
          onClick={() => handleSelectOption(opt.id)}
        >
          {opt.text}
        </button>
      ))}

      <div className="nav-buttons">
        <button onClick={goToPreviousQuestion} disabled={currentQuestion === 0}>
          {" "}
          Previous{" "}
        </button>

        <button onClick={goToNextQuestion} disabled={!selectedAnswer}>
          {currentQuestion === questions.length - 1 ? "Finish Quiz" : "Next"}
        </button>
      </div>
    </>
  );
}
export default Quiz;
