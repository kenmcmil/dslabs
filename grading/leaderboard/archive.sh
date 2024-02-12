#!/bin/bash
# Moves results and failures into an archival directory
# ChatGPT generated

# Define the archive and target directories
archive_dir="archive"
target_dir="${archive_dir}/$(date +%m-%d-%Y)"

# Create the archive directory if it doesn't exist
if [ ! -d "$archive_dir" ]; then
    mkdir "$archive_dir"
fi

# Create the target directory
mkdir -p "$target_dir"

# Move the 'results' directory and 'failed_git_pull.txt' file
if [ -d "results" ] || [ -f "failed_git_pull.txt" ]; then
    cp -r results "$target_dir"
    cp failed_git_pull.txt "$target_dir"
else
    echo "Either 'results' directory or 'failed_git_pull.txt' file does not exist."
fi
