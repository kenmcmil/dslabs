"""
load_config.py

Load config and secrets
"""
import json

def load_config():
    # Read configuration file
    with open("config.json", "r") as fd:
        config = json.loads(fd.read())
    
    with open(config["canvas_api_key_path"], "r") as fd:
        canvas_api_key = fd.read()
   
    alias_to_git = {} 
    with open(config["alias_file"], "r") as fd:
        for line in fd:
            alias, github_user = line.split(' ')
            alias_to_git[alias] = github_user

    return config, canvas_api_key, alias_to_git
        
    
