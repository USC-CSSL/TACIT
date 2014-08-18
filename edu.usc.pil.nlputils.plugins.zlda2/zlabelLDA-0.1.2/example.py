import math
from numpy import *
from zlabelLDA import zlabelLDA

# alpha and beta *must* be NumPy Array objects
#
# Their dimensionalities implicitly specify:
# -the number of topics T
# -the vocabulary size W
# -number of f-label values F (just 1 for 'standard' LDA)
#
# alpha = F x T
# beta = T x W
#
(T,W) = (4,6)
alpha = .1 * ones((1,T))
beta = .1 * ones((T,W))
eta = .95 # confidence in the our labels

# docs must be a Python List of Python lists
# Each individual List contains the indices of word tokens
# (That is, docs[0] = "w1 w1 w2", docs[1] = "w1 w1 w1 w1 w2", etc) 
#
docs = [[1,1,2],
        [1,1,1,1,2],
        [3,3,3,3,5,5,5],
        [3,3,3,3,4,4,4],
        [0,0,0,0,0],
        [0,0,0,0]]

# Observed/constrained z
# -force '1' words to topic 0
# -force '2' words to topic 1
#
# (each entry is ignored unless it is a List)
zs = [[[0],[0],[1]],
      [[0],[0],[0],[0],[1]],
      [0,0,0,0,0,0,0],
      [0,0,0,0,0,0,0],
      [0,0,0,0,0],
      [0,0,0,0]]

# numsamp specifies how many samples to take from the Gibbs sampler
numsamp = 100

# randseed is used to initialize the Gibbs sampler random number generator
randseed = 194582

# This command will run the standard LDA model
# (eta = 0 --> don't use z-labels)
#eta = 0
#(phi,theta,sample) = zlabelLDA(docs,zs,eta,alpha,beta,numsamp,randseed)

# This command will run the z-label LDA model
# (eta = 1 --> "hard" z-labels)
eta = 1
(phi,theta,sample) = zlabelLDA(docs,zs,eta,alpha,beta,numsamp,randseed)

# This command will initialize the Gibbs sampler from a user-supplied sample
#
#(phi,theta,sample) = zlabelLDA(docs,zs,eta,alpha,beta,
#                               numsamp,randseed,init=sample)

# This command will run standard LDA, but show Gibbs sampler output
# ("Gibbs sample X of Y")
#
#(phi,theta,sample) = zlabelLDA(docs,zs,eta,alpha,beta,numsamp,
#                               randseed,verbose=1)

# theta is the matrix of document-topic probabilities
# (estimated from final sample)
# 
# theta = D x T
# theta[di,zj] = P(z=zj | d=di)
#
print ''
print 'Theta - P(z|d)'
print array_str(theta,precision=2)
print ''

# phi is the matrix of topic-word probabilities 
# (estimated from final sample)
# 
# phi = T x W
# phi[zj,wi] = P(w=wi | z=zj)
#
print ''
print 'Phi - P(w|z)'
print array_str(phi,precision=2)
print ''

# Since the simple documents we created and fed into zlabelLDA exhibit such
# clearly divided word usage patterns, the resulting phi and theta
# should reflect these patterns nicely
