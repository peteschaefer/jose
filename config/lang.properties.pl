file.encoding=Windows-1250

##############################################################################
#	Polish translation for jose 1.3.4 RC 1 by Tomasz Sokó³ (progtom@wp.pl)
##############################################################################

#	Application Name
application.name	= jose

#	Frame Titles
window.board	= Szachownica
window.console	= Konsola silnika
window.database	= Baza danych
window.filter	= Filtr
window.list	= Lista posuniêæ
window.clock	= Zegar
window.game	= Gra
window.engine	= Silnik
window.eval     = Szacowanie profilu

window.collectionlist	= Baza danych
window.query		= ZnajdŸ
window.gamelist		= Baza danych

window.sqlquery		= Zapytanie SQL
window.sqllist		= Wynik

window.toolbar.1	= Pasek narzêdzi 1
window.toolbar.2	= Pasek narzêdzi 2
window.toolbar.3	= Pasek narzêdzi 3
window.toolbar.symbols	= Przypisy
window.help		        = Pomoc
window.print.preview    = Podgl¹d wydruku

# dialog titles

dialog.option	= Opcje
dialog.about	= Informacje
dialog.animate  = Animacja
dialog.setup	= Ustawienia
dialog.message.title = Wiadomoœæ

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
menu.file.new.tip	= Rozpocznij now¹ grê
menu.file.new.frc = Nowy FRC
menu.file.open		= Otwórz...
menu.file.open.tip  	= Otwórz plik PGN
menu.file.open.url	= Otwórz URL...
menu.file.close		= Zamknij
menu.file.close.tip	= Zamknij bie¿¹ce okno
menu.file.save		= Zapisz
menu.file.save.tip  	= Zapisz bie¿¹c¹ grê w bazie danych
menu.file.save.as    	= Zapisz jako...
menu.file.save.as.tip	= Zapisz bie¿¹c¹ grê w nowej kopii w bazie danych
menu.file.save.all   	= Zapisz wszystko
menu.file.save.all.tip  = Zapisz wszystkie otwarte gry w bazie danych
menu.file.revert	= Przywróæ grê
menu.file.print		= Drukuj...
menu.file.print.tip	= Drukuj bie¿¹c¹ grê
menu.file.print.setup	= Ustawienia strony...
menu.file.print.setup.tip = Ustawienia drukarki i rozmiar papieru
menu.file.print.preview = Podgl¹d wydruku...
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
menu.edit.copy.fen.tip = Kopiuj bie¿¹c¹ pozycjê do schowka (w notacji FEN)
menu.edit.copy.img  = Diagram graficzny i t³o
menu.edit.copy.img.tip = Kopiuj bie¿¹c¹ pozycjê do schowka (jako obraz)
menu.edit.copy.imgt  = Diagram graficzny
menu.edit.copy.imgt.tip = Kopiuj bie¿¹c¹ pozycjê do schowka (jako obraz)
menu.edit.copy.text  = Diagram tekstowy
menu.edit.copy.text.tip = Kopiuj bie¿¹c¹ pozycjê do schowka (jako tekst)

menu.edit.copy.pgn  = Kopiuj PGN
menu.edit.copy.pgn.tip = Kopiuj bie¿¹c¹ grê do schowka (jako tekst PGN)
menu.edit.paste		= Wklej
menu.edit.paste.tip = Wklej ze schowka
menu.edit.paste.copy		= Wklej kopie
menu.edit.paste.copy.tip 	= Wklej gry ze schowka
menu.edit.paste.same 	= Wklej gry
menu.edit.paste.same.tip = Przenieœ gry ze schowka
menu.edit.paste.pgn = Wklej PGN
menu.edit.paste.pgn.tip = Wstaw grê ze schowka (jako tekst PGN)
menu.edit.clear		= Wyczyœæ
menu.edit.option	= Opcje...
menu.edit.option.tip	= Otwórz okno dialogowe opcji

menu.edit.games			= Baza danych
menu.edit.collection.new 	= Nowy folder
menu.edit.collection.rename 	= Zmieñ nazwê
menu.edit.collection.crunch = Dostosuj
menu.edit.collection.crunch.tip = Dostosuj kolumnê indeksu
menu.edit.empty.trash		= Wyczyœæ kosz
menu.edit.restore		= Przywróæ

#menu.edit.position.index    = Uaktualnij listê posunieæ
menu.edit.search.current    = Szukaj pozycji
menu.edit.ecofy             = Klasyfikuj ECO

menu.edit.style = Styl tekstu
menu.edit.bold = Pogrubiony
menu.edit.italic = Kursywa
menu.edit.underline = Podkreœlony
menu.edit.plain = Normalny
menu.edit.left = Wyrównany do lewej
menu.edit.center = wyœrodkowany
menu.edit.right = Wyrównany do prawej
menu.edit.larger = Zwiêksz rozmiar czcionki
menu.edit.smaller = Zmniejsz rozmiar czcionki
menu.edit.color = Kolor czcionki

# Game Menu

menu.game		= Gra
menu.game.details	= Szczegó³y...
menu.game.analysis  	= Tryb analizy
menu.game.navigate	= PrzejdŸ do...
menu.game.time.controls = Partia
menu.game.time.control = Partia
menu.game.details.tip 	= Edytuj szczegó³y gry (gracze, itd.)
menu.game.hint		= Podpowiedz
menu.game.hint.tip  = Poka¿ podpowiedŸ
menu.game.draw		= Przyjmij ruch
menu.game.resign	= Poddajê
menu.game.2d		= Widok 2D
menu.game.3d		= Widok 3D
menu.game.flip		= Obróæ szachownicê
menu.game.coords	= Wspó³rzêdne
menu.game.coords.tip	= Pokazuj wspó³rzêdne
menu.game.animate 	= Animacja...
menu.game.previous 	= Poprzednia zak³adka
menu.game.next 		= Nastêpna zak³adka
menu.game.close 	= Zamknij
menu.game.close.tip 	= Zamknij bie¿¹c¹ grê
menu.game.close.all 	= Zamknij wszystkie
menu.game.close.all.tip = Zamknij wszystkie otwarte gry
menu.game.close.all.but = Zamknij wszystkie oprócz bie¿¹cej
menu.game.close.all.but.tip = Zamknij wszystkie otwarte gry oprócz bie¿¹cej
menu.game.setup		= Ustaw pozycjê

menu.game.copy.line = Kopiuj liniê
menu.game.copy.line.tip = Kopiuj tê liniê do schowka
menu.game.paste.line = Wklej liniê
menu.game.paste.line.tip = Wstaw tê liniê do bie¿¹cej gry

# Window Menu

menu.window		= Okno
menu.window.fullscreen 	= Widok pe³noekranowy
menu.window.reset   	= Odœwie¿ okno

