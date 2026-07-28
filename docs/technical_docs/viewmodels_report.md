# Presentation Layer ViewModels Report

This document details the viewmodes in presentation layer of the client module of X Clone.

## Overview
The presentation layer follows an MVVM pattern.    
Alongside `viewmodels`, the `presentation` package also contains `controllers`, `navigation`, `state`, and `theme` — these are JavaFX/UI-specific concerns and are out of scope for this report.

Each viewmodel wraps a corresponding port from `domain/ports` — `AuthPort`, `FeedPort`, `FollowPort`, `HashtagPort`, `MediaPort`, `PostPort`, `UserPort` — and exposes observable state and actions to the controllers, without the controllers ever talking to the ports directly.

## ViewModels

* **AuthViewModel** — wraps `AuthPort`. Handles login and registration flows, exposing the credentials/error state controllers bind to.
* **UserViewModel** — wraps `UserPort`. Manages loading and updating user profile data. Keeps edit-form fields separate from the currently loaded profile state, so an in-progress edit doesn't corrupt the displayed profile until it's confirmed.
* **PostViewModel** — wraps `PostPort`. The largest viewmodel — handles post creation, deletion, retrieval, like/unlinke and search.
* **MediaViewModel** — wraps `MediaPort`. Handles media registration/selection tied to post creation, using `MediaDto` to move media data across the boundary.
* **FollowViewModel** — wraps `FollowPort`. Manages follow/unfollow actions and follower/following state for a given user.
* **FeedViewModel** — wraps `FeedPort`. Pulls and exposes the current user's feed, backed by `PostDto` entries rendered in the feed view. It also handles real-time updates.
* **HashtagViewModel** — wraps `HashtagPort`. Handles hashtag search/lookup, exposing posts associated with a given hashtag.

### Shared patterns across viewmodels
* **Optimistic local updates**: mutations update the already-loaded DTOs in place rather than triggering a re-fetch.
* **Read-only exposure**: single-object loaded state is exposed via `ReadOnlyObjectProperty` on public getters, so controllers can observe but not mutate viewmodel state directly.
* etc.

