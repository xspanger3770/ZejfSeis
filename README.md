# ZejfSeis

Java application for displaying data measured by homemade seismometer. Communicates with [ZejfCSeis](https://github.com/xspanger3770/ZejfCSeis) using TCP connection. It can be used to display realtime data, browse all stored data and also to view nearby earthquakes that might have been detected.

# Socket connection

To connect to a running [ZejfCSeis](https://github.com/xspanger3770/ZejfCSeis) server, simply go to `Settings->Socket` and enter the IP address and port number you used to start the server.

# Realtime tab
The realtime tab contains 2 main features. On the top is a interactive plot of the incoming data, that scales automatically based on the highest amplitude in the given time frame. The default setting for the time frame duration is 5 minutes and can be changed in `Settings->Realtime`. 

### Spectrogram
On the bottom is a spectrogram panel, that allows you to analyze the waveform in more detail and helps you to distinguish between noise and real earthquakes. You can tweak the colors by setting the `Spectrogram gain` field in `Settings->Realtime` so that background noise is displayed as a dark blue color.

![](https://user-images.githubusercontent.com/100421968/230724558-52bbcdf1-1ace-4fac-b23d-15c901bb1f0a.png)

# Drum tab

The drum tab allows you to browse the recorded data. Each line represents a time interval whose start is labeled on the left side - red numbers indicating hours and gray ones minutes. On default, each line is 10 minutes. For navigation you can scroll with your mouse or you can use buttons on the upper control panel. `<` will move the drum by one line and `<<` will move it by 10 lines. By pressing the `Now` button you will move the drum to the latest data and for accessing older data you can use the `Goto` button and enter specific date and time. <br>
The drum can be configured by accessing `Settings->Drum Settings` - by changing the value `Gain` you can adjust the scale of the waveform and you can increase the `Decimate` field to lower the waveform resolution for better performance.

![](https://user-images.githubusercontent.com/100421968/230572243-ad604679-4adf-420e-9f8f-30c36f75cf50.png)

### Data explorer

Another functionality of the Drum tab is Data explorer. It can be accessed by dragging your mouse over part of the drum - it will highlight selected time interval with gray overlay. It has three main display modes: chart, spectrogram and FFT analyisis. Those can be accessed by right-clicking inside the window. You can also select more precise time interval by again dragging your mouse from left to right - this will zoom into the selected area. By dragging in the opposite direction you will reset the zoom.

### Picking earthquakes

By left-clicking in the graph on two different locations, you will select the P and S wave arrival of nearby earthquake and estimated epicenter distance will apear in the lower right corner. You can then click the `Add Event` button to save the event into your event database.

![screenshot_data_explorer](https://user-images.githubusercontent.com/100421968/230775680-46cffdaa-9761-4142-9779-d044ef3d1dd1.png)

**Work in progress!**
