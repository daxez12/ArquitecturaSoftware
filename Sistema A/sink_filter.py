from filter_framework import FilterFramework, EndOfStreamException


class SinkFilter(FilterFramework):
    """ A template for creating sink filters. The details of threading, connections writing output
        are contained in the FilterFramework (super) class. In order to use this template the program should rename the class.
        The template includes the run() method which is executed when the filter is started.
        The run() method is the guts of the filter and is where the programmer should put their filter specific code.
        In the template there is a main read-write loop for reading from the input port of the filter. The programmer is
        responsible for writing the data to a file, or device of some kind. This template assumes that the filter is a sink
        filter that reads data from the input file and writes the output from this filter to a file or device of some kind.
        In this case, only the input port is used by the filter. In cases where the filter is a standard filter or a source
        filter, you should use the FilterTemplate or the SourceFilterTemplate as a starting point for creating
        standard or source filters.   
        """
        
        
    def __init__(self):
        FilterFramework.__init__(self)
        
    def run(self):
        while True:
            """ This is the main processing loop for the filter. Since this
                is a sink filter, we read until there is no more data
             	available on the input port.
                """
            try:
                """ Here we read a byte from the input port. Note that regardless how the data is written, data must be read one
                    byte at a time from the input pipe. This has been done to adhere to the pipe and filter paradigm and provide a
                    high degree of portability between filters. However, you must convert output data as needed on your own.
                    """
                            
                dataByte = self.readFilterInputPort()
                
                # The programmer can insert code for the filter operations	here to include writing the data to some device or file.
                
                """ When we reach the end of the input stream, an exception is thrown which is shown below. At this point, you should
                    finish up any processing, close your ports and exit.
                    """
                                
            except EndOfStreamException:
                self.closePorts()
                break