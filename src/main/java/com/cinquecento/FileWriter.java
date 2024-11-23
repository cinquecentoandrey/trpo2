package com.cinquecento;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * A class for writing strings to a file using buffering.
 * Writes data to a file in chunks to minimize the number of write operations.
 * When the buffer reaches a specified size, the data is written to the file.
 * At the end of the process, any remaining data in the buffer is written to the file.
 */
public class FileWriter {

    /**
     * A list of strings that will be stored in the buffer until a specified size is reached.
     * When this size is exceeded, the data is written to the file.
     */
    private final List<String> buffer;

    /**
     * A temporary buffer that holds all the strings from the main buffer before writing them to the file.
     */
    private final List<String> tmpBuffer;

    /**
     * The name of the file where the data will be written.
     */
    private final String filename;

    /**
     * The maximum capacity of the buffer after which the data will be written to the file.
     */
    private final Long bufferCapacity;

    /**
     * Constructor to create an instance of FileWriter with a specified file name and buffer capacity.
     * Initializes internal buffers to store data.
     *
     * @param filename       the name of the file where the strings will be written
     * @param bufferCapacity the size of the buffer
     */
    public FileWriter(String filename, Long bufferCapacity) {
        this.filename = filename;
        this.bufferCapacity = bufferCapacity;
        buffer = new ArrayList<>();
        tmpBuffer = new ArrayList<>();
    }

    /**
     * Adds a string to the buffer for later writing to the file.
     * If the buffer size exceeds the specified capacity, the data is written to the file.
     *
     * @param stringToAppend the string to be added to the buffer
     */
    public void append(String stringToAppend) {
        if (buffer.size() <= bufferCapacity) {
            buffer.add(stringToAppend);
        } else {
            printToFile();
        }
    }

    /**
     * Writes the contents of the buffer to the file and clears the buffer.
     * The data is written line by line.
     */
    private void printToFile() {
        tmpBuffer.addAll(buffer);
        buffer.clear();
        try (PrintWriter out = new PrintWriter(new java.io.FileWriter(filename, true))) {
            for (String str : tmpBuffer) {
                out.println(str);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error writing to file", e);
        }
        tmpBuffer.clear();
    }

    /**
     * Closes the file writing process and writes any remaining data in the buffer to the file.
     * This method ensures that no data is left unwritten before completing the process.
     */
    public void close() {
        if (!buffer.isEmpty()) {
            printToFile();
        }
    }
}