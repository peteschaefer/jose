
##############################################################################
#	Spanish Translation for jose
#   (by Agustin Gomila and Alex Coroanado)
##############################################################################

#	Application Name
application.name	= jose

#	Frame Titles
window.board	= Tablero
window.console	= Consola del Motor
window.database	= Base de Datos
window.filter	= Filtro
window.list	= Listado de Partidas
window.clock	= Reloj
window.game	= Partida
window.engine	= Motor
window.eval     = Perfil de evaluación

window.collectionlist	= Base de Datos
window.query		= Buscar
window.gamelist		= Base de datos

window.sqlquery		= Consulta SQL
window.sqllist		= Resultado

window.toolbar.1	= Barra de Herramientas 1
window.toolbar.2	= Barra de Herramientas 2
window.toolbar.3	= Barra de Herramientas 3
window.toolbar.symbols	= Anotaciones
window.help		= Ayuda
window.print.preview      =   Previsualizar impresión

# dialog titles

dialog.option	= Opciones
dialog.about	= Acerca de jose
dialog.animate  = Animar partida
dialog.setup	= Configurar posición
dialog.message.title = Mensaje

# number formats:
format.byte		= ###0.# 'B'
format.kilobyte		= ###0.# 'kB'
format.megabyte		= ###0.# 'MB'


##############################################################################
# 	Menus
##############################################################################

# File Menu

menu.file		= Archivo
menu.file.new		= Nuevo
menu.file.new.tip	= Iniciar una partida nueva
menu.file.new.frc   = Nuevo FRC
menu.file.new.frc.tip = Inicia una Nueva Partida de Ajedrez al Azar de Fischer
menu.file.new.shuffle   = Nuevo Shuffle
menu.file.new.shuffle.tip = Inicia una Nueva Partida de Ajedrez Shuffle
menu.file.open		= Abrir...
menu.file.open.tip  	= Abrir un archivo PGN
menu.file.open.url	= Abrir URL...
menu.file.close		= Cerrar
menu.file.close.tip	= Cerrar la Ventana actual
menu.file.save		= Guardar
menu.file.save.tip  	= Guardar la partida actual en la base de datos
menu.file.save.as    	= Guardar como...
menu.file.save.as.tip	= Guardar una nueva copia de la partida actual en la base de datos
menu.file.save.all   	= Guardar Todo
menu.file.save.all.tip  = Guardar todas las partidas abiertas en la base de datos
menu.file.revert	= Revertir partida
menu.file.print		= Imprimir...
menu.file.print.tip	= Imprimir la partida actual
menu.file.print.setup	= Configurar Página...
menu.file.print.setup.tip = Configurar la impresora y el tamaño de la página
menu.file.print.preview   =   Previsualizar impresión...
menu.file.quit		= Salir
menu.file.quit.tip	= Salir de jose

# Edit Menu

menu.edit		= Editar
menu.edit.undo		= Deshacer (%action%)
menu.edit.cant.undo 	= Deshacer
menu.edit.redo		= Rehacer (%action%)
menu.edit.cant.redo 	= Rehacer
menu.edit.select.all    = Seleccionar Todo
menu.edit.select.none   = Quitar Selección
menu.edit.cut		= Cortar
menu.edit.copy		= Copiar
menu.edit.copy.fen  = Copiar FEN
menu.edit.copy.fen.tip = Copiar la posición actual al portapapeles (como valores FEN)
menu.edit.copy.img      =    Imagen+Fondo
menu.edit.copy.pgn.tip = Copiar la partida actual al portapapeles (como texto PGN)
menu.edit.copy.imgt     =    Imagen
menu.edit.copy.img.tip  =    Copiar la posición actual al portapapeles (como Imagen)
menu.edit.copy.text     =    Diagrama de texto
menu.edit.copy.imgt.tip =    Copiar la posición actual al portapapeles (como Imagen)

menu.edit.copy.pgn  = Copiar PGN
menu.edit.copy.text.tip =    Copiar la posición actual al portapapeles (como texto con estilo)
menu.edit.paste		= Pegar
menu.edit.paste.tip = Pegar desde el portapapeles
menu.edit.paste.copy		= Pegar copia
menu.edit.paste.copy.tip 	= Copiar partidas desde el portapapeles
menu.edit.paste.same 	= Pegar Partidas
menu.edit.paste.same.tip = Mover partidas desde el portapapeles
menu.edit.paste.pgn = Pegar PGN
menu.edit.paste.pgn.tip = Insertar una partida desde el portapapeles (como texto PGN) 
menu.edit.clear		= Limpiar
menu.edit.option	= Opciones...
menu.edit.option.tip	= Abrir diálogo de opciones

menu.edit.games			= Base de Datos
menu.edit.collection.new 	= Nueva Carpeta
menu.edit.collection.rename 	= Renombrar
menu.edit.collection.crunch = Ajustar
menu.edit.collection.crunch.tip = Re-Ajustar índice de columna
menu.edit.empty.trash		= Vaciar la papelera
menu.edit.restore		= Restaurar

#menu.edit.position.index    = Actualizar índice de posiciones
menu.edit.search.current    = Buscar esta Posición
menu.edit.ecofy             = Clasificar ECO

menu.edit.style = Estilo de texto
menu.edit.bold = Negrita
menu.edit.italic = Cursiva
menu.edit.underline = Subrrayado
menu.edit.plain = Texto plano
menu.edit.left = Alinear a la izquierda
menu.edit.center = Centrado
menu.edit.right = Alinear a la derecha
menu.edit.larger = Aumentar tamaño del texto
menu.edit.smaller = Disminuir tamaño del texto
menu.edit.color = Color del texto

# Game Menu

menu.game		= Partida
menu.game.details	= Detalles...
menu.game.analysis  	= Modo de Análisis
menu.game.navigate	= Ir a...
menu.game.time.controls = Controles de tiempo
menu.game.time.control = Control de Tiempo
menu.game.details.tip 	= Editar detalles de la partida (Jugadores, etc.)
menu.game.hint		= Sugerencia
menu.game.hint.tip  = Mostrar la sugerencia
menu.game.draw		= Ofrecer tablas
menu.game.resign	= Abandonar
menu.game.2d		= Vista 2D
menu.game.3d		= Vista 3D
menu.game.flip		= Girar tablero
menu.game.coords	= Coordenadas
menu.game.coords.tip	= Cambiar la vista de las coordenadas
menu.game.animate 	= Animar...
menu.game.previous 	= Etiqueta previa
menu.game.next 		= Etiqueta siguiente
menu.game.close 	= Cerrar
menu.game.close.tip 	= Cerrar la partida actual
menu.game.close.all 	= Cerrar todo
menu.game.close.all.tip = Cerrar todas las partidas abiertas
menu.game.close.all.but = Cerrar todas menos esta
menu.game.close.all.but.tip = Cerrar todas las partidas abiertas menos la actual
menu.game.setup		= Configurar Posición

menu.game.copy.line = Copiar línea
menu.game.copy.line.tip = Copiar esta línea al portapapeles
menu.game.paste.line = Pegar línea
menu.game.paste.line.tip = Insertar esta línea en la partida actual

# Window Menu

menu.window		= Ventana
menu.window.fullscreen 	= Pantalla completa
menu.window.reset   	= Reiniciar disposición

# Help Menu

