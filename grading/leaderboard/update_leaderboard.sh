#!/bin/bash
cd "$(dirname "$0")"

set -e
python3 pull_git.py
rm -rf ../results
rm -rf ../distrib-results
python3 run_grader.py
rm -r results
mv ../results results
cd results
python3 ../../scripts/summarize.py
cd ..
python3 post_canvas.py
./archive.sh
