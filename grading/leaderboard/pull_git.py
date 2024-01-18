"""
pull_git.py

Pulls student submissions from GitHub.
Clones repos if not present in config['submission_path'], otherwise just pulls master.
"""
import os
import subprocess
from load_config import load_config

REPO_NAME = "dslabs-371d"


def update_student_repos(submission_path, alias_to_git):
    if os.path.exists(submission_path):
        existing_repos = os.listdir(submission_path)
    else:
        os.mkdir(submission_path)
        existing_repos = []

    for alias, git_user in alias_to_git.items():
        git_user = git_user.strip()
        if alias not in existing_repos:
            # Need to git clone the student's repo
            GITHUB_URL = "git@github.com"
            full_git_url = f"{GITHUB_URL}:{git_user}/{REPO_NAME}.git"
            subprocess.run(
                ["git", "clone", full_git_url], cwd=submission_path, check=True
            )

            # Rename to the student's alias
            subprocess.run(["mv", REPO_NAME, alias], cwd=submission_path, check=True)

        student_path = os.path.join(submission_path, alias)
        # Use git fetch instead of pull since the grader will cause merge conflicts
        subprocess.run(["git", "fetch", "--all"], cwd=student_path, check=True)
        subprocess.run(
            ["git", "reset", "--hard", "origin/master"], cwd=student_path, check=True
        )


if __name__ == "__main__":
    config, _, alias_to_git = load_config()
    submission_path = config["submission_path"]

    update_student_repos(submission_path, alias_to_git)
