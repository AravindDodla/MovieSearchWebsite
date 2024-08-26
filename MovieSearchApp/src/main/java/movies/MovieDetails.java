package movies;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class MovieDetails extends HttpServlet {
    private static final String TMDB_API_KEY = "589c688f1ec69463e97880f0caca2a22";

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("Servlet is called");
        String movieId = request.getParameter("id");
        System.out.println(movieId);
        if (movieId == null || movieId.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Movie ID is missing");
            return;
        }

        String apiUrl = "https://api.themoviedb.org/3/movie/" + movieId + "?api_key=" + TMDB_API_KEY;

        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        System.out.println(apiUrl);
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            // Parse JSON response
            Gson gson = new Gson();
            JsonObject movie = gson.fromJson(content.toString(), JsonObject.class);

            // Start building the HTML response
            StringBuilder htmlResponse = new StringBuilder();
            htmlResponse.append("<!DOCTYPE html>");
            htmlResponse.append("<html lang='en'>");
            htmlResponse.append("<head>");
            htmlResponse.append("<meta charset='UTF-8'>");
            htmlResponse.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            htmlResponse.append("<title>").append(escapeHtml(getJsonString(movie, "title"))).append("</title>");
            htmlResponse.append("<style>");
            htmlResponse.append("body { font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #e0f7fa; }"); // Light cyan background
            htmlResponse.append(".container { max-width: 1000px; margin: 20px auto; padding: 20px; background: #ffffff; border-radius: 8px; box-shadow: 0 0 15px rgba(0, 0, 0, 0.2); display: flex; align-items: flex-start; }");
            htmlResponse.append(".movie-image { flex: 0 0 300px; margin-right: 20px; }");
            htmlResponse.append(".movie-image img { width: 100%; border-radius: 8px; }");
            htmlResponse.append(".movie-details { flex: 1; }");
            htmlResponse.append("h1 { color: #00796b; }"); // Teal color for title
            htmlResponse.append("p { line-height: 1.6; color: #333; }");
            htmlResponse.append("</style>");
            htmlResponse.append("</head>");
            htmlResponse.append("<body>");
            htmlResponse.append("<div class='container'>");

            String title = escapeHtml(getJsonString(movie, "title"));
            String overview = escapeHtml(getJsonString(movie, "overview"));
            String releaseDate = escapeHtml(getJsonString(movie, "release_date"));
            String ratings = escapeHtml(getJsonString(movie, "vote_average"));
            String posterPath = getJsonString(movie, "poster_path");

            htmlResponse.append("<div class='movie-image'>");
            htmlResponse.append("<img src='https://image.tmdb.org/t/p/w500").append(posterPath).append("' alt='").append(title).append("'>");
            htmlResponse.append("</div>");
            htmlResponse.append("<div class='movie-details'>");
            htmlResponse.append("<h1>").append(title).append("</h1>");
            htmlResponse.append("<p><strong>Release Date:</strong> ").append(releaseDate).append("</p>");
            htmlResponse.append("<p><strong>Overview:</strong> \"").append(overview).append("</p>");
            htmlResponse.append("<p><strong>Ratings:</strong> ").append(ratings).append("</p>");
            htmlResponse.append("</div>");

            htmlResponse.append("</div>");
            htmlResponse.append("</body></html>");

            // Send the HTML response back to the client
            response.setContentType("text/html");
            response.getWriter().write(htmlResponse.toString());

        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Failed to fetch movie details from TMDB API");
        }
        connection.disconnect();
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
