
##############################################################################
#	Swedish Translation for jose
#       Hans Eriksson (hans.ericson@bredband.net)
##############################################################################

#	Application Name
application.name	= jose

#	Frame Titles
window.board	= Schackbr�de
window.console	= Schackmotor-inst�llningar
window.database	= Databas
window.filter	= Filter
window.list	= Partilista
window.clock	= Klocka
window.game	= Parti
window.engine	= Schackmotor
window.eval     = V�rdering

window.collectionlist	= Databas
window.query		= S�k
window.gamelist		= Databas

window.sqlquery		= SQL S�kning
window.sqllist		= Resultat

window.toolbar.1	= Verktygsrad 1
window.toolbar.2	= Verktygsrad 2
window.toolbar.3	= Verktygsrad 3
window.toolbar.symbols	= Kommentarer
window.help		        = Hj�lp
window.print.preview    = F�rhandsgranskning

# dialog titles

dialog.option	= Alternativ
dialog.about	= Om jose
dialog.animate  = Spela upp
dialog.setup	= Inst�llningar
dialog.message.title = Meddelande

# number formats:
format.byte		= ###0.# 'b'
format.kilobyte		= ###0.# 'kb'
format.megabyte		= ###0.# 'MB'


##############################################################################
# 	Menus
##############################################################################

# File Menu

menu.file		= Arkiv
menu.file.new		= Nytt
menu.file.new.tip	= Startar ett nytt parti
menu.file.new.frc		= Nytt FRC
menu.file.open		= �ppna...
menu.file.open.tip  	= �ppnar en PGN-fil
menu.file.open.url	= �ppna URL...
menu.file.close		= St�ng
menu.file.close.tip	= St�nger det aktuella f�nstret
menu.file.save		= Spara
menu.file.save.tip  	= Sparar det aktuella partiet i databasen
menu.file.save.as    	= Spara som...
menu.file.save.as.tip	= Sparar en ny kopia av det aktuella partiet i databasen
menu.file.save.all   	= Spara alla
menu.file.save.all.tip  = Sparar alla �ppna partier i databasen
menu.file.revert	= �terg� till partiet
menu.file.print		= Skriv ut...
menu.file.print.tip	= Skriver ut det aktuella partiet
menu.file.print.setup	= Skrivarinst�llningar...
menu.file.print.setup.tip = St�ller in skrivare och pappersstorlek
menu.file.print.preview = F�rhandsgranskning...
menu.file.quit		= Avsluta
menu.file.quit.tip	= Avslutar Jose

# Edit Menu

menu.edit		= Redigera
menu.edit.undo		= �ngra (%action%)
menu.edit.cant.undo 	= �ngra
menu.edit.redo		= G�r om (%action%)
menu.edit.cant.redo 	= G�r om
menu.edit.select.all    = V�lj alla
menu.edit.select.none   = V�lj ingen
menu.edit.cut		= Klipp ut
menu.edit.copy		= Kopiera
menu.edit.copy.fen  = FEN str�ng
menu.edit.copy.fen.tip = Kopierar den aktuella positionen till klippboken (som en FEN-str�ng)
menu.edit.copy.img  = Bild+Bakgrund
menu.edit.copy.img.tip = Kopierar den aktuella positionen till klippboken (som en bild med bakgrund)
menu.edit.copy.imgt  = Bild
menu.edit.copy.imgt.tip = Kopierar den aktuella positionen till klippboken (som en bild)
menu.edit.copy.text  = Textdiagram
menu.edit.copy.text.tip = Kopierar den aktuella positionen till klippboken (som ett diagram med text)

menu.edit.copy.pgn  = Kopiera PGN
menu.edit.copy.pgn.tip = Kopierar det aktuella partiet till klippboken (som en PGN-text)
menu.edit.paste		= Klistra in
menu.edit.paste.tip = Klistrar in fr�n Klippboken
menu.edit.paste.copy		= Klistra in kopierade partier
menu.edit.paste.copy.tip 	= Klistrar in partier fr�n klippboken
menu.edit.paste.same 	= Klistra in partier
menu.edit.paste.same.tip = Klistrar in partier fr�n klippboken
menu.edit.paste.pgn = Klistra in PGN
menu.edit.paste.pgn.tip = Klistrar in ett parti fr�n klippboken (som en PGN-text)
menu.edit.clear		= T�m
menu.edit.option	= Alternativ...
menu.edit.option.tip	= �ppnar Alternativinst�llningarna

menu.edit.games			= Databas
menu.edit.collection.new 	= Ny katalog
menu.edit.collection.rename 	= Byt namn
menu.edit.empty.trash		= T�m skr�pkorg
menu.edit.restore		= Ta tillbaka

#menu.edit.position.index    = Uppdatera Positionsindex
menu.edit.search.current    = S�k efter denna position
menu.edit.ecofy             = Klassificera ECO
menu.edit.search.current    = S�k efter denna position
menu.edit.ecofy             = Klassificera ECO

menu.edit.style = Format
menu.edit.bold = Fet
menu.edit.italic = Lutande
menu.edit.underline = Understruken
menu.edit.plain = Enkel text
menu.edit.left = V�nsterjusterad
menu.edit.center = Centrerad
menu.edit.right = H�gerjusterad
menu.edit.larger = �ka textstorlek
menu.edit.smaller = Minska textstorlek
menu.edit.color = Textf�rg

# Game Menu

menu.game		= Parti
menu.game.details	= Partidata...
menu.game.analysis  	= Analysmod
menu.game.navigate	= G� till...
menu.game.time.controls = Tidskontroller
menu.game.time.control = Tidskontroll
menu.game.details.tip 	= �ndra partidata (Spelare,etc.)
menu.game.hint		= Be om hj�lp
menu.game.hint.tip  = Visar hj�lp
menu.game.draw		= Erbjud remi
menu.game.resign	= Ge upp
menu.game.2d		= 2D utseende
menu.game.3d		= 3D utseende
menu.game.flip		= V�nd schackbr�det
menu.game.coords	= Koordinater
menu.game.coords.tip	= �ndrar koordinatvisning
menu.game.animate 	= Spela upp...
menu.game.previous 	= F�rra partiet
menu.game.next 		= N�sta parti
menu.game.close 	= St�ng
menu.game.close.tip 	= St�ng aktuellt parti
menu.game.close.all 	= St�ng alla
menu.game.close.all.tip = St�nger alla �ppna partier
menu.game.close.all.but = St�ng alla UTOM detta
menu.game.close.all.but.tip = St�nger alla �ppna partier utom det aktuella partiet
menu.game.setup		= S�tt up st�llning

menu.game.copy.line = Kopiera variant
menu.game.copy.line.tip = Kopierar denna variant till klippboken
menu.game.paste.line = Klistra in variant
menu.game.paste.line.tip = Infogar denna variant i det aktuella partiet

# Window Menu

menu.window		= F�nster
menu.window.fullscreen 	= Helsk�rmsvisning
menu.window.reset   	= �terst�ll layouten

# Help Menu

