
SCORES = {}
TIMES_TO_RUN=2
SEARCH_TERMS = ['Tests passed', 'Points', 'Total time']

# Calculate the score for this student        
from os import listdir
from os.path import join, isdir
from re import search
import json

for student in [d for d in listdir('.') if isdir(d)]:
    SCORES[student] = {}
    for run_index in range(TIMES_TO_RUN):

        # Add to the summary
        log_out_path = join(student, 'test-results-' + str(run_index) + '.txt')
        with open(log_out_path, 'r') as out:
            test_results = out.read()
            SCORES[student][run_index] = {}
            for line in test_results.split('\n'):
                for term in SEARCH_TERMS:
                    if term in line:
                        SCORES[student][run_index][term] = line.split(':')[1].strip()

with open('test-summary.txt', 'w+') as out:
    json.dump(SCORES, out)
