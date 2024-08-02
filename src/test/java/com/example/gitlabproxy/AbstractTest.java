package com.example.gitlabproxy;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;

public class AbstractTest {

    protected final SoftAssertions softly = new SoftAssertions();

    @AfterEach
    public void assertAll() {
        softly.assertAll();
    }

}
