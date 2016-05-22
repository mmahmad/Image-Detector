Image Detector
==============

Introduction
------------

This Android application is a client application that is designed to upload photos to the server which will be analyzed and classified. The server then sends its analysis back to this app, which displays it to the user in a proper UI.

The analysis part on the server is independent of this Android application. As such, this app can be used to send images and receive text.

How to Run
----------

Import the project into Android Studio, and run the application.

The UI is basic, and requires the user to perform 2 basic steps:

1) the user first selects a photo, either by capturing one via the camera, or by selecting one from the gallery

2) the user then inputs the IP address and the port number of the server running the server-side application.

Error handling is built-in, with proper messages to describe the problems, if faced, such as no connection, failure to connect to the server, no photo selected, etc.