menu.help		= Hj�lp
menu.help.splash	= Om...
menu.help.about		= Information...
menu.help.license	= Licens...
menu.help.context   	= Hj�lp om Jose
menu.help.manual    	= Manual
menu.help.web		= jose p� internet

menu.web.home		= Hemsida
menu.web.update		= Uppdatering via internet 
menu.web.download	= Ladda ner
menu.web.report		= Felrapporter
menu.web.support	= Supportf�rfr�gningar
menu.web.feature	= Funktionsf�rfr�gningar
menu.web.forum		= Forum
menu.web.donate     = Donera
menu.web.browser	= V�lj webl�sare...


##############################################################################
# 	Context Menu
##############################################################################

panel.hide		= D�lj
panel.hide.tip		= D�ljer detta f�nster
panel.undock		= Nytt f�nster
panel.undock.tip	= �ppnar ett nytt f�nster
panel.move		= Flytta
panel.move.tip		= Flyttar detta f�nster till en annan position
panel.dock		= St�ng
panel.dock.tip		= St�nger detta f�nster

panel.orig.pos = Originalpositionen
panel.dock.here = St�ng f�nstret h�r
panel.undock.here = Nytt f�nster h�r

#################
# Document Panel
#################

# deprecated:
tab.place 	= Kommentarsplacering
tab.place.top 	= �ver
tab.place.left 	= V�nster
tab.place.bottom = Under
tab.place.right = H�ger
#

tab.layout 		= Kommentarslayout
tab.layout.wrap 	= V�nd
tab.layout.scroll 	= Scrolla

doc.menu.annotate 	= Kommentera
doc.menu.delete.comment = Ta bort kommentar
doc.menu.line.promote 	= L�gg till variant
doc.menu.line.delete 	= Ta bort variant
doc.menu.line.cut 	= Avsluta variant
doc.menu.line.uncomment = Tar bort alla kommentarer
doc.menu.remove.annotation = -Inga-
doc.menu.more.annotations = fler...

tab.untitled 	= Namnl�s
confirm 	= Bekr�fta
confirm.save.one = Spara aktuellt parti ?
confirm.save.all = Spara �ndrade partier ?

dialog.confirm.save = Spara
dialog.confirm.dont.save = Spara inte

dialog.engine.offers.draw = %engine% erbjuder remi.

dialog.accept.draw = Acceptera remi
dialog.decline.draw = Avb�j remi

dialog.autoimport.title = Importera
dialog.autoimport.ask = Filen ^0 har �ndrats p� disken \n �ppna den igen ?

dialog.paste.message = Du �r p� v�g att infoga data fr�n klippboken. \n\
     Vill du flytta partierna, eller skapa en ny kopia ?
dialog.paste.title = Klistra in partier
dialog.paste.same = Flytta
dialog.paste.copy = Kopiera

###################
# Game Navigation
###################

move.first	= B�rjan av partiet
move.backward 	= Tillbaka
move.delete 	= Ta tillbaka f�rra draget
engine.stop 	= Stopp
move.start 	= Dra nu
move.forward 	= Fram�t
move.last 	= Slutet av partiet
move.animate	 = Spela upp


##################################
# Engine Panel
##################################

engine.paused.tip 	= %engine% �r stoppad
engine.thinking.tip 	= %engine% t�nker p� sitt n�sta drag
engine.pondering.tip 	= %engine% t�nker p� ditt n�sta drag
engine.analyzing.tip 	= %engine% analyserar
engine.hint.tip 	= Hj�lp: %move%

engine.paused.title 	= %engine%
engine.thinking.title 	= %engine% t�nker
engine.pondering.title 	= %engine% t�nker p� ditt drag
engine.analyzing.title 	= %engine% analyserar

plugin.name 		= %name% %version%
plugin.name.author 	= %name% %version% av %author%

plugin.book.move 	= Bok
plugin.book.move.tip 	= Bokdrag
plugin.hash.move 	= Hashtabell
plugin.hash.move.tip 	= Utv�rderat fr�n hashtabellen
plugin.tb.move 		= Slutspelstabell
plugin.tb.move.tip 	= Utv�rderat fr�n slutspelstabellen

plugin.currentmove.title       = Drag
plugin.depth.title      = S�kdjup
plugin.elapsed.time.title  = Tid
plugin.nodecount.title  = Noder
plugin.nps.title        = Noder/sekund

plugin.currentmove = %move%
plugin.currentmove.max = %move% %moveno%/%maxmove%

plugin.currentmove.tip = Det nu v�rderade draget �r %move%.
plugin.currentmove.max.tip = Det nu v�rderade draget �r %move%. (nr. %moveno% av %maxmove%)

plugin.depth 		= %depth%
plugin.depth.tip 	= S�kdjup: %depth% ply

plugin.depth.sel 	= %depth% (%seldepth%)
plugin.depth.sel.tip 	= S�kdjup: %depth% ply, Valt s�kdjup: %seldepth% ply

plugin.white.mates 	= +#%eval%
plugin.white.mates.tip 	= Vit g�r matt i %eval% drag
plugin.black.mates 	= -#%eval%
plugin.black.mates.tip 	= Svart g�r matt i %eval% drag

plugin.evaluation 	= %eval%
plugin.evaluation.tip 	= Positionsv�rdet �r %eval%

plugin.line.tip = Ber�knad variant

plugin.elapsed.time = %time%
plugin.elapsed.time.tip = Anv�nd tid f�r denna ber�kning.

plugin.nodecount 	= %antal noder%
plugin.nodecount.tip 	= %antal noder% positioner har utv�rderats

plugin.nps      = %nps%
plugin.nps.tip  = %nps% noder utv�rderas per sekund

plugin.pv.history = Expertmod

restart.plugin		= �terstarta schackmotor


######################
# Board Panel
######################

wait.3d = Laddar 3D. Var god v�nta...

message.result			= Resultat 
message.white 			= Vit
message.black 			= Svart
message.mate 			= Matt. \n %player% vinner.
message.stalemate		= Patt. \n Partiet �r remi.
message.draw3			= Position upprepad 3 g�nger. \n Partiet �r remi.
message.draw50			= Ingen pj�s tagen p� 50 drag. \n Partiet �r remi.
message.resign			= %player% ger upp. \n Du vinner.
message.time.draw		= Tiden har g�tt ut. \n Partiet �r remi.
message.time.lose		= Tiden har g�tt ut. \n %player% vinner.


################
# Clock Panel
################

clock.mode.analog	= Analog
clock.mode.analog.tip 	= Visa analog klocka
clock.mode.digital	= Digital
clock.mode.digital.tip 	= Visa digital klocka


##############################################################################
#	Dialogs
##############################################################################

