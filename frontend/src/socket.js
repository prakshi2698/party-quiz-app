// src/socket.js
import { io } from "socket.io-client";

const SOCKET_URL = window.location.hostname === "localhost"
    ? "http://localhost:3001"
    : "http://54.165.56.138:3001";
// connect to Node.js WebSocket server
const socket = io(SOCKET_URL);

export default socket;