
##############################################################################
#	Swedish Translation for jose
#       Hans Eriksson (hans.ericson@bredband.net)
##############################################################################

#	Application Name
application.name	= jose

#	Frame Titles
window.board	= Schackbräde
window.console	= Schackmotor-inställningar
window.database	= Databas
window.filter	= Filter
window.list	= Partilista
window.clock	= Klocka
window.game	= Parti
window.engine	= Schackmotor
window.eval     = Värdering

window.collectionlist	= Databas
window.query		= Sök
window.gamelist		= Databas

window.sqlquery		= SQL Sökning
window.sqllist		= Resultat

window.toolbar.1	= Verktygsrad 1
window.toolbar.2	= Verktygsrad 2
window.toolbar.3	= Verktygsrad 3
window.toolbar.symbols	= Kommentarer
window.help		        = Hjälp
window.print.preview    = Förhandsgranskning

# dialog titles

dialog.option	= Alternativ
dialog.about	= Om jose
dialog.animate  = Spela upp
dialog.setup	= Inställningar
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
menu.file.open		= Öppna...
menu.file.open.tip  	= Öppnar en PGN-fil
menu.file.open.url	= Öppna URL...
menu.file.close		= Stäng
menu.file.close.tip	= Stänger det aktuella fönstret
menu.file.save		= Spara
menu.file.save.tip  	= Sparar det aktuella partiet i databasen
menu.file.save.as    	= Spara som...
menu.file.save.as.tip	= Sparar en ny kopia av det aktuella partiet i databasen
menu.file.save.all   	= Spara alla
menu.file.save.all.tip  = Sparar alla öppna partier i databasen
menu.file.revert	= Återgå till partiet
menu.file.print		= Skriv ut...
menu.file.print.tip	= Skriver ut det aktuella partiet
menu.file.print.setup	= Skrivarinställningar...
menu.file.print.setup.tip = Ställer in skrivare och pappersstorlek
menu.file.print.preview = Förhandsgranskning...
menu.file.quit		= Avsluta
menu.file.quit.tip	= Avslutar Jose

# Edit Menu

menu.edit		= Redigera
menu.edit.undo		= Ångra (%action%)
menu.edit.cant.undo 	= Ångra
menu.edit.redo		= Gör om (%action%)
menu.edit.cant.redo 	= Gör om
menu.edit.select.all    = Välj alla
menu.edit.select.none   = Välj ingen
menu.edit.cut		= Klipp ut
menu.edit.copy		= Kopiera
menu.edit.copy.fen  = FEN sträng
menu.edit.copy.fen.tip = Kopierar den aktuella positionen till klippboken (som en FEN-sträng)
menu.edit.copy.img  = Bild+Bakgrund
menu.edit.copy.img.tip = Kopierar den aktuella positionen till klippboken (som en bild med bakgrund)
menu.edit.copy.imgt  = Bild
menu.edit.copy.imgt.tip = Kopierar den aktuella positionen till klippboken (som en bild)
menu.edit.copy.text  = Textdiagram
menu.edit.copy.text.tip = Kopierar den aktuella positionen till klippboken (som ett diagram med text)

menu.edit.copy.pgn  = Kopiera PGN
menu.edit.copy.pgn.tip = Kopierar det aktuella partiet till klippboken (som en PGN-text)
menu.edit.paste		= Klistra in
menu.edit.paste.tip = Klistrar in från Klippboken
menu.edit.paste.copy		= Klistra in kopierade partier
menu.edit.paste.copy.tip 	= Klistrar in partier från klippboken
menu.edit.paste.same 	= Klistra in partier
menu.edit.paste.same.tip = Klistrar in partier från klippboken
menu.edit.paste.pgn = Klistra in PGN
menu.edit.paste.pgn.tip = Klistrar in ett parti från klippboken (som en PGN-text)
menu.edit.clear		= Töm
menu.edit.option	= Alternativ...
menu.edit.option.tip	= Öppnar Alternativinställningarna

menu.edit.games			= Databas
menu.edit.collection.new 	= Ny katalog
menu.edit.collection.rename 	= Byt namn
menu.edit.empty.trash		= Töm skräpkorg
menu.edit.restore		= Ta tillbaka

#menu.edit.position.index    = Uppdatera Positionsindex
menu.edit.search.current    = Sök efter denna position
menu.edit.ecofy             = Klassificera ECO
menu.edit.search.current    = Sök efter denna position
menu.edit.ecofy             = Klassificera ECO

menu.edit.style = Format
menu.edit.bold = Fet
menu.edit.italic = Lutande
menu.edit.underline = Understruken
menu.edit.plain = Enkel text
menu.edit.left = Vänsterjusterad
menu.edit.center = Centrerad
menu.edit.right = Högerjusterad
menu.edit.larger = Öka textstorlek
menu.edit.smaller = Minska textstorlek
menu.edit.color = Textfärg

# Game Menu

menu.game		= Parti
menu.game.details	= Partidata...
menu.game.analysis  	= Analysmod
menu.game.navigate	= Gå till...
menu.game.time.controls = Tidskontroller
menu.game.time.control = Tidskontroll
menu.game.details.tip 	= Ändra partidata (Spelare,etc.)
menu.game.hint		= Be om hjälp
menu.game.hint.tip  = Visar hjälp
menu.game.draw		= Erbjud remi
menu.game.resign	= Ge upp
menu.game.2d		= 2D utseende
menu.game.3d		= 3D utseende
menu.game.flip		= Vänd schackbrädet
menu.game.coords	= Koordinater
menu.game.coords.tip	= Ändrar koordinatvisning
menu.game.animate 	= Spela upp...
menu.game.previous 	= Förra partiet
menu.game.next 		= Nästa parti
menu.game.close 	= Stäng
menu.game.close.tip 	= Stäng aktuellt parti
menu.game.close.all 	= Stäng alla
menu.game.close.all.tip = Stänger alla öppna partier
menu.game.close.all.but = Stäng alla UTOM detta
menu.game.close.all.but.tip = Stänger alla öppna partier utom det aktuella partiet
menu.game.setup		= Sätt up ställning