menu.help		= Ayuda
menu.help.splash	= Acerca de...
menu.help.about		= Información...
menu.help.license	= Licencia...
menu.help.context   	= Ayuda contextual
menu.help.manual    	= Manual
menu.help.web		= jose en la Web

menu.web.home		= Pagina en Internet
menu.web.update		= Actualización en línea
menu.web.download	= Descargar
menu.web.report		= Reporte de fallos
menu.web.support	= Petición de soporte
menu.web.feature	= Petición de características
menu.web.forum		= Foro
menu.web.donate     = Donación
menu.web.browser	= Seleccionar Navegador...


##############################################################################
# 	Context Menu
##############################################################################

panel.hide		= Ocultar
panel.hide.tip		= Ocultar este panel
panel.undock		= Nueva ventana
panel.undock.tip	= Abrir este panel en una ventana aparte
panel.move		= Mover
panel.move.tip		= Mover este panel a otra posición
panel.dock		= Anclar
panel.dock.tip		= "Anclar" esta ventana

panel.orig.pos = Posición original
panel.dock.here = Anclar aquí
panel.undock.here = Desanclar de aquí

#################
# Document Panel
#################

# deprecated:
tab.place 	= Ubicación de la etiqueta
tab.place.top 	= Arriba
tab.place.left 	= Izquierda
tab.place.bottom = Abajo
tab.place.right = Derecha
#

tab.layout 		= Disposición de la etiqueta
tab.layout.wrap 	= Cubierta
tab.layout.scroll 	= Enrollada

doc.menu.annotate 	= Anotación
doc.menu.delete.comment = Eliminar comentario
doc.menu.line.promote 	= Promover línea
doc.menu.line.delete 	= Eliminar línea
doc.menu.line.cut 	= Cortar línea
doc.menu.line.uncomment = Eliminar todos los comentarios
doc.menu.remove.annotation = -Nada-
doc.menu.more.annotations = Más...

tab.untitled 	= Sin Título
confirm 	= Confirmar
confirm.save.one = ¿Guardar la partida actual?
confirm.save.all = ¿Guardar las partidas modificadas?

dialog.confirm.save = Guardar
dialog.confirm.dont.save = No guardar

dialog.engine.offers.draw = %engine% ofrece tablas.

dialog.accept.draw = Aceptar
dialog.decline.draw = Rechazar

dialog.autoimport.title = Importar
dialog.autoimport.ask = El archivo ^0 ha sido cambiado en el disco. \n ¿Abrir nuevamente?

dialog.paste.message = Usted está a punto de insertar datos en el portapapeles. \n\
     ¿Quiere mover las partidas, o crear una nueva copia de éstas?
dialog.paste.title = Pegar partidas
dialog.paste.same = Mover
dialog.paste.copy = Copiar

###################
# Game Navigation
###################

move.first	= Inicio de la partida
move.backward 	= Atrás
move.delete 	= Deshacer último movimiento
engine.stop 	= Pausar
move.start 	= Mover ahora
move.forward 	= Adelante
move.last 	= Final de la partida
move.animate	= Animar


##################################
# Engine Panel
##################################

engine.paused.tip 	= %engine% está detenido
engine.thinking.tip 	= %engine% está pensando el próximo movimiento
engine.pondering.tip 	= %engine% está ponderando tu próximo movimiento
engine.analyzing.tip 	= %engine% está analizando
engine.hint.tip 	= Sugerencia: %move%

engine.paused.title 	= %engine%
engine.thinking.title 	= %engine% está pensando
engine.pondering.title 	= %engine% está ponderando
engine.analyzing.title 	= %engine% está analizando

plugin.name 		= %name% %version%
plugin.name.author 	= %name% %version% por %author%

plugin.book.move 	= BK
plugin.book.move.tip 	= Movimiento del libro
plugin.hash.move 	= HT
plugin.hash.move.tip 	= Evaluación desde la tabla Hash
plugin.tb.move 		= TB
plugin.tb.move.tip 	= Evaluación desde la base de finales

plugin.currentmove.title       = Movimiento
plugin.depth.title      = Profundidad
plugin.elapsed.time.title  = Tiempo
plugin.nodecount.title  = Nodos
plugin.nps.title        = N/seg

plugin.currentmove = %move%
plugin.currentmove.max = %move% %moveno%/%maxmove%

plugin.currentmove.tip = El movimiento evaluado actualmente es %move%.
plugin.currentmove.max.tip = El movimiento evaluado actualmente es %move%. (núm. %moveno% de %maxmove%)

plugin.depth 		= %depth%
plugin.depth.tip 	= Profundidad de búsqueda: %depth% mov. medios

plugin.depth.sel 	= %depth% (%seldepth%)
plugin.depth.sel.tip 	= Profundidad de búsqueda: %depth% mov. medios, Profundiad selectiva: %seldepth% mov. medios

plugin.white.mates 	= +#%eval%
plugin.white.mates.tip 	= Mate de las Blancas en %eval% movimientos
plugin.black.mates 	= -#%eval%
plugin.black.mates.tip 	= Mate de las Negras en %eval% movimientos

plugin.evaluation 	= %eval%
plugin.evaluation.tip 	= El valor de la posición es %eval%

plugin.line.tip = Línea calculada

plugin.elapsed.time = %time%
plugin.elapsed.time.tip = Tiempo transcurrido para este cálculo.

plugin.nodecount 	= %nodecount%
plugin.nodecount.tip 	= %nodecount% posiciones han sido evaluadas

plugin.nps      = %nps%
plugin.nps.tip  = %nps% nodos analizados por segundo

plugin.pv.history   =   Modo experto

restart.plugin		= Reiniciar motor

######################
# Board Panel
######################

wait.3d = Cargando 3D. Por favor espere...

message.result			= Resultado
message.white 			= Blancas
message.black 			= Negras
message.mate 			= Mate. \n %player% ganan.
message.stalemate		= Ahogado. \n La partida es tablas.
message.draw3			= Posición repetida por 3ra vez. \n La partida es tablas.
message.draw50			= No hubo piezas capturadas en 50 movimientos. \n La partida es tablas.
message.drawmat         = Material insuficiente para dar mate. \n La partida es tablas.
message.resign			= %player% abandona. \n Tu ganas.
message.time.draw		= El tiempo a terminado. \n La partida es tablas.
message.time.lose		= El tiempo a terminado. \n %player% ganan.


################
# Clock Panel
################

clock.mode.analog	= Analógico
clock.mode.analog.tip 	= Mostrar reloj analógico
clock.mode.digital	= Digital
clock.mode.digital.tip 	= Mostrar reloj digital


##############################################################################
#	Dialogs
##############################################################################

dialog.button.ok		= Aceptar
dialog.button.ok.tip		= Haga click aquí para aplicar los cambios
dialog.button.cancel		= Cancelar
dialog.button.cancel.tip	= Haga click aquí para cerrar el diálogo sin aplicar los cambios
dialog.button.apply		= Aplicar
dialog.button.apply.tip		= Haga click aquí para aplicar los cambios inmediatamente
dialog.button.revert		= Revertir
dialog.button.revert.tip	= Haga click aquí para descartar los cambios
dialog.button.clear		= Limpiar
dialog.button.delete		= Eliminar
dialog.button.yes		= Si
dialog.button.no		= No
dialog.button.next		= Siguiente
dialog.button.back		= Anterior
dialog.button.close		= Cerrar
dialog.button.help		= Ayuda
dialog.button.help.tip		= Mostrar tema de ayuda

