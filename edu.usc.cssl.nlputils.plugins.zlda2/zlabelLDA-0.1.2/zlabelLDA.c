/**
   zlabelLDA - Implementation of Latent Dirichlet Allocation with 
   Topic-in-Set Knowledge (z-labels)
   Copyright (C) 2009 David Andrzejewski (andrzeje@cs.wisc.edu)
 
   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#include <Python.h>
#include <numpy/arrayobject.h>

#include "zlabelLDA.h"

/**
 * This is the exposed method which is called from Python
 */
static PyObject * zlabelLDA(PyObject *self, PyObject *args, PyObject* keywds)
{
  // Null-terminated list of arg keywords
  //
  static char *kwlist[] = {"docs","zsets","eta","alpha","beta","numsamp",
                           "randseed","f","init","verbose",NULL};
  // Required args
  //
  PyObject* docs_arg; // List of Lists
  PyObject* zs_arg; // List of Lists
  double eta; // Strength parameter for constraints
  PyArrayObject* alpha; // NumPy Array
  PyArrayObject* beta; // NumPy Array
  int numsamp;
  int randseed;
  // Optional args
  // 
  PyObject* f_arg = NULL; // List of f-labels foreach doc
  PyObject* init = NULL; // List of Lists to initialize Gibbs
  int verbose = 0; // 1 = verbose output

  // Parse function args
  //
  if(!PyArg_ParseTupleAndKeywords(args,keywds,"O!O!dO!O!ii|O!O!i",kwlist,
                                  &PyList_Type,&docs_arg,
                                  &PyList_Type,&zs_arg,
                                  &eta,
                                  &PyArray_Type,&alpha,
                                  &PyArray_Type,&beta,
                                  &numsamp,&randseed,
                                  &PyList_Type,&f_arg,
                                  &PyList_Type,&init,
                                  &verbose))
    // ERROR - bad args
    return NULL;
 
  // Use args to populate structs
  // (also check for *validity*)
  //
  model_params* mp;
  dataset* ds;
  if(ARGS_BAD == convert_args(docs_arg,zs_arg,eta,
                              alpha,beta,f_arg,init,&mp,&ds))
    {
      // Args bad! Return to Python...error condition should be set
      return NULL;
    }
  
  // Init random number generator
  //
  srand((unsigned int) randseed);

  // Init from previous sample, or do online init?
  //
  int si,d,i,ej;
  counts* c;
  if(init == NULL)
    {
      c = online_init(mp,ds);
    }
  else
    {
      c = given_init(mp,ds,init);
      if(c == NULL)
        {
          // ERROR - something went wrong with user-supplied init          
          for(d = 0; d < ds->D; d++) 
            {
              free(ds->docs[d]);
              for(ej = 0; ej < ds->doclens[d]; ej++)
                {
                  if(ds->zsets[d][ej])
                    {
                      free(ds->zsets[d][ej]->C);
                      free(ds->zsets[d][ej]);
                    }
                }
              free(ds->zsets[d]);
            }
          free(ds->docs);
          free(ds->zsets);
          free(ds->doclens);
          free(ds->f);
          free(ds);
          Py_DECREF(mp->alphasum);
          Py_DECREF(mp->betasum);
          free(mp);
          return NULL;
        }
    }
  
  // Do the requested number of Gibbs samples
  for(si=0; si < numsamp; si++)
    {
      // In order to make it easier to test against intLDA...
      srand((unsigned int) randseed + si);

      if(verbose == 1)
        printf("Gibbs sample %d of %d\n",si,numsamp);

      gibbs_chain(mp,ds,c);      
    }
  
  // Estimate phi and theta
  PyArrayObject* phi = est_phi(mp, ds, c);
  PyArrayObject* theta = est_theta(mp, ds, c);

  // Convert final sample back to List of Lists for return
  PyObject* finalsamp = PyList_New(ds->D);
  for(d = 0; d < ds->D; d++)
    {
      PyObject* docz = PyList_New(ds->doclens[d]);
      for(i = 0; i < ds->doclens[d]; i++)
        PyList_SetItem(docz,i,PyInt_FromLong(ds->sample[d][i]));
      PyList_SetItem(finalsamp,d,docz);
    }
  
  // Package phi, theta, and final sample in tuple for return
  PyObject* retval = PyTuple_New(3);
  PyTuple_SetItem(retval,0,(PyObject*) phi);
  PyTuple_SetItem(retval,1,(PyObject*) theta);
  PyTuple_SetItem(retval,2,finalsamp);

  // Do memory cleanup...
  Py_DECREF(c->nw);
  Py_DECREF(c->nd);
  Py_DECREF(c->nw_colsum);
  free(c);

  for(d = 0; d < ds->D; d++) 
    {
      free(ds->docs[d]);
      for(ej = 0; ej < ds->doclens[d]; ej++)
        {
          if(ds->zsets[d][ej])
            {
              free(ds->zsets[d][ej]->C);
              free(ds->zsets[d][ej]);
            }
        }
      free(ds->zsets[d]);
      free(ds->sample[d]);      
    }
  free(ds->docs);
  free(ds->zsets);
  free(ds->sample);
  free(ds->doclens);
  free(ds->f);
  free(ds);

  Py_DECREF(mp->alphasum);
  Py_DECREF(mp->betasum);
  free(mp);
  
  return (PyObject*) retval;
}

