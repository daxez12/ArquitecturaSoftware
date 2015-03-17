from filter_framework import FilterFramework, EndOfStreamException

class MiddleFilter(FilterFramework):
    """ It is an example for how to use the FilterRemplate to create a standard filter. This particular
        example is a simple "pass-through" filter that reads data from the filter's input port and writes data out the
        filter's output port.
        """
    
    def run(self):
        read = 0        # Number of bytes read from the input file
        written = 0     # Number of bytes written to the stream.
        data = 0        # The byte of data read from the file

        # Next we write a message to the terminal to let the world know we are alive...
        print "{0}::Middle Reading ".format(self.getName())

        while (True):

            #        Here we read a byte and write a byte
            try:
                data = self.readFilterInputPort()
                read += 1
                self.writeFilterOutputPort(data)
                written += 1                
                #print "MiddleBytesWritten:" + str(written)
            
            except EndOfStreamException:
                self.closePorts()
                print "{0}::Middle Exiting; bytes read: {1}, bytes written: {2}".format(self.getName(), read, written)
                break
                