file.encoding=Windows-1250

##############################################################################
#	Polish translation for jose 1.3.4 RC 1 by Tomasz Sok� (progtom@wp.pl)
##############################################################################

#	Application Name
application.name	= jose

#	Frame Titles
window.board	= Szachownica
window.console	= Konsola silnika
window.database	= Baza danych
window.filter	= Filtr
window.list	= Lista posuni��
window.clock	= Zegar
window.game	= Gra
window.engine	= Silnik
window.eval     = Szacowanie profilu

window.collectionlist	= Baza danych
window.query		= Znajd�
window.gamelist		= Baza danych

window.sqlquery		= Zapytanie SQL
window.sqllist		= Wynik

window.toolbar.1	= Pasek narz�dzi 1
window.toolbar.2	= Pasek narz�dzi 2
window.toolbar.3	= Pasek narz�dzi 3
window.toolbar.symbols	= Przypisy
window.help		        = Pomoc
window.print.preview    = Podgl�d wydruku

# dialog titles

dialog.option	= Opcje
dialog.about	= Informacje
dialog.animate  = Animacja
dialog.setup	= Ustawienia
dialog.message.title = Wiadomo��

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
menu.file.new.tip	= Rozpocznij now� gr�
menu.file.new.frc = Nowy FRC
menu.file.open		= Otw�rz...
menu.file.open.tip  	= Otw�rz plik PGN
menu.file.open.url	= Otw�rz URL...
menu.file.close		= Zamknij
menu.file.close.tip	= Zamknij bie��ce okno
menu.file.save		= Zapisz
menu.file.save.tip  	= Zapisz bie��c� gr� w bazie danych
menu.file.save.as    	= Zapisz jako...
menu.file.save.as.tip	= Zapisz bie��c� gr� w nowej kopii w bazie danych
menu.file.save.all   	= Zapisz wszystko
menu.file.save.all.tip  = Zapisz wszystkie otwarte gry w bazie danych
menu.file.revert	= Przywr�� gr�
menu.file.print		= Drukuj...
menu.file.print.tip	= Drukuj bie��c� gr�
menu.file.print.setup	= Ustawienia strony...
menu.file.print.setup.tip = Ustawienia drukarki i rozmiar papieru
menu.file.print.preview = Podgl�d wydruku...
menu.file.quit		= Koniec
menu.file.quit.tip	= Zamknij jose

# Edit Menu

menu.edit		= Edycja
menu.edit.undo		= Cofnij (%action%)
menu.edit.cant.undo 	= Cofnij
menu.edit.redo		= Pon�w (%action%)
menu.edit.cant.redo 	= Pon�w
menu.edit.select.all    = Zaznacz wszystko
menu.edit.select.none   = Odznacz wszytko
menu.edit.cut		= Wytnij
menu.edit.copy		= Kopiuj
menu.edit.copy.fen  = Zapis FEN
menu.edit.copy.fen.tip = Kopiuj bie��c� pozycj� do schowka (w notacji FEN)
menu.edit.copy.img  = Diagram graficzny i t�o
menu.edit.copy.img.tip = Kopiuj bie��c� pozycj� do schowka (jako obraz)
menu.edit.copy.imgt  = Diagram graficzny
menu.edit.copy.imgt.tip = Kopiuj bie��c� pozycj� do schowka (jako obraz)
menu.edit.copy.text  = Diagram tekstowy
menu.edit.copy.text.tip = Kopiuj bie��c� pozycj� do schowka (jako tekst)

menu.edit.copy.pgn  = Kopiuj PGN
menu.edit.copy.pgn.tip = Kopiuj bie��c� gr� do schowka (jako tekst PGN)
menu.edit.paste		= Wklej
menu.edit.paste.tip = Wklej ze schowka
menu.edit.paste.copy		= Wklej kopie
menu.edit.paste.copy.tip 	= Wklej gry ze schowka
menu.edit.paste.same 	= Wklej gry
menu.edit.paste.same.tip = Przenie� gry ze schowka
menu.edit.paste.pgn = Wklej PGN
menu.edit.paste.pgn.tip = Wstaw gr� ze schowka (jako tekst PGN)
menu.edit.clear		= Wyczy��
menu.edit.option	= Opcje...
menu.edit.option.tip	= Otw�rz okno dialogowe opcji

menu.edit.games			= Baza danych
menu.edit.collection.new 	= Nowy folder
menu.edit.collection.rename 	= Zmie� nazw�
menu.edit.collection.crunch = Dostosuj
menu.edit.collection.crunch.tip = Dostosuj kolumn� indeksu
menu.edit.empty.trash		= Wyczy�� kosz
menu.edit.restore		= Przywr��

#menu.edit.position.index    = Uaktualnij list� posunie�
menu.edit.search.current    = Szukaj pozycji
menu.edit.ecofy             = Klasyfikuj ECO

menu.edit.style = Styl tekstu
menu.edit.bold = Pogrubiony
menu.edit.italic = Kursywa
menu.edit.underline = Podkre�lony
menu.edit.plain = Normalny
menu.edit.left = Wyr�wnany do lewej
menu.edit.center = wy�rodkowany
menu.edit.right = Wyr�wnany do prawej
menu.edit.larger = Zwi�ksz rozmiar czcionki
menu.edit.smaller = Zmniejsz rozmiar czcionki
menu.edit.color = Kolor czcionki

# Game Menu

menu.game		= Gra
menu.game.details	= Szczeg�y...
menu.game.analysis  	= Tryb analizy
menu.game.navigate	= Przejd� do...
menu.game.time.controls = Partia
menu.game.time.control = Partia
menu.game.details.tip 	= Edytuj szczeg�y gry (gracze, itd.)
menu.game.hint		= Podpowiedz
menu.game.hint.tip  = Poka� podpowied�
menu.game.draw		= Przyjmij ruch
menu.game.resign	= Poddaj�
menu.game.2d		= Widok 2D
menu.game.3d		= Widok 3D
menu.game.flip		= Obr�� szachownic�
menu.game.coords	= Wsp�rz�dne
menu.game.coords.tip	= Pokazuj wsp�rz�dne
menu.game.animate 	= Animacja...
menu.game.previous 	= Poprzednia zak�adka
menu.game.next 		= Nast�pna zak�adka
menu.game.close 	= Zamknij
menu.game.close.tip 	= Zamknij bie��c� gr�
menu.game.close.all 	= Zamknij wszystkie
menu.game.close.all.tip = Zamknij wszystkie otwarte gry
menu.game.close.all.but = Zamknij wszystkie opr�cz bie��cej
menu.game.close.all.but.tip = Zamknij wszystkie otwarte gry opr�cz bie��cej
menu.game.setup		= Ustaw pozycj�

menu.game.copy.line = Kopiuj lini�
menu.game.copy.line.tip = Kopiuj t� lini� do schowka
menu.game.paste.line = Wklej lini�
menu.game.paste.line.tip = Wstaw t� lini� do bie��cej gry

