#!/usr/bin/env python

import shutil
import sys

if __name__ == '__main__':
  try:
    with open('submits/version.txt', 'r') as version_file:
      version = int(version_file.read())
  except:
    version = 0
  shutil.make_archive('submits/{}'.format(version), 'zip', 'src')
  print("saved version {}".format(version))
  with open('submits/version.txt', 'w') as version_file:
    print(version + 1, file=version_file)