/**
 * Do an "online" init of Gibbs chain, adding one word
 * position at a time and then sampling for each new position
 */
static counts* online_init(model_params* mp, dataset* ds)
{   
  // Do some init
  //
  int W = ds->W;
  int D = ds->D;
  int T = mp->T;
 
  // Alloc and init count matrices and init sample
  //
  counts* c = (counts*) malloc(sizeof(counts));  

  npy_intp* nwdims = malloc(sizeof(npy_intp)*2);
  nwdims[0] = W;
  nwdims[1] = T;
  c->nw = (PyArrayObject*) PyArray_ZEROS(2,nwdims,PyArray_INT,0);
  free(nwdims);

  npy_intp* nddims = malloc(sizeof(npy_intp)*2);
  nddims[0] = D;
  nddims[1] = T;
  c->nd =  (PyArrayObject*) PyArray_ZEROS(2,nddims,PyArray_INT,0);
  c->nw_colsum = (PyArrayObject*) PyArray_Sum(c->nw,0,PyArray_INT,NULL);
  free(nddims);

  ds->sample = malloc(sizeof(int*) * D);
  
  // Build init z sample, one word at a time
  //

  // Temporary arrays used for sampling
  double* num = malloc(sizeof(double)*T);

  // For each doc in corpus
  int d,i,j,oki; // oki = OK index (for constraint testing...)
  for(d = 0; d < D; d++) 
    {
      // Get this doc, f-label, z-label 
      int* doc = ds->docs[d];
      zset** z = ds->zsets[d];
      int doclen = ds->doclens[d];
      int f = ds->f[d];

      // Create this sample
      ds->sample[d] = malloc(sizeof(int) * doclen);
      int* sample = ds->sample[d];

      // For each word in doc
      for(i = 0; i < doclen; i++)
        {      
          int w_i = doc[i];
	
          // For each topic, calculate numerators
          double norm_sum = 0;
          for(j = 0; j < T; j++) 
            { 
              double alpha_j = *((double*)PyArray_GETPTR2(mp->alpha,f,j));
              double beta_i = *((double*)PyArray_GETPTR2(mp->beta,j,w_i));
              double betasum = *((double*)PyArray_GETPTR1(mp->betasum,j));	
              double denom_1 = *((int*)PyArray_GETPTR1(c->nw_colsum,j)) + betasum;

              // Calculate numerator for this topic
              // (NOTE: alpha denom omitted, since same for all topics)
              num[j] = ((*((int*)PyArray_GETPTR2(c->nw,w_i,j)))+beta_i) / denom_1;
              num[j] = num[j] * (*((int*)PyArray_GETPTR2(c->nd,d,j))+alpha_j);

              // Apply a multiplicative penalty (if applicable)
              //
              if(z[i])
                {
                  int ok = 0;
                  // Look for j in the OK set
                  for(oki = 0; oki < z[i]->len; oki++)
                    {
                      if(j == z[i]->C[oki])
                        {
                          ok = 1;
                          break;
                        }
                    }
                  // If we didn't find it, penalize!
                  if(ok == 0)
                    num[j] = num[j] * (1-mp->eta);
                }

              // Add to running normalization sum
              norm_sum += num[j];
            }

          // Draw sample 
          j = mult_sample(num,norm_sum);
	
          // Update count/cache matrices and initial sample vec
          //
          sample[i] = j;

          (*((int*)PyArray_GETPTR2(c->nw,w_i,j)))++;
          (*((int*)PyArray_GETPTR2(c->nd,d,j)))++;
          (*((int*)PyArray_GETPTR1(c->nw_colsum,j)))++;
        }
    }
  // Cleanup, put all counts in struct, and return
  //
  free(num);
  return c;
}