# Window Menu

menu.window		= Okno
menu.window.fullscreen 	= Widok pe�noekranowy
menu.window.reset   	= Od�wie� okno

# Help Menu

menu.help		= Pomoc
menu.help.splash	= O jose...
menu.help.about		= Informacje...
menu.help.license	= Licencja...
menu.help.context   	= Pomoc kontekstowa
menu.help.manual    	= Podr�cznik
menu.help.web		= jose w Internecie

menu.web.home		= Strona domowa
menu.web.update		= Aktualizacja online
menu.web.download	= Pobierz
menu.web.report		= Zg�o� b��d
menu.web.support	= Pro�ba o pomoc
menu.web.feature	= Pro�ba o przedstawienie
menu.web.forum		= Forum
menu.web.donate     = Wesprzyj
menu.web.browser	= Wybierz przegl�dark�...


##############################################################################
# 	Context Menu
##############################################################################

panel.hide		= Ukryj
panel.hide.tip		= Ukryj ten panel
panel.undock		= Nowe okno
panel.undock.tip	= Otw�rz ten panel w osobnym oknie
panel.move		= Przesu�
panel.move.tip		= Przesu� ten panel w nowe po�o�enie
panel.dock		= Dokuj
panel.dock.tip		= Dokuj to okno

panel.orig.pos = Pierwotne po�o�enie
panel.dock.here = Dokuj tutaj
panel.undock.here = Oddokuj

#################
# Document Panel
#################

# deprecated:
tab.place 	= Umiejscowienie zak�adek
tab.place.top 	= U g�ry
tab.place.left 	= Po lewej
tab.place.bottom = Na dole
tab.place.right = Po prawej
#

tab.layout 		= Uk�ad zak�adek
tab.layout.wrap 	= Pi�tra
tab.layout.scroll 	= Szereg

doc.menu.annotate 	= Przypisy
doc.menu.delete.comment = Usu� komentarz
doc.menu.line.promote 	= Awansuj lini�
doc.menu.line.delete 	= Usu� lini�
doc.menu.line.cut 	= Odetnij lini�
doc.menu.line.uncomment = Usu� wszystkie komentarze
doc.menu.remove.annotation = -None-
doc.menu.more.annotations = Wi�cej...

tab.untitled 	= Bez tytu�u
confirm 	= Potwierdzenie
confirm.save.one = Czy zapisa� bie��c� gr�?
confirm.save.all = Czy zapisa� zmodyfikowane gry?

dialog.confirm.save = Zapisz
dialog.confirm.dont.save = Nie zapisuj

dialog.engine.offers.draw = %engine% proponuje ruch.

dialog.accept.draw = Akceptuj ruch
dialog.decline.draw = Odrzu� ruch

dialog.autoimport.title = Importuj
dialog.autoimport.ask = Plik ^0 zmieni� si� na dysku. \n Czy ponownie go otworzy�?

dialog.paste.message = Zamierzasz wstawi� dane ze schowka. \n\
     Czy chcesz przesun�� gry, czy utworzy� now� kopi�?
dialog.paste.title = Wklej gry
dialog.paste.same = Przesu�
dialog.paste.copy = Kopiuj

###################
# Game Navigation
###################

move.first	= Pocz�tek gry
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
engine.thinking.tip 	= %engine% my�li nad nast�pnym ruchem
engine.pondering.tip 	= %engine% rozwa�a tw�j nast�pny ruch
engine.analyzing.tip 	= %engine% analizuje
engine.hint.tip 	= Podpowied�: %move%

engine.paused.title 	= %engine%
engine.thinking.title 	= %engine% my�li
engine.pondering.title 	= %engine% rozwa�a
engine.analyzing.title 	= %engine% analizuje

plugin.name 		= %name% %version%
plugin.name.author 	= %name% %version%. Autor %author%

plugin.book.move 	= BK
plugin.book.move.tip 	= Zbi�r ruch�w
plugin.hash.move 	= HT
plugin.hash.move.tip 	= Szacowany ze zbioru r�nych pozycji
plugin.tb.move 		= TB
plugin.tb.move.tip 	= Szacowany ze zbioru ko�c�wek

plugin.currentmove.title       = Ruch
plugin.depth.title      = G��boko��
plugin.elapsed.time.title  = Czas
plugin.nodecount.title  = Warianty
plugin.nps.title        = W/sek.

plugin.currentmove = %move%
plugin.currentmove.max = %move% %moveno%/%maxmove%

plugin.currentmove.tip = Aktualnie szacowany ruch %move%.
plugin.currentmove.max.tip = Aktualnie szacowany ruch %move%. (nr %moveno% z %maxmove%)

plugin.depth 		= %depth%
plugin.depth.tip 	= G��boko�� szukania: Warstwa %depth%.

plugin.depth.sel 	= %depth% (%seldepth%)
plugin.depth.sel.tip 	= G��boko�� szukania: %depth%. warstwa. G��boko�� wybi�rcza: %seldepth%. warstwa

plugin.white.mates 	= +#%eval%
plugin.white.mates.tip 	= Bia�e matuj� w %eval% ruchach
plugin.black.mates 	= -#%eval%
plugin.black.mates.tip 	= Czarne matuj� w %eval% ruchach

plugin.evaluation 	= %eval%
plugin.evaluation.tip 	= Warto�� pozycji: %eval%

plugin.line.tip = Szacowanie zakresu

plugin.elapsed.time = %time%
plugin.elapsed.time.tip = Up�yw czasu dla tego obliczenia.

plugin.nodecount 	= %nodecount%
plugin.nodecount.tip 	= %nodecount% oszacowanych pozycji

plugin.nps      = %nps%
plugin.nps.tip  = %nps% szacowanych wariant�w na sekund�

plugin.pv.history = Tryb eksperta

restart.plugin		= Ponownie uruchom silnik


######################
# Board Panel
######################

wait.3d = �adowanie 3D. Prosz� czeka�...

message.result			= Wynik
message.white 			= Bia�e
message.black 			= Czarne
message.mate 			= Mat. \n %player% wygra�.
message.stalemate		= Pat. \n Remis.
message.draw3			= Pozycja powt�rzona 3 razy. \n Remis.
message.draw50			= Bez zmiany w bierkach przez 50 ruch�w. \n Remis.
message.resign			= %player% podda�. \n Wygra�e�.
message.time.draw		= Up�yn�� czas. \n Remis.
message.time.lose		= Up�yn�� czas. \n %player% wygra�.


################
# Clock Panel
################

clock.mode.analog	= Analogowy
clock.mode.analog.tip 	= Poka� zegar analogowy
clock.mode.digital	= Cyfrowy
clock.mode.digital.tip 	= Poka� zegar cyfrowy


