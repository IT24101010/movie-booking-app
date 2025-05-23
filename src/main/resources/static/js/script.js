// Main JavaScript file - script.js

document.addEventListener('DOMContentLoaded', () => {
    // --- Global Variables & API Base URL ---
    const API_BASE_URL = '/api';
    let allMovies = [];
    let currentlyDisplayedMovies = [];
    let moviesPerPage = 10;
    let currentPage = 1;
    let currentSortActive = false; // To track if sorting by release date is active

    // --- DOM Elements ---
    const movieGrid = document.getElementById('movie-grid');
    const categoryFilters = document.getElementById('category-filters');
    const dateTabsContainer = document.getElementById('date-tabs');
    const timeSlotsContainer = document.getElementById('time-slots');
    const movieSearchInput = document.getElementById('movie-search-input');
    const loadMoreMoviesButton = document.getElementById('load-more-movies');
    const buyTicketMainBtn = document.getElementById('buy-ticket-main');


    const heroTitle = document.getElementById('hero-title');
    const heroDescription = document.getElementById('hero-description');
    const heroYear = document.getElementById('hero-year'); // This was used for release year, can be updated or kept
    const heroRating = document.getElementById('hero-rating');
    const heroSection = document.getElementById('hero-section');
    const heroReadMore = document.getElementById('hero-read-more');

    const mobileMenuButton = document.getElementById('mobile-menu-button');
    const mobileMenu = document.getElementById('mobile-menu');

    const userActionButton = document.getElementById('user-action-btn');
    const mobileUserActionButton = document.getElementById('mobile-user-action-btn');
    const userProfileDropdown = document.getElementById('user-profile-dropdown');
    const mobileUserProfileDropdown = document.getElementById('mobile-user-profile-dropdown');
    const userAvatarButton = document.getElementById('user-avatar-btn');
    const dropdownMenu = document.getElementById('dropdown-menu');
    const logoutButton = document.getElementById('logout-btn');
    const mobileLogoutButton = document.getElementById('mobile-logout-btn');

    // New Sort Button
    const sortByReleaseBtn = document.getElementById('sort-by-release-date-btn');


    // --- Utility Functions ---
    const displayFlashMessage = (message, type = 'success') => {
        // ... (keep existing flash message function)
        const flashDiv = document.createElement('div');
        flashDiv.className = `fixed top-24 right-5 p-4 rounded-md shadow-lg text-white z-[100] ${type === 'success' ? 'bg-green-500' : 'bg-red-500'}`;
        flashDiv.textContent = message;
        document.body.appendChild(flashDiv);
        setTimeout(() => {
            flashDiv.style.transition = 'opacity 0.5s ease';
            flashDiv.style.opacity = '0';
            setTimeout(() => flashDiv.remove(), 500);
        }, 3000);
    };


    // --- Authentication State ---
    const checkLoginState = () => {
        // ... (keep existing checkLoginState function)
        const loggedInUser = JSON.parse(localStorage.getItem('loggedInUser'));
        if (loggedInUser) {
            if (userActionButton) userActionButton.classList.add('hidden');
            if (mobileUserActionButton) mobileUserActionButton.classList.add('hidden');

            if (userProfileDropdown) userProfileDropdown.classList.remove('hidden');
            if (mobileUserProfileDropdown) mobileUserProfileDropdown.classList.remove('hidden');

            if(userAvatarButton && loggedInUser.username) {
                userAvatarButton.innerHTML = `<span class="text-lg font-semibold">${loggedInUser.username.charAt(0).toUpperCase()}</span>`;
            }
        } else {
            if (userActionButton) userActionButton.classList.remove('hidden');
            if (mobileUserActionButton) mobileUserActionButton.classList.remove('hidden');

            if (userProfileDropdown) userProfileDropdown.classList.add('hidden');
            if (mobileUserProfileDropdown) mobileUserProfileDropdown.classList.add('hidden');
        }
    };

    const handleLogout = () => {
        // ... (keep existing handleLogout function)
        localStorage.removeItem('loggedInUser');
        localStorage.removeItem('authToken'); // If you use tokens
        checkLoginState();
        displayFlashMessage('Logged out successfully.', 'success');
        window.location.href = 'index.html';
    };


    // --- API Fetch Functions ---
    const fetchMovies = async (filter = 'all', sortByRelease = false) => {
        if (!movieGrid) return; // Exit if movieGrid is not on the current page
        movieGrid.innerHTML = '<p class="text-center col-span-full text-gray-400">Loading movies...</p>';
        if(loadMoreMoviesButton) loadMoreMoviesButton.classList.add('hidden');

        try {
            let apiUrl = `${API_BASE_URL}/movies`;
            const params = new URLSearchParams();

            if (sortByRelease) {
                params.append('sortByReleaseDate', 'true');
                currentSortActive = true;
            } else {
                // If not sorting by release, check other filters (this part can be expanded)
                // For now, 'filter' is mainly for UI active state, not API query
                currentSortActive = false;
            }

            // Example: if you add more specific filters later for "now_playing" or "coming_soon" via API
            // if (filter === 'now_playing') params.append('status', 'now_playing');
            // if (filter === 'coming_soon') params.append('status', 'coming_soon');

            const queryString = params.toString();
            if (queryString) {
                apiUrl += `?${queryString}`;
            }

            const response = await fetch(apiUrl);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const movies = await response.json();
            allMovies = movies;
            currentPage = 1;
            currentlyDisplayedMovies = [];
            renderMovies();
            // Only update hero section if it's a default load (not sorting or specific filter)
            // And if the hero section elements exist
            if (!sortByRelease && filter === 'all' && heroTitle) {
                 updateHeroSection(movies);
            }
        } catch (error) {
            console.error("Error fetching movies:", error);
            if (movieGrid) movieGrid.innerHTML = '<p class="text-center text-red-400 col-span-full">Could not load movies. Please try again later.</p>';
        }
    };

    // --- Rendering Functions ---
    const renderMovieCard = (movie) => {
        // This assumes movie.showtimes is not part of the main movie object from /api/movies
        // You'd typically fetch showtimes separately or they'd be embedded if the API supports it.
        // For this example, we'll remove the direct showtimes display from the card
        // as it's not provided by the /api/movies endpoint directly.

        const genre = movie.genre || 'N/A';
        const rating = movie.rating || 'N/A'; // This is the text rating like 'PG-13' or numeric '8.7'
        let releaseDateFormatted = 'N/A';
        if (movie.releaseDate) { // movie.releaseDate is expected as "YYYY-MM-DD"
            try {
                // Split the date string and reformat it or use Date object carefully
                const [year, month, day] = movie.releaseDate.split('-');
                const dateObj = new Date(year, month - 1, day); // Month is 0-indexed
                if (!isNaN(dateObj)) {
                    releaseDateFormatted = dateObj.toLocaleDateString('en-US', {
                        year: 'numeric', month: 'short', day: 'numeric'
                    });
                }
            } catch (e) {
                console.error("Error formatting release date for card:", movie.releaseDate, e);
            }
        }
        const price = movie.price !== undefined && movie.price !== null ? `$${movie.price.toFixed(2)}` : 'N/A';


        return `
            <div class="movie-card bg-zinc-800 rounded-lg overflow-hidden shadow-xl transform hover:scale-105 transition duration-300 cursor-pointer flex flex-col" data-movie-id="${movie.movieId}">
                <img src="${movie.posterUrl || `https://placehold.co/300x450/4a5568/ffffff?text=${encodeURIComponent(movie.title)}`}" alt="${movie.title}" class="w-full h-auto object-cover aspect-[2/3]" onerror="this.onerror=null;this.src='https://placehold.co/300x450/718096/ffffff?text=Image+Not+Found';">
                <div class="p-4 flex flex-col flex-grow">
                    <h3 class="text-lg font-semibold mb-1 truncate flex-shrink-0" title="${movie.title}">${movie.title}</h3>
                    <p class="text-xs text-gray-400 mb-1 truncate flex-shrink-0">${genre} | ${rating}</p>
                    <p class="text-xs text-gray-500 mb-2 truncate flex-shrink-0">Released: ${releaseDateFormatted}</p>
                    <div class="mt-auto pt-2 flex-shrink-0"> <span class="text-sm font-semibold text-red-400">${price}</span>
                    </div>
                </div>
            </div>
        `;
    };

    const renderMovies = () => {
        if (!movieGrid) return;

        const moviesToDisplay = allMovies.slice(0, currentPage * moviesPerPage);
        currentlyDisplayedMovies = moviesToDisplay;

        if (moviesToDisplay.length === 0 && movieSearchInput.value.trim() === '') { // Avoid showing "no movies" if search is active and yields no results
            movieGrid.innerHTML = '<p class="text-center text-gray-400 col-span-full">No movies available at the moment.</p>';
            if(loadMoreMoviesButton) loadMoreMoviesButton.classList.add('hidden');
            return;
        } else if (moviesToDisplay.length === 0 && movieSearchInput.value.trim() !== '') {
             movieGrid.innerHTML = '<p class="text-center text-gray-400 col-span-full">No movies found matching your search.</p>';
             if(loadMoreMoviesButton) loadMoreMoviesButton.classList.add('hidden');
             return;
        }


        movieGrid.innerHTML = moviesToDisplay.map(renderMovieCard).join('');

        if (loadMoreMoviesButton) {
            if (currentlyDisplayedMovies.length < allMovies.length && !currentSortActive && movieSearchInput.value.trim() === '') {
                // Show load more only if not sorting and not searching (as these display all results at once)
                loadMoreMoviesButton.classList.remove('hidden');
            } else {
                loadMoreMoviesButton.classList.add('hidden');
            }
        }

        document.querySelectorAll('.movie-card').forEach(card => {
            card.addEventListener('click', () => {
                const movieId = card.dataset.movieId;
                window.location.href = `movie_details.html?id=${movieId}`;
            });
        });
    };

    const updateHeroSection = (movies) => {
        if (!movies || movies.length === 0 || !heroTitle) return; // Check if hero elements exist

        const featuredMovie = movies[0]; // Or a specifically chosen featured movie

        heroTitle.textContent = featuredMovie.title;
        heroDescription.textContent = featuredMovie.synopsis ? featuredMovie.synopsis.substring(0, 150) + '...' : 'No description available.';

        if (featuredMovie.releaseDate) {
            try {
                heroYear.textContent = new Date(featuredMovie.releaseDate).getFullYear();
            } catch (e) {
                 heroYear.textContent = new Date().getFullYear(); // Fallback
            }
        } else {
            heroYear.textContent = new Date().getFullYear(); // Fallback
        }

        if (heroSection && featuredMovie.bannerUrl) {
            heroSection.style.backgroundImage = `linear-gradient(rgba(0,0,0,0.6), rgba(0,0,0,0.6)), url('${featuredMovie.bannerUrl}')`;
        } else if (heroSection && featuredMovie.posterUrl) {
             heroSection.style.backgroundImage = `linear-gradient(rgba(0,0,0,0.6), rgba(0,0,0,0.6)), url('${featuredMovie.posterUrl}')`;
        } else if (heroSection) {
            heroSection.style.backgroundImage = `linear-gradient(rgba(0,0,0,0.6), rgba(0,0,0,0.6)), url('https://placehold.co/1920x1080/2d3748/e2e8f0?text=Featured+Movie')`;
        }

        if (heroReadMore) heroReadMore.href = `movie_details.html?id=${featuredMovie.movieId}`;
        if (heroRating) heroRating.innerHTML = `<i class="fas fa-star"></i> <i class="fas fa-star"></i> <i class="fas fa-star"></i> <i class="fas fa-star-half-alt"></i> <i class="far fa-star"></i> <span class="ml-2 text-white">${featuredMovie.rating || 'N/A'}</span>`; // Assuming 'rating' is the text rating
    };


    const generateDateTabs = () => {
        if (!dateTabsContainer) return;
        // ... (keep existing generateDateTabs function)
        dateTabsContainer.innerHTML = ''; // Clear existing
        const today = new Date();
        const days = ['SUN', 'MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT'];
        for (let i = 0; i < 7; i++) { // Display next 7 days
            const date = new Date(today);
            date.setDate(today.getDate() + i);
            const dayName = days[date.getDay()];
            const dayOfMonth = String(date.getDate()).padStart(2, '0');
            const isActive = i === 0 ? 'active bg-red-600 text-white' : 'bg-zinc-700 hover:bg-red-500 text-gray-300';
            const button = document.createElement('button');
            button.className = `date-tab ${isActive} px-3 py-2 sm:px-4 rounded-md text-xs sm:text-sm transition duration-300`;
            button.textContent = `${dayName}, ${dayOfMonth}`;
            button.dataset.date = date.toISOString().split('T')[0]; // YYYY-MM-DD
            dateTabsContainer.appendChild(button);
        }
    };

    // --- Event Listeners ---
    if (categoryFilters) {
        categoryFilters.addEventListener('click', (e) => {
            if (e.target.classList.contains('category-filter')) {
                document.querySelectorAll('.category-filter').forEach(btn => btn.classList.remove('active', 'text-white', 'border-red-500'));
                e.target.classList.add('active', 'text-white', 'border-red-500');

                const filterType = e.target.dataset.filter;
                if (filterType === 'release_date_sort') {
                    fetchMovies('all', true); // Call fetchMovies with sortByRelease true
                } else {
                    // Handle other filters like "all" (now playing), "coming_soon"
                    // For "all", fetch normally (unsorted or default backend sort)
                    fetchMovies(filterType, false);
                }
            }
        });
    }


    if (dateTabsContainer) {
        // ... (keep existing dateTabsContainer listener)
        dateTabsContainer.addEventListener('click', (e) => {
            if (e.target.classList.contains('date-tab')) {
                document.querySelectorAll('.date-tab').forEach(btn => btn.classList.remove('active', 'bg-red-600', 'text-white'));
                e.target.classList.add('active', 'bg-red-600', 'text-white');
                const selectedDate = e.target.dataset.date;
                console.log("Selected date:", selectedDate);
                // Mock time slots update or fetch actual time slots
                if(timeSlotsContainer) timeSlotsContainer.innerHTML = `<span class="time-slot bg-zinc-700 text-gray-300 px-3 py-1 rounded text-sm">10:30</span> <span class="time-slot bg-zinc-700 text-gray-300 px-3 py-1 rounded text-sm">15:00</span>`;
            }
        });
    }

    if (movieSearchInput) {
        movieSearchInput.addEventListener('input', (e) => {
            // ... (keep existing movieSearchInput listener)
            const searchTerm = e.target.value.toLowerCase();
            const filteredMovies = allMovies.filter(movie => movie.title.toLowerCase().includes(searchTerm));

            if (movieGrid) {
                 if (filteredMovies.length === 0 && searchTerm.length > 0) {
                    movieGrid.innerHTML = '<p class="text-center text-gray-400 col-span-full">No movies found matching your search.</p>';
                    if(loadMoreMoviesButton) loadMoreMoviesButton.classList.add('hidden');
                } else if (searchTerm.length === 0) {
                    // Reset to default view if search is cleared
                    currentPage = 1; // Reset pagination
                    renderMovies(); // This will use allMovies and current page
                }
                 else {
                    // Display search results without pagination for simplicity during search
                    movieGrid.innerHTML = filteredMovies.map(renderMovieCard).join('');
                    if(loadMoreMoviesButton) loadMoreMoviesButton.classList.add('hidden');
                }
            }
        });
    }

    if (loadMoreMoviesButton) {
        // ... (keep existing loadMoreMoviesButton listener)
         loadMoreMoviesButton.addEventListener('click', () => {
            currentPage++;
            renderMovies(); // This will render the next set from allMovies
        });
    }

    if (mobileMenuButton && mobileMenu) {
        // ... (keep existing mobileMenuButton listener)
        mobileMenuButton.addEventListener('click', () => {
            mobileMenu.classList.toggle('hidden');
        });
    }

    if (userAvatarButton && dropdownMenu) {
        // ... (keep existing userAvatarButton listener)
        userAvatarButton.addEventListener('click', (e) => {
            e.stopPropagation();
            dropdownMenu.classList.toggle('hidden');
        });
    }
    document.addEventListener('click', (e) => {
        // ... (keep existing document click listener for dropdown)
        if (userAvatarButton && dropdownMenu && !userAvatarButton.contains(e.target) && !dropdownMenu.contains(e.target)) {
            dropdownMenu.classList.add('hidden');
        }
    });


    if (logoutButton) logoutButton.addEventListener('click', handleLogout);
    if (mobileLogoutButton) mobileLogoutButton.addEventListener('click', handleLogout);
    if (buyTicketMainBtn) {
        buyTicketMainBtn.addEventListener('click', () => {
            window.location.href = 'booking_page.html';
        });
    }

    // --- Initializations ---
    if(document.getElementById('footer-year')) {
        document.getElementById('footer-year').textContent = new Date().getFullYear();
    }
    fetchMovies(); // Initial fetch (default: "all" or "now_playing", unsorted)
    generateDateTabs();
    checkLoginState();

});