menu.game.copy.line = Kopiera variant
menu.game.copy.line.tip = Kopierar denna variant till klippboken
menu.game.paste.line = Klistra in variant
menu.game.paste.line.tip = Infogar denna variant i det aktuella partiet

# Window Menu

menu.window		= Fönster
menu.window.fullscreen 	= Helskärmsvisning
menu.window.reset   	= Återställ layouten

# Help Menu

menu.help		= Hjälp
menu.help.splash	= Om...
menu.help.about		= Information...
menu.help.license	= Licens...
menu.help.context   	= Hjälp om Jose
menu.help.manual    	= Manual
menu.help.web		= jose på internet

menu.web.home		= Hemsida
menu.web.update		= Uppdatering via internet 
menu.web.download	= Ladda ner
menu.web.report		= Felrapporter
menu.web.support	= Supportförfrågningar
menu.web.feature	= Funktionsförfrågningar
menu.web.forum		= Forum
menu.web.donate     = Donera
menu.web.browser	= Välj webläsare...


##############################################################################
# 	Context Menu
##############################################################################

panel.hide		= Dölj
panel.hide.tip		= Döljer detta fönster
panel.undock		= Nytt fönster
panel.undock.tip	= Öppnar ett nytt fönster
panel.move		= Flytta
panel.move.tip		= Flyttar detta fönster till en annan position
panel.dock		= Stäng
panel.dock.tip		= Stänger detta fönster

panel.orig.pos = Originalpositionen
panel.dock.here = Stäng fönstret här
panel.undock.here = Nytt fönster här

#################
# Document Panel
#################

# deprecated:
tab.place 	= Kommentarsplacering
tab.place.top 	= Över
tab.place.left 	= Vänster
tab.place.bottom = Under
tab.place.right = Höger
#

tab.layout 		= Kommentarslayout
tab.layout.wrap 	= Vänd
tab.layout.scroll 	= Scrolla

doc.menu.annotate 	= Kommentera
doc.menu.delete.comment = Ta bort kommentar
doc.menu.line.promote 	= Lägg till variant
doc.menu.line.delete 	= Ta bort variant
doc.menu.line.cut 	= Avsluta variant
doc.menu.line.uncomment = Tar bort alla kommentarer
doc.menu.remove.annotation = -Inga-
doc.menu.more.annotations = fler...

tab.untitled 	= Namnlös
confirm 	= Bekräfta
confirm.save.one = Spara aktuellt parti ?
confirm.save.all = Spara ändrade partier ?

dialog.confirm.save = Spara
dialog.confirm.dont.save = Spara inte

dialog.engine.offers.draw = %engine% erbjuder remi.

dialog.accept.draw = Acceptera remi
dialog.decline.draw = Avböj remi

dialog.autoimport.title = Importera
dialog.autoimport.ask = Filen ^0 har ändrats på disken \n Öppna den igen ?

dialog.paste.message = Du är på väg att infoga data från klippboken. \n\
     Vill du flytta partierna, eller skapa en ny kopia ?
dialog.paste.title = Klistra in partier
dialog.paste.same = Flytta
dialog.paste.copy = Kopiera

###################
# Game Navigation
###################

move.first	= Början av partiet
move.backward 	= Tillbaka
move.delete 	= Ta tillbaka förra draget
engine.stop 	= Stopp
move.start 	= Dra nu
move.forward 	= Framåt
move.last 	= Slutet av partiet
move.animate	 = Spela upp


##################################
# Engine Panel
##################################

engine.paused.tip 	= %engine% är stoppad
engine.thinking.tip 	= %engine% tänker på sitt nästa drag
engine.pondering.tip 	= %engine% tänker på ditt nästa drag
engine.analyzing.tip 	= %engine% analyserar
engine.hint.tip 	= Hjälp: %move%

engine.paused.title 	= %engine%
engine.thinking.title 	= %engine% tänker
engine.pondering.title 	= %engine% tänker på ditt drag
engine.analyzing.title 	= %engine% analyserar

plugin.name 		= %name% %version%
plugin.name.author 	= %name% %version% av %author%

plugin.book.move 	= Bok
plugin.book.move.tip 	= Bokdrag
plugin.hash.move 	= Hashtabell
plugin.hash.move.tip 	= Utvärderat från hashtabellen
plugin.tb.move 		= Slutspelstabell
plugin.tb.move.tip 	= Utvärderat från slutspelstabellen

plugin.currentmove.title       = Drag
plugin.depth.title      = Sökdjup
plugin.elapsed.time.title  = Tid
plugin.nodecount.title  = Noder
plugin.nps.title        = Noder/sekund

plugin.currentmove = %move%
plugin.currentmove.max = %move% %moveno%/%maxmove%

plugin.currentmove.tip = Det nu värderade draget är %move%.
plugin.currentmove.max.tip = Det nu värderade draget är %move%. (nr. %moveno% av %maxmove%)

plugin.depth 		= %depth%
plugin.depth.tip 	= Sökdjup: %depth% ply

plugin.depth.sel 	= %depth% (%seldepth%)
plugin.depth.sel.tip 	= Sökdjup: %depth% ply, Valt sökdjup: %seldepth% ply

plugin.white.mates 	= +#%eval%
plugin.white.mates.tip 	= Vit gör matt i %eval% drag
plugin.black.mates 	= -#%eval%
plugin.black.mates.tip 	= Svart gör matt i %eval% drag

plugin.evaluation 	= %eval%
plugin.evaluation.tip 	= Positionsvärdet är %eval%

plugin.line.tip = Beräknad variant

plugin.elapsed.time = %time%
plugin.elapsed.time.tip = Använd tid för denna beräkning.

plugin.nodecount 	= %antal noder%
plugin.nodecount.tip 	= %antal noder% positioner har utvärderats

plugin.nps      = %nps%
plugin.nps.tip  = %nps% noder utvärderas per sekund

plugin.pv.history = Expertmod

restart.plugin		= Återstarta schackmotor


######################
# Board Panel
######################

wait.3d = Laddar 3D. Var god vänta...