# Help Menu

menu.help		= Pomoc
menu.help.splash	= O jose...
menu.help.about		= Informacje...
menu.help.license	= Licencja...
menu.help.context   	= Pomoc kontekstowa
menu.help.manual    	= Podrêcznik
menu.help.web		= jose w Internecie

menu.web.home		= Strona domowa
menu.web.update		= Aktualizacja online
menu.web.download	= Pobierz
menu.web.report		= Zg³oœ b³¹d
menu.web.support	= Proœba o pomoc
menu.web.feature	= Proœba o przedstawienie
menu.web.forum		= Forum
menu.web.donate     = Wesprzyj
menu.web.browser	= Wybierz przegl¹darkê...


##############################################################################
# 	Context Menu
##############################################################################

panel.hide		= Ukryj
panel.hide.tip		= Ukryj ten panel
panel.undock		= Nowe okno
panel.undock.tip	= Otwórz ten panel w osobnym oknie
panel.move		= Przesuñ
panel.move.tip		= Przesuñ ten panel w nowe po³o¿enie
panel.dock		= Dokuj
panel.dock.tip		= Dokuj to okno

panel.orig.pos = Pierwotne po³o¿enie
panel.dock.here = Dokuj tutaj
panel.undock.here = Oddokuj

#################
# Document Panel
#################

# deprecated:
tab.place 	= Umiejscowienie zak³adek
tab.place.top 	= U góry
tab.place.left 	= Po lewej
tab.place.bottom = Na dole
tab.place.right = Po prawej
#

tab.layout 		= Uk³ad zak³adek
tab.layout.wrap 	= Piêtra
tab.layout.scroll 	= Szereg

doc.menu.annotate 	= Przypisy
doc.menu.delete.comment = Usuñ komentarz
doc.menu.line.promote 	= Awansuj liniê
doc.menu.line.delete 	= Usuñ liniê
doc.menu.line.cut 	= Odetnij liniê
doc.menu.line.uncomment = Usuñ wszystkie komentarze
doc.menu.remove.annotation = -None-
doc.menu.more.annotations = Wiêcej...

tab.untitled 	= Bez tytu³u
confirm 	= Potwierdzenie
confirm.save.one = Czy zapisaæ bie¿¹c¹ grê?
confirm.save.all = Czy zapisaæ zmodyfikowane gry?

dialog.confirm.save = Zapisz
dialog.confirm.dont.save = Nie zapisuj

dialog.engine.offers.draw = %engine% proponuje ruch.

dialog.accept.draw = Akceptuj ruch
dialog.decline.draw = Odrzuæ ruch

dialog.autoimport.title = Importuj
dialog.autoimport.ask = Plik ^0 zmieni³ siê na dysku. \n Czy ponownie go otworzyæ?

dialog.paste.message = Zamierzasz wstawiæ dane ze schowka. \n\
     Czy chcesz przesun¹æ gry, czy utworzyæ now¹ kopiê?
dialog.paste.title = Wklej gry
dialog.paste.same = Przesuñ
dialog.paste.copy = Kopiuj

###################
# Game Navigation
###################

move.first	= Pocz¹tek gry
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
engine.thinking.tip 	= %engine% myœli nad nastêpnym ruchem
engine.pondering.tip 	= %engine% rozwa¿a twój nastêpny ruch
engine.analyzing.tip 	= %engine% analizuje
engine.hint.tip 	= PodpowiedŸ: %move%

engine.paused.title 	= %engine%
engine.thinking.title 	= %engine% myœli
engine.pondering.title 	= %engine% rozwa¿a
engine.analyzing.title 	= %engine% analizuje

plugin.name 		= %name% %version%
plugin.name.author 	= %name% %version%. Autor %author%

plugin.book.move 	= BK
plugin.book.move.tip 	= Zbiór ruchów
plugin.hash.move 	= HT
plugin.hash.move.tip 	= Szacowany ze zbioru ró¿nych pozycji
plugin.tb.move 		= TB
plugin.tb.move.tip 	= Szacowany ze zbioru koñcówek

plugin.currentmove.title       = Ruch
plugin.depth.title      = G³êbokoœæ
plugin.elapsed.time.title  = Czas
plugin.nodecount.title  = Warianty
plugin.nps.title        = W/sek.

plugin.currentmove = %move%
plugin.currentmove.max = %move% %moveno%/%maxmove%

plugin.currentmove.tip = Aktualnie szacowany ruch %move%.
plugin.currentmove.max.tip = Aktualnie szacowany ruch %move%. (nr %moveno% z %maxmove%)

plugin.depth 		= %depth%
plugin.depth.tip 	= G³êbokoœæ szukania: Warstwa %depth%.

plugin.depth.sel 	= %depth% (%seldepth%)
plugin.depth.sel.tip 	= G³êbokoœæ szukania: %depth%. warstwa. G³êbokoœæ wybiórcza: %seldepth%. warstwa

plugin.white.mates 	= +#%eval%
plugin.white.mates.tip 	= Bia³e matuj¹ w %eval% ruchach
plugin.black.mates 	= -#%eval%
plugin.black.mates.tip 	= Czarne matuj¹ w %eval% ruchach

plugin.evaluation 	= %eval%
plugin.evaluation.tip 	= Wartoœæ pozycji: %eval%

plugin.line.tip = Szacowanie zakresu

plugin.elapsed.time = %time%
plugin.elapsed.time.tip = Up³yw czasu dla tego obliczenia.

plugin.nodecount 	= %nodecount%
plugin.nodecount.tip 	= %nodecount% oszacowanych pozycji

plugin.nps      = %nps%
plugin.nps.tip  = %nps% szacowanych wariantów na sekundê

plugin.pv.history = Tryb eksperta

restart.plugin		= Ponownie uruchom silnik


######################
# Board Panel
######################

wait.3d = £adowanie 3D. Proszê czekaæ...

message.result			= Wynik
message.white 			= Bia³e
message.black 			= Czarne
message.mate 			= Mat. \n %player% wygra³.
message.stalemate		= Pat. \n Remis.
message.draw3			= Pozycja powtórzona 3 razy. \n Remis.
message.draw50			= Bez zmiany w bierkach przez 50 ruchów. \n Remis.
message.resign			= %player% podda³. \n Wygra³eœ.
message.time.draw		= Up³yn¹³ czas. \n Remis.
message.time.lose		= Up³yn¹³ czas. \n %player% wygra³.


################
# Clock Panel
################

clock.mode.analog	= Analogowy
clock.mode.analog.tip 	= Poka¿ zegar analogowy
clock.mode.digital	= Cyfrowy
clock.mode.digital.tip 	= Poka¿ zegar cyfrowy


