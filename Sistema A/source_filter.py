from filter_framework import FilterFramework

class SourceFilter(FilterFramework):
    """ It is an example for how to use the SourceFilterTemplate to create a source filter. This particular
        * filter is a source filter that reads some input from the FlightData.dat file and writes the bytes up stream..
        """
        
    def __init__(self, filename):
        
        FilterFramework.__init__(self)
        self.__filename = filename
        
    def run(self):
        try:            
            fileInput = open(self.__filename, 'r') # Input data file.
            bytesRead = 0                          #  Number of bytes read from the input file. 
            bytesWritten = 0                       # Number of bytes written to the stream. 
            
            print "{0}::Source reading file..".format(self.getName())
            
            """ Here we read the data from the file and send it out the filter's output port one
            	byte at a time. The loop stops when it encounters an EOFExecption.
                """
            
            while (True):             
                
                data = fileInput.read(1)
            
                if data:
                    bytesRead += 1                    
                    if bytesWritten == 1023:
                        pass
                    self.writeFilterOutputPort(data)
                    bytesWritten += 1

                else:
                    fileInput.close()
                    self.closePorts()
                    print "{0}::Read file complete, bytes read:{1}, bytes written:{2}".format(self.getName(), bytesRead, bytesWritten)     
                    break

        except IOError, e:
            print "{0}::Problem reading input data file::{1}".format(self.getName(), e)

