import threading

class PipedOutputStream:
    """ Based on all classes' documentaion guess what is in here :P
        Please document it.
        """
    
    def __init__(self):
        self.__sink = None
        self._cond = threading.Condition()
    
    def connect(self, sink):
        self._cond.acquire()
        try:
            if sink is None:
                raise Exception("Null pointer exception")
            elif (self.__sink is not None or sink._connected):
                raise Exception("Already connected")
            sink.__in = -1
            sink.__out = 0        
            sink._connected = True
            self.__sink = sink
        finally:    
            self._cond.release()
    
    def write(self, data):
        if (self.__sink is None):
            raise Exception("Pipe not connected")
        self.__sink._receive_(data)
        
    def flush(self):
        self._cond.acquire()
        
        sink = self.__sink
        if sink is not None:
            sink._cond.acquire()
            sink._cond.notifyAll()
            sink._cond.release()
            
        self._cond.release()
    
    def close(self):
        sink = self.__sink
        if sink is not None:
            sink._received_last_()
            
class PipedInputStream:
    """ Based on all classes' documentaion guess what is in here :P
        Please document it.
        """
    def __init__(self):
        self.__buffer = bytearray(1024)
        self._cond = threading.Condition()
        self.__in = -1
        self.__out = 0
        self.__close_by_reader = False #Volatile
        self.__close_by_writer = False
        self.__read_side = None
        self.__write_side = None
        self._connected = False
        
    def connect(self, src):
        src.connect(self)
    
    def _receive_(self, b):
        self._cond.acquire()

        try:
            self.__check_state__()       
            self.__write_side = threading.current_thread()
            
            if (self.__in == self.__out):
                self._await_space_()
                
            if (self.__in < 0):
                self.__in = 0
                self.__out = 0
        
            self.__buffer[self.__in] = b
            self.__in += 1
        
            if (self.__in >= len(self.__buffer)):
                self.__in = 0
                
        except Exception as error:
            raise error
        finally:
            self._cond.release()
            
    def __check_state__(self):
        if self._connected == False:
            raise Exception("Pipe not connected")
        elif self.__close_by_reader or self.__close_by_writer:
            raise Exception("Pipe closed")
        elif (self.__read_side is not None) and (not self.__read_side.isAlive()):
            raise Exception("Read end dead")
        
    def _await_space_(self):
        while self.__in == self.__out:
            self.__check_state__()
            # full: kick any waiting readers
            self._cond.notifyAll()
            self._cond.wait(1.0)
        
    def _received_last_(self):
        self._cond.acquire()
        self.__close_by_writer = True
        self._cond.notify_all()
        self._cond.release()
    
    def read(self):
        self._cond.acquire()
        try:
            if not self._connected:
                raise Exception("Pipe not connected")
            elif self.__close_by_reader:
                raise Exception("Pipe closed")
            elif self.__write_side is not None and not self.__write_side.isAlive() and not self.__close_by_reader and (self.__in < 0):
                raise Exception("Write end dead")
            
            self.__read_side = threading.current_thread()
            trials = 2;
            while self.__in < 0 :
                if self.__close_by_writer:
                    #closed by writer, return EOF
                    return -1
                if (self.__write_side is not None and not self.__write_side.isAlive()):
                    trials -= 1
                    if trials < 0:
                        raise Exception("Pipe broken")            
                #might be a writer waiting
                self._cond.notifyAll()
                self._cond.wait(1.0)
            
            ret = self.__buffer[self.__out]
            self.__out += 1
            
            if (self.__out >= len(self.__buffer)):
                self.__out = 0;
    
            if (self.__in == self.__out):
                # now empty 
                self.__in = -1;
                
        finally:
            self._cond.release()
            
        return ret;
    
    def available(self):
        self._cond.acquire()      
        if self.__in < 0:
            aux = 0
        elif self.__in == self.__out:
            aux =  len(self.__buffer)
        elif self.__in > self.__out:
            aux = self.__in - self.__out;
        else:
            aux = self.__in + len(self.__buffer) - self.__out;
        self._cond.release()
        return aux
    
    def close(self):
        self.__close_by_reader = True
        self._cond.acquire()
        self.__in = -1
        self._cond.release()
        