dialog.button.ok		= OK
dialog.button.ok.tip		= Klicka h�r f�r att g�ra �ndringarna
dialog.button.cancel		= Avbryt
dialog.button.cancel.tip	= Klicka h�r f�r att st�nga  dialogen utan att g�ra �ndringar
dialog.button.apply		= Verkst�ll
dialog.button.apply.tip		= Klicka h�r f�r att verkst�lla f�r�ndringarna omedelbart
dialog.button.revert		= �terg�
dialog.button.revert.tip	= Klicka h�r f�r att �ngra f�r�ndringarna
dialog.button.clear		= Rensa
dialog.button.delete		= Ta bort
dialog.button.yes		= Ja
dialog.button.no		= Nej
dialog.button.next		= N�sta
dialog.button.back		= Backa
dialog.button.close		= St�ng
dialog.button.help		= Hj�lp
dialog.button.help.tip		= Visa Hj�lp om funktioner

dialog.button.commit		= Verkst�ll
dialog.button.commit.tip	= Klicka h�r f�r att verkst�lla uppdateringar
dialog.button.rollback		= �ngra
dialog.button.rollback.tip	= Klicka h�r f�r att �ngra uppdateringar

dialog.error.title		= Fel

###################################
#  File Chooser Dialog
###################################

filechooser.pgn			= Portable Game Notation (*.pgn,*.zip)
filechooser.epd         = EPD eller FEN (*.epd,*.fen)
filechooser.db 			= jose Arkiv (*.jose)
filechooser.db.Games 		= jose Partiarkiv (*.jose)
filechooser.db.Games.MySQL 	= jose Partiarkiv (snabbspara) (*.jose)
filechooser.txt 		= Textfiler (*.txt)
filechooser.html 		= HTML-Filer (*.html)
filechooser.pdf 		= Acrobat Reader (*.pdf)
filechooser.exe         = Exekuterbara filer
filechooser.img         = Bildfiler (*.gif,*.jpg,*.png,*.bmp)

filechooser.overwrite 	= Skriva �ver existerande "%file.name%" ?
filechooser.do.overwrite = Skriv �ver

#################
# Color Chooser
#################

colorchooser.texture	= Bakgrund
colorchooser.preview	= F�rhandsgranska
colorchooser.gradient   = Nyans
colorchooser.gradient.color1 = F�rsta f�rg
colorchooser.gradient.color2 = Andra f�rg
colorchooser.gradient.cyclic = cyklisk

colorchooser.texture.mnemonic = T
colorchooser.gradient.mnemonic = G

animation.slider.fast   = snabb
animation.slider.slow   = l�ngsam

##############################################################################
# Option dialog
##############################################################################

# Tab Titles

dialog.option.tab.1	= Spelare
dialog.option.tab.2	= Schackbr�de
dialog.option.tab.3	= F�rger
dialog.option.tab.4	= Tid
dialog.option.tab.5     = Schackmotor
dialog.option.tab.6 = Opening Book
# TODO
dialog.option.tab.7     = 3D
dialog.option.tab.8	= Fonter

# User settings

dialog.option.user.name		= Namn
dialog.option.user.language	= Spr�k
dialog.option.ui.look.and.feel	= Se & k�nn

doc.load.history	= Ladda tidigare partier
doc.classify.eco	= Klassificera  �ppning utifr�n ECO
doc.associate.pgn   = �ppna PGN filer med jose

dialog.option.animation = Spela upp
dialog.option.animation.speed = Hastighet

dialog.option.doc.write.mode	= Infoga nytt drag
write.mode.new.line		= Ny variant
write.mode.new.main.line	= Ny huvudvariant
write.mode.overwrite		= Skriv �ver
write.mode.ask			= Fr�ga
write.mode.dont.ask		= Fr�ga inte n�got mer
# Don't ask anymore
write.mode.cancel		= Avbryt

board.animation.hints   = Visa hj�lp under uppspelning

dialog.option.sound = Ljud
dialog.option.sound.moves.dir = Dragannonseringar:
sound.moves.engine  = Annonsera schackmotordrag
sound.moves.ack.user = Bekr�fta spelardrag
sound.moves.user = Annonsera spelardrag

# Fonts

dialog.option.font.diagram	= Diagram
dialog.option.font.text		= Text
dialog.option.font.inline	= Textdiagram
dialog.option.font.figurine	= Figur
dialog.option.font.symbol	= Symbol
dialog.option.font.size     	= Storlek
figurine.usefont.true 		= Grafikfonter
figurine.usefont.false 		= Textfonter

doc.panel.antialias		= Anv�nd antialiasing fonter

# Notation

dialog.option.doc.move.format	= Notation:
move.format			= Notation
move.format.short		= Kort
move.format.long		= L�ng
move.format.algebraic		= Algebraisk
move.format.correspondence	= Korrespondens
move.format.english		= Engelsk
move.format.telegraphic		= Telegrafisk

# Colors

dialog.option.board.surface.light	= Ljusa rutor
dialog.option.board.surface.dark	= M�rka rutor
dialog.option.board.surface.white	= Vita pj�ser
dialog.option.board.surface.black	= Svarta pj�ser

dialog.option.board.surface.background	= Bakgrund
dialog.option.board.surface.frame	= Br�dutseende
dialog.option.board.surface.coords	= Koordinater

dialog.option.board.3d.model            = Modell:
dialog.option.board.3d.clock            = Klocka
dialog.option.board.3d.surface.frame	= Yta:
dialog.option.board.3d.light.ambient	= Bakgrundsljus:
dialog.option.board.3d.light.directional = Rikningsljus:
dialog.option.board.3d.knight.angle     = Springare:

board.surface.light	= Ljusa rutor
board.surface.dark	= M�rka rutor
board.surface.white	= Vita pj�ser
board.surface.black	= Svarta pj�ser
board.hilite.squares 	= F�rgl�gg rutor

# Time Controls

dialog.option.time.control      = Tidskontroll
dialog.option.phase.1		= Tidskontroll 1
dialog.option.phase.2		= Tidskontroll 2
dialog.option.phase.3		= Tidskontroll 3
dialog.option.all.moves		= alla
dialog.option.moves.in 		= drag p�
dialog.option.increment 	= plus
dialog.option.increment.label 	= per drag

time.control.blitz		= Blixt
time.control.rapid		= Snabbschack
time.control.fischer		= Fischer
time.control.tournament		= Turnering
# default name for new time control
time.control.new		= Ny
time.control.delete		= Ta bort

# Engine Settings

dialog.option.plugin.1		= Schackmotor 1
dialog.option.plugin.2		= Schackmotor 2

plugin.add =l�gg till
plugin.delete =ta bort
plugin.duplicate =duplicera
plugin.add.tip = l�gger till en ny schackmotor
plugin.delete.tip = tar bort konfigurationen
plugin.duplicate.tip = duplicerar konfigurationen

dialog.option.plugin.file 	= Konfigurationsfil:
dialog.option.plugin.name 	= Namn:
dialog.option.plugin.version 	= Version:
dialog.option.plugin.author 	= Konstrukt�r:
dialog.option.plugin.dir 	= Katalog:
dialog.option.plugin.logo 	= Logga:
dialog.option.plugin.startup 	= Starta:

dialog.option.plugin.exe = Exekuterbar fil:
dialog.option.plugin.args = Parametrar:
dialog.option.plugin.default = Standardinst�llningar

