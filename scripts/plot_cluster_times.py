import os
import json
import re
import numpy as np
import matplotlib.pyplot as plt
import pandas as pd

# Converts time string like "711s -> 11m 51s" into seconds
def parse_time(t: str) -> int:
    match = re.match(r"(\d+)s", t.strip())
    return int(match.group(1)) if match else 0

# Reads all generated Cluster_*.json files from the given folder
def load_cluster_data(folder: str):
    data = []
    for fname in os.listdir(folder):
        if fname.startswith("Cluster_") and fname.endswith(".json"):
            with open(os.path.join(folder, fname), "r") as f:
                raw = f.read().strip()

                # Fixes JSON format if needed
                if not raw.startswith("["):
                    raw = "[" + raw
                if not raw.endswith("]"):
                    raw = raw + "]"
                raw = raw.replace("}, {", "},\n{")

                try:
                    content = json.loads(raw)
                except json.JSONDecodeError as e:
                    print(f"JSON error in {fname}: {e}")
                    continue

                for entry in content:
                    total_time = parse_time(entry["total_time"])
                    n_instances = entry["number_of_instances"]

                    # pmachine times
                    pmachine = {
                        f"pm_{k}": parse_time(v)
                        for k, v in entry["partial_times"]["pmachine"].items()
                    }

                    # npmachines times average over all npmachines
                    npmachines = entry["partial_times"]["npmachines"]
                    np_data = {}
                    if npmachines:
                        keys = npmachines[0].keys()
                        for k in keys:
                            np_data[f"np_{k}"] = np.mean([parse_time(m[k]) for m in npmachines])

                    row = {
                        "cluster": fname,
                        "instances": n_instances,
                        "total_time": total_time
                    }
                    row.update(pmachine)
                    row.update(np_data)
                    data.append(row)

    return pd.DataFrame(data)

def analyze_and_plot(df: pd.DataFrame, outdir="results_analysis"):
    os.makedirs(outdir, exist_ok=True)

    # Keeps only the numeric columns
    numeric_df = df.select_dtypes(include=[np.number])
    # Group the numeric data by the number of instances ('instances')
    # and calculate the following statistics, using an aggregated function for each numeric column:
    #   - mean (average value)
    #   - var  (variance, measure of spread)
    #   - std  (standard deviation, square root of variance)
    stats = (numeric_df.groupby(df["instances"])
             .agg(["mean", "var", "std"]))

    if ("instances", "mean") in stats.columns:
        stats = stats.drop(columns="instances")

    stats_json = os.path.join(outdir, "statistics.json")

    # Converts the stats DataFrame into JSON:
    with open(stats_json, "w") as f:
        json.dump(json.loads(stats.to_json(orient="index")), f, indent=4)

    print("Stats saved to:", stats_json)

    # Gets the time columns
    time_cols = [c for c in numeric_df.columns if c != "instances"]

    for col in time_cols:
        if col == "total_time":
            folder = os.path.join(outdir, "total_time")
        elif col.startswith("pm_"):
            folder = os.path.join(outdir, "pmachine", col.replace("pm_", ""))
        elif col.startswith("np_"):
            folder = os.path.join(outdir, "npmachines", col.replace("np_", ""))
        else:
            folder = os.path.join(outdir, "others")

        os.makedirs(folder, exist_ok=True)

        # Boxplot
        plt.figure(figsize=(8, 5))
        df.boxplot(column=col, by="instances")
        plt.title(f"{col} by number of machines")
        plt.xlabel("Number of machines")
        plt.ylabel("Time (s)")
        plt.suptitle("")
        plt.savefig(os.path.join(folder, f"boxplot_{col}.png"))
        plt.close()

        # Mean line plot
        plt.figure(figsize=(8, 5))
        df.groupby("instances")[col].mean(numeric_only=True).plot(marker="o")
        plt.title(f"Mean {col} vs number of machines")
        plt.xlabel("Number of machines")
        plt.ylabel("Mean time (s)")
        plt.grid(True)
        plt.savefig(os.path.join(folder, f"mean_{col}.png"))
        plt.close()

    print(f"Plots saved into organized subfolders inside '{outdir}'")

if __name__ == "__main__":
    folder = r"/home/bernardo/Desktop/TestesAWS"
    df = load_cluster_data(folder)
    analyze_and_plot(df)
    print("Analysis finished!")