message.result			= Resultat 
message.white 			= Vit
message.black 			= Svart
message.mate 			= Matt. \n %player% vinner.
message.stalemate		= Patt. \n Partiet är remi.
message.draw3			= Position upprepad 3 gånger. \n Partiet är remi.
message.draw50			= Ingen pjäs tagen på 50 drag. \n Partiet är remi.
message.resign			= %player% ger upp. \n Du vinner.
message.time.draw		= Tiden har gått ut. \n Partiet är remi.
message.time.lose		= Tiden har gått ut. \n %player% vinner.


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
dialog.button.ok.tip		= Klicka här för att göra ändringarna
dialog.button.cancel		= Avbryt
dialog.button.cancel.tip	= Klicka här för att stänga  dialogen utan att göra ändringar
dialog.button.apply		= Verkställ
dialog.button.apply.tip		= Klicka här för att verkställa förändringarna omedelbart
dialog.button.revert		= Återgå
dialog.button.revert.tip	= Klicka här för att ångra förändringarna
dialog.button.clear		= Rensa
dialog.button.delete		= Ta bort
dialog.button.yes		= Ja
dialog.button.no		= Nej
dialog.button.next		= Nästa
dialog.button.back		= Backa
dialog.button.close		= Stäng
dialog.button.help		= Hjälp
dialog.button.help.tip		= Visa Hjälp om funktioner

dialog.button.commit		= Verkställ
dialog.button.commit.tip	= Klicka här för att verkställa uppdateringar
dialog.button.rollback		= Ångra
dialog.button.rollback.tip	= Klicka här för att ångra uppdateringar

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

filechooser.overwrite 	= Skriva över existerande "%file.name%" ?
filechooser.do.overwrite = Skriv över

#################
# Color Chooser
#################

colorchooser.texture	= Bakgrund
colorchooser.preview	= Förhandsgranska
colorchooser.gradient   = Nyans
colorchooser.gradient.color1 = Första färg
colorchooser.gradient.color2 = Andra färg
colorchooser.gradient.cyclic = cyklisk

colorchooser.texture.mnemonic = T
colorchooser.gradient.mnemonic = G

animation.slider.fast   = snabb
animation.slider.slow   = långsam

##############################################################################
# Option dialog
##############################################################################

# Tab Titles

dialog.option.tab.1	= Spelare
dialog.option.tab.2	= Schackbräde
dialog.option.tab.3	= Färger
dialog.option.tab.4	= Tid
dialog.option.tab.5     = Schackmotor
dialog.option.tab.6 = Opening Book
# TODO
dialog.option.tab.7     = 3D
dialog.option.tab.8	= Fonter

# User settings

dialog.option.user.name		= Namn
dialog.option.user.language	= Språk
dialog.option.ui.look.and.feel	= Se & känn

doc.load.history	= Ladda tidigare partier
doc.classify.eco	= Klassificera  öppning utifrån ECO
doc.associate.pgn   = Öppna PGN filer med jose

dialog.option.animation = Spela upp
dialog.option.animation.speed = Hastighet

dialog.option.doc.write.mode	= Infoga nytt drag
write.mode.new.line		= Ny variant
write.mode.new.main.line	= Ny huvudvariant
write.mode.overwrite		= Skriv över
write.mode.ask			= Fråga
write.mode.dont.ask		= Fråga inte något mer
# Don't ask anymore
write.mode.cancel		= Avbryt

board.animation.hints   = Visa hjälp under uppspelning

dialog.option.sound = Ljud
dialog.option.sound.moves.dir = Dragannonseringar:
sound.moves.engine  = Annonsera schackmotordrag
sound.moves.ack.user = Bekräfta spelardrag
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

doc.panel.antialias		= Använd antialiasing fonter

# Notation

dialog.option.doc.move.format	= Notation:
move.format			= Notation
move.format.short		= Kort
move.format.long		= Lång
move.format.algebraic		= Algebraisk
move.format.correspondence	= Korrespondens
move.format.english		= Engelsk
move.format.telegraphic		= Telegrafisk

# Colors

dialog.option.board.surface.light	= Ljusa rutor
dialog.option.board.surface.dark	= Mörka rutor
dialog.option.board.surface.white	= Vita pjäser
dialog.option.board.surface.black	= Svarta pjäser

dialog.option.board.surface.background	= Bakgrund
dialog.option.board.surface.frame	= Brädutseende
dialog.option.board.surface.coords	= Koordinater

dialog.option.board.3d.model            = Modell:
dialog.option.board.3d.clock            = Klocka
dialog.option.board.3d.surface.frame	= Yta:
dialog.option.board.3d.light.ambient	= Bakgrundsljus:
dialog.option.board.3d.light.directional = Rikningsljus:
dialog.option.board.3d.knight.angle     = Springare:

board.surface.light	= Ljusa rutor
board.surface.dark	= Mörka rutor
board.surface.white	= Vita pjäser
board.surface.black	= Svarta pjäser
board.hilite.squares 	= Färglägg rutor

# Time Controls

dialog.option.time.control      = Tidskontroll
dialog.option.phase.1		= Tidskontroll 1
dialog.option.phase.2		= Tidskontroll 2
dialog.option.phase.3		= Tidskontroll 3
dialog.option.all.moves		= alla
dialog.option.moves.in 		= drag på
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

plugin.add =lägg till
plugin.delete =ta bort
plugin.duplicate =duplicera
plugin.add.tip = lägger till en ny schackmotor
plugin.delete.tip = tar bort konfigurationen
plugin.duplicate.tip = duplicerar konfigurationen

dialog.option.plugin.file 	= Konfigurationsfil:
dialog.option.plugin.name 	= Namn:
dialog.option.plugin.version 	= Version:
dialog.option.plugin.author 	= Konstruktör:
dialog.option.plugin.dir 	= Katalog:
dialog.option.plugin.logo 	= Logga:
dialog.option.plugin.startup 	= Starta:

dialog.option.plugin.exe = Exekuterbar fil:
dialog.option.plugin.args = Parametrar:
dialog.option.plugin.default = Standardinställningar