/**
 * Init counts and sampler from a user-supplied initial state
 */
static counts* given_init(model_params* mp, dataset* ds, PyObject* init) 
{  
  // Do some init
  //
  int W = ds->W;
  int D = ds->D;
  int T = mp->T;
 
  // Make sure initial sample has correct number of docs
  if(D != PyList_Size(init))
    {
      // ERROR
      PyErr_SetString(PyExc_RuntimeError,
                      "Number of docs / number of init samples mismatch");
      return NULL;
    }

  // Alloc and init count matrices and init sample
  //
  counts* c = (counts*) malloc(sizeof(counts));  

  npy_intp* nwdims = malloc(sizeof(npy_intp)*2);
  nwdims[0] = W;
  nwdims[1] = T;
  c->nw = (PyArrayObject*) PyArray_ZEROS(2,nwdims,PyArray_INT,0);
  free(nwdims);

  npy_intp* nddims = malloc(sizeof(npy_intp)*2);
  nddims[0] = D;
  nddims[1] = T;
  c->nd =  (PyArrayObject*) PyArray_ZEROS(2,nddims,PyArray_INT,0);
  c->nw_colsum = (PyArrayObject*) PyArray_Sum(c->nw,0,PyArray_INT,NULL);
  free(nddims);
  
  ds->sample = malloc(sizeof(int*) * D);

  // Now populate count matrices from given init sample
  //
  int d,i;
  for(d = 0; d < D; d++)
    {
      ds->sample[d] = malloc(sizeof(int) * ds->doclens[d]);
      PyObject* docinit = PyList_GetItem(init,d);
      // Verify that this is a List, and that it has the right length
      if(!PyList_Check(docinit))
        {
          // ERROR
          PyErr_SetString(PyExc_RuntimeError,
                          "Non-List element in initial sample");
          Py_DECREF(c->nw);
          Py_DECREF(c->nd);
          Py_DECREF(c->nw_colsum);
          free(c);
          for(i = 0; i <= d; i++)
            free(ds->sample[i]);
          free(ds->sample);
          return NULL;
        }
      else if(ds->doclens[d] != PyList_Size(docinit))
        {
          // ERROR
          PyErr_SetString(PyExc_RuntimeError,
                          "Init sample/doc length mismatch");
          Py_DECREF(c->nw);
          Py_DECREF(c->nd);
          Py_DECREF(c->nw_colsum);
          free(c);
          for(i = 0; i <= d; i++)
            free(ds->sample[i]);
          free(ds->sample);
          return NULL;
        }

      for(i = 0; i < ds->doclens[d]; i++) 
        {
          // Get topic from init sample
          int zi = PyInt_AsLong(PyList_GetItem(docinit,i));
          if(zi < 0 || zi > (T-1))
            {
              // ERROR
              PyErr_SetString(PyExc_RuntimeError,
                              "Non-numeric or out of range init sample value");
              Py_DECREF(c->nw);
              Py_DECREF(c->nd);
              Py_DECREF(c->nw_colsum);
              free(c);
              for(i = 0; i <= d; i++)
                free(ds->sample[i]);
              free(ds->sample);
              return NULL;              
            }
 
          // Get word
          int wi = ds->docs[d][i];

          // Store topic and increment count matrices
          ds->sample[d][i] = zi;
          (*((int*)PyArray_GETPTR2(c->nw,wi,zi)))++;
          (*((int*)PyArray_GETPTR2(c->nd,d,zi)))++;
          (*((int*)PyArray_GETPTR1(c->nw_colsum,zi)))++;
        }
    }
  
  return c;
}

