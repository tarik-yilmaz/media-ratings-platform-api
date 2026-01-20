package at.technikum.mrp.service;

import at.technikum.mrp.model.Media;
import at.technikum.mrp.repository.MediaRepository;
import at.technikum.mrp.repository.RatingRepository;

import java.util.*;

/**
 * Einfache Recommendation-Logik:
 * - Nimmt alle Medien, die der User >= 4 Sterne bewertet hat
 * - Sucht Medien, die der User noch nicht bewertet hat
 * - Vergibt Score basierend auf Genre/Typ/Altersfreigabe
 */
public class RecommendationService {

    private final MediaRepository mediaRepository;
    private final RatingRepository ratingRepository;

    public RecommendationService(MediaRepository mediaRepository, RatingRepository ratingRepository) {
        this.mediaRepository = mediaRepository;
        this.ratingRepository = ratingRepository;
    }

    public List<Map<String, Object>> recommendForUser(int userId, int limit) {
        int MIN_STARS = 4;

        List<Media> liked = mediaRepository.findHighlyRatedByUser(userId, MIN_STARS);
        List<Media> candidates = mediaRepository.findNotRatedByUser(userId);

        // Wenn der User noch nichts (gut) bewertet hat -> Fallback: Top Rated
        if (liked.isEmpty()) {
            List<Media> top = mediaRepository.findTopRated(limit);
            List<Map<String, Object>> out = new ArrayList<>();
            for (Media m : top) {
                out.add(mediaToJsonWithScore(m, 0, "fallback_topRated"));
            }
            return out;
        }

        String favoriteGenre = findFavoriteGenre(liked);

        List<ScoredMedia> scored = new ArrayList<>();
        for (Media c : candidates) {
            int score = 0;

            // Genre Bonus (Lieblingsgenre)
            if (favoriteGenre != null && hasGenre(c, favoriteGenre)) {
                score += 3;
            }

            // Content Similarity: best match zu einem der liked Media
            int bestSim = 0;
            for (Media l : liked) {
                int sim = similarity(c, l);
                if (sim > bestSim) bestSim = sim;
            }
            score += bestSim;

            scored.add(new ScoredMedia(c, score));
        }

        scored.sort((a, b) -> {
            // Score DESC
            int cmp = Integer.compare(b.score, a.score);
            if (cmp != 0) return cmp;

            // averageScore DESC
            cmp = Double.compare(
                    b.media.getAverageScore() == null ? 0.0 : b.media.getAverageScore(),
                    a.media.getAverageScore() == null ? 0.0 : a.media.getAverageScore()
            );
            if (cmp != 0) return cmp;

            // title ASC
            String ta = a.media.getTitle() == null ? "" : a.media.getTitle();
            String tb = b.media.getTitle() == null ? "" : b.media.getTitle();
            return ta.compareToIgnoreCase(tb);
        });

        List<Map<String, Object>> out = new ArrayList<>();
        for (int i = 0; i < scored.size() && out.size() < limit; i++) {
            ScoredMedia sm = scored.get(i);
            out.add(mediaToJsonWithScore(sm.media, sm.score, "favoriteGenre=" + favoriteGenre));
        }

        return out;
    }

    private int similarity(Media a, Media b) {
        int score = 0;

        // gleicher Typ -> +2
        if (a.getType() != null && b.getType() != null && a.getType().equalsIgnoreCase(b.getType())) {
            score += 2;
        }

        // gleiche Altersfreigabe -> +1
        if (a.getAgeRestriction() != null && b.getAgeRestriction() != null &&
                a.getAgeRestriction().equals(b.getAgeRestriction())) {
            score += 1;
        }

        // Genre-Overlap -> +1 pro Übereinstimmung
        Set<String> ga = new HashSet<>(safeGenres(a));
        Set<String> gb = new HashSet<>(safeGenres(b));
        ga.retainAll(gb);
        score += ga.size();

        return score;
    }

    private String findFavoriteGenre(List<Media> liked) {
        Map<String, Integer> count = new HashMap<>();

        for (Media m : liked) {
            for (String g : safeGenres(m)) {
                String key = g.trim().toLowerCase();
                if (key.isEmpty()) continue;
                count.put(key, count.getOrDefault(key, 0) + 1);
            }
        }

        String best = null;
        int bestCount = 0;
        for (Map.Entry<String, Integer> e : count.entrySet()) {
            if (e.getValue() > bestCount) {
                bestCount = e.getValue();
                best = e.getKey();
            }
        }
        return best;
    }

    private boolean hasGenre(Media m, String genreLowerCase) {
        if (genreLowerCase == null) return false;
        for (String g : safeGenres(m)) {
            if (g.trim().equalsIgnoreCase(genreLowerCase)) return true;
        }
        return false;
    }

    private List<String> safeGenres(Media m) {
        List<String> g = m.getGenres();
        if (g == null) return List.of();
        return g;
    }

    private Map<String, Object> mediaToJsonWithScore(Media m, int score, String reason) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", m.getId());
        out.put("title", m.getTitle());
        out.put("description", m.getDescription());
        out.put("mediaType", m.getType());
        out.put("releaseYear", m.getReleaseYear());
        out.put("genres", m.getGenres());
        out.put("ageRestriction", m.getAgeRestriction());
        out.put("creatorId", m.getCreatorId());
        out.put("averageScore", m.getAverageScore());
        out.put("createdAt", m.getCreatedAt());
        out.put("recommendationScore", score);
        out.put("reason", reason);
        return out;
    }

    private static class ScoredMedia {
        final Media media;
        final int score;
        ScoredMedia(Media media, int score) {
            this.media = media;
            this.score = score;
        }
    }
}
