package no.cantara.jau.eventextraction;

import no.cantara.cs.client.EventExtractionUtil;
import no.cantara.cs.dto.event.Event;
import no.cantara.cs.dto.event.EventExtractionConfig;
import no.cantara.cs.dto.event.EventExtractionTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class EventExtractorService {
    private static final Logger log = LoggerFactory.getLogger(EventExtractorService.class);
    private final EventRepo repo;
    private final String startPattern;
    private List<EventExtractor> eventExtractors = Collections.emptyList();
    private final ExecutorService executor;

    public EventExtractorService(EventRepo repo, String startPattern) {
        this.repo = repo;
        this.startPattern = startPattern;
        this.executor = Executors.newCachedThreadPool();
    }

    public void updateConfigs(List<EventExtractionConfig> configs) {
        createEventExtractors(configs);
    }

    private List<Future<String>> runEventExtractors() {
        try {
            return executor.invokeAll(eventExtractors);
        } catch (InterruptedException e) {
            log.error("Execution of EventExtractor was interrupted!", e);
        }
        return null;
    }

    private void createEventExtractors(List<EventExtractionConfig> configs) {
        log.info(configs.toString());
        eventExtractors = new ArrayList<>();
        for (EventExtractionConfig config : configs) {
            Map<String, List<EventExtractionTag>> tagsByFile = EventExtractionUtil
                    .groupExtractionConfigsByFile(config);

            for (String filePath : tagsByFile.keySet()) {
                List<EventExtractionTag> eventExtractionTags = tagsByFile.get(filePath);
                EventExtractor extractor = new EventExtractor(repo, eventExtractionTags, filePath, config.groupName, startPattern);
                eventExtractors.add(extractor);
            }
        }
        log.debug("Created {} EventExtractors", eventExtractors.size());
    }

    public List<Event> extractEvents() {
        log.debug("Extracting events.");
        runEventExtractors();
        return repo.getEvents();
    }

    public void clearRepo() {
        repo.clearEvents();
    }
}