/**
 * Run Gibbs chain to get a new full sample
 */
void gibbs_chain(model_params* mp, dataset* ds, counts* c)
{
  // Do some init 
  //
  int D = ds->D;
  int T = mp->T;
 
  // Temporary arrays used for sampling
  double* num = malloc(sizeof(double)*T);

  // Use Gibbs sampling to get a new z
  // sample, one position at a time
  //

  // foreach doc in corpus
  //
  int d,j,i,oki; // oki = OK index (for constraint testing...)
  for(d = 0; d < D; d++) 
    {
      // Get this doc and f-label
      int doclen = ds->doclens[d];
      int* doc = ds->docs[d];
      zset** z = ds->zsets[d];
      int f = ds->f[d];

      // Get this sample
      int* sample = ds->sample[d];

      // For each word in doc
      for(i = 0; i < doclen; i++)
        {      
          // remove this w/z pair from all count/cache matrices 
          int z_i = sample[i];
          int w_i = doc[i];

          (*((int*)PyArray_GETPTR2(c->nw,w_i,z_i)))--;
          (*((int*)PyArray_GETPTR2(c->nd,d,z_i)))--;
          (*((int*)PyArray_GETPTR1(c->nw_colsum,z_i)))--;
      	
          // For each topic, calculate numerators
          double norm_sum = 0;
          for(j = 0; j < T; j++) 
            { 
              double alpha_j = *((double*)PyArray_GETPTR2(mp->alpha,f,j));
              double beta_i = *((double*)PyArray_GETPTR2(mp->beta,j,w_i));
              double betasum = *((double*)PyArray_GETPTR1(mp->betasum,j));
              double denom_1 = *((int*)PyArray_GETPTR1(c->nw_colsum,j)) 
                + betasum;	
              // Calculate numerator for each topic
              // (NOTE: alpha denom omitted, since same for all topics)
              num[j] = ((*((int*)PyArray_GETPTR2(c->nw,w_i,j)))+beta_i) 
                / denom_1;
              num[j] = num[j] * (*((int*)PyArray_GETPTR2(c->nd,d,j))+alpha_j);

              // Apply a multiplicative penalty (if applicable)
              //
              if(z[i])
                {
                  int ok = 0;
                  // Look for j in the OK set
                  for(oki = 0; oki < z[i]->len; oki++)
                    {
                      if(j == z[i]->C[oki])
                        {
                          ok = 1;
                          break;
                        }
                    }
                  // If we didn't find it, penalize!
                  if(ok == 0)
                    num[j] = num[j] * (1-mp->eta);
                }

              // Add to running normalization sum
              norm_sum += num[j];
            }	

          // Draw sample 
          j = mult_sample(num,norm_sum);

          // update count/cache matrices and sample vec
          //
          sample[i] = j;
          (*((int*)PyArray_GETPTR2(c->nw,w_i,j)))++;
          (*((int*)PyArray_GETPTR2(c->nd,d,j)))++;
          (*((int*)PyArray_GETPTR1(c->nw_colsum,j)))++;
        }
    }
  // Just cleanup and return 
  // (new sample will be returned in ds->sample
  //
  free(num);
  return;
}

