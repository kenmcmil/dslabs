"""
setup_student.py

Set up to run tests in student directory

"""

import argparse
import json
import os
import subprocess
from distutils.dir_util import copy_tree
from shutil import copyfile, rmtree
from threading import Timer

parser = argparse.ArgumentParser()
parser.add_argument(
    "-d",
    "--handout",
    dest="handout",
    help="handout directory",
    required=True)
parser.add_argument(
    "-n",
    "--lab-num",
    dest="lab_num",
    help="The number corresponding to the lab",
    required=True)
parser.add_argument(
    "-l",
    "--lab-name",
    dest='lab',
    help='The name of the lab, such as "lab1-clientserver"',
    required=True)
args = parser.parse_args()

HANDOUT_DIRECTORY = args.handout

LAB_NAME = args.lab
LAB_NUMBER = args.lab_num

TEST_DIRECTORY = os.path.join(HANDOUT_DIRECTORY, 'labs', LAB_NAME, 'tst')



try:
    print("Setting up student ")

    student_path = '.'

    # Copy over all test folders
    for lab in os.listdir(os.path.join(HANDOUT_DIRECTORY, 'labs')):
            src_test_path = os.path.join(HANDOUT_DIRECTORY, 'labs', lab, 'tst')
            dst_test_path = os.path.join(student_path, 'labs', lab, 'tst')
            copy_tree(src_test_path, dst_test_path)

    #Copy jars files
    src_jars_path = os.path.join(HANDOUT_DIRECTORY, 'jars')
    output_jars_path = os.path.join(student_path, 'jars')
    copy_tree(src_jars_path, output_jars_path)

    for f in os.listdir(HANDOUT_DIRECTORY):
        full_file_path = os.path.join(HANDOUT_DIRECTORY, f)
        # Copy jars/Makefile/lombok.config/etc.
        if os.path.isfile(full_file_path):
            output_path = os.path.join(student_path, f)
            copyfile(full_file_path, output_path)


except ValueError as e:
    print('Encountered ' + str(e))
    raise(e)

