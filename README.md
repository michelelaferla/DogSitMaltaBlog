# DogSit Malta Blog — Android Studio Java Project

A Java/XML Android application demonstrating an online/offline blogging workflow
for dog-sitting posts in Malta.

## Open in Android Studio

1. Extract the ZIP.
2. Open the **DogSitMaltaBlog** folder in Android Studio.
3. Allow Gradle to sync and download dependencies.
4. Make sure Android SDK 36 is installed.
5. Run the `app` configuration on an emulator or Android device.

The project uses:

- Java 17
- XML layouts
- Android SQLite via `SQLiteOpenHelper`
- RecyclerView
- Android Activity Result APIs
- `FileProvider`
- Android `ACTION_SEND` / `ACTION_SEND_MULTIPLE`
- No Firebase
- No cloud database

## Coursework requirements covered

### 1. Offline text input
`PostEditorActivity.java` lets the user enter a title, Malta locality and blog
message without an internet connection.

### 2. Offline message management
The app supports:

- Create
- Edit
- View an individual message
- View the current list
- Search title/message/location
- Delete one message
- Long-press messages to select and delete multiple messages

### 3. SQLite on device
`DatabaseHelper.java` uses `SQLiteOpenHelper`.

Database:

`dogsit_blog.db`

Table:

`posts`

Fields:

- id
- title
- message
- location
- image_path
- created_at
- updated_at

No Firebase or remote database is used.

### 4. Attach image
`PostEditorActivity` supports:

- Gallery/system image selection
- Camera capture

Images are copied/saved to private internal application storage. SQLite stores
the local image path.

### 5. Share individual message
The **Share / Email** button uses the standard Android `ACTION_SEND` mechanism
and includes text plus the image when one is attached.

The Android chooser allows the user to choose Gmail, Outlook, or another
compatible email/sharing application installed on the device.

### 6. Publish one or multiple posts online
The **Publish online** button sends one post through the Android sharing
mechanism.

From the main list, long-press posts to select several posts and choose
**Publish** in the contextual action bar. This uses `ACTION_SEND_MULTIPLE` when
multiple images are present.

The destination can be a compatible installed public/social application such
as Facebook, Instagram, LinkedIn, etc.

The external platform controls the final publishing confirmation. Direct,
unattended posting to specific social networks would require that network's
authentication/API and potentially app review, which is deliberately not
required by this local SQLite project.

## Package

`com.example.dogsitmalta`

## Main source folders

```text
app/src/main/java/com/example/dogsitmalta/
├── MainActivity.java
├── PostEditorActivity.java
├── PostDetailActivity.java
├── adapter/
│   └── BlogPostAdapter.java
├── database/
│   └── DatabaseHelper.java
├── model/
│   └── BlogPost.java
└── utils/
    ├── ImageUtils.java
    └── ShareUtils.java
```

## Notes

`local.properties` is intentionally not included because Android Studio creates
it for the local computer's Android SDK path.
We will build the app as we go along. The code added here is just for demonstation purposes