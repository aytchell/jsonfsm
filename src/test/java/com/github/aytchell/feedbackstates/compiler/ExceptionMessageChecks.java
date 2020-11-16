package com.github.aytchell.feedbackstates.compiler;

import com.github.aytchell.feedbackstates.StateMachineParser;
import com.github.aytchell.validator.exceptions.ValidationException;
import org.junit.jupiter.api.function.Executable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class ExceptionMessageChecks {
    public static void parseFileAssertThrowsAndMessageReadsLike(String filename, List<String> substrings) {
        final ValidationException e = assertThrows(ValidationException.class,
                () -> StateMachineParser.parseAndListRequiredDeviceIds(readResourceTextFile(filename)));
        messageReadsLike(e, substrings);
    }

    public static void assertThrowsAndMessageReadsLike(Executable executable, String substring) {
        assertThrowsAndMessageReadsLike(executable, List.of(substring));
    }

    public static void assertThrowsAndMessageReadsLike(Executable executable, List<String> substringList) {
        messageReadsLike(
                assertThrows(ValidationException.class, executable),
                substringList
        );
    }

    private static void messageReadsLike(ValidationException e, List<String> substringList) {
        final String message = e.getMessage();
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

    private static String readResourceTextFile(String filename) throws IOException {
        try (
                final InputStream input = ExceptionMessageChecks.class.getResourceAsStream(filename);
        ) {
            byte[] content = input.readAllBytes();
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
