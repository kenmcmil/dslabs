"""
run_grader.py

Runs grader with appropriate flags from config
"""
from load_config import load_config
import subprocess

if __name__ == "__main__":
    config, _, _ = load_config()
    # e.g. python3 grader.py -s ~/distributed/test_submissions -n 0 -l lab0-pingpong
    subprocess.run(
        [
            "python3",
            "grader.py",
            "-s",
            config["submission_path"],
            "-n",
            config["lab_number"],
            "-l",
            config["lab_name"],
            "--no-tar",
        ],
        cwd="..",
        check=True,
    )
