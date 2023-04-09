# ZejfSeis

ZejfSeis is a Java application designed to display data measured by a homemade seismometer. It communicates with [ZejfCSeis](https://github.com/xspanger3770/ZejfCSeis) through a TCP connection and provides real-time data display, browsing of stored data and also displaying nearby earthquakes that might have been detected.

## Socket connection

To connect to a running [ZejfCSeis](https://github.com/xspanger3770/ZejfCSeis) server, go to `Settings->Socket` and enter the IP address and port number of the server.

## Realtime tab
The Realtime tab contains two main features. On the top is an interactive plot of the incoming data, which scales automatically based on the highest amplitude in the given time frame. The default setting for the time frame duration is 5 minutes and can be changed in `Settings->Realtime`.

### Spectrogram

The bottom section of the Realtime tab is a Spectrogram panel, which allows you to analyze the waveform in more detail and helps you distinguish between noise and real earthquakes. You can adjust the colors by setting the Spectrogram gain field in `Settings->Realtime` so that background noise is displayed as a dark blue color.

![](https://user-images.githubusercontent.com/100421968/230724558-52bbcdf1-1ace-4fac-b23d-15c901bb1f0a.png)

## Drum tab

The Drum tab allows you to browse the recorded data. Each line represents a time interval, with its start labeled on the left side (red numbers indicating hours and gray ones minutes). By default, each line represents 10 minutes of data. For navigation, you can scroll with your mouse, or you can use the buttons on the upper control panel. The `<` button will move the drum by one line, and `<<` will move it by 10 lines. By pressing the `Now` button, you will move the drum to the latest data. To access older data, you can use the `Goto` button and enter a specific date and time. 

The drum can be configured by accessing `Settings->Drum Settings`. By changing the value of `Gain`, you can adjust the scale of the waveform, and you can increase the `Decimate` field to lower the waveform resolution for better performance.

![](https://user-images.githubusercontent.com/100421968/230572243-ad604679-4adf-420e-9f8f-30c36f75cf50.png)

### Data explorer

Another functionality of the Drum tab is the Data explorer. It can be accessed by dragging your mouse over part of the drum, which will highlight the selected time interval with a gray overlay. The Data explorer has three main display modes: chart, spectrogram, and FFT analysis. These can be accessed by right-clicking inside the window. You can also select a more precise time interval by dragging your mouse from left to right - this will zoom into the selected area. By dragging in the opposite direction, you will reset the zoom.

### Picking earthquakes

By left-clicking in the graph on two different locations, you can select the P and S wave arrival of a nearby earthquake, and the estimated epicenter distance will appear in the lower right corner. You can then click the `Add Event` button to save the event into your event database.

![screenshot_data_explorer](https://user-images.githubusercontent.com/100421968/230775680-46cffdaa-9761-4142-9779-d044ef3d1dd1.png)

**Work in progress!**