##############################################################################
#	Dialogs
##############################################################################

dialog.button.ok		= OK
dialog.button.ok.tip		= Uaktywnij wprowadzone zmiany
dialog.button.cancel		= Anuluj
dialog.button.cancel.tip	= Zamknij okno dialogowe bez uaktywniania wprowadzonych zmian
dialog.button.apply		= Zatwierd�
dialog.button.apply.tip		= Natychmiast zatwierd� wprowadzone zmiany
dialog.button.revert		= Przywr��
dialog.button.revert.tip	= Przywr�� poprzednie ustawienia
dialog.button.clear		= Wyczy��
dialog.button.delete		= Usu�
dialog.button.yes		= Tak
dialog.button.no		= Nie
dialog.button.next		= Dalej
dialog.button.back		= Wstecz
dialog.button.close		= Zamknij
dialog.button.help		= Pomoc
dialog.button.help.tip		= Poka� tematy pomocy

dialog.button.commit		= Przyjmij
dialog.button.commit.tip	= Naci�nij tutaj, aby przyj�� uaktualnienieClick here to commit updates
dialog.button.rollback		= Wycofaj
dialog.button.rollback.tip	= Naci�nij tutaj, aby odrzuci� uaktualnienie

dialog.error.title		= B��d

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

filechooser.overwrite 	= Nadpisa� istniej�cy "%file.name%"?
filechooser.do.overwrite = Nadpisz

#################
# Color Chooser
#################

colorchooser.texture	= Tekstura
colorchooser.preview	= Podgl�d
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
dialog.option.user.language	= J�zyk
dialog.option.ui.look.and.feel	= Interfejs

doc.load.history	= �aduj ostatnio otwarte gry
doc.classify.eco	= Klasyfikuj otwieranie przez ECO

dialog.option.animation = Animacja
dialog.option.animation.speed = Szybko��

dialog.option.doc.write.mode	= Wstaw nowy ruch
write.mode.new.line		= Nowa linia
write.mode.new.main.line	= Nowa g��wna linia
write.mode.overwrite		= Nadpisz
write.mode.ask			= Pytaj
write.mode.dont.ask		= Nie pytaj wi�cej
write.mode.cancel		= Anuluj

board.animation.hints   = Poka� drog� podczas animacji

dialog.option.sound = Mowa
dialog.option.sound.moves.dir = Mowa przy ruchu:
sound.moves.engine  = Wypowiadaj ruch silnika
sound.moves.ack.user = Potwierd� ruch gracza
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

doc.panel.antialias		= Wyg�adzanie czcionek

# Notation

dialog.option.doc.move.format	= Notacja:
move.format			= Notacja
move.format.short		= Kr�tka
move.format.long		= D�uga
move.format.algebraic		= Algebraiczna
move.format.correspondence	= Korespondencyjna
move.format.english		= Angielska
move.format.telegraphic		= Telegraficzna

# Colors

dialog.option.board.surface.light	= Bia�e pola
dialog.option.board.surface.dark	= Czarne pola
dialog.option.board.surface.white	= Bia�e bierki
dialog.option.board.surface.black	= Czarne bierki

dialog.option.board.surface.background	= T�o
dialog.option.board.surface.frame	= Obramowanie
dialog.option.board.surface.coords	= Wsp�rz�dne

dialog.option.board.3d.model            = Model:
dialog.option.board.3d.clock            = Zegar
dialog.option.board.3d.surface.frame	= Obramowanie:
dialog.option.board.3d.light.ambient	= �wiat�o otaczaj�ce:
dialog.option.board.3d.light.directional = �wiat�o bezpo�rednie:
dialog.option.board.3d.knight.angle     = Skoczki:

board.surface.light	= Bi��e pola
board.surface.dark	= Czarne pola
board.surface.white	= Bia�e bierki
board.surface.black	= Czarne bierki
board.hilite.squares 	= Pod�wietlenie p�l

# Time Controls

dialog.option.time.control      = Partia
dialog.option.phase.1		= Faza 1.
dialog.option.phase.2		= Faza 2.
dialog.option.phase.3		= Faza 3.
dialog.option.all.moves		= Wszystkie
dialog.option.moves.in 		= ruch-�w/y w
dialog.option.increment 	= plus
dialog.option.increment.label 	= na ruch

time.control.blitz		= Blitz
time.control.rapid		= Szybka
time.control.fischer		= Fischer
time.control.tournament		= Turniejowa
# default name for new time control
time.control.new		= Nowa
time.control.delete		= Usu�

# Engine Settings

dialog.option.plugin.1		= Silnik 1
dialog.option.plugin.2		= Silnik 2

plugin.add =
plugin.delete =
plugin.duplicate =
plugin.add.tip = Dodaj nowy silnik
plugin.delete.tip = Usu� konfiguracj�
plugin.duplicate.tip = Duplikuj konfiguracj�

dialog.option.plugin.file 	= Plik konfiguracyjny:
dialog.option.plugin.name 	= Nazwa:
dialog.option.plugin.version 	= Wersja:
dialog.option.plugin.author 	= Autor:
dialog.option.plugin.dir 	= �cie�ka:
dialog.option.plugin.logo 	= Logo:
dialog.option.plugin.startup 	= Uruchamianie:

dialog.option.plugin.exe = Wykonywalny:
dialog.option.plugin.args = Argumenty:
dialog.option.plugin.default = Ustawienia domy�lne

plugin.info                 = Informacje og�lne
plugin.protocol.xboard      = Protok� XBoard
plugin.protocol.uci         = Protok� UCI
plugin.options              = Opcje silnika
plugin.startup              = Wi�cej opcji
plugin.show.logos           = Poka� logo
plugin.show.text            = Poka� tekst

plugin.switch.ask           = Wybra�e� inny silnik.\n Czy uruchomi� go teraz?
plugin.restart.ask          = Zmieni�e� niekt�re ustawienia silnika.\n Czy ponownie uruchomi� silnik?
plugin.show.info            = Poka� informacje
plugin.log.file             = Loguj do pliku

# UCI option name
plugin.option.Ponder        = Rozwa�anie
plugin.option.Random        = Losowo
plugin.option.Hash          = Rozmiar zbioru r�nych pozycji (MB)
plugin.option.NalimovPath   = �cie�ka do zbioru bazy Nalimova
plugin.option.NalimovCache  = Pami�� dla bazy Nalimova (MB)
plugin.option.OwnBook       = U�yj zbioru otwar�
plugin.option.BookFile      = Zbi�r otwar�
plugin.option.BookLearning  = Zbi�r �Ucz si�
plugin.option.MultiPV       = Wariant pierwotny
plugin.option.ClearHash     = Wyczy�� zbi�r r�nych pozycji
plugin.option.UCI_ShowCurrLine  = Poka� bie��c� lini�
plugin.option.UCI_ShowRefutations = Poka� obalenie
plugin.option.UCI_LimitStrength = Ogranicz si��
plugin.option.UCI_Elo       = ELO

