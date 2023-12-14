# iRadioAndroid

Ein experimenteller Softwarebaukasten für den Aufbau neuer Radios oder dem Umbau alter Radios zu einem Internetradio auf Android Basis.

![sysoverview](https://github.com/BM45/iRadioAndroid/blob/main/pics4www/systemoverview.jpg)

#### unterstützte Systeme

Zur Zeit werden Android-basierten Systeme (Smartphones und Tablet PCs) ab API Level 17 (A4.2 Jelly Bean) bis Android 12 getestet und unterstützt.
Eine Anwendung auf neueren Geräten scheint prizipiell möglich zu sein, kann jedoch in Zukunft wegen Änderungen an den APIs durch Google bestimmten Einschränkungen unterliegen.
Die Geräte müssen in der Regel nicht gerootet sein!

#### Systemdesign

Das Design des iRadioAndroid richtet sich an dem Baukastenprinzip des iRadio für Raspberry (https://github.com/BM45/iRadio) und dem iRadioMini für ESP32 (https://github.com/BM45/iRadioMini) aus.
Neben dem Medienplayer als Hintergrundprozess haben wir "Prozesse" für die Visualisierung (displayd) und die Steuerung über GPIOs (gpiod). Letztere werden über einen OTG-USB-Serial Port angebunden. Beispielfirmware zur Ansteuerung des iRadioAndroid von Außen liegt im Ordner "firmware" vor.






