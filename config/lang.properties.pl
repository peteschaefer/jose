
##############################################################################
#	Polish translation for jose 1.3.4 RC 1 by Tomasz Sokół (progtom@wp.pl)
##############################################################################

#	Application Name
application.name	= jose

#	Frame Titles
window.board	= Szachownica
window.console	= Konsola silnika
window.database	= Baza danych
window.filter	= Filtr
window.list	= Lista posunięć
window.clock	= Zegar
window.game	= Gra
window.engine	= Silnik
window.eval     = Szacowanie profilu

window.collectionlist	= Baza danych
window.query		= Znajdź
window.gamelist		= Baza danych

window.sqlquery		= Zapytanie SQL
window.sqllist		= Wynik

window.toolbar.1	= Pasek narzędzi 1
window.toolbar.2	= Pasek narzędzi 2
window.toolbar.3	= Pasek narzędzi 3
window.toolbar.symbols	= Przypisy
window.help		        = Pomoc
window.print.preview    = Podgląd wydruku

# dialog titles

dialog.option	= Opcje
dialog.about	= Informacje
dialog.animate  = Animacja
dialog.setup	= Ustawienia
dialog.message.title = Wiadomość

# number formats:
format.byte		= ###0.# 'B'
format.kilobyte		= ###0.# 'kB'
format.megabyte		= ###0.# 'MB'


##############################################################################
# 	Menus
##############################################################################

# File Menu

menu.file		= Plik
menu.file.new		= Nowy
menu.file.new.tip	= Rozpocznij nową grę
menu.file.new.frc = Nowy FRC
menu.file.open		= Otwórz...
menu.file.open.tip  	= Otwórz plik PGN
menu.file.open.url	= Otwórz URL...
menu.file.close		= Zamknij
menu.file.close.tip	= Zamknij bieżące okno
menu.file.save		= Zapisz
menu.file.save.tip  	= Zapisz bieżącą grę w bazie danych
menu.file.save.as    	= Zapisz jako...
menu.file.save.as.tip	= Zapisz bieżącą grę w nowej kopii w bazie danych
menu.file.save.all   	= Zapisz wszystko
menu.file.save.all.tip  = Zapisz wszystkie otwarte gry w bazie danych
menu.file.revert	= Przywróć grę
menu.file.print		= Drukuj...
menu.file.print.tip	= Drukuj bieżącą grę
menu.file.print.setup	= Ustawienia strony...
menu.file.print.setup.tip = Ustawienia drukarki i rozmiar papieru
menu.file.print.preview = Podgląd wydruku...
menu.file.quit		= Koniec
menu.file.quit.tip	= Zamknij jose

# Edit Menu

menu.edit		= Edycja
menu.edit.undo		= Cofnij (%action%)
menu.edit.cant.undo 	= Cofnij
menu.edit.redo		= Ponów (%action%)
menu.edit.cant.redo 	= Ponów
menu.edit.select.all    = Zaznacz wszystko
menu.edit.select.none   = Odznacz wszytko
menu.edit.cut		= Wytnij
menu.edit.copy		= Kopiuj
menu.edit.copy.fen  = Zapis FEN
menu.edit.copy.fen.tip = Kopiuj bieżącą pozycję do schowka (w notacji FEN)
menu.edit.copy.img  = Diagram graficzny i tło
menu.edit.copy.img.tip = Kopiuj bieżącą pozycję do schowka (jako obraz)
menu.edit.copy.imgt  = Diagram graficzny
menu.edit.copy.imgt.tip = Kopiuj bieżącą pozycję do schowka (jako obraz)
menu.edit.copy.text  = Diagram tekstowy
menu.edit.copy.text.tip = Kopiuj bieżącą pozycję do schowka (jako tekst)

menu.edit.copy.pgn  = Kopiuj PGN
menu.edit.copy.pgn.tip = Kopiuj bieżącą grę do schowka (jako tekst PGN)
menu.edit.paste		= Wklej
menu.edit.paste.tip = Wklej ze schowka
menu.edit.paste.copy		= Wklej kopie
menu.edit.paste.copy.tip 	= Wklej gry ze schowka
menu.edit.paste.same 	= Wklej gry
menu.edit.paste.same.tip = Przenieś gry ze schowka
menu.edit.paste.pgn = Wklej PGN
menu.edit.paste.pgn.tip = Wstaw grę ze schowka (jako tekst PGN)
menu.edit.clear		= Wyczyść
menu.edit.option	= Opcje...
menu.edit.option.tip	= Otwórz okno dialogowe opcji

menu.edit.games			= Baza danych
menu.edit.collection.new 	= Nowy folder
menu.edit.collection.rename 	= Zmień nazwę
menu.edit.collection.crunch = Dostosuj
menu.edit.collection.crunch.tip = Dostosuj kolumnę indeksu
menu.edit.empty.trash		= Wyczyść kosz
menu.edit.restore		= Przywróć

#menu.edit.position.index    = Uaktualnij listę posunieć
menu.edit.search.current    = Szukaj pozycji
menu.edit.ecofy             = Klasyfikuj ECO

menu.edit.style = Styl tekstu
menu.edit.bold = Pogrubiony
menu.edit.italic = Kursywa
menu.edit.underline = Podkreślony
menu.edit.plain = Normalny
menu.edit.left = Wyrównany do lewej
menu.edit.center = wyśrodkowany
menu.edit.right = Wyrównany do prawej
menu.edit.larger = Zwiększ rozmiar czcionki
menu.edit.smaller = Zmniejsz rozmiar czcionki
menu.edit.color = Kolor czcionki

# Game Menu

menu.game		= Gra
menu.game.details	= Szczegóły...
menu.game.analysis  	= Tryb analizy
menu.game.navigate	= Przejdź do...
menu.game.time.controls = Partia
menu.game.time.control = Partia
menu.game.details.tip 	= Edytuj szczegóły gry (gracze, itd.)
menu.game.hint		= Podpowiedz
menu.game.hint.tip  = Pokaż podpowiedź
menu.game.draw		= Przyjmij ruch
menu.game.resign	= Poddaję
menu.game.2d		= Widok 2D
menu.game.3d		= Widok 3D
menu.game.flip		= Obróć szachownicę
menu.game.coords	= Współrzędne
menu.game.coords.tip	= Pokazuj współrzędne
menu.game.animate 	= Animacja...
menu.game.previous 	= Poprzednia zakładka
menu.game.next 		= Następna zakładka
menu.game.close 	= Zamknij
menu.game.close.tip 	= Zamknij bieżącą grę
menu.game.close.all 	= Zamknij wszystkie
menu.game.close.all.tip = Zamknij wszystkie otwarte gry
menu.game.close.all.but = Zamknij wszystkie oprócz bieżącej
menu.game.close.all.but.tip = Zamknij wszystkie otwarte gry oprócz bieżącej
menu.game.setup		= Ustaw pozycję

menu.game.copy.line = Kopiuj linię
menu.game.copy.line.tip = Kopiuj tę linię do schowka
menu.game.paste.line = Wklej linię
menu.game.paste.line.tip = Wstaw tę linię do bieżącej gry

# Window Menu

menu.window		= Okno
menu.window.fullscreen 	= Widok pełnoekranowy
menu.window.reset   	= Odśwież okno

# Help Menu