##############################################################################
#	Dialogs
##############################################################################

dialog.button.ok		= OK
dialog.button.ok.tip		= Uaktywnij wprowadzone zmiany
dialog.button.cancel		= Anuluj
dialog.button.cancel.tip	= Zamknij okno dialogowe bez uaktywniania wprowadzonych zmian
dialog.button.apply		= ZatwierdŸ
dialog.button.apply.tip		= Natychmiast zatwierdŸ wprowadzone zmiany
dialog.button.revert		= Przywróæ
dialog.button.revert.tip	= Przywróæ poprzednie ustawienia
dialog.button.clear		= Wyczyœæ
dialog.button.delete		= Usuñ
dialog.button.yes		= Tak
dialog.button.no		= Nie
dialog.button.next		= Dalej
dialog.button.back		= Wstecz
dialog.button.close		= Zamknij
dialog.button.help		= Pomoc
dialog.button.help.tip		= Poka¿ tematy pomocy

dialog.button.commit		= Przyjmij
dialog.button.commit.tip	= Naciœnij tutaj, aby przyj¹æ uaktualnienieClick here to commit updates
dialog.button.rollback		= Wycofaj
dialog.button.rollback.tip	= Naciœnij tutaj, aby odrzuciæ uaktualnienie

dialog.error.title		= B³¹d

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

filechooser.overwrite 	= Nadpisaæ istniej¹cy "%file.name%"?
filechooser.do.overwrite = Nadpisz

#################
# Color Chooser
#################

colorchooser.texture	= Tekstura
colorchooser.preview	= Podgl¹d
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
dialog.option.user.language	= Jêzyk
dialog.option.ui.look.and.feel	= Interfejs

doc.load.history	= £aduj ostatnio otwarte gry
doc.classify.eco	= Klasyfikuj otwieranie przez ECO

dialog.option.animation = Animacja
dialog.option.animation.speed = Szybkoœæ

dialog.option.doc.write.mode	= Wstaw nowy ruch
write.mode.new.line		= Nowa linia
write.mode.new.main.line	= Nowa g³ówna linia
write.mode.overwrite		= Nadpisz
write.mode.ask			= Pytaj
write.mode.dont.ask		= Nie pytaj wiêcej
write.mode.cancel		= Anuluj

board.animation.hints   = Poka¿ drogê podczas animacji

dialog.option.sound = Mowa
dialog.option.sound.moves.dir = Mowa przy ruchu:
sound.moves.engine  = Wypowiadaj ruch silnika
sound.moves.ack.user = PotwierdŸ ruch gracza
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

doc.panel.antialias		= Wyg³adzanie czcionek

# Notation

dialog.option.doc.move.format	= Notacja:
move.format			= Notacja
move.format.short		= Krótka
move.format.long		= D³uga
move.format.algebraic		= Algebraiczna
move.format.correspondence	= Korespondencyjna
move.format.english		= Angielska
move.format.telegraphic		= Telegraficzna

# Colors

dialog.option.board.surface.light	= Bia³e pola
dialog.option.board.surface.dark	= Czarne pola
dialog.option.board.surface.white	= Bia³e bierki
dialog.option.board.surface.black	= Czarne bierki

dialog.option.board.surface.background	= T³o
dialog.option.board.surface.frame	= Obramowanie
dialog.option.board.surface.coords	= Wspó³rzêdne

dialog.option.board.3d.model            = Model:
dialog.option.board.3d.clock            = Zegar
dialog.option.board.3d.surface.frame	= Obramowanie:
dialog.option.board.3d.light.ambient	= Œwiat³o otaczaj¹ce:
dialog.option.board.3d.light.directional = Œwiat³o bezpoœrednie:
dialog.option.board.3d.knight.angle     = Skoczki:

board.surface.light	= Bi¹³e pola
board.surface.dark	= Czarne pola
board.surface.white	= Bia³e bierki
board.surface.black	= Czarne bierki
board.hilite.squares 	= Podœwietlenie pól

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
time.control.delete		= Usuñ

# Engine Settings

dialog.option.plugin.1		= Silnik 1
dialog.option.plugin.2		= Silnik 2

plugin.add =
plugin.delete =
plugin.duplicate =
plugin.add.tip = Dodaj nowy silnik
plugin.delete.tip = Usuñ konfiguracjê
plugin.duplicate.tip = Duplikuj konfiguracjê

dialog.option.plugin.file 	= Plik konfiguracyjny:
dialog.option.plugin.name 	= Nazwa:
dialog.option.plugin.version 	= Wersja:
dialog.option.plugin.author 	= Autor:
dialog.option.plugin.dir 	= Œcie¿ka:
dialog.option.plugin.logo 	= Logo:
dialog.option.plugin.startup 	= Uruchamianie:

dialog.option.plugin.exe = Wykonywalny:
dialog.option.plugin.args = Argumenty:
dialog.option.plugin.default = Ustawienia domyœlne

plugin.info                 = Informacje ogólne
plugin.protocol.xboard      = Protokó³ XBoard
plugin.protocol.uci         = Protokó³ UCI
plugin.options              = Opcje silnika
plugin.startup              = Wiêcej opcji
plugin.show.logos           = Poka¿ logo
plugin.show.text            = Poka¿ tekst

plugin.switch.ask           = Wybra³eœ inny silnik.\n Czy uruchomiæ go teraz?
plugin.restart.ask          = Zmieni³eœ niektóre ustawienia silnika.\n Czy ponownie uruchomiæ silnik?
plugin.show.info            = Poka¿ informacje
plugin.log.file             = Loguj do pliku

# UCI option name
plugin.option.Ponder        = Rozwa¿anie
plugin.option.Random        = Losowo
plugin.option.Hash          = Rozmiar zbioru ró¿nych pozycji (MB)
plugin.option.NalimovPath   = Œcie¿ka do zbioru bazy Nalimova
plugin.option.NalimovCache  = Pamiêæ dla bazy Nalimova (MB)
plugin.option.OwnBook       = U¿yj zbioru otwaræ
plugin.option.BookFile      = Zbiór otwaræ
plugin.option.BookLearning  = Zbiór „Ucz siê”
plugin.option.MultiPV       = Wariant pierwotny
plugin.option.ClearHash     = Wyczyœæ zbiór ró¿nych pozycji
plugin.option.UCI_ShowCurrLine  = Poka¿ bie¿¹c¹ liniê
plugin.option.UCI_ShowRefutations = Poka¿ obalenie
plugin.option.UCI_LimitStrength = Ogranicz si³ê
plugin.option.UCI_Elo       = ELO

# 3D Settings

