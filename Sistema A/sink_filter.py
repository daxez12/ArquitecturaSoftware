import datetime
import struct
from filter_framework import FilterFramework


class SinkFilter(FilterFramework):
    """ This class serves as an example for using the SinkFilterTemplate for creating a sink filter. This particular
        filter reads some input from the filter's input port and does the following:

        1) It parses the input stream and "decommutates" the measurement ID
        2) It parses the input steam for measurments and "decommutates" measurements, storing the bits in a long word.

        This filter illustrates how to convert the byte stream data from the upstream filterinto useable data found in
        the stream: namely time (long type) and measurements (double type).
        """
        
    def __init__(self):
        FilterFramework.__init__(self)
        
        
    def run(self):
        """ TimeStamp is used to compute time .
		    TimeStampFormat is used to format the time value so that it can be easily printed
		    to the terminal.
            """
            
        timeStamp = 0
        timeStampFormat = "%Y %m %d::%H:%M:%S:%f"
        measurementLength = 8   # This is the length of all measurements (including time) in bytes
        idLength = 4            # This is the length of IDs in the byte stream
        
        dataByte = 0            # This is the data byte read from the stream
        bytesRead = 0           # This is the number of bytes read from the stream
        
        measurement = 0         # This is the word used to store all measurements - conversions are illustrated.
        idMeasurement = 0       # This is the measurement id
        
        # We announce to the world that we are alive ...
        print "{0}::Sink reading".format(self.getName())
        
        while True:
            try:
                """ We know that the first data coming to this filter is going to be an ID and
				    that it is IdLength long. So we first decommutate the ID bytes.
                    """
                                               
                idMeasurement = 0
                i = 0
                
                while i < idLength:
                    dataByte = self.readFilterInputPort() # This is where we read the byte from the stream...
                    
                    idMeasurement = idMeasurement << 8     # We append the byte on to ID ...
                    idMeasurement = idMeasurement | dataByte                
                    bytesRead += 1
                    i += 1
                
                """ Here we read measurements. All measurement data is read as a stream of bytes
				    and stored as a long value. This permits us to do bitwise manipulation that
				    is neccesary to convert the byte stream into data words. Note that bitwise
				    manipulation is not permitted on any kind of floating point types in Java.
				    If the id = 0 then this is a time value and is therefore a long value - no
				    problem. However, if the id is something other than 0, then the bits in the
				    long value is really of type double and we need to convert the value using
				    Double.longBitsToDouble(long val) to do the conversion which is illustrated.
				    below.
                    """
                
                measurement = 0
                i = 0
                while i < measurementLength:
                    dataByte = self.readFilterInputPort()
                    measurement = measurement << 8
                    measurement = measurement | dataByte
                    bytesRead += 1
                    i += 1
                
                    """ Here we look for an ID of 0 which indicates this is a time measurement.
				        Every frame begins with an ID of 0, followed by a time stamp which correlates
				        to the time that each proceeding measurement was recorded. Time is stored
				        in milliseconds since Epoch. This allows us to use datetime to
				        retrieve time and also use text format classes to format the output into
				        a form humans can read. So this provides great flexibility in terms of
				        dealing with time arithmetically or for string display purposes. This is
				        illustrated below.
                        """
                        
                    if idMeasurement == 0:
                        timeStamp = datetime.datetime.fromtimestamp(measurement/1000.0)
                    
        
                        """ Here we pick up a measurement (ID = 4 in this case), but you can pick up
				        any measurement you want to. All measurements in the stream are
				        decommutated by this class. Note that all data measurements are double types.
				        This illustrates how to convert the bits read from the stream into a double
				        type. 
                    
                        Lo siguiente en espaniol por claridad :P
                        Esto es bastante simple pasando el valor de tipo long a un arreglo de bytes
                        y despues pasandolo a un tipo double. Hay que tener cuidado aqui con el primer
                        parametro que se le pasa al metodo pack, esto indica los endianess que se usan,
                        esto es dependiente del procesador donde se este ejecutando. Puede considerar 
                        cambiarlo por ">Q" para big-endian o "<Q" para little endian, el "@Q" es para el nativo.
                        """
                    
                    if idMeasurement == 4:
                        #convertimos la medida de 8 bytes en un arreglo de 8 bytes
                        byteArray = struct.pack("@Q", measurement) 
                    
                        #convertimos el arreglo de 8 bytes en un numero decimal
                        doubleValue = struct.unpack('d', byteArray)[0]
                    
                        print "{0} -- ID = {1} {2}".format(timeStamp.strftime(timeStampFormat), idMeasurement, doubleValue)
            except:
                self.closePorts()
                print "{0}::Sink Exiting; bytes read: {1}".format(self.getName(), bytesRead)
                break