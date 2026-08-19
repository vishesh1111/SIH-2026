# Implementation Plan - Fixing Structured Chatbot Responses

The chatbot is currently displaying raw JSON strings (e.g., `{"text": "..."}`) instead of formatted messages and interactive elements. This is happening because the backend returns structured data as a JSON-encoded string within the `text` field of the `ChatResponse`.

## User Review Required

> [!IMPORTANT]
> This plan assumes that the backend will continue to send structured data as a JSON string inside the `text` field. If the backend can be modified to send a proper `ChatResponse` with `action_buttons` populated directly, some of these changes might be redundant, but parsing the inner JSON is a robust client-side fix.

## Proposed Changes

### Chatbot Component

#### [MODIFY] [ChatbotViewModel.kt](file:///Users/visheshverma/Documents/SIH-2026/android/app/src/main/java/com/sih2026/touristsafety/presentation/screens/chatbot/ChatbotViewModel.kt)
- Add a local `data class` or use Moshi to parse the inner JSON structure if detected in `body.text`.
- Update `sendMessage` logic to:
    1. Check if `body.text` starts with `{` and ends with `}`.
    2. Attempt to parse it as a structured object (containing `text`, `action_buttons`, etc.).
    3. If parsing succeeds, extract the clean text and any buttons/images found inside.
    4. Update the `ChatMessage` with this extracted data.

#### [MODIFY] [ChatbotScreen.kt](file:///Users/visheshverma/Documents/SIH-2026/android/app/src/main/java/com/sih2026/touristsafety/presentation/screens/chatbot/ChatbotScreen.kt)
- Ensure the `MessageBubble` correctly handles the `actionButtons` (it already has some logic, but it needs to be verified).
- Update suggestion chips logic to hide suggestions after the first message or when the bot provides its own action buttons.

## Verification Plan

### Automated Tests
- I will verify the build succeeds after the changes.

### Manual Verification
1. Open the Chatbot screen.
2. Send a message that triggers a structured response (e.g., "Nearby attractions").
3. Verify that the response is shown as plain text (no JSON braces) and that any suggested actions appear as buttons.