dialog.button.commit		= Finalizar
dialog.button.commit.tip	= Haga click aquí para finalizar las actualizaciones
dialog.button.rollback		= Descartar
dialog.button.rollback.tip	= Haga click aquí para descartar las actualizaciones

dialog.error.title		= Error

###################################
#  File Chooser Dialog
###################################

filechooser.pgn			= Archivo PGN (*.pgn,*.zip)
filechooser.epd         = Archivo EPD o FEN (*.epd,*.fen)
filechooser.db 			= Archivos jose (*.jose)
filechooser.db.Games 		= Archivos de partidas jose (*.jose)
filechooser.db.Games.MySQL 	= Archivos de partidas jose (guardado rápido) (*.jose)
filechooser.txt 		= Archivos de texto (*.txt)
filechooser.html 		= Archivos Web (*.html)
filechooser.pdf 		= Acrobat Reader (*.pdf)
filechooser.exe         = Archivos ejecutables
filechooser.img         = Archivos de imágenes (*.gif,*.jpg,*.png,*.bmp)

filechooser.overwrite 	= ¿Sobrescribir el archivo "%file.name%" existente?
filechooser.do.overwrite = Sobrescribir

#################
# Color Chooser
#################

colorchooser.texture	= Textura
colorchooser.preview	= Vista previa
colorchooser.gradient   = Gradiente
colorchooser.gradient.color1 = Primer color
colorchooser.gradient.color2 = Segundo color
colorchooser.gradient.cyclic = Cíclico

colorchooser.texture.mnemonic = T
colorchooser.gradient.mnemonic = G

animation.slider.fast   = Rápido
animation.slider.slow   = Lento

##############################################################################
# Option dialog
##############################################################################

# Tab Titles

dialog.option.tab.1	= Usuario
dialog.option.tab.2	= Fuentes
dialog.option.tab.3	= Colores
dialog.option.tab.4	= Tiempo
dialog.option.tab.5     = Motores
dialog.option.tab.6 = Opening Book
# TODO
dialog.option.tab.7     = 3D
dialog.option.tab.8	= Estilos

# User settings

dialog.option.user.name		= Nombre
dialog.option.user.language	= Lenguaje
dialog.option.ui.look.and.feel	= Presentacion

doc.load.history	= Cargar partidas recientes
doc.classify.eco	= Clasificar aperturas por ECO
doc.associate.pgn   = Abrir archivos PGN con jose

dialog.option.animation = Animación
dialog.option.animation.speed = Velocidad de animación

dialog.option.doc.write.mode	= Insertar nuevo movimiento
write.mode.new.line		= Nueva línea
write.mode.new.main.line	= Línea principal
write.mode.overwrite		= Sobrescribir
write.mode.ask			= Preguntar
write.mode.dont.ask		= No preguntar nunca
# Don't ask anymore
write.mode.cancel		= Cancelar

board.animation.hints   = Mostrar sugerencias mientras anima

dialog.option.sound = Sonido
dialog.option.sound.moves.dir = Anuncios de movimientos:
sound.moves.engine  = Anunciar movimientos del motor
sound.moves.ack.user = Admitir movimientos del usuario
sound.moves.user = Anunciar movimientos del usuario

# Fonts

dialog.option.font.diagram	= Diagrama
dialog.option.font.text		= Texto
dialog.option.font.inline	= Diagrama de texto
dialog.option.font.figurine	= Figuras
dialog.option.font.symbol	= Símbolos
dialog.option.font.size     	= Tamaño
figurine.usefont.true 		= Figuras gráficas
figurine.usefont.false 		= Figuras de texto

doc.panel.antialias		= Usar fuentes antialias

# Notation

dialog.option.doc.move.format	= Notación:
move.format			= Notación
move.format.short		= Corta
move.format.long		= Larga
move.format.algebraic		= Algebráica
move.format.correspondence	= Correspondencia
move.format.english		= Inglés
move.format.telegraphic		= Telegráfica

# Colors

dialog.option.board.surface.light	= Casillas claras
dialog.option.board.surface.dark	= Casillas oscuras
dialog.option.board.surface.white	= Piezas Blancas
dialog.option.board.surface.black	= Piezas Negras

dialog.option.board.surface.background	= Fondo
dialog.option.board.surface.frame	= Marco
dialog.option.board.surface.coords	= Coordenadas

dialog.option.board.3d.model            = Modelo:
dialog.option.board.3d.clock            = Reloj
dialog.option.board.3d.surface.frame	= Marco:
dialog.option.board.3d.light.ambient	= Luz ambiental:
dialog.option.board.3d.light.directional = Luz direccional:
dialog.option.board.3d.knight.angle     = Caballos:

board.surface.light	= Casillas claras
board.surface.dark	= Casillas oscuras
board.surface.white	= Piezas Blancas
board.surface.black	= Piezas Negras
board.hilite.squares 	= Resaltar casillas

# Time Controls

dialog.option.time.control      = Control de tiempo
dialog.option.phase.1		= Fase 1
dialog.option.phase.2		= Fase 2
dialog.option.phase.3		= Fase 3
dialog.option.all.moves		= Todos
dialog.option.moves.in 		= movimientos en
dialog.option.increment 	= plus
dialog.option.increment.label 	= por movimiento

time.control.blitz		= Blitz
time.control.rapid		= Rápida
time.control.fischer		= Fischer
time.control.tournament		= Torneo
# default name for new time control
time.control.new		= Nuevo
time.control.delete		= Eliminar

# Engine Settings

dialog.option.plugin.1		= Motor 1
dialog.option.plugin.2		= Motor 2

plugin.add =
plugin.delete =
plugin.duplicate =
plugin.add.tip = Agregar un nuevo motor
plugin.delete.tip = Eliminar configuración
plugin.duplicate.tip = Duplicar configuración

dialog.option.plugin.file 	= Archivo de configuración:
dialog.option.plugin.name 	= Nombre:
dialog.option.plugin.version 	= Versión:
dialog.option.plugin.author 	= Autor:
dialog.option.plugin.dir 	= Directorio:
dialog.option.plugin.logo 	= Logo:
dialog.option.plugin.startup 	= Iniciar:

dialog.option.plugin.exe = Ejecutable:
dialog.option.plugin.args = Argumentos:
dialog.option.plugin.default = Parámetros por defecto

plugin.info                 = Información general
plugin.protocol.xboard      = Protocolo XBoard
plugin.protocol.uci         = Protocolo UCI
plugin.options              = Opciones del motor
plugin.startup              = Más opciones
plugin.show.logos           = Mostrar logos
plugin.show.text            = Mostrar texto

plugin.switch.ask           = Usted tiene seleccionado otro motor.\n ¿Iniciar éste ahora?
plugin.restart.ask          = Usted tiene parámetros del motor modificados.\n ¿Reiniciar el motor ahora?
plugin.show.info            = Mostrar "info"
plugin.log.file             = Log a archivo

