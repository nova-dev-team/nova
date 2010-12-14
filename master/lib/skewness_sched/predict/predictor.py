'''
Created on Jun 16, 2009

@author: sonic
'''
import copy

class Predictor(object):
    '''
    This is a parent abstract class of all load predictors
    '''


    # para of constructors:
    # 'pspan' - prediction span in sampling interval, default to 1
    def __init__(self,**kw):
        '''
        Constructor
        '''
        if 'pspan' in kw.keys():
            self.pspan = kw['pspan'];
        else:
            self.pspan = 1;
        if 'lookbackbufsize' in kw.keys():
            self.lbbs = kw['lookbackbufsize'];
        else:
            self.lbbs = self.pspan;
    
    def predict(self,sample):
        # to be override:
        raise Exception("predict is not implemented!");
    
    def reset(self):
        # to be override:
        raise Exception("reset is no implemented!");

##end of class definition of Predictor

def get_sla_threshold(oarr, slath):
    arr = copy.copy(oarr)
    n = len(arr);
    k = int(n*slath+0.99999);
    k = min(n,k);
    arr.sort();
    return arr[k-1];

# predict one: SINGLE EWMA Predictor
class SingleEWMAPredictor(Predictor):
    
    # initial parameters:
    # pspan - prediction span
    # alpha - alpha in [0,1] for EWMA algorithm, default to 0
    # slath - SLA Threshold 95% as default.
    def __init__(self, **kw):
        Predictor.__init__(self,**kw);
        # get alpha
        if 'alpha' not in kw.keys():
            self.alpha=0;
        else:
            self.alpha=kw['alpha'];
        # get SLA threshold
        if 'slath' not in kw.keys():
            self.slath=0.95;
        else:
            self.slath=kw['slath'];
        # data structures
        self.sample_buffer = []
        self.first_try = 1
        
    def predict(self,sample):
        self.sample_buffer.append(sample);
        # keep the size of the sample buffer
#        if len(self.sample_buffer) > self.pspan:
        if len(self.sample_buffer) > self.lbbs:
            self.sample_buffer = self.sample_buffer[1:]
        # get observation
        observation = get_sla_threshold(self.sample_buffer,self.slath);
        # estimate
        if self.first_try :
            self.last_estimation = observation
            self.first_try = False;
        else:
            self.last_estimation = self.last_estimation*self.alpha + observation*(1-self.alpha)
        return self.pspan,self.last_estimation
    
    def reset(self):
        self.sample_buffer = []
        self.first_try = 1
## end of class definition of SingleWEMAPredictor

# predict two: FastUpSlowDownPredictor
class FastUpSlowDownPredictor(SingleEWMAPredictor):
    
    # initial parameters:
    # pspan - prediction span
    # alpha - alpha in [0,1] for EWMA algorithm, default to 0
    # slath - SLA Threshold 95% as default.
    def __init__(self, **kw):
        SingleEWMAPredictor.__init__(self,**kw);
        self.upalpha = kw["upalpha"]
        self.downalpha = kw["downalpha"]
        
    def predict(self,sample):
        self.sample_buffer.append(sample);
        # keep the size of the sample buffer
#        if len(self.sample_buffer) > self.pspan:
        if len(self.sample_buffer) > self.lbbs:
            self.sample_buffer = self.sample_buffer[1:]
        # get observation
        observation = get_sla_threshold(self.sample_buffer,self.slath);
        # estimate
        if self.first_try :
            self.last_estimation = observation
            self.first_try = False;
        elif observation > self.last_estimation:
            self.last_estimation = self.last_estimation*self.upalpha + observation*(1-self.upalpha)
        else:
            self.last_estimation = self.last_estimation*self.downalpha + observation*(1-self.downalpha)
        return self.pspan,self.last_estimation
## end of class definition of SingleWEMAPredictor
