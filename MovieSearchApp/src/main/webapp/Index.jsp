<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Movie Search Application</title>
    <style>
        body {
            background-color: #a9f12b;
            margin: 0;
            padding: 0;
            display: flex;
            flex-direction: column;
            min-height: 100vh;
        }

        header {
            background-color: #111110;
            color: white;
            height: 100px; 
            text-align: center;
            width: 100%;
            display: flex;
            justify-content: space-around;
            align-items: center;
            flex-wrap: wrap; 
            position: relative;
            padding: 0 20px;
        }

        header h1 {
            margin: 0;
            font-size: 24px;
            cursor: pointer;
            position: relative;
        }

        .dropdown {
            display: none;
            position: absolute;
            background-color: #333;
            color: white;
            min-width: 200px;
            z-index: 1;
            text-align: left;
        }

        .dropdown a {
            display: block;
            padding: 10px;
            color: white;
            text-decoration: none;
        }

        .dropdown a:hover {
            background-color: #444;
        }

        form {
            display: flex;
            align-items: center;
            gap: 5px; 
            position: relative;
        }

        input[type="text"] {
            width: 100%;
            max-width: 500px;
            min-width: 200px; 
            height: 40px;
            padding: 10px;
            font-size: 18px;
            border: none;
            border-radius: 5px;
        }

        .clear-btn {
            position: absolute;
            right: 80px;
            background: none;
            border: none;
            font-size: 20px;
            color: #FF4C4C;
            cursor: pointer;
            display: none;
        }

        input[type="text"]:focus + .clear-btn {
            display: block;
        }

        input[type="submit"] {
            height: 40px;
            padding: 10px 20px;
            font-size: 18px;
            background-color: #4CAF50;
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
        }

        input[type="submit"]:hover {
            background-color: #45a049;
        }

        .content-section {
            padding: 20px;
            flex: 1;
            overflow: hidden; 
        }

        .movie-cards {
            display: flex;
            flex-wrap: wrap;
            gap: 20px;
        }

        .movie-card {
            border: 1px solid #ddd;
            padding: 10px;
            border-radius: 5px;
            background-color: white;
            width: 200px;
        }

        .movie-card img {
            width: 100%;
            height: auto;
            border-radius: 5px;
        }

        a {
            color: white;
            text-decoration: none;
        }

        a:hover h1 {
            text-decoration: underline;
        }

        iframe {
        scrollbar:hidden;
            width: 100%;
            height: 100vh; /* Full viewport height to accommodate content */
            border: none;
            overflow: hidden; /* Hides the scrollbar */
        }
    </style>
</head>
<body>
    <header>
        <div class="menu-item">
            <a href="LatestMovies">
                <h1>Latest Movies</h1>
            </a>
        </div>
        <div class="menu-item">
            <a href="PopularMoviesServlet">
                <h1>Popular Movies</h1>
            </a>
        </div>
        <div class="menu-item">
            <a href="PopularTvShowsServlet">
                <h1>Popular Tv Shows</h1>
            </a>
        </div>
        <form action="MovieServlet">
            <input type="text" id="searchInput" name="movie" placeholder="Search your movie here..." oninput="toggleClearButton(this)">
            <button type="button" class="clear-btn" onclick="clearSearch()">×</button>
            <input type="submit" value="Go">
        </form>
    </header>

    <h1> Hello Welcome to Movie Search Engine</h1>
    
    <div class="content-section" id="contentSection">
        
        <iframe src="MovieDisplayServlet" id="movieIframe"></iframe>
    </div>

    <script>
        function toggleClearButton(input) {
            const clearBtn = document.querySelector('.clear-btn');
            if (input.value.trim() !== '') {
                clearBtn.style.display = 'block';
            } else {
                clearBtn.style.display = 'none';
            }
        }

        function clearSearch() {
            const searchInput = document.getElementById('searchInput');
            searchInput.value = '';
            toggleClearButton(searchInput);
        }

        // Adjust iframe height dynamically
        window.addEventListener('load', function() {
            const iframe = document.getElementById('movieIframe');
            iframe.onload = function() {
                // Get the height of the iframe's content
                iframe.style.height = iframe.contentWindow.document.body.scrollHeight + 'px';
            };
        });
    </script>
</body>
</html>
