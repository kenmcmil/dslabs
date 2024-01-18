#!/bin/bash
cd "$(dirname "$0")"

set -e
python3 pull_git.py
python3 run_grader.py
rm -r results
mv ../results results
python3 post_canvas.py