Run `update_leaderboard.sh` to pull student repos, grade, and post to Canvas.

Requires the `requests` library to interface with the Canvas API.

## Config and required files

The config.json is the same as the one in the parent directory with a few exceptions.

For privacy and security purposes this script's `config.json` should contain paths to two files on the local machine.

- `canvas_api_key_path` should be a path to a plaintext file containing just a Canvas API key
- `alias_file` defines the mapping between student aliases (publicly shown on leaderboard) and their GitHub usernames (private for anonymity). This should be a path to a file with the following format:
```
<alias1> <github username1>
<alias2> <github username2>
...
```

This script assumes that the current user has git authentication already set up, including access to all student repos.

## Known Issues
The grading script expects some additional directories to do some git diff so it prints some errors like

```
error: Could not access 'grading/handout/labs/lab0-pingpong/src'
```
This doesn't affect leaderboard correctness.