plugin.info                 = Generell Information
plugin.protocol.xboard      = XBoard-protokoll
plugin.protocol.uci         = UCI-protokoll
plugin.options              = Schackmotor-alternativ
plugin.startup              = Fler alternativ
plugin.show.logos           = Visa loggor
plugin.show.text            = Visa Text

plugin.switch.ask           = Du har valt en annan schackmotor.\n Vill du starta den nu ?
plugin.restart.ask          = Du har �ndrat n�gra schackmotor-inst�llningar.\n Vill du starta om den nu ?
plugin.show.info            = Visa "info"
plugin.log.file             = Logg till fil

# UCI option name
plugin.option.Ponder        = T�nkande
plugin.option.Random        = Slumpm�ssig
plugin.option.Hash          = Hashtabellstorlek (MB)
plugin.option.NalimovPath   = S�kv�g till Nalimov-slutspelstabeller
plugin.option.NalimovCache  = Cache f�r Nalimov-slutspelstabeller (MB)
plugin.option.OwnBook       = Anv�nd �ppningsbok
plugin.option.BookFile      = �ppningsbok
plugin.option.BookLearning  = Bokl�rande
plugin.option.MultiPV       = Prim�ra variationer
plugin.option.ClearHash     = Rensa Hashtabeller
plugin.option.UCI_ShowCurrLine  = Visa aktuell variant
plugin.option.UCI_ShowRefutations = Visa vederl�ggningar
plugin.option.UCI_LimitStrength = Begr�nsa spelstyrka
plugin.option.UCI_Elo       = ELO
plugin.option.UCI_EngineAbout = 

# 3D Settings

board.surface.background	= Bakgrund
board.surface.coords		= Koordinater
board.3d.clock              	= Klocka
board.3d.shadow			= Skuggor
board.3d.reflection		= Reflektioner
board.3d.anisotropic        	= Anisotropiskt filter
board.3d.fsaa               	= Helsk�rms antialiasing (fsaa)

board.3d.surface.frame		= Schackbr�de
board.3d.light.ambient		= Bakgrundsljus
board.3d.light.directional	= Riktningsljus
board.3d.screenshot		= Sk�rmdump
board.3d.defaultview    = Standardutseende

# Text Styles

font.color 	= F�rg
font.name	= Fontnamn
font.size	= Storlek
font.bold	= Fet
font.italic = Kursiv
font.sample	= Exempel-text


##############################################################################
#	Database Panels
##############################################################################

# default collection folders

collection.trash 	= Papperskorg
collection.autosave 	= Autospara
collection.clipboard 	= Klippboken

# default name for new folders
collection.new 		= Ny katalog

# name of starter database
collection.starter 	= Capablancas partier

# column titles

column.collection.name 		= Namn
column.collection.gamecount 	= Partier
column.collection.lastmodified 	= �ndrad

column.game.index 	= Index
column.game.white.name 	= Vit
column.game.black.name 	= Svart
column.game.event 	= Evenemang
column.game.site 	= Plats
column.game.date 	= Datum
column.game.result 	= Resultat
column.game.round 	= Runda
column.game.board 	= Schackbr�de
column.game.eco 	= ECO
column.game.opening 	= �ppning
column.game.movecount 	= Drag
column.game.annotator 	= Kommentator
column.game.fen     = Startposition

#deprecated
column.problem.author 	= Konstrukt�r
column.problem.source 	= K�lla
column.problem.number 	= Nr.
column.problem.date 	= Datum
column.problem.stipulation = Stipulation.
column.problem.dedication = Dedikation
column.problem.award = Pris
column.problem.solution = L�sning
column.problem.cplus = C+
column.problem.genre = Genre
column.problem.keyword = Nyckelord
#deprecated

bootstrap.confirm 	= Datakatalogen '%datadir%' finns inte.\n Vill du skapa en ny katalog ? 
bootstrap.create 	= Skapa datakatalog

edit.game = �ppna
edit.all = �ppna alla
dnd.move.top.level	= G� till �vre niv�


##############################################################################
#  Search Panel
##############################################################################

# Tab Titles
dialog.query.info 		= Information
dialog.query.comments 		= Kommentarer
dialog.query.position 		= Position

dialog.query.search 	= S�k
dialog.query.clear 	= Rensa
dialog.query.search.in.progress = S�ker...

dialog.query.0.results 	= Inga Resultat
dialog.query.1.result 	= Ett Resultat
dialog.query.n.results 	= %count% Resultat

dialog.query.white		= Vit:
dialog.query.black		= Svart:

dialog.query.flags 		= Alternativ
dialog.query.color.sensitive 	= F�rgk�nslig
dialog.query.swap.colors 	=Byt f�rg
dialog.query.swap.colors.tip = Byter f�rger
dialog.query.case.sensitive 	= Versalk�nslig
dialog.query.soundex 		= L�ter som
dialog.query.result 		= Resultat
dialog.query.stop.results   =

dialog.query.event 		= Evenemang:
dialog.query.site 		= Plats:
dialog.query.eco 		= ECO:
dialog.query.annotator 		= Kommentator:
dialog.query.to 		= till
dialog.query.opening 		= �ppning:
dialog.query.date 		= Datum:
dialog.query.movecount 		= Drag:

dialog.query.commenttext 	= Kommentar:
dialog.query.com.flag 		= har kommentarer
dialog.query.com.flag.tip 	= s�k efter partier med kommentarer

dialog.query.var.flag 		= har variationer
dialog.query.var.flag.tip 	= s�k efter partier med variationer

dialog.query.errors 		= Fel i S�kuttryck:
query.error.date.too.small 	= Datumet �r f�r litet
query.error.movecount.too.small = Antalet drag �r f�r litet
query.error.eco.too.long 	= Avv�nd tre tecken f�r ECO-koder
query.error.eco.character.expected = ECO-koder m�ste b�rja med A,B,C,D,eller E
query.error.eco.number.expected = ECO-koder best�r av en bokstav och ett tal fr�n 0 till 99
query.error.number.format 	= Fel nummerformat
query.error.date.format 	= Fel datumformat

query.setup.enable 		= S�k position
query.setup.next 		= N�sta drag:
query.setup.next.white 		= Vit
query.setup.next.white.tip 	= hittar positioner d�r vit drar sedan
query.setup.next.black 		= Svart
query.setup.next.black.tip 	= s�ker positioner d�r svart drar sedan
query.setup.next.any 		= Vit eller svart
query.setup.next.any.tip 	= s�ker positioner d�r n�gon f�rg drar sedan
query.setup.reversed 		= S�k ombytta f�rger
query.setup.reversed.tip 	= s�ker identiska positioner med ombytta f�rger
query.setup.var 		= S�k variationer
query.setup.var.tip 		= s�ker inom variationer



##############################################################################
#	Game Details dialog
##############################################################################

dialog.game		= Partidata
dialog.game.tab.1	= Evenemang
dialog.game.tab.2	= Spelare
dialog.game.tab.3	= Fler