# UCI option name
plugin.option.Ponder        = Ponderar
plugin.option.Random        = Aleatorio
plugin.option.Hash          = Tamaño de la tabla Hash (MB)
plugin.option.NalimovPath   = Ruta a las bases de finales Nalimov
plugin.option.NalimovCache  = Caché para las bases Nalimov (MB)
plugin.option.OwnBook       = Usar libro de aperturas
plugin.option.BookFile      = Libro de aperturas
plugin.option.BookLearning  = Aprendizaje de libro
plugin.option.MultiPV       = Variantes primarias
plugin.option.ClearHash     = Limpiar tabla Hash
plugin.option.UCI_ShowCurrLine  = Mostrar la variante actual
plugin.option.UCI_ShowRefutations = Mostrar refutaciones
plugin.option.UCI_LimitStrength = Limitar fuerza
plugin.option.UCI_Elo       = ELO
plugin.option.UCI_EngineAbout =

# 3D Settings

board.surface.background	= Fondo
board.surface.coords		= Coordenadas
board.3d.clock              	= Reloj
board.3d.shadow			= Sombra
board.3d.reflection		= Reflejos
board.3d.anisotropic        	= Filtro anisotrópico
board.3d.fsaa               	= Pantalla completa antialias

board.3d.surface.frame		= Marco
board.3d.light.ambient		= Luz ambiental
board.3d.light.directional	= Luz direccional
board.3d.screenshot		= Capturar pantalla
board.3d.defaultview		= Vista por defecto

# Text Styles

font.color 	= Color
font.name	= Familia
font.size	= Tamaño
font.bold	= Negrita
font.italic	= Cursiva
font.sample	= Texto de muestra


##############################################################################
#	Database Panels
##############################################################################

# default collection folders

collection.trash 	= Papelera
collection.autosave 	= Autoguardar
collection.clipboard 	= Portapapeles

# default name for new folders
collection.new 		= Nueva Carpeta

# name of starter database
collection.starter 	= Partidas de Capablanca

# column titles

column.collection.name 		= Nombre
column.collection.gamecount 	= Partidas
column.collection.lastmodified 	= Modificado

column.game.index 	= Núm.
column.game.white.name 	= Blancas
column.game.black.name 	= Negras
column.game.event 	= Evento
column.game.site 	= Lugar
column.game.date 	= Fecha
column.game.result 	= Resultado
column.game.round 	= Ronda
column.game.board 	= Tablero
column.game.eco 	= ECO
column.game.opening 	= Apertura
column.game.movecount 	= Movimientos
column.game.annotator 	= Anotador
column.game.fen     = Posición inicial

#deprecated
column.problem.author 	= Autor
column.problem.source 	= Fuente
column.problem.number 	= Núm.
column.problem.date 	= Fecha
column.problem.stipulation = Estip.
column.problem.dedication = Dedicatoria
column.problem.award = Premio
column.problem.solution = Solución
column.problem.cplus = C+
column.problem.genre = Género
column.problem.keyword = Palabra clave
#deprecated

bootstrap.confirm 	= El directorio de datos '%datadir%' no existe.\n ¿Crear un nuevo directorio de datos? 
bootstrap.create 	= Crear directorio de datos

edit.game = Abrir
edit.all = Abrir en etiquetas
dnd.move.top.level	= Mover al nivel superior


##############################################################################
#  Search Panel
##############################################################################

# Tab Titles
dialog.query.info 		= Información
dialog.query.comments 		= Comentarios
dialog.query.position 		= Posición

dialog.query.search 	= Buscar
dialog.query.clear 	= Limpiar
dialog.query.search.in.progress = Buscando...

dialog.query.0.results 	= Sin resultados
dialog.query.1.result 	= Un resultado
dialog.query.n.results 	= %count% Resultados

dialog.query.white		= Blancas:
dialog.query.black		= Negras:

dialog.query.flags 		= Opciones
dialog.query.color.sensitive 	= Sensibilidad al color
dialog.query.swap.colors 	=
dialog.query.swap.colors.tip = Intercambiar colores
dialog.query.case.sensitive 	= Sensibilidad al caso
dialog.query.soundex 		= Suena parecido
dialog.query.result 		= Result.
dialog.query.stop.results   =

dialog.query.event 		= Evento:
dialog.query.site 		= Lugar:
dialog.query.eco 		= ECO:
dialog.query.annotator 		= Anotador:
dialog.query.to 		= a
dialog.query.opening 		= Apertura:
dialog.query.date 		= Fecha:
dialog.query.movecount 		= Movimientos:

dialog.query.commenttext 	= Comentario:
dialog.query.com.flag 		= Tiene comentarios
dialog.query.com.flag.tip 	= Buscar partidas con comentarios

dialog.query.var.flag 		= Tiene variantes
dialog.query.var.flag.tip 	= Buscar partidas con variantes

dialog.query.errors 		= Error en la expresión de búsqueda:
query.error.date.too.small 	= La fecha está fuera del rango
query.error.movecount.too.small = El número está fuera del rango
query.error.eco.too.long 	= Usar tres caracteres para el código ECO
query.error.eco.character.expected = Los códigos ECO deben empezar con A, B, C, D, o E
query.error.eco.number.expected = Los códigos ECO consisten de un caracter y un número desde 0 a 99
query.error.number.format 	= Formato erroneo de número
query.error.date.format 	= Formato erroneo de fecha

query.setup.enable 		= Buscar posición
query.setup.next 		= Movimientos siguientes:
query.setup.next.white 		= Blancas
query.setup.next.white.tip 	= Encontrar posiciones donde las Blancas tienen el siguiente movimiento
query.setup.next.black 		= Negras
query.setup.next.black.tip 	= Encontrar posiciones donde las Negras tienen el siguiente movimiento
query.setup.next.any 		= Blancas o Negras
query.setup.next.any.tip 	= Encontrar posiciones donde cualquiera de los dos tiene el siguiente movimiento
query.setup.reversed 		= Buscar con colores invertidos
query.setup.reversed.tip 	= Encontrar posiciones idénticas con los colores invertidos
query.setup.var 		= Buscar en variantes
query.setup.var.tip 		= Buscar dentro de las variantes



##############################################################################
#	Game Details dialog
##############################################################################

dialog.game		= Detalle de la partida
dialog.game.tab.1	= Evento
dialog.game.tab.2	= Jugadores
dialog.game.tab.3	= Más

dialog.details.event 	= Evento:
dialog.details.site 	= Lugar:
dialog.details.date 	= Fecha:
dialog.details.eventdate = Fecha del evento:
dialog.details.round 	= Ronda:
dialog.details.board 	= Tablero:

dialog.details.white 	= Blancas
dialog.details.black 	= Negras
dialog.details.name 	= Nombre:
dialog.details.elo 	= ELO:
dialog.details.title 	= Título:
dialog.details.result 	= Resultado:

dialog.details.eco 	= ECO:
dialog.details.opening 	= Apertura:
dialog.details.annotator = Anotador:

Result.0-1 = 0-1
Result.1-0 = 1-0
Result.1/2 = 1/2
Result.* = *


##############################################################################
#	Setup dialog
##############################################################################

dialog.setup.clear	= Limpiar
dialog.setup.initial	= Posición inicial
dialog.setup.copy	= Copiar desde el panel principal

dialog.setup.next.white	= Mueven las Blancas
dialog.setup.next.black	= Mueven las Negras
dialog.setup.move.no	= Movimiento número