# 3D Settings

board.surface.background	= T�o
board.surface.coords		= Wsp�rz�dne
board.3d.clock              	= Zegar
board.3d.shadow			= Cie�
board.3d.reflection		= Odblask
board.3d.anisotropic        	= Filtr anizotropiczny
board.3d.fsaa               	= Wyg�adzanie pe�noekranowe

board.3d.surface.frame		= Obramowanie
board.3d.light.ambient		= �wiat�o otaczaj�ce
board.3d.light.directional	= �wiat�o bezpo�rednie
board.3d.screenshot		= Zrzut ekranu
board.3d.defaultview    = Widok domy�lny

# Text Styles

font.color 	= Kolor
font.name	= Kr�j
font.size	= Rozmiar
font.bold	= Pogrubienie
font.italic	= Kursywa
font.sample	= Pr�bka tekstu


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
column.game.white.name 	= Bia�e
column.game.black.name 	= Czarne
column.game.event 	= Wydarzenie
column.game.site 	= Miejscowo��
column.game.date 	= Data
column.game.result 	= Wynik
column.game.round 	= Runda
column.game.board 	= Szachownica
column.game.eco 	= ECO
column.game.opening 	= Otwarcie
column.game.movecount 	= Ruch�w
column.game.annotator 	= Sekretarz
column.game.fen     = Pozycja pocz�tkowa

#deprecated
column.problem.author 	= Autor
column.problem.source 	= ?r�d�o
column.problem.number 	= Nr
column.problem.date 	= Data
column.problem.stipulation = Zastrze�enie
column.problem.dedication = Dedykacja
column.problem.award = Nagroda
column.problem.solution = Rozwi�zanie
column.problem.cplus = C+
column.problem.genre = Rodzaj
column.problem.keyword = Has�o
#deprecated

bootstrap.confirm 	= �cie�ka do danych '%datadir%' nie istnieje.\n Czy utworzy� now� �cie�k�? 
bootstrap.create 	= Utw�rz �cie�k� do danych

edit.game = Otw�rz
edit.all = Otw�rz w zak�adkach
dnd.move.top.level	= Przesu� na wierzch


##############################################################################
#  Search Panel
##############################################################################

# Tab Titles
dialog.query.info 		= Informacje
dialog.query.comments 		= Komentarze
dialog.query.position 		= Pozycja

dialog.query.search 	= Szukaj
dialog.query.clear 	= Wyczy��
dialog.query.search.in.progress = Szukanie...

dialog.query.0.results 	= Bez wyniku
dialog.query.1.result 	= Jeden wynik
dialog.query.n.results 	= %count% wynik�w

dialog.query.white		= Bia�e:
dialog.query.black		= Czarne:

dialog.query.flags 		= Opcje
dialog.query.color.sensitive 	= Uwzgl�dniaj kolory
dialog.query.swap.colors 	=
dialog.query.swap.colors.tip = Zamie� kolory
dialog.query.case.sensitive 	= Uwzgl�dniaj wielko�� liter
dialog.query.soundex 		= Podobne
dialog.query.result 		= Wynik
dialog.query.stop.results   =

dialog.query.event 		= Wydarzenie:
dialog.query.site 		= Miejscowo��:
dialog.query.eco 		= ECO:
dialog.query.annotator 		= Sekretarz:
dialog.query.to 		= do
dialog.query.opening 		= Otwarcie:
dialog.query.date 		= Data:
dialog.query.movecount 		= Ruch�w:

dialog.query.commenttext 	= Komentarz:
dialog.query.com.flag 		= Posiada komentarz
dialog.query.com.flag.tip 	= Szukaj gry z komentarzami

dialog.query.var.flag 		= Posiada warianty
dialog.query.var.flag.tip 	= Szukaj gier z wariantami

dialog.query.errors 		= B��d w szukanym wyra�eniu:
query.error.date.too.small 	= Dane spoza kryterium
query.error.movecount.too.small = Liczba spoza kryterium
query.error.eco.too.long 	= U�yj trzech znak�w dla kod�w ECO
query.error.eco.character.expected = Kody ECO musz� si� zaczyna� od A,B,C,D albo E
query.error.eco.number.expected = Kody ECO zawieraj� litery i liczby od 0 do 99
query.error.number.format 	= Z�y format liczby
query.error.date.format 	= Z�y format danych

query.setup.enable 		= Szukaj pozycji
query.setup.next 		= Nast�pny ruch:
query.setup.next.white 		= Bia�e
query.setup.next.white.tip 	= Szukaj pozycji przy ruchu bia�ych
query.setup.next.black 		= Czarne
query.setup.next.black.tip 	= Szukaj pozycji przy ruchu czarnych
query.setup.next.any 		= Bia�e albo czarne
query.setup.next.any.tip 	= Szukaj pozycji przy ruchu dowolnego koloru
query.setup.reversed 		= Szukaj odwr�conych kolor�w
query.setup.reversed.tip 	= Szukaj identycznej pozycji przy odwr�conych kolorach
query.setup.var 		= Szukaj wariant�w
query.setup.var.tip 		= Szukaj wewn�trz wariant�w



##############################################################################
#	Game Details dialog
##############################################################################

dialog.game		= Szczeg�y gry
dialog.game.tab.1	= Wydarzenie
dialog.game.tab.2	= Gracze
dialog.game.tab.3	= Wi�cej

dialog.details.event 	= Wydarzenie:
dialog.details.site 	= Miejscowo��:
dialog.details.date 	= Data:
dialog.details.eventdate = Data wydarzenia:
dialog.details.round 	= Runda:
dialog.details.board 	= Szachownica:

dialog.details.white 	= Bia�e
dialog.details.black 	= Czarne
dialog.details.name 	= Nazwisko:
dialog.details.elo 	= ELO:
dialog.details.title 	= Tytu�:
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

dialog.setup.clear	= Wyczy��
dialog.setup.initial	= Pozycja wyj�ciowa
dialog.setup.copy	= Kopiuj z g��wnego panelu

dialog.setup.next.white	= Ruch bia�ych
dialog.setup.next.black	= Ruch czarnych
dialog.setup.move.no	= Numer ruchu

dialog.setup.castling		= Roszada
dialog.setup.castling.wk	= Bia�e 0-0
dialog.setup.castling.wk.tip	= Roszada na skrzydle kr�lewskim bia�ych
dialog.setup.castling.wq	= Bia�e 0-0-0
dialog.setup.castling.wq.tip	= Roszada na skrzydle hetma�skim bia�ych
dialog.setup.castling.bk	= Czarne 0-0
dialog.setup.castling.bk.tip	= Roszada na skrzydle kr�lewskim czarnych
dialog.setup.castling.bq	= Czarne 0-0-0
dialog.setup.castling.bq.tip	= Roszada na skrzydle hetma�skim czarnych
dialog.setup.invalid.fen    = Nieprawid�owy zapis FEN.

