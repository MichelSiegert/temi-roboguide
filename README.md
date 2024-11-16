# Team Würfel Abgabe 1
Es gibt folgende Screens:

## Installation
Folgt einfach der Installation von https://github.com/robotemi/sdk.
   Alternativ nutzt es die Android SDK version 35.
Gradle version 8.10.1

## Kommentare
Einige funktionen werden beschrieben. 
Andere sind trivial oder offensichtlich.
LocationToggleManager.populateLocationToggles() ist hinreichend komplex, und wird bsp grob beschrieben.
Funktionen wie LocationToggleManager.resizeBitmap() werden nicht  erklärt, da die funktionalität offensichtlich ist.

## Screens/Bildschirme
- First_screenv
- individual_tour_planner
- tour_screen
- eval_screen

### First Screen
Hier wird eine Tour ausgewählt.
es gibt 4 Tours, welche geplant wurden.
Ebenfalls gibt es die Option eine Individuelle Tour zu stellen.

### Individual Tour Screen
Hier können Orte ausgewählt werden, welche angefahren werden können.
hier kann ebenfalls ausgewählt werden, ob die Stationen ausführlich oder nur normal beschrieben werden sollen.

### Tour Screen.
Hier werden die Bilder, Videos, Bewegung und Sprache des Temis gesteuert während der Tour.

### Eval Screen
Hier findet die Bewertung des Temis über das Anklicken eines der Drei Smileys statt.

## Voraussetzung der Daten
### Routing
1. Es gibt von jeden ort maximal 1 mal als Start location, und einmal als ziel location.
1. Es gibt Exakt einen Ort, der nur einmal als ziel Existiert.
1. Es gibt Exakt einen Ort, der nur einmal als Start existiert.

### Beispiel
* Als beschreibung haben wir 3 Orte, A, B, C, D.
* Ort A hat eine Route zu ort B.
* Ort B hat eine Route zu ort A.
* Wenn es eine Route von Ort B zu D geben *würde* wüsste der Temi nicht, was die Korrekte Route ist, um weiter zu fahren.
* Wenn es eine Route von D zu A geben *würde* wüsste der Temi nicht, ob nun B der Korrekte Startort wäre, oder A.

Deswegen muss eine Route einer Strengen Ordnung Unterliegen.

## Bilder und Videos
1. Jedes Bild das über das Internet erreichbar ist kann hier wiedergegeben werden.
1. Formate sollten möglichst dem Format 16/9 bzw. 4/3 entsprechen. Die bilder werden auf dieses Format gesetzt.
    * das bedeutet, dass bilder die Hochkant aufgenommen wurden ebenfalls auf dieses Format gebracht werden. Dies wird nicht gut aussehen, und ist dringlichst zu vermeiden.
    * Die exakte verwendete Größe ist 1066 länge x 600 höhe.
1. Youtube links müssen direkte Links sein.
    * Beispiel: https://www.youtube.com/watch?v=qQZIqPOvyx8 ist ein Valider link, da es mit ?v=... Endet.
    * https://www.youtube.com/embed/qQZIqPOvyx8?si=8PJ8pheunmEpRSKw ist ein valider Youtube link, aber die ID ist nicht so ersichtlich aus diesem Link.
    * https://www.youtube.com/watch?v=7gBSHCyHs_w&list=PLNfmGpK6ai91xBWnrCajXJDwUbGAhJ4Rx&index=1 Ist ebenfalls ein Valider link, aber hat aber eine Playlist noch zusätzlich im Link. Hier müsste &list= und alles was danach kommt gelöscht werden, um den Link abspielbar zumachen.
1. Das Erste bild Sollte das Gerät zeigen. Dies wird dann in der Erstellung der Individuellen Tour angezeigt.

## Anderes
1. Man muss davon ausgehen, dass der Temi stets mit dem Internet Verbunden ist.

### Verhalten:
* Es wird entweder der detailierte Text, oder der normale Texte wiedergegeben.
* Falls es keinen Text gibt, aber es beispielsweise einen Detailierten text zu der Station/Gegenstand/Transfer gibt, wird dieser angezeigt und abgespielt.
* Falls es weder einen Detailierten, noch einen Normalen Text zu einem Transfer gibt, wird schon einmal der Text des Ziels angezeigt.
* bevor der Temi anfängt zu sprechen, wartet er immer einige Sekunden, um sicherzugehen, dass alle Personen da sind, und sich das Ausstellungsstück anschauen konnten.
* Der Temi Erzählt zu einem Ort/Gegenstand solange dinge, bis er zu der Station nichts mehr zu erzählen hat.
* wenn er nichts mehr zu erzählen hat, informiert er die Gäste dass er gleich weiter fährt.
* Es gibt einen Button zum Pausieren einer Tour.
* Falls der Temi beim Sprechen unterbrochen wurde, fängt er erneut an zu erzählen.
* Falls zzt. ein Video abgespielt wird, geht der Temi erst weiter, wenn zzt. kein Video mehr läuft.
* Es gibt einen Button zum überspringen/ weitermachen der Tour.
* Es gibt einen Button zum Beenden der Tour.
* falls der Temi von einer Person auf einem Transfer blockiert wird, sagt er "Entschuldigung, Hier komme ich nicht durch".
* Falls der Temi zuvor noch gesprochen hat.
* Falls es keinen Ersichtlichen weg für den Temi gibt, dass ziel zu erreichen, gibt es einen Dialog. Hier kann beschlossen werden, ob die Station übersprungen wird, Die Station neu angefahren werden soll, oder die Tour beendet werden soll.
* Falls der Temi zurückgelassen wird während einer Tour, beim Bewerten, oder beim Erstellen einer Individuellen Tour, verlässt dieser nach einer Weile seinen Status und fährt zurück zur Ersten Station.
* im Individual tour screen werden die Stationen als bilder dargestellt wenn möglich.
* Wenn die Station nicht ausgewählt ist, ist das Bild ausgegraut.
* Sobald eine Location ausgewählt ist, wird sie in Farbe angezeigt.
* Falls kein Bild gefunden wird, ist es als Text auf einem Button dargestellt.
* Falls kein Bild gefunden wurde, und die Location ausgewählt wurde, wird der Hintergrund des Buttons Grün.


## Programm beschreibung
### Programm Ablauf:
![Programm ablauf Diagramm!](<Untitled Diagram.drawio.png>)