menu.help		= Pomoc
menu.help.splash	= O jose...
menu.help.about		= Informacje...
menu.help.license	= Licencja...
menu.help.context   	= Pomoc kontekstowa
menu.help.manual    	= Podręcznik
menu.help.web		= jose w Internecie

menu.web.home		= Strona domowa
menu.web.update		= Aktualizacja online
menu.web.download	= Pobierz
menu.web.report		= Zgłoś błąd
menu.web.support	= Prośba o pomoc
menu.web.feature	= Prośba o przedstawienie
menu.web.forum		= Forum
menu.web.donate     = Wesprzyj
menu.web.browser	= Wybierz przeglądarkę...


##############################################################################
# 	Context Menu
##############################################################################

panel.hide		= Ukryj
panel.hide.tip		= Ukryj ten panel
panel.undock		= Nowe okno
panel.undock.tip	= Otwórz ten panel w osobnym oknie
panel.move		= Przesuń
panel.move.tip		= Przesuń ten panel w nowe położenie
panel.dock		= Dokuj
panel.dock.tip		= Dokuj to okno

panel.orig.pos = Pierwotne położenie
panel.dock.here = Dokuj tutaj
panel.undock.here = Oddokuj

#################
# Document Panel
#################

# deprecated:
tab.place 	= Umiejscowienie zakładek
tab.place.top 	= U góry
tab.place.left 	= Po lewej
tab.place.bottom = Na dole
tab.place.right = Po prawej
#

tab.layout 		= Układ zakładek
tab.layout.wrap 	= Piętra
tab.layout.scroll 	= Szereg

doc.menu.annotate 	= Przypisy
doc.menu.delete.comment = Usuń komentarz
doc.menu.line.promote 	= Awansuj linię
doc.menu.line.delete 	= Usuń linię
doc.menu.line.cut 	= Odetnij linię
doc.menu.line.uncomment = Usuń wszystkie komentarze
doc.menu.remove.annotation = -None-
doc.menu.more.annotations = Więcej...

tab.untitled 	= Bez tytułu
confirm 	= Potwierdzenie
confirm.save.one = Czy zapisać bieżącą grę?
confirm.save.all = Czy zapisać zmodyfikowane gry?

dialog.confirm.save = Zapisz
dialog.confirm.dont.save = Nie zapisuj

dialog.engine.offers.draw = %engine% proponuje ruch.

dialog.accept.draw = Akceptuj ruch
dialog.decline.draw = Odrzuć ruch

dialog.autoimport.title = Importuj
dialog.autoimport.ask = Plik ^0 zmienił się na dysku. \n Czy ponownie go otworzyć?

dialog.paste.message = Zamierzasz wstawić dane ze schowka. \n\
     Czy chcesz przesunąć gry, czy utworzyć nową kopię?
dialog.paste.title = Wklej gry
dialog.paste.same = Przesuń
dialog.paste.copy = Kopiuj

###################
# Game Navigation
###################

move.first	= Początek gry
move.backward 	= Wstecz
move.delete 	= Cofnij ostatni ruch
engine.stop 	= Pauza
move.start 	= Wykonaj ruch
move.forward 	= Do przodu
move.last 	= Ostatni ruch
move.animate	 = Animacja


##################################
# Engine Panel
##################################

engine.paused.tip 	= %engine% zatrzymany
engine.thinking.tip 	= %engine% myśli nad następnym ruchem
engine.pondering.tip 	= %engine% rozważa twój następny ruch
engine.analyzing.tip 	= %engine% analizuje
engine.hint.tip 	= Podpowiedź: %move%

engine.paused.title 	= %engine%
engine.thinking.title 	= %engine% myśli
engine.pondering.title 	= %engine% rozważa
engine.analyzing.title 	= %engine% analizuje

plugin.name 		= %name% %version%
plugin.name.author 	= %name% %version%. Autor %author%

plugin.book.move 	= BK
plugin.book.move.tip 	= Zbiór ruchów
plugin.hash.move 	= HT
plugin.hash.move.tip 	= Szacowany ze zbioru różnych pozycji
plugin.tb.move 		= TB
plugin.tb.move.tip 	= Szacowany ze zbioru końcówek

plugin.currentmove.title       = Ruch
plugin.depth.title      = Głębokość
plugin.elapsed.time.title  = Czas
plugin.nodecount.title  = Warianty
plugin.nps.title        = W/sek.

plugin.currentmove = %move%
plugin.currentmove.max = %move% %moveno%/%maxmove%

plugin.currentmove.tip = Aktualnie szacowany ruch %move%.
plugin.currentmove.max.tip = Aktualnie szacowany ruch %move%. (nr %moveno% z %maxmove%)

plugin.depth 		= %depth%
plugin.depth.tip 	= Głębokość szukania: Warstwa %depth%.

plugin.depth.sel 	= %depth% (%seldepth%)
plugin.depth.sel.tip 	= Głębokość szukania: %depth%. warstwa. Głębokość wybiórcza: %seldepth%. warstwa

plugin.white.mates 	= +#%eval%
plugin.white.mates.tip 	= Białe matują w %eval% ruchach
plugin.black.mates 	= -#%eval%
plugin.black.mates.tip 	= Czarne matują w %eval% ruchach

plugin.evaluation 	= %eval%
plugin.evaluation.tip 	= Wartość pozycji: %eval%

plugin.line.tip = Szacowanie zakresu

plugin.elapsed.time = %time%
plugin.elapsed.time.tip = Upływ czasu dla tego obliczenia.

plugin.nodecount 	= %nodecount%
plugin.nodecount.tip 	= %nodecount% oszacowanych pozycji

plugin.nps      = %nps%
plugin.nps.tip  = %nps% szacowanych wariantów na sekundę

plugin.pv.history = Tryb eksperta

restart.plugin		= Ponownie uruchom silnik


######################
# Board Panel
######################

wait.3d = Ładowanie 3D. Proszę czekać...

message.result			= Wynik
message.white 			= Białe
message.black 			= Czarne
message.mate 			= Mat. \n %player% wygrał.
message.stalemate		= Pat. \n Remis.
message.draw3			= Pozycja powtórzona 3 razy. \n Remis.
message.draw50			= Bez zmiany w bierkach przez 50 ruchów. \n Remis.
message.resign			= %player% poddał. \n Wygrałeś.
message.time.draw		= Upłynął czas. \n Remis.
message.time.lose		= Upłynął czas. \n %player% wygrał.


################
# Clock Panel
################

clock.mode.analog	= Analogowy
clock.mode.analog.tip 	= Pokaż zegar analogowy
clock.mode.digital	= Cyfrowy
clock.mode.digital.tip 	= Pokaż zegar cyfrowy


##############################################################################
#	Dialogs
##############################################################################

dialog.button.ok		= OK
dialog.button.ok.tip		= Uaktywnij wprowadzone zmiany
dialog.button.cancel		= Anuluj
dialog.button.cancel.tip	= Zamknij okno dialogowe bez uaktywniania wprowadzonych zmian
dialog.button.apply		= Zatwierdź
dialog.button.apply.tip		= Natychmiast zatwierdź wprowadzone zmiany
dialog.button.revert		= Przywróć
dialog.button.revert.tip	= Przywróć poprzednie ustawienia
dialog.button.clear		= Wyczyść
dialog.button.delete		= Usuń
dialog.button.yes		= Tak
dialog.button.no		= Nie
dialog.button.next		= Dalej
dialog.button.back		= Wstecz
dialog.button.close		= Zamknij
dialog.button.help		= Pomoc
dialog.button.help.tip		= Pokaż tematy pomocy