plugin.info                 = Generell Information
plugin.protocol.xboard      = XBoard-protokoll
plugin.protocol.uci         = UCI-protokoll
plugin.options              = Schackmotor-alternativ
plugin.startup              = Fler alternativ
plugin.show.logos           = Visa loggor
plugin.show.text            = Visa Text

plugin.switch.ask           = Du har valt en annan schackmotor.\n Vill du starta den nu ?
plugin.restart.ask          = Du har ändrat några schackmotor-inställningar.\n Vill du starta om den nu ?
plugin.show.info            = Visa "info"
plugin.log.file             = Logg till fil

# UCI option name
plugin.option.Ponder        = Tänkande
plugin.option.Random        = Slumpmässig
plugin.option.Hash          = Hashtabellstorlek (MB)
plugin.option.NalimovPath   = Sökväg till Nalimov-slutspelstabeller
plugin.option.NalimovCache  = Cache för Nalimov-slutspelstabeller (MB)
plugin.option.OwnBook       = Använd öppningsbok
plugin.option.BookFile      = Öppningsbok
plugin.option.BookLearning  = Boklärande
plugin.option.MultiPV       = Primära variationer
plugin.option.ClearHash     = Rensa Hashtabeller
plugin.option.UCI_ShowCurrLine  = Visa aktuell variant
plugin.option.UCI_ShowRefutations = Visa vederläggningar
plugin.option.UCI_LimitStrength = Begränsa spelstyrka
plugin.option.UCI_Elo       = ELO
plugin.option.UCI_EngineAbout = 

# 3D Settings

board.surface.background	= Bakgrund
board.surface.coords		= Koordinater
board.3d.clock              	= Klocka
board.3d.shadow			= Skuggor
board.3d.reflection		= Reflektioner
board.3d.anisotropic        	= Anisotropiskt filter
board.3d.fsaa               	= Helskärms antialiasing (fsaa)

board.3d.surface.frame		= Schackbräde
board.3d.light.ambient		= Bakgrundsljus
board.3d.light.directional	= Riktningsljus
board.3d.screenshot		= Skärmdump
board.3d.defaultview    = Standardutseende

# Text Styles

font.color 	= Färg
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
column.collection.lastmodified 	= Ändrad

column.game.index 	= Index
column.game.white.name 	= Vit
column.game.black.name 	= Svart
column.game.event 	= Evenemang
column.game.site 	= Plats
column.game.date 	= Datum
column.game.result 	= Resultat
column.game.round 	= Runda
column.game.board 	= Schackbräde
column.game.eco 	= ECO
column.game.opening 	= Öppning
column.game.movecount 	= Drag
column.game.annotator 	= Kommentator
column.game.fen     = Startposition

#deprecated
column.problem.author 	= Konstruktör
column.problem.source 	= Källa
column.problem.number 	= Nr.
column.problem.date 	= Datum
column.problem.stipulation = Stipulation.
column.problem.dedication = Dedikation
column.problem.award = Pris
column.problem.solution = Lösning
column.problem.cplus = C+
column.problem.genre = Genre
column.problem.keyword = Nyckelord
#deprecated

bootstrap.confirm 	= Datakatalogen '%datadir%' finns inte.\n Vill du skapa en ny katalog ? 
bootstrap.create 	= Skapa datakatalog

edit.game = Öppna
edit.all = Öppna alla
dnd.move.top.level	= Gå till övre nivå


##############################################################################
#  Search Panel
##############################################################################

# Tab Titles
dialog.query.info 		= Information
dialog.query.comments 		= Kommentarer
dialog.query.position 		= Position

dialog.query.search 	= Sök
dialog.query.clear 	= Rensa
dialog.query.search.in.progress = Söker...

dialog.query.0.results 	= Inga Resultat
dialog.query.1.result 	= Ett Resultat
dialog.query.n.results 	= %count% Resultat

dialog.query.white		= Vit:
dialog.query.black		= Svart:

dialog.query.flags 		= Alternativ
dialog.query.color.sensitive 	= Färgkänslig
dialog.query.swap.colors 	=Byt färg
dialog.query.swap.colors.tip = Byter färger
dialog.query.case.sensitive 	= Versalkänslig
dialog.query.soundex 		= Låter som
dialog.query.result 		= Resultat
dialog.query.stop.results   =

dialog.query.event 		= Evenemang:
dialog.query.site 		= Plats:
dialog.query.eco 		= ECO:
dialog.query.annotator 		= Kommentator:
dialog.query.to 		= till
dialog.query.opening 		= Öppning:
dialog.query.date 		= Datum:
dialog.query.movecount 		= Drag:

dialog.query.commenttext 	= Kommentar:
dialog.query.com.flag 		= har kommentarer
dialog.query.com.flag.tip 	= sök efter partier med kommentarer

dialog.query.var.flag 		= har variationer
dialog.query.var.flag.tip 	= sök efter partier med variationer

dialog.query.errors 		= Fel i Sökuttryck:
query.error.date.too.small 	= Datumet är för litet
query.error.movecount.too.small = Antalet drag är för litet
query.error.eco.too.long 	= Avvänd tre tecken för ECO-koder
query.error.eco.character.expected = ECO-koder måste börja med A,B,C,D,eller E
query.error.eco.number.expected = ECO-koder består av en bokstav och ett tal från 0 till 99
query.error.number.format 	= Fel nummerformat
query.error.date.format 	= Fel datumformat

query.setup.enable 		= Sök position
query.setup.next 		= Nästa drag:
query.setup.next.white 		= Vit
query.setup.next.white.tip 	= hittar positioner där vit drar sedan
query.setup.next.black 		= Svart
query.setup.next.black.tip 	= söker positioner där svart drar sedan
query.setup.next.any 		= Vit eller svart
query.setup.next.any.tip 	= söker positioner där någon färg drar sedan
query.setup.reversed 		= Sök ombytta färger
query.setup.reversed.tip 	= söker identiska positioner med ombytta färger
query.setup.var 		= Sök variationer
query.setup.var.tip 		= söker inom variationer



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
dialog.details.board 	= Bräde:

