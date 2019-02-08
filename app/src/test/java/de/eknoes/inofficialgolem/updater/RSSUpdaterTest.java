package de.eknoes.inofficialgolem.updater;

import com.android.volley.toolbox.StringRequest;
import org.junit.Test;

import static org.junit.Assert.*;

public class RSSUpdaterTest {

    @Test
    public void getItems() {
        new GolemFeedParser().parse(new StringRequest())
    }
}