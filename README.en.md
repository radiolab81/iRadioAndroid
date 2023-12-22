# iRadioAndroid

An experimental software kit for building new radios or converting old radios into an Android-based internet radio.

![sysoverview](https://github.com/BM45/iRadioAndroid/blob/main/pics4www/systemoverview.jpg)

#### Supported systems

Android-based systems (smartphones and tablet PCs) from API Level 17 (A4.2 Jelly Bean) to Android 12 are currently tested and supported.
Usage on newer devices seems to be possible in principle, but may be subject to certain restrictions in the future due to API changes by Google.
The devices usually do not need to be rooted!

#### System design

The design of the iRadioAndroid is based on the modular principle of the iRadio for Raspberry (https://github.com/BM45/iRadio) and the iRadioMini for ESP32 (https://github.com/BM45/iRadioMini).
In addition to the media player as a background process (iRadioPlayer), we have various "processes" for visualization (displayd...) and controls via GPIOs (gpiod) which are connected via an OTG USB serial port. Sample firmware for controlling the iRadioAndroid from outside is in the “firmware” folder.


![sysoverview](https://github.com/BM45/iRadioAndroid/blob/main/pics4www/folders.jpg)

The "drawable" folder contains all used image resources (PNGs with alpha channel) for the radio scale simulation added as an example. Changes and additions to image files should be made analogously to the description for iRadio scale simulations in post #127 here https://radio-bastler.de/forum/showthread.php?tid=11484&pid=142892#pid142892.

The central starting point for the various iRadioAndroid services is the iRadioStartup.java file:


![startup](https://github.com/BM45/iRadioAndroid/blob/main/pics4www/startup.jpg)

Here, as in the rc.local file for the iRadio for Raspberry, all components (background services, UI such as radio scale simulation) of the iRadioAndroid are selected and started:

Even if controlling the iRadio via buttons and rotary encoders simplifies integration into old radios, this option is only optional. Like any other app, the iRadioAndroid can be operated directly via the system's touch display, the included displayd example codes support this type of operation.
The iRadioAndroid, like all other iRadio ports, sees itself less as a ready-to-use solution, but rather as a guideline for your own ideas and developments. The target group is the technology and PC-savvy radio hobbyist!



#### Installation

To compile iRadioAndroid you first need Google's Android Studio. https://developer.android.com/studio
The current version 2023.1.1 Hedgehog is recommended by the developers. After downloading and installing the Android Studio, the iRadioAndroid is copied to the local projects directory.

Calling


`git clone https://github.com/BM45/iRadioAndroid/`


from terminal / projects-folder will do the job.

The future Android radio device must now be switched to developer mode. howto: https://developer.android.com/studio/debug/dev-options#enable

Activate USB debugging (if necessary debugging via WiFi) and then connecting (pairing) to the development PC and the Android Studio. howto: https://developer.android.com/studio/run/device#connect

Immediately after compiling the iRadio application, it is also installed on the Android device connected to the PC. In addition, all debug output from the Android device is displayed in Android Studio (Logcat display), which makes later monitoring or troubleshooting easier in the event of problems.
![logcat](https://developer.android.com/static/studio/images/debug/logcat_dolphin_2x.png)

For performance reasons, it is recommended that all development work not be carried out on the Android Emulator, but directly with a real smartphone/tablet! Scale simulations usually have to be adapted to the resolution and geometry of the device used anyway!

![skalensim_cass](https://github.com/BM45/iRadioAndroid/blob/main/pics4www/skalensim.jpg)


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


![gpiodcontrol](https://github.com/BM45/iRadioAndroid/blob/main/pics4www/gpiodcommands.jpg)


Your own changes and extensions can be made freely and easily and are always welcome on your own radio.
So using an OTG-USB cable is a two-way street! Just as data can be transported to the iRadioAndroid for control purposes, the iRadioAndroid can also send data to other radio peripherals. For example, numerical values ​​such as the field strength of the WiFi network can be transmitted over OTG-USB in order to create a simulation of historical display tubes (magic eyes).

![me1](https://github.com/BM45/iRadioAndroid/blob/main/pics4www/me1.jpg)  
![me2](https://github.com/BM45/iRadioAndroid/blob/main/pics4www/me2.jpg)

The sample code for a gpiod is available in the file gpiodSerialOTG_magiceye_support.java. The firmware for both tube types shown is in the RP2040 firmware folder. This application is by no means limited to displays of the GC9A01 type. Thanks to the library used in the firmware (https://github.com/moononournation/Arduino_GFX/tree/master/src/display), a large number of displays can already be addressed and used for a wide variety of outputs on secondary screens.

![devkit](https://github.com/BM45/iRadioAndroid/blob/main/pics4www/devkit.jpg)*A typical use case: the iRadioAndroid in a test environment for building an internet tube radio.*

Notice: USB devices have an identifier consisting of a manufacturer ID (vendor ID) and product ID.
For some GPIO interfaces, IDs should already be included in iRadioAndroid in the file https://github.com/BM45/iRadioAndroid/blob/main/app/src/main/res/xml/device_filter.xml.


![devicefilter](https://github.com/BM45/iRadioAndroid/blob/main/pics4www/devicefilter.jpg)

If the GPIO interface developed by your own has a different identifier, please enter this identifier in the device_filter file before compiling the project.


To start automatically the iRadioAndroid-App on your fully developed radio, set iRadioAndroid to the autostart of the Android os.