/**
 * Use final sample to estimate theta = P(z|d)
 */
PyArrayObject* est_theta(model_params* mp, dataset* ds, counts* c)
{
  int D = ds->D;
  int T = mp->T;
 
  npy_intp* tdims = malloc(sizeof(npy_intp)*2);
  tdims[0] = D;
  tdims[1] = T;
  PyArrayObject* theta = (PyArrayObject*) 
    PyArray_ZEROS(2,tdims,PyArray_DOUBLE,0);
  free(tdims);
    
  PyArrayObject* rowsums = (PyArrayObject*) PyArray_Sum(c->nd,1,PyArray_DOUBLE,NULL);

  int d,t;
  for(d = 0; d < D; d++) 
    {
      double rowsum = *((double*)PyArray_GETPTR1(rowsums,d));
      int f = ds->f[d];
      double alphasum = *((double*)PyArray_GETPTR1(mp->alphasum,f));
      for(t = 0; t < T; t++)
        {
          double alpha_t = *((double*)PyArray_GETPTR2(mp->alpha,f,t));
          int ndct = *((int*)PyArray_GETPTR2(c->nd,d,t));

          // Calc and assign theta entry
          double newval = (ndct + alpha_t) / (rowsum+alphasum);
          *((double*)PyArray_GETPTR2(theta,d,t)) = newval;
        }
    }
  return theta;
}

/**
 * Use final sample to estimate phi = P(w|z)
 */
PyArrayObject* est_phi(model_params* mp, dataset* ds, counts* c)
{  
  int W = ds->W;
  int T = mp->T;
 
  npy_intp* pdims = malloc(sizeof(npy_intp)*2);
  pdims[0] = T;
  pdims[1] = W;
  PyArrayObject* phi = (PyArrayObject*) 
    PyArray_ZEROS(2,pdims,PyArray_DOUBLE,0);
  free(pdims);

  int t,w;
  for(t = 0; t < T; t++) 
    {
      int colsum = (*((int*)PyArray_GETPTR1(c->nw_colsum,t)));
      double betasum = *((double*)PyArray_GETPTR1(mp->betasum,t));
      for(w = 0; w < W; w++) 
        {
          double beta_w = *((double*)PyArray_GETPTR2(mp->beta,t,w));
          int nwct = *((int*)PyArray_GETPTR2(c->nw,w,t));
          double newval = (beta_w + nwct) / (betasum + colsum);
          *((double*)PyArray_GETPTR2(phi,t,w)) = newval;
        }
    }
  return phi;
}


/**
 * Simultaneously check args and populate structs
 */
