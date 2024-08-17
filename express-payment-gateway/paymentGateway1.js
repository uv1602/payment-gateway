const express = require("express");
const bodyParser = require("body-parser");
const app = express();
const port = 3000;

// Middleware to parse JSON bodies
app.use(bodyParser.json());

// Initialize a counter for requests
let requestCounter = 0;

// Middleware to block every 4th request
app.use((req, res, next) => {
  requestCounter += 1;

  // Check if the request is the 4th one
  if (requestCounter % 4 === 0) {
    // Respond with a 403 Forbidden status for every 4th request
    return res
      .status(403)
      .json({ success: false, message: "Request blocked. Try again later." });
  }

  // Continue to the next middleware or route handler
  next();
});

// Mock payment processing function
function processPayment(paymentDetails) {
  // Simulate payment processing logic here
  // This could be where you integrate with a real payment gateway
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      if (paymentDetails.amount > 0) {
        resolve({ success: true, message: "Payment processed successfully!" });
      } else {
        reject({ success: false, message: "Invalid amount!" });
      }
    }, 1000); // Simulate network delay
  });
}

// Endpoint to handle payment requests
app.post("/pay", async (req, res) => {
  try {
    const paymentDetails = req.body;

    // Validate payment details
    if (
      !paymentDetails.amount ||
      !paymentDetails.currency ||
      !paymentDetails.source
    ) {
      return res
        .status(400)
        .json({ success: false, message: "Missing required fields" });
    }

    // Process payment
    const result = await processPayment(paymentDetails);

    // Respond with success
    res.status(200).json(result);
  } catch (error) {
    // Respond with error
    res.status(500).json(error);
  }
});

// Start the server
app.listen(port, () => {
  console.log(`Payment gateway server running at http://localhost:${port}`);
});