dialog.button.commit		= Przyjmij
dialog.button.commit.tip	= Naciśnij tutaj, aby przyjąć uaktualnienieClick here to commit updates
dialog.button.rollback		= Wycofaj
dialog.button.rollback.tip	= Naciśnij tutaj, aby odrzucić uaktualnienie

dialog.error.title		= Błąd

###################################
#  File Chooser Dialog
###################################

filechooser.pgn			= Portable Game Notation (*.pgn,*.zip)
filechooser.epd         = EPD albo FEN (*.epd,*.fen)
filechooser.db 			= Archiwum jose (*.jose)
filechooser.db.Games 		= Archiwum gry jose (*.jose)
filechooser.db.Games.MySQL 	= Archiwum gry jose (Szybki zapis) (*.jose)
filechooser.txt 		= Plik tekstowy (*.txt)
filechooser.html 		= Pliki internetowe (*.html)
filechooser.pdf 		= Acrobat Reader (*.pdf)
filechooser.exe         = Pliki wykonywalne
filechooser.img         = Pliki graficzne (*.gif,*.jpg,*.png,*.bmp)

filechooser.overwrite 	= Nadpisać istniejący "%file.name%"?
filechooser.do.overwrite = Nadpisz

#################
# Color Chooser
#################

colorchooser.texture	= Tekstura
colorchooser.preview	= Podgląd
colorchooser.gradient   = Gradient
colorchooser.gradient.color1 = Pierwszy kolor
colorchooser.gradient.color2 = Drugi kolor
colorchooser.gradient.cyclic = Cyklicznie

colorchooser.texture.mnemonic = T
colorchooser.gradient.mnemonic = G

animation.slider.fast   = Szybko
animation.slider.slow   = Powoli

##############################################################################
# Option dialog
##############################################################################

# Tab Titles

dialog.option.tab.1	= Gracz
dialog.option.tab.2	= Szachownica
dialog.option.tab.3	= Kolory
dialog.option.tab.4	= Partia
dialog.option.tab.5     = Silnik
dialog.option.tab.6 = Opening Book
# TODO
dialog.option.tab.7     = 3D
dialog.option.tab.8	= Czcionki

# User settings

dialog.option.user.name		= Nazwisko
dialog.option.user.language	= Język
dialog.option.ui.look.and.feel	= Interfejs

doc.load.history	= Ładuj ostatnio otwarte gry
doc.classify.eco	= Klasyfikuj otwieranie przez ECO

dialog.option.animation = Animacja
dialog.option.animation.speed = Szybkość

dialog.option.doc.write.mode	= Wstaw nowy ruch
write.mode.new.line		= Nowa linia
write.mode.new.main.line	= Nowa główna linia
write.mode.overwrite		= Nadpisz
write.mode.ask			= Pytaj
write.mode.dont.ask		= Nie pytaj więcej
write.mode.cancel		= Anuluj

board.animation.hints   = Pokaż drogę podczas animacji

dialog.option.sound = Mowa
dialog.option.sound.moves.dir = Mowa przy ruchu:
sound.moves.engine  = Wypowiadaj ruch silnika
sound.moves.ack.user = Potwierdź ruch gracza
sound.moves.user = Wypowiadaj ruch gracza

# Fonts

dialog.option.font.diagram	= Diagram
dialog.option.font.text		= Tekst
dialog.option.font.inline	= Diagram tekstowy
dialog.option.font.figurine	= Bierki
dialog.option.font.symbol	= Symbol
dialog.option.font.size     	= Rozmiar
figurine.usefont.true 		= Figury graficznie
figurine.usefont.false 		= Figury literowo

doc.panel.antialias		= Wygładzanie czcionek

# Notation

dialog.option.doc.move.format	= Notacja:
move.format			= Notacja
move.format.short		= Krótka
move.format.long		= Długa
move.format.algebraic		= Algebraiczna
move.format.correspondence	= Korespondencyjna
move.format.english		= Angielska
move.format.telegraphic		= Telegraficzna

# Colors

dialog.option.board.surface.light	= Białe pola
dialog.option.board.surface.dark	= Czarne pola
dialog.option.board.surface.white	= Białe bierki
dialog.option.board.surface.black	= Czarne bierki

dialog.option.board.surface.background	= Tło
dialog.option.board.surface.frame	= Obramowanie
dialog.option.board.surface.coords	= Współrzędne

dialog.option.board.3d.model            = Model:
dialog.option.board.3d.clock            = Zegar
dialog.option.board.3d.surface.frame	= Obramowanie:
dialog.option.board.3d.light.ambient	= Światło otaczające:
dialog.option.board.3d.light.directional = Światło bezpośrednie:
dialog.option.board.3d.knight.angle     = Skoczki:

board.surface.light	= Biąłe pola
board.surface.dark	= Czarne pola
board.surface.white	= Białe bierki
board.surface.black	= Czarne bierki
board.hilite.squares 	= Podświetlenie pól

# Time Controls

dialog.option.time.control      = Partia
dialog.option.phase.1		= Faza 1.
dialog.option.phase.2		= Faza 2.
dialog.option.phase.3		= Faza 3.
dialog.option.all.moves		= Wszystkie
dialog.option.moves.in 		= ruch-ów/y w
dialog.option.increment 	= plus
dialog.option.increment.label 	= na ruch

time.control.blitz		= Blitz
time.control.rapid		= Szybka
time.control.fischer		= Fischer
time.control.tournament		= Turniejowa
# default name for new time control
time.control.new		= Nowa
time.control.delete		= Usuń

# Engine Settings

dialog.option.plugin.1		= Silnik 1
dialog.option.plugin.2		= Silnik 2

plugin.add =
plugin.delete =
plugin.duplicate =
plugin.add.tip = Dodaj nowy silnik
plugin.delete.tip = Usuń konfigurację
plugin.duplicate.tip = Duplikuj konfigurację

dialog.option.plugin.file 	= Plik konfiguracyjny:
dialog.option.plugin.name 	= Nazwa:
dialog.option.plugin.version 	= Wersja:
dialog.option.plugin.author 	= Autor:
dialog.option.plugin.dir 	= Ścieżka:
dialog.option.plugin.logo 	= Logo:
dialog.option.plugin.startup 	= Uruchamianie:

dialog.option.plugin.exe = Wykonywalny:
dialog.option.plugin.args = Argumenty:
dialog.option.plugin.default = Ustawienia domyślne

plugin.info                 = Informacje ogólne
plugin.protocol.xboard      = Protokół XBoard
plugin.protocol.uci         = Protokół UCI
plugin.options              = Opcje silnika
plugin.startup              = Więcej opcji
plugin.show.logos           = Pokaż logo
plugin.show.text            = Pokaż tekst

plugin.switch.ask           = Wybrałeś inny silnik.\n Czy uruchomić go teraz?
plugin.restart.ask          = Zmieniłeś niektóre ustawienia silnika.\n Czy ponownie uruchomić silnik?
plugin.show.info            = Pokaż informacje
plugin.log.file             = Loguj do pliku

