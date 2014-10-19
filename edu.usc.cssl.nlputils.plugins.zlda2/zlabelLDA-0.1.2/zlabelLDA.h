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

// Uniform rand between [0,1] (inclusive)
#define unif() ((double) rand()) / ((double) RAND_MAX)

#define ARGS_OK 0
#define ARGS_BAD 1

// Represents the z-set constraint 
// for a given position in the corpus
typedef struct {
  int len; // length of C
  int* C; // acceptable values for this z
} zset;

typedef struct {
  PyArrayObject* nw;
  PyArrayObject* nd;
  PyArrayObject* nw_colsum;
} counts;

typedef struct {
  PyArrayObject* alpha;
  PyArrayObject* alphasum;
  PyArrayObject* beta;
  PyArrayObject* betasum;
  double eta;
  int T;
} model_params;

typedef struct {
  int D;
  int W;
  int* doclens;
  int** docs;
  zset*** zsets;
  int** sample;
  int* f;
} dataset;

static PyObject* zlabelLDA(PyObject *self, PyObject *args, PyObject* keywds);

static int convert_args(PyObject* docs_arg, PyObject* zs_arg, double eta,
                        PyArrayObject* alpha,
                        PyArrayObject* beta, PyObject* f_arg, PyObject* init,
                        model_params** mp, dataset** ds);

static counts* given_init(model_params* mp, dataset* ds, PyObject* init);
static counts* online_init(model_params* mp, dataset* ds);

static void gibbs_chain(model_params* mp, dataset* ds, counts* c);

static int mult_sample(double* vals, double sum);

static PyArrayObject* est_phi(model_params* mp, dataset* ds, counts* c);
static PyArrayObject* est_theta(model_params* mp, dataset* ds, counts* c);