dialog.details.event 	= Evenemang:
dialog.details.site 	= Plats:
dialog.details.date 	= Datum:
dialog.details.eventdate = Evenemangsdatum:
dialog.details.round 	= Runda:
dialog.details.board 	= Br�de:

dialog.details.white 	= Vit
dialog.details.black 	= Svart
dialog.details.name 	= Namn:
dialog.details.elo 	= ELO:
dialog.details.title 	= Titel:
dialog.details.result 	= Resultat:

dialog.details.eco 	= ECO:
dialog.details.opening 	= �ppning:
dialog.details.annotator = Kommentator:

Result.0-1 = 0-1
Result.1-0 = 1-0
Result.1/2 = 1/2
Result.* = *


##############################################################################
#	Setup dialog
##############################################################################

dialog.setup.clear	= Rensa
dialog.setup.initial	= Startposition
dialog.setup.copy	= Kopiera fr�n huvudf�nstret

dialog.setup.next.white	= Vit drar
dialog.setup.next.black	= Svart drar
dialog.setup.move.no	= Drag Nr.

dialog.setup.castling		= Rockad
dialog.setup.castling.wk	= Vit 0-0
dialog.setup.castling.wk.tip	= Vit kort rockad
dialog.setup.castling.wq	= Vit 0-0-0
dialog.setup.castling.wq.tip	= Vit l�ng rockad
dialog.setup.castling.bk	= Svart 0-0
dialog.setup.castling.bk.tip	= Svart kort rockad
dialog.setup.castling.bq	= Svart 0-0-0
dialog.setup.castling.bq.tip	= Svart l�ng rockad
dialog.setup.invalid.fen    = Felaktig FEN-str�ng.

##############################################################################
#	About dialog
##############################################################################

dialog.about.tab.1	= jose
dialog.about.tab.2	= Databas
dialog.about.tab.3	= Medhj�lpare
dialog.about.tab.4	= System
dialog.about.tab.5	= 3D
dialog.about.tab.6	= Licens

dialog.about.gpl	=   Detta program distribueras under villkoren i GNU General Public License

dialog.about.2	=	<b>%dbname%</b> <br> %dbversion% <br><br> \
					Server URL: %dburl%

dialog.about.MySQL = www.mysql.com

dialog.about.3	=	<b>�vers�ttningar:</b><br>\
			Frederic Raimbault, Jos� de Paula, \
			Agust�n Gomila, Alex Coronado, \
			Harold Roig, Hans Eriksson, \
			Guido Grazioli, Tomasz Sok�l, "Direktx" <br>\
			<br>\
			<b>TrueType Fontdesign:</b> <br>\
			Armando Hernandez Marroquin, \
			Eric Bentzen, \
			Alan Cowderoy, <br> \
			Hans Bodlaender \
			(www.chessvariants.com/d.font/fonts.html) <br>\
			<br>\
			<b>FlatLaf Look & Feel:</b> <br>\
			FormDev Software (www.formdev.com/flatlaf)<br>\
			<br>\
			<b>3D Modellering:</b> <br>\
			Renzo Del Fabbro, \
			Francisco Barala Faura <br>\
			<br>\
			<b>Apple Mac support:</b> <br>\
			Andreas G�ttinger, Randy Countryman


dialog.about.4 =	Java Version: %java.version% (%java.vendor%) <br>\
					Java VM: %java.vm.version% %java.vm.info% (%java.vm.vendor%) <br>\
					Runtime: %java.runtime.name% %java.runtime.version% <br>\
					Grafikomgivning: %java.awt.graphicsenv% <br>\
					AWT Toolkit: %awt.toolkit%<br>\
					Hemkatalog: %java.home%<br>\
					<br>\
					Totalt minne: %maxmem%<br>\
					Fritt minne: %freemem%<br>\
					<br>\
					Operativsystem: %os.name%  %os.version% <br>\
					Systemarkitektur: %os.arch%

dialog.about.5.no3d = Java3D �r nu inte tillg�nglig
dialog.about.5.model =

dialog.about.5.native = Ursprunglig plattform: &nbsp;
dialog.about.5.native.unknown = nu ok�nd

##############################################################################
#	Export/Print Dialog
##############################################################################

dialog.export = Exportera & Skriv ut
dialog.export.tab.1 = Resultat
dialog.export.tab.2 = St�lla in f�r utskrift
dialog.export.tab.3 = Stilar

dialog.export.print = Skriv ut...
dialog.export.save = Spara
dialog.export.saveas = Spara som...
dialog.export.preview = F�rhandsgranska
dialog.export.browser = Webbl�sarf�rhandsgranskning

dialog.export.paper = Papper
dialog.export.orientation = Orientering
dialog.export.margins = Marginaler

dialog.export.paper.format = Papper:
dialog.export.paper.size = Storlek:

dialog.print.custom.paper = St�lla in

dialog.export.margin.top = Ovan:
dialog.export.margin.bottom = Under:
dialog.export.margin.left = V�nster:
dialog.export.margin.right = H�ger:

dialog.export.ori.port = Portr�tt
dialog.export.ori.land = Landskap

dialog.export.games.0 = Du har inte valt n�gra partier att skriva ut.
dialog.export.games.1 = Du har valt <b>ett parti</b> f�r utskrift.
dialog.export.games.n = Du har valt <b>%n% partier</b> f�r utskrift.
dialog.export.games.? = Du har valt ett <b>ok�nt</b> antal partier att skriva ut.

dialog.export.confirm = �r du s�ker ?
dialog.export.yes = Skriv ut allt

# xsl stylesheet options

export.pgn = PGN-fil
export.pgn.tip = Exporterar partier som en Portable Game Notation(PGN)-fil.

print.awt = Skriv ut
print.awt.tip = Skriver ut partier fr�n sk�rmen.<br> \
				    <li>Klicka <b>Skriv ut...</b> f�r att skriva ut till en ansluten skrivare \
				    <li>Klicka <b>F�rhandsgranska</b> f�r att se dokumentet

xsl.html = HTML<br>Webbsida
xsl.html.tip = Skapar en Webbsida  (HTML-fil).<br> \
				   <li>Klicka <b>Spara som...</b> f�r att spara filen till h�rddisken \
				   <li>Klicka <b>Webl�sarf�rhandsgranska</b> f�r att se sidan med en webl�sare

xsl.dhtml = Dynamisk<br>Webbsida
xsl.dhtml.tip = Skapar en Webbsida med <i>dynamiska</i> effekter.<br> \
				   <li>Klicka <b>Spara som...</b> f�r att spara filen till h�rddisken \
				   <li>Klicka <b>Webbl�sarf�rhandsgranska</b> f�r att se sidan med en webbl�sare <br> \
				  JavaScript m�ste vara aktiverad i webbl�saren.

xsl.debug = XML-Fil<br>(fels�kning)
export.xml.tip = Skapar en XML-fil f�r fels�kning.