##############################################################################
#	About dialog
##############################################################################

dialog.about.tab.1	= jose
dialog.about.tab.2	= Baza danych
dialog.about.tab.3	= Wsp�pracownicy
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

dialog.about.3	=	<b>T�umacze:</b><br>\
			Frederic Raimbault, Jos� de Paula, \
			Agust�n Gomila, Alex Coronado, \
			Harold Roig, Hans Eriksson, \
			Guido Grazioli, Tomasz Sok�, \
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
			Andreas G�ttinger, Randy Countryman


dialog.about.4 =	Wersja Javy: %java.version% (%java.vendor%) <br>\
					Java VM: %java.vm.version% %java.vm.info% (%java.vm.vendor%) <br>\
					Runtime: %java.runtime.name% %java.runtime.version% <br>\
					�rodowisko graficzne: %java.awt.graphicsenv% <br>\
					Zestaw narz�dzi AWT: %awt.toolkit%<br>\
					�cie�ka domowa: %java.home%<br>\
					<br>\
					Ca�kowita pami��: %maxmem%<br>\
					Dost�pna pami��: %freemem%<br>\
					<br>\
					System operacyjny: %os.name%  %os.version% <br>\
					Architektura systemu: %os.arch%

dialog.about.5.no3d = Java3D jest dost�pna
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
dialog.export.preview = Podgl�d
dialog.export.browser = Podgl�d w przegl�darce

dialog.export.paper = Strona
dialog.export.orientation = Orientacja
dialog.export.margins = Marginesy

dialog.export.paper.format = Papier:
dialog.export.paper.size = Powi�kszenie:

dialog.print.custom.paper = U�ytkownika

dialog.export.margin.top = G�rny:
dialog.export.margin.bottom = Dolny:
dialog.export.margin.left = Lewy:
dialog.export.margin.right = Prawy:

dialog.export.ori.port = Pionowa
dialog.export.ori.land = Pozioma

dialog.export.games.0 = Nie wybra�e� �adnych gier do drukowania.
dialog.export.games.1 = Wybra�e� <b>jedn� gr�</b> do drukowania.
dialog.export.games.n = Wybra�e� <b>%n% gry/gier</b> do drukowania.
dialog.export.games.? = Wybra�e� <b>nieznan�</b> ilo�� gier do drukowania.

dialog.export.confirm = Czy jeste� pewien?
dialog.export.yes = Drukuj wszystkie

# xsl stylesheet options

export.pgn = Plik PGN
export.pgn.tip = Eksportuj gry jako plik Portable Game Notation.

print.awt = Drukuj
print.awt.tip = Drukuj gry z ekranu.<br> \
				    <li>Naci�nij <b>drukuj...</b> aby drukowa� na drukarce domy�lnej \
				    <li>Naci�nij <b>podgl�d</b> aby obejrze� dokument

xsl.html = HTML<br>Strona internetowa
xsl.html.tip = Utw�rz stron� internetow� (plik HTML).<br> \
				   <li>Naci�nij <b>zapisz jako...</b> aby zapisa� plik na dysku \
				   <li>Naci�nij <b>podgl�d w przegl�darce</b> aby obejrze� stron� w przegl�darce internetowej

xsl.dhtml = Dynamiczna<br>strona internetowa
xsl.dhtml.tip = Utw�rz stron� internetow� <i>z dynamicznymi</i> efektami.<br> \
				   <li>Naci�nij <b>zapisz jako...</b> aby zapisa� plik na dysku \
				   <li>Naci�nij <b>podgl�d w przegl�darce</b> aby obejrze� stron� w przegl�darce internetowej <br> \
				  JavaScript musi by� w��czony w przegl�darce internetowej.

xsl.debug = Plik XML<br>(debug)
export.xml.tip = Utw�rz plik XML dla debugowania.

xsl.pdf = Drukuj PDF
xsl.pdf.tip = Utw�rz albo drukuj plik PDF.<br> \
				<li>Naci�nij <b>zapisz jako...</b> aby zapisa� plik na dysku \
				<li>Naci�nij <b>drukuj...</b> aby wydrukowa� dokument \
				<li>Naci�nij <b>podgl�d</b> aby obejrze� dokument

xsl.tex = Plik TeX
xsl.tex.tip = Utw�rz plik dla przetwarzania TeX.

xsl.html.figs.tt = Bierki TrueType
xsl.html.figs.img = Bierki graficznie
xsl.css.standalone = Dla danych CSS utw�rz osobny plik

xsl.html.img.dir = �cie�ka
xsl.create.images = Utw�rz obraz

xsl.pdf.embed = Osad� czcionki TrueType
xsl.pdf.font.dir = Dodatkowe czcionki:

default.file.name = Partia


##############################################################################
#	Print Preview Dialog
##############################################################################

print.preview.page.one =
print.preview.page.two =
print.preview.page.one.tip = Poka� jedn� stron�
print.preview.page.two.tip = Poka� dwie strony

print.preview.ori.land =
print.preview.ori.port =
print.preview.ori.land.tip = U�yj poziomej orientacji papieru
print.preview.ori.port.tip = U�yj pionowej orientacji papieru

print.preview.fit.page = Ca�a strona
print.preview.fit.width = Szeroko�� strony
print.preview.fit.textwidth = Szeroko�� tekstu

print.preview.next.page =
print.preview.previous.page =

preview.wait = Zaczekaj chwil�...

##############################################################################
#	Online Update Dialog
##############################################################################

online.update.title	= Aktualizacja online
online.update.tab.1	= Nowa wersja
online.update.tab.2	= Co nowego?
online.update.tab.3 	= Wa�ne uwagi

update.install	= Pobierz i zainstaluj teraz
update.download	= Pobierz teraz, zainstaluj p�niej
update.mirror	= Pobierz ze strony lustrzanej

download.file.progress 			= Pobieranie %file%
download.file.title			= Pobierz
download.error.invalid.url		= Nieprawid�owy URL: %p%.
download.error.connect.fail		= Nieudane po��czenie z %p%.
download.error.parse.xml		= B��d odczytu: %p%.
download.error.version.missing	= Nie mo�na odczyta� wersji z %p%.
download.error.os.missing		= Brak pliku dla twojego systemu operacyjnego.
download.error.browser.fail		= Nie mo�na wy�wietli� %p%.
download.error.update			= Podczas aktualizacji jose wyst�pi� b��d.\n R�cznie uaktualnij aplikacj�.

download.message.up.to.date		= Masz zainstalowan� najnowsz� wersj�.
download.message.success		= Aktualizacja jose do wersji %p% zako�czy�a si� sukcesem \n. Ponownie uruchom aplikacj�.