dialog.setup.castling		= Enroque
dialog.setup.castling.wk	= Blancas 0-0
dialog.setup.castling.wk.tip	= Las Blancas pueden enrocar hacia el lado del rey
dialog.setup.castling.wq	= Blancas 0-0-0
dialog.setup.castling.wq.tip	= Las Blancas pueden enrocar hacia el lado de la dama
dialog.setup.castling.bk	= Negras 0-0
dialog.setup.castling.bk.tip	= Las Negras pueden enrocar hacia el lado del rey
dialog.setup.castling.bq	= Negras 0-0-0
dialog.setup.castling.bq.tip	= Las Negras pueden enrocar hacia el lado de la dama
dialog.setup.castling.frc   = Enroque FRC
dialog.setup.castling.frc.tip = Check this option to allow FRC castlings
dialog.setup.invalid.fen    = Valor FEN no válido.
dialog.setup.shuffle.title  = Shuffle
dialog.setup.shuffle =
dialog.setup.shuffle.tip = selecciona una posición al azar para Ajedrez Shuffle
dialog.setup.frc =
dialog.setup.frc.tip = selecciona una posición para Ajedrez al Azar de Fischer

##############################################################################
#	About dialog
##############################################################################

dialog.about.tab.1	= jose
dialog.about.tab.2	= Base de Datos
dialog.about.tab.3	= Contribuciones
dialog.about.tab.4	= Sistema
dialog.about.tab.5	= 3D
dialog.about.tab.6	= Licencia

dialog.about.1a	=   Versión %version%
dialog.about.1b =   <center><font size=+1><b><a href=\"http://%project-url%\">%project-url%</a></b></font></center> \
                    <br><br> \
                     <table><tr><td>Copyright &copy; %year% %author%  (%contact%)</td> \
                     <td><font size=-1><a href=\"%donate-url%\"><img src=\"%donate-img%\" border=0></a></font></td></tr></table> \
					<br><br> \
					<font size=-1>%gpl-hint%</font>
					
dialog.about.gpl =	Este programa es distribuido bajo los términos de la licencia pública general GNU


dialog.about.2	=	<b>%dbname%</b> <br> %dbversion% <br><br> \
					URL del servidor: %dburl%

dialog.about.QED =	Quadcap Embedded Database \n \
					www.quadcap.com

dialog.about.Cloudscape = Cloudscape \n \
							www.cloudscape.com

dialog.about.Oracle = www.oracle.com

dialog.about.MySQL = www.mysql.com

dialog.about.3	=	<b>Traducciones:</b><br>\
			Frederic Raimbault, José de Paula, \
			Agustín Gomila, Alex Coronado, \
			Harold Roig, Hans Eriksson, \
			Guido Grazioli, Tomasz Sokól, \
			"Direktx" (Húngaro) <br>\
			<br>\
			<b>Desarrollo de fuentes TrueType:</b> <br>\
			Armando Hernandez Marroquin, \
			Eric Bentzen, \
			Alan Cowderoy, <br>\
			Hans Bodlaender \
			(www.chessvariants.com/d.font/fonts.html) <br>\
			<br>\
			<b>Presentación FlatLaf:</b> <br>\
               FormDev Software (www.formdev.com/flatlaf)<br>\
			<br>\
			<b>Modelado 3D:</b> <br>\
			Renzo Del Fabbro, \
			Francisco Barala Faura <br> \
			<br>\
			<b>Apple Mac support:</b> <br>\
			Andreas Güttinger, Randy Countryman


dialog.about.4 =	Versión de Java: %java.version% (%java.vendor%) <br>\
					Java VM: %java.vm.version% %java.vm.info% (%java.vm.vendor%) <br>\
					Runtime: %java.runtime.name% %java.runtime.version% <br>\
					Ambiente Grafico: %java.awt.graphicsenv% <br>\
					Herramientas AWT: %awt.toolkit%<br>\
					Directorio: %java.home%<br>\
					<br>\
					Memoria Total: %maxmem%<br>\
					Memoria Libre: %freemem%<br>\
					<br>\
					Sistema Operativo: %os.name%  %os.version% <br>\
					Arquitectura del Sistema: %os.arch%

dialog.about.5.no3d = Java3D no disponible actualmente
dialog.about.5.model =

dialog.about.5.native = Plataforma nativa: &nbsp;
dialog.about.5.native.unknown = actualmente desconocida

##############################################################################
#	Export/Print Dialog
##############################################################################

dialog.export          =    Exportar & Imprimir
dialog.export.tab.1    =    Salida
dialog.export.tab.2    =    Configurar página
dialog.export.tab.3    =    Estilos

dialog.export.print    =    Imprimir...
dialog.export.save     =    Guardar
dialog.export.saveas   =    Guardar como...
dialog.export.preview  =    Previsualizar
dialog.export.browser  =    Previsualizar en navegador

dialog.export.paper         =    Papel
dialog.export.orientation   =    Orientación
dialog.export.margins       =    Márgenes

dialog.export.paper.format  =    Papel:
dialog.export.paper.size    =    Tamaño:

dialog.print.custom.paper   =    Predeterminado

dialog.export.margin.top    =    Superior:
dialog.export.margin.bottom =    Inferior:
dialog.export.margin.left   =    Izquierdo:
dialog.export.margin.right  =    Derecho:

dialog.export.ori.port   =    Vertical
dialog.export.ori.land   =    Horizontal

dialog.export.games.0    =    Usted no tiene ninguna partida seleccionada para imprimir.
dialog.export.games.1    =    Usted tiene seleccionada <b>Una Partida</b> para imprimir.
dialog.export.games.n    =    Usted tiene seleccionadas <b>%n% Partidas</b> para imprimir.
dialog.export.games.?    =    Usted tiene seleccionado un número <b>desconocido</b> de Partidas para imprimir.

dialog.export.confirm    =    ¿Está usted seguro?
dialog.export.yes        =    Imprimir todo

# xsl stylesheet options

export.pgn       =    Archivo PGN
export.pgn.tip   =    Exportar partidas como Portable Game Notation (Archivo PGN).

print.awt        =    Impimir
print.awt.tip    =    Impimir partidas desde la Pantalla.<br> \
				    <li>Pulse <b>Imprimir...</b> para impimir en la impresora conectada \
				    <li>Pulse <b>Previsualizar</b> para ver el documento

xsl.html        =    Página Web HTML<br>
xsl.html.tip    =    Crear una página Web (archivo HTML).<br> \
				   <li>Pulse <b>Guardar como...</b> para guardar el archivo en disco \
				   <li>Pulse <b>Previsualizar en navegador</b> para ver la Página con el navegador Web

xsl.dhtml       =    Página Web dinámica<br>
xsl.dhtml.tip   =    Crear una página Web con efectos <i>dinámicos</i>.<br> \
				   <li>Pulse <b>Guardar como...</b> para guardar el archivo en disco \
				   <li>Pulse <b>Previsualizar en navegador</b> para ver la Página con el navegador Web <br> \
				  JavaScript debe estar activado en el navegador de Web.

xsl.debug       =    Archivo XML<br>(puesta a punto)
export.xml.tip  =    Crear un archivo XML de puesta a punto.

xsl.pdf        =    Archivo PDF
xsl.pdf.tip    =    Crear o imprimir un archivo PDF.<br> \
				<li>Pulse <b>Guardar como...</b> para guardar el archivo en disco \
				<li>Pulse <b>Imprimir...</b> para imprimir el documento \
				<li>Pulse <b>Previsualizar</b> para ver el documento

xsl.tex = Archivo TeX
xsl.tex.tip = Crear un archivo para procesamiento con TeX.

