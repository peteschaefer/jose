rem set java=jre\bin\java
set java=C:\Users\nightrider\Downloads\jbrsdk-21.0.6-windows-x64-b825.77\jbrsdk-21.0.6-windows-x64-b825.77\bin\java
set vm_args=--add-exports java.desktop/sun.awt=ALL-UNNAMED
set class_path=classes
set library_path=.;lib\Windows
set main=de.jose.Main
%java% %vm_args% -Djava.class.path=%class_path% -Djava.library_path=%library_path% %main% %*