//in results we want to calculate score of user
//so we use props questionBank and userAnswers which we get from quiz
//using it we calculate user's score->totalScore will be length of questioBank
function Results({ questionBank, userAnswers, restartQuiz }) {
  const questions = questionBank[0].questionList;
  function getScore() {
    let finalScore = 0;
    //to check the score we want to check how many answers user have corectly given
    //so loop through userAnswers and compare answer by user with answer in questionBank

    userAnswers.forEach((answer, index) => {
      const correctOption = questions[index].optionList.find(
        (opt) => opt.isCorrect,
      );
      if (answer === correctOption?.id) {
        finalScore++;
      }
    });
    return finalScore;
  }
  const score = getScore();

  return (
    <>
      <h2> Quiz Completed!</h2>
      <p>
        {" "}
        Your Score: {score}/{questions.length}
      </p>
      <button className="restart-button" onClick={restartQuiz}>
        {" "}
        Restart Quiz
      </button>
    </>
  );
}
export default Results;