xsl.pdf = Skriv ut PDF
xsl.pdf.tip = Skapar eller skriver ut en PDF-fil.<br> \
				<li>Klicka <b>Spara som...</b> f�r att spara filen till h�rddisken \
				<li>Klicka <b>Skriv ut...</b> f�r att skriva ut dokumentet \
				<li>Klicka <b>F�rhandsgranska</b> f�r att se dokumentet

xsl.tex = TeX-fil
xsl.tex.tip = Skapa en fil att bearbeta med TeX.

xsl.html.figs.tt = TrueType-figurer
xsl.html.figs.img = Bild-figurer
xsl.css.standalone = CSS-stilinst�llningar i en separat fil

xsl.html.img.dir = Katalog
xsl.create.images = Skapa bilder

xsl.pdf.embed = Anv�nd TrueType-fonter
xsl.pdf.font.dir = Ytterligare Fonter:

default.file.name = Schackparti


##############################################################################
#	Print Preview Dialog
##############################################################################

print.preview.page.one =en sida
print.preview.page.two =tv� sidor
print.preview.page.one.tip = visar en sida
print.preview.page.two.tip = visar tv� sidor

print.preview.ori.land =Landskap
print.preview.ori.port =Portr�tt
print.preview.ori.land.tip = Anv�nd landskapsorienterat papper
print.preview.ori.port.tip = Anv�nd portr�ttorienterat papper

print.preview.fit.page = Hela sidan
print.preview.fit.width = Sidbredd
print.preview.fit.textwidth = Textbredd
print.preview.next.page =
print.preview.previous.page =

preview.wait = Ett �gonblick...


##############################################################################
#	Online Update Dialog
##############################################################################

online.update.title	= Onlineuppdatering
online.update.tab.1	= Ny version
online.update.tab.2	= Vad �r nytt ?
online.update.tab.3 	= Viktiga anteckningar

update.install	= Ladda ner & Installera nu
update.download	= Ladda ner, Installera senare
update.mirror	= Ladda ner fr�n alternativ sida

download.file.progress 			= Laddar ner %file%
download.file.title			= Ladda ner
download.error.invalid.url		= Ogiltig URL: %p%.
download.error.connect.fail		= Anslutning till %p% misslyckades.
download.error.parse.xml		= Tolkningsfel: %p%.
download.error.version.missing	= Kunde inte l�sa version fr�n %p%.
download.error.os.missing		= Ingen version av jose hittades till ditt operativsystem.
download.error.browser.fail		= Kan inte visa %p%.
download.error.update			= Ett fel upptr�dde medan jose uppdaterades.\n Var god och uppdatera jose manuellt.

download.message.up.to.date		= Din installerade version �r uppdaterad.
download.message.success		= jose har blivit framg�ngsrikt uppdaterad till version %p% \n. Var god och starta om jose.

download.message			= \
  Versionen <b>%version%</b> finns fr�n <br>\
  <font color=blue>%url%</font><br>\
  Storlek: %size%

dialog.browser		= Var sn�ll och hj�lp mig starta din HTML-webbl�sare.\n Ange kommandot som anv�nds f�r att starta webbl�saren.
dialog.browser.title 	= Lokalisera webbl�saren

# deprecated
#online.report.title	= Rapport
#online.report.bug	= Felrapport
#online.report.feature	= Funktionsf�rfr�gan
#online.report.support	= Supportf�rfr�gan

#online.report.type	= Typ:
#online.report.subject	= �mne:
#online.report.description	= Beskrivning:
#online.report.email		= EMail:

#online.report.default.subject		= <�mne>
#online.report.default.description	= <Var god och f�rs�k beskriva vad som orsakade felet>
#online.report.default.email		= <din e-mail address = frivillig uppgift>
#online.report.info			= Denna rapport kommer att skickas till http://jose-chess.sourceforge.net

#online.report.success		= Din rapport har blivit skickad.
#online.report.failed		= Kunde inte skicka din rapport.
# deprecated


##########################################
# 	Error Messages
##########################################

error.not.selected 		= Var god och v�lj ett parti att spara.
error.duplicate.database.access = Var god och k�r inte tv� versioner av jose samtidigt \n\
	och anv�nd dem inte p� samma databas.\n\n\
	Det kan g�ra att data blir f�rlorat. \n\
	Var god och avsluta en version av  jose.
error.lnf.not.supported 	= Denna Se&K�nn finns inte \n p� den aktuella plattformen.

error.bad.uci = Detta verkar inte vara en UCI-schackmotor.\n �r du s�ker att det �r en UCI-schackmotor ?

error.bug	= <center><b>Ett ov�ntat fel har intr�ffat.</b></center><br><br> \
 Det skulle vara hj�lpsamt om du skickade en felrapport.<br>\
 Var god och f�rs�k beskriva de steg som orsakade felet <br>\
 och bifoga denna fil till din rapport: <br>\
  <center><b> %error.log% </b></center>

# errors in setup dialog

pos.error.white.king.missing	= Vit kung saknas.
pos.error.black.king.missing	= Svart kung saknas.
pos.error.too.many.white.kings	= F�r m�nga vita kungar.
pos.error.too.many.black.kings	= F�r m�nga svarta kungar.
pos.error.white.king.checked	= Vits kung f�r inte vara i schack.
pos.error.black.king.checked	= Svarts kung f�r inte vara i schack.
pos.error.white.pawn.base		= Vita b�nder f�r inte placeras p� den f�rsta raden.
pos.error.white.pawn.promo		= Vita b�nder f�r inte placeras p� den �ttonde raden.
pos.error.black.pawn.base		= Svarta b�nder f�r inte placeras p� den �ttonde raden.
pos.error.black.pawn.promo		= Svarta b�nder f�r inte placeras p� den f�rsta raden.
pos.warning.too.many.white.pieces	= F�r m�nga vita pj�ser.
pos.warning.too.many.black.pieces	= F�r m�nga svarta pj�ser.
pos.warning.too.many.white.pawns	= F�r m�nga vita b�nder.
pos.warning.too.many.black.pawns	= F�r m�nga svarta b�nder.
pos.warning.too.many.white.knights	= F�r m�nga vita springare.
pos.warning.too.many.black.knights	= F�r m�nga svarta springare.
pos.warning.too.many.white.bishops	= F�r m�nga vita l�pare.
pos.warning.too.many.black.bishops	= F�r m�nga svarta l�pare.
pos.warning.too.many.white.rooks	= F�r m�nga vita torn.
pos.warning.too.many.black.rooks	= F�r m�nga svarta torn.
pos.warning.too.many.white.queens	= F�r m�nga vita damer.
pos.warning.too.many.black.queens	= F�r m�nga svarta damer.
pos.warning.strange.white.bishops	= Vita l�pare p� samma f�rg ?
pos.warning.strange.black.bishops	= Svarta l�pare p� samma f�rg ?


##############################################################################
#	Style Names
##############################################################################