board.surface.background	= T³o
board.surface.coords		= Wspó³rzêdne
board.3d.clock              	= Zegar
board.3d.shadow			= Cieñ
board.3d.reflection		= Odblask
board.3d.anisotropic        	= Filtr anizotropiczny
board.3d.fsaa               	= Wyg³adzanie pe³noekranowe

board.3d.surface.frame		= Obramowanie
board.3d.light.ambient		= Œwiat³o otaczaj¹ce
board.3d.light.directional	= Œwiat³o bezpoœrednie
board.3d.screenshot		= Zrzut ekranu
board.3d.defaultview    = Widok domyœlny

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
column.game.white.name 	= Bia³e
column.game.black.name 	= Czarne
column.game.event 	= Wydarzenie
column.game.site 	= Miejscowoœæ
column.game.date 	= Data
column.game.result 	= Wynik
column.game.round 	= Runda
column.game.board 	= Szachownica
column.game.eco 	= ECO
column.game.opening 	= Otwarcie
column.game.movecount 	= Ruchów
column.game.annotator 	= Sekretarz
column.game.fen     = Pozycja pocz¹tkowa

#deprecated
column.problem.author 	= Autor
column.problem.source 	= ?ród³o
column.problem.number 	= Nr
column.problem.date 	= Data
column.problem.stipulation = Zastrze¿enie
column.problem.dedication = Dedykacja
column.problem.award = Nagroda
column.problem.solution = Rozwi¹zanie
column.problem.cplus = C+
column.problem.genre = Rodzaj
column.problem.keyword = Has³o
#deprecated

bootstrap.confirm 	= Œcie¿ka do danych '%datadir%' nie istnieje.\n Czy utworzyæ now¹ œcie¿kê? 
bootstrap.create 	= Utwórz œcie¿kê do danych

edit.game = Otwórz
edit.all = Otwórz w zak³adkach
dnd.move.top.level	= Przesuñ na wierzch


##############################################################################
#  Search Panel
##############################################################################

# Tab Titles
dialog.query.info 		= Informacje
dialog.query.comments 		= Komentarze
dialog.query.position 		= Pozycja

dialog.query.search 	= Szukaj
dialog.query.clear 	= Wyczyœæ
dialog.query.search.in.progress = Szukanie...

dialog.query.0.results 	= Bez wyniku
dialog.query.1.result 	= Jeden wynik
dialog.query.n.results 	= %count% wyników

dialog.query.white		= Bia³e:
dialog.query.black		= Czarne:

dialog.query.flags 		= Opcje
dialog.query.color.sensitive 	= Uwzglêdniaj kolory
dialog.query.swap.colors 	=
dialog.query.swap.colors.tip = Zamieñ kolory
dialog.query.case.sensitive 	= Uwzglêdniaj wielkoœæ liter
dialog.query.soundex 		= Podobne
dialog.query.result 		= Wynik
dialog.query.stop.results   =

dialog.query.event 		= Wydarzenie:
dialog.query.site 		= Miejscowoœæ:
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

dialog.query.errors 		= B³¹d w szukanym wyra¿eniu:
query.error.date.too.small 	= Dane spoza kryterium
query.error.movecount.too.small = Liczba spoza kryterium
query.error.eco.too.long 	= U¿yj trzech znaków dla kodów ECO
query.error.eco.character.expected = Kody ECO musz¹ siê zaczynaæ od A,B,C,D albo E
query.error.eco.number.expected = Kody ECO zawieraj¹ litery i liczby od 0 do 99
query.error.number.format 	= Z³y format liczby
query.error.date.format 	= Z³y format danych

query.setup.enable 		= Szukaj pozycji
query.setup.next 		= Nastêpny ruch:
query.setup.next.white 		= Bia³e
query.setup.next.white.tip 	= Szukaj pozycji przy ruchu bia³ych
query.setup.next.black 		= Czarne
query.setup.next.black.tip 	= Szukaj pozycji przy ruchu czarnych
query.setup.next.any 		= Bia³e albo czarne
query.setup.next.any.tip 	= Szukaj pozycji przy ruchu dowolnego koloru
query.setup.reversed 		= Szukaj odwróconych kolorów
query.setup.reversed.tip 	= Szukaj identycznej pozycji przy odwróconych kolorach
query.setup.var 		= Szukaj wariantów
query.setup.var.tip 		= Szukaj wewn¹trz wariantów



##############################################################################
#	Game Details dialog
##############################################################################

dialog.game		= Szczegó³y gry
dialog.game.tab.1	= Wydarzenie
dialog.game.tab.2	= Gracze
dialog.game.tab.3	= Wiêcej

dialog.details.event 	= Wydarzenie:
dialog.details.site 	= Miejscowoœæ:
dialog.details.date 	= Data:
dialog.details.eventdate = Data wydarzenia:
dialog.details.round 	= Runda:
dialog.details.board 	= Szachownica:

dialog.details.white 	= Bia³e
dialog.details.black 	= Czarne
dialog.details.name 	= Nazwisko:
dialog.details.elo 	= ELO:
dialog.details.title 	= Tytu³:
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

dialog.setup.clear	= Wyczyœæ
dialog.setup.initial	= Pozycja wyjœciowa
dialog.setup.copy	= Kopiuj z g³ównego panelu

dialog.setup.next.white	= Ruch bia³ych
dialog.setup.next.black	= Ruch czarnych
dialog.setup.move.no	= Numer ruchu

dialog.setup.castling		= Roszada
dialog.setup.castling.wk	= Bia³e 0-0
dialog.setup.castling.wk.tip	= Roszada na skrzydle królewskim bia³ych
dialog.setup.castling.wq	= Bia³e 0-0-0
dialog.setup.castling.wq.tip	= Roszada na skrzydle hetmañskim bia³ych
dialog.setup.castling.bk	= Czarne 0-0
dialog.setup.castling.bk.tip	= Roszada na skrzydle królewskim czarnych
dialog.setup.castling.bq	= Czarne 0-0-0
dialog.setup.castling.bq.tip	= Roszada na skrzydle hetmañskim czarnych
dialog.setup.invalid.fen    = Nieprawid³owy zapis FEN.

##############################################################################
#	About dialog
##############################################################################

dialog.about.tab.1	= jose
dialog.about.tab.2	= Baza danych
dialog.about.tab.3	= Wspó³pracownicy
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

