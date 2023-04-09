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

By left-clicking in the graph on two different locations, you can select the P and S wave arrival of a nearby earthquake, and the estimated epicenter distance will appear in the lower right corner. You can then click the `Add Event` button to save the event into your event database so you can see it in the following Events tab.

![screenshot_data_explorer](https://user-images.githubusercontent.com/100421968/230775680-46cffdaa-9761-4142-9779-d044ef3d1dd1.png)

## Events tab

The Events tab displays a table of recorded earthquakes for each month. These events are automatically downloaded using the [EMSC API](https://www.seismicportal.eu/fdsn-wsevent.html) for the past ~2 days. By clicking the `Download` button, you can download events for the entire month.

The earthquake distance and the detection probability is calculated based on the coordinates entered at `Settings->Seismometer`.

To navigate through the months, use the buttons in the upper control panel. Click `<` to go one month into the past, `<<` to go one year back, and the text in between the buttons to fast forward to the current month.

Note: Keep in mind that the EMSC API has a limit on the number of requests per minute, so if you're having trouble downloading the events, you may need to wait a few minutes before trying again.

### Event explorer 

By double-clicking on an individual event in the Events tab, you can open the Event Explorer window. It is similar to the Data Explorer window, providing the three main display modes. Additionally, you can manually assign the measured amplitude for this earthquake and set the status of the event.

The status can be one of the following: `Unknown`, `Not Detected`, `Broken`, `Noise`, or `Detected`. Clicking the `Select` button will automatically assign the maximum amplitude from the chart as the event amplitude. Use the `Save` button to save and close any changes made, or use `Delete` to remove the earthquake from your database.

## Filters

All the data displayed in the application are filtered using a band-pass filter, meaning that frequencies outside the range specified at `Settings->Filter` will get attenuated. Additionally, there is a quick filter selector at the top of the main window. These filters makes it easier seeing earthquakes at different distances by filtering some noise out from the signal.
