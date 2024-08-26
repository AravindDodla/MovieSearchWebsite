package movies;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MovieServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String API_KEY = "589c688f1ec69463e97880f0caca2a22"; 
    private static final String TMDB_API_BASE_URL = "https://api.themoviedb.org/3/search/movie?api_key=" + API_KEY + "&query=";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Forward GET request to POST handler
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String query = request.getParameter("movie");

        // Validate the input
        if (query == null || query.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Movie query cannot be empty.");
            return;
        }

        // Fetch movie data
        String jsonResponse = fetchMovieData(query);

        // Set response content type
        response.setContentType("text/html;charset=UTF-8");

        // Generate HTML content
        StringBuilder htmlResponse = new StringBuilder();
        htmlResponse.append("<!DOCTYPE html>");
        htmlResponse.append("<html lang='en'>");
        htmlResponse.append("<head>");
        htmlResponse.append("<meta charset='UTF-8'>");
        htmlResponse.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        htmlResponse.append("<title>Movie Search Results</title>");
        htmlResponse.append("<style>");
        htmlResponse.append("body { font-family: Arial, sans-serif; background-color: #7C93C3; margin: 0; padding: 20px; }");
        htmlResponse.append("header { background-color: #021526; color: #fff; padding: 15px 30px; display: flex; align-items: center; justify-content: space-between; }");
        htmlResponse.append("header .nav-links { display: flex; gap: 20px; }");
        htmlResponse.append("header .nav-links a { color: #fff; text-decoration: none; font-weight: bold; padding: 10px 15px; border-radius: 5px; transition: background-color 0.3s, color 0.3s; }");
        htmlResponse.append("header .nav-links a:hover { background-color: #2a3b5f; color: #e0e0e0; }");
        htmlResponse.append("h1 { text-align: center; color: #333; margin-top: 20px; }");
        htmlResponse.append(".movie-container { display: flex; flex-wrap: wrap; justify-content: center; gap: 20px; }");
        htmlResponse.append(".movie-card { background-color: #fff; border-radius: 8px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1); padding: 15px; margin: 16px; width: 220px; text-align: center; transition: transform 0.2s, box-shadow 0.2s; }"); // Decreased width to 220px
        htmlResponse.append(".movie-card:hover { transform: translateY(-5px); box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15); }");
        htmlResponse.append(".movie-card img { max-width: 100%; height: auto; border-radius: 4px; margin-bottom: 10px; }");
        htmlResponse.append(".movie-title { font-size: 18px; font-weight: bold; margin: 8px 0; color: #333; }");
        htmlResponse.append(".movie-info { font-size: 14px; color: #666; margin-bottom: 5px; }");
        htmlResponse.append(".movie-overview { font-size: 13px; color: #777; text-align: left; margin-top: 10px; max-height: 80px; overflow-y: auto; }"); // Added overflow-y: auto
        htmlResponse.append("</style>");
        htmlResponse.append("</head>");
        htmlResponse.append("<body>");

        
        htmlResponse.append("<header>");
        htmlResponse.append("<div class='nav-links'>");
        htmlResponse.append("<a href='PopularMoviesServlet'>Popular Movies</a>");
        htmlResponse.append("<a href='PopularTvShowsServlet'>Popular TV Shows</a>");
        htmlResponse.append("<a href='LatestMovies'>Latest Movies</a>");
        htmlResponse.append("</div>");
        htmlResponse.append("</header>");

        htmlResponse.append("<h1>Movie Search Results</h1>");
        htmlResponse.append("<div class='movie-container'>");

        // Parse JSON response and generate movie cards
        if (jsonResponse != null && !jsonResponse.isEmpty()) {
            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
            JsonArray movies = jsonObject.getAsJsonArray("results");

            if (movies != null && movies.size() > 0) {
                for (JsonElement movieElement : movies) {
                    JsonObject movie = movieElement.getAsJsonObject();

                    String title = escapeHtml(getJsonString(movie, "title"));
                    String overview = escapeHtml(getJsonString(movie, "overview"));
                    String releaseDate = escapeHtml(getJsonString(movie, "release_date"));
                    double voteAverage = movie.has("vote_average") ? movie.get("vote_average").getAsDouble() : 0.0;
                    String posterPath = getJsonString(movie, "poster_path");
                    String imageUrl = posterPath.isEmpty() ? "https://via.placeholder.com/300x450?text=No+Image" : "https://image.tmdb.org/t/p/w300" + posterPath;

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
                htmlResponse.append("<p>No movies found for the search query.</p>");
            }
        } else {
            htmlResponse.append("<p>Unable to retrieve movie data. Please try again later.</p>");
        }

        htmlResponse.append("</div>"); // Close movie-container
        htmlResponse.append("</body>");
        htmlResponse.append("</html>");

        // Write HTML response to client
        response.getWriter().write(htmlResponse.toString());
    }

    private String fetchMovieData(String query) throws IOException {
        String encodedQuery = URLEncoder.encode(query, "UTF-8");
        String urlString = TMDB_API_BASE_URL + encodedQuery;
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            System.err.println("HTTP Response Code: " + responseCode);
            return null; // Handle the error gracefully by returning null
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