dialog.about.3	=	<b>T³umacze:</b><br>\
			Frederic Raimbault, José de Paula, \
			Agustín Gomila, Alex Coronado, \
			Harold Roig, Hans Eriksson, \
			Guido Grazioli, Tomasz Sokó³, \
			"Direktx", <br>\
			<br>\
			<b>Projektanci czccionek TrueType:</b> <br>\
			Armando Hernandez Marroquin, \
			Eric Bentzen, \
			Alan Cowderoy, <br> \
			Hans Bodlaender \
			(www.chessvariants.com/d.font/fonts.html) <br>\
			<br>\
			<b>Interfejs Metouia:</b> <br>\
			Taoufik Romdhane (mlf.sourceforge.net)<br>\
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
					Œrodowisko graficzne: %java.awt.graphicsenv% <br>\
					Zestaw narzêdzi AWT: %awt.toolkit%<br>\
					Œcie¿ka domowa: %java.home%<br>\
					<br>\
					Ca³kowita pamiêæ: %maxmem%<br>\
					Dostêpna pamiêæ: %freemem%<br>\
					<br>\
					System operacyjny: %os.name%  %os.version% <br>\
					Architektura systemu: %os.arch%

dialog.about.5.no3d = Java3D jest dostêpna
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
dialog.export.preview = Podgl¹d
dialog.export.browser = Podgl¹d w przegl¹darce

dialog.export.paper = Strona
dialog.export.orientation = Orientacja
dialog.export.margins = Marginesy

dialog.export.paper.format = Papier:
dialog.export.paper.size = Powiêkszenie:

dialog.print.custom.paper = U¿ytkownika

dialog.export.margin.top = Górny:
dialog.export.margin.bottom = Dolny:
dialog.export.margin.left = Lewy:
dialog.export.margin.right = Prawy:

dialog.export.ori.port = Pionowa
dialog.export.ori.land = Pozioma

dialog.export.games.0 = Nie wybra³eœ ¿adnych gier do drukowania.
dialog.export.games.1 = Wybra³eœ <b>jedn¹ grê</b> do drukowania.
dialog.export.games.n = Wybra³eœ <b>%n% gry/gier</b> do drukowania.
dialog.export.games.? = Wybra³eœ <b>nieznan¹</b> iloœæ gier do drukowania.

dialog.export.confirm = Czy jesteœ pewien?
dialog.export.yes = Drukuj wszystkie

# xsl stylesheet options

export.pgn = Plik PGN
export.pgn.tip = Eksportuj gry jako plik Portable Game Notation.

print.awt = Drukuj
print.awt.tip = Drukuj gry z ekranu.<br> \
				    <li>Naciœnij <b>drukuj...</b> aby drukowaæ na drukarce domyœlnej \
				    <li>Naciœnij <b>podgl¹d</b> aby obejrzeæ dokument

xsl.html = HTML<br>Strona internetowa
xsl.html.tip = Utwórz stronê internetow¹ (plik HTML).<br> \
				   <li>Naciœnij <b>zapisz jako...</b> aby zapisaæ plik na dysku \
				   <li>Naciœnij <b>podgl¹d w przegl¹darce</b> aby obejrzeæ stronê w przegl¹darce internetowej

xsl.dhtml = Dynamiczna<br>strona internetowa
xsl.dhtml.tip = Utwórz stronê internetow¹ <i>z dynamicznymi</i> efektami.<br> \
				   <li>Naciœnij <b>zapisz jako...</b> aby zapisaæ plik na dysku \
				   <li>Naciœnij <b>podgl¹d w przegl¹darce</b> aby obejrzeæ stronê w przegl¹darce internetowej <br> \
				  JavaScript musi byæ w³¹czony w przegl¹darce internetowej.

xsl.debug = Plik XML<br>(debug)
export.xml.tip = Utwórz plik XML dla debugowania.

xsl.pdf = Drukuj PDF
xsl.pdf.tip = Utwórz albo drukuj plik PDF.<br> \
				<li>Naciœnij <b>zapisz jako...</b> aby zapisaæ plik na dysku \
				<li>Naciœnij <b>drukuj...</b> aby wydrukowaæ dokument \
				<li>Naciœnij <b>podgl¹d</b> aby obejrzeæ dokument

xsl.tex = Plik TeX
xsl.tex.tip = Utwórz plik dla przetwarzania TeX.

xsl.html.figs.tt = Bierki TrueType
xsl.html.figs.img = Bierki graficznie
xsl.css.standalone = Dla danych CSS utwórz osobny plik

xsl.html.img.dir = Œcie¿ka
xsl.create.images = Utwórz obraz

xsl.pdf.embed = OsadŸ czcionki TrueType
xsl.pdf.font.dir = Dodatkowe czcionki:

default.file.name = Partia


##############################################################################
#	Print Preview Dialog
##############################################################################

print.preview.page.one =
print.preview.page.two =
print.preview.page.one.tip = Poka¿ jedn¹ stronê
print.preview.page.two.tip = Poka¿ dwie strony

print.preview.ori.land =
print.preview.ori.port =
print.preview.ori.land.tip = U¿yj poziomej orientacji papieru
print.preview.ori.port.tip = U¿yj pionowej orientacji papieru

print.preview.fit.page = Ca³a strona
print.preview.fit.width = Szerokoœæ strony
print.preview.fit.textwidth = Szerokoœæ tekstu

print.preview.next.page =
print.preview.previous.page =

preview.wait = Zaczekaj chwilê...

##############################################################################
#	Online Update Dialog
##############################################################################

online.update.title	= Aktualizacja online
online.update.tab.1	= Nowa wersja
online.update.tab.2	= Co nowego?
online.update.tab.3 	= Wa¿ne uwagi

update.install	= Pobierz i zainstaluj teraz
update.download	= Pobierz teraz, zainstaluj póŸniej
update.mirror	= Pobierz ze strony lustrzanej

download.file.progress 			= Pobieranie %file%
download.file.title			= Pobierz
download.error.invalid.url		= Nieprawid³owy URL: %p%.
download.error.connect.fail		= Nieudane po³¹czenie z %p%.
download.error.parse.xml		= B³¹d odczytu: %p%.
download.error.version.missing	= Nie mo¿na odczytaæ wersji z %p%.
download.error.os.missing		= Brak pliku dla twojego systemu operacyjnego.
download.error.browser.fail		= Nie mo¿na wyœwietliæ %p%.
download.error.update			= Podczas aktualizacji jose wyst¹pi³ b³¹d.\n Rêcznie uaktualnij aplikacjê.

download.message.up.to.date		= Masz zainstalowan¹ najnowsz¹ wersjê.
download.message.success		= Aktualizacja jose do wersji %p% zakoñczy³a siê sukcesem \n. Ponownie uruchom aplikacjê.

download.message			= \
  Wersja <b>%version%</b> jest dostêpna na <br>\
  <font color=blue>%url%</font><br>\
  Rozmiar: %size%

dialog.browser		= Uruchom przegl¹darkê HTML.\n Wpisz komendê, która pozwoli uruchomiæ przegl¹darkê.
dialog.browser.title 	= Lokalizacja przegl¹darki

