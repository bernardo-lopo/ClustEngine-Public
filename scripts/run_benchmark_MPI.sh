#!/bin/bash

OUTPUT_FILE="times.txt"
NUM_RUNS=10

# Clear the previous file, if it exists
echo "Execution times (in seconds):" > "$OUTPUT_FILE"

for i in $(seq 1 $NUM_RUNS); do
    echo "Run $i..."
    
    # Runs the program, capturing the output
    output=$(mpirun --hostfile ./MPIThermions/hosts --oversubscribe -np 17 ./MPIThermions/thermionSD term1.dat outputMPI_term1.txt)

    # Show the output
    echo "$output"

    # Extract the time from the line starting with the string "Tempo total:"
    time=$(echo "$output" | grep "Tempo total:" | awk '{print $3}')

    # Save the time to the file
    echo "$time" >> "$OUTPUT_FILE"
done

echo "Times saved to $OUTPUT_FILE."