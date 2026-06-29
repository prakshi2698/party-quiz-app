//first page which player sees as soon as they open the link of app.
//of enter pin, name, click join button
import { useEffect, useState } from "react";
import socket from "../socket";

function JoinPage({ onJoined }) {
  const [pin, setPin] = useState("");
  const [name, setName] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    //Register listeners once
    socket.on("room:joined", (data) => {
      onJoined({ pin: data.pin, name: data.playerName });
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