# deprecated
#online.report.title	= Raport
#online.report.bug	= Zg³oœ b³¹d
#online.report.feature	= Feature Request
#online.report.support	= Support Request

#online.report.type	= Rodzaj:
#online.report.subject	= Temat:
#online.report.description	= Opis:
#online.report.email		= E-mail:

#online.report.default.subject		= <Subject>
#online.report.default.description	= <Please try to describe the steps that caused the error>
#online.report.default.email		= <your e-mail address - optional>
#online.report.info			= Raport zostanie wys³any do http://jose-chess.sourceforge.net

#online.report.success		= Twój raport zosta³ przed³o¿ony.
#online.report.failed		= Nie mo¿na przed³o¿yæ twojego raportu.
# deprecated


##########################################
# 	Error Messages
##########################################

error.not.selected 		= Wybierz grê do zapisania.
error.duplicate.database.access = Nie uruchamiaj naraz dwóch aplikacji jose \n\
	korzystaj¹cych z tej samej bazy danych.\n\n\
	Takie postêpowanie mo¿e spowodowaæ utratê danych. \n\
	Zakoñcz jedn¹ aplikacjê jose.
error.lnf.not.supported 	= Ten interfejs nie jest dostêpny \n na bie¿¹cej platformie.

error.bad.uci = Wydaje siê, ¿e nie jest to silnik UCI.\n Czy jesteœ pewien?

error.bug	= <center><b>Wyst¹pi³ niespodziewany b³¹d.</b></center><br><br> \
 Pomocne mo¿e byæ przed³o¿enie raportu o b³êdach.<br>\
 Opisz kroki, które doprowadzi³y do powstania b³êdu <br>\
 i do³¹cz do opisu nastêpuj¹cy plik: <br>\
  <center><b> %error.log% </b></center>

# errors in setup dialog

pos.error.white.king.missing	= Brakuje bia³ego króla.
pos.error.black.king.missing	= Brakuje czarnego króla.
pos.error.too.many.white.kings	= Za du¿o bia³ych króli.
pos.error.too.many.black.kings	= Za du¿o czarnych króli.
pos.error.white.king.checked	= Bia³y król nie mo¿e byæ w szachu.
pos.error.black.king.checked	= Czarny król nie mo¿e byæ w szachu.
pos.error.white.pawn.base		= Bia³e piony nie mog¹ byæ umieszczone na pierwszej linii.
pos.error.white.pawn.promo		= Bia³e piony nie mog¹ byæ umieszczone na ósmej linii.
pos.error.black.pawn.base		= Czarne piony nie mog¹ byæ umieszczone na ósmej linii.
pos.error.black.pawn.promo		= Czarne piony nie mog¹ byæ umieszczone na pierwszej linii.
pos.warning.too.many.white.pieces	= Za du¿o bia³ych bierek.
pos.warning.too.many.black.pieces	= Za du¿o czarnych bierek.
pos.warning.too.many.white.pawns	= Za du¿o bia³ych pionów.
pos.warning.too.many.black.pawns	= Za du¿o czarnych pionów.
pos.warning.too.many.white.knights	= Za du¿o bia³ych skoczków.
pos.warning.too.many.black.knights	= Za du¿o czarnych skoczków.
pos.warning.too.many.white.bishops	= Za du¿o bia³ych goñców.
pos.warning.too.many.black.bishops	= Za du¿o czarnych goñców.
pos.warning.too.many.white.rooks	= Za du¿o bia³ych wie¿.
pos.warning.too.many.black.rooks	= Za du¿o czarnych wie¿.
pos.warning.too.many.white.queens	= Za du¿o bia³ych hetmanów.
pos.warning.too.many.black.queens	= Za du¿o czarnych hetmanów.
pos.warning.strange.white.bishops	= Bia³e goñce na jednobarwnych polach?
pos.warning.strange.black.bishops	= Czarne goñce na jednobarwnych polach?


##############################################################################
#	Style Names
##############################################################################


base			= Ogólne
header			= Informacje o grze
header.event	= Wydarzenie
header.site		= Miejscowoœæ
header.date		= Data
header.round	= Runda
header.white	= Bia³e
header.black	= Czarne
header.result	= Wynik gry
body			= Zapis gry
body.line		= Ruchy
body.line.0		= G³ówna linia
body.line.1		= Warianty
body.line.2		= Podwarianty
body.line.3		= 2. podwariant
body.line.4		= 3. podwariant
body.symbol		= Symbole
body.inline		= Diagram tekstowy
body.figurine	= Bierki
body.figurine.0	= G³ówna linia
body.figurine.1	= Wariant
body.figurine.2	= Podwariant
body.figurine.3	= 2. podwariant
body.figurine.4	= 3. podwariant
body.comment	= Komentarze
body.comment.0	= G³ówna linia
body.comment.1	= Wariant
body.comment.2	= Podwariant
body.comment.3	= 2. podwariant
body.comment.4	= 3. podwariant
body.result		= Wynik
html.large      = Diagram na stronie internetowej


##############################################################################
#	Task Dialogs (progress)
##############################################################################

dialog.progress.time 		= Pozosta³o: %time%

dialog.read-progress.title 	= jose – otwieranie pliku
dialog.read-progress.text 	= Otwieranie %fileName%

dialog.eco 			= Klasyfikuj ECO
dialog.eco.clobber.eco 		= Nadpisz kody ECO
dialog.eco.clobber.name 	= Nadpisz otwieranie nazwy
dialog.eco.language 		= Jêzyk:


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
fig.ru = ÏÑÊËÔ"Êð"
fig.ukr = ÏÑÊËÔ"Êð"
# please note that Russians use the latin alphabet for files (a,b,c,d,e,f,g,h)
# (so we don't have to care about that ;-)

##############################################################################
#	foreign language names
##############################################################################

lang.cs = czeski
lang.da = duñski
lang.nl = holenderski
lang.en = angielski
lang.et = estoñski
lang.fi = fiñski
lang.fr = francuski
lang.de = niemiecki
lang.hu = wêgierski
lang.is = islandzki
lang.it = w³oski
lang.no = norweski
lang.pl = polski
lang.pt = portugalski
lang.ro = rumuñski
lang.es = hiszpañski
lang.ca = kataloñski
lang.sv = szwedzki
lang.ru = rosyjski
lang.he = hebrajski
lang.tr = turecki
lang.ukr = ukraiñski


##############################################################################
#	PGN Annotations
##############################################################################

