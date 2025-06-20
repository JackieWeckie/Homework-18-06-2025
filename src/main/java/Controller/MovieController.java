package Controller;


import Model.Search;
import Model.MovieApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("api/movie")
public class MovieController {
    private final String apiKey = "266b43cd";
    private final String baseUrl = "https://www.omdbapi.com";

    @GetMapping
    public ResponseEntity<?> getMovies(
            @RequestParam String title,
            @RequestParam int size,
            @RequestParam int page
    ) {
        String url = baseUrl + "/?s=" + title + "&apiKey=" + apiKey + "&page=";
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<MovieApiResponse> firstResponse = restTemplate.getForEntity(url + 1, MovieApiResponse.class);
        if (!firstResponse.getStatusCode().is2xxSuccessful() || firstResponse.getBody() == null) {
            return ResponseEntity.status(404).body("Error: Movies not found");
        }

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
