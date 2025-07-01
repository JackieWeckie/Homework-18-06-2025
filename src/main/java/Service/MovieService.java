package Service;


import Config.Config;
import Model.MovieApiResponse;
import Model.Search;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieService {
    private final Config config;

    @Value("${baseUrl}")
    private String baseUrl;

    @Value("${apiKey}")
    private String apiKey;

    public ResponseEntity<MovieApiResponse> getMovies(String title, int size, int page) {
        String url = baseUrl + "/?s=" + title + "&apiKey=" + apiKey + "&page=" + page;
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<MovieApiResponse> firstResponse = restTemplate.getForEntity(url, MovieApiResponse.class);

        MovieApiResponse firstResult = firstResponse.getBody();
        int totalResults = Integer.parseInt(firstResult.getResultCount());
        int totalPages = (int) Math.ceil((double) totalResults / size);

        int initialIndex = (page - 1) * size + 1;
        int finalIndex = Math.min(initialIndex + size - 1, totalResults);

        int initialOmdbPage = (int) Math.ceil(initialIndex / 10.00);
        int finalOmdbPage = (int) Math.ceil(finalIndex / 10.00);

        List<Search> allMovies = new ArrayList<>();

        for (int i = initialOmdbPage; i <= finalOmdbPage; i++) {
            ResponseEntity<MovieApiResponse> response = restTemplate.getForEntity(url + i, MovieApiResponse.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                allMovies.addAll(response.getBody().getSearch());
            }
        }

        int offsetInList = (initialIndex - 1) % 10 + (initialIndex - 1) * 10;
        List<Search> selectedMovies = allMovies.stream()
                .skip(offsetInList)
                .limit(size)
                .toList();

        MovieApiResponse result = new MovieApiResponse();
        result.setSearch(new ArrayList<>(selectedMovies));
        result.setResultCount(String.valueOf(totalResults));
        result.setTotalPages(totalPages);
        result.setCurrentPage(page);
        result.setOwner("Nikita Morozov");
        result.setGroup("JAVA411");

        return ResponseEntity.ok(result);
    }
}