dialog.details.white 	= Vit
dialog.details.black 	= Svart
dialog.details.name 	= Namn:
dialog.details.elo 	= ELO:
dialog.details.title 	= Titel:
dialog.details.result 	= Resultat:

dialog.details.eco 	= ECO:
dialog.details.opening 	= Öppning:
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
dialog.setup.copy	= Kopiera från huvudfönstret

dialog.setup.next.white	= Vit drar
dialog.setup.next.black	= Svart drar
dialog.setup.move.no	= Drag Nr.

dialog.setup.castling		= Rockad
dialog.setup.castling.wk	= Vit 0-0
dialog.setup.castling.wk.tip	= Vit kort rockad
dialog.setup.castling.wq	= Vit 0-0-0
dialog.setup.castling.wq.tip	= Vit lång rockad
dialog.setup.castling.bk	= Svart 0-0
dialog.setup.castling.bk.tip	= Svart kort rockad
dialog.setup.castling.bq	= Svart 0-0-0
dialog.setup.castling.bq.tip	= Svart lång rockad
dialog.setup.invalid.fen    = Felaktig FEN-sträng.

##############################################################################
#	About dialog
##############################################################################

dialog.about.tab.1	= jose
dialog.about.tab.2	= Databas
dialog.about.tab.3	= Medhjälpare
dialog.about.tab.4	= System
dialog.about.tab.5	= 3D
dialog.about.tab.6	= Licens

dialog.about.gpl	=   Detta program distribueras under villkoren i GNU General Public License

dialog.about.2	=	<b>%dbname%</b> <br> %dbversion% <br><br> \
					Server URL: %dburl%

dialog.about.MySQL = www.mysql.com

dialog.about.3	=	<b>Översättningar:</b><br>\
			Frederic Raimbault, José de Paula, \
			Agustín Gomila, Alex Coronado, \
			Harold Roig, Hans Eriksson, \
			Guido Grazioli, Tomasz Sokól, "Direktx" <br>\
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
			Andreas Güttinger, Randy Countryman


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

dialog.about.5.no3d = Java3D är nu inte tillgänglig
dialog.about.5.model =

dialog.about.5.native = Ursprunglig plattform: &nbsp;
dialog.about.5.native.unknown = nu okänd

##############################################################################
#	Export/Print Dialog
##############################################################################

dialog.export = Exportera & Skriv ut
dialog.export.tab.1 = Resultat
dialog.export.tab.2 = Ställa in för utskrift
dialog.export.tab.3 = Stilar

dialog.export.print = Skriv ut...
dialog.export.save = Spara
dialog.export.saveas = Spara som...
dialog.export.preview = Förhandsgranska
dialog.export.browser = Webbläsarförhandsgranskning

dialog.export.paper = Papper
dialog.export.orientation = Orientering
dialog.export.margins = Marginaler

dialog.export.paper.format = Papper:
dialog.export.paper.size = Storlek:

dialog.print.custom.paper = Ställa in

dialog.export.margin.top = Ovan:
dialog.export.margin.bottom = Under:
dialog.export.margin.left = Vänster:
dialog.export.margin.right = Höger:

dialog.export.ori.port = Porträtt
dialog.export.ori.land = Landskap

dialog.export.games.0 = Du har inte valt några partier att skriva ut.
dialog.export.games.1 = Du har valt <b>ett parti</b> för utskrift.
dialog.export.games.n = Du har valt <b>%n% partier</b> för utskrift.
dialog.export.games.? = Du har valt ett <b>okänt</b> antal partier att skriva ut.

dialog.export.confirm = Är du säker ?
dialog.export.yes = Skriv ut allt

# xsl stylesheet options

export.pgn = PGN-fil
export.pgn.tip = Exporterar partier som en Portable Game Notation(PGN)-fil.

print.awt = Skriv ut
print.awt.tip = Skriver ut partier från skärmen.<br> \
				    <li>Klicka <b>Skriv ut...</b> för att skriva ut till en ansluten skrivare \
				    <li>Klicka <b>Förhandsgranska</b> för att se dokumentet

xsl.html = HTML<br>Webbsida
xsl.html.tip = Skapar en Webbsida  (HTML-fil).<br> \
				   <li>Klicka <b>Spara som...</b> för att spara filen till hårddisken \
				   <li>Klicka <b>Webläsarförhandsgranska</b> för att se sidan med en webläsare

xsl.dhtml = Dynamisk<br>Webbsida
xsl.dhtml.tip = Skapar en Webbsida med <i>dynamiska</i> effekter.<br> \
				   <li>Klicka <b>Spara som...</b> för att spara filen till hårddisken \
				   <li>Klicka <b>Webbläsarförhandsgranska</b> för att se sidan med en webbläsare <br> \
				  JavaScript måste vara aktiverad i webbläsaren.

xsl.debug = XML-Fil<br>(felsökning)
export.xml.tip = Skapar en XML-fil för felsökning.

xsl.pdf = Skriv ut PDF
xsl.pdf.tip = Skapar eller skriver ut en PDF-fil.<br> \
				<li>Klicka <b>Spara som...</b> för att spara filen till hårddisken \
				<li>Klicka <b>Skriv ut...</b> för att skriva ut dokumentet \
				<li>Klicka <b>Förhandsgranska</b> för att se dokumentet

xsl.tex = TeX-fil
xsl.tex.tip = Skapa en fil att bearbeta med TeX.

xsl.html.figs.tt = TrueType-figurer
xsl.html.figs.img = Bild-figurer
xsl.css.standalone = CSS-stilinställningar i en separat fil

xsl.html.img.dir = Katalog
xsl.create.images = Skapa bilder

xsl.pdf.embed = Använd TrueType-fonter
xsl.pdf.font.dir = Ytterligare Fonter:

default.file.name = Schackparti


##############################################################################
#	Print Preview Dialog
##############################################################################

print.preview.page.one =en sida
print.preview.page.two =två sidor
print.preview.page.one.tip = visar en sida
print.preview.page.two.tip = visar två sidor