download.message			= \
  Wersja <b>%version%</b> jest dost�pna na <br>\
  <font color=blue>%url%</font><br>\
  Rozmiar: %size%

dialog.browser		= Uruchom przegl�dark� HTML.\n Wpisz komend�, kt�ra pozwoli uruchomi� przegl�dark�.
dialog.browser.title 	= Lokalizacja przegl�darki

# deprecated
#online.report.title	= Raport
#online.report.bug	= Zg�o� b��d
#online.report.feature	= Feature Request
#online.report.support	= Support Request

#online.report.type	= Rodzaj:
#online.report.subject	= Temat:
#online.report.description	= Opis:
#online.report.email		= E-mail:

#online.report.default.subject		= <Subject>
#online.report.default.description	= <Please try to describe the steps that caused the error>
#online.report.default.email		= <your e-mail address - optional>
#online.report.info			= Raport zostanie wys�any do http://jose-chess.sourceforge.net

#online.report.success		= Tw�j raport zosta� przed�o�ony.
#online.report.failed		= Nie mo�na przed�o�y� twojego raportu.
# deprecated


##########################################
# 	Error Messages
##########################################

error.not.selected 		= Wybierz gr� do zapisania.
error.duplicate.database.access = Nie uruchamiaj naraz dw�ch aplikacji jose \n\
	korzystaj�cych z tej samej bazy danych.\n\n\
	Takie post�powanie mo�e spowodowa� utrat� danych. \n\
	Zako�cz jedn� aplikacj� jose.
error.lnf.not.supported 	= Ten interfejs nie jest dost�pny \n na bie��cej platformie.

error.bad.uci = Wydaje si�, �e nie jest to silnik UCI.\n Czy jeste� pewien?

error.bug	= <center><b>Wyst�pi� niespodziewany b��d.</b></center><br><br> \
 Pomocne mo�e by� przed�o�enie raportu o b��dach.<br>\
 Opisz kroki, kt�re doprowadzi�y do powstania b��du <br>\
 i do��cz do opisu nast�puj�cy plik: <br>\
  <center><b> %error.log% </b></center>

# errors in setup dialog

pos.error.white.king.missing	= Brakuje bia�ego kr�la.
pos.error.black.king.missing	= Brakuje czarnego kr�la.
pos.error.too.many.white.kings	= Za du�o bia�ych kr�li.
pos.error.too.many.black.kings	= Za du�o czarnych kr�li.
pos.error.white.king.checked	= Bia�y kr�l nie mo�e by� w szachu.
pos.error.black.king.checked	= Czarny kr�l nie mo�e by� w szachu.
pos.error.white.pawn.base		= Bia�e piony nie mog� by� umieszczone na pierwszej linii.
pos.error.white.pawn.promo		= Bia�e piony nie mog� by� umieszczone na �smej linii.
pos.error.black.pawn.base		= Czarne piony nie mog� by� umieszczone na �smej linii.
pos.error.black.pawn.promo		= Czarne piony nie mog� by� umieszczone na pierwszej linii.
pos.warning.too.many.white.pieces	= Za du�o bia�ych bierek.
pos.warning.too.many.black.pieces	= Za du�o czarnych bierek.
pos.warning.too.many.white.pawns	= Za du�o bia�ych pion�w.
pos.warning.too.many.black.pawns	= Za du�o czarnych pion�w.
pos.warning.too.many.white.knights	= Za du�o bia�ych skoczk�w.
pos.warning.too.many.black.knights	= Za du�o czarnych skoczk�w.
pos.warning.too.many.white.bishops	= Za du�o bia�ych go�c�w.
pos.warning.too.many.black.bishops	= Za du�o czarnych go�c�w.
pos.warning.too.many.white.rooks	= Za du�o bia�ych wie�.
pos.warning.too.many.black.rooks	= Za du�o czarnych wie�.
pos.warning.too.many.white.queens	= Za du�o bia�ych hetman�w.
pos.warning.too.many.black.queens	= Za du�o czarnych hetman�w.
pos.warning.strange.white.bishops	= Bia�e go�ce na jednobarwnych polach?
pos.warning.strange.black.bishops	= Czarne go�ce na jednobarwnych polach?


##############################################################################
#	Style Names
##############################################################################


base			= Og�lne
header			= Informacje o grze
header.event	= Wydarzenie
header.site		= Miejscowo��
header.date		= Data
header.round	= Runda
header.white	= Bia�e
header.black	= Czarne
header.result	= Wynik gry
body			= Zapis gry
body.line		= Ruchy
body.line.0		= G��wna linia
body.line.1		= Warianty
body.line.2		= Podwarianty
body.line.3		= 2. podwariant
body.line.4		= 3. podwariant
body.symbol		= Symbole
body.inline		= Diagram tekstowy
body.figurine	= Bierki
body.figurine.0	= G��wna linia
body.figurine.1	= Wariant
body.figurine.2	= Podwariant
body.figurine.3	= 2. podwariant
body.figurine.4	= 3. podwariant
body.comment	= Komentarze
body.comment.0	= G��wna linia
body.comment.1	= Wariant
body.comment.2	= Podwariant
body.comment.3	= 2. podwariant
body.comment.4	= 3. podwariant
body.result		= Wynik
html.large      = Diagram na stronie internetowej


##############################################################################
#	Task Dialogs (progress)
##############################################################################

dialog.progress.time 		= Pozosta�o: %time%

dialog.read-progress.title 	= jose � otwieranie pliku
dialog.read-progress.text 	= Otwieranie %fileName%

dialog.eco 			= Klasyfikuj ECO
dialog.eco.clobber.eco 		= Nadpisz kody ECO
dialog.eco.clobber.name 	= Nadpisz otwieranie nazwy
dialog.eco.language 		= J�zyk:


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
fig.ru = �����"��"
fig.ukr = �����"��"
# please note that Russians use the latin alphabet for files (a,b,c,d,e,f,g,h)
# (so we don't have to care about that ;-)

##############################################################################
#	foreign language names
##############################################################################

lang.cs = czeski
lang.da = du�ski
lang.nl = holenderski
lang.en = angielski
lang.et = esto�ski
lang.fi = fi�ski
lang.fr = francuski
lang.de = niemiecki
lang.hu = w�gierski
lang.is = islandzki
lang.it = w�oski
lang.no = norweski
lang.pl = polski
lang.pt = portugalski
lang.ro = rumu�ski
lang.es = hiszpa�ski
lang.ca = katalo�ski
lang.sv = szwedzki
lang.ru = rosyjski
lang.he = hebrajski
lang.tr = turecki
lang.ukr = ukrai�ski


##############################################################################
#	PGN Annotations
##############################################################################

