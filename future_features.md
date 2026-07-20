# Future Features: Receipt Scanning & Autofill

This document contains notes on how to implement automatic receipt scanning (OCR) to autofill Cash In transactions.

## Feature Overview
When a user attaches a receipt photo to a transaction, the app can automatically extract details like the Amount, Description, and Date, and pre-fill the transaction form fields.

## Approach 1: Google ML Kit (Recommended for this app)
**Best for:** Offline apps that need a completely free solution.
- **How it works:** Uses on-device Machine Learning to extract raw text from the image. We then write custom logic (RegEx or keyword matching) to find the amount (e.g., searching for "Total", "₹", etc.) and the date.
- **Pros:** 100% offline, perfectly matches the current local-only architecture of the app, completely free, very fast.
- **Cons:** Custom parsing logic might not be 100% accurate for every store's receipt format, requiring occasional manual correction by the user.

## Approach 2: Cloud AI (Google Gemini API / Cloud Document AI)
**Best for:** Flawless accuracy and structured data.
- **How it works:** Sends the image to a cloud AI model and requests a structured JSON response containing the Amount, Date, Category, and Vendor.
- **Pros:** Incredibly accurate. Can infer context (e.g., knows the difference between a phone number and a total amount).
- **Cons:** Requires an active internet connection and an API key. Could incur costs if usage scales heavily.

## Suggested UX Flow
1. User clicks **CASH IN**.
2. User clicks the **Camera/Image** icon and selects a receipt photo.
3. A loading indicator appears ("Scanning receipt...").
4. The Amount, Description, and Date fields auto-populate.
5. User reviews the extracted data, makes any necessary edits, and clicks **Save**.
