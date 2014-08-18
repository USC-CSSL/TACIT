#!/usr/bin/python

##############################################
#
# zlabelLDA wrapper
#
# ./zlab.sh TOPICS_FILE DOCUMENT_DIRECTORY N
#
# TOPICS_FILE is a text file with one topic
# per line, with seed words for each topic
# (topics.txt is a sample topics file)
#
# DOCUMENT_DIRECTORY is a directory
# containing the text files to process
#
# N is the desired number of topics
#
# Kenji Sagae
# sagae@ict.usc.edu
#
#############################################

import math
import re
import os
from numpy import *
import sys
sys.path.append(os.path.dirname(os.path.realpath(__file__)) + '/lib/python' + sys.version.split('.')[0] + '.' + sys.version.split('.')[1] + '/site-packages')
from zlabelLDA import zlabelLDA
import Stemmer

topicsfilename = sys.argv[1]
datadir = sys.argv[2]
NUMTOPICS = int(sys.argv[3])

pstem = Stemmer.Stemmer('english')

numsamp = 2000

dct = {}
id = {}
dcnt = {}
spl =  re.compile(r'[^a-zA-Z0-9\-\_]')
wrdre = re.compile(r'[a-zA-Z0-9]')
docs = []
tdocs = []
zs = []

stopwords = open('stop.txt').read().split() # []

tf = open(topicsfilename, 'r')
ptops = {}
tnum = 0

while(1):
    str = tf.readline().rstrip()
    if(str == ''):
        break
    astr = str.split()
    for w in astr:
        ptops[pstem.stemWord(w)] = tnum
    tnum += 1
        
# first go through all the files and build
# the dictionary

for root, dirs, files in os.walk(datadir): 
    for name in files:
        # print name
        mywords = re.sub(spl, ' ', open(os.path.join(root,name)).read()).split()
        mywords = map(lambda x:x.lower(), mywords)
        mywords = pstem.stemWords(mywords)

        for w in mywords:
            # w = w.lower()
	    if not wrdre.match(w):
		continue
            if w in stopwords:
                continue
            if w not in dcnt:
                dcnt[w] = 0
            dcnt[w] += 1
        tdocs.append(mywords)

#print tdocs

dnum = 0
for d in tdocs:
    wnum = 0
    docs.append([])
    zs.append([])
    for w in d:
        w = w.lower()
	if not wrdre.match(w):
	    continue
        if w in stopwords:
            continue
        if dcnt[w] < 5:
            continue
        if w not in dct:
            id[len(id)] = w
            dct[w] = len(dct)
        docs[len(docs)-1].append(dct[w])
        if(w in ptops):
            zs[len(zs)-1].append([ptops[w]])
        else:
            zs[len(zs)-1].append(0)

vsize = len(dct)
print len(dct)

# print zs
# print docs

#######################################

# now we've loaded the docs

def sort_by_value(d):
    """ Returns the keys of dictionary d sorted by their values """
    items = [(v, k) for k, v in d.items()]
    items.sort()
    items.reverse()
    return [(k, v) for v, k in items]

alpha = .5 * ones((1,NUMTOPICS))
beta = .1 * ones((NUMTOPICS,vsize))

#zs = 1 * docs

randseed = 104582
eta = 1
(phi,theta,sample) = zlabelLDA(docs,zs,eta,alpha,beta,numsamp,randseed)

# print array_str(phi,precision=2)

j = 1
for t in phi:
    print "TOPIC ", j
    tw = {}
    i = 0
    for w in t:
        tw[id[i]] = w
        i += 1

    st = sort_by_value(tw)

    i = 0
    for it in st:
        print it
        i += 1
        if(i > 50):
            break
    print
    j += 1
