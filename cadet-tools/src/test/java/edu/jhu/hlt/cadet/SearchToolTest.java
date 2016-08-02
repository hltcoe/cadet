package edu.jhu.hlt.cadet;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import edu.jhu.hlt.cadet.SearchTool.Output;

public class SearchToolTest {

    @Test
    public void testGetQuestions() {
        String q1 = "?:What is blue? ?:What is green?";
        try (SearchTool tool = new SearchTool();) {
            Output output = tool.getQuestions(q1);
            assertEquals("What is blue?", output.items.get(0));
            assertEquals("What is green?", output.items.get(1));
            assertEquals("", output.query);

            String q2 = "colors ?:\"What is red?\" ?:What is green?";
            output = tool.getQuestions(q2);
            assertEquals("What is red?", output.items.get(0));
            assertEquals("What is green?", output.items.get(1));
            assertEquals("colors", output.query);

            String q3 = "colors ?:\"What is red\" ?:\"What is green?\" test";
            output = tool.getQuestions(q3);
            assertEquals("What is red", output.items.get(0));
            assertEquals("What is green?", output.items.get(1));
            assertEquals("colors   test", output.query);
        }
    }

    @Test
    public void testGetTerms() {
        String q1 = "red blue green";
        try(SearchTool tool = new SearchTool();) {
            List<String> terms = tool.getTerms(q1);
            assertEquals("red", terms.get(0));
            assertEquals("blue", terms.get(1));
            assertEquals("green", terms.get(2));

            String q2 = "yellow \"red blue\" green";
            terms = tool.getTerms(q2);
            assertEquals("yellow", terms.get(0));
            assertEquals("red blue", terms.get(1));
            assertEquals("green", terms.get(2));

            String q3 = "电脑坏了 他的大脑仍然很活跃";
            terms = tool.getTerms(q3);
            assertEquals("电脑坏了", terms.get(0));
            assertEquals("他的大脑仍然很活跃", terms.get(1));

            String q4 = "\"" + "السلام عليكم" + "\"" +  "بالتوفيق";
            terms = tool.getTerms(q4);
            assertEquals(2, terms.size());
            assertEquals("السلام عليكم", terms.get(0));
            assertEquals("بالتوفيق", terms.get(1));
        }
    }
}
