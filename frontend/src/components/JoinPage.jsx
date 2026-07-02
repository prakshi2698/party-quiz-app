//first page which player sees as soon as they open the link of app.
//of enter pin, name, click join button
import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import socket from "../socket";

function JoinPage({ onJoined }) {
  const navigate = useNavigate(); //this lets us change URL from inside component

  //useSearchParams reads the query param from URL
  const [searchParams] = useSearchParams();

  //if pin exists in URL use it, else make empty str for manual entry
  const pinFromURL = searchParams.get("pin") || "";

  const [pin, setPin] = useState(pinFromURL); //pre filled with URL pin
  const [name, setName] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    //Register listeners once
    socket.on("room:joined", (data) => {
      navigate("/quiz-live", {
        state: {
          pin: data.pin,
          playerName: data.playerName,
        },
      });
    });
    socket.on("room:error", (msg) => {
      setError(msg);
    });
    return () => {
      socket.off("room:joined");
      socket.off("room:error");
    };
  }, []);

  function handleJoin() {
    if (!pin || !name) {
      setError("Please enter PIN and name!");
      return;
    }
    setError("");
    //send join event to Node.js server
    socket.emit("player:join-room", { pin, playerName: name });
  }
  return (
    <div className="join-container">
      <h2>Join Quiz</h2>
      <input
        type="text"
        placeholder="Enter PIN"
        value={pin}
        //if pin came from URL then disable input, player cant change it
        //if no pin in URL, allow manual typing as before
        disabled={!!pinFromURL}
        onChange={(e) => setPin(e.target.value)}
      />
      <input
        type="text"
        placeholder="Your Name"
        value={name}
        onChange={(e) => setName(e.target.value)}
      />
      {error && <p className="error">{error}</p>}
      <button onClick={handleJoin}>Join Quiz!</button>
    </div>
  );
}
export default JoinPage;