print.preview.ori.land =Landskap
print.preview.ori.port =Porträtt
print.preview.ori.land.tip = Använd landskapsorienterat papper
print.preview.ori.port.tip = Använd porträttorienterat papper

print.preview.fit.page = Hela sidan
print.preview.fit.width = Sidbredd
print.preview.fit.textwidth = Textbredd
print.preview.next.page =
print.preview.previous.page =

preview.wait = Ett ögonblick...


##############################################################################
#	Online Update Dialog
##############################################################################

online.update.title	= Onlineuppdatering
online.update.tab.1	= Ny version
online.update.tab.2	= Vad är nytt ?
online.update.tab.3 	= Viktiga anteckningar

update.install	= Ladda ner & Installera nu
update.download	= Ladda ner, Installera senare
update.mirror	= Ladda ner från alternativ sida

download.file.progress 			= Laddar ner %file%
download.file.title			= Ladda ner
download.error.invalid.url		= Ogiltig URL: %p%.
download.error.connect.fail		= Anslutning till %p% misslyckades.
download.error.parse.xml		= Tolkningsfel: %p%.
download.error.version.missing	= Kunde inte läsa version från %p%.
download.error.os.missing		= Ingen version av jose hittades till ditt operativsystem.
download.error.browser.fail		= Kan inte visa %p%.
download.error.update			= Ett fel uppträdde medan jose uppdaterades.\n Var god och uppdatera jose manuellt.

download.message.up.to.date		= Din installerade version är uppdaterad.
download.message.success		= jose har blivit framgångsrikt uppdaterad till version %p% \n. Var god och starta om jose.

download.message			= \
  Versionen <b>%version%</b> finns från <br>\
  <font color=blue>%url%</font><br>\
  Storlek: %size%

dialog.browser		= Var snäll och hjälp mig starta din HTML-webbläsare.\n Ange kommandot som används för att starta webbläsaren.
dialog.browser.title 	= Lokalisera webbläsaren

# deprecated
#online.report.title	= Rapport
#online.report.bug	= Felrapport
#online.report.feature	= Funktionsförfrågan
#online.report.support	= Supportförfrågan

#online.report.type	= Typ:
#online.report.subject	= Ämne:
#online.report.description	= Beskrivning:
#online.report.email		= EMail:

#online.report.default.subject		= <Ämne>
#online.report.default.description	= <Var god och försök beskriva vad som orsakade felet>
#online.report.default.email		= <din e-mail address = frivillig uppgift>
#online.report.info			= Denna rapport kommer att skickas till http://jose-chess.sourceforge.net

#online.report.success		= Din rapport har blivit skickad.
#online.report.failed		= Kunde inte skicka din rapport.
# deprecated


##########################################
# 	Error Messages
##########################################

error.not.selected 		= Var god och välj ett parti att spara.
error.duplicate.database.access = Var god och kör inte två versioner av jose samtidigt \n\
	och använd dem inte på samma databas.\n\n\
	Det kan göra att data blir förlorat. \n\
	Var god och avsluta en version av  jose.
error.lnf.not.supported 	= Denna Se&Känn finns inte \n på den aktuella plattformen.

error.bad.uci = Detta verkar inte vara en UCI-schackmotor.\n Är du säker att det är en UCI-schackmotor ?

error.bug	= <center><b>Ett oväntat fel har inträffat.</b></center><br><br> \
 Det skulle vara hjälpsamt om du skickade en felrapport.<br>\
 Var god och försök beskriva de steg som orsakade felet <br>\
 och bifoga denna fil till din rapport: <br>\
  <center><b> %error.log% </b></center>

# errors in setup dialog

pos.error.white.king.missing	= Vit kung saknas.
pos.error.black.king.missing	= Svart kung saknas.
pos.error.too.many.white.kings	= För många vita kungar.
pos.error.too.many.black.kings	= För många svarta kungar.
pos.error.white.king.checked	= Vits kung får inte vara i schack.
pos.error.black.king.checked	= Svarts kung får inte vara i schack.
pos.error.white.pawn.base		= Vita bönder får inte placeras på den första raden.
pos.error.white.pawn.promo		= Vita bönder får inte placeras på den åttonde raden.
pos.error.black.pawn.base		= Svarta bönder får inte placeras på den åttonde raden.
pos.error.black.pawn.promo		= Svarta bönder får inte placeras på den första raden.
pos.warning.too.many.white.pieces	= För många vita pjäser.
pos.warning.too.many.black.pieces	= För många svarta pjäser.
pos.warning.too.many.white.pawns	= För många vita bönder.
pos.warning.too.many.black.pawns	= För många svarta bönder.
pos.warning.too.many.white.knights	= För många vita springare.
pos.warning.too.many.black.knights	= För många svarta springare.
pos.warning.too.many.white.bishops	= För många vita löpare.
pos.warning.too.many.black.bishops	= För många svarta löpare.
pos.warning.too.many.white.rooks	= För många vita torn.
pos.warning.too.many.black.rooks	= För många svarta torn.
pos.warning.too.many.white.queens	= För många vita damer.
pos.warning.too.many.black.queens	= För många svarta damer.
pos.warning.strange.white.bishops	= Vita löpare på samma färg ?
pos.warning.strange.black.bishops	= Svarta löpare på samma färg ?


##############################################################################
#	Style Names
##############################################################################


base			= Grundläggande stil
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
body.line.2		= första subvariationen
body.line.3		= andra subvariationen
body.line.4		= tredje subvariationen
body.symbol		= Symboler
body.inline		= Diagram infogat i text
body.figurine	= Figurer
body.figurine.0	= Huvudvariant
body.figurine.1	= Variation
body.figurine.2	= första subvariationen
body.figurine.3	= andra subvariationen
body.figurine.4	= tredje subvariationen
body.comment	= Kommentarer
body.comment.0	= Huvudvariant
body.comment.1	= Variation
body.comment.2	= första subvariationen
body.comment.3	= andra subvariationen
body.comment.4	= tredje subvariationen
body.result		= Resultat
html.large      = Diagram på Webbsida


##############################################################################
#	Task Dialogs (progress)
##############################################################################

dialog.progress.time 		= återstår: %time%

