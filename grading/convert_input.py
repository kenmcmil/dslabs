#!/usr/bin/env python3
# coding: utf-8

#Converts submission zip files into this format - student_name/submit.tar.gz

import sys
import os
import json
import subprocess
import shutil

# Read configuration file
with open('config.json', 'r') as fd:
    config = json.loads(fd.read())

STUDENT_SUBMISSION_PATH = os.path.expanduser(config['submission_path'])

for f in os.listdir(STUDENT_SUBMISSION_PATH):
    if f.endswith(".zip"):
        student_name = f.partition('_')[0]
        zip_file = os.path.join(STUDENT_SUBMISSION_PATH, f)
        student_name_dir = os.path.join(STUDENT_SUBMISSION_PATH, student_name)
        #Create a new directory for each student
        os.makedirs(student_name_dir, exist_ok=True)
        #Unzip the submission zip file
        shutil.unpack_archive(zip_file,STUDENT_SUBMISSION_PATH , "zip")
        submit_path_name = os.path.join(student_name_dir, "submit")
        #Create submit.tar.gz file inside the student directory
        shutil.make_archive(submit_path_name, "gztar", STUDENT_SUBMISSION_PATH, "labs")
        #Remove labs folder and zip file
        shutil.rmtree(os.path.join(STUDENT_SUBMISSION_PATH, "labs"))
        os.remove(zip_file)
