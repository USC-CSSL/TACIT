import pdb
import unittest
import math

from numpy import *
from zlabelLDA import zlabelLDA

class TestZhLDA(unittest.TestCase):
    
    def setUp(self):
        """
        Set up base data/parameter values
        """
        (self.T,self.W) = (4,6)
        self.alpha = .1 * ones((1,self.T))
        self.beta = .1 * ones((self.T,self.W))

        # docs must be a Python List of Python lists
        # Each individual List contains the indices of word tokens
        # (That is, docs[0] = "w1 w1 w2", docs[1] = "w1 w1 w1 w1 w2", etc) 
        #
        self.docs = [[1,1,2],
                     [1,1,1,1,2],
                     [3,3,3,3,5,5,5],
                     [3,3,3,3,4,4,4],
                     [0,0,0,0,0],
                     [0,0,0,0]]

        # Observed/constrained z
        self.zs = [[0,0,0],
                   [0,0,0,0,0],
                   [[0],[0],0,0,0,0,0],
                   [[1],[1],0,0,0,0,0],
                   [0,0,0,0,0],
                   [0,0,0,0]]

        # Confidence in these labels
        self.eta = .95
                
        self.init = [[0 for w in doc] for doc in self.docs]
        self.numsamp = 50
        self.randseed = 194582

        # equality tolerance for testing
        self.tol = 1e-6

    def matProb(self,mat):
        """
        Given a NumPy matrix,
        check that all values >= 0, sum to 1 (valid prob dist)
        """
        sumto1 = all([abs(val - float(1)) < self.tol
                      for val in mat.sum(axis=1)])
        geq0 = all([val >= 0 for val in
                    mat.reshape(mat.size)])
        return sumto1 and geq0

    #
    # These are to test *correct* operation
    #    

    def testStandard(self):
        """
        Test standard LDA with base data/params
        """
        (phi,theta,sample) = zlabelLDA(self.docs,self.zs,self.eta,
                                   self.alpha,self.beta,
                                   self.numsamp,self.randseed)

        # theta should clust docs [0,1], [2], [3], [4,5]
        maxtheta = argmax(theta,axis=1)
        self.assert_(maxtheta[2] == 0)
        self.assert_(maxtheta[3] == 1)
        self.assert_(maxtheta[0] == maxtheta[1])
        self.assert_(maxtheta[4] == maxtheta[5])      
        # theta valid prob matrix
        self.assert_(self.matProb(theta))

        # corresponding phi should emph [1], [3], [3], [0]
        maxphi = argmax(phi,axis=1)
        self.assert_(maxphi[maxtheta[0]] == 1)
        self.assert_(maxphi[maxtheta[4]] == 0)
        self.assert_(maxphi[0] == 3)
        self.assert_(maxphi[1] == 3)

        # phi valid prob matrix
        self.assert_(self.matProb(phi))
        

    #
    # These test that the program fails correctly on *bad input*
    #    

    """
    Test bad given z-labels, confidences 
    """

    def testNonListZ(self):
        """ Non-List z-label entry """
        self.zs[0] = 3
        self.assertRaises(RuntimeError,zlabelLDA,self.docs,self.zs,self.eta,
                          self.alpha,
                          self.beta,
                          self.numsamp,self.randseed)

    def testTooBigZ(self):
        """ Too large z-label entry """
        self.zs[0][0] = [self.T]
        self.assertRaises(RuntimeError,zlabelLDA,self.docs,self.zs,self.eta,
                          self.alpha,
                          self.beta,
                          self.numsamp,self.randseed)
        
    def testTooSmallZ(self):
        """ Too small z-label entry """
        self.zs[0][0] = [-1]
        self.assertRaises(RuntimeError,zlabelLDA,self.docs,self.zs,self.eta,
                          self.alpha,
                          self.beta,
                          self.numsamp,self.randseed)

    def testNonIntZ(self):
        """ Non-Int z-label entry """
        self.zs[0][0] = ['a']
        self.assertRaises(RuntimeError,zlabelLDA,self.docs,self.zs,self.eta,
                          self.alpha,
                          self.beta,
                          self.numsamp,self.randseed)
        
    """
    Test bad document data
    """

    def testNonListDoc(self):
        """  Non-list doc """
        self.docs[0] = None
        self.assertRaises(RuntimeError,zlabelLDA,self.docs,self.zs,self.eta,
                          self.alpha,
                          self.beta,
                          self.numsamp,self.randseed)
    def testNegWord(self):
        """  Bad word (negative) """
        self.docs[0][-1] = -1
        self.assertRaises(RuntimeError,zlabelLDA,self.docs,self.zs,self.eta,self.alpha,
                          self.beta,       
                          self.numsamp,self.randseed)
    def testBigWord(self):
        """  Bad word (too big)                 """
        self.docs[0][-1] = self.W
        self.assertRaises(RuntimeError,zlabelLDA,self.docs,self.zs,self.eta,self.alpha,
                          self.beta,       
                          self.numsamp,self.randseed)
    def testNonNumWord(self):
        """  Bad word (non-numeric) """
        self.docs[0][-1] = ''
        self.assertRaises(RuntimeError,zlabelLDA,self.docs,self.zs,self.eta,self.alpha,
                          self.beta,       
                          self.numsamp,self.randseed)

    """
    Test bad alpha/beta values
    """

    def testNegAlpha(self):
        """  Negative alpha """
        self.alpha[0,1] = -1
        self.assertRaises(RuntimeError,zlabelLDA,self.docs,self.zs,self.eta,self.alpha,self.beta,
                          self.numsamp,self.randseed)
    def testNegBeta(self):
        """  Negative beta """
        self.beta[1,2] = -1
        self.assertRaises(RuntimeError,zlabelLDA,self.docs,self.zs,self.eta,self.alpha,self.beta,
                          self.numsamp,self.randseed)
    def testAlphaBetaDim(self):
        """  Alpha/Beta dim mismatch """
        self.alpha = .1 * ones((1,4))
        self.beta = ones((3,5))
        self.assertRaises(RuntimeError,zlabelLDA,self.docs,self.zs,self.eta,self.alpha,self.beta,
                          self.numsamp,self.randseed)

    """
    Test bad init samples
    """

    def testTooFewInit(self):
        """  Too few docs """
        self.init = self.init[:-1]
        self.assertRaises(RuntimeError,zlabelLDA,self.docs,self.zs,self.eta,self.alpha,
                          self.beta,
                          self.numsamp,self.randseed,init=self.init)
    def testTooManyInit(self):
        """  Too many docs """
        self.init = self.init + [[]]
        self.assertRaises(RuntimeError,zlabelLDA,self.docs,self.zs,self.eta,self.alpha,
                          self.beta,
                          self.numsamp,self.randseed,init=self.init)
    def testLenMisInit(self):
        """  Doc length mismatch """
        self.init[2].append(0)
        self.assertRaises(RuntimeError,zlabelLDA,self.docs,self.zs,self.eta,self.alpha,
                          self.beta,
                          self.numsamp,self.randseed,init=self.init)
    def testNonListInit(self):
        """  Non-list doc sample """
        self.init[0] = None
        self.assertRaises(RuntimeError,zlabelLDA,self.docs,self.zs,self.eta,self.alpha,
                          self.beta,
                          self.numsamp,self.randseed,init=self.init)
    def testNegInit(self):
        """  Out-of-range topic value (negative) """
        self.init[0][0] = -2
        self.assertRaises(RuntimeError,zlabelLDA,self.docs,self.zs,self.eta,self.alpha,
                          self.beta,
                          self.numsamp,self.randseed,init=self.init)
    def testBigInit(self):
        """  Out-of-range topic value (too big) """
        self.init[0][0] = self.T
        self.assertRaises(RuntimeError,zlabelLDA,self.docs,self.zs,self.eta,self.alpha,
                          self.beta,
                          self.numsamp,self.randseed,init=self.init)
    def testNonNumInit(self):
        """  Out-of-range topic value (non-numeric) """
        self.init[0][0] = ''
        self.assertRaises(RuntimeError,zlabelLDA,self.docs,self.zs,self.eta,self.alpha,
                          self.beta,
                          self.numsamp,self.randseed,init=self.init)

    
"""  Run the unit tests! """
if __name__ == '__main__':
    unittest.main()