# UCI option name
plugin.option.Ponder        = Rozważanie
plugin.option.Random        = Losowo
plugin.option.Hash          = Rozmiar zbioru różnych pozycji (MB)
plugin.option.NalimovPath   = Ścieżka do zbioru bazy Nalimova
plugin.option.NalimovCache  = Pamięć dla bazy Nalimova (MB)
plugin.option.OwnBook       = Użyj zbioru otwarć
plugin.option.BookFile      = Zbiór otwarć
plugin.option.BookLearning  = Zbiór „Ucz się”
plugin.option.MultiPV       = Wariant pierwotny
plugin.option.ClearHash     = Wyczyść zbiór różnych pozycji
plugin.option.UCI_ShowCurrLine  = Pokaż bieżącą linię
plugin.option.UCI_ShowRefutations = Pokaż obalenie
plugin.option.UCI_LimitStrength = Ogranicz siłę
plugin.option.UCI_Elo       = ELO

# 3D Settings

board.surface.background	= Tło
board.surface.coords		= Współrzędne
board.3d.clock              	= Zegar
board.3d.shadow			= Cień
board.3d.reflection		= Odblask
board.3d.anisotropic        	= Filtr anizotropiczny
board.3d.fsaa               	= Wygładzanie pełnoekranowe

board.3d.surface.frame		= Obramowanie
board.3d.light.ambient		= Światło otaczające
board.3d.light.directional	= Światło bezpośrednie
board.3d.screenshot		= Zrzut ekranu
board.3d.defaultview    = Widok domyślny

# Text Styles

font.color 	= Kolor
font.name	= Krój
font.size	= Rozmiar
font.bold	= Pogrubienie
font.italic	= Kursywa
font.sample	= Próbka tekstu


##############################################################################
#	Database Panels
##############################################################################

# default collection folders

collection.trash 	= Kosz
collection.autosave 	= Zapisz automatycznie
collection.clipboard 	= Schowek

# default name for new folders
collection.new 		= Nowy folder

# name of starter database
collection.starter 	= Partia Capablanki

# column titles

column.collection.name 		= Nazwisko
column.collection.gamecount 	= Gry
column.collection.lastmodified 	= Zmodyfikowane

column.game.index 	= Indeks
column.game.white.name 	= Białe
column.game.black.name 	= Czarne
column.game.event 	= Wydarzenie
column.game.site 	= Miejscowość
column.game.date 	= Data
column.game.result 	= Wynik
column.game.round 	= Runda
column.game.board 	= Szachownica
column.game.eco 	= ECO
column.game.opening 	= Otwarcie
column.game.movecount 	= Ruchów
column.game.annotator 	= Sekretarz
column.game.fen     = Pozycja początkowa

#deprecated
column.problem.author 	= Autor
column.problem.source 	= ?ródło
column.problem.number 	= Nr
column.problem.date 	= Data
column.problem.stipulation = Zastrzeżenie
column.problem.dedication = Dedykacja
column.problem.award = Nagroda
column.problem.solution = Rozwiązanie
column.problem.cplus = C+
column.problem.genre = Rodzaj
column.problem.keyword = Hasło
#deprecated

bootstrap.confirm 	= Ścieżka do danych '%datadir%' nie istnieje.\n Czy utworzyć nową ścieżkę? 
bootstrap.create 	= Utwórz ścieżkę do danych

edit.game = Otwórz
edit.all = Otwórz w zakładkach
dnd.move.top.level	= Przesuń na wierzch


##############################################################################
#  Search Panel
##############################################################################

# Tab Titles
dialog.query.info 		= Informacje
dialog.query.comments 		= Komentarze
dialog.query.position 		= Pozycja

dialog.query.search 	= Szukaj
dialog.query.clear 	= Wyczyść
dialog.query.search.in.progress = Szukanie...

dialog.query.0.results 	= Bez wyniku
dialog.query.1.result 	= Jeden wynik
dialog.query.n.results 	= %count% wyników

dialog.query.white		= Białe:
dialog.query.black		= Czarne:

dialog.query.flags 		= Opcje
dialog.query.color.sensitive 	= Uwzględniaj kolory
dialog.query.swap.colors 	=
dialog.query.swap.colors.tip = Zamień kolory
dialog.query.case.sensitive 	= Uwzględniaj wielkość liter
dialog.query.soundex 		= Podobne
dialog.query.result 		= Wynik
dialog.query.stop.results   =

dialog.query.event 		= Wydarzenie:
dialog.query.site 		= Miejscowość:
dialog.query.eco 		= ECO:
dialog.query.annotator 		= Sekretarz:
dialog.query.to 		= do
dialog.query.opening 		= Otwarcie:
dialog.query.date 		= Data:
dialog.query.movecount 		= Ruchów:

dialog.query.commenttext 	= Komentarz:
dialog.query.com.flag 		= Posiada komentarz
dialog.query.com.flag.tip 	= Szukaj gry z komentarzami

dialog.query.var.flag 		= Posiada warianty
dialog.query.var.flag.tip 	= Szukaj gier z wariantami

dialog.query.errors 		= Błąd w szukanym wyrażeniu:
query.error.date.too.small 	= Dane spoza kryterium
query.error.movecount.too.small = Liczba spoza kryterium
query.error.eco.too.long 	= Użyj trzech znaków dla kodów ECO
query.error.eco.character.expected = Kody ECO muszą się zaczynać od A,B,C,D albo E
query.error.eco.number.expected = Kody ECO zawierają litery i liczby od 0 do 99
query.error.number.format 	= Zły format liczby
query.error.date.format 	= Zły format danych

query.setup.enable 		= Szukaj pozycji
query.setup.next 		= Następny ruch:
query.setup.next.white 		= Białe
query.setup.next.white.tip 	= Szukaj pozycji przy ruchu białych
query.setup.next.black 		= Czarne
query.setup.next.black.tip 	= Szukaj pozycji przy ruchu czarnych
query.setup.next.any 		= Białe albo czarne
query.setup.next.any.tip 	= Szukaj pozycji przy ruchu dowolnego koloru
query.setup.reversed 		= Szukaj odwróconych kolorów
query.setup.reversed.tip 	= Szukaj identycznej pozycji przy odwróconych kolorach
query.setup.var 		= Szukaj wariantów
query.setup.var.tip 		= Szukaj wewnątrz wariantów



##############################################################################
#	Game Details dialog
##############################################################################

dialog.game		= Szczegóły gry
dialog.game.tab.1	= Wydarzenie
dialog.game.tab.2	= Gracze
dialog.game.tab.3	= Więcej

dialog.details.event 	= Wydarzenie:
dialog.details.site 	= Miejscowość:
dialog.details.date 	= Data:
dialog.details.eventdate = Data wydarzenia:
dialog.details.round 	= Runda:
dialog.details.board 	= Szachownica:

dialog.details.white 	= Białe
dialog.details.black 	= Czarne
dialog.details.name 	= Nazwisko:
dialog.details.elo 	= ELO:
dialog.details.title 	= Tytuł:
dialog.details.result 	= Wynik:

dialog.details.eco 	= ECO:
dialog.details.opening 	= Otwarcie:
dialog.details.annotator = Sekretarz:

Result.0-1 = 0-1
Result.1-0 = 1-0
Result.1/2 = 1/2
Result.* = *


##############################################################################
#	Setup dialog
##############################################################################

dialog.setup.clear	= Wyczyść
dialog.setup.initial	= Pozycja wyjściowa
dialog.setup.copy	= Kopiuj z głównego panelu

dialog.setup.next.white	= Ruch białych
dialog.setup.next.black	= Ruch czarnych
dialog.setup.move.no	= Numer ruchu