pgn.nag.0    = uniewa�nij przypisy
pgn.nag.1    	= !
pgn.nag.1.tip	= dobry ruch
pgn.nag.2	= ?
pgn.nag.2.tip	= z�y ruch
pgn.nag.3    	= !!
pgn.nag.3.tip	= bardzo dobry ruch
pgn.nag.4    	= ??
pgn.nag.4.tip	= bardzo z�y ruch
pgn.nag.5    	= !?
pgn.nag.5.tip	= interesuj�cy ruch
pgn.nag.6    	= ?!
pgn.nag.6.tip	= w�tpliwy ruch
pgn.nag.7    = mocny ruch
pgn.nag.7.tip = mocny ruch (ka�dy inny szybko spowoduje straty)
pgn.nag.8    = osobliwy ruch
pgn.nag.8.tip    = osobliwy ruch (niezbyt racjonalny)
pgn.nag.9    = najgorszy ruch
pgn.nag.10      = =
pgn.nag.10.tip  = pozycja remisowa
pgn.nag.11      = =
pgn.nag.11.tip  = wyr�wnane szanse, bierna pozycja
pgn.nag.12.tip   = wyr�wnane szanse, aktywna pozycja
pgn.nag.13   = niejasne
pgn.nag.13.tip   = niejasna pozycja
pgn.nag.14	= +=
pgn.nag.14.tip	= Bia�e maj� nieznaczn� przewag�
pgn.nag.15   	= =+
pgn.nag.15.tip 	= Czarne maj� nieznaczn� przewag�
pgn.nag.16	= +/-
pgn.nag.16.tip 	= Bia�e maj� umiarkowan� przewag�
pgn.nag.17	= -/+
pgn.nag.17.tip 	= Czarne maj� umiarkowan� przewag�
pgn.nag.18	= +-
pgn.nag.18.tip  = Bia�e maj� zdecydowan� przewag�
pgn.nag.19	= -+
pgn.nag.19.tip 	= Czarne maj� zdecydowan� przewag�
pgn.nag.20   = Bia�e maj� mia�d��c� przewag� (Czarne powinny si� podda�)
pgn.nag.21   = Czarne maj� mia�d��c� przewag� (Bia�e powinny si� podda�)
pgn.nag.22   = Przymusowy ruch bia�ych (zugzwang)
pgn.nag.23   = Przymusowy ruch czarnych (zugzwang)
pgn.nag.24   = Bia�e maj� nieznaczn� przewag� pola
pgn.nag.25   = Czarne maj� nieznaczn� przewag� pola
pgn.nag.26   = Bia�e maj� umiarkowan� przewag� pola
pgn.nag.27   = Czarne maj� umiarkowan� przewag� pola
pgn.nag.28   = Bia�e maj� zdecydowan� przewag� pola
pgn.nag.29   = Czarne maj� zdecydowan� przewag� pola
pgn.nag.30   = Bia�e maj� nieznaczn� (post�puj�c�) przewag� czasu
pgn.nag.31   = Czarne maj� nieznaczn� (post�puj�c�) przewag� czasu
pgn.nag.32   = Bia�e maj� umiarkowan� (post�puj�c�) przewag� czasu
pgn.nag.33   = Czarne maj� umiarkowan� (post�puj�c�) przewag� czasu
pgn.nag.34   = Bia�e maj� zdecydowan� (post�puj�c�) przewag� czasu
pgn.nag.35   = Czarne maj� zdecydowan� (post�puj�c�) przewag� czasu
pgn.nag.36   = Inicjatywa po stronie bia�ych
pgn.nag.37   = Inicjatywa po stronie czarnych
pgn.nag.38   = Sta�a inicjatywa po stronie bia�ych
pgn.nag.39   = Sta�a inicjatywa po stronie czarnych
pgn.nag.40   = Bia�e w ataku
pgn.nag.41   = Czarne w ataku
pgn.nag.42   = Bia�e nie maj� dostatecznej kompensaty strat materialnych
pgn.nag.43   = Czarne nie maj� dostatecznej kompensaty strat materialnych
pgn.nag.44   = Bia�e maj� dostateczn� kompensat� strat materialnych
pgn.nag.45   = Czarne maj� dostateczn� kompensat� strat materialnych
pgn.nag.46   = Kompensata bia�ych jest wi�ksza ni� straty materialne
pgn.nag.47   = Kompensata czarnych jest wi�ksza ni� straty materialne
pgn.nag.48   = Nieznaczna przewaga bia�ych w kontroli centrum
pgn.nag.49   = Nieznaczna przewaga czarnych w kontroli centrum
pgn.nag.50   = Umiarkowana przewaga bia�ych w kontroli centrum
pgn.nag.51   = Umiarkowana przewaga czarnych w kontroli centrum
pgn.nag.52   = Zdecydowana przewaga bia�ych w kontroli centrum
pgn.nag.53   = Zdecydowana przewaga czarnych w kontroli centrum
pgn.nag.54   = Nieznaczna przewaga bia�ych w kontroli skrzyd�a kr�lewskiego
pgn.nag.55   = Nieznaczna przewaga czarnych w kontroli skrzyd�a kr�lewskiego
pgn.nag.56   = Umiarkowana przewaga bia�ych w kontroli skrzyd�a kr�lewskiego
pgn.nag.57   = Umiarkowana przewaga czarnych w kontroli skrzyd�a kr�lewskiego
pgn.nag.58   = Zdecydowana przewaga bia�ych w kontroli skrzyd�a kr�lewskiego
pgn.nag.59   = Zdecydowana przewaga czarnych w kontroli skrzyd�a kr�lewskiego
pgn.nag.60   = Nieznaczna przewaga bia�ych w kontroli skrzyd�a hetma�skiego
pgn.nag.61   = Nieznaczna przewaga czarnych w kontroli skrzyd�a hetma�skiego
pgn.nag.62   = Umiarkowana przewaga bia�ych w kontroli skrzyd�a hetma�skiego
pgn.nag.63   = Umiarkowana przewaga czarnych w kontroli skrzyd�a hetma�skiego
pgn.nag.64   = Zdecydowana przewaga bia�ych w kontroli skrzyd�a hetma�skiego
pgn.nag.65   = Zdecydowana przewaga czarnych w kontroli skrzyd�a hetma�skiego
pgn.nag.66   = Niezabezpieczona pierwsza linia bia�ych
pgn.nag.67   = Niezabezpieczona pierwsza linia czarnych
pgn.nag.68   = Zabezpieczona pierwsza linia bia�ych
pgn.nag.69   = Zabezpieczona pierwsza linia czarnych
pgn.nag.70   = Niewystarczaj�ca ochrona bia�ego kr�la
pgn.nag.71   = Niewystarczaj�ca ochrona czarnego kr�la
pgn.nag.72   = Dobra ochrona bia�ego kr�la
pgn.nag.73   = Dobra ochrona czarnego kr�la
pgn.nag.74   = Kiepskie umiejscowienie bia�ego kr�la
pgn.nag.75   = Kiepskie umiejscowienie czrnego kr�la
pgn.nag.76   = Dobre umiejscowienie bia�ego kr�la
pgn.nag.77   = Dobre umiejscowienie czarnego kr�la
pgn.nag.78   = Bardzo niedobry uk�ad bia�ych pion�w
pgn.nag.79   = Bardzo niedobry uk�ad czarnych pion�w
pgn.nag.80   = Bia�e maj� umiarkowanie z�y uk�ad pion�w
pgn.nag.81   = Czarne maj� umiarkowanie z�y uk�ad pion�w
pgn.nag.82   = Bia�e maj� umiarkowanie silny uk�ad pion�w
pgn.nag.83   = Czarne maj� umiarkowanie silny uk�ad pion�w
pgn.nag.84   = Bia�e maj� bardzo silny uk�ad pion�w
pgn.nag.85   = Czarne maj� bardzo silny uk�ad pion�w
pgn.nag.86   = Kiepskie umiejscowienie bia�ego skoczka
pgn.nag.87   = Kiepskie umiejscowienie czarnego skoczka
pgn.nag.88   = Dobre umiejscowienie bia�ego skoczka
pgn.nag.89   = Dobre umiejscowienie czarnego skoczka
pgn.nag.90   = Kiepskie umiejscowienie bia�ego go�ca
pgn.nag.91   = Kiepskie umiejscowienie czarnego go�ca
pgn.nag.92   = Dobre umiejscowienie bia�ego go�ca
pgn.nag.93   = Dobre umiejscowienie czarnego go�ca
pgn.nag.94   = Kiepskie umiejscowienie bia�ej wie�y
pgn.nag.95   = Kiepskie umiejscowienie czarnej wie�y
pgn.nag.96   = Dobre umiejscowienie bia�ej wie�y
pgn.nag.97   = Dobre umiejscowienie czarnej wie�y
pgn.nag.98   = Kiepskie umiejscowienie bia�ego hetmana
pgn.nag.99   = Kiepskie umiejscowienie czarnego hetmana
pgn.nag.100  = Dobre umiejscowienie bia�ego hetmana
pgn.nag.101  = Dobre umiejscowienie czarnego hetmana
pgn.nag.102  = Niewystarczaj�ca koordynacja bia�ych bierek
pgn.nag.103  = Niewystarczaj�ca koordynacja czarnych bierek
pgn.nag.104  = Dobra koordynacja bia�ych bierek
pgn.nag.105  = Dobra koordynacja czarnych bierek
pgn.nag.106  = Bia�e bardzo �le rozegra�y otwarcie
pgn.nag.107  = Czarne bardzo �le rozegra�y otwarcie
pgn.nag.108  = Bia�e �le rozegra�y otwarcie
pgn.nag.109  = Czarne �le rozegra�y otwarcie
pgn.nag.110  = Bia�e dobrze rozegra�y otwarcie
pgn.nag.111  = Czarne dobrze rozegra�y otwarcie
pgn.nag.112  = Bia�e bardzo dobrze rozegra�y otwarcie
pgn.nag.113  = Czarne bardzo dobrze rozegra�y otwarcie
pgn.nag.114  = Bia�e bardzo �le rozegra�y �rodkow� cz�� gry
pgn.nag.115  = Czarne bardzo �le rozegra�y �rodkow� cz�� gry
pgn.nag.116  = Bia�e �le rozegra�y �rodkow� cz�� gry
pgn.nag.117  = Czarne �le rozegra�y �rodkow� cz�� gry
pgn.nag.118  = Bia�e dobrze rozegra�y �rodkow� cz�� gry
pgn.nag.119  = Czarne dobrze rozegra�y �rodkow� cz�� gry
pgn.nag.120  = Bia�e bardzo dobrze rozegra�y �rodkow� cz�� gry
pgn.nag.121  = Czarne bardzo dobrze rozegra�y �rodkow� cz�� gry
pgn.nag.122  = Bia�e bardzo �le rozegra�y ko�c�wk�
pgn.nag.123  = Czarne bardzo �le rozegra�y ko�c�wk�
pgn.nag.124  = Bia�e �le rozegra�y ko�c�wk�
pgn.nag.125  = Czarne �le rozegra�y ko�c�wk�
pgn.nag.126  = Bia�e dobrze rozegra�y ko�c�wk�
pgn.nag.127  = Czarne dobrze rozegra�y ko�c�wk�
pgn.nag.128  = Bia�e bardzo dobrze rozegra�y ko�c�wk�
pgn.nag.129  = Czarne bardzo dobrze rozegra�y ko�c�wk�
pgn.nag.130  = Bia�e w nieznacznym przeciwnatarciu
pgn.nag.131  = Czarne w nieznacznym przeciwnatarciu
pgn.nag.132  = Bia�e w umiarkowanym przeciwnatarciu
pgn.nag.133  = Czarne w umiarkowanym przeciwnatarciu
pgn.nag.134  = Bia�e w zdecydowanym przeciwnatarciu
pgn.nag.135  = Czarne w zdecydowanym przeciwnatarciu
pgn.nag.136  = Bia�e w umiarkowanym niedoczasie
pgn.nag.137  = Czarne w umiarkowanym niedoczasie
pgn.nag.138  = Bia�e w dotkliwym niedoczasie
pgn.nag.139  = Czarne w dotkliwym niedoczasie

