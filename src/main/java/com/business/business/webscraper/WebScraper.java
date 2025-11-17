package com.business.business.webscraper;

import com.github.lekan128.webscraper.WebSearchProcessor;
import com.github.lekan128.webscraper.text_extractor.ExtractedPageSummary;
import io.github.lekan128.aiagent.api.annotation.AiToolMethod;
import io.github.lekan128.aiagent.api.annotation.ArgDesc;

import java.io.IOException;
import java.util.List;

public class WebScraper {
    @AiToolMethod("Use to search the web for information")
    public List<ExtractedPageSummary> searchTheWeb(@ArgDesc("The search parameter") String searchWord){
        List<ExtractedPageSummary> search;
        try {
            search = WebSearchProcessor.search(searchWord);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return search;
    }
}
