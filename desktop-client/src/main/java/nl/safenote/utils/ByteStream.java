package nl.safenote.utils;

import java.util.Arrays;

/**
 * Utility class for reading and writing fragments from/to an underlying byte[], called source,
 * Provides an alternative to repeated arraycopies and and ByteArrayOutputStream.
 * Simple API reduces the chance of mistakes.
 *
 * ByteStream has 2 modes (and separate constructors for both modes):
 *
 *     Read - allows easy sequential access to the source.
 *
 *     Write - works exactly like a ByteArrayOutputStream, save for the fact that the capacity (source length)
 *     cannot be expanded. In write mode, the source cannot be retrieved without writing until full capacity is reached
 */
public class ByteStream {

    private final byte[] source;
    int readIndex;
    int writeIndex;

    /**
     * Initialize read mode
     * @param source
     */
    public ByteStream(byte[] source) {
        if(source == null)
            throw new NullPointerException();
        this.source = source;
        this.writeIndex = source.length;
    }

    /**
     * Returns an array of requested length containg next available bytes from the source
     * cannot be used in write mode until source is full
     * @param len
     * @return fragment
     */
    public byte[] read(int len) {
        if(readIndex == source.length)
            throw new IllegalArgumentException("Source is depleted.");
        if(len < 0 || (len + readIndex > source.length))
            throw new IllegalArgumentException(source.length - readIndex + " bytes remaining in source");
        if(writeIndex!=source.length)
            throw new IllegalArgumentException("Source is not full yet");
        return Arrays.copyOfRange(source, readIndex, readIndex += len);
    }

    /**
     * Returns all remaining bytes in source
     * Preferred method to retrieve source in write mode
     * cannot be used in write mode until source is full
     * @return remaining bytes in source
     */
    public byte[] read() {
        if(source.length == readIndex)
            throw new IllegalArgumentException("Source is depleted");
        if(writeIndex!=source.length)
            throw new IllegalArgumentException("Source is not full yet");
        if(readIndex==0)
            return source;
        else
            return read(source.length - readIndex);
    }


    /**
     * Initialize write mode with lengths for all fragments specified individually, or one int for total capacity
     * @param capacity
     */
    public ByteStream(int... capacity){
        this.source = new byte[Arrays.stream(capacity).sum()];
    }

    /**
     * write bytes to source, returns self to allow for method chaining.
     * @param bytes
     * @return self
     */
    public ByteStream write(byte[] bytes){
        if(writeIndex+bytes.length>source.length)
            throw new ArrayIndexOutOfBoundsException(source.length - writeIndex + " empty bytes remaining");
        System.arraycopy(bytes, 0, source, writeIndex, bytes.length);
        writeIndex += bytes.length;
        return this;
    }
}