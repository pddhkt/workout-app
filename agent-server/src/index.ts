import "dotenv/config";
import express from "express";
import conversationRoutes from "./routes/conversations.js";
import messageRoutes from "./routes/messages.js";

const app = express();
const PORT = parseInt(process.env.SERVER_PORT || "3141", 10);

// CORS middleware - allow requests from any origin (mobile app on different host)
app.use((_req, res, next) => {
  res.header("Access-Control-Allow-Origin", "*");
  res.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
  res.header(
    "Access-Control-Allow-Headers",
    "Origin, X-Requested-With, Content-Type, Accept, Authorization"
  );

  if (_req.method === "OPTIONS") {
    res.status(204).send();
    return;
  }

  next();
});

// JSON body parsing
app.use(express.json());

// Health check
app.get("/health", (_req, res) => {
  res.json({ status: "ok", timestamp: Date.now() });
});

// Mount routes
app.use("/conversations", conversationRoutes);
app.use("/conversations", messageRoutes);

// Start server
app.listen(PORT, "0.0.0.0", () => {
  console.log(`Workout Agent Server running at http://0.0.0.0:${PORT}`);
  console.log(`  Health check: http://localhost:${PORT}/health`);
  console.log(`  Conversations: http://localhost:${PORT}/conversations`);
  console.log(`  Messages: http://localhost:${PORT}/conversations/:id/messages`);
});