dialog.setup.castling		= Roszada
dialog.setup.castling.wk	= Białe 0-0
dialog.setup.castling.wk.tip	= Roszada na skrzydle królewskim białych
dialog.setup.castling.wq	= Białe 0-0-0
dialog.setup.castling.wq.tip	= Roszada na skrzydle hetmańskim białych
dialog.setup.castling.bk	= Czarne 0-0
dialog.setup.castling.bk.tip	= Roszada na skrzydle królewskim czarnych
dialog.setup.castling.bq	= Czarne 0-0-0
dialog.setup.castling.bq.tip	= Roszada na skrzydle hetmańskim czarnych
dialog.setup.invalid.fen    = Nieprawidłowy zapis FEN.

##############################################################################
#	About dialog
##############################################################################

dialog.about.tab.1	= jose
dialog.about.tab.2	= Baza danych
dialog.about.tab.3	= Współpracownicy
dialog.about.tab.4	= System
dialog.about.tab.5	= 3D
dialog.about.tab.6	= Licencja

dialog.about.1a =   Wersja %version%
dialog.about.1b =   <center><font size=+1><b><a href=\"http://%project-url%\">%project-url%</a></b></font></center> \
                    <br><br> \
                     <table><tr><td>Copyright & copy; %year% %author%  (%contact%)</td> \
                     <td><font size=-1><a href=\"%donate-url%\"><img src=\"%donate-img%\" border=0></a></font></td></tr></table> \
					<br><br> \
					<font size=-1>%gpl-hint%</font>

dialog.about.gpl = Ten program jest rozpowszechniany na zasadach licencji GNU (General Public License)


dialog.about.2	=	<b>%dbname%</b> <br> %dbversion% <br><br> \
					Serwer URL: %dburl%

dialog.about.QED =	Quadcap Embedded Database \n \
					www.quadcap.com

dialog.about.Cloudscape = Cloudscape \n \
							www.cloudscape.com

dialog.about.Oracle = www.oracle.com

dialog.about.MySQL = www.mysql.com

dialog.about.3	=	<b>Tłumacze:</b><br>\
			Frederic Raimbault, José de Paula, \
			Agustín Gomila, Alex Coronado, \
			Harold Roig, Hans Eriksson, \
			Guido Grazioli, Tomasz Sokół, \
			"Direktx", <br>\
			<br>\
			<b>Projektanci czccionek TrueType:</b> <br>\
			Armando Hernandez Marroquin, \
			Eric Bentzen, \
			Alan Cowderoy, <br> \
			Hans Bodlaender \
			(www.chessvariants.com/d.font/fonts.html) <br>\
			<br>\
			<b>Interfejs FlatLaf:</b> <br>\
               FormDev Software (www.formdev.com/flatlaf)<br>\
			<br>\
			<b>Modelowanie 3D:</b> <br>\
			Renzo Del Fabbro, \
			Francisco Barala Faura <br>\
			<br>\
			<b>Pomoc Apple Mac:</b> <br>\
			Andreas Güttinger, Randy Countryman


dialog.about.4 =	Wersja Javy: %java.version% (%java.vendor%) <br>\
					Java VM: %java.vm.version% %java.vm.info% (%java.vm.vendor%) <br>\
					Runtime: %java.runtime.name% %java.runtime.version% <br>\
					Środowisko graficzne: %java.awt.graphicsenv% <br>\
					Zestaw narzędzi AWT: %awt.toolkit%<br>\
					Ścieżka domowa: %java.home%<br>\
					<br>\
					Całkowita pamięć: %maxmem%<br>\
					Dostępna pamięć: %freemem%<br>\
					<br>\
					System operacyjny: %os.name%  %os.version% <br>\
					Architektura systemu: %os.arch%

dialog.about.5.no3d = Java3D jest dostępna
dialog.about.5.model =

dialog.about.5.native = Rodzima platforma: &nbsp;
dialog.about.5.native.unknown = Aktualnie nieznane

##############################################################################
#	Export/Print Dialog
##############################################################################

dialog.export = Eksport i wydruk
dialog.export.tab.1 = Eksport
dialog.export.tab.2 = Ustawienia strony
dialog.export.tab.3 = Style

dialog.export.print = Drukuj...
dialog.export.save = Zapisz
dialog.export.saveas = Zapisz jako...
dialog.export.preview = Podgląd
dialog.export.browser = Podgląd w przeglądarce

dialog.export.paper = Strona
dialog.export.orientation = Orientacja
dialog.export.margins = Marginesy

dialog.export.paper.format = Papier:
dialog.export.paper.size = Powiększenie:

dialog.print.custom.paper = Użytkownika

dialog.export.margin.top = Górny:
dialog.export.margin.bottom = Dolny:
dialog.export.margin.left = Lewy:
dialog.export.margin.right = Prawy:

dialog.export.ori.port = Pionowa
dialog.export.ori.land = Pozioma

dialog.export.games.0 = Nie wybrałeś żadnych gier do drukowania.
dialog.export.games.1 = Wybrałeś <b>jedną grę</b> do drukowania.
dialog.export.games.n = Wybrałeś <b>%n% gry/gier</b> do drukowania.
dialog.export.games.? = Wybrałeś <b>nieznaną</b> ilość gier do drukowania.

dialog.export.confirm = Czy jesteś pewien?
dialog.export.yes = Drukuj wszystkie

# xsl stylesheet options

export.pgn = Plik PGN
export.pgn.tip = Eksportuj gry jako plik Portable Game Notation.

print.awt = Drukuj
print.awt.tip = Drukuj gry z ekranu.<br> \
				    <li>Naciśnij <b>drukuj...</b> aby drukować na drukarce domyślnej \
				    <li>Naciśnij <b>podgląd</b> aby obejrzeć dokument

xsl.html = HTML<br>Strona internetowa
xsl.html.tip = Utwórz stronę internetową (plik HTML).<br> \
				   <li>Naciśnij <b>zapisz jako...</b> aby zapisać plik na dysku \
				   <li>Naciśnij <b>podgląd w przeglądarce</b> aby obejrzeć stronę w przeglądarce internetowej

xsl.dhtml = Dynamiczna<br>strona internetowa
xsl.dhtml.tip = Utwórz stronę internetową <i>z dynamicznymi</i> efektami.<br> \
				   <li>Naciśnij <b>zapisz jako...</b> aby zapisać plik na dysku \
				   <li>Naciśnij <b>podgląd w przeglądarce</b> aby obejrzeć stronę w przeglądarce internetowej <br> \
				  JavaScript musi być włączony w przeglądarce internetowej.

xsl.debug = Plik XML<br>(debug)
export.xml.tip = Utwórz plik XML dla debugowania.

xsl.pdf = Drukuj PDF
xsl.pdf.tip = Utwórz albo drukuj plik PDF.<br> \
				<li>Naciśnij <b>zapisz jako...</b> aby zapisać plik na dysku \
				<li>Naciśnij <b>drukuj...</b> aby wydrukować dokument \
				<li>Naciśnij <b>podgląd</b> aby obejrzeć dokument

xsl.tex = Plik TeX
xsl.tex.tip = Utwórz plik dla przetwarzania TeX.

xsl.html.figs.tt = Bierki TrueType
xsl.html.figs.img = Bierki graficznie
xsl.css.standalone = Dla danych CSS utwórz osobny plik

xsl.html.img.dir = Ścieżka
xsl.create.images = Utwórz obraz

