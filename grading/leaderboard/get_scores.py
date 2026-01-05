from os import listdir
from os.path import join, isdir
import sys
from re import search

if len(sys.argv) != 2:
    print ('synposis: python get_scores.py date')
    exit(1)

date = sys.argv[1]
dir = join('archive',date,'results')

if not isdir(dir):
    print ('not  directory: {}'.format(dir))
    exit(1)

print ('getting scores from: {}'.format(dir))

aliases = []

with open('aliases.txt','r') as alf:
    for x in alf:
        aliases.append(x.split(' ')[0])

aliases = list(sorted(aliases))

to_avg = 3



for alias in aliases:
    std = join(dir,alias)
    if isdir(std):
        scores = []
        for log_path in [f for f in listdir(std) if f.startswith('test-results')]:
                with open(join(std, log_path), 'r') as fd:
                        try:
                                score_group = search('Points: (\d+)', fd.read())
                                scores += [float(score_group.group(1))]
                        except Exception as e:
                                pass
        if len(scores) > 0:
                print('%s %f' % (alias, sum(sorted(scores)[-to_avg:]) / min(to_avg, len(scores))))
        else:
            print (alias + ' 0')
    else:
        print(alias + ' ')
