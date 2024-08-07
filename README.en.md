# iRadioAndroid (now also supports internet TV & WebSDR/KiwiSDR)

An experimental software kit for building new radios or converting old radios into an Android-based internet radio.

![sysoverview](https://github.com/radiolab81/iRadioAndroid/blob/main/pics4www/systemoverview.jpg)

#### Supported systems

Android-based systems (smartphones and tablet PCs) from API Level 17 (A4.2 Jelly Bean) to Android 12 are currently tested and supported.
Usage on newer devices seems to be possible in principle, but may be subject to certain restrictions in the future due to API changes by Google.
The devices usually do not need to be rooted!

#### System design

The design of the iRadioAndroid is based on the modular principle of the iRadio for Raspberry (https://github.com/radiolab81/iRadio) and the iRadioMini for ESP32 (https://github.com/radiolab81/iRadioMini).
In addition to the media player as a background process (iRadioPlayer), we have various "processes" for visualization (displayd...) and controls via GPIOs (gpiod) which are connected via an OTG USB serial port. Sample firmware for controlling the iRadioAndroid from outside is in the “firmware” folder.


![sysoverview](https://github.com/radiolab81/iRadioAndroid/blob/main/pics4www/folders.jpg)

The "drawable" folder contains all used image resources (PNGs with alpha channel) for the radio scale simulation added as an example. 

The central starting point for the various iRadioAndroid services is the iRadioStartup.java file:


![startup](https://github.com/radiolab81/iRadioAndroid/blob/main/pics4www/startup.jpg)

Here, as in the rc.local file for the iRadio for Raspberry, all components (background services, UI such as radio scale simulation) of the iRadioAndroid are selected and started:

Even if controlling the iRadio via buttons and rotary encoders simplifies integration into old radios, this option is only optional. Like any other app, the iRadioAndroid can be operated directly via the system's touch display, the included displayd example codes support this type of operation.
The iRadioAndroid, like all other iRadio ports, sees itself less as a ready-to-use solution, but rather as a guideline for your own ideas and developments. The target group is the technology and PC-savvy radio hobbyist!



#### Installation

To compile iRadioAndroid you first need Google's Android Studio. https://developer.android.com/studio
The current version 2023.1.1 Hedgehog is recommended by the developers. After downloading and installing the Android Studio, the iRadioAndroid is copied to the local projects directory.

Calling


`git clone https://github.com/radiolab81/iRadioAndroid/`


from terminal / projects-folder will do the job.

The future Android radio device must now be switched to developer mode. howto: https://developer.android.com/studio/debug/dev-options#enable

Activate USB debugging (if necessary debugging via WiFi) and then connecting (pairing) to the development PC and the Android Studio. howto: https://developer.android.com/studio/run/device#connect

Immediately after compiling the iRadio application, it is also installed on the Android device connected to the PC. In addition, all debug output from the Android device is displayed in Android Studio (Logcat display), which makes later monitoring or troubleshooting easier in the event of problems.
![logcat](https://developer.android.com/static/studio/images/debug/logcat_dolphin_2x.png)

For performance reasons, it is recommended that all development work not be carried out on the Android Emulator, but directly with a real smartphone/tablet! Scale simulations usually have to be adapted to the resolution and geometry of the device used anyway!

![skalensim_cass](https://github.com/radiolab81/iRadioAndroid/blob/main/pics4www/skalensim.jpg)


Important: After the first start of iRadioAndroid, a small internal station list is still used!
First, access to the device memory must be granted to the iRadioAndroid app via Android settings/apps. This makes it possible to use your own station list (playlist.m3u), analogous to the iRadio/iRadioMini.
By default, this channel list is stored in the download folder of the Android device.
An existing playlist.m3u can be copied into the device from the PC using the ADB shell (comparable to SSH access from the iRadio on Raspberry).
If the Android device is paired with the PC via USB cable or WiFi, you can initiate a transfer to the smartphone/tablet from the PC folder where the channel list is stored as shown:


`adb push playlist.m3u /sdcard/Download`


With the adb shell command you can log into the phone and use (Linux) commands such as ls, cd, cp, mv, reboot ... to navigate in the phone's file system and check whether the channel list is available.
From this moment on, iRadioAndroid will use the self-created station list every time the app is started.

In the future, the iRadioAndroid can also be configured for new WiFi access via the ADB shell if your radio housing no longer allows direct access to a system touch screen:


`adb shell cmd -w wifi connect-network "Home" wpa2 "qwerty"`


This example sets up the Android device for the WiFi network "Home" with the password "qwerty".

For controlling iRadioAndroid via rotary pulse encoder and buttons, example code for the Arduino and RP2040 (Raspberry Pico) platform is included in the "firmware" folder.  To use this type of operation, in addition to an Arduino or RP2040 microcontroller itself, you also need the Arduino IDE https://www.arduino.cc/en/software, a USB programming cable and a USB OTG cable. After the microcontroller has been programmed and buttons and rotary encoders are connected according to the source code, connect the Arduino or RP2040 to the Android device using an OTG USB cable/hub. Android will ask you for USB port permission. Permanently grant this permission to the iRadioAndroid app. After restarting the app (with the peripherals previously connected), the iRadio can now also be operated from the outside.

The communication and all commands are specified on the one hand in the firmware of the external processor that provides the physical GPIO interface and on the other hand in the example code for the gpiod.


![gpiodcontrol](https://github.com/radiolab81/iRadioAndroid/blob/main/pics4www/gpiodcommands.jpg)


Your own changes and extensions can be made freely and easily and are always welcome on your own radio.
So using an OTG-USB cable is a two-way street! Just as data can be transported to the iRadioAndroid for control purposes, the iRadioAndroid can also send data to other radio peripherals. For example, numerical values ​​such as the field strength of the WiFi network can be transmitted over OTG-USB in order to create a simulation of historical display tubes (magic eyes).

![me1](https://github.com/radiolab81/iRadioAndroid/blob/main/pics4www/me1.jpg)  
![me2](https://github.com/radiolab81/iRadioAndroid/blob/main/pics4www/me2.jpg)

The sample code for a gpiod is available in the file gpiodSerialOTG_magiceye_support.java. The firmware for both tube types shown is in the RP2040 firmware folder. This application is by no means limited to displays of the GC9A01 type. Thanks to the library used in the firmware (https://github.com/moononournation/Arduino_GFX/tree/master/src/display), a large number of displays can already be addressed and used for a wide variety of outputs on secondary screens.

![devkit](https://github.com/radiolab81/iRadioAndroid/blob/main/pics4www/devkit.jpg)*A typical use case: the iRadioAndroid in a test environment for building an internet tube radio.*

Notice: USB devices have an identifier consisting of a manufacturer ID (vendor ID) and product ID.
For some GPIO interfaces, IDs should already be included in iRadioAndroid in the file https://github.com/radiolab81/iRadioAndroid/blob/main/app/src/main/res/xml/device_filter.xml.


![devicefilter](https://github.com/radiolab81/iRadioAndroid/blob/main/pics4www/devicefilter.jpg)

If the GPIO interface developed by your own has a different identifier, please enter this identifier in the device_filter file before compiling the project.

#### Simulation of a tuning noise

In order to simulate the behavior of a real radio even better, a service/process was introduced in iRadioAndroid that already exists for the iRadio for Raspberry, the noised service.

That means that a tuning sound can be played between switching of two Internet radio stations and during the connection and buffering period. Two prepared tuning sounds are included in the files noise.mp3 and tuning.wav, which are located in the res/raw folder of the app. Your own tuning sounds are also possible. To do this, copy your own file with tuning sounds into the res/raw folder. Please enter your filename in the noised source code at the position R.raw.my_file_with_noise (without file extension).

```
noisePlayer.setDataSource(getApplicationContext(), Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.tuning));
```

To enable the noised service, comment out the service startup in the iRadioStartup.java file.

The use of noised is not (!) tied to a scale simulation, but is particularly suitable for this!

In addition playback of an Internet radio program can be paused until the frequency dial reaches the new position. To do this, set WAIT_UNTIL_RADIO_DIAL_STOPS = false; in the file iRadioStartup.java to true .


#### Internet-TV support

With iRadioAndroid you can receive numerous TV channels in addition to Internet radio!
Within a radio scale simulation, switching between radio and TV picture can be done completely automatically! The streaming addresses of radio and television stations are saved in one playlist.

![radioandtv](https://github.com/radiolab81/iRadioAndroid/blob/main/pics4www/tvandradiosupport.jpg)*radio scale simulation - displaydRadioAndTV - with automatic radio/TV switching included as example code*

#### WebSDR / KiwiSDR Support

![websdr](https://github.com/radiolab81/iRadioAndroid/blob/main/pics4www/websdrsupport.jpg)*radio scale simulation with WebSDR/KiwiSDR control*

The iRadioAndroid offers support for controlling WebSDR and KiwiSDR. This means that the iRadioAndroid is ready to receive hundreds of SDR receivers worldwide. From time signal transmitters to Qatar-OSCAR 100.
The included demo scale simulation shows the integration of web-based sdr-receivers into your own application.

![websdr2](https://github.com/radiolab81/iRadioAndroid/blob/main/pics4www/kiwidb.jpg)*management/updating of KiwiSDR servers within scale simulation*

#### Broadcasting internet radio program to old radios

The iRadioAndroid can serve as a modulation source for old radios and relay Internet radio stations via RF.
Using the inet2RF service, the radio stations in the station list are mapped into a selectable RF range (if supported by the transmitter module). The change of the transmission frequency and the internet radio program to be broadcast is carried out by measuring the frequency of the local oscillator of the radio to be supplied, taking into account the intermediate frequency using a superheterodyne principle.

![inet2rf](https://github.com/radiolab81/iRadioAndroid/blob/main/pics4www/inet2rf.jpg)*typical setup for mapping a playlist in the medium wave range of a radio*

The Lo frequency measured on the radio, the transmitter frequency and modulation source are displayed in real time.

[![IMAGE ALT TEXT HERE](https://img.youtube.com/vi/wITcIP00m-0/0.jpg)](https://www.youtube.com/watch?v=wITcIP00m-0)

To start automatically the iRadioAndroid-App on your fully developed radio, set iRadioAndroid to the autostart of the Android OS.
