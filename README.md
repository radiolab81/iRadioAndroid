# iRadioAndroid  ( jetzt auch für Internet-TV und WebSDR/KiwiSDR )
(for english version click here: https://github.com/BM45/iRadioAndroid/blob/main/README.en.md)

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

Im Ordner "drawable" befinden sich alle genutzten Bildressourcen (PNGs mit Alphakanal), für die als Beispiel hinzugefügten Skalensimulation. 

Zentraler Startpunkt für die verschiedenen Dienste des iRadioAndroid ist die Datei iRadioStartup.java:

![startup](https://github.com/BM45/iRadioAndroid/blob/main/pics4www/startup.jpg)

Hier werden, wie in der Datei rc.local beim iRadio für Raspberry, sämtliche Komponenten (Hintergrunddienste, UI wie Skalensimulation) des iRadioAndroid ausgewählt und gestartet:

Auch wenn die Ansteuerung des iRadios über Tasten und Drehimpulsgeber die Integration in alte Radios vereinfacht, so ist diese Möglichkeit nur optional. Das iRadioAndroid kann wie jede andere App auch direkt über das Touch-Display des Systems bedient werden, die mitgelieferten displayd-Beispielcodes unterstützen diese Art der Bedienung.
Auch das iRadioAndroid versteht sich, wie alle anderen iRadio-Portierungen bisher auch, weniger als Fertiggericht, sondern als Richtschnur für eigene Ideen und Entwicklungen. Die Zielgruppe ist der Technik- und PC-affine Radiobastler!

#### Installation

Zum Compilieren des iRadioAndroid wird zunächst das Android Studio von Google benötigt. https://developer.android.com/studio  
Die zur Zeit akutelle Version 2023.1.1 Hedgehog wird dafür von den Entwicklern empfohlen. Nach dem Download und der Installation des Android Studios wird das iRadioAndroid in das lokale Projekteverzeichnis kopiert. 

Aufruf von


`git clone https://github.com/BM45/iRadioAndroid/`


im Terminal aus dem Projekteverzeichnis heraus.

Das zukünftige Androidgerät muss nun in den Entwicklermodus (Developer Mode) geschaltet werden. howto: https://developer.android.com/studio/debug/dev-options#enable

Das USB Debugging (ggf. Debugging über WiFi) aktivieren und anschließend mit dem Entwicklungs-PC und dem Android Studio verbinden (pairen).  howto: https://developer.android.com/studio/run/device#connect

Direkt nach dem Compilieren der iRadio-Anwendung wird diese auch auf dem am PC angeschlossenen Androidgerät installiert. Zusätzlich werden auch alle Debugausgaben vom Androidgerät im Android Studio (Logcat-Anzeige) angezeigt, was ein späteres Monitoring oder Fehlersuche bei Problemen erleichtert.

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


Dieses Beispiel richtet das Androidgerät für das WiFi-Netz "Home" mit dem Passwort "qwerty" ein.

Für die Ansteuerung von iRadioAndroid über Drehimpulsgeber und Taster ist im Ordner "firmware" Beispielcode für die Arduino und RP2040 (Raspberry Pico) Plattform mitgegeben. Um diese Art der Bedienung zu nutzen, benötigen Sie neben einen Arduino oder RP2040 selbst, noch die Arduino-IDE https://www.arduino.cc/en/software , ein USB-Programmierkabel und ein USB-OTG-Kabel. Nachdem der Mikrocontroller programmiert wurde und Taster, Drehimpulsgeber entsprechend des Quellcodes angeschlossen sind, verbinden Sie den Arduino oder RP2040 noch über ein OTG-USB-Kabel/Hub mit dem Androidgerät. Android wird Sie nach der Berechtigung für den USB-Port fragen. Erteilen Sie diese Berechtigung der iRadioAndroid-App dauerhaft. Nach einem Neustart der App (bei vorher angeschlossener Peripherie) ist die Bedienung des iRadios nun auch von außen möglich.

Die Kommunikation und alle Befehle werden auf der einen Seite in der Firmware des extenern Prozessors, der die physikalische GPIO Schnittstelle bereitstellt, und auf der anderen Seite im Beispielcode für den gpiod festgelegt.

![gpiodcontrol](https://github.com/BM45/iRadioAndroid/blob/main/pics4www/gpiodcommands.jpg)

Hier sind eigene Änderungen und Erweiterungen beliebig und leicht möglich und im eigenen Radio jederzeit willkommen. So ist der Weg über ein OTG-USB Kabel keine Einbahnstraße! So wie Daten zum iRadioAndroid für Ansteuerungszwecke transportiert werden können, kann das iRadioAndroid auch Daten an weitere Radio-Peripherie darüber verschicken. Zum Beispiel können numerische Werte, wie die Empfangsfeldstärke des WiFi-Netzes übertragen werden, um damit eine Simulation von historischen Anzeigeröhren (Magische Augen) zu verwirklichen.

![me1](https://github.com/BM45/iRadioAndroid/blob/main/pics4www/me1.jpg)  
![me2](https://github.com/BM45/iRadioAndroid/blob/main/pics4www/me2.jpg)

Der Beispielcode eines gpiod liegt in der Datei gpiodSerialOTG_magiceye_support.java vor. Die Firmware für beide abgebildete Röhrentypen im RP2040-Firmware Ordner. 
Diese Anwendung beschränkt sich dabei keineswegs auf Displays vom GC9A01-Typ. Durch die in der Firmware verwendete Bibliothek (https://github.com/moononournation/Arduino_GFX/tree/master/src/display) lässt sich bereits jetzt ein großer Teil von Displays ansprechen und für verschiedenste Ausgaben auf Sekundärbildschirmen benutzen.

![devkit](https://github.com/BM45/iRadioAndroid/blob/main/pics4www/devkit.jpg)*Ein typischer Anwendungsfall: das iRadioAndroid in einer Testumgebung für den Aufbau eines Internetröhrenradios.*

Hinweis: 
USB Geräte besitzen eine Kennung bestehend aus Hersteller-ID (Vendor-ID) und Product-ID mit denen sie sich am System anmelden.
Für einige GPIO-Interfaces sind sollte Kennungen bereits im iRadioAndroid in der Datei https://github.com/BM45/iRadioAndroid/blob/main/app/src/main/res/xml/device_filter.xml eingepflegt. 

![devicefilter](https://github.com/BM45/iRadioAndroid/blob/main/pics4www/devicefilter.jpg)

Trägt Ihr entwickeltes GPIO-Interface eine davon abweichende Kennung, tragen Sie diese Kennung bitte vor dem Compilieren des Projektes in die Datei device_filter ein.

#### Simulation eines Abstimmgeräusches
Um das Verhalten eines echten Radios noch besser zu simulieren, wurde ein Service/Prozess im iRadioAndroid eingeführt, den es bereits für das iRadio für Raspberry gibt, den noised - Service. 

Damit wird zwischen dem Umschalten zweier Internetradiosender, sowie im Verbindungs- und Pufferungszeitraum, ein Abstimmgeräusch eingespielt.
Es werden zwei vorbereitete Abstimmgeräusche in den Dateien noise.mp3 und tuning.wav mitgeliefert, diese liegen im res/raw Ordner der App. Eigene Abstimmgeräusche sind ebenso möglich. Kopieren Sie dazu 
ihre eigene Datei mit Abstimmgeräuschen in den res/raw Ordner. Ihre Datei tragen Sie bitte im noised Quellcode an der Stelle R.raw.meine_Datei_mit_Abstimmgeräusch (ohne Dateiendung) ein.

```
noisePlayer.setDataSource(getApplicationContext(), Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.tuning));
```

Um den noised-Service zu aktivieren, kommentieren Sie den Start des Service in der Datei iRadioStartup.java aus.

Die Verwendung des noised ist nicht(!) an eine Skalensimulation gebunden, eignet sich für diese aber besonders gut!

Zusätzlich kann bei Skalensimulationen die Wiedergabe eines Internetradioprogramms bis zum Erreichen der neuen Position des Skalenzeigers pausiert werden. Setzen Sie dazu  WAIT_UNTIL_RADIO_DIAL_STOPS = false; in der Datei iRadioStartup.java auf true .


#### Internet-TV Support

Mit dem iRadioAndroid kann man neben Internetradio auch zahlreiche TV-Sender empfangen!
Innerhalb einer Skalensimulation kann die Umschaltung zwischen Radioansicht und TV-Bild vollständig automatisch erfolgen! Die Streamingadressen von Radio und Fernsehsendern können in einer gemeinsamen Playlist gespeichert werden. 

![radioandtv](https://github.com/BM45/iRadioAndroid/blob/main/pics4www/tvandradiosupport.jpg)*Skalensimulation - displaydRadioAndTV - mit automatischer Radio/TV-Umschaltung als Beispielcode enthalten*

#### WebSDR / KiwiSDR Support

![websdr](https://github.com/BM45/iRadioAndroid/blob/main/pics4www/websdrsupport.jpg)*Skalensimulation - mit WebSDR/KiwiSDR Ansteuerung (als Beispielcode enthalten)*

Das iRadioAndroid bietet Unterstützung zur Ansteuerung von WebSDR und KiwiSDR. Damit ist das iRadioAndroid empfangsbereit für weltweit hunderte SDR-Empfänger. Vom Zeitzeichensender bis zu Qatar-OSCAR 100. 
Die mitgelieferte Demoskalensimualtion zeigt die Einbindung der entfernten Empfänger in eine eigene Anwendung.

![websdr2](https://github.com/BM45/iRadioAndroid/blob/main/pics4www/kiwidb.jpg)*Demo: Verwaltung/Aktualisierung von KiwiSDR Servern innerhalb der Skalensimulation*

#### Aussendung des Internetradioprogramms an alte Radios

Das iRadioAndroid kann als Modulationsquelle für alte Radios dienen und Internetradiosender über HF erneut ausspielen.
Über den Service inet2RF werden die Radiostationen der Senderliste (Playlist.m3u) in einen wählbaren HF-Bereich (sofern vom Sendemodul unterstützt) abgebildet. Die Umschaltung der Sendefrequenz und des zu sendenden Internetradioprogramms erfolgt durch Messung der Frequenz des Lokaloszillators des zu versorgenden Radios unter Berücksichtigung der Zwischenfrequenz bei einem Superhetprinzip.

![inet2rf](https://github.com/BM45/iRadioAndroid/blob/main/pics4www/inet2rf.jpg)*typisches Setup für das Mapping einer Playlist in den Mittelwellenbereich eines Radios*

Die Darstellung der am Radio gemessenen Lo-Frequenz, der aktuell eingestellten Senderfrequenz und Modulationsquelle erfolgen in Echtzeit. 

[![IMAGE ALT TEXT HERE](https://img.youtube.com/vi/wITcIP00m-0/0.jpg)](https://www.youtube.com/watch?v=wITcIP00m-0)

Hinweis: Sollte in Ihrem fertig entwickeltem Radiogerät die iRadioAndroid-App nach dem Bootprozess von Android selbstständig starten können, so ist iRadioAndroid noch in den "Autostart" von Android zu setzen.
