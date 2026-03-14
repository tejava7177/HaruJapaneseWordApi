package com.haru.api.user.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RandomBuddyCodeGeneratorTest {

    private final RandomBuddyCodeGenerator generator = new RandomBuddyCodeGenerator();

    @Test
    void generate_returnsEightCharacterCodeWithoutAmbiguousCharacters() {
        String code = generator.generate();

        assertThat(code).hasSize(8);
        assertThat(code).matches("[A-HJ-NP-Z2-9]{8}");
    }
}
