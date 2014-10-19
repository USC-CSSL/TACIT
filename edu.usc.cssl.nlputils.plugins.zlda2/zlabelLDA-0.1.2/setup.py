from distutils.core import setup, Extension
import os

from numpy.distutils.misc_util import *

numpyincl = get_numpy_include_dirs()

zhldamodule = Extension("zlabelLDA",
                    sources = ["zlabelLDA.c"],
                    include_dirs = [os.getcwd()] + numpyincl,
                    library_dirs = [],
                    libraries = [],
                    extra_compile_args = ['-O3','-Wall'],
                    extra_link_args = [])

setup(name = 'zlabelLDA',
      description = 'z-label LDA model',
      version = '0.1.2',
      author = 'David Andrzejewski',
      author_email = 'andrzeje@cs.wisc.edu',
      license = 'GNU General Public License (Version 3 or later)',
      url = 'http://pages.cs.wisc.edu/~andrzeje/research/zlabel_lda.html',
      ext_modules = [zhldamodule],
      py_modules = [])
