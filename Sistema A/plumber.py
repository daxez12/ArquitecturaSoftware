from source_filter import SourceFilter
from altitude_filter import AltitudeFilter
from temperature_filter import TemperatureFilter
from format_output_filter import FormatOutputFilter
from sink_filter import SinkFilter

""" An example to illustrate how to use the PlumberTemplate to create a main thread that
    instantiates and connects a set of filters. This example consists of three filters: a source, a middle filter
    that acts as a pass-through filter (it does nothing to the data), and a sink filter which illustrates all kinds
    of useful things that you can do with the input stream of data
    """

        
if __name__ == "__main__":
    # Here we instantiate three filters.
    filter1 = SourceFilter("../FlightData.dat")
    filter2 = AltitudeFilter()
    filter3 = TemperatureFilter()
    filter4 = FormatOutputFilter()
    filter5 = SinkFilter()
    
    """ Here we connect the filters starting with the sink filter (Filter 1) which
    	we connect to Filter2 the middle filter. Then we connect Filter2 to the	source filter (Filter3).
        """

    filter5.connect(filter4) 
    filter4.connect(filter3) 
    filter3.connect(filter2) # This essentially says, "connect Filter3 input port to Filter2 output port
    filter2.connect(filter1) # This essentially says, "connect Filter2 intput port to Filter1 output port
    
    # Here we start the filters up. All-in-all ... its really kind of boring.
    filter1.start()  
    filter2.start()    
    filter3.start()
    filter4.start()
    filter5.start()