xsl.html.figs.tt    =    Figurines TrueType
xsl.html.figs.img   =    Figurines de Imagen
xsl.css.standalone  =    Hoja de estilo CSS en archivo aparte

xsl.html.img.dir    =    Ubicación
xsl.create.images   =    Crear Imágenes

xsl.pdf.embed       =    Empotrar fuentes TrueType
xsl.pdf.font.dir    =    Fuentes Adicionales:

default.file.name = ChessGame


##############################################################################
#	Print Preview Dialog
##############################################################################

print.preview.page.one =
print.preview.page.two =
print.preview.page.one.tip    =    mostrar una página
print.preview.page.two.tip    =    mostrar dos páginas

print.preview.ori.land =
print.preview.ori.port =
print.preview.ori.land.tip    =    Usar papel de orientación horizontal
print.preview.ori.port.tip    =    Usar papel de orientación vertical

print.preview.fit.page        =    Página Completa
print.preview.fit.width       =    Ancho de Página
print.preview.fit.textwidth   =    Ancho de Texto

print.preview.next.page =
print.preview.previous.page =

preview.wait = Un momento...

##############################################################################
#	Online Update Dialog
##############################################################################

online.update.title	= Actualización en línea
online.update.tab.1	= Nueva versión
online.update.tab.2	= ¿Qué hay de nuevo?
online.update.tab.3 	= Sugerencias importantes

update.install	= Descargar e instalar ahora
update.download	= Descargar ahora, instalar después
update.mirror	= Descargar desde un sitio espejo

download.file.progress 			= Descargando %file%
download.file.title			= Descargar
download.error.invalid.url		= URL inválido: %p%.
download.error.connect.fail		= Conexión a %p% fallida.
download.error.parse.xml		= Error de análisis: %p%.
download.error.version.missing	= No se ha podido leer la versión desde %p%.
download.error.os.missing		= No se encontraron paquetes para su SO.
download.error.browser.fail		= No se puede mostrar %p%.
download.error.update			= Un error ha ocurrido mientras jose se actualizaba.\n Por favor actualice manualmente la aplicación.

download.message.up.to.date		= Su versión instalada está actualizada.
download.message.success		= jose fue correctamente actualizado a la versión %p% \n. Por favor reinicie la aplicación.

download.message			= \
  Versión <b>%version%</b> está disponible desde <br>\
  <font color=blue>%url%</font><br>\
  Tamaño: %size%

dialog.browser		= Por favor ayudeme a iniciar su navegador HTML.\n Ingrese el comando que es utilizado para lanzar el navegador.
dialog.browser.title 	= Localizar Navegador

# deprecated
#online.report.title	= Reporte
online.report.bug	= Reporte de Problemas
#online.report.feature	= Petición de características
#online.report.support	= Petición de soporte

#online.report.type	= Tipo:
#online.report.subject	= Asunto:
#online.report.description	= Descripción:
#online.report.email		= Correo electrónico:

#online.report.default.subject		= <Asunto>
#online.report.default.description	= <Por favor trate de describir los pasos que han causado el error>
#online.report.default.email		= <su dirección de correo electrónico - opcional>
#online.report.info			= Este reporte será enviado a http://jose-chess.sourceforge.net

#online.report.success		= Su reporte ha sido propuesto.
#online.report.failed		= Su reporte no ha sido propuesto.
# deprecated


##########################################
# 	Error Messages
##########################################

error.not.selected 		= Por favor seleccione una partida para guardar.
error.duplicate.database.access = Por favor no ejecute dos sesiones de jose al mismo tiempo \n\
	y sobre una misma base de datos.\n\n\
	Así puede causar que los datos se pierdan. \n\
	Por favor cierre una de las sesiones de jose.
error.lnf.not.supported 	= Esta Presentación no está disponible \n en la plataforma actual.

error.bad.uci = Este no parece ser un motor UCI.\n ¿Está usted seguro?
error.engine.title = Error del Motor
error.engine = %engine% reportó un error:\n %message%
error.engine.fatal = %engine% ha reportado un error:\n %message%. \n El motor será reiniciado.

warning.engine = Precaución
warning.engine.no.frc = El motor conectado no puede jugar partidas de <b>Ajedrez al Azar de Fischer</b>. <br><br>\
Puede jugar de todos modos contra este motor, pero éste no reconocerá los movimientos de enroque. <br>\
Si recibe un mensaje de advertencia durante la partida, presione el botón "Mover ahora" para reiniciar el motor.<br><br>\
Haga click <a href="http://uciengines.de/UCI-Engines/FRC/hauptteil_frc.html">aquí</a> para seleccionar algún motor FRC disponible.
warning.engine.off = No volver a mostrar este mensaje.

error.bug	= <center><b>Un error inesperado ha ocurrido.</b></center><br><br> \
 Sería util que proponga un reporte de este error.<br>\
 Por favor trate de describir los pasos que antecedieron al error <br>\
 y adjunte este archivo a su reporte: <br>\
  <center><b> %error.log% </b></center>

# errors in setup dialog

pos.error.white.king.missing	= No se encuentra el rey Blanco.
pos.error.black.king.missing	= No se encuentra el rey Negro.
pos.error.too.many.white.kings	= Demasiados reyes Blancos.
pos.error.too.many.black.kings	= Demasiados reyes Negros.
pos.error.white.king.checked	= El rey Blanco no debe estar en jaque.
pos.error.black.king.checked	= El rey Negro no debe estar en jaque.
pos.error.white.pawn.base		= Los peones Blancos no pueden ubicarse en la primera línea.
pos.error.white.pawn.promo		= Los peones Blancos no pueden ubicarse en la octava línea.
pos.error.black.pawn.base		= Los peones Negros no pueden ubicarse en la octava línea.
pos.error.black.pawn.promo		= Los peones Negros no pueden ubicarse en la primera línea.
pos.warning.too.many.white.pieces	= Demasiadas piezas Blancas.
pos.warning.too.many.black.pieces	= Demasiadas piezas Negras.
pos.warning.too.many.white.pawns	= Demasiados peones Blancos.
pos.warning.too.many.black.pawns	= Demasiados peones Negros.
pos.warning.too.many.white.knights	= Demasiados caballos Blancos.
pos.warning.too.many.black.knights	= Demasiados caballos Negros.
pos.warning.too.many.white.bishops	= Demasiados alfiles Blancos.
pos.warning.too.many.black.bishops	= Demasiados alfiles Negros.
pos.warning.too.many.white.rooks	= Demasiadas torres Blancas.
pos.warning.too.many.black.rooks	= Demasiadas torres Negras.
pos.warning.too.many.white.queens	= Demasiadas damas Blancas.
pos.warning.too.many.black.queens	= Demasiadas damas Negras.
pos.warning.strange.white.bishops	= ¿Alfiles Blancos en el mismo color?
pos.warning.strange.black.bishops	= ¿Alfiles Negros en el mismo color?


##############################################################################
#	Style Names
##############################################################################