base			= Grundl�ggande stil
header			= Partidata
header.event	= Evenemang
header.site		= Plats
header.date		= Datum
header.round	= Runda
header.white	= Vit spelare
header.black	= Svart spelare
header.result	= Partiresultat
body			= Partitext
body.line		= Drag
body.line.0		= Huvudvariant
body.line.1		= Variation
body.line.2		= f�rsta subvariationen
body.line.3		= andra subvariationen
body.line.4		= tredje subvariationen
body.symbol		= Symboler
body.inline		= Diagram infogat i text
body.figurine	= Figurer
body.figurine.0	= Huvudvariant
body.figurine.1	= Variation
body.figurine.2	= f�rsta subvariationen
body.figurine.3	= andra subvariationen
body.figurine.4	= tredje subvariationen
body.comment	= Kommentarer
body.comment.0	= Huvudvariant
body.comment.1	= Variation
body.comment.2	= f�rsta subvariationen
body.comment.3	= andra subvariationen
body.comment.4	= tredje subvariationen
body.result		= Resultat
html.large      = Diagram p� Webbsida


##############################################################################
#	Task Dialogs (progress)
##############################################################################

dialog.progress.time 		= �terst�r: %time%

dialog.read-progress.title 	= jose - L�s Fil
dialog.read-progress.text 	= l�ser %fileName%

dialog.eco 			= Klassificera ECO
dialog.eco.clobber.eco 		= Skriv �ver ECO-koder
dialog.eco.clobber.name 	= Skriv �ver �ppningsnamn
dialog.eco.language 		= Spr�k:


##############################################################################
#	foreign language figurines (this block need not be translated)
##############################################################################

fig.langs = cs,da,nl,en,et,fi,fr,de,hu,is,it,no,pl,pt,ro,es,sv,ru,ca

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

lang.cs = Tjeckiska
lang.da = Danska
lang.nl = Nederl�ndska
lang.en = Engelska
lang.et = Estniska
lang.fi = Finska
lang.fr = Franska
lang.de = Tyska
lang.hu = Ungerska
lang.is = Isl�ndska
lang.it = Italienska
lang.no = Norska
lang.pl = Polska
lang.pt = Portugisiska
lang.ro = Rum�nska
lang.es = Spanska
lang.ca = Katalanska
lang.sv = Svenska
lang.ru = Ryska
lang.he = Hebreiska
lang.tr = Turkiska
lang.ukr = Ukrainska


##############################################################################
#	PGN Annotations
##############################################################################

