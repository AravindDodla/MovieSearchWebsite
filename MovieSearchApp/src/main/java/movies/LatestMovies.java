package movies;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LatestMovies extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String API_KEY = "589c688f1ec69463e97880f0caca2a22"; 
    private static final String TMDB_API_BASE_URL = "https://api.themoviedb.org/3/movie/upcoming?api_key=" + API_KEY + "&language=en-US";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Fetch the latest movies data
        String jsonResponse = fetchLatestMoviesData();

        // Set response content type
        response.setContentType("text/html;charset=UTF-8");

        // Generate HTML content
        StringBuilder htmlResponse = new StringBuilder();
        htmlResponse.append("<!DOCTYPE html>");
        htmlResponse.append("<html lang='en'>");
        htmlResponse.append("<head>");
        htmlResponse.append("<meta charset='UTF-8'>");
        htmlResponse.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        htmlResponse.append("<title>Latest Movies</title>");
        htmlResponse.append("<style>");
        htmlResponse.append("body { font-family: Arial, sans-serif; background-color: #7C93C3; margin: 0; padding: 0; }");
        htmlResponse.append("header { background-color: #021526; color: #fff; padding: 10px 20px; display: flex; align-items: center; justify-content: space-between; }");
        htmlResponse.append("header .nav-links a { color: #fff; text-decoration: none; margin: 0 10px; }");
        htmlResponse.append("header .search-bar { margin-left: auto; }");
        htmlResponse.append("header .search-bar input[type='text'] { padding: 5px; border-radius: 4px; border: none; }");
        htmlResponse.append("header .search-bar button { padding: 5px 10px; border: none; background-color: #333; color: #fff; border-radius: 4px; cursor: pointer; }");
        htmlResponse.append(".title { text-align: center; font-size: 28px; font-weight: bold; color: #fff; padding: 20px 0; background-color: #4A6EA8; margin-top: 0; }");
        htmlResponse.append(".movie-container { display: flex; flex-wrap: wrap; justify-content: center; gap: 20px; padding: 20px; }");
        htmlResponse.append(".movie-card { background-color: #fff; border-radius: 8px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1); padding: 15px; margin: 16px; width: 220px; text-align: center; transition: transform 0.2s, box-shadow 0.2s; }"); // Decreased width to 220px
        htmlResponse.append(".movie-card:hover { transform: translateY(-5px); box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15); }");
        htmlResponse.append(".movie-card img { max-width: 100%; height: auto; border-radius: 4px; margin-bottom: 10px; }");
        htmlResponse.append(".movie-title { font-size: 18px; font-weight: bold; margin: 8px 0; color: #333; }");
        htmlResponse.append(".movie-info { font-size: 14px; color: #666; margin-bottom: 5px; }");
        htmlResponse.append(".movie-overview { font-size: 13px; color: #777; text-align: left; max-height: 80px; overflow-y: auto; margin-top: 10px; }"); // Added overflow-y: auto
        htmlResponse.append("</style>");
        htmlResponse.append("</head>");
        htmlResponse.append("<body>");
        htmlResponse.append("<header>");
        htmlResponse.append("<div class='nav-links'>");
        htmlResponse.append("<a href='PopularMoviesServlet'>Popular Movies</a>");
        htmlResponse.append("<a href='PopularTvShowsServlet'>Popular TV Shows</a>");
        htmlResponse.append("</div>");
        htmlResponse.append("<div class='search-bar'>");
        htmlResponse.append("<form action='MovieServlet' method='get'>");
        htmlResponse.append("<input type='text' name='movie' placeholder='Search movies...' />");
        htmlResponse.append("<button type='submit'>Search</button>");
        htmlResponse.append("</form>");
        htmlResponse.append("</div>");
        htmlResponse.append("</header>");
        htmlResponse.append("<div class='title'>Latest Movies</div>");
        htmlResponse.append("<div class='movie-container'>");

        // Parse JSON response and generate movie cards
        if (jsonResponse != null && !jsonResponse.isEmpty()) {
            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
            JsonArray movies = jsonObject.getAsJsonArray("results");
            if (movies != null && movies.size() > 0) {
                for (JsonElement element : movies) {
                    JsonObject movie = element.getAsJsonObject();
                    String title = escapeHtml(getJsonString(movie, "title"));
                    String overview = escapeHtml(getJsonString(movie, "overview"));
                    String releaseDate = escapeHtml(getJsonString(movie, "release_date"));
                    double voteAverage = movie.has("vote_average") ? movie.get("vote_average").getAsDouble() : 0.0;
                    String posterPath = getJsonString(movie, "poster_path");
                    String imageUrl = !posterPath.isEmpty() ? "https://image.tmdb.org/t/p/w300" + posterPath : "https://via.placeholder.com/300x450?text=No+Image";

                    // Add movie card HTML
                    htmlResponse.append("<div class='movie-card'>");
                    htmlResponse.append("<img src='").append(imageUrl).append("' alt='").append(title).append(" Poster'>");
                    htmlResponse.append("<div class='movie-title'>").append(title).append("</div>");
                    htmlResponse.append("<div class='movie-info'>Release Date: ").append(releaseDate).append("</div>");
                    htmlResponse.append("<div class='movie-info'>Rating: ").append(voteAverage).append("</div>");
                    htmlResponse.append("<div class='movie-overview'>").append(overview).append("</div>");
                    htmlResponse.append("</div>");
                }
            } else {
                htmlResponse.append("<p>No movies found.</p>");
            }
        } else {
            htmlResponse.append("<p>Failed to retrieve movie data.</p>");
        }

        htmlResponse.append("</div>"); 
        htmlResponse.append("</body>");
        htmlResponse.append("</html>");

        // Write HTML response to client
        response.getWriter().write(htmlResponse.toString());
    }

    private String fetchLatestMoviesData() throws IOException {
        URL url = new URL(TMDB_API_BASE_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Failed to fetch data. HTTP Status Code: " + responseCode);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder jsonResponse = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonResponse.append(line);
        }
        reader.close();

        return jsonResponse.toString();
    }

    // Helper method to safely get a String from a JsonObject
    private String getJsonString(JsonObject jsonObject, String memberName) {
        JsonElement jsonElement = jsonObject.get(memberName);
        return jsonElement != null && !jsonElement.isJsonNull() ? jsonElement.getAsString() : "";
    }

    // Helper method to escape HTML characters
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
