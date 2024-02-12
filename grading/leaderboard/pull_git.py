"""
pull_git.py

Pulls student submissions from GitHub.
Clones repos if not present in config['submission_path'], otherwise just pulls master.
"""

import os
import subprocess
from load_config import load_config
import pathlib

REPO_NAME = "dslabs-371d"
FAILED_PATH = "failed_git_pull.txt"


def update_student_repos(submission_path, alias_to_git):
    if os.path.exists(submission_path):
        existing_repos = os.listdir(submission_path)
    else:
        os.mkdir(submission_path)
        existing_repos = []

    pathlib.Path(FAILED_PATH).unlink(missing_ok=True)

    for alias, git_user in alias_to_git.items():
        try:
            print("Starting", alias, " github username:", git_user)
            git_user = git_user.strip()
            if alias not in existing_repos:
                # Need to git clone the student's repo
                GITHUB_URL = "git@github.com"
                full_git_url = f"{GITHUB_URL}:{git_user}/{REPO_NAME}.git"

                # Rename to the student's alias
                subprocess.run(
                    ["git", "clone", full_git_url, alias],
                    cwd=submission_path,
                    check=True,
                )

            student_path = os.path.join(submission_path, alias)
            # Use git fetch instead of pull since the grader will cause merge conflicts
            subprocess.run(["git", "fetch", "--all"], cwd=student_path, check=True)
            try:
                subprocess.run(
                    ["git", "reset", "--hard", "origin/master"],
                    cwd=student_path,
                    check=True,
                )
            except Exception as e:
                # Hacky fix but some students may name the primary branch main
                subprocess.run(
                    ["git", "reset", "--hard", "origin/main"],
                    cwd=student_path,
                    check=True,
                )

        except Exception as e:
            with open(FAILED_PATH, "a") as f:
                f.write(f"{alias} {git_user} ")
                f.write(str(e) + "\n")

            subprocess.run(["rm", "-rf", alias], cwd=submission_path, check=True)


if __name__ == "__main__":
    config, _, alias_to_git = load_config()
    submission_path = config["submission_path"]

    update_student_repos(submission_path, alias_to_git)