pgn.nag.0    = null-kommentar
pgn.nag.1    	= !
pgn.nag.1.tip	= bra drag
pgn.nag.2	= ?
pgn.nag.2.tip	= d�ligt drag
pgn.nag.3    	= !!
pgn.nag.3.tip	= mycket bra drag
pgn.nag.4    	= ??
pgn.nag.4.tip	= mycket d�ligt drag
pgn.nag.5    	= !?
pgn.nag.5.tip	= intressant drag
pgn.nag.6    	= ?!
pgn.nag.6.tip	= tveksamt drag
pgn.nag.7    = forcerat drag
pgn.nag.7.tip = forcerat drag (alla andra drag f�rlorar snabbt) 
pgn.nag.8    = enda draget
pgn.nag.8.tip    = enda draget (inga andra bra alternativ)
pgn.nag.9    = s�msta draget
pgn.nag.10      = =
pgn.nag.10.tip   = remiartad position
pgn.nag.11      = =
pgn.nag.11.tip   = lika chancer, stilla position
pgn.nag.12.tip   = lika chancer, aktiv position
pgn.nag.13   = oklar
pgn.nag.13.tip   = oklar position
pgn.nag.14	= +=
pgn.nag.14.tip	= Vit ha en liten f�rdel
pgn.nag.15   	= =+
pgn.nag.15.tip 	= Svart har en liten f�rdel
pgn.nag.16	= +/-
pgn.nag.16.tip 	= Vit har en r�tt bra f�rdel
pgn.nag.17	= -/+
pgn.nag.17.tip 	= Svart har en r�tt bra f�rdel
pgn.nag.18	= +-
pgn.nag.18.tip  = Vit har en avg�rande f�rdel
pgn.nag.19	= -+
pgn.nag.19.tip 	= Svart har en avg�rande f�rdel
pgn.nag.20   = Vit har en f�rkrossande f�rdel (Svart borde ge upp)
pgn.nag.21   = Svart har en f�rkrossande f�rdel (Vit borde ge upp)
pgn.nag.22   = Vit �r i dragtv�ng (zugzwang)
pgn.nag.23   = Svart �r i dragtv�ng (zugzwang)
pgn.nag.24   = Vit har en liten utrymmesf�rdel
pgn.nag.25   = Svart har en liten utrymmesf�rdel
pgn.nag.26   = Vit har en ganska bra utrymmesf�rdel
pgn.nag.27   = Svart har en ganska bra utrymmesf�rdel
pgn.nag.28   = Vit har en avg�rande utrymmesf�rdel
pgn.nag.29   = Svart har en avg�rande utrymmesf�rdel
pgn.nag.30   = Vit har liten tids (utvecklings) f�rdel
pgn.nag.31   = Svart har en liten tids (utvecklings) f�rdel
pgn.nag.32   = Vit har en ganska bra tids (utvecklings) f�rdel
pgn.nag.33   = Svart har en ganska bra tids (utvecklings) f�rdel
pgn.nag.34   = Vit har en avg�rande tids (utvecklings) f�rdel
pgn.nag.35   = Svart har en avg�rande tids (utvecklings) f�rdel
pgn.nag.36   = Vit har ett initiativ
pgn.nag.37   = Svart har ett initiativ
pgn.nag.38   = Vit har ett l�ngvarigt initiativ
pgn.nag.39   = Svart har en l�ngvarigt initiativ
pgn.nag.40   = Vit har en attack
pgn.nag.41   = Svart har en attack
pgn.nag.42   = Vit har en otillr�cklig  kompensation f�r materiellt underskott
pgn.nag.43   = Svart har en otillr�cklig kompensation f�r materiellt underskott
pgn.nag.44   = Vit har en tillr�cklig kompensation f�r materiellt underskott
pgn.nag.45   = Svart har en tillr�cklig kompensation f�r materiellt underskott
pgn.nag.46   = Vit har mer �n en tillr�cklig kompensation f�r materiellt underskott
pgn.nag.47   = Svart har mer �n en tillr�cklig kompensation f�r materiellt underskott
pgn.nag.48   = Vit har en liten centerkontrollf�rdel
pgn.nag.49   = Svart har en liten centerkontrollf�rdel
pgn.nag.50   = Vit har en ganska bra centerkontrollf�rdel
pgn.nag.51   = Svart har en ganska bra centerkontrollf�rdel
pgn.nag.52   = Vit har en avg�rande centerkontrollf�rdel
pgn.nag.53   = Svart har en avg�rande centerkontrollf�rdel
pgn.nag.54   = Vit har en liten kungssidekontrollf�rdel
pgn.nag.55   = Svart har en liten kungssidekontrollf�rdel
pgn.nag.56   = Vit har en ganska bra kungssidekontrollf�rdel
pgn.nag.57   = Svart har en ganska bra kungssidekontrollf�rdel
pgn.nag.58   = Vit har en avg�rande kungssidekontrollf�rdel
pgn.nag.59   = Svart har en avg�rande kungssidekontrollf�rdel
pgn.nag.60   = Vit har en liten damsidekontrollf�rdel
pgn.nag.61   = Svart har en liten damsidekontrollf�rdel
pgn.nag.62   = Vit har en ganska bra damsidekontrollf�rdel
pgn.nag.63   = Svart har en ganska bra damsidekontrollf�rdel
pgn.nag.64   = Vit har en avg�rande damsidekontrollf�rdel
pgn.nag.65   = Svart har en avg�rande damsidekontrollf�rdel
pgn.nag.66   = Vit har en s�rbar f�rsta rad
pgn.nag.67   = Svart har en s�rbar f�rsta rad
pgn.nag.68   = Vit har en v�l skyddad f�rsta rad
pgn.nag.69   = Svart har en v�l skyddad f�rsta rad
pgn.nag.70   = Vit har en d�ligt skyddad kung
pgn.nag.71   = Svart har en d�ligt skyddad kung
pgn.nag.72   = Vit har en bra skyddad kung
pgn.nag.73   = Svart har en bra skyddad kung
pgn.nag.74   = Vit har en d�ligt placerad kung
pgn.nag.75   = Svart har en d�ligt placerad kung
pgn.nag.76   = Vit har en v�lplacerad kung
pgn.nag.77   = Svart har en v�lplacerad kung
pgn.nag.78   = Vit har en mycket svag bondestruktur
pgn.nag.79   = Svart har en mycket svag bondestruktur
pgn.nag.80   = Vit har en m�ttligt bra bondestruktur
pgn.nag.81   = Svart har en m�ttligt bra bondestruktur
pgn.nag.82   = Vit har en ganska stark bondestruktur
pgn.nag.83   = Svart har en ganska stark bondestruktur
pgn.nag.84   = Vit har en v�ldigt stark bondestruktur
pgn.nag.85   = Svart har en v�ldigt stark bondestruktur
pgn.nag.86   = Vit har en d�lig springarplacering
pgn.nag.87   = Svart har en d�lig springarplacering
pgn.nag.88   = Vit har en bra springarplacering
pgn.nag.89   = Svart har en bra springarplacering
pgn.nag.90   = Vit har en d�lig l�parplacering
pgn.nag.91   = Svart har en d�lig l�parplacering
pgn.nag.92   = Vit har en bra l�parplacering
pgn.nag.93   = Svart har en bra l�parplacering
pgn.nag.94   = Vit har en d�lig tornplacering
pgn.nag.95   = Svart har en d�lig tornplacering
pgn.nag.96   = Vit har en bra tornplacering
pgn.nag.97   = Svart har en bra tornplacering
pgn.nag.98   = Vit har en d�lig damplacering
pgn.nag.99   = Svart har en d�lig damplacering
pgn.nag.100  = Vit har en bra damplacering
pgn.nag.101  = Svart har en bra damplacering
pgn.nag.102  = Vit har en d�lig pj�skoordination
pgn.nag.103  = Svart har en d�lig pj�skoordination
pgn.nag.104  = Vit har en bra pj�skoordination
pgn.nag.105  = Svart har en bra pj�skoordination
pgn.nag.106  = Vit har spelat �ppningen mycket d�ligt
pgn.nag.107  = Svart har spelat �ppningen mycket d�ligt
pgn.nag.108  = Vit har spelat �ppningen d�ligt
pgn.nag.109  = Svart har spelat �ppningen d�ligt
pgn.nag.110  = Vit har spelat �ppningen bra
pgn.nag.111  = Svart har spelat �ppningen bra
pgn.nag.112  = Vit har spelat �ppningen mycket bra
pgn.nag.113  = Svart har spelat �ppningen mycket bra
pgn.nag.114  = Vit har spelat mittspelet mycket d�ligt
pgn.nag.115  = Svart har spelat mittspelet mycket d�ligt
pgn.nag.116  = Vit har spelat mittspelet d�ligt
pgn.nag.117  = Svart har spelat mittspelet d�ligt
pgn.nag.118  = Vit har spelat mittspelet bra
pgn.nag.119  = Svart har spelat mittspelet bra
pgn.nag.120  = Vit har spelat mittspelet mycket bra
pgn.nag.121  = Svart har spelat mittspelet mycket bra
pgn.nag.122  = Vit har spelat slutspelet mycket d�ligt
pgn.nag.123  = Svart har spelat slutspelet mycket d�ligt
pgn.nag.124  = Vit har spelat slutspelet d�ligt
pgn.nag.125  = Svart har spelat slutspelet d�ligt
pgn.nag.126  = Vit har spelat slutspelet bra
pgn.nag.127  = Svart har spelat slutspelet bra
pgn.nag.128  = Vit har spelat slutspelet mycket bra
pgn.nag.129  = Svart har spelat slutspelet mycket bra
pgn.nag.130  = Vit har lite motspel
pgn.nag.131  = Svart har lite motspel
pgn.nag.132  = Vit har ganska bra motspel
pgn.nag.133  = Svart har ganska bra motspel
pgn.nag.134  = Vit har avg�rande motspel
pgn.nag.135  = Svart har avg�rande motspel
pgn.nag.136  = Vit har ganska bra tidskontrolltryck
pgn.nag.137  = Svart har ganska bra tidskontrolltryck
pgn.nag.138  = Vit har kraftigt tidskontrolltryck
pgn.nag.139  = Svart har kraftigt tidskontrolltryck

# following codes are defined by Fritz or SCID

pgn.nag.140  	= med iden
pgn.nag.141  	= mot
pgn.nag.142  	= �r b�ttre
pgn.nag.143  	= �r s�mre
pgn.nag.144  	= =
pgn.nag.144.tip = �r ekvivalent
pgn.nag.145  	= RR
pgn.nag.145.tip	= redaktionell kommentar?
pgn.nag.146  	= N
pgn.nag.146.tip = Nyhet
pgn.nag.147     = Svag punkt
pgn.nag.148     = Slutspel
pgn.nag.149		= fil
pgn.nag.150		= Diagonal
pgn.nag.151		= Vit har ett l�parpar
pgn.nag.152     = Svart har ett l�parpar
pgn.nag.153		= L�pare med olika f�rger
pgn.nag.154		= L�pare med samma f�rg
# following codes are defined by us (equivalent to Informator symbols)
# (is there a standard definition for these symbols ?)

pgn.nag.156		= fribonde
pgn.nag.157		= fler b�nder
pgn.nag.158		= med
pgn.nag.159		= utan
pgn.nag.161		= se
pgn.nag.163		= rad


# defined by SCID:
pgn.nag.190		= etc.
pgn.nag.191		= dubblerade b�nder
pgn.nag.192		= separade b�nder
pgn.nag.193		= f�renade b�nder
pgn.nag.194     = H�ngande b�nder 
pgn.nag.195     = Bakv�nda b�nder

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