dialog.read-progress.title 	= jose - Läs Fil
dialog.read-progress.text 	= läser %fileName%

dialog.eco 			= Klassificera ECO
dialog.eco.clobber.eco 		= Skriv över ECO-koder
dialog.eco.clobber.name 	= Skriv över öppningsnamn
dialog.eco.language 		= Språk:


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
fig.ru = ÏÑÊËÔ"Êð"
fig.ukr = ÏÑÊËÔ"Êð"
# please note that Russians use the latin alphabet for files (a,b,c,d,e,f,g,h)
# (so we don't have to care about that ;-)

##############################################################################
#	foreign language names
##############################################################################

lang.cs = Tjeckiska
lang.da = Danska
lang.nl = Nederländska
lang.en = Engelska
lang.et = Estniska
lang.fi = Finska
lang.fr = Franska
lang.de = Tyska
lang.hu = Ungerska
lang.is = Isländska
lang.it = Italienska
lang.no = Norska
lang.pl = Polska
lang.pt = Portugisiska
lang.ro = Rumänska
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
pgn.nag.2.tip	= dåligt drag
pgn.nag.3    	= !!
pgn.nag.3.tip	= mycket bra drag
pgn.nag.4    	= ??
pgn.nag.4.tip	= mycket dåligt drag
pgn.nag.5    	= !?
pgn.nag.5.tip	= intressant drag
pgn.nag.6    	= ?!
pgn.nag.6.tip	= tveksamt drag
pgn.nag.7    = forcerat drag
pgn.nag.7.tip = forcerat drag (alla andra drag förlorar snabbt) 
pgn.nag.8    = enda draget
pgn.nag.8.tip    = enda draget (inga andra bra alternativ)
pgn.nag.9    = sämsta draget
pgn.nag.10      = =
pgn.nag.10.tip   = remiartad position
pgn.nag.11      = =
pgn.nag.11.tip   = lika chancer, stilla position
pgn.nag.12.tip   = lika chancer, aktiv position
pgn.nag.13   = oklar
pgn.nag.13.tip   = oklar position
pgn.nag.14	= +=
pgn.nag.14.tip	= Vit ha en liten fördel
pgn.nag.15   	= =+
pgn.nag.15.tip 	= Svart har en liten fördel
pgn.nag.16	= +/-
pgn.nag.16.tip 	= Vit har en rätt bra fördel
pgn.nag.17	= -/+
pgn.nag.17.tip 	= Svart har en rätt bra fördel
pgn.nag.18	= +-
pgn.nag.18.tip  = Vit har en avgörande fördel
pgn.nag.19	= -+
pgn.nag.19.tip 	= Svart har en avgörande fördel
pgn.nag.20   = Vit har en förkrossande fördel (Svart borde ge upp)
pgn.nag.21   = Svart har en förkrossande fördel (Vit borde ge upp)
pgn.nag.22   = Vit är i dragtvång (zugzwang)
pgn.nag.23   = Svart är i dragtvång (zugzwang)
pgn.nag.24   = Vit har en liten utrymmesfördel
pgn.nag.25   = Svart har en liten utrymmesfördel
pgn.nag.26   = Vit har en ganska bra utrymmesfördel
pgn.nag.27   = Svart har en ganska bra utrymmesfördel
pgn.nag.28   = Vit har en avgörande utrymmesfördel
pgn.nag.29   = Svart har en avgörande utrymmesfördel
pgn.nag.30   = Vit har liten tids (utvecklings) fördel
pgn.nag.31   = Svart har en liten tids (utvecklings) fördel
pgn.nag.32   = Vit har en ganska bra tids (utvecklings) fördel
pgn.nag.33   = Svart har en ganska bra tids (utvecklings) fördel
pgn.nag.34   = Vit har en avgörande tids (utvecklings) fördel
pgn.nag.35   = Svart har en avgörande tids (utvecklings) fördel
pgn.nag.36   = Vit har ett initiativ
pgn.nag.37   = Svart har ett initiativ
pgn.nag.38   = Vit har ett långvarigt initiativ
pgn.nag.39   = Svart har en långvarigt initiativ
pgn.nag.40   = Vit har en attack
pgn.nag.41   = Svart har en attack
pgn.nag.42   = Vit har en otillräcklig  kompensation för materiellt underskott
pgn.nag.43   = Svart har en otillräcklig kompensation för materiellt underskott
pgn.nag.44   = Vit har en tillräcklig kompensation för materiellt underskott
pgn.nag.45   = Svart har en tillräcklig kompensation för materiellt underskott
pgn.nag.46   = Vit har mer än en tillräcklig kompensation för materiellt underskott
pgn.nag.47   = Svart har mer än en tillräcklig kompensation för materiellt underskott
pgn.nag.48   = Vit har en liten centerkontrollfördel
pgn.nag.49   = Svart har en liten centerkontrollfördel
pgn.nag.50   = Vit har en ganska bra centerkontrollfördel
pgn.nag.51   = Svart har en ganska bra centerkontrollfördel
pgn.nag.52   = Vit har en avgörande centerkontrollfördel
pgn.nag.53   = Svart har en avgörande centerkontrollfördel
pgn.nag.54   = Vit har en liten kungssidekontrollfördel
pgn.nag.55   = Svart har en liten kungssidekontrollfördel
pgn.nag.56   = Vit har en ganska bra kungssidekontrollfördel
pgn.nag.57   = Svart har en ganska bra kungssidekontrollfördel
pgn.nag.58   = Vit har en avgörande kungssidekontrollfördel
pgn.nag.59   = Svart har en avgörande kungssidekontrollfördel
pgn.nag.60   = Vit har en liten damsidekontrollfördel
pgn.nag.61   = Svart har en liten damsidekontrollfördel
pgn.nag.62   = Vit har en ganska bra damsidekontrollfördel
pgn.nag.63   = Svart har en ganska bra damsidekontrollfördel
pgn.nag.64   = Vit har en avgörande damsidekontrollfördel
pgn.nag.65   = Svart har en avgörande damsidekontrollfördel
pgn.nag.66   = Vit har en sårbar första rad
pgn.nag.67   = Svart har en sårbar första rad
pgn.nag.68   = Vit har en väl skyddad första rad
pgn.nag.69   = Svart har en väl skyddad första rad
pgn.nag.70   = Vit har en dåligt skyddad kung
pgn.nag.71   = Svart har en dåligt skyddad kung
pgn.nag.72   = Vit har en bra skyddad kung
pgn.nag.73   = Svart har en bra skyddad kung
pgn.nag.74   = Vit har en dåligt placerad kung
pgn.nag.75   = Svart har en dåligt placerad kung
pgn.nag.76   = Vit har en välplacerad kung
pgn.nag.77   = Svart har en välplacerad kung
pgn.nag.78   = Vit har en mycket svag bondestruktur
pgn.nag.79   = Svart har en mycket svag bondestruktur
pgn.nag.80   = Vit har en måttligt bra bondestruktur
pgn.nag.81   = Svart har en måttligt bra bondestruktur
pgn.nag.82   = Vit har en ganska stark bondestruktur
pgn.nag.83   = Svart har en ganska stark bondestruktur
pgn.nag.84   = Vit har en väldigt stark bondestruktur
pgn.nag.85   = Svart har en väldigt stark bondestruktur
pgn.nag.86   = Vit har en dålig springarplacering
pgn.nag.87   = Svart har en dålig springarplacering
pgn.nag.88   = Vit har en bra springarplacering
pgn.nag.89   = Svart har en bra springarplacering
pgn.nag.90   = Vit har en dålig löparplacering
pgn.nag.91   = Svart har en dålig löparplacering
pgn.nag.92   = Vit har en bra löparplacering
pgn.nag.93   = Svart har en bra löparplacering
pgn.nag.94   = Vit har en dålig tornplacering
pgn.nag.95   = Svart har en dålig tornplacering
pgn.nag.96   = Vit har en bra tornplacering
pgn.nag.97   = Svart har en bra tornplacering
pgn.nag.98   = Vit har en dålig damplacering
pgn.nag.99   = Svart har en dålig damplacering
pgn.nag.100  = Vit har en bra damplacering
pgn.nag.101  = Svart har en bra damplacering
pgn.nag.102  = Vit har en dålig pjäskoordination
pgn.nag.103  = Svart har en dålig pjäskoordination
pgn.nag.104  = Vit har en bra pjäskoordination
pgn.nag.105  = Svart har en bra pjäskoordination
pgn.nag.106  = Vit har spelat öppningen mycket dåligt
pgn.nag.107  = Svart har spelat öppningen mycket dåligt
pgn.nag.108  = Vit har spelat öppningen dåligt
pgn.nag.109  = Svart har spelat öppningen dåligt
pgn.nag.110  = Vit har spelat öppningen bra
pgn.nag.111  = Svart har spelat öppningen bra
pgn.nag.112  = Vit har spelat öppningen mycket bra
pgn.nag.113  = Svart har spelat öppningen mycket bra
pgn.nag.114  = Vit har spelat mittspelet mycket dåligt
pgn.nag.115  = Svart har spelat mittspelet mycket dåligt
pgn.nag.116  = Vit har spelat mittspelet dåligt
pgn.nag.117  = Svart har spelat mittspelet dåligt
pgn.nag.118  = Vit har spelat mittspelet bra
pgn.nag.119  = Svart har spelat mittspelet bra
pgn.nag.120  = Vit har spelat mittspelet mycket bra
pgn.nag.121  = Svart har spelat mittspelet mycket bra
pgn.nag.122  = Vit har spelat slutspelet mycket dåligt
pgn.nag.123  = Svart har spelat slutspelet mycket dåligt
pgn.nag.124  = Vit har spelat slutspelet dåligt
pgn.nag.125  = Svart har spelat slutspelet dåligt
pgn.nag.126  = Vit har spelat slutspelet bra
pgn.nag.127  = Svart har spelat slutspelet bra
pgn.nag.128  = Vit har spelat slutspelet mycket bra
pgn.nag.129  = Svart har spelat slutspelet mycket bra
pgn.nag.130  = Vit har lite motspel
pgn.nag.131  = Svart har lite motspel
pgn.nag.132  = Vit har ganska bra motspel
pgn.nag.133  = Svart har ganska bra motspel
pgn.nag.134  = Vit har avgörande motspel
pgn.nag.135  = Svart har avgörande motspel
pgn.nag.136  = Vit har ganska bra tidskontrolltryck
pgn.nag.137  = Svart har ganska bra tidskontrolltryck
pgn.nag.138  = Vit har kraftigt tidskontrolltryck
pgn.nag.139  = Svart har kraftigt tidskontrolltryck

