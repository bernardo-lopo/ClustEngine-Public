#!/bin/bash

OUTPUT_FILE="times-exp.txt"
NUM_RUNS=10

# Clear the previous file, if it exists
echo "Execution times (Tempo MPI em segundos):" > "$OUTPUT_FILE"

for i in $(seq 1 $NUM_RUNS); do
    echo "Run $i..."

    # Runs the program, capturing the output
    ./run_thermions_aws.sh

    time=$(grep "Tempo MPI:" global_output.txt | awk '{print $3}')

    echo "Tempo MPI run $i: $time"

    echo "$time" >> "$OUTPUT_FILE"
done

echo "Times saved to $OUTPUT_FILE."
