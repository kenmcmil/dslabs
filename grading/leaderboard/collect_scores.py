"""
Utility script to get scores in an easily-copyable format for export into excel.
"""
import argparse
from post_canvas import sanitize_results, extract_score, RUNS
import os
import json

SUMMARY_FILE = 'test-summary.txt'

def sort_scores_by_alias(results, sort_order):
    results = sanitize_results(results)

    scores = []
    for alias in sort_order:
        if alias not in results:
            print(f"Alias {alias} not found in results")
            scores.append(alias)
        else:
            score = [int(results[alias][str(run)]["Points"].split('/')[0]) for run in range(RUNS)] 
            scores.append(alias + ' ' + str(sum(score) / len(score)))
     
    return scores


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Collect scores from the leaderboard archive')
    parser.add_argument('--result_dir', type=str, help='Path to the results directory')
    
    args = parser.parse_args()

    summary_path = os.path.join(args.result_dir, SUMMARY_FILE)
    with open(summary_path, 'r') as f:
        results = json.load(f)
        
    with open('aliases.txt', 'r') as f:
        order = sorted([line.split(' ')[0] for line in f.read().splitlines()])
   
    scores = sort_scores_by_alias(results, order)
    
    for score in scores:
        print(score)
        
    
