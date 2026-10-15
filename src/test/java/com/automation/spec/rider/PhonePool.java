package com.automation.spec.rider;

import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;

/**
 * Fresh phone numbers for sign-ups. Canada runs use fictional North American numbers (area code + 555-01NN, the range
 * reserved for fiction, so no real person is ever texted): 5 area codes x 100 = 500 numbers, handed out in turn from a
 * counter kept in the user's home directory so riders, drivers and the API suite (separate JVMs, possibly at the same
 * time) never get the same number twice. The counter starts at a random point so a rerun does not begin where the last
 * run began. India runs use a fresh 10-digit +91 mobile number.
 */
public final class PhonePool {

    private PhonePool() {}

    private static final String[] AREA_CODES = {"416", "647", "437", "905", "289"};
    private static final Path COUNTER = Path.of(System.getProperty("user.home"), ".hovr-canada-phone-seq");

    private static int nextSequence() {
        try (RandomAccessFile f = new RandomAccessFile(COUNTER.toFile(), "rw");
             FileChannel ch = f.getChannel();
             FileLock lock = ch.lock()) {
            int n;
            if (f.length() == 0) {
                n = (int) (System.nanoTime() % 500);
            } else {
                n = Integer.parseInt(f.readLine().trim());
            }
            f.setLength(0);
            f.writeBytes(String.valueOf((n + 1) % 500));
            return n;
        } catch (Exception e) {
            return (int) (System.nanoTime() % 500);
        }
    }

    /** A fresh Canadian national number, e.g. 4165550137 (without the +1). */
    public static String canadian() {
        int n = nextSequence();
        return AREA_CODES[(n / 100) % AREA_CODES.length] + "55501" + String.format("%02d", n % 100);
    }

    /** A fresh Indian national mobile number (without the +91). */
    public static String indian() {
        return "9" + String.format("%09d", System.currentTimeMillis() % 1_000_000_000L);
    }
}
