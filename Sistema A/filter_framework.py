import threading
from pipe_connectors import PipedInputStream, PipedOutputStream
from time import sleep

                
class EndOfStreamException(Exception):
    def __init__(self, message = None):
        Exception.__init__(self)
        self.message = message

class FilterFramework(threading.Thread):
    """ This (super)class defines a skeletal filter framework that defines a filter in terms of the input and output
        ports. 
        All filters must be defined in terms of this framework. That is, filters must extend this class
        in order to be considered valid system filters. 
        Filters as standalone threads until the inputport no longer
        has any data - at which point the filter finishes up any work it has to do and then terminates.
        """
    
    
    def __init__(self):
        threading.Thread.__init__(self)
        self.__inputReadPort = PipedInputStream()       # This is the filter's input port.
        self.__outputWritePort = PipedOutputStream()    # This the filter's output port. 
        
    def connect(self, filter_framework):
        """ Connects filters to each other. All connections are through the inputport of each filter. 
            That is each filter's inputport is connected to another filter's output port through this method.

            Args:
            filter_framework - A reference to the filter that is connected to the instance filter's input port. 
            """
                
        try:
            # Connect this filter's input to the upstream pipe's output stream
            self.__inputFilter = filter_framework
            self.__inputReadPort.connect(filter_framework.__outputWritePort)
  
        except BaseException, error:
            print "{0} FilterFramework error connecting::{1}".format(self.getName(), error)
                      
    def readFilterInputPort(self):
        # Reads data from the input port one byte at a time. 

        try:
            """ Since delays are possible on upstream filters, we first wait untilthere is data available 
                on the input port. We check,... if no data is available on the input port we wait for 
                a quarter of a second and check again. Note there is no timeout enforced here at all and 
                if upstream filters are deadlocked, then this can result in infinite waits in this loop. 
                It is necessary to check to see if we are at the end of stream in the wait loop because 
                it is possible that the upstream filter completes while we are waiting. If this happens 
                and we do not check for the end of stream, then we could wait forever on an upstream pipe 
                that is long gone. What we do here is to see if the upstream filter is alive. If it is, 
                we assume the pipe is still open and sending data. If the filter is not alive, then 
                we assume the end of stream has been reached. 
                """
            while self.__inputReadPort.available() == 0:
                if self.endOfInputStream():
                    raise EndOfStreamException("End of input stream reached")
                sleep(0.250)
                
        except EndOfStreamException as error:
            raise error
        except Exception as error:
            print "{0} Error in read port wait loop::{1}".format(self.getName(), error)
        
        try:
            data = self.__inputReadPort.read()
            return data
        except Exception as error:
            print "{0} Pipe read error::{1}", self.getName(), error
            return 0
    
    def writeFilterOutputPort(self, data):
        """ Writes data to the output port one byte at a time..

            Args:
            data - the byte that will be written on the output portof the filter.. 
            """
        try:
            self.__outputWritePort.write(data)
            self.__outputWritePort.flush()
        except Exception as error:
            print "{0} Pipe write error::{1}".format(self.getName(), error)
            
            
    def endOfInputStream(self):
        # Checks if the upstream filter is still alive. 
          
        return not self.__inputFilter.isAlive()
    
    def closePorts(self):
        """ Closes the input and output ports of the filter. 
            It is important that filters close their ports before the filter thread exits. 
            """
        try:
            self.__inputReadPort.close()
            self.__outputWritePort.close()
        except Exception as error:
            print "{0} Pipe write error::{1}".format(self.getName(), error)
    
    def run(self):
        """ This method should be overridden by the subordinate class. 
            Please see the example applications provided for more details. 
            """
        pass
                

            


    