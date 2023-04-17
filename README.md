# ZejfSeis

ZejfSeis is a user-friendly and easy to use Java application that allows for the display and analysis of seismic data captured by a homemade seismometer. With its intuitive interface, users can easily access a range of useful features, including real-time seismic plot display, spectrogram visualization, and browsing of recorded data in a drum-like style. In addition to these functionalities, the application offers a convenient and efficient way to store recorded earthquake data in a local database for later review and analysis. Whether you're a professional seismologist or simply an enthusiast, ZejfSeis is a powerful tool that can help you gain a deeper understanding of the Earth's seismic activity!

## Getting started

To begin your seismology journey, it's recommended that you first review my tutorial on setting up the seismometer itself, which can be found at the following link: [Seismometer tutorial](https://github.com/xspanger3770/ZejfSeis/tree/develop/arduino). This guide provides instructions for assembling and calibrating the seismometer, ensuring that it's ready to capture accurate seismic data.

## Data source

There are two ways of connecting ZejfSeis application to a running seismometer. The easiest way is to plug your Arduino directly to your computer's USB port and use `Connection->Serial Port` option in the upper menu, which will establish a direct connection and allow you to view incoming data with ease.

Alternatively, you can opt for the slightly more involved approach of setting up a [ZejfSeis Server](https://github.com/xspanger3770/ZejfSeisServer) server on a separate device. While this requires a bit more setup time, it provides the benefit of running the project continuously and enabling you to connect to the running server from anywhere by simply entering its address under `Connection->Server`.

## Realtime tab
The Realtime tab contains two main features. On the top is an interactive plot of the incoming data, which scales automatically based on the highest amplitude in the given time frame. The default setting for the time frame duration is 5 minutes and can be changed in `Settings->Realtime`.

### Spectrogram

The bottom section of the Realtime tab is a Spectrogram panel, which allows you to analyze the waveform in more detail and helps you distinguish between noise and real earthquakes. You can adjust the colors by setting the Spectrogram gain field in `Settings->Realtime` so that background noise is displayed as a dark blue color.

![](https://user-images.githubusercontent.com/100421968/232130962-271493b3-8b2e-41bc-902b-3cf56cbbf69a.png)

## Drum tab

The Drum tab allows you to browse the recorded data. Each line represents a time interval, with its start labeled on the left side (red numbers indicating hours and gray ones minutes). By default, each line represents 10 minutes of data. For navigation, you can scroll with your mouse, or you can use the buttons on the upper control panel. The `<` button will move the drum by one line, and `<<` will move it by 10 lines. By pressing the `Now` button, you will move the drum to the latest data. To access older data, you can use the `Goto` button and enter a specific date and time. 

The drum can be configured by accessing `Settings->Drum`. By changing the value of `Gain`, you can adjust the scale of the waveform, and you can increase the `Decimate` field to lower the waveform resolution for better performance.

![](https://user-images.githubusercontent.com/100421968/232131108-2ccce048-c082-4465-bd44-5f26395e212f.png)

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

All the data displayed in the application are filtered using a band-pass filter, which makes frequencies outside the range will get attenuated. You can configure the filter using the `Filters` menu. You can either select one of the prepared filters or create custom one in `Filters->Custom`. These filters makes it easier seeing earthquakes at different distances by filtering some noise out from the signal.
