# ZejfSeis

Java application for displaying data measured by homemade seismometer. Communicates with [ZejfCSeis](https://github.com/xspanger3770/ZejfCSeis) using TCP connection. It can be used to display realtime data, browse all stored data and also to view nearby earthquakes that might have been detected.

# Socket connection

To connect to a running [ZejfCSeis](https://github.com/xspanger3770/ZejfCSeis) server, simply go to `Settings->Socket` and enter the IP address and port number you used to start the server.

# Realtime tab
The realtime tab contains 2 main features. On the top is a interactive plot of the incoming data, that scales automatically based on the highest amplitude in the given time frame. The default setting for the time frame duration is 5 minutes and can be changed in `Settings->Realtime`. On the bottom is a spectrogram panel, that allows you to analyze the waveform in more detail and helps you to distinguish between noise and real earthquakes. You can tweak the colors by setting the `Spectrogram gain` field in `Settings->Realtime` so that background noise is displayed as a dark blue color.

![](https://user-images.githubusercontent.com/100421968/230724558-52bbcdf1-1ace-4fac-b23d-15c901bb1f0a.png)

# Drum tab
![](https://user-images.githubusercontent.com/100421968/230572243-ad604679-4adf-420e-9f8f-30c36f75cf50.png)

**Work in progress!**
