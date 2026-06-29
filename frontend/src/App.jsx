//Apps.jsx is main router, decides which screen to show based on who you are

import { useState } from "react";
import "./index.css";

//import all components used in app
import Quiz from "./components/quiz";
import JoinPage from "./components/JoinPage";
import PlayerView from "./components/PlayerView";
import HostView from "./components/HostView";

function App() {
  //tracks which mode user selected-> null,host,player,solo
  const [mode, setMode] = useState(null);

  //stores player data after joining a room
  const [playerData, setPlayerData] = useState(null);

  //Screen1: Mode selection
  //when mode = null
  if (!mode) {
    return (
      <div className="app-container">
        <h1>Party Quiz App</h1>
        <button onClick={() => setMode("host")}> I am Host </button>
        <button onClick={() => setMode("player")}> I am a Player</button>
        <button onClick={() => setMode("solo")}> Solo Quiz</button>
      </div>
    );
  }

  //Screen2: Host View
  if (mode === "host") {
    return <HostView />;
  }

  //Screen3: Join Page
  if (mode === "player" && !playerData) {
    return <JoinPage onJoined={(data) => setPlayerData(data)} />;
  }

  //Screen4: Player View
  if (mode === "player" && playerData) {
    return <PlayerView pin={playerData.pin} playerName={playerData.name} />;
  }

  //Screen5: Solo Quiz
  if (mode === "solo") {
    return (
      <div className="app-container">
        <h1>Solo Quiz</h1>
        <Quiz />
      </div>
    );
  }
}

export default App;
