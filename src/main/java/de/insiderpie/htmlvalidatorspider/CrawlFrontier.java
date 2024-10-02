package de.insiderpie.htmlvalidatorspider;

import java.net.URI;
import java.util.HashSet;

/**
 * Implements an unordered list of URIs that keeps track of enqueued and
 * done URIs and makes sure the same URI is not returned twice by `next()`.
 */
public class CrawlFrontier {
    private final HashSet<URI> queued;
    private final HashSet<URI> done;

    public CrawlFrontier() {
        queued = new HashSet<>();
        done = new HashSet<>();
    }

    /**
     * Indicates if the queue is currently empty.
     */
    public boolean isEmpty() {
        return queued.isEmpty();
    }

    /**
     * Return an arbitrary URI from the queue. This method is guaranteed
     * to never return the same URI twice for the same UnorderedURIQueue instance.
     * @return The next URI, or null if the queue is empty.
     */
    public URI next() {
        URI uri = queued.iterator().next();
        queued.remove(uri);
        done.add(uri);
        return uri;
    }

    /**
     * Enqueue a new URI if it has not been processed already.
     * @return true if the URI was added to the queue. false if it
     * is either already in the queue or already done.
     */
    public boolean enqueue(URI uri) {
        if (done.contains(uri)) {
            return false;
        }
        return queued.add(uri);
    }
}
