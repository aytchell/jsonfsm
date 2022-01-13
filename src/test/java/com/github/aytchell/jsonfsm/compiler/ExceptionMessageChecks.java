package com.github.aytchell.jsonfsm.compiler;

import com.github.aytchell.jsonfsm.StateMachineParser;
import com.github.aytchell.validator.exceptions.ValidationException;
import org.junit.jupiter.api.function.Executable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ExceptionMessageChecks {
    public static void parseFileAssertThrowsAndMessageReadsLike(String filename, List<String> substrings) {
        final ValidationException e = assertThrows(ValidationException.class,
                () -> StateMachineParser.parseAndListRequiredDeviceIds(readResourceTextFile(filename)));
        assertMessageReadsLike(e, substrings);
    }

    public static void assertThrowsAndMessageReadsLike(Executable executable, String substring) {
        assertThrowsAndMessageReadsLike(executable, List.of(substring));
    }

    public static void assertThrowsAndMessageReadsLike(Executable executable, List<String> substringList) {
        assertMessageReadsLike(
                assertThrows(ValidationException.class, executable),
                substringList
        );
    }

    private static void assertMessageReadsLike(ValidationException e, List<String> substringList) {
        final String message = e.getMessage();
        assertMessageReadsLike(message, substringList);
    }

    public static void assertMessageReadsLike(String message, List<String> substringList) {
        final IndexKeeper startIndex = new IndexKeeper();

        substringList.forEach(
                substring -> {
                    final int newIndex = message.indexOf(substring, startIndex.get());
                    if (newIndex == -1) {
                        if (message.contains(substring)) {
                            fail(String.format("'%s' is contained in '%s' but at the wrong position",
                                    substring, message));
                        } else {
                            fail(String.format("Expected '%s' to be contained in '%s'", substring, message));
                        }
                    }
                    startIndex.set(newIndex + 1);
                }
        );
    }

    static String readResourceTextFile(String filename) throws IOException {
        try (
                final InputStream input = ExceptionMessageChecks.class.getResourceAsStream(filename)
        ) {
            assertNotNull(input);
            byte[] content = input.readAllBytes();
            assertNotNull(content);
            return new String(content, StandardCharsets.UTF_8);
        }
    }

    private static class IndexKeeper {
        private int index = 0;

        int get() {
            return index;
        }

        void set(int newIndex) {
            this.index = newIndex;
        }
    }
}