# following codes are defined by Fritz or SCID

pgn.nag.140  	= Pomys�
pgn.nag.141  	= Przeciwko
pgn.nag.142  	= Jest lepsze
pgn.nag.143  	= Jest gorsze
pgn.nag.144  	= =
pgn.nag.144.tip = Jest r�wnowa�ne
pgn.nag.145  	= RR
pgn.nag.145.tip	= Notka redakcyjna
pgn.nag.146  	= N
pgn.nag.146.tip = Innowacja
pgn.nag.147     = S�aby punkt
pgn.nag.148     = Ko�c�wka
pgn.nag.149     = Linia
pgn.nag.150		= Przek�tna
pgn.nag.151		= Bia�e maj� par� go�c�w
pgn.nag.152     = Czarne maj� par� go�c�w
pgn.nag.153		= Go�ce na dwubarwnych polach
pgn.nag.154		= Go�ce na jednobarwnych polach

# following codes are defined by us (equivalent to Informator symbols)
# (is there a standard definition for these symbols ?)

pgn.nag.156		= Wolny pion
pgn.nag.157		= Wi�cej pion�w
pgn.nag.158		= Z
pgn.nag.159		= Bez
pgn.nag.161		= Patrz
pgn.nag.163		= Szereg

# defined by SCID:
pgn.nag.190		= Itd.
pgn.nag.191		= Zdublowane piony
pgn.nag.192		= Odizolowane piony
pgn.nag.193		= Powi�zane piony
pgn.nag.194     = Zawieszone piony
pgn.nag.195     = Sp�nione piony

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