base			= Estilo básico
header			= Información de la partida
header.event	= Evento
header.site		= Lugar
header.date		= Fecha
header.round	= Ronda
header.white	= Jugador Blanco
header.black	= Jugador Negro
header.result	= Resultado de la partida
body			= Texto de la partida
body.line		= Movimientos
body.line.0		= Línea principal
body.line.1		= Variante
body.line.2		= Subvariante
body.line.3		= 2da Subvariante
body.line.4		= 3ra Subvariante
body.symbol		= Símbolos
body.inline		= Diagramas de texto
body.figurine	= Figuras
body.figurine.0	= Línea principal
body.figurine.1	= Variante
body.figurine.2	= Subvariante
body.figurine.3	= 2da Subvariante
body.figurine.4	= 3ra Subvariante
body.comment	= Comentarios
body.comment.0	= Línea principal
body.comment.1	= Variante
body.comment.2	= Subvariante
body.comment.3	= 2da Subvariante
body.comment.4	= 3ra Subvariante
body.result		= Resultado
html.large          =   Diagrama en página Web


##############################################################################
#	Task Dialogs (progress)
##############################################################################

dialog.progress.time 		= Tiempo restante: %time%

dialog.read-progress.title 	= jose - Leer archivo
dialog.read-progress.text 	= Leyendo %fileName%

dialog.eco 			= Clasificar ECO
dialog.eco.clobber.eco 		= Sobrescribir códigos ECO
dialog.eco.clobber.name 	= Sobrescribir nombres de las aperturas
dialog.eco.language 		= Lenguaje:


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

lang.cs = Checo
lang.da = Danés
lang.nl = Holandés
lang.en = Inglés
lang.et = Estonio
lang.fi = Finés
lang.fr = Francés
lang.de = Alemán
lang.hu = Húngaro
lang.is = Islandés
lang.it = Italiano
lang.no = Noruego
lang.pl = Polaco
lang.pt = Portugués
lang.ro = Rumano
lang.es = Español
lang.ca = Catalán
lang.sv = Sueco
lang.ru = Ruso
lang.he = Hebreo
lang.tr = Turco
lang.ukr = Ucraniano


##############################################################################
#	PGN Annotations
##############################################################################

