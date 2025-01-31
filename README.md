# BeatSaver Android Application

A mobile application for panic disorder risk assessment that combines ECG data from wearables with psychological evaluations.

## Features

### Main Components

- ECG data collection from Galaxy Watch
- Two standardized psychological assessments:
  - PDSS (Panic Disorder Severity Scale)
  - APPQ (Anxiety Sensitivity Profile Questionnaire)
- Comprehensive risk visualization

### UI Components

- Modern Material Design implementation
- Clear progress indicators
- Intuitive assessment flows
- Visual result presentation

## Assessments

### PDSS Assessment
- 7 core questions
- Response-driven navigation
- Progress tracking
- Real-time validation

### APPQ Assessment
- 27 questions total
- Grouped into 5 questions per page
- 0-8 scale responses
- SeekBar input implementation

### Results Display
- Overall risk probability
- Component-wise scores breakdown
- Heart rate metrics display
- HRV (Heart Rate Variability) visualization

## Technical Implementation

### Activities
- `MainActivity`: Entry point and navigation hub
- `DSM`: PDSS questionnaire implementation
- `SMPD`: APPQ questionnaire implementation
- `result`: Results visualization and analysis

### Layout Resources
- `activity_main.xml`: Main interface
- `activity_dsm.xml`: PDSS questionnaire layout
- `activity_smpd.xml`: APPQ questionnaire layout
- `activity_result.xml`: Results visualization

## Network Communication

- OkHttp client implementation
- RESTful API integration
- JSON request/response handling
- Error handling and retry logic

## Design

- Color scheme: Primary #5F8D80
- Background: #F4FBF8
- Material Components integration
- Responsive layouts
- Accessibility considerations

## Setup

1. Clone the repository
2. Open in Android Studio
3. Configure Galaxy Watch connection settings
4. Build and run

## Requirements

- Android SDK 21+
- Kotlin 1.5+
- Galaxy Watch compatibility
- Network connectivity

## Note

This application is part of an integrated system for panic disorder assessment and monitoring, designed to work with the BeatSaver backend server.
