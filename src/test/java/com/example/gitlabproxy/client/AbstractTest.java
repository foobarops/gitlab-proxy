package com.example.gitlabproxy.client;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;

public class AbstractTest {

    protected final SoftAssertions softly = new SoftAssertions();

    @AfterEach
    void tearDown() {
        softly.assertAll();
    }

}