pgn.nag.0    = uniewa¿nij przypisy
pgn.nag.1    	= !
pgn.nag.1.tip	= dobry ruch
pgn.nag.2	= ?
pgn.nag.2.tip	= z³y ruch
pgn.nag.3    	= !!
pgn.nag.3.tip	= bardzo dobry ruch
pgn.nag.4    	= ??
pgn.nag.4.tip	= bardzo z³y ruch
pgn.nag.5    	= !?
pgn.nag.5.tip	= interesuj¹cy ruch
pgn.nag.6    	= ?!
pgn.nag.6.tip	= w¹tpliwy ruch
pgn.nag.7    = mocny ruch
pgn.nag.7.tip = mocny ruch (ka¿dy inny szybko spowoduje straty)
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
pgn.nag.14.tip	= Bia³e maj¹ nieznaczn¹ przewagê
pgn.nag.15   	= =+
pgn.nag.15.tip 	= Czarne maj¹ nieznaczn¹ przewagê
pgn.nag.16	= +/-
pgn.nag.16.tip 	= Bia³e maj¹ umiarkowan¹ przewagê
pgn.nag.17	= -/+
pgn.nag.17.tip 	= Czarne maj¹ umiarkowan¹ przewagê
pgn.nag.18	= +-
pgn.nag.18.tip  = Bia³e maj¹ zdecydowan¹ przewagê
pgn.nag.19	= -+
pgn.nag.19.tip 	= Czarne maj¹ zdecydowan¹ przewagê
pgn.nag.20   = Bia³e maj¹ mia¿d¿¹c¹ przewagê (Czarne powinny siê poddaæ)
pgn.nag.21   = Czarne maj¹ mia¿d¿¹c¹ przewagê (Bia³e powinny siê poddaæ)
pgn.nag.22   = Przymusowy ruch bia³ych (zugzwang)
pgn.nag.23   = Przymusowy ruch czarnych (zugzwang)
pgn.nag.24   = Bia³e maj¹ nieznaczn¹ przewagê pola
pgn.nag.25   = Czarne maj¹ nieznaczn¹ przewagê pola
pgn.nag.26   = Bia³e maj¹ umiarkowan¹ przewagê pola
pgn.nag.27   = Czarne maj¹ umiarkowan¹ przewagê pola
pgn.nag.28   = Bia³e maj¹ zdecydowan¹ przewagê pola
pgn.nag.29   = Czarne maj¹ zdecydowan¹ przewagê pola
pgn.nag.30   = Bia³e maj¹ nieznaczn¹ (postêpuj¹c¹) przewagê czasu
pgn.nag.31   = Czarne maj¹ nieznaczn¹ (postêpuj¹c¹) przewagê czasu
pgn.nag.32   = Bia³e maj¹ umiarkowan¹ (postêpuj¹c¹) przewagê czasu
pgn.nag.33   = Czarne maj¹ umiarkowan¹ (postêpuj¹c¹) przewagê czasu
pgn.nag.34   = Bia³e maj¹ zdecydowan¹ (postêpuj¹c¹) przewagê czasu
pgn.nag.35   = Czarne maj¹ zdecydowan¹ (postêpuj¹c¹) przewagê czasu
pgn.nag.36   = Inicjatywa po stronie bia³ych
pgn.nag.37   = Inicjatywa po stronie czarnych
pgn.nag.38   = Sta³a inicjatywa po stronie bia³ych
pgn.nag.39   = Sta³a inicjatywa po stronie czarnych
pgn.nag.40   = Bia³e w ataku
pgn.nag.41   = Czarne w ataku
pgn.nag.42   = Bia³e nie maj¹ dostatecznej kompensaty strat materialnych
pgn.nag.43   = Czarne nie maj¹ dostatecznej kompensaty strat materialnych
pgn.nag.44   = Bia³e maj¹ dostateczn¹ kompensatê strat materialnych
pgn.nag.45   = Czarne maj¹ dostateczn¹ kompensatê strat materialnych
pgn.nag.46   = Kompensata bia³ych jest wiêksza ni¿ straty materialne
pgn.nag.47   = Kompensata czarnych jest wiêksza ni¿ straty materialne
pgn.nag.48   = Nieznaczna przewaga bia³ych w kontroli centrum
pgn.nag.49   = Nieznaczna przewaga czarnych w kontroli centrum
pgn.nag.50   = Umiarkowana przewaga bia³ych w kontroli centrum
pgn.nag.51   = Umiarkowana przewaga czarnych w kontroli centrum
pgn.nag.52   = Zdecydowana przewaga bia³ych w kontroli centrum
pgn.nag.53   = Zdecydowana przewaga czarnych w kontroli centrum
pgn.nag.54   = Nieznaczna przewaga bia³ych w kontroli skrzyd³a królewskiego
pgn.nag.55   = Nieznaczna przewaga czarnych w kontroli skrzyd³a królewskiego
pgn.nag.56   = Umiarkowana przewaga bia³ych w kontroli skrzyd³a królewskiego
pgn.nag.57   = Umiarkowana przewaga czarnych w kontroli skrzyd³a królewskiego
pgn.nag.58   = Zdecydowana przewaga bia³ych w kontroli skrzyd³a królewskiego
pgn.nag.59   = Zdecydowana przewaga czarnych w kontroli skrzyd³a królewskiego
pgn.nag.60   = Nieznaczna przewaga bia³ych w kontroli skrzyd³a hetmañskiego
pgn.nag.61   = Nieznaczna przewaga czarnych w kontroli skrzyd³a hetmañskiego
pgn.nag.62   = Umiarkowana przewaga bia³ych w kontroli skrzyd³a hetmañskiego
pgn.nag.63   = Umiarkowana przewaga czarnych w kontroli skrzyd³a hetmañskiego
pgn.nag.64   = Zdecydowana przewaga bia³ych w kontroli skrzyd³a hetmañskiego
pgn.nag.65   = Zdecydowana przewaga czarnych w kontroli skrzyd³a hetmañskiego
pgn.nag.66   = Niezabezpieczona pierwsza linia bia³ych
pgn.nag.67   = Niezabezpieczona pierwsza linia czarnych
pgn.nag.68   = Zabezpieczona pierwsza linia bia³ych
pgn.nag.69   = Zabezpieczona pierwsza linia czarnych
pgn.nag.70   = Niewystarczaj¹ca ochrona bia³ego króla
pgn.nag.71   = Niewystarczaj¹ca ochrona czarnego króla
pgn.nag.72   = Dobra ochrona bia³ego króla
pgn.nag.73   = Dobra ochrona czarnego króla
pgn.nag.74   = Kiepskie umiejscowienie bia³ego króla
pgn.nag.75   = Kiepskie umiejscowienie czrnego króla
pgn.nag.76   = Dobre umiejscowienie bia³ego króla
pgn.nag.77   = Dobre umiejscowienie czarnego króla
pgn.nag.78   = Bardzo niedobry uk³ad bia³ych pionów
pgn.nag.79   = Bardzo niedobry uk³ad czarnych pionów
pgn.nag.80   = Bia³e maj¹ umiarkowanie z³y uk³ad pionów
pgn.nag.81   = Czarne maj¹ umiarkowanie z³y uk³ad pionów
pgn.nag.82   = Bia³e maj¹ umiarkowanie silny uk³ad pionów
pgn.nag.83   = Czarne maj¹ umiarkowanie silny uk³ad pionów
pgn.nag.84   = Bia³e maj¹ bardzo silny uk³ad pionów
pgn.nag.85   = Czarne maj¹ bardzo silny uk³ad pionów
pgn.nag.86   = Kiepskie umiejscowienie bia³ego skoczka
pgn.nag.87   = Kiepskie umiejscowienie czarnego skoczka
pgn.nag.88   = Dobre umiejscowienie bia³ego skoczka
pgn.nag.89   = Dobre umiejscowienie czarnego skoczka
pgn.nag.90   = Kiepskie umiejscowienie bia³ego goñca
pgn.nag.91   = Kiepskie umiejscowienie czarnego goñca
pgn.nag.92   = Dobre umiejscowienie bia³ego goñca
pgn.nag.93   = Dobre umiejscowienie czarnego goñca
pgn.nag.94   = Kiepskie umiejscowienie bia³ej wie¿y
pgn.nag.95   = Kiepskie umiejscowienie czarnej wie¿y
pgn.nag.96   = Dobre umiejscowienie bia³ej wie¿y
pgn.nag.97   = Dobre umiejscowienie czarnej wie¿y
pgn.nag.98   = Kiepskie umiejscowienie bia³ego hetmana
pgn.nag.99   = Kiepskie umiejscowienie czarnego hetmana
pgn.nag.100  = Dobre umiejscowienie bia³ego hetmana
pgn.nag.101  = Dobre umiejscowienie czarnego hetmana
pgn.nag.102  = Niewystarczaj¹ca koordynacja bia³ych bierek
pgn.nag.103  = Niewystarczaj¹ca koordynacja czarnych bierek
pgn.nag.104  = Dobra koordynacja bia³ych bierek
pgn.nag.105  = Dobra koordynacja czarnych bierek
pgn.nag.106  = Bia³e bardzo Ÿle rozegra³y otwarcie
pgn.nag.107  = Czarne bardzo Ÿle rozegra³y otwarcie
pgn.nag.108  = Bia³e Ÿle rozegra³y otwarcie
pgn.nag.109  = Czarne Ÿle rozegra³y otwarcie
pgn.nag.110  = Bia³e dobrze rozegra³y otwarcie
pgn.nag.111  = Czarne dobrze rozegra³y otwarcie
pgn.nag.112  = Bia³e bardzo dobrze rozegra³y otwarcie
pgn.nag.113  = Czarne bardzo dobrze rozegra³y otwarcie
pgn.nag.114  = Bia³e bardzo Ÿle rozegra³y œrodkow¹ czêœæ gry
pgn.nag.115  = Czarne bardzo Ÿle rozegra³y œrodkow¹ czêœæ gry
pgn.nag.116  = Bia³e Ÿle rozegra³y œrodkow¹ czêœæ gry
pgn.nag.117  = Czarne Ÿle rozegra³y œrodkow¹ czêœæ gry
pgn.nag.118  = Bia³e dobrze rozegra³y œrodkow¹ czêœæ gry
pgn.nag.119  = Czarne dobrze rozegra³y œrodkow¹ czêœæ gry
pgn.nag.120  = Bia³e bardzo dobrze rozegra³y œrodkow¹ czêœæ gry
pgn.nag.121  = Czarne bardzo dobrze rozegra³y œrodkow¹ czêœæ gry
pgn.nag.122  = Bia³e bardzo Ÿle rozegra³y koñcówkê
pgn.nag.123  = Czarne bardzo Ÿle rozegra³y koñcówkê
pgn.nag.124  = Bia³e Ÿle rozegra³y koñcówkê
pgn.nag.125  = Czarne Ÿle rozegra³y koñcówkê
pgn.nag.126  = Bia³e dobrze rozegra³y koñcówkê
pgn.nag.127  = Czarne dobrze rozegra³y koñcówkê
pgn.nag.128  = Bia³e bardzo dobrze rozegra³y koñcówkê
pgn.nag.129  = Czarne bardzo dobrze rozegra³y koñcówkê
pgn.nag.130  = Bia³e w nieznacznym przeciwnatarciu
pgn.nag.131  = Czarne w nieznacznym przeciwnatarciu
pgn.nag.132  = Bia³e w umiarkowanym przeciwnatarciu
pgn.nag.133  = Czarne w umiarkowanym przeciwnatarciu
pgn.nag.134  = Bia³e w zdecydowanym przeciwnatarciu
pgn.nag.135  = Czarne w zdecydowanym przeciwnatarciu
pgn.nag.136  = Bia³e w umiarkowanym niedoczasie
pgn.nag.137  = Czarne w umiarkowanym niedoczasie
pgn.nag.138  = Bia³e w dotkliwym niedoczasie
pgn.nag.139  = Czarne w dotkliwym niedoczasie