xsl.pdf.embed = Osadź czcionki TrueType
xsl.pdf.font.dir = Dodatkowe czcionki:

default.file.name = Partia


##############################################################################
#	Print Preview Dialog
##############################################################################

print.preview.page.one =
print.preview.page.two =
print.preview.page.one.tip = Pokaż jedną stronę
print.preview.page.two.tip = Pokaż dwie strony

print.preview.ori.land =
print.preview.ori.port =
print.preview.ori.land.tip = Użyj poziomej orientacji papieru
print.preview.ori.port.tip = Użyj pionowej orientacji papieru

print.preview.fit.page = Cała strona
print.preview.fit.width = Szerokość strony
print.preview.fit.textwidth = Szerokość tekstu

print.preview.next.page =
print.preview.previous.page =

preview.wait = Zaczekaj chwilę...

##############################################################################
#	Online Update Dialog
##############################################################################

online.update.title	= Aktualizacja online
online.update.tab.1	= Nowa wersja
online.update.tab.2	= Co nowego?
online.update.tab.3 	= Ważne uwagi

update.install	= Pobierz i zainstaluj teraz
update.download	= Pobierz teraz, zainstaluj później
update.mirror	= Pobierz ze strony lustrzanej

download.file.progress 			= Pobieranie %file%
download.file.title			= Pobierz
download.error.invalid.url		= Nieprawidłowy URL: %p%.
download.error.connect.fail		= Nieudane połączenie z %p%.
download.error.parse.xml		= Błąd odczytu: %p%.
download.error.version.missing	= Nie można odczytać wersji z %p%.
download.error.os.missing		= Brak pliku dla twojego systemu operacyjnego.
download.error.browser.fail		= Nie można wyświetlić %p%.
download.error.update			= Podczas aktualizacji jose wystąpił błąd.\n Ręcznie uaktualnij aplikację.

download.message.up.to.date		= Masz zainstalowaną najnowszą wersję.
download.message.success		= Aktualizacja jose do wersji %p% zakończyła się sukcesem \n. Ponownie uruchom aplikację.

download.message			= \
  Wersja <b>%version%</b> jest dostępna na <br>\
  <font color=blue>%url%</font><br>\
  Rozmiar: %size%

dialog.browser		= Uruchom przeglądarkę HTML.\n Wpisz komendę, która pozwoli uruchomić przeglądarkę.
dialog.browser.title 	= Lokalizacja przeglądarki

# deprecated
#online.report.title	= Raport
#online.report.bug	= Zgłoś błąd
#online.report.feature	= Feature Request
#online.report.support	= Support Request

#online.report.type	= Rodzaj:
#online.report.subject	= Temat:
#online.report.description	= Opis:
#online.report.email		= E-mail:

#online.report.default.subject		= <Subject>
#online.report.default.description	= <Please try to describe the steps that caused the error>
#online.report.default.email		= <your e-mail address - optional>
#online.report.info			= Raport zostanie wysłany do http://jose-chess.sourceforge.net

#online.report.success		= Twój raport został przedłożony.
#online.report.failed		= Nie można przedłożyć twojego raportu.
# deprecated


##########################################
# 	Error Messages
##########################################

error.not.selected 		= Wybierz grę do zapisania.
error.duplicate.database.access = Nie uruchamiaj naraz dwóch aplikacji jose \n\
	korzystających z tej samej bazy danych.\n\n\
	Takie postępowanie może spowodować utratę danych. \n\
	Zakończ jedną aplikację jose.
error.lnf.not.supported 	= Ten interfejs nie jest dostępny \n na bieżącej platformie.

error.bad.uci = Wydaje się, że nie jest to silnik UCI.\n Czy jesteś pewien?

error.bug	= <center><b>Wystąpił niespodziewany błąd.</b></center><br><br> \
 Pomocne może być przedłożenie raportu o błędach.<br>\
 Opisz kroki, które doprowadziły do powstania błędu <br>\
 i dołącz do opisu następujący plik: <br>\
  <center><b> %error.log% </b></center>

# errors in setup dialog

pos.error.white.king.missing	= Brakuje białego króla.
pos.error.black.king.missing	= Brakuje czarnego króla.
pos.error.too.many.white.kings	= Za dużo białych króli.
pos.error.too.many.black.kings	= Za dużo czarnych króli.
pos.error.white.king.checked	= Biały król nie może być w szachu.
pos.error.black.king.checked	= Czarny król nie może być w szachu.
pos.error.white.pawn.base		= Białe piony nie mogą być umieszczone na pierwszej linii.
pos.error.white.pawn.promo		= Białe piony nie mogą być umieszczone na ósmej linii.
pos.error.black.pawn.base		= Czarne piony nie mogą być umieszczone na ósmej linii.
pos.error.black.pawn.promo		= Czarne piony nie mogą być umieszczone na pierwszej linii.
pos.warning.too.many.white.pieces	= Za dużo białych bierek.
pos.warning.too.many.black.pieces	= Za dużo czarnych bierek.
pos.warning.too.many.white.pawns	= Za dużo białych pionów.
pos.warning.too.many.black.pawns	= Za dużo czarnych pionów.
pos.warning.too.many.white.knights	= Za dużo białych skoczków.
pos.warning.too.many.black.knights	= Za dużo czarnych skoczków.
pos.warning.too.many.white.bishops	= Za dużo białych gońców.
pos.warning.too.many.black.bishops	= Za dużo czarnych gońców.
pos.warning.too.many.white.rooks	= Za dużo białych wież.
pos.warning.too.many.black.rooks	= Za dużo czarnych wież.
pos.warning.too.many.white.queens	= Za dużo białych hetmanów.
pos.warning.too.many.black.queens	= Za dużo czarnych hetmanów.
pos.warning.strange.white.bishops	= Białe gońce na jednobarwnych polach?
pos.warning.strange.black.bishops	= Czarne gońce na jednobarwnych polach?


##############################################################################
#	Style Names
##############################################################################


base			= Ogólne
header			= Informacje o grze
header.event	= Wydarzenie
header.site		= Miejscowość
header.date		= Data
header.round	= Runda
header.white	= Białe
header.black	= Czarne
header.result	= Wynik gry
body			= Zapis gry
body.line		= Ruchy
body.line.0		= Główna linia
body.line.1		= Warianty
body.line.2		= Podwarianty
body.line.3		= 2. podwariant
body.line.4		= 3. podwariant
body.symbol		= Symbole
body.inline		= Diagram tekstowy
body.figurine	= Bierki
body.figurine.0	= Główna linia
body.figurine.1	= Wariant
body.figurine.2	= Podwariant
body.figurine.3	= 2. podwariant
body.figurine.4	= 3. podwariant
body.comment	= Komentarze
body.comment.0	= Główna linia
body.comment.1	= Wariant
body.comment.2	= Podwariant
body.comment.3	= 2. podwariant
body.comment.4	= 3. podwariant
body.result		= Wynik
html.large      = Diagram na stronie internetowej


##############################################################################
#	Task Dialogs (progress)
##############################################################################

dialog.progress.time 		= Pozostało: %time%

dialog.read-progress.title 	= jose – otwieranie pliku
dialog.read-progress.text 	= Otwieranie %fileName%

dialog.eco 			= Klasyfikuj ECO
dialog.eco.clobber.eco 		= Nadpisz kody ECO
dialog.eco.clobber.name 	= Nadpisz otwieranie nazwy
dialog.eco.language 		= Język:


