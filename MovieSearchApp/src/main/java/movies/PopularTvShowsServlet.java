package movies;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;

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

public class PopularTvShowsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String TMDB_API_KEY = "589c688f1ec69463e97880f0caca2a22";
    private static final String TMDB_API_BASE_URL = "https://api.themoviedb.org/3/tv/popular?api_key=" + TMDB_API_KEY + "&language=en-US&page=1";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TMDB API URL for popular TV shows
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
            htmlResponse.append("<title>Popular TV Shows</title>");
            htmlResponse.append("<link rel='stylesheet' type='text/css' href='TestingPage.css'>");
            htmlResponse.append("<link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css\" integrity=\"sha512-DTOQO9RWCH3ppGqcWaEA1BIZOC6xxalwEsw9c2QQeAIftl+Vegovlnee1c9QX4TctnWMn13TZye+giMm8e2LwA==\" crossorigin=\"anonymous\" referrerpolicy=\"no-referrer\" />");
            htmlResponse.append("<style>");
            htmlResponse.append("body { font-family: Arial, sans-serif; background-color: #ffdfd6; margin: 0; padding: 0; }");
            htmlResponse.append("header { background-color: #021526; color: #fff; padding: 10px 20px; display: flex; align-items: center; justify-content: space-between; }");
            htmlResponse.append("header .nav-links a { color: #fff; text-decoration: none; margin: 0 10px; }");
            htmlResponse.append("header .search-bar { margin-left: auto; }");
            htmlResponse.append("header .search-bar input[type='text'] { padding: 5px; border-radius: 4px; border: none; }");
            htmlResponse.append("header .search-bar button { padding: 5px 10px; border: none; background-color: #333; color: #fff; border-radius: 4px; cursor: pointer; }");
            htmlResponse.append(".title { text-align: center; font-size: 28px; font-weight: bold; color: #fff; padding: 20px 0; background-color: #4A6EA8; margin-top: 0; }");
            htmlResponse.append(".tv-show-container { display: flex; flex-wrap: wrap; gap: 20px; justify-content: center; padding: 20px; }");
            htmlResponse.append(".card { background-color: white; border-radius: 8px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1); overflow: hidden; width: 200px; transition: transform 0.3s; display: flex; flex-direction: column; align-items: center; }");
            htmlResponse.append(".card:hover { transform: scale(1.05); }");
            htmlResponse.append(".img-container { width: 100%; height: 300px; }");
            htmlResponse.append(".img-container img { width: 100%; height: 100%; object-fit: cover; border-bottom: 1px solid #ddd; }");
            htmlResponse.append(".card-body { padding: 10px; width: 100%; text-align: center; }");
            htmlResponse.append(".card-title { font-size: 16px; font-weight: bold; margin-top: 10px; }");
            htmlResponse.append(".card-overview { font-size: 14px; color: #555; margin-top: 5px; max-height: 60px; overflow: hidden; }");
            htmlResponse.append("</style>");
            htmlResponse.append("</head>");
            htmlResponse.append("<body>");
            htmlResponse.append("<header>");
            htmlResponse.append("<div class='nav-links'>");
            htmlResponse.append("<a href='PopularMoviesServlet'>Popular Movies</a>");
            htmlResponse.append("<a href='LatestMovies'>Latest Movies</a>");
            htmlResponse.append("</div>");
            htmlResponse.append("<div class='search-bar'>");
            htmlResponse.append("<form action='MovieServlet' method='get'>");
            htmlResponse.append("<input type='text' name='movie' placeholder='Search movies...' />");
            htmlResponse.append("<button type='submit'>Search</button>");
            htmlResponse.append("</form>");
            htmlResponse.append("</div>");
            htmlResponse.append("</header>");
            htmlResponse.append("<div class='title'>Popular TV Shows</div>");
            htmlResponse.append("<div class='tv-show-container'>");

            for (JsonElement element : results) {
                JsonObject tvShowObject = element.getAsJsonObject();

                String id = tvShowObject.get("id").getAsString();
                String name = escapeHtml(getJsonString(tvShowObject, "name"));
                String overview = escapeHtml(getJsonString(tvShowObject, "overview"));
                String posterPath = getJsonString(tvShowObject, "poster_path");

                htmlResponse.append("<div class='card'>");
                
                String contextPath = request.getContextPath();
                htmlResponse.append("<a href='").append(contextPath).append("/TvShowDetails?id=").append(id).append("-").append("'>");;

                
                if (!posterPath.isEmpty()) {
                    htmlResponse.append("<div class='img-container'>");
                    htmlResponse.append("<img src='https://image.tmdb.org/t/p/w500").append(posterPath).append("' alt='").append(name).append("'>");
                    htmlResponse.append("</div>");
                }
                htmlResponse.append("</a>");
                htmlResponse.append("<div class='card-body'>");
                htmlResponse.append("<div class='card-title'>").append(name).append("</div>");
                htmlResponse.append("<div class='card-overview'>").append(overview).append("</div>");
                htmlResponse.append("</div>");
                htmlResponse.append("</div>");
            }

            htmlResponse.append("</div>");
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

  
    private String getJsonString(JsonObject jsonObject, String memberName) {
        JsonElement jsonElement = jsonObject.get(memberName);
        return jsonElement != null && !jsonElement.isJsonNull() ? jsonElement.getAsString() : "";
    }

    
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