# following codes are defined by Fritz or SCID

pgn.nag.140  	= Pomys³
pgn.nag.141  	= Przeciwko
pgn.nag.142  	= Jest lepsze
pgn.nag.143  	= Jest gorsze
pgn.nag.144  	= =
pgn.nag.144.tip = Jest równowa¿ne
pgn.nag.145  	= RR
pgn.nag.145.tip	= Notka redakcyjna
pgn.nag.146  	= N
pgn.nag.146.tip = Innowacja
pgn.nag.147     = S³aby punkt
pgn.nag.148     = Koñcówka
pgn.nag.149     = Linia
pgn.nag.150		= Przek¹tna
pgn.nag.151		= Bia³e maj¹ parê goñców
pgn.nag.152     = Czarne maj¹ parê goñców
pgn.nag.153		= Goñce na dwubarwnych polach
pgn.nag.154		= Goñce na jednobarwnych polach

# following codes are defined by us (equivalent to Informator symbols)
# (is there a standard definition for these symbols ?)

pgn.nag.156		= Wolny pion
pgn.nag.157		= Wiêcej pionów
pgn.nag.158		= Z
pgn.nag.159		= Bez
pgn.nag.161		= Patrz
pgn.nag.163		= Szereg

# defined by SCID:
pgn.nag.190		= Itd.
pgn.nag.191		= Zdublowane piony
pgn.nag.192		= Odizolowane piony
pgn.nag.193		= Powi¹zane piony
pgn.nag.194     = Zawieszone piony
pgn.nag.195     = SpóŸnione piony

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