static int convert_args(PyObject* docs_arg, PyObject* zs_arg, double eta,
                        PyArrayObject* alpha,
                        PyArrayObject* beta, PyObject* f_arg, PyObject* init,
                        model_params** p_mp, dataset** p_ds)
  {
    int i;
    int fmax = 0; // Will need to ensure alpha dim is this big
    int D = PyList_Size(docs_arg);
    int* f = malloc(sizeof(int)*D);    
     
    // If f-labels were not specified, just set all to zero
    if(f_arg == NULL)
      {
        for(i = 0; i < D; i++)
          f[i] = 0;
      }
    // Otherwise convert and check
    else
      {
        if(D != PyList_Size(f_arg)) 
          {
            // ERROR
            PyErr_SetString(PyExc_RuntimeError,
                            "f and docs have different lengths");
            free(f);
            return ARGS_BAD;            
          }
        for(i = 0; i < D; i++)
          {            
            f[i] = PyInt_AsLong(PyList_GetItem(f_arg,i));
            // If PyInt_AsLong fails, it returns -1
            // (also should not be getting neg f-values anyway)
            if(f[i] < 0)
              {
                // ERROR
                PyErr_SetString(PyExc_RuntimeError,
                                "Non-numeric or negative f-value");
                free(f);
                return ARGS_BAD;
              }
            else if(f[i] > fmax)
              {
                fmax = f[i];
              }
          }
      }
  
    // Get some basic information from parameters
    // (and check dimensionality agreement)
    int T = PyArray_DIM(beta,0);
    int W = PyArray_DIM(beta,1);
    int F = PyArray_DIM(alpha,0);
    if(fmax != (F - 1))
      {
        // ERROR
        PyErr_SetString(PyExc_RuntimeError,
                        "Alpha/f dimensionality mismatch");
        free(f);
        return ARGS_BAD;
      }
    if(T != PyArray_DIM(alpha,1))
      {
        // ERROR
        PyErr_SetString(PyExc_RuntimeError,
                        "Alpha/Beta dimensionality mismatch");
        free(f);
        return ARGS_BAD;
      }

    // Check that all alpha/beta values are non-negative
    //
    double betamin = PyFloat_AsDouble(PyArray_Min(beta,NPY_MAXDIMS,NULL));
    double alphamin = PyFloat_AsDouble(PyArray_Min(alpha,NPY_MAXDIMS,NULL));
    if(betamin <= 0 || alphamin < 0)       
      {
        // ERROR
        PyErr_SetString(PyExc_RuntimeError,
                        "Negative Alpha or negative/zero Beta value");
        free(f);
        return ARGS_BAD;
      }

    // Convert documents from PyObject* to int[] 
    //
    int d,k;
    int ei,ej; // error indices for cleanups...
    int* doclens = malloc(sizeof(int) * D);
    int** docs = malloc(sizeof(int*) * D);
    zset*** zsets = malloc(sizeof(zset**) * D);

    for(d = 0; d < D; d++)
      {
        PyObject* z = PyList_GetItem(zs_arg,d);
        PyObject* doc = PyList_GetItem(docs_arg,d);
        if(!PyList_Check(doc) || !PyList_Check(z))
          {
            // ERROR
            PyErr_SetString(PyExc_RuntimeError,
                            "Non-List element in docs or zsets");
            free(f);
            for(ei = 0; ei < d; ei++)
              {
                free(docs[ei]);
                for(ej = 0; ej < doclens[ei]; ej++)
                  {
                    if(zsets[ei][ej])
                      {
                        free(zsets[ei][ej]->C);
                        free(zsets[ei][ej]);
                      }
                  }
                free(zsets[ei]);
              }
            free(zsets);
            free(docs);
            free(doclens);              
            return ARGS_BAD;
          }
        doclens[d] = PyList_Size(doc);
        if(doclens[d] != PyList_Size(z))
          {
            // ERROR
            PyErr_SetString(PyExc_RuntimeError,
                            "Doc/z length mismatch!");
            free(f);
            for(ei = 0; ei < d; ei++)
              {
                free(docs[ei]);
                for(ej = 0; ej < doclens[ei]; ej++)
                  {
                    if(zsets[ei][ej])
                      {
                        free(zsets[ei][ej]->C);
                        free(zsets[ei][ej]);
                      }
                  }
                free(zsets[ei]);
              }
            free(zsets);
            free(docs);
            free(doclens);              
            return ARGS_BAD;
          }

        docs[d] = malloc(sizeof(int) * doclens[d]);
        zsets[d] = malloc(sizeof(zset*) * doclens[d]);
        for(i = 0; i < doclens[d]; i++)
          {
            docs[d][i] = PyInt_AsLong(PyList_GetItem(doc,i));
            PyObject* cur_z = PyList_GetItem(z,i);
            if(PyList_Check(cur_z))
              {
                zsets[d][i] = malloc(sizeof(zset));
                zsets[d][i]->len = PyList_Size(cur_z);
                zsets[d][i]->C = malloc(sizeof(int)*zsets[d][i]->len);
                for(k = 0; k < zsets[d][i]->len; k++)
                  {
                    // Validate the z-labels
                    zsets[d][i]->C[k] = PyInt_AsLong(PyList_GetItem(cur_z,k));
                    if(zsets[d][i]->C[k] < 0 || zsets[d][i]->C[k] >= T
                       || !PyInt_Check(PyList_GetItem(cur_z,k)))
                      {
                        // ERROR
                        PyErr_SetString(PyExc_RuntimeError,
                                        "Out-of-range or non-Int z-label");
                        free(f);
                        for(ei = 0; ei < d; ei++) 
                          {
                            free(docs[ei]);
                            for(ej = 0; ej < doclens[ei]; ej++)
                              {
                                if(zsets[ei][ej])
                                  {
                                    free(zsets[ei][ej]->C);
                                    free(zsets[ei][ej]);
                                  }
                              }
                            free(zsets[ei]);
                          }
                        // Now ei==d, do final zsets up to where we failed
                        free(docs[ei]);
                        for(ej = 0; ej <= i; ej++)
                          {
                            if(zsets[ei][ej])
                              {
                                free(zsets[ei][ej]->C);
                                free(zsets[ei][ej]);
                              }
                          }
                        free(zsets[ei]);

                        free(zsets);
                        free(docs);
                        free(doclens);              
                        return ARGS_BAD;
                      }
                  }
              }
            else
              {
                zsets[d][i] = NULL;
              }
            if(docs[d][i] < 0 || docs[d][i] > (W - 1))
              {
                // ERROR
                PyErr_SetString(PyExc_RuntimeError,
                                "Non-numeric or out of range word");
                free(f);
                for(ei = 0; ei <= d; ei++) 
                  {
                    free(docs[ei]);
                    for(ej = 0; ej < doclens[ei]; ej++)
                      {
                        if(zsets[ei][ej])
                          {
                            free(zsets[ei][ej]->C);
                            free(zsets[ei][ej]);
                          }
                      }
                    free(zsets[ei]);
                  }
                free(zsets);
                free(docs);
                free(doclens);              
                return ARGS_BAD;
              }
          }
      }

    // Populate dataset struct
    //
    dataset* ds = (dataset*) malloc(sizeof(dataset));
    ds->D = D;
    ds->W = W;
    ds->doclens = doclens;
    ds->docs = docs;
    ds->zsets = zsets;
    ds->sample = NULL;
    ds->f = f;
 
    // Populate model params struct
    //     
    model_params* mp = (model_params*) malloc(sizeof(model_params));
    mp->eta = eta;
    mp->alpha = alpha;
    mp->beta = beta;
    mp->T = T;

    mp->alphasum = (PyArrayObject*) PyArray_Sum(alpha,1,PyArray_DOUBLE,NULL);
    mp->betasum = (PyArrayObject*) PyArray_Sum(beta,1,PyArray_DOUBLE,NULL);
    
    *(p_ds) = ds;
    *(p_mp) = mp;
    return ARGS_OK;
  }

/**
 * Draw a multinomial sample propto vals
 * 
 * (!!! we're assuming sum is the correct sum for vals !!!)
 * 
 */
static int mult_sample(double* vals, double norm_sum)
{
  double rand_sample = unif() * norm_sum;
  double tmp_sum = 0;
  int j = 0;
  while(tmp_sum < rand_sample || j == 0) {
    tmp_sum += vals[j];
    j++;
  }
  return j - 1;
}

//
// PYTHON EXTENSION BOILERPLATE BELOW
//

// Defines the module method table
PyMethodDef methods[] = 
  {
    {"zlabelLDA", (PyCFunction) zlabelLDA, 
     METH_VARARGS | METH_KEYWORDS, "Do inference for z-label LDA"},
    {NULL, NULL, 0, NULL}  // Is a 'sentinel' (?)
  };

// This is a macro that does stuff for us (linkage, declaration, etc)
PyMODINIT_FUNC 
initzlabelLDA() // Passes method table to init our module
{
  (void) Py_InitModule("zlabelLDA", methods); 
  import_array(); // Must do this to satisfy NumPy (!)
}
