# Presentation Layer Report

This document details the presentation layer of the client module of X Clone. (view models excluded)

## Overview
The presentation layer follows an MVVM pattern.    
The `presentation` package also contains `controllers`, `navigation`, `state`, and `theme` which are JavaFX/UI-specific concerns and are out of scope for this report.
* View models are excluded from this report, there is a dedicated separate report in regard to them.

---
## Navigation
This package is consisted of one interface `Navigator.java` , introducing navigation functions, and its implementation `JavaFxNavigator.java` . 
This package has the duty of switching between scenes and applying CSS styling to them according to the theme. 
When being constructed, the navigator stores the stage `MainApplication.java` passes to it. It also holds a `AuthViewModel` reference since it's a shared view model most of the controllers require it.
During the construction process, navigator load both styles for dark mode and light mode along with setting light mode as its initial theme mode. 
Navigator provides two methods for styling, one for changing theme and one for restyling a scene (usually when user changes the theme). 
The rest of the navigator methods are for changing scenes. Each method loads it's corresponding view and sets it as stage's scene. 
Each scene switching method loads the fxml file. Adds style to it. Provides the view's controller with the required view models along with passing a reference to itself. Finally, it might add some general sizing or page-positioning details to the scene, and then attaches it to the stage.

- **Navigator.java** — An interface introducing navigator's functionalities for switching scenes and styles
- **JavaFxNavigator.java** — Implementation of `Navigator.java` .
- ---
## Controllers
This package includes controllers for each view in the project, plus an abstract base controller. 
A controller's duty is to handle user interactions with UI, handle UI changes, handle scene switching, making the calls to view models and binding FXML objects to their corresponding property inside the view model.

- **BaseController** — The abstract base class of controllers. This controller has references to the navigator which each controller needs and the theme mode. This base class has methods for creating component nodes. Also, this class includes a method for changing style theme and a method to be overridden by the subclasses to update icons when theme changes
- **HomeController** — Handles Home page UI interactions.
- **LoginController** — Handles Login page UI interactions.
- **RegisterController** — Handles Register page UI interactions.
- **PostDetailsController** — Handles Post Details page UI interactions.
- **ProfileController** — Handles Profile page UI interactions.

---
## State
Currently, this package is dedicated to Home page only. It's consisted of this page's UI events contract and a state record. 
The UI events contract show what events the page has which has to be handled by the view model. The UI state record helps with dynamic rendering and UI changes.
The UI state holds information about different aspects of the view. For instance, it the view has tabs, the state record has a property indicating which tab user is currently on.

* State records are immutable for reliability. When a state changes, a new instance of the state record is returned.

- **FeedUiEvent** — A sealed interface introducing Home page events;
- **FeedUiState** — A record including Home page states along with methods for returning new instances of this record when a change in states is needed. This record also has an enum representing Home page tabs.

---
## Theme
This package only contain an enum representing styling theme, current themes are:

- Light mode
- Dark mode

each of these themes have their dedicated CSS styling file.

---
## Utils

This package contains utility tools for the presentation layer.

- **IconUtils** : This utility class has methods for retrieving UI icons for according to the theme. This class has methods for setting icons for buttons and labels. 
- **MediaUiUtils** : This utility class has methods for loading users' avatar, loading medias and their previews for posts, handling downloading media.  
---
## Tree

```tree
presentation
├───controllers
│   ├─── BaseController.java
│   ├─── HomeController.java
│   ├─── LoginController.java
│   ├─── PostController.java
│   ├─── ProfileController.java
│   └─── RegisterController.java
├───navigation
│   ├─── JavaFxNavigator.java
│   └─── Navigator.java
├───state
│   ├─── FeedUiEvent.java
│   └─── FeedUiState.java
├───theme
│   └─── ThemeMode.java
├───utils
│   ├─── IconUtils.java
│   └─── MediaUiUtills.java
├───viewmodels
│   ├─── AuthViewModel.java
│   ├─── FeedViewModel.java
│   ├─── FollowViewModel.java
│   ├─── HashtagViewModel.java
│   ├─── MediaViewModel.java
│   ├─── PostViewModel.java
│   └─── UserViewModel.java
└─── MainApplication.java
```
