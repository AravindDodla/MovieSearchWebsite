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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class MovieDisplayServlet extends HttpServlet {

    private static final String TMDB_API_KEY = "589c688f1ec69463e97880f0caca2a22"; // Replace with your TMDB API key
    private static final String TMDB_API_BASE_URL = "https://api.themoviedb.org/3/trending/movie/day?api_key=" + TMDB_API_KEY;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TMDB API URL for popular movies
        String apiUrl = TMDB_API_BASE_URL;

        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

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
            JsonObject jsonResponse = gson.fromJson(content.toString(), JsonObject.class);
            JsonArray results = jsonResponse.getAsJsonArray("results");

            // Start building the HTML response
            StringBuilder htmlResponse = new StringBuilder();
            htmlResponse.append("<!DOCTYPE html>");
            htmlResponse.append("<html lang='en'>");
            htmlResponse.append("<head>");
            htmlResponse.append("<meta charset='UTF-8'>");
            htmlResponse.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            htmlResponse.append("<title>Trending Movies</title>");
            htmlResponse.append("<link rel='stylesheet' type='text/css' href='TestingPage.css'>");
            htmlResponse.append("<link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css\" integrity=\"sha512-DTOQO9RWCH3ppGqcWaEA1BIZOC6xxalwEsw9c2QQeAIftl+Vegovlnee1c9QX4TctnWMn13TZye+giMm8e2LwA==\" crossorigin=\"anonymous\" referrerpolicy=\"no-referrer\" />");
            htmlResponse.append("<style>");
            htmlResponse.append("body { font-family: Arial, sans-serif; margin: 0; padding: 0; }");
            htmlResponse.append(".movie-container { display: flex; flex-wrap: wrap; gap: 20px; justify-content: center; padding: 20px; }");
            htmlResponse.append(".card { background-color: white; border-radius: 8px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1); overflow: hidden; width: 200px; transition: transform 0.3s; display: flex; flex-direction: column; align-items: center; }");
            htmlResponse.append(".card:hover { transform: scale(1.05); }");
            htmlResponse.append(".img-container { width: 100%; height: 300px; background-size: cover; background-position: center; }");
            htmlResponse.append(".card-body { padding: 10px; width: 100%; text-align: center; }");
            htmlResponse.append(".card-title { font-size: 16px; font-weight: bold; margin-top: 10px; }");
            htmlResponse.append(".trending-title { text-align: center; font-size: 24px; font-weight: bold; margin-top: 20px; }"); // Style for trending title
            htmlResponse.append("</style>");
            htmlResponse.append("</head>");
            htmlResponse.append("<body>");            
            htmlResponse.append("<div class='trending-title'>Trending Movies</div>"); // Added trending title
            htmlResponse.append("<div id='movie-container' class='movie-container'>");

            for (int i = 0; i < results.size(); i++) {
                JsonObject movie = results.get(i).getAsJsonObject();

                String id = movie.get("id").getAsString();
                String title = escapeHtml(getJsonString(movie, "title"));
                String overview = escapeHtml(getJsonString(movie, "overview"));
                String releaseDate = escapeHtml(getJsonString(movie, "release_date"));
                String posterPath = getJsonString(movie, "poster_path");

                htmlResponse.append("<div class='card'>");
                String contextPath = request.getContextPath();
                htmlResponse.append("<a href='").append(contextPath).append("/MovieDetails?id=").append(id).append("-").append(title).append("'>");

                if (!posterPath.isEmpty()) {
                    htmlResponse.append("<div class='img-container'>");
                    htmlResponse.append("<img src='https://image.tmdb.org/t/p/w500").append(posterPath).append("' alt='").append(title).append("' style='width:100%; height:100%;'>");
                    htmlResponse.append("</div>");
                }

                htmlResponse.append("</a>");
                htmlResponse.append("<div class='card-body'>");
                htmlResponse.append("<div class='card-title'>").append(title).append("</div>");
                htmlResponse.append("</div>");
                htmlResponse.append("</div>");
            }

            htmlResponse.append("</div>");
            htmlResponse.append("<script>");
            htmlResponse.append("function getDetails(event, id) {");
            htmlResponse.append(" event.preventDefault(); ");
            htmlResponse.append("window.location.href = 'https://api.themoviedb.org/3/movie/' + id + '?api_key=" + TMDB_API_KEY + "';");
            htmlResponse.append("}");
            htmlResponse.append("</script>");
            htmlResponse.append("</body></html>");

            // Send the HTML response back to the client
            response.setContentType("text/html");
            response.getWriter().write(htmlResponse.toString());

        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Failed to fetch data from TMDB API");
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
