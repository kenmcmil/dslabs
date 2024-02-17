#!/usr/bin/env python3
# coding: utf-8

import json
import csv

with open('results/merged.json', encoding='utf-8') as inputfile:
    data = json.load(inputfile)

final_data = []
for student in data:
    student_data = {}
    student_data['name'] = student
    for run in data[student]:
        for key in data[student][run]:
            if(key == 'Points'):
                student_data[key+ ' ' + run] = data[student][run][key].split('/')[0]
            else:    
                student_data[key+ ' ' + run] = data[student][run][key]
    final_data.append(student_data)        

with open('results/grades.csv', 'w') as f:
    dw = csv.DictWriter(f, final_data[0].keys())
    dw.writeheader()
    for row in final_data:
        dw.writerow(row)
