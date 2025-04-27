"""
post_canvas.py

Posts grader results to Canvas.
"""

import json
import requests
import datetime
import re
from copy import deepcopy
from load_config import load_config

config, CANVAS_API_KEY, _ = load_config()


API_ENDPOINT = "https://utexas.instructure.com/api/v1"
RUNS = 2


def extract_score(s):
    # Regular expression to match a percentage pattern, thanks ChatGPT
    pattern = r"\d+\.\d+%"

    match = re.search(pattern, s)

    if match:
        return float(match.group()[:-1])
    raise ValueError("could not find percentage expression")


def get_page(auth_token, course_id, page_id):
    """Returns Canvas page given course and page id"""
    endpoint = f"{API_ENDPOINT}/courses/{course_id}/pages/{page_id}"
    headers = {"Authorization": f"Bearer {auth_token}"}

    response = requests.get(endpoint, headers=headers)
    return response.json()


def put_page(auth_token, course_id, page_id, body):
    """Updates Canvas page given course id, page id, and HTML body"""
    endpoint = f"{API_ENDPOINT}/courses/{course_id}/pages/{page_id}"
    headers = {"Authorization": f"Bearer {auth_token}"}

    payload = {"wiki_page[body]": body}
    response = requests.put(endpoint, headers=headers, data=payload)
    print(response.status_code)
    return response.json()


def sanitize_results(results):
    """
    Sometimes the grader will fail, e.g. b/c a student submission
    doesn't compile.
    """

    FAILED_DICT = {
        "Tests passed": "FAILED TO RUN",
        "Points": "0/0 (0.00%)",
        "Total time": "0s",
    }
    for alias, result in results.items():
        for trial in range(RUNS):
            if str(trial) not in results[alias] or results[alias][str(trial)] == {}:
                results[alias][str(trial)] = deepcopy(FAILED_DICT)

    return results


def sort_results(results):
    """Sort results by descending average score"""

    results = sanitize_results(results)

    score_aliases = []
    for alias, result in results.items():
        print(alias, result)
        scores = [extract_score(result[str(run)]["Points"]) for run in range(RUNS)]
        score_aliases.append((sum(scores) / len(scores), alias))

    # Python 3.6+ dicts are ordered by insertion
    order = sorted(score_aliases, reverse=True)
    new_results = {}
    for _, alias in order:
        new_results[alias] = results[alias]

    return new_results


def update_body(old_body, results):
    """Prepend new results as HTML table and hide old contents in a dropdown"""
    # We collapse previous tables into a HTML dropdown using <details>
    # Each time the script runs it needs to find and move this tag.
    DETAIL_START = "<details> <summary> Previous results </summary>"
    DETAIL_END = "</details>"

    FIELDS = ["Tests passed", "Points", "Total time"]

    # Header with title for each column
    table_header = " ".join(
        ["<th> Alias </th>"]
        + [
            f"<th>Run {trial}: {field}</th>"
            for trial in range(RUNS)
            for field in FIELDS
        ]
    )

    # Generate rows of the table for each student
    table_contents = ""
    for alias, result in results.items():
        formatted_results = [
            f'<td style="border: 1px solid black; text-align: center;">{result[str(trial)][field]}</td>'
            for trial in range(RUNS)
            for field in FIELDS
        ]
        row = f"""
            <tr>
            <td style="border: 1px solid black; text-align: center;">{alias}</td>
            {' '.join(formatted_results)}
            """
        table_contents += row

    # The actual table
    new_body = f"""
    <table style="width: 100%; border: 1px solid black;">
        <thead>
            <tr>
            <th colspan="{1 + RUNS * len(FIELDS)}">{config['lab_name']} - Posted at {datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S %Z%z')}</th>
            </tr>
            <tr>
            {table_header}
            </tr>
        </thead>
        <tbody>
            {table_contents}
        </tbody>
    </table>
    <br>
    """

    # Roll up old stuff
    if old_body is None or old_body == "":
        old_tables = ""
    else:
        old_tables = old_body

        for word in DETAIL_START.split(" ") + [DETAIL_END]:
            old_tables = old_tables.replace(word, "")

    new_body += DETAIL_START
    new_body += old_tables
    new_body += DETAIL_END

    return new_body


def post_new_results():
    print("Pulling existing Canvas page...")
    old_page = get_page(
        CANVAS_API_KEY, config["canvas_course_id"], config["canvas_page_id"]
    )
    old_body = old_page["body"]

    print("Generating new HTML...")
    RESULT_PATH = "results/test-summary.txt"
    with open(RESULT_PATH, "r") as f:
        results = json.load(f)
    results = sort_results(results)

    new_body = update_body(old_body, results)

    print("Posting results to Canvas...")
    resp = put_page(
        CANVAS_API_KEY, config["canvas_course_id"], config["canvas_page_id"], new_body
    )

    print("Finished, response", resp)


if __name__ == "__main__":
    post_new_results()
