from filter_framework import FilterFramework, EndOfStreamException

class AltitudeFilter(FilterFramework):
    def run(self):
        read = 0
        written = 0
        id = 0
        altitud = 0
        
        
        print"{0}::AltitudeFilter Reading ".format(self.getName())
        
        while(True):
            try:
                data = self.readFilterInputPort()
                read += 1
                self.writeFilterOutput(data)
                written += 1
            
            except EndOfStreamException:
                self.closePorts()
                print "{0}::Altitude Filter Exiting; bytes read: {1}, bytes written: {2}".format(self.getName(), read, written)
                break
                