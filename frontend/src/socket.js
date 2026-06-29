// src/socket.js
import { io } from "socket.io-client";

// connect to Node.js WebSocket server
const socket = io("http://localhost:3001");

export default socket;