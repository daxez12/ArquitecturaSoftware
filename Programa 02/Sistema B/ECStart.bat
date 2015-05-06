%ECHO OFF
%ECHO Starting ECS System
PAUSE
%ECHO ECS Monitoring Console
START "MUSEUM ENVIRONMENTAL CONTROL SYSTEM CONSOLE" /NORMAL java ECSConsole %1

%ECHO Starting Temperature Controller Console
START "TEMPERATURE CONTROLLER CONSOLE" /MIN /NORMAL java TemperatureController %1

%ECHO Starting Humidity Sensor Console
START "HUMIDITY CONTROLLER CONSOLE" /MIN /NORMAL java HumidityController %1

START "TEMPERATURE SENSOR CONSOLE" /MIN /NORMAL java TemperatureSensor %1
%ECHO Starting Humidity Sensor Console
START "HUMIDITY SENSOR CONSOLE" /MIN /NORMAL java HumiditySensor %1

%ECHO Iniciando el sensor de seguridad (alarmas)
START "SECURITY SENSORE CONSOLE" /MIN /NORMAL java SecuritySensor %1

%ECHO Iniciando el sensor de fuego
START "FIRE SENSORE CONSOLE" /MIN /NORMAL java FireSensor %1

%ECHO Iniciando la consola para el controlador de fuego
START "FIRE CONTROLLER CONSOLE" /MIN /NORMAL java FireController %1

