document.addEventListener('DOMContentLoaded', () => {
    // DOM Elements
    const movieSelect = document.getElementById('booking-movie-select');
    const showtimeSelect = document.getElementById('booking-showtime-select');
    const selectedShowtimeDetails = document.getElementById('selected-showtime-details');
    const ticketPriceDisplay = document.getElementById('ticket-price-display'); // New element for price
    const seatMapContainer = document.getElementById('seat-map-container');
    const confirmBookingBtn = document.getElementById('confirm-booking-btn');
    const flashMessageContainer = document.getElementById('flash-message-container');

    const step1Section = document.getElementById('step-1-selection');
    const step2Section = document.getElementById('step-2-seat-selection');
    const step3Section = document.getElementById('step-3-summary');

    const summaryMovieTitle = document.getElementById('summary-movie-title');
    const summaryShowtimeDateTime = document.getElementById('summary-showtime-datetime');
    const summaryShowtimeScreen = document.getElementById('summary-showtime-screen');
    const summarySelectedSeats = document.getElementById('summary-selected-seats');
    const summaryNumTickets = document.getElementById('summary-num-tickets');
    const summaryTotalPrice = document.getElementById('summary-total-price');

    const API_BASE_URL = '/api';
    let allMoviesData = []; // Will store { movieId, title, price }
    let allShowtimesForMovie = [];
    let selectedSeats = [];
    let currentShowtimeData = null;
    let currentMoviePrice = 0; // Store the price of the currently selected movie

    // --- Utility Functions ---
    const displayFlash = (message, type = 'error') => {
        if (!flashMessageContainer) return;
        flashMessageContainer.innerHTML = `<div class="p-3 mb-4 rounded-md text-sm ${type === 'success' ? 'bg-green-600 text-green-100' : 'bg-red-600 text-red-100'}">${message}</div>`;
        setTimeout(() => flashMessageContainer.innerHTML = '', 5000);
    };

    // --- Header Auth State ---
    const userActionButtonHeader = document.getElementById('user-action-btn-header');
    const mobileUserActionButtonHeader = document.getElementById('mobile-user-action-btn-header');
    const userProfileDropdownHeader = document.getElementById('user-profile-dropdown-header');
    const mobileUserProfileDropdownHeader = document.getElementById('mobile-user-profile-dropdown-header');
    const userAvatarButtonHeader = document.getElementById('user-avatar-btn-header');
    const dropdownMenuHeader = document.getElementById('dropdown-menu-header');
    const logoutButtonHeader = document.getElementById('logout-btn-header');
    const mobileLogoutButtonHeader = document.getElementById('mobile-logout-btn-header');

    const checkLoginStateHeader = () => {
        const loggedInUser = JSON.parse(localStorage.getItem('loggedInUser'));
        if (!loggedInUser) {
            displayFlash('Please log in to book tickets.', 'error');
            setTimeout(() => window.location.href = 'login.html', 2000);
            return false;
        }
        if (userActionButtonHeader) userActionButtonHeader.classList.add('hidden');
        if (mobileUserActionButtonHeader) mobileUserActionButtonHeader.classList.add('hidden');
        if (userProfileDropdownHeader) userProfileDropdownHeader.classList.remove('hidden');
        if (mobileUserProfileDropdownHeader) mobileUserProfileDropdownHeader.classList.remove('hidden');
        if (userAvatarButtonHeader && loggedInUser.username) {
            userAvatarButtonHeader.innerHTML = `<span class="text-lg font-semibold">${loggedInUser.username.charAt(0).toUpperCase()}</span>`;
        }
        return true;
    };
    const handleLogoutHeader = () => {
        localStorage.removeItem('loggedInUser');
        checkLoginStateHeader();
    };
    if (userAvatarButtonHeader && dropdownMenuHeader) {
        userAvatarButtonHeader.addEventListener('click', (e) => {e.stopPropagation(); dropdownMenuHeader.classList.toggle('hidden');});
    }
    document.addEventListener('click', (e) => {
        if (userAvatarButtonHeader && dropdownMenuHeader && !userAvatarButtonHeader.contains(e.target) && !dropdownMenuHeader.contains(e.target)) {
            dropdownMenuHeader.classList.add('hidden');
        }
    });
    if(logoutButtonHeader) logoutButtonHeader.addEventListener('click', handleLogoutHeader);
    if(mobileLogoutButtonHeader) mobileLogoutButtonHeader.addEventListener('click', handleLogoutHeader);
    // --- End Header Auth State ---

    const loadMovies = async () => {
        try {
            const response = await fetch(`${API_BASE_URL}/movies`);
            if (!response.ok) throw new Error('Failed to load movies.');
            allMoviesData = await response.json();
            movieSelect.innerHTML = '<option value="" disabled selected>Select a Movie</option>';
            allMoviesData.forEach(movie => {
                const option = document.createElement('option');
                option.value = movie.movieId;
                option.textContent = movie.title;
                option.dataset.price = movie.price !== undefined ? movie.price : 0; // Store price
                movieSelect.appendChild(option);
            });
            const urlParams = new URLSearchParams(window.location.search);
            const urlMovieId = urlParams.get('movieId');
            if (urlMovieId && allMoviesData.some(m => m.movieId === urlMovieId)) {
                movieSelect.value = urlMovieId;
                const selectedOption = movieSelect.options[movieSelect.selectedIndex];
                currentMoviePrice = parseFloat(selectedOption.dataset.price) || 0;
                if(ticketPriceDisplay) ticketPriceDisplay.textContent = `Ticket Price: $${currentMoviePrice.toFixed(2)}`;
                loadShowtimesForMovie(urlMovieId);
            }

        } catch (error) {
            console.error(error);
            displayFlash(error.message);
            if(movieSelect) movieSelect.innerHTML = '<option value="">Error loading movies</option>';
        }
    };

    const loadShowtimesForMovie = async (movieId) => {
        if (!movieId) {
            if(showtimeSelect) {
                showtimeSelect.innerHTML = '<option value="" disabled selected>Select a movie first</option>';
                showtimeSelect.disabled = true;
            }
            if(selectedShowtimeDetails) selectedShowtimeDetails.innerHTML = '';
            if(ticketPriceDisplay) ticketPriceDisplay.innerHTML = '';
            if(step2Section) step2Section.classList.add('hidden');
            if(step3Section) step3Section.classList.add('hidden');
            return;
        }

        const selectedMovie = allMoviesData.find(m => m.movieId === movieId);
        currentMoviePrice = selectedMovie ? (selectedMovie.price !== undefined ? selectedMovie.price : 0) : 0;
        if(ticketPriceDisplay) ticketPriceDisplay.textContent = `Ticket Price: $${currentMoviePrice.toFixed(2)}`;


        if(showtimeSelect) {
            showtimeSelect.innerHTML = '<option value="">Loading showtimes...</option>';
            showtimeSelect.disabled = true;
        }
        try {
            const response = await fetch(`${API_BASE_URL}/showtimes/movie/${movieId}`);
            if (!response.ok) throw new Error('Failed to load showtimes.');
            allShowtimesForMovie = await response.json();
            if(showtimeSelect) showtimeSelect.innerHTML = '<option value="" disabled selected>Select a Showtime</option>';

            if (allShowtimesForMovie.length > 0) {
                allShowtimesForMovie.forEach(st => {
                    const option = document.createElement('option');
                    option.value = st.showtimeId;
                    const dateTime = new Date(st.dateTime).toLocaleString('en-US', { weekday: 'short', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit', hour12: true });
                    option.textContent = `${dateTime} - ${st.screenOrAuditorium}`;
                    if(showtimeSelect) showtimeSelect.appendChild(option);
                });
                if(showtimeSelect) showtimeSelect.disabled = false;

                const urlParams = new URLSearchParams(window.location.search);
                const urlShowtimeId = urlParams.get('showtimeId');
                if (urlShowtimeId && allShowtimesForMovie.some(s => s.showtimeId === urlShowtimeId)) {
                    if(showtimeSelect) showtimeSelect.value = urlShowtimeId;
                    handleShowtimeSelection(urlShowtimeId);
                }
            } else {
                if(showtimeSelect) showtimeSelect.innerHTML = '<option value="">No showtimes available</option>';
            }
        } catch (error) {
            console.error(error);
            displayFlash(error.message);
            if(showtimeSelect) showtimeSelect.innerHTML = '<option value="">Error loading showtimes</option>';
        }
    };

    const handleShowtimeSelection = (showtimeId) => {
        currentShowtimeData = allShowtimesForMovie.find(st => st.showtimeId === showtimeId);
        if (currentShowtimeData) {
            if(selectedShowtimeDetails) selectedShowtimeDetails.innerHTML = `
                Screen: <span class="font-semibold">${currentShowtimeData.screenOrAuditorium}</span> |
                Available Seats: <span class="font-semibold">${currentShowtimeData.availableSeats}</span> / ${currentShowtimeData.totalSeats}
            `;
            generateSeatMap(currentShowtimeData.totalSeats, currentShowtimeData.availableSeats);
            if(step2Section) step2Section.classList.remove('hidden');
            if(step3Section) step3Section.classList.add('hidden');
            selectedSeats = [];
            updateBookingSummary();
        } else {
            if(selectedShowtimeDetails) selectedShowtimeDetails.innerHTML = '';
            if(step2Section) step2Section.classList.add('hidden');
        }
    };

    const generateSeatMap = (totalSeats, availableSeats) => {
        if (!seatMapContainer) return;
        seatMapContainer.innerHTML = '';
        const rows = Math.ceil(totalSeats / 10);
        let seatNumber = 1;
        const occupiedCount = totalSeats - availableSeats;
        let occupiedPlaced = 0;

        for (let i = 0; i < rows; i++) {
            const rowDiv = document.createElement('div');
            rowDiv.className = 'flex justify-center';
            for (let j = 0; j < 10; j++) {
                if (seatNumber <= totalSeats) {
                    const seatDiv = document.createElement('div');
                    seatDiv.classList.add('seat');
                    seatDiv.dataset.seatId = `${String.fromCharCode(65 + i)}${j + 1}`;
                    seatDiv.textContent = seatDiv.dataset.seatId;
                    // This is a placeholder for actual occupied seat data from backend
                    // For now, it just blocks some seats if availableSeats < totalSeats
                    if (occupiedPlaced < occupiedCount && Math.random() < 0.3) { // Simplified random blocking
                         seatDiv.classList.add('occupied');
                         occupiedPlaced++;
                    } else {
                        seatDiv.addEventListener('click', toggleSeatSelection);
                    }
                    rowDiv.appendChild(seatDiv);
                    seatNumber++;
                } else {
                    const gapDiv = document.createElement('div');
                    gapDiv.classList.add('seat', 'gap');
                    rowDiv.appendChild(gapDiv);
                }
            }
            seatMapContainer.appendChild(rowDiv);
        }
    };

    const toggleSeatSelection = (e) => {
        const seat = e.target.closest('.seat');
        if (!seat || seat.classList.contains('occupied') || seat.classList.contains('gap')) return;

        const seatId = seat.dataset.seatId;
        seat.classList.toggle('selected');

        if (seat.classList.contains('selected')) {
            selectedSeats.push(seatId);
        } else {
            selectedSeats = selectedSeats.filter(s => s !== seatId);
        }
        updateBookingSummary();
    };

    const updateBookingSummary = () => {
        if (!currentShowtimeData || !movieSelect || !movieSelect.value) {
            if(step3Section) step3Section.classList.add('hidden');
            return;
        }
        if (selectedSeats.length > 0) {
            if(step3Section) step3Section.classList.remove('hidden');
        } else {
            if(step3Section) step3Section.classList.add('hidden');
        }

        const selectedMovie = allMoviesData.find(m => m.movieId === movieSelect.value);
        if(summaryMovieTitle) summaryMovieTitle.textContent = selectedMovie ? selectedMovie.title : 'N/A';

        const showtimeDateTime = new Date(currentShowtimeData.dateTime).toLocaleString('en-US', { dateStyle: 'medium', timeStyle: 'short' });
        if(summaryShowtimeDateTime) summaryShowtimeDateTime.textContent = showtimeDateTime;
        if(summaryShowtimeScreen) summaryShowtimeScreen.textContent = currentShowtimeData.screenOrAuditorium;
        if(summarySelectedSeats) summarySelectedSeats.textContent = selectedSeats.join(', ') || 'None';
        if(summaryNumTickets) summaryNumTickets.textContent = selectedSeats.length;
        if(summaryTotalPrice) summaryTotalPrice.textContent = (selectedSeats.length * currentMoviePrice).toFixed(2);
    };

    const handleConfirmBooking = async () => {
        const loggedInUser = JSON.parse(localStorage.getItem('loggedInUser'));
        if (!loggedInUser || !loggedInUser.userId) {
            displayFlash('Please log in to confirm your booking.', 'error');
            return;
        }
        if (selectedSeats.length === 0) {
            displayFlash('Please select at least one seat.', 'error');
            return;
        }
        if (!currentShowtimeData || !movieSelect || !movieSelect.value) {
            displayFlash('Please select a movie and showtime.', 'error');
            return;
        }

        const bookingData = {
            userId: loggedInUser.userId,
            showtimeId: currentShowtimeData.showtimeId,
            seatsBooked: selectedSeats,
            numberOfSeats: selectedSeats.length,
            totalPrice: parseFloat(summaryTotalPrice.textContent)
        };

        if(confirmBookingBtn) {
            confirmBookingBtn.disabled = true;
            confirmBookingBtn.innerHTML = '<i class="fas fa-spinner fa-spin mr-2"></i>Processing...';
        }

        try {
            const response = await fetch(`${API_BASE_URL}/bookings`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(bookingData)
            });
            const result = await response.json();
            if (!response.ok) {
                throw new Error(result.message || 'Booking failed.');
            }
            displayFlash('Booking successful! Your Booking ID: ' + result.bookingId, 'success');
            setTimeout(() => {
                window.location.href = `booking_history.html?highlight=${result.bookingId}`;
            }, 3000);
        } catch (error) {
            console.error(error);
            displayFlash(error.message, 'error');
        } finally {
            if(confirmBookingBtn) {
                confirmBookingBtn.disabled = false;
                confirmBookingBtn.innerHTML = 'Confirm & Book Tickets';
            }
        }
    };

    // Event Listeners
    if (movieSelect) movieSelect.addEventListener('change', (e) => {
        const selectedOption = e.target.options[e.target.selectedIndex];
        currentMoviePrice = parseFloat(selectedOption.dataset.price) || 0;
        if(ticketPriceDisplay) ticketPriceDisplay.textContent = `Ticket Price: $${currentMoviePrice.toFixed(2)}`;
        loadShowtimesForMovie(e.target.value);
    });
    if (showtimeSelect) showtimeSelect.addEventListener('change', (e) => handleShowtimeSelection(e.target.value));
    if (confirmBookingBtn) confirmBookingBtn.addEventListener('click', handleConfirmBooking);

    // Initial Page Load
    const mobileMenuButton = document.getElementById('mobile-menu-button');
    const mobileMenu = document.getElementById('mobile-menu');
    if (mobileMenuButton && mobileMenu) {
        mobileMenuButton.addEventListener('click', () => mobileMenu.classList.toggle('hidden'));
    }
    if(document.getElementById('footer-year')) document.getElementById('footer-year').textContent = new Date().getFullYear();

    if(checkLoginStateHeader()) {
        loadMovies();
    }
});
