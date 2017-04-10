package Server.File;

import java.io.Serializable;

/**
 * Immutable file descriptor. A file descriptor contains a file name
 * and file length in bytes. Each file descriptor will have a unique
 * hash code based on the file's name and length. File descriptors are
 * serializable so they can be sent remotely.
 *
 * @author Xavier McNulty
 */
public class FileDescriptor implements Serializable {
    private final String FILE_NAME;
    private final int FILE_LEN, HASH_CODE;

    /**
     * Constructs a new file descriptor.
     * @param FILE_NAME Name of the file.
     * @param FILE_LEN Length of the file in bytes.
     */
    public FileDescriptor(String FILE_NAME, int FILE_LEN) {
        this.FILE_NAME = FILE_NAME;
        this.FILE_LEN = FILE_LEN;

        HASH_CODE = Integer.hashCode(FILE_NAME.hashCode() + FILE_LEN);
    }

    // Hash code.
    @Override
    public int hashCode() {
        return HASH_CODE;
    }

    // equals
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof FileDescriptor))
            return false;

        FileDescriptor fd = (FileDescriptor) obj;

        return fd.HASH_CODE == this.HASH_CODE;
    }

    // toString
    @Override
    public String toString() {
        return FILE_NAME + "\t" + FILE_LEN;
    }

    // Getters
    public String getFileName() {
        return FILE_NAME;
    }

    public int getFileLen() {
        return FILE_LEN;
    }
}
