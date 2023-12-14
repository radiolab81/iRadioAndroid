# iRadioAndroid

Ein experimenteller Softwarebaukasten für den Aufbau neuer Radios oder dem Umbau alter Radios zu einem Internetradio auf Android Basis.

![sysoverview](https://github.com/BM45/iRadioAndroid/blob/main/pics4www/systemoverview.jpg)

#### Unterstützte Systeme

Zur Zeit werden Android-basierte Systeme (Smartphones und Tablet PCs) ab API Level 17 (A4.2 Jelly Bean) bis Android 12 getestet und unterstützt.
Eine Nutzung auf neueren Geräten scheint prinzipiell möglich zu sein, kann jedoch in Zukunft wegen API-Änderungen durch Google bestimmten Einschränkungen unterliegen.
Die Geräte müssen in der Regel nicht gerootet sein!

#### Systemdesign

Das Design des iRadioAndroid richtet sich an dem Baukastenprinzip des iRadio für Raspberry (https://github.com/BM45/iRadio) und dem iRadioMini für ESP32 (https://github.com/BM45/iRadioMini) aus.
Neben dem Medienplayer als Hintergrundprozess (iRadioPlayer) haben wir verschiedene "Prozesse" für die Visualisierung (displayd...) und die Steuerung über GPIOs (gpiod). Letztere werden über einen OTG-USB-Serial Port angebunden. Beispielfirmware zur Ansteuerung des iRadioAndroid von außen liegt im Ordner "firmware". 

![sysoverview](https://github.com/BM45/iRadioAndroid/blob/main/pics4www/folders.jpg)

Im Ordner "drawable" befinden sich alle für die als Beispiel hinzugefügten Skalensimulation genutzten Bildressourcen (PNGs mit Alphakanal). Änderungen und Hinzufügungen von Bilddateien sind analog der Beschreibung für iRadio-Skalensimulationen in Beitrag #127 hier https://radio-bastler.de/forum/showthread.php?tid=11484&pid=142892#pid142892 vorzunehmen.

Zentraler Startpunkt für die verschiedenen Dienste des iRadioAndroid ist die Datei iRadioStartup.java:

![startup](https://github.com/BM45/iRadioAndroid/blob/main/pics4www/startup.jpg)

Hier werden, wie in der Datei rc.local beim iRadio für Raspberry, sämtliche Komponenten (Hintergrunddienste, UI wie Skalensimulation) des iRadioAndroid ausgewählt und gestartet:

Auch wenn die Ansteuerung des iRadios über Tasten und Drehimpulsgeber die Integration in alte Radios vereinfacht, so ist diese Möglichkeit nur optional. Das iRadioAndroid kann wie jede andere App auch direkt über das Touch-Display des Systems bedient werden, die mitgelieferten displayd-Beispielcodes unterstützen diese Art der Bedienung.
Auch das iRadioAndroid versteht sich, wie alle anderen iRadio-Portierungen bisher auch, weniger als Fertiggericht, sondern als Richtschnur für eigenen Ideen und Entwicklungen. Die Zielgruppe ist der Technik- und PC-affine Radiobastler!

#### Installation

Zum Compilieren des iRadioAndroid wird zunächst das Android Studio von Google benötigt. https://developer.android.com/studio  
Die zur Zeit akutelle Version 2023.1.1 Hedgehog wird dafür von den Entwicklern empfohlen. Nach dem Download und der Installation des Android Studios wird das iRadioAndroid in das lokale Projekteverzeichnis kopiert. 

Aufruf von


`git clone https://github.com/BM45/iRadioAndroid/`


im Terminal aus dem Projekteverzeichnis heraus.

Das zukünftige Androidgerät muss nun in den Entwicklermodus (Developer Mode) geschaltet werden. howto: https://developer.android.com/studio/debug/dev-options#enable

Das USB Debugging (ggf. Debugging über WiFi) aktivieren und anschließend mit dem Entwicklungs-PC und dem Android Studio verbinden (pairen).  howto: https://developer.android.com/studio/run/device#connect

Direkt nach dem Compilieren der iRadio-Anwendung wird diese so auf dem Androidgerät installiert. Zusätzlich werden auch alle Debugausgaben vom Androidgerät im Android Studio (Logcat-Anzeige) angezeigt, was ein späteres Monitoring oder Fehlersuche erleichtert.

![logcat](https://developer.android.com/static/studio/images/debug/logcat_dolphin_2x.png)

Es wird aus Leistungsgründen empfohlen, sämtliche Entwicklungsarbeiten nicht am AndroidEmulator, sondern direkt mit einem richtigen Smartphone/Tablet vorzunehmen! Skalensimulationen müssen in der Regel sowieso an die Auflösung und Geometrie des genutzen Endgerätes angepasst werden!

![skalensim_cass](https://github.com/BM45/iRadioAndroid/blob/main/pics4www/skalensim.jpg)


Wichtig: Nach dem ersten Start vom iRadioAndroid wird noch eine kleine interne Senderliste verwendet!
Zunächst ist über Android-Einstellungen/Apps der iRadioAndroid-App der Zugriff auf den Gerätespeicher zu gewähren. Damit ist es möglich eine eigene Senderliste (playlist.m3u), analog dem iRadio/iRadioMini zu nutzen.
Standardmäßig wird diese Senderliste im Download-Ordner des Androidgerätes hinterlegt. 
Eine vorhandene playlist.m3u kann mit Hilfe der ADB-Shell vom PC aus in das Gerät kopiert werden (vergleichbar mit SSH-Zugang des iRadio auf Raspberry). 
Wenn das Androidgerät mit dem PC über USB-Kabel oder WiFi gekoppelt ist, kann man aus dem PC-Ordner wo die Senderliste hinterlegt ist, einen Transfer zum Smartphone/Tablet so einleiten:


`adb push playlist.m3u /sdcard/Download`


Mit dem Befehl adb shell kann man sich am Telefon einloggen und mit (Linux-)Befehlen wie ls, cd, cp, mv, reboot ... im Dateisystem des Telefons navigieren und kontrollieren ob die Senderliste vorhanden ist.
Ab diesem Moment wird iRadioAndroid bei jedem weiterem Start der App die selbst erstellte Senderliste verwenden. 

Über den Weg der ADB-Shell kann das iRadioAndroid zukünftig auch für neue WiFi-Zugänge konfiguriert werden, falls Ihr Radiogehäuse keinen direkten Zugang zu einem System-Touch-Screen mehr ermöglicht:


`adb shell cmd -w wifi connect-network "Home" wpa2 "qwerty"`


Diese Beispiel richtet das Androidgerät für das WiFi-Netz "Home" mit dem Passwort "qwerty" ein.

Für die Ansteuerung von iRadioAndroid über Drehimpulsgeber und Taster ist im Ordner <firmware> Beispielcode für die Arduino-Plattform mitgegeben. Um diese Art der Bedienung zu nutzen, benötigen Sie neben einen Arduino selbst, noch die Arduino-IDE https://www.arduino.cc/en/software , ein USB-Programmierkabel und ein USB-OTG-Kabel. Nachdem der Arduino-Mikrocontroller programmiert wurde und Taster, Drehimpulsgeber entsprechend des Quellcodes angeschlossen sind, verbinden Sie den Arduino noch über ein OTG-USB-Kabel/Hub mit dem Androidgerät. Android wird Sie nach der Berechtigung für den USB-Port fragen. Erteilen Sie diese Berechtigung der iRadioAndroid-App dauerhaft. Nach einem Neustart der App (bei vorher angeschlossener Peripherie) ist die Bedienung des iRadios nun auch von außen möglich.

Sollte in Ihrem fertig entwickeltem Radiogerät die iRadioAndroid-App nach dem Bootprozess von Android selbstständig starten können, so ist iRadioAndroid noch in den "Autostart" von Android zu setzen.


















