# **Daily Care App**

## **Overview**
The **Daily Care App** is designed to assist users in managing their daily tasks and contacts effectively. The app focuses on providing reminders for important activities and managing emergency and other contacts in a user-friendly interface. This document summarizes the progress made so far, highlighting key features and implementations.

---

## **Features**

### **1. Reminders**
The reminders feature allows users to:
- **Add new reminders**:
  - Users can select the type of reminder (e.g., medication, security).
  - Input the reminder details:
    - Name
    - Time
    - Interval (daily, every 2 days, specific weekday)
    - Start date
- **Categorized Reminders**:
  - Medication reminders.
  - Security-related reminders (e.g., lock doors, close windows).


### **2. Emergency Contacts**
The emergency contacts feature helps users manage and categorize their contacts:
- **Add Contacts**:
  - Users can add new contacts under:
    - Emergency Contacts.
    - Other Contacts.
  - Inputs include:
    - Name
    - Phone Number
- **Categorized Views**:
  - Contacts are displayed in two sections: Emergency and Other.
- **Call Contacts**:
  - Each contact entry includes a "Call" button, enabling direct calls from the app.
- **Simple Back Navigation**:
  - Users can return to the previous screen with a back button.

---

## **Design**
The app is designed to be **minimalist** and **accessible**, focusing on essential functionalities.

### **UI Highlights**
- **Toolbar**:
  - Includes navigation buttons and an add button for new entries.
- **Dialogs**:
  - Used for adding reminders and contacts.
  - Input fields for details (e.g., name, phone number, reminder time).


---

## **Implemented Code Highlights**



### **Reminder Feature**
- Implements a step-by-step flow for creating reminders:
  - Selecting the type (medication, security).
  - Setting the reminder name, time, interval, and start date.

### **EmergencyContactsActivity**
- Manages the emergency and other contacts feature.
- Allows users to add, save, and categorize contacts.
- Utilizes **dialogs** for user input.
---