pgn.nag.0    = Anotación nula
pgn.nag.1    	= !
pgn.nag.1.tip	= Movimiento bueno
pgn.nag.2	= ?
pgn.nag.2.tip	= Movimiento malo
pgn.nag.3    	= !!
pgn.nag.3.tip	= Movimiento muy bueno
pgn.nag.4    	= ??
pgn.nag.4.tip	= Movimiento muy malo
pgn.nag.5    	= !?
pgn.nag.5.tip	= Movimiento interesante
pgn.nag.6    	= ?!
pgn.nag.6.tip	= Movimiento cuestionable
pgn.nag.7    = Movimiento forzado
pgn.nag.7.tip = Movimiento forzado (todos los otros pierden rápidamente) 
pgn.nag.8    = Movimiento singular
pgn.nag.8.tip    = Movimiento singular (ninguna alternativa razonable)
pgn.nag.9    = Peor movimiento
pgn.nag.10      = =
pgn.nag.10.tip   = Posición de tablas
pgn.nag.11      = =
pgn.nag.11.tip   = Chances iguales, posición pasiva
pgn.nag.12.tip   = Chances iguales, posición activa
pgn.nag.13   = Incierto
pgn.nag.13.tip   = Posición incierta
pgn.nag.14	= +=
pgn.nag.14.tip	= Las Blancas tienen una ligera ventaja
pgn.nag.15   	= =+
pgn.nag.15.tip 	= Las Negras tienen una ligera ventaja
pgn.nag.16	= +/-
pgn.nag.16.tip 	= Las Blancas tienen una ventaja moderada
pgn.nag.17	= -/+
pgn.nag.17.tip 	= Las Negras tienen una ventaja moderada
pgn.nag.18	= +-
pgn.nag.18.tip  = Las Blancas tienen una ventaja decisiva
pgn.nag.19	= -+
pgn.nag.19.tip 	= Las Negras tienen una ventaja decisiva
pgn.nag.20   = Las Blancas tienen una ventaja aplastante (Las Negras deberían abandonar)
pgn.nag.21   = Las Negras tienen una ventaja aplastante (Las Blancas deberían abandonar)
pgn.nag.22   = Las Blancas están en zugzwang
pgn.nag.23   = Las Negras están en zugzwang
pgn.nag.24   = Las Blancas tienen una ligera ventaja espacial
pgn.nag.25   = Las Negras tienen una ligera ventaja espacial
pgn.nag.26   = Las Blancas tienen una ventaja espacial moderada
pgn.nag.27   = Las Negras tienen una ventaja espacial moderada
pgn.nag.28   = Las Blancas tienen una ventaja espacial decisiva
pgn.nag.29   = Las Negras tienen una ventaja espacial decisiva
pgn.nag.30   = Las Blancas tienen una ligera ventaja de tiempo (desarrollo)
pgn.nag.31   = Las Negras tienen una ligera ventaja de tiempo (desarrollo)
pgn.nag.32   = Las Blancas tienen una ventaja de tiempo moderada (desarrollo)
pgn.nag.33   = Las Negras tienen una ventaja de tiempo moderada (desarrollo)
pgn.nag.34   = Las Blancas tienen una ventaja de tiempo decisiva (desarrollo)
pgn.nag.35   = Las Negras tienen una ventaja de tiempo decisiva (desarrollo)
pgn.nag.36   = Las Blancas tienen la iniciativa
pgn.nag.37   = Las Negras tienen la iniciativa
pgn.nag.38   = Las Blancas tienen una iniciativa duradera
pgn.nag.39   = Las Negras tienen una iniciativa duradera
pgn.nag.40   = Las Blancas tienen el ataque
pgn.nag.41   = Las Negras tienen el ataque
pgn.nag.42   = Las Blancas tienen una compensación insuficiente por el déficit de material
pgn.nag.43   = Las Negras tienen una compensación insuficiente por el déficit de material
pgn.nag.44   = Las Blancas tienen una compensación suficiente por el déficit de material
pgn.nag.45   = Las Negras tienen una compensación suficiente por el déficit de material
pgn.nag.46   = Las Blancas tienen una compensación más que adecuada por el déficit de material
pgn.nag.47   = Las Negras tienen una compensación más que adecuada por el déficit de material
pgn.nag.48   = Las Blancas tienen una ligera ventaja en el control del centro
pgn.nag.49   = Las Negras tienen una ligera ventaja en el control del centro
pgn.nag.50   = Las Blancas tienen una ventaja moderada en el control del centro
pgn.nag.51   = Las Negras tienen una ventaja moderada en el control del centro
pgn.nag.52   = Las Blancas tienen una ventaja decisiva en el control del centro
pgn.nag.53   = Las Negras tienen una ventaja decisiva en el control del centro
pgn.nag.54   = Las Blancas tienen una ligera ventaja en el control del lado del rey
pgn.nag.55   = Las Negras tienen una ligera ventaja en el control del lado del rey
pgn.nag.56   = Las Blancas tienen una ventaja moderada en el control del lado del rey
pgn.nag.57   = Las Negras tienen una ventaja moderada en el control del lado del rey
pgn.nag.58   = Las Blancas tienen una ventaja decisiva en el control del lado del rey
pgn.nag.59   = Las Negras tienen una ventaja decisiva en el control del lado del rey
pgn.nag.60   = Las Blancas tienen una ligera ventaja en el control del lado de la dama
pgn.nag.61   = Las Negras tienen una ligera ventaja en el control del lado de la dama
pgn.nag.62   = Las Blancas tienen una ventaja moderada en el control del lado de la dama
pgn.nag.63   = Las Negras tienen una ventaja moderada en el control del lado de la dama
pgn.nag.64   = Las Blancas tienen una ventaja decisiva en el control del lado de la dama
pgn.nag.65   = Las Negras tienen una ventaja decisiva en el control del lado de la dama
pgn.nag.66   = Las Blancas tienen una primera fila vulnerable
pgn.nag.67   = Las Negras tienen una primera fila vulnerable
pgn.nag.68   = Las Blancas tienen una primera fila bien protegida
pgn.nag.69   = Las Negras tienen una primera fila bien protegida
pgn.nag.70   = Las Blancas tienen al rey pobremente protegido
pgn.nag.71   = Las Negras tienen al rey pobremente protegido
pgn.nag.72   = Las Blancas tienen al rey bien protegido
pgn.nag.73   = Las Negras tienen al rey bien protegido
pgn.nag.74   = Las Blancas tienen al rey pobremente ubicado
pgn.nag.75   = Las Negras tienen al rey pobremente ubicado
pgn.nag.76   = Las Blancas tienen al rey bien ubicado
pgn.nag.77   = Las Negras tienen al rey bien ubicado
pgn.nag.78   = Las Blancas tienen una estructura de peones muy débil
pgn.nag.79   = Las Negras tienen una estructura de peones muy débil
pgn.nag.80   = Las Blancas tienen una estructura de peones moderadamente débil
pgn.nag.81   = Las Negras tienen una estructura de peones moderadamente débil
pgn.nag.82   = Las Blancas tienen una estructura de peones moderadamente fuerte
pgn.nag.83   = Las Negras tienen una estructura de peones moderadamente fuerte
pgn.nag.84   = Las Blancas tienen una estructura de peones muy fuerte
pgn.nag.85   = Las Negras tienen una estructura de peones muy fuerte
pgn.nag.86   = Las Blancas tienen una pobre ubicación del caballo
pgn.nag.87   = Las Negras tienen una pobre ubicación del caballo
pgn.nag.88   = Las Blancas tienen una buena ubicación del caballo
pgn.nag.89   = Las Negras tienen una buena ubicación del caballo
pgn.nag.90   = Las Blancas tienen una pobre ubicación del alfil
pgn.nag.91   = Las Negras tienen una pobre ubicación del alfil
pgn.nag.92   = Las Blancas tienen una buena ubicación del alfil
pgn.nag.93   = Las Negras tienen una buena ubicación del alfil
pgn.nag.94   = Las Blancas tienen una pobre ubicación de la torre
pgn.nag.95   = Las Negras tienen una pobre ubicación de la torre
pgn.nag.96   = Las Blancas tienen una buena ubicación de la torre
pgn.nag.97   = Las Negras tienen una buena ubicación de la torre
pgn.nag.98   = Las Blancas tienen una pobre ubicación de la dama
pgn.nag.99   = Las Negras tienen una pobre ubicación de la dama
pgn.nag.100  = Las Blancas tienen una buena ubicación de la dama
pgn.nag.101  = Las Negras tienen una pobre ubicación de la dama
pgn.nag.102  = Las Blancas tienen una pobre coordinación de las piezas
pgn.nag.103  = Las Negras tienen una pobre coordinación de las piezas
pgn.nag.104  = Las Blancas tienen una buena coordinación de las piezas
pgn.nag.105  = Las Negras tienen una buena coordinación de las piezas
pgn.nag.106  = Las Blancas tienen una apertura muy pobremente jugada
pgn.nag.107  = Las Negras tienen una apertura muy pobremente jugada
pgn.nag.108  = Las Blancas tienen una apertura pobremente jugada
pgn.nag.109  = Las Negras tienen una apertura pobremente jugada
pgn.nag.110  = Las Blancas tienen una apertura bien jugada
pgn.nag.111  = Las Negras tienen una apertura bien jugada
pgn.nag.112  = Las Blancas tienen una apertura muy bien jugada
pgn.nag.113  = Las Negras tienen una apertura muy bien jugada
pgn.nag.114  = Las Blancas tienen un medio juego muy pobremente jugado
pgn.nag.115  = Las Negras tienen un medio juego muy pobremente jugado
pgn.nag.116  = Las Blancas tienen un medio juego pobremente jugado
pgn.nag.117  = Las Negras tienen un medio juego pobremente jugado
pgn.nag.118  = Las Blancas tienen un medio juego bien jugado
pgn.nag.119  = Las Negras tienen un medio juego bien jugado
pgn.nag.120  = Las Blancas tienen un medio juego muy bien jugado
pgn.nag.121  = Las Negras tienen un medio juego muy bien jugado
pgn.nag.122  = Las Blancas tienen un final muy pobremente jugado
pgn.nag.123  = Las Negras tienen un final muy pobremente jugado
pgn.nag.124  = Las Blancas tienen un final pobremente jugado
pgn.nag.125  = Las Negras tienen un final pobremente jugado
pgn.nag.126  = Las Blancas tienen un final bien jugado
pgn.nag.127  = Las Negras tienen un final bien jugado
pgn.nag.128  = Las Blancas tienen un final muy bien jugado
pgn.nag.129  = Las Negras tienen un final muy bien jugado
pgn.nag.130  = Las Blancas tienen una ligera contrapartida
pgn.nag.131  = Las Negras tienen una ligera contrapartida
pgn.nag.132  = Las Blancas tienen una moderada contrapartida
pgn.nag.133  = Las Negras tienen una moderada contrapartida
pgn.nag.134  = Las Blancas tienen una decisiva contrapartida
pgn.nag.135  = Las Negras tienen una decisiva contrapartida
pgn.nag.136  = Las Blancas tienen una moderada presión del control de tiempo
pgn.nag.137  = Las Negras tienen una moderada presión del control de tiempo
pgn.nag.138  = Las Blancas tienen una severa presión del control de tiempo
pgn.nag.139  = Las Negras tienen una severa presión del control de tiempo

# following codes are defined by Fritz

pgn.nag.140  	= con la idea
pgn.nag.141  	= contra
pgn.nag.142  	= es mejor
pgn.nag.143  	= es peor
pgn.nag.144  	= =
pgn.nag.144.tip = es equivalente
pgn.nag.145  	= RR
pgn.nag.145.tip	= anotación redactada?
pgn.nag.146  	= N
pgn.nag.146.tip = Novedad
pgn.nag.147     = Punto débil
pgn.nag.148     = Final de partida
pgn.nag.149		= Linea
pgn.nag.150		= Diagonal
pgn.nag.151		= Las blancas tienen un par de alfiles
pgn.nag.152     = Las Negras tienen un par de alfiles
pgn.nag.153		= Alfiles en colores opuestos
pgn.nag.154		= Alfiles en el mismo color

# following codes are defined by us (equivalent to Informator symbols)
# (is there a standard definition for these symbols ?)

pgn.nag.156		= peones pasados
pgn.nag.157		= más peones
pgn.nag.158		= con
pgn.nag.159		= sin
pgn.nag.161		= ver
pgn.nag.163		= rank

# defined by SCID:
pgn.nag.190		= etc.
pgn.nag.191		= peones dobles
pgn.nag.192		= peones separados
pgn.nag.193		= peones unidos
pgn.nag.194     = Peones pasados
pgn.nag.195     = Peones retrasados

# this code is only defined by us
pgn.nag.201  = Diagrama
pgn.nag.250  = Diagrama


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