# following codes are defined by Fritz or SCID

pgn.nag.140  	= med iden
pgn.nag.141  	= mot
pgn.nag.142  	= är bättre
pgn.nag.143  	= är sämre
pgn.nag.144  	= =
pgn.nag.144.tip = är ekvivalent
pgn.nag.145  	= RR
pgn.nag.145.tip	= redaktionell kommentar?
pgn.nag.146  	= N
pgn.nag.146.tip = Nyhet
pgn.nag.147     = Svag punkt
pgn.nag.148     = Slutspel
pgn.nag.149		= fil
pgn.nag.150		= Diagonal
pgn.nag.151		= Vit har ett löparpar
pgn.nag.152     = Svart har ett löparpar
pgn.nag.153		= Löpare med olika färger
pgn.nag.154		= Löpare med samma färg
# following codes are defined by us (equivalent to Informator symbols)
# (is there a standard definition for these symbols ?)

pgn.nag.156		= fribonde
pgn.nag.157		= fler bönder
pgn.nag.158		= med
pgn.nag.159		= utan
pgn.nag.161		= se
pgn.nag.163		= rad


# defined by SCID:
pgn.nag.190		= etc.
pgn.nag.191		= dubblerade bönder
pgn.nag.192		= separade bönder
pgn.nag.193		= förenade bönder
pgn.nag.194     = Hängande bönder 
pgn.nag.195     = Bakvända bönder

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