##############################################################################
#	foreign language figurines (this block need not be translated)
##############################################################################

fig.langs = cs,da,nl,en,et,fi,fr,de,hu,is,it,no,pl,pt,ro,es,sv,ru,ca,tr,ukr

fig.cs = PJSVDK
fig.da = BSLTDK
fig.nl = OPLTDK
fig.en = PNBRQK
fig.et = PROVLK
fig.fi = PRLTDK
fig.fr = PCFTDR
fig.de = BSLTDK
fig.hu = GHFBVK
fig.is = PRBHDK
fig.it = PCATDR
fig.no = BSLTDK
fig.pl = PSGWHK
fig.pt = PCBTDR
fig.ro = PCNTDR
fig.es = PCATDR
fig.ca = PCATDR
fig.sv = BSLTDK
fig.tr = ??????

# Windows-1251 encoding (russian)
fig.ru = ĎŃĘËÔ"Ęđ"
fig.ukr = ĎŃĘËÔ"Ęđ"
# please note that Russians use the latin alphabet for files (a,b,c,d,e,f,g,h)
# (so we don't have to care about that ;-)

##############################################################################
#	foreign language names
##############################################################################

lang.cs = czeski
lang.da = duński
lang.nl = holenderski
lang.en = angielski
lang.et = estoński
lang.fi = fiński
lang.fr = francuski
lang.de = niemiecki
lang.hu = węgierski
lang.is = islandzki
lang.it = włoski
lang.no = norweski
lang.pl = polski
lang.pt = portugalski
lang.ro = rumuński
lang.es = hiszpański
lang.ca = kataloński
lang.sv = szwedzki
lang.ru = rosyjski
lang.he = hebrajski
lang.tr = turecki
lang.ukr = ukraiński


##############################################################################
#	PGN Annotations
##############################################################################

pgn.nag.0    = unieważnij przypisy
pgn.nag.1    	= !
pgn.nag.1.tip	= dobry ruch
pgn.nag.2	= ?
pgn.nag.2.tip	= zły ruch
pgn.nag.3    	= !!
pgn.nag.3.tip	= bardzo dobry ruch
pgn.nag.4    	= ??
pgn.nag.4.tip	= bardzo zły ruch
pgn.nag.5    	= !?
pgn.nag.5.tip	= interesujący ruch
pgn.nag.6    	= ?!
pgn.nag.6.tip	= wątpliwy ruch
pgn.nag.7    = mocny ruch
pgn.nag.7.tip = mocny ruch (każdy inny szybko spowoduje straty)
pgn.nag.8    = osobliwy ruch
pgn.nag.8.tip    = osobliwy ruch (niezbyt racjonalny)
pgn.nag.9    = najgorszy ruch
pgn.nag.10      = =
pgn.nag.10.tip  = pozycja remisowa
pgn.nag.11      = =
pgn.nag.11.tip  = wyrównane szanse, bierna pozycja
pgn.nag.12.tip   = wyrównane szanse, aktywna pozycja
pgn.nag.13   = niejasne
pgn.nag.13.tip   = niejasna pozycja
pgn.nag.14	= +=
pgn.nag.14.tip	= Białe mają nieznaczną przewagę
pgn.nag.15   	= =+
pgn.nag.15.tip 	= Czarne mają nieznaczną przewagę
pgn.nag.16	= +/-
pgn.nag.16.tip 	= Białe mają umiarkowaną przewagę
pgn.nag.17	= -/+
pgn.nag.17.tip 	= Czarne mają umiarkowaną przewagę
pgn.nag.18	= +-
pgn.nag.18.tip  = Białe mają zdecydowaną przewagę
pgn.nag.19	= -+
pgn.nag.19.tip 	= Czarne mają zdecydowaną przewagę
pgn.nag.20   = Białe mają miażdżącą przewagę (Czarne powinny się poddać)
pgn.nag.21   = Czarne mają miażdżącą przewagę (Białe powinny się poddać)
pgn.nag.22   = Przymusowy ruch białych (zugzwang)
pgn.nag.23   = Przymusowy ruch czarnych (zugzwang)
pgn.nag.24   = Białe mają nieznaczną przewagę pola
pgn.nag.25   = Czarne mają nieznaczną przewagę pola
pgn.nag.26   = Białe mają umiarkowaną przewagę pola
pgn.nag.27   = Czarne mają umiarkowaną przewagę pola
pgn.nag.28   = Białe mają zdecydowaną przewagę pola
pgn.nag.29   = Czarne mają zdecydowaną przewagę pola
pgn.nag.30   = Białe mają nieznaczną (postępującą) przewagę czasu
pgn.nag.31   = Czarne mają nieznaczną (postępującą) przewagę czasu
pgn.nag.32   = Białe mają umiarkowaną (postępującą) przewagę czasu
pgn.nag.33   = Czarne mają umiarkowaną (postępującą) przewagę czasu
pgn.nag.34   = Białe mają zdecydowaną (postępującą) przewagę czasu
pgn.nag.35   = Czarne mają zdecydowaną (postępującą) przewagę czasu
pgn.nag.36   = Inicjatywa po stronie białych
pgn.nag.37   = Inicjatywa po stronie czarnych
pgn.nag.38   = Stała inicjatywa po stronie białych
pgn.nag.39   = Stała inicjatywa po stronie czarnych
pgn.nag.40   = Białe w ataku
pgn.nag.41   = Czarne w ataku
pgn.nag.42   = Białe nie mają dostatecznej kompensaty strat materialnych
pgn.nag.43   = Czarne nie mają dostatecznej kompensaty strat materialnych
pgn.nag.44   = Białe mają dostateczną kompensatę strat materialnych
pgn.nag.45   = Czarne mają dostateczną kompensatę strat materialnych
pgn.nag.46   = Kompensata białych jest większa niż straty materialne
pgn.nag.47   = Kompensata czarnych jest większa niż straty materialne
pgn.nag.48   = Nieznaczna przewaga białych w kontroli centrum
pgn.nag.49   = Nieznaczna przewaga czarnych w kontroli centrum
pgn.nag.50   = Umiarkowana przewaga białych w kontroli centrum
pgn.nag.51   = Umiarkowana przewaga czarnych w kontroli centrum
pgn.nag.52   = Zdecydowana przewaga białych w kontroli centrum
pgn.nag.53   = Zdecydowana przewaga czarnych w kontroli centrum
pgn.nag.54   = Nieznaczna przewaga białych w kontroli skrzydła królewskiego
pgn.nag.55   = Nieznaczna przewaga czarnych w kontroli skrzydła królewskiego
pgn.nag.56   = Umiarkowana przewaga białych w kontroli skrzydła królewskiego
pgn.nag.57   = Umiarkowana przewaga czarnych w kontroli skrzydła królewskiego
pgn.nag.58   = Zdecydowana przewaga białych w kontroli skrzydła królewskiego
pgn.nag.59   = Zdecydowana przewaga czarnych w kontroli skrzydła królewskiego
pgn.nag.60   = Nieznaczna przewaga białych w kontroli skrzydła hetmańskiego
pgn.nag.61   = Nieznaczna przewaga czarnych w kontroli skrzydła hetmańskiego
pgn.nag.62   = Umiarkowana przewaga białych w kontroli skrzydła hetmańskiego
pgn.nag.63   = Umiarkowana przewaga czarnych w kontroli skrzydła hetmańskiego
pgn.nag.64   = Zdecydowana przewaga białych w kontroli skrzydła hetmańskiego
pgn.nag.65   = Zdecydowana przewaga czarnych w kontroli skrzydła hetmańskiego
pgn.nag.66   = Niezabezpieczona pierwsza linia białych
pgn.nag.67   = Niezabezpieczona pierwsza linia czarnych
pgn.nag.68   = Zabezpieczona pierwsza linia białych
pgn.nag.69   = Zabezpieczona pierwsza linia czarnych
pgn.nag.70   = Niewystarczająca ochrona białego króla
pgn.nag.71   = Niewystarczająca ochrona czarnego króla
pgn.nag.72   = Dobra ochrona białego króla
pgn.nag.73   = Dobra ochrona czarnego króla
pgn.nag.74   = Kiepskie umiejscowienie białego króla
pgn.nag.75   = Kiepskie umiejscowienie czrnego króla
pgn.nag.76   = Dobre umiejscowienie białego króla
pgn.nag.77   = Dobre umiejscowienie czarnego króla
pgn.nag.78   = Bardzo niedobry układ białych pionów
pgn.nag.79   = Bardzo niedobry układ czarnych pionów
pgn.nag.80   = Białe mają umiarkowanie zły układ pionów
pgn.nag.81   = Czarne mają umiarkowanie zły układ pionów
pgn.nag.82   = Białe mają umiarkowanie silny układ pionów
pgn.nag.83   = Czarne mają umiarkowanie silny układ pionów
pgn.nag.84   = Białe mają bardzo silny układ pionów
pgn.nag.85   = Czarne mają bardzo silny układ pionów
pgn.nag.86   = Kiepskie umiejscowienie białego skoczka
pgn.nag.87   = Kiepskie umiejscowienie czarnego skoczka
pgn.nag.88   = Dobre umiejscowienie białego skoczka
pgn.nag.89   = Dobre umiejscowienie czarnego skoczka
pgn.nag.90   = Kiepskie umiejscowienie białego gońca
pgn.nag.91   = Kiepskie umiejscowienie czarnego gońca
pgn.nag.92   = Dobre umiejscowienie białego gońca
pgn.nag.93   = Dobre umiejscowienie czarnego gońca
pgn.nag.94   = Kiepskie umiejscowienie białej wieży
pgn.nag.95   = Kiepskie umiejscowienie czarnej wieży
pgn.nag.96   = Dobre umiejscowienie białej wieży
pgn.nag.97   = Dobre umiejscowienie czarnej wieży
pgn.nag.98   = Kiepskie umiejscowienie białego hetmana
pgn.nag.99   = Kiepskie umiejscowienie czarnego hetmana
pgn.nag.100  = Dobre umiejscowienie białego hetmana
pgn.nag.101  = Dobre umiejscowienie czarnego hetmana
pgn.nag.102  = Niewystarczająca koordynacja białych bierek
pgn.nag.103  = Niewystarczająca koordynacja czarnych bierek
pgn.nag.104  = Dobra koordynacja białych bierek
pgn.nag.105  = Dobra koordynacja czarnych bierek
pgn.nag.106  = Białe bardzo źle rozegrały otwarcie
pgn.nag.107  = Czarne bardzo źle rozegrały otwarcie
pgn.nag.108  = Białe źle rozegrały otwarcie
pgn.nag.109  = Czarne źle rozegrały otwarcie
pgn.nag.110  = Białe dobrze rozegrały otwarcie
pgn.nag.111  = Czarne dobrze rozegrały otwarcie
pgn.nag.112  = Białe bardzo dobrze rozegrały otwarcie
pgn.nag.113  = Czarne bardzo dobrze rozegrały otwarcie
pgn.nag.114  = Białe bardzo źle rozegrały środkową część gry
pgn.nag.115  = Czarne bardzo źle rozegrały środkową część gry
pgn.nag.116  = Białe źle rozegrały środkową część gry
pgn.nag.117  = Czarne źle rozegrały środkową część gry
pgn.nag.118  = Białe dobrze rozegrały środkową część gry
pgn.nag.119  = Czarne dobrze rozegrały środkową część gry
pgn.nag.120  = Białe bardzo dobrze rozegrały środkową część gry
pgn.nag.121  = Czarne bardzo dobrze rozegrały środkową część gry
pgn.nag.122  = Białe bardzo źle rozegrały końcówkę
pgn.nag.123  = Czarne bardzo źle rozegrały końcówkę
pgn.nag.124  = Białe źle rozegrały końcówkę
pgn.nag.125  = Czarne źle rozegrały końcówkę
pgn.nag.126  = Białe dobrze rozegrały końcówkę
pgn.nag.127  = Czarne dobrze rozegrały końcówkę
pgn.nag.128  = Białe bardzo dobrze rozegrały końcówkę
pgn.nag.129  = Czarne bardzo dobrze rozegrały końcówkę
pgn.nag.130  = Białe w nieznacznym przeciwnatarciu
pgn.nag.131  = Czarne w nieznacznym przeciwnatarciu
pgn.nag.132  = Białe w umiarkowanym przeciwnatarciu
pgn.nag.133  = Czarne w umiarkowanym przeciwnatarciu
pgn.nag.134  = Białe w zdecydowanym przeciwnatarciu
pgn.nag.135  = Czarne w zdecydowanym przeciwnatarciu
pgn.nag.136  = Białe w umiarkowanym niedoczasie
pgn.nag.137  = Czarne w umiarkowanym niedoczasie
pgn.nag.138  = Białe w dotkliwym niedoczasie
pgn.nag.139  = Czarne w dotkliwym niedoczasie

