# Event Driven Order System - Frontend

This is a React (Vite) frontend for the Event Driven Order Processing System.

## Prerequisites
- Node.js (v16+) installed.

## Setup

1. **Install Dependencies**
   Open a terminal in this `frontend` folder and run:
   ```bash
   npm install
   ```

2. **Start Development Server**
   ```bash
   npm run dev
   ```
   The app will run at `http://localhost:3000`.

## Features
- **Login**: Enter any username (e.g., `user123`) to get a JWT token.
- **Dashboard**: View your order history and live status (requires clicking Refresh).
- **Create Order**: Place a new order to trigger the event chain.

## Backend Requirement
Ensure the backend services are running. The frontend expects the **API Gateway** to be available at `http://localhost:8080`.