# following codes are defined by Fritz or SCID

pgn.nag.140  	= Pomysł
pgn.nag.141  	= Przeciwko
pgn.nag.142  	= Jest lepsze
pgn.nag.143  	= Jest gorsze
pgn.nag.144  	= =
pgn.nag.144.tip = Jest równoważne
pgn.nag.145  	= RR
pgn.nag.145.tip	= Notka redakcyjna
pgn.nag.146  	= N
pgn.nag.146.tip = Innowacja
pgn.nag.147     = Słaby punkt
pgn.nag.148     = Końcówka
pgn.nag.149     = Linia
pgn.nag.150		= Przekątna
pgn.nag.151		= Białe mają parę gońców
pgn.nag.152     = Czarne mają parę gońców
pgn.nag.153		= Gońce na dwubarwnych polach
pgn.nag.154		= Gońce na jednobarwnych polach

# following codes are defined by us (equivalent to Informator symbols)
# (is there a standard definition for these symbols ?)

pgn.nag.156		= Wolny pion
pgn.nag.157		= Więcej pionów
pgn.nag.158		= Z
pgn.nag.159		= Bez
pgn.nag.161		= Patrz
pgn.nag.163		= Szereg

# defined by SCID:
pgn.nag.190		= Itd.
pgn.nag.191		= Zdublowane piony
pgn.nag.192		= Odizolowane piony
pgn.nag.193		= Powiązane piony
pgn.nag.194     = Zawieszone piony
pgn.nag.195     = Spóźnione piony

# this code is only defined by us
pgn.nag.201  = Diagram
pgn.nag.250  = Diagram


#	old     new
#---------+---------
#		147
#		148
#	162	149
#
#	164     150
#	150     151
#		152
#	151     153
#	152     154
#	160     190
#	155     191
#	154     192
#	153     193
#